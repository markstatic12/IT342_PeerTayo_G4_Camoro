package edu.cit.camoro.peertayo.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class EvaluationResultsResponse {
    private Long evaluationId;
    private List<EvaluateeResultResponse> evaluatees;
}
