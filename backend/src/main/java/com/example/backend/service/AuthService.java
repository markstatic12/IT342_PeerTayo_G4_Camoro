package com.example.backend.service;

import com.example.backend.dto.request.GoogleAuthRequest;
import com.example.backend.dto.request.LoginRequest;
import com.example.backend.dto.request.RegisterRequest;
import com.example.backend.dto.response.AuthResponse;
import com.example.backend.dto.response.UserResponse;
import com.example.backend.entity.EProvider;
import com.example.backend.entity.ERole;
import com.example.backend.entity.Role;
import com.example.backend.entity.User;
import com.example.backend.exception.DuplicateEntryException;
import com.example.backend.exception.ResourceNotFoundException;
import com.example.backend.repository.RoleRepository;
import com.example.backend.repository.UserRepository;
import com.example.backend.security.JwtService;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final EmailService emailService;

    @Value("${app.google.client-id}")
    private String googleClientId;

    public AuthService(UserRepository userRepository, RoleRepository roleRepository,
                       PasswordEncoder passwordEncoder, JwtService jwtService,
                       AuthenticationManager authenticationManager,
                       UserDetailsService userDetailsService, EmailService emailService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.emailService = emailService;
    }

    // ── Register ─────────────────────────────────────────────────────────────

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
                .provider(EProvider.LOCAL)
                .build();

        user.getRoles().add(respondentRole);
        userRepository.save(user);

        // Async welcome email — does not block response
        emailService.sendWelcomeEmail(user.getEmail(), user.getFirstName());

        String token = generateTokenForEmail(user.getEmail());
        return buildAuthResponse(user, token);
    }

    // ── Login ────────────────────────────────────────────────────────────────

    public AuthResponse login(LoginRequest request) {
        // Spring Security validates credentials; throws BadCredentialsException on failure
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        String token = generateTokenForEmail(user.getEmail());
        return buildAuthResponse(user, token);
    }

    // ── Google OAuth ─────────────────────────────────────────────────────────

    @Transactional
    public AuthResponse googleAuth(GoogleAuthRequest request) {
        GoogleIdToken.Payload payload = verifyGoogleToken(request.getGoogleToken());

        String email = payload.getEmail();
        String firstName = (String) payload.get("given_name");
        String lastName = (String) payload.get("family_name");

        User user = userRepository.findByEmail(email)
                .orElseGet(() -> createGoogleUser(email, firstName, lastName));

        String token = generateTokenForEmail(user.getEmail());
        return buildAuthResponse(user, token);
    }

    // ── Get Current User ─────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public UserResponse getCurrentUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return toUserResponse(user);
    }

    // ── Internal helpers ─────────────────────────────────────────────────────

    private User createGoogleUser(String email, String firstName, String lastName) {
        Role respondentRole = getOrCreateRole(ERole.RESPONDENT);

        User user = User.builder()
                .firstName(firstName != null ? firstName : "")
                .lastName(lastName != null ? lastName : "")
                .email(email)
                .provider(EProvider.GOOGLE)
                .build();

        user.getRoles().add(respondentRole);
        userRepository.save(user);

        emailService.sendWelcomeEmail(email, user.getFirstName());
        log.info("New Google OAuth user registered: {}", email);
        return user;
    }

    private GoogleIdToken.Payload verifyGoogleToken(String idTokenString) {
        try {
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                    new NetHttpTransport(), GsonFactory.getDefaultInstance())
                    .setAudience(Collections.singletonList(googleClientId))
                    .build();

            GoogleIdToken idToken = verifier.verify(idTokenString);
            if (idToken == null) {
                throw new IllegalArgumentException("Invalid Google ID token");
            }
            return idToken.getPayload();
        } catch (GeneralSecurityException | IOException e) {
            log.error("Google token verification failed: {}", e.getMessage());
            throw new IllegalArgumentException("Failed to verify Google token: " + e.getMessage());
        }
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
