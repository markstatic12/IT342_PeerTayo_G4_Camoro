package edu.cit.camoro.peertayo.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class EvaluateeResultResponse {
    private Long userId;
    private String evaluateeName;
    private Double overallAverage;
    private Integer submittedResponses;
    private Integer totalResponses;
    private List<CriterionAverageResponse> criteriaAverages;
}
