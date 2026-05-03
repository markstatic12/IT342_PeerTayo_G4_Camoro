package edu.cit.camoro.peertayo.auth.token;

import edu.cit.camoro.peertayo.auth.security.JwtService;
import edu.cit.camoro.peertayo.auth.security.TokenBlacklistService;
import edu.cit.camoro.peertayo.auth.shared.UserResponse;
import edu.cit.camoro.peertayo.shared.response.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class TokenController {

    private final TokenService tokenService;
    private final JwtService jwtService;
    private final TokenBlacklistService tokenBlacklistService;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<Map<String, UserResponse>>> getCurrentUser(
            @AuthenticationPrincipal UserDetails userDetails) {
        UserResponse userResponse = tokenService.getCurrentUser(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok(Map.of("user", userResponse)));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponse>> refresh(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.ok(tokenService.refresh(userDetails.getUsername())));
    }

    @PostMapping("/promote-to-facilitator")
    public ResponseEntity<ApiResponse<AuthResponse>> promoteToFacilitator(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.ok(
                tokenService.promoteToFacilitator(userDetails.getUsername())));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (StringUtils.hasText(header) && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            try {
                long expiryMs = jwtService.extractExpiration(token).getTime();
                tokenBlacklistService.blacklist(token, expiryMs);
            } catch (Exception ignored) {
                // Token malformed or already expired — nothing to blacklist
            }
        }
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}
