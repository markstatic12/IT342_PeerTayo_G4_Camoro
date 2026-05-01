package edu.cit.camoro.peertayo.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class CreatedEvaluationListItemResponse {
    private Long id;
    private String title;
    private LocalDateTime deadline;
    private LocalDateTime createdAt;
    private String submissionProgress;
    private String status;
    private String description;
}
