package edu.cit.camoro.peertayo.evaluation.submission;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CompletedCriterionResponse {
    private Long criteriaId;
    private String criteriaName;
    private String criteriaDescription;
    private Integer rating;
}
