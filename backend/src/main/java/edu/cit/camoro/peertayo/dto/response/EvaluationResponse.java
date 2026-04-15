package edu.cit.camoro.peertayo.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class EvaluationResponse {
    private Long id;
    private String title;
    private String description;
    private LocalDateTime deadline;
    private String status;
    private List<String> criteria;
    private List<String> questions;
    private List<String> ratingFields;
    private String createdByEmail;
    private String createdByName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
