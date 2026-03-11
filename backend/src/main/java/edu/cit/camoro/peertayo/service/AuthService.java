package edu.cit.camoro.peertayo.service;

import edu.cit.camoro.peertayo.dto.request.LoginRequest;
import edu.cit.camoro.peertayo.dto.request.RegisterRequest;
import edu.cit.camoro.peertayo.dto.response.AuthResponse;
import edu.cit.camoro.peertayo.dto.response.UserResponse;
import edu.cit.camoro.peertayo.entity.ERole;
import edu.cit.camoro.peertayo.entity.Role;
import edu.cit.camoro.peertayo.entity.User;
import edu.cit.camoro.peertayo.exception.DuplicateEntryException;
import edu.cit.camoro.peertayo.exception.ResourceNotFoundException;
import edu.cit.camoro.peertayo.repository.RoleRepository;
import edu.cit.camoro.peertayo.repository.UserRepository;
import edu.cit.camoro.peertayo.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateEntryException(
                    "An account with email '" + request.getEmail() + "' already exists");
        }

        Role respondentRole = getOrCreateRole(ERole.RESPONDENT);

        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .build();

        user.getRoles().add(respondentRole);
        userRepository.save(user);

        String token = generateTokenForEmail(user.getEmail());
        return buildAuthResponse(user, token);
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (user.getPasswordHash() == null || !passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new BadCredentialsException("Invalid email or password");
        }

        String token = generateTokenForEmail(user.getEmail());
        return buildAuthResponse(user, token);
    }

    @Transactional
    public AuthResponse authenticateWithGoogleOAuth2User(OAuth2User oAuth2User) {
        String email = oAuth2User.getAttribute("email");
        if (email == null) {
            throw new IllegalArgumentException("Email not found in OAuth2 user attributes");
        }

        User user = userRepository.findByEmail(email).orElseGet(() -> {
            String firstName = oAuth2User.getAttribute("given_name");
            String lastName = oAuth2User.getAttribute("family_name");

            Role respondentRole = getOrCreateRole(ERole.RESPONDENT);

            User newUser = User.builder()
                    .firstName(firstName != null ? firstName : "")
                    .lastName(lastName != null ? lastName : "")
                    .email(email)
                    .build();
            newUser.getRoles().add(respondentRole);
            return userRepository.save(newUser);
        });

        String token = generateTokenForEmail(user.getEmail());
        return buildAuthResponse(user, token);
    }

    @Transactional(readOnly = true)
    public UserResponse getCurrentUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return toUserResponse(user);
    }

    private Role getOrCreateRole(ERole roleName) {
        return roleRepository.findByName(roleName)
                .orElseGet(() -> roleRepository.save(new Role(null, roleName)));
    }

    private String generateTokenForEmail(String email) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(email);
        return jwtService.generateToken(userDetails);
    }

    private AuthResponse buildAuthResponse(User user, String token) {
        return AuthResponse.builder()
                .user(toUserResponse(user))
                .token(token)
                .build();
    }

    private UserResponse toUserResponse(User user) {
        List<String> roleNames = user.getRoles().stream()
                .map(role -> role.getName().name())
                .collect(Collectors.toList());

        return UserResponse.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .roles(roleNames)
                .build();
    }
}
