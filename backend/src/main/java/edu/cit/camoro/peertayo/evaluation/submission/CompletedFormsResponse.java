package edu.cit.camoro.peertayo.evaluation.submission;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class CompletedFormsResponse {
    private Integer totalSubmitted;
    private Integer submittedThisMonth;
    private Double avgScoreGiven;
    private List<CompletedFormResponse> forms;
}
