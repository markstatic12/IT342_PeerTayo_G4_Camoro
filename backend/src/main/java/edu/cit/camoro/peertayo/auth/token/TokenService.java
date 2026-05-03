package edu.cit.camoro.peertayo.auth.token;

import edu.cit.camoro.peertayo.auth.entity.ERole;
import edu.cit.camoro.peertayo.auth.entity.Role;
import edu.cit.camoro.peertayo.auth.entity.User;
import edu.cit.camoro.peertayo.auth.repository.RoleRepository;
import edu.cit.camoro.peertayo.auth.repository.UserRepository;
import edu.cit.camoro.peertayo.auth.shared.AuthResponseBuilder;
import edu.cit.camoro.peertayo.auth.shared.UserResponse;
import edu.cit.camoro.peertayo.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TokenService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final AuthResponseBuilder authResponseBuilder;

    @Transactional(readOnly = true)
    public AuthResponse refresh(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return authResponseBuilder.buildForUser(user);
    }

    @Transactional
    public AuthResponse promoteToFacilitator(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        boolean alreadyFacilitator = user.getRoles().stream()
                .anyMatch(r -> r.getName() == ERole.FACILITATOR);

        if (!alreadyFacilitator) {
            Role facilitatorRole = roleRepository.findByName(ERole.FACILITATOR)
                    .orElseGet(() -> roleRepository.save(new Role(null, ERole.FACILITATOR)));
            user.getRoles().add(facilitatorRole);
            userRepository.save(user);
        }
        return authResponseBuilder.buildForUser(user);
    }

    @Transactional(readOnly = true)
    public UserResponse getCurrentUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return authResponseBuilder.toUserResponse(user);
    }
}
