package edu.cit.camoro.peertayo.auth.oauth2;

import edu.cit.camoro.peertayo.auth.entity.ERole;
import edu.cit.camoro.peertayo.auth.entity.Role;
import edu.cit.camoro.peertayo.auth.entity.User;
import edu.cit.camoro.peertayo.auth.repository.RoleRepository;
import edu.cit.camoro.peertayo.auth.repository.UserRepository;
import edu.cit.camoro.peertayo.auth.shared.AuthResponseBuilder;
import edu.cit.camoro.peertayo.auth.token.AuthResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GoogleOAuth2Service {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final AuthResponseBuilder authResponseBuilder;

    @org.springframework.beans.factory.annotation.Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String googleClientId;

    /**
     * Authenticates a user from a Spring Security OAuth2User (Web Flow).
     */
    @Transactional
    public AuthResponse authenticate(OAuth2User oAuth2User) {
        String email = oAuth2User.getAttribute("email");
        String firstName = oAuth2User.getAttribute("given_name");
        String lastName = oAuth2User.getAttribute("family_name");
        return processAuthentication(email, firstName, lastName);
    }

    /**
     * Authenticates a user from a Google ID Token (Mobile Flow).
     */
    @Transactional
    public AuthResponse authenticateWithIdToken(String idToken) {
        try {
            com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier verifier = 
                new com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier.Builder(
                    new com.google.api.client.http.javanet.NetHttpTransport(),
                    new com.google.api.client.json.gson.GsonFactory()
                )
                .setAudience(java.util.Collections.singletonList(googleClientId))
                .build();

            com.google.api.client.googleapis.auth.oauth2.GoogleIdToken token = verifier.verify(idToken);
            if (token == null) {
                throw new IllegalArgumentException("Invalid Google ID Token");
            }

            com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload payload = token.getPayload();
            String email = payload.getEmail();
            String firstName = (String) payload.get("given_name");
            String lastName = (String) payload.get("family_name");

            return processAuthentication(email, firstName, lastName);

        } catch (Exception e) {
            throw new RuntimeException("Google authentication failed: " + e.getMessage());
        }
    }

    private AuthResponse processAuthentication(String email, String firstName, String lastName) {
        if (email == null) {
            throw new IllegalArgumentException("Email not found in Google attributes");
        }

        User user = userRepository.findByEmail(email).orElseGet(() -> {
            Role respondentRole = roleRepository.findByName(ERole.RESPONDENT)
                    .orElseGet(() -> roleRepository.save(new Role(null, ERole.RESPONDENT)));

            User newUser = User.builder()
                    .firstName(firstName != null ? firstName : "")
                    .lastName(lastName != null ? lastName : "")
                    .email(email)
                    .provider("GOOGLE")
                    .build();
            newUser.getRoles().add(respondentRole);
            return userRepository.save(newUser);
        });

        return authResponseBuilder.buildForUser(user);
    }
}
