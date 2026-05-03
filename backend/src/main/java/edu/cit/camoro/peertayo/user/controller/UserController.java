package edu.cit.camoro.peertayo.user.controller;

import edu.cit.camoro.peertayo.auth.entity.User;
import edu.cit.camoro.peertayo.auth.dto.response.UserResponse;
import edu.cit.camoro.peertayo.auth.repository.UserRepository;
import edu.cit.camoro.peertayo.shared.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;

    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, List<UserResponse>>>> list(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(name = "q", required = false) String query) {

        String normalizedQuery = query == null ? "" : query.trim();

        List<User> matches = normalizedQuery.isEmpty()
                ? userRepository.findTop20ByOrderByFirstNameAsc()
                : userRepository
                .findTop20ByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCaseOrEmailContainingIgnoreCaseOrderByFirstNameAsc(
                        normalizedQuery, normalizedQuery, normalizedQuery);

        List<UserResponse> users = matches.stream()
                .map(this::toResponse)
                .toList();
        return ResponseEntity.ok(ApiResponse.ok(Map.of("users", users)));
    }

    private UserResponse toResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .roles(user.getRoles().stream().map(r -> r.getName().name()).toList())
                .build();
    }
}
