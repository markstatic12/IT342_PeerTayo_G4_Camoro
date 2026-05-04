package edu.cit.camoro.peertayo.evaluation.results;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class MyEvaluationResultResponse {
    private Long evaluationId;
    private String title;
    private String createdByName;
    private LocalDateTime submittedAt;
    private Integer responses;
    private Double overallAverage;
    private List<CriterionAverageResponse> criteriaAverages;
    private List<EvaluationCommentResponse> comments;
    private boolean archived;
}
