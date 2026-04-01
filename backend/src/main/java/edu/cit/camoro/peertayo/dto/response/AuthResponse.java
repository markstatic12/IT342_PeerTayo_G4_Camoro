package edu.cit.camoro.peertayo.dto.response;

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
