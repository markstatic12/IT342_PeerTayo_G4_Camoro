package edu.cit.camoro.peertayo.evaluation.results;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class MyResultsResponse {
    private Double overallAverage;
    private Integer totalResponses;
    private List<CriterionAverageResponse> questionAverages;
    private List<String> comments;
}
