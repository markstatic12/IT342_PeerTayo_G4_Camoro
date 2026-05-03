package edu.cit.camoro.peertayo.evaluation.submission;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class PendingEvaluationResponse {
    private Long id;
    private Long assignmentId;
    private String title;
    private LocalDateTime deadline;
    private String evaluateeName;
}
