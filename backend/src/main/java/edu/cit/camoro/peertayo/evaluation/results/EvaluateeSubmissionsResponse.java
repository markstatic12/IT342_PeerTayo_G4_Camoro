package edu.cit.camoro.peertayo.evaluation.results;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class EvaluateeSubmissionsResponse {
    private Long evaluateeId;
    private String evaluateeName;
    private Integer submittedCount;
    private Integer totalCount;
    private Double overallAverage;
    private List<EvaluatorSubmissionResponse> evaluators;
}
