package edu.cit.camoro.peertayo.dto.response;

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
