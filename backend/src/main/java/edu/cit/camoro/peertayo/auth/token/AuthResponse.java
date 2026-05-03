package edu.cit.camoro.peertayo.auth.token;

import edu.cit.camoro.peertayo.auth.shared.UserResponse;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {
    private UserResponse user;
    private String token;
}
