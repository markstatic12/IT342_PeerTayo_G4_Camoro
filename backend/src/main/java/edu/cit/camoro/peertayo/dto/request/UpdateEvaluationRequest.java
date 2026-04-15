package edu.cit.camoro.peertayo.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class UpdateEvaluationRequest {

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Description is required")
    private String description;

    @NotNull(message = "Deadline is required")
    private LocalDateTime deadline;

    private String status;

    private List<String> criteria;

    private List<String> questions;

    private List<String> ratingFields;
}
