package edu.cit.camoro.peertayo.evaluation.form;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class EvaluationParticipantsResponse {

    @Data
    @Builder
    public static class Participant {
        private Long id;
        private String firstName;
        private String lastName;
        private String email;
    }

    private List<Participant> evaluators;
    private List<Participant> evaluatees;
}
