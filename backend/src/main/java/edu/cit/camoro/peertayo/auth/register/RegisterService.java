package edu.cit.camoro.peertayo.auth.register;

import edu.cit.camoro.peertayo.auth.entity.ERole;
import edu.cit.camoro.peertayo.auth.entity.Role;
import edu.cit.camoro.peertayo.auth.entity.User;
import edu.cit.camoro.peertayo.auth.repository.RoleRepository;
import edu.cit.camoro.peertayo.auth.repository.UserRepository;
import edu.cit.camoro.peertayo.auth.shared.AuthResponseBuilder;
import edu.cit.camoro.peertayo.auth.token.AuthResponse;
import edu.cit.camoro.peertayo.shared.exception.DuplicateEntryException;
import edu.cit.camoro.peertayo.notification.entity.NotificationType;
import edu.cit.camoro.peertayo.notification.shared.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RegisterService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthResponseBuilder authResponseBuilder;
    private final NotificationService notificationService;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateEntryException(
                    "An account with email '" + request.getEmail() + "' already exists");
        }

        Role respondentRole = roleRepository.findByName(ERole.RESPONDENT)
                .orElseGet(() -> roleRepository.save(new Role(null, ERole.RESPONDENT)));

        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .provider("LOCAL")
                .build();

        user.getRoles().add(respondentRole);
        User savedUser = userRepository.save(user);

        // Trigger Welcome Notification (and Gmail email)
        notificationService.send(
            savedUser, 
            "Welcome to PeerTayo! Your account has been successfully created. We're excited to have you on board.", 
            NotificationType.WELCOME
        );

        return authResponseBuilder.buildForUser(savedUser);
    }
}
