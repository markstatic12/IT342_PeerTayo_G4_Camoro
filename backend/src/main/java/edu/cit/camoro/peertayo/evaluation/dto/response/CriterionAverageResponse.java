package edu.cit.camoro.peertayo.evaluation.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CriterionAverageResponse {
    private Long criteriaId;
    private Double average;
}
