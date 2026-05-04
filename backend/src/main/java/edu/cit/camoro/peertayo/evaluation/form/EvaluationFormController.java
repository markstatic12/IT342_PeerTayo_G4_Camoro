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
    @PreAuthorize("hasRole('FACILITATOR')")
    public ResponseEntity<ApiResponse<Map<String, CreatedEvaluationResponse>>> create(
            @Valid @RequestBody CreateEvaluationRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        CreatedEvaluationResponse response = evaluationFormService.create(request, userDetails.getUsername());
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
}
