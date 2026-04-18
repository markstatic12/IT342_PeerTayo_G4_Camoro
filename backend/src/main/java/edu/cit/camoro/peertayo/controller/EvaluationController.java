package edu.cit.camoro.peertayo.controller;

import edu.cit.camoro.peertayo.dto.request.CreateEvaluationRequest;
import edu.cit.camoro.peertayo.dto.request.SubmitEvaluationRequest;
import edu.cit.camoro.peertayo.dto.response.*;
import edu.cit.camoro.peertayo.service.EvaluationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<ApiResponse<Map<String, List<CreatedEvaluationListItemResponse>>>> getCreated(
            @AuthenticationPrincipal UserDetails userDetails) {
        List<CreatedEvaluationListItemResponse> data = evaluationService.getCreatedEvaluations(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok(Map.of("evaluations", data)));
    }

    @GetMapping("/{id}/results")
    public ResponseEntity<ApiResponse<EvaluationResultsResponse>> getEvaluationResults(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        EvaluationResultsResponse response = evaluationService.getEvaluationResults(id, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
