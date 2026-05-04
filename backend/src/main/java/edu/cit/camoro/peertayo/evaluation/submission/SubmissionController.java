package edu.cit.camoro.peertayo.evaluation.submission;

import edu.cit.camoro.peertayo.shared.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/evaluations")
@RequiredArgsConstructor
public class SubmissionController {

    private final SubmissionService submissionService;

    @GetMapping("/pending")
    public ResponseEntity<ApiResponse<Map<String, List<PendingEvaluationResponse>>>> getPending(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(name = "archived", defaultValue = "false") boolean archived) {
        List<PendingEvaluationResponse> data = submissionService.getPending(userDetails.getUsername(), archived);
        return ResponseEntity.ok(ApiResponse.ok(Map.of("evaluations", data)));
    }

    @PostMapping("/pending/{evaluationId}/archive")
    public ResponseEntity<ApiResponse<Map<String, String>>> archivePending(
            @PathVariable Long evaluationId,
            @AuthenticationPrincipal UserDetails userDetails) {
        submissionService.setArchivedForEvaluation(evaluationId, userDetails.getUsername(), true);
        return ResponseEntity.ok(ApiResponse.ok(Map.of("message", "Evaluation archived")));
    }

    @PostMapping("/pending/{evaluationId}/unarchive")
    public ResponseEntity<ApiResponse<Map<String, String>>> unarchivePending(
            @PathVariable Long evaluationId,
            @AuthenticationPrincipal UserDetails userDetails) {
        submissionService.setArchivedForEvaluation(evaluationId, userDetails.getUsername(), false);
        return ResponseEntity.ok(ApiResponse.ok(Map.of("message", "Evaluation unarchived")));
    }

    @GetMapping("/submitted/summary")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getSubmittedSummary(
            @AuthenticationPrincipal UserDetails userDetails) {
        long submittedThisMonth = submissionService.getSubmittedThisMonthCount(userDetails.getUsername());
        long totalSubmitted = submissionService.getSubmittedCount(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok(Map.of(
            "submittedThisMonth", submittedThisMonth,
            "totalSubmitted", totalSubmitted
        )));
    }

    @PostMapping("/{id}/submit")
    public ResponseEntity<ApiResponse<Map<String, String>>> submit(
            @PathVariable Long id,
            @Valid @RequestBody SubmitEvaluationRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        submissionService.submit(id, userDetails.getUsername(), request);
        return ResponseEntity.ok(ApiResponse.ok(Map.of("message", "Evaluation submitted successfully")));
    }
}
