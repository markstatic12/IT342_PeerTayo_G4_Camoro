package edu.cit.camoro.peertayo.evaluation.form;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class CreateEvaluationRequest {

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Description is required")
    private String description;

    @NotNull(message = "Deadline is required")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm[:ss]")
    private LocalDateTime deadline;

    @NotEmpty(message = "At least one evaluatee is required")
    private List<Long> evaluateeIds;

    @NotEmpty(message = "At least one evaluator is required")
    private List<Long> evaluatorIds;
}
