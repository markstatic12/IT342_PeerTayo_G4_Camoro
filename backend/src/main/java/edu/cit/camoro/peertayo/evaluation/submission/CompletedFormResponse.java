package edu.cit.camoro.peertayo.evaluation.submission;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class CompletedFormResponse {
    private Long evaluationId;
    private String title;
    private String creatorName;
    private LocalDateTime submittedAt;
    private Integer totalEvaluatees;
    private boolean archived;
    private List<CompletedEvaluateeResponse> evaluatees;
}
