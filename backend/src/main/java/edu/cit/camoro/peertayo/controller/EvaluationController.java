package edu.cit.camoro.peertayo.controller;

import edu.cit.camoro.peertayo.dto.request.CreateEvaluationRequest;
import edu.cit.camoro.peertayo.dto.request.UpdateEvaluationRequest;
import edu.cit.camoro.peertayo.dto.response.ApiResponse;
import edu.cit.camoro.peertayo.dto.response.EvaluationResponse;
import edu.cit.camoro.peertayo.service.EvaluationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/evaluations")
@RequiredArgsConstructor
public class EvaluationController {

    private final EvaluationService evaluationService;

    @PostMapping
    public ResponseEntity<ApiResponse<EvaluationResponse>> create(
            @Valid @RequestBody CreateEvaluationRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        EvaluationResponse response = evaluationService.create(request, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<EvaluationResponse>>> list(
            @AuthenticationPrincipal UserDetails userDetails) {
        List<EvaluationResponse> responses = evaluationService.findAllByCreator(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok(responses));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<EvaluationResponse>> getById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        EvaluationResponse response = evaluationService.findById(id, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<EvaluationResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateEvaluationRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        EvaluationResponse response = evaluationService.update(id, request, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        evaluationService.delete(id, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}
