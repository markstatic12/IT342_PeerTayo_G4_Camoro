package edu.cit.camoro.peertayo.evaluation.controller;

import edu.cit.camoro.peertayo.evaluation.dto.request.CreateEvaluationRequest;
import edu.cit.camoro.peertayo.evaluation.dto.request.SubmitEvaluationRequest;
import edu.cit.camoro.peertayo.evaluation.dto.request.UpdateEvaluationRequest;
import edu.cit.camoro.peertayo.evaluation.dto.response.*;
import edu.cit.camoro.peertayo.evaluation.service.EvaluationService;
import edu.cit.camoro.peertayo.shared.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/evaluations")
@RequiredArgsConstructor
public class EvaluationController {

    private final EvaluationService evaluationService;

    @PostMapping
    @PreAuthorize("hasRole('FACILITATOR')")
    public ResponseEntity<ApiResponse<Map<String, CreatedEvaluationResponse>>> create(
            @Valid @RequestBody CreateEvaluationRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        CreatedEvaluationResponse response = evaluationService.create(request, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(Map.of("evaluation", response)));
    }

    @GetMapping("/pending")
    public ResponseEntity<ApiResponse<Map<String, List<PendingEvaluationResponse>>>> getPending(
            @AuthenticationPrincipal UserDetails userDetails) {
        List<PendingEvaluationResponse> data = evaluationService.getPendingEvaluations(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok(Map.of("evaluations", data)));
    }

    @PostMapping("/{id}/submit")
    public ResponseEntity<ApiResponse<Map<String, String>>> submit(
            @PathVariable Long id,
            @Valid @RequestBody SubmitEvaluationRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        evaluationService.submitEvaluation(id, userDetails.getUsername(), request);
        return ResponseEntity.ok(ApiResponse.ok(Map.of("message", "Evaluation submitted successfully")));
    }

    @GetMapping("/my-results")
    public ResponseEntity<ApiResponse<Map<String, MyResultsResponse>>> getMyResults(
            @AuthenticationPrincipal UserDetails userDetails) {
        MyResultsResponse results = evaluationService.getMyResults(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok(Map.of("results", results)));
    }

    @GetMapping({"", "/created"})
    @PreAuthorize("hasRole('FACILITATOR')")
    public ResponseEntity<ApiResponse<Map<String, List<CreatedEvaluationListItemResponse>>>> getCreated(
            @AuthenticationPrincipal UserDetails userDetails) {
        List<CreatedEvaluationListItemResponse> data = evaluationService.getCreatedEvaluations(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok(Map.of("evaluations", data)));
    }

    @GetMapping("/{id}/results")
    @PreAuthorize("hasRole('FACILITATOR')")
    public ResponseEntity<ApiResponse<EvaluationResultsResponse>> getEvaluationResults(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        EvaluationResultsResponse response = evaluationService.getEvaluationResults(id, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('FACILITATOR')")
    public ResponseEntity<ApiResponse<Map<String, CreatedEvaluationListItemResponse>>> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateEvaluationRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        CreatedEvaluationListItemResponse updated = evaluationService.updateEvaluation(id, request, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok(Map.of("evaluation", updated)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('FACILITATOR')")
    public ResponseEntity<ApiResponse<Map<String, String>>> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        evaluationService.deleteEvaluation(id, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok(Map.of("message", "Evaluation deleted successfully")));
    }
}
