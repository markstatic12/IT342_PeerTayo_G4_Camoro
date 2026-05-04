package edu.cit.camoro.peertayo.evaluation.submission;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class CompletedEvaluateeResponse {
    private Long assignmentId;
    private String evaluateeName;
    private LocalDateTime submittedAt;
    private String comment;
    private List<CompletedCriterionResponse> criteria;
}
