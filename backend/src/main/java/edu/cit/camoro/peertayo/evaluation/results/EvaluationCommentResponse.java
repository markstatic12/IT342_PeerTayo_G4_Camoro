package edu.cit.camoro.peertayo.evaluation.results;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class EvaluationCommentResponse {
    private String comment;
    private LocalDateTime submittedAt;
}
