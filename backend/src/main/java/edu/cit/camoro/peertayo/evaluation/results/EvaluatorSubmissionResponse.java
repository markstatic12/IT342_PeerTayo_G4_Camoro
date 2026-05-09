package edu.cit.camoro.peertayo.evaluation.results;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Per-evaluator submission detail for a specific evaluatee within an evaluation.
 * Used by the facilitator's "View Results" screen.
 */
@Data
@Builder
public class EvaluatorSubmissionResponse {
    private Long evaluatorId;
    private String evaluatorName;
    private Double overallAverage;
    private boolean submitted;
    private LocalDateTime submittedAt;
    private String comment;
    private List<CriterionRatingResponse> criteriaRatings;
}
