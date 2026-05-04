package edu.cit.camoro.peertayo.evaluation.results;

import edu.cit.camoro.peertayo.shared.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/evaluations")
@RequiredArgsConstructor
public class ResultsController {

    private final ResultsService resultsService;

    @GetMapping("/my-results")
    public ResponseEntity<ApiResponse<Map<String, MyResultsResponse>>> getMyResults(
            @AuthenticationPrincipal UserDetails userDetails) {
        MyResultsResponse results = resultsService.getMyResults(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok(Map.of("results", results)));
    }

    @PostMapping("/my-results/{evaluationId}/archive")
    public ResponseEntity<ApiResponse<Map<String, String>>> archiveMyResults(
            @PathVariable Long evaluationId,
            @AuthenticationPrincipal UserDetails userDetails) {
        resultsService.setMyResultsArchived(evaluationId, userDetails.getUsername(), true);
        return ResponseEntity.ok(ApiResponse.ok(Map.of("message", "Evaluation archived")));
    }

    @PostMapping("/my-results/{evaluationId}/unarchive")
    public ResponseEntity<ApiResponse<Map<String, String>>> unarchiveMyResults(
            @PathVariable Long evaluationId,
            @AuthenticationPrincipal UserDetails userDetails) {
        resultsService.setMyResultsArchived(evaluationId, userDetails.getUsername(), false);
        return ResponseEntity.ok(ApiResponse.ok(Map.of("message", "Evaluation unarchived")));
    }

    @GetMapping("/{id}/results")
    @PreAuthorize("hasRole('FACILITATOR')")
    public ResponseEntity<ApiResponse<EvaluationResultsResponse>> getEvaluationResults(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        EvaluationResultsResponse response = resultsService.getEvaluationResults(id, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
