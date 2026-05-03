package edu.cit.camoro.peertayo.evaluation.results;

import edu.cit.camoro.peertayo.auth.entity.User;
import edu.cit.camoro.peertayo.auth.repository.UserRepository;
import edu.cit.camoro.peertayo.evaluation.entity.EvaluationAssignment;
import edu.cit.camoro.peertayo.evaluation.entity.EvaluationForm;
import edu.cit.camoro.peertayo.evaluation.entity.Rating;
import edu.cit.camoro.peertayo.evaluation.repository.EvaluationAssignmentRepository;
import edu.cit.camoro.peertayo.evaluation.repository.EvaluationFormRepository;
import edu.cit.camoro.peertayo.evaluation.repository.RatingRepository;
import edu.cit.camoro.peertayo.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ResultsService {

    private final EvaluationFormRepository evaluationFormRepository;
    private final EvaluationAssignmentRepository evaluationAssignmentRepository;
    private final RatingRepository ratingRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public MyResultsResponse getMyResults(String email) {
        User me = getUser(email);

        List<EvaluationAssignment> submitted = evaluationAssignmentRepository.findAll().stream()
                .filter(a -> Objects.equals(a.getEvaluatee().getId(), me.getId()) && a.isSubmitted())
                .toList();

        if (submitted.isEmpty()) {
            return MyResultsResponse.builder()
                    .overallAverage(0.0).totalResponses(0)
                    .questionAverages(Collections.emptyList())
                    .comments(Collections.emptyList()).build();
        }

        List<Rating> ratings = ratingRepository.findAllByAssignmentInAndActiveTrue(submitted);

        List<CriterionAverageResponse> criterionAverages = ratings.stream()
                .collect(Collectors.groupingBy(r -> r.getCriterion().getId()))
                .entrySet().stream()
                .map(e -> CriterionAverageResponse.builder()
                        .criteriaId(e.getKey())
                        .average(e.getValue().stream().mapToInt(Rating::getRating).average().orElse(0))
                        .build())
                .sorted(Comparator.comparing(CriterionAverageResponse::getCriteriaId))
                .toList();

        return MyResultsResponse.builder()
                .overallAverage(ratings.stream().mapToInt(Rating::getRating).average().orElse(0))
                .totalResponses(submitted.size())
                .questionAverages(criterionAverages)
                .comments(submitted.stream().map(EvaluationAssignment::getComment)
                        .filter(c -> c != null && !c.isBlank()).toList())
                .build();
    }

    @Transactional(readOnly = true)
    public EvaluationResultsResponse getEvaluationResults(Long evaluationId, String email) {
        User creator = getUser(email);
        EvaluationForm evaluation = evaluationFormRepository.findByIdAndCreatedBy(evaluationId, creator)
                .orElseThrow(() -> new ResourceNotFoundException("Evaluation not found"));

        List<EvaluationAssignment> all = evaluationAssignmentRepository.findAllByEvaluation(evaluation);

        Map<Long, List<EvaluationAssignment>> allByEvaluatee = all.stream()
                .collect(Collectors.groupingBy(a -> a.getEvaluatee().getId()));

        Map<Long, List<EvaluationAssignment>> submittedByEvaluatee = all.stream()
                .filter(EvaluationAssignment::isSubmitted)
                .collect(Collectors.groupingBy(a -> a.getEvaluatee().getId()));

        List<EvaluateeResultResponse> evaluatees = new ArrayList<>();
        for (Map.Entry<Long, List<EvaluationAssignment>> entry : allByEvaluatee.entrySet()) {
            Long evaluateeId = entry.getKey();
            User evaluatee = entry.getValue().get(0).getEvaluatee();
            int total = entry.getValue().size();

            List<EvaluationAssignment> submittedList =
                    submittedByEvaluatee.getOrDefault(evaluateeId, Collections.emptyList());

            List<Rating> ratings = submittedList.isEmpty()
                    ? Collections.emptyList()
                    : ratingRepository.findAllByAssignmentInAndActiveTrue(submittedList);

            List<CriterionAverageResponse> criterionAverages = ratings.stream()
                    .collect(Collectors.groupingBy(r -> r.getCriterion().getId()))
                    .entrySet().stream()
                    .map(c -> CriterionAverageResponse.builder()
                            .criteriaId(c.getKey())
                            .average(c.getValue().stream().mapToInt(Rating::getRating).average().orElse(0))
                            .build())
                    .sorted(Comparator.comparing(CriterionAverageResponse::getCriteriaId))
                    .toList();

            evaluatees.add(EvaluateeResultResponse.builder()
                    .userId(evaluateeId)
                    .evaluateeName((evaluatee.getFirstName() + " " + evaluatee.getLastName()).trim())
                    .overallAverage(ratings.isEmpty() ? 0.0
                            : ratings.stream().mapToInt(Rating::getRating).average().orElse(0))
                    .submittedResponses(submittedList.size())
                    .totalResponses(total)
                    .criteriaAverages(criterionAverages)
                    .build());
        }

        return EvaluationResultsResponse.builder()
                .evaluationId(evaluation.getId())
                .evaluatees(evaluatees)
                .build();
    }

    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}
