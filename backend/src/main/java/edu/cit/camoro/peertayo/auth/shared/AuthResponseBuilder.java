package edu.cit.camoro.peertayo.auth.shared;

import edu.cit.camoro.peertayo.auth.entity.User;
import edu.cit.camoro.peertayo.auth.security.JwtService;
import edu.cit.camoro.peertayo.auth.token.AuthResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Shared helper used by all auth sub-slices to build a consistent
 * AuthResponse (user + fresh JWT) from a User entity.
 */
@Component
@RequiredArgsConstructor
public class AuthResponseBuilder {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    public AuthResponse buildForUser(User user) {
        String token = jwtService.generateToken(
                userDetailsService.loadUserByUsername(user.getEmail()));
        return AuthResponse.builder()
                .user(toUserResponse(user))
                .token(token)
                .build();
    }

    public UserResponse toUserResponse(User user) {
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
