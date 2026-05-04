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

import java.time.LocalDateTime;
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
        List<EvaluationAssignment> submitted = evaluationAssignmentRepository
                .findAllByEvaluateeAndSubmittedTrue(me);

        if (submitted.isEmpty()) {
            return MyResultsResponse.builder()
                    .overallAverage(0.0).totalResponses(0)
                    .questionAverages(Collections.emptyList())
                    .comments(Collections.emptyList())
                    .evaluations(Collections.emptyList())
                    .build();
        }

        List<Rating> ratings = ratingRepository.findAllByAssignmentInAndActiveTrue(submitted);

        List<CriterionAverageResponse> criterionAverages = toCriterionAverages(ratings);

        List<MyEvaluationResultResponse> evaluations = submitted.stream()
                .collect(Collectors.groupingBy(EvaluationAssignment::getEvaluation))
                .entrySet().stream()
                .map(entry -> buildEvaluationSummary(entry.getKey(), entry.getValue()))
                .sorted(Comparator.comparing(MyEvaluationResultResponse::getSubmittedAt,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();

        return MyResultsResponse.builder()
                .overallAverage(ratings.stream().mapToInt(Rating::getRating).average().orElse(0))
                .totalResponses(submitted.size())
                .questionAverages(criterionAverages)
                .comments(submitted.stream().map(EvaluationAssignment::getComment)
                        .filter(c -> c != null && !c.isBlank()).toList())
                .evaluations(evaluations)
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

            List<CriterionAverageResponse> criterionAverages = toCriterionAverages(ratings);

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

        @Transactional
        public void setMyResultsArchived(Long evaluationId, String email, boolean archived) {
                User me = getUser(email);
                List<EvaluationAssignment> assignments = evaluationAssignmentRepository
                                .findAllByEvaluationIdAndEvaluatee(evaluationId, me)
                                .stream()
                                .filter(EvaluationAssignment::isSubmitted)
                                .toList();

                if (assignments.isEmpty()) {
                        throw new ResourceNotFoundException("Evaluation results not found");
                }

                assignments.forEach(a -> a.setArchivedByEvaluatee(archived));
                evaluationAssignmentRepository.saveAll(assignments);
        }

    private List<CriterionAverageResponse> toCriterionAverages(List<Rating> ratings) {
        return ratings.stream()
                .collect(Collectors.groupingBy(r -> r.getCriterion().getId()))
                .entrySet().stream()
                .map(entry -> CriterionAverageResponse.builder()
                        .criteriaId(entry.getKey())
                        .criteriaName(entry.getValue().get(0).getCriterion().getTitle())
                        .average(entry.getValue().stream().mapToInt(Rating::getRating).average().orElse(0))
                        .build())
                .sorted(Comparator.comparing(CriterionAverageResponse::getCriteriaId))
                .toList();
    }

        private MyEvaluationResultResponse buildEvaluationSummary(EvaluationForm evaluation, List<EvaluationAssignment> assignments) {
        List<Rating> ratings = ratingRepository.findAllByAssignmentInAndActiveTrue(assignments);
        List<CriterionAverageResponse> criterionAverages = toCriterionAverages(ratings);
        double overallAverage = ratings.stream().mapToInt(Rating::getRating).average().orElse(0);

        List<EvaluationCommentResponse> comments = assignments.stream()
                .filter(a -> a.getComment() != null && !a.getComment().isBlank())
                .map(a -> EvaluationCommentResponse.builder()
                        .comment(a.getComment())
                        .submittedAt(a.getSubmittedAt())
                        .build())
                .sorted(Comparator.comparing(EvaluationCommentResponse::getSubmittedAt,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();

        LocalDateTime latestSubmittedAt = assignments.stream()
                .map(EvaluationAssignment::getSubmittedAt)
                .filter(Objects::nonNull)
                .max(Comparator.naturalOrder())
                .orElse(null);

        String creatorName = (evaluation.getCreatedBy().getFirstName() + " " + evaluation.getCreatedBy().getLastName()).trim();

        boolean archived = assignments.stream().allMatch(EvaluationAssignment::isArchivedByEvaluatee);

        return MyEvaluationResultResponse.builder()
                .evaluationId(evaluation.getId())
                .title(evaluation.getTitle())
                .createdByName(creatorName)
                .submittedAt(latestSubmittedAt)
                .responses(assignments.size())
                .overallAverage(overallAverage)
                .criteriaAverages(criterionAverages)
                .comments(comments)
                .archived(archived)
                .build();
    }

    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}
