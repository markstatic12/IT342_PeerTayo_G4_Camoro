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

    @Transactional
    public AuthResponse authenticate(OAuth2User oAuth2User) {
        String email = oAuth2User.getAttribute("email");
        if (email == null) {
            throw new IllegalArgumentException("Email not found in OAuth2 user attributes");
        }

        User user = userRepository.findByEmail(email).orElseGet(() -> {
            String firstName = oAuth2User.getAttribute("given_name");
            String lastName = oAuth2User.getAttribute("family_name");

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
