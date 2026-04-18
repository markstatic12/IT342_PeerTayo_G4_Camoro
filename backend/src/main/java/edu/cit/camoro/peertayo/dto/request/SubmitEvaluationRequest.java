package edu.cit.camoro.peertayo.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class SubmitEvaluationRequest {

    @NotEmpty(message = "Responses are required")
    @Valid
    private List<ResponseItem> responses;

    private String comment;

    @Data
    public static class ResponseItem {
        @NotNull(message = "criteriaId is required")
        private Long criteriaId;

        @NotNull(message = "rating is required")
        @Min(value = 1, message = "rating must be between 1 and 5")
        @Max(value = 5, message = "rating must be between 1 and 5")
        private Integer rating;
    }
}
