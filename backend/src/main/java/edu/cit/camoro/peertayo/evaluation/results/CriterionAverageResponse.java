package edu.cit.camoro.peertayo.evaluation.results;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CriterionAverageResponse {
    private Long criteriaId;
    private String criteriaName;
    private Double average;
}
