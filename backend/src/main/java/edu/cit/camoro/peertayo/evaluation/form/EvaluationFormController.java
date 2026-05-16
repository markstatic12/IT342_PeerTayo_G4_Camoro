package edu.cit.camoro.peertayo.evaluation.form;

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
public class EvaluationFormController {

    private final EvaluationFormService evaluationFormService;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Map<String, CreatedEvaluationResponse>>> create(
            @Valid @RequestBody CreateEvaluationRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        CreatedEvaluationResponse response = evaluationFormService.create(request, userDetails.getUsername());
        // Fire notifications in a separate transaction after the evaluation is committed
        evaluationFormService.sendAssignmentNotificationsById(request.getEvaluatorIds(), response.getTitle());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(Map.of("evaluation", response)));
    }

    @GetMapping({"", "/created"})
    @PreAuthorize("hasRole('FACILITATOR')")
    public ResponseEntity<ApiResponse<Map<String, List<CreatedEvaluationListItemResponse>>>> getCreated(
            @AuthenticationPrincipal UserDetails userDetails) {
        List<CreatedEvaluationListItemResponse> data = evaluationFormService.getCreated(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok(Map.of("evaluations", data)));
    }

    @GetMapping("/{id}/participants")
    @PreAuthorize("hasRole('FACILITATOR')")
    public ResponseEntity<ApiResponse<EvaluationParticipantsResponse>> getParticipants(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        EvaluationParticipantsResponse response = evaluationFormService.getParticipants(id, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('FACILITATOR')")
    public ResponseEntity<ApiResponse<Map<String, CreatedEvaluationListItemResponse>>> getOne(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        CreatedEvaluationListItemResponse data = evaluationFormService.getById(id, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok(Map.of("evaluation", data)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('FACILITATOR')")
    public ResponseEntity<ApiResponse<Map<String, CreatedEvaluationListItemResponse>>> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateEvaluationRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        CreatedEvaluationListItemResponse updated = evaluationFormService.update(id, request, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok(Map.of("evaluation", updated)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('FACILITATOR')")
    public ResponseEntity<ApiResponse<Map<String, String>>> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        evaluationFormService.delete(id, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok(Map.of("message", "Evaluation deleted successfully")));
    }

    @PostMapping("/{id}/extend-deadline")
    @PreAuthorize("hasRole('FACILITATOR')")
    public ResponseEntity<ApiResponse<Map<String, String>>> extendDeadline(
            @PathVariable Long id,
            @RequestBody Map<String, String> payload,
            @AuthenticationPrincipal UserDetails userDetails) {
        String newDeadlineStr = payload.get("newDeadline");
        if (newDeadlineStr == null) {
            throw new IllegalArgumentException("newDeadline is required");
        }
        // Handle ISO-8601 strings with timezone offsets (like 'Z') by parsing as ZonedDateTime first
        java.time.LocalDateTime newDeadline;
        try {
            if (newDeadlineStr.contains("Z") || newDeadlineStr.contains("+")) {
                // Convert UTC/Offset time to the Server's Local Time
                newDeadline = java.time.ZonedDateTime.parse(newDeadlineStr)
                        .withZoneSameInstant(java.time.ZoneId.systemDefault())
                        .toLocalDateTime();
            } else {
                newDeadline = java.time.LocalDateTime.parse(newDeadlineStr);
            }
        } catch (Exception e) {
            newDeadline = java.time.OffsetDateTime.parse(newDeadlineStr)
                    .atZoneSameInstant(java.time.ZoneId.systemDefault())
                    .toLocalDateTime();
        }
        evaluationFormService.extendDeadline(id, newDeadline, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok(Map.of("message", "Deadline extended successfully")));
    }

    @PostMapping("/{id}/close-permanently")
    @PreAuthorize("hasRole('FACILITATOR')")
    public ResponseEntity<ApiResponse<Map<String, String>>> closePermanently(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        evaluationFormService.closePermanently(id, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok(Map.of("message", "Evaluation closed permanently")));
    }
}
