package edu.cit.camoro.peertayo.auth.controller;

import edu.cit.camoro.peertayo.auth.dto.request.LoginRequest;
import edu.cit.camoro.peertayo.auth.dto.request.RegisterRequest;
import edu.cit.camoro.peertayo.auth.dto.response.AuthResponse;
import edu.cit.camoro.peertayo.auth.dto.response.UserResponse;
import edu.cit.camoro.peertayo.auth.security.JwtService;
import edu.cit.camoro.peertayo.auth.security.TokenBlacklistService;
import edu.cit.camoro.peertayo.auth.service.AuthService;
import edu.cit.camoro.peertayo.shared.response.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final JwtService jwtService;
    private final TokenBlacklistService tokenBlacklistService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(
            @Valid @RequestBody RegisterRequest request) {
        AuthResponse authResponse = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(authResponse));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request) {
        AuthResponse authResponse = authService.login(request);
        return ResponseEntity.ok(ApiResponse.ok(authResponse));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<Map<String, UserResponse>>> getCurrentUser(
            @AuthenticationPrincipal UserDetails userDetails) {
        UserResponse userResponse = authService.getCurrentUser(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok(Map.of("user", userResponse)));
    }

    @PostMapping("/promote-to-facilitator")
    public ResponseEntity<ApiResponse<AuthResponse>> promoteToFacilitator(
            @AuthenticationPrincipal UserDetails userDetails) {
        AuthResponse authResponse = authService.promoteToFacilitator(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok(authResponse));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponse>> refresh(
            @AuthenticationPrincipal UserDetails userDetails) {
        AuthResponse authResponse = authService.refreshSession(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok(authResponse));
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
                // Token is malformed or already expired — nothing to blacklist
            }
        }
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}
