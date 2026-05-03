package edu.cit.camoro.peertayo.evaluation.form;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class CreatedEvaluationResponse {
    private Long id;
    private String title;
    private LocalDateTime deadline;
    private Long createdBy;
    private String status;
}
