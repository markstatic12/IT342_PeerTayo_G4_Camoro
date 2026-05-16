package edu.cit.camoro.peertayo.evaluation.submission;

import edu.cit.camoro.peertayo.auth.entity.User;
import edu.cit.camoro.peertayo.auth.repository.UserRepository;
import edu.cit.camoro.peertayo.evaluation.entity.Criterion;
import edu.cit.camoro.peertayo.evaluation.entity.EvaluationAssignment;
import edu.cit.camoro.peertayo.evaluation.entity.EvaluationForm;
import edu.cit.camoro.peertayo.evaluation.entity.Rating;
import edu.cit.camoro.peertayo.evaluation.repository.CriterionRepository;
import edu.cit.camoro.peertayo.evaluation.repository.EvaluationAssignmentRepository;
import edu.cit.camoro.peertayo.evaluation.repository.EvaluationFormRepository;
import edu.cit.camoro.peertayo.evaluation.repository.RatingRepository;
import edu.cit.camoro.peertayo.shared.exception.BusinessRuleException;
import edu.cit.camoro.peertayo.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SubmissionService {

    private final EvaluationFormRepository evaluationFormRepository;
    private final EvaluationAssignmentRepository evaluationAssignmentRepository;
    private final CriterionRepository criterionRepository;
    private final RatingRepository ratingRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
        public List<PendingEvaluationResponse> getPending(String email, boolean archived) {
        User currentUser = getUser(email);
        LocalDateTime now = LocalDateTime.now();

        return evaluationAssignmentRepository
            .findAllByEvaluatorAndSubmittedFalseAndArchivedByEvaluatorOrderByEvaluationDeadlineAsc(currentUser, archived)
                .stream()
                .filter(item -> item.getEvaluation().getDeletedAt() == null) // BR-003: Skip deleted
                .map(item -> PendingEvaluationResponse.builder()
                        .id(item.getEvaluation().getId())
                        .assignmentId(item.getId())
                        .title(item.getEvaluation().getTitle())
                        .deadline(item.getEvaluation().getDeadline())
                        .evaluateeName(fullName(item.getEvaluatee()))
                        .creatorName(fullName(item.getEvaluation().getCreatedBy()))
                        .archived(item.isArchivedByEvaluator())
                        .build())
                .toList();
    }

        @Transactional
        public void setArchivedForEvaluation(Long evaluationId, String email, boolean archived) {
        User currentUser = getUser(email);
        EvaluationForm evaluation = evaluationFormRepository.findById(evaluationId)
            .orElseThrow(() -> new ResourceNotFoundException("Evaluation not found"));

        List<EvaluationAssignment> assignments = evaluationAssignmentRepository
            .findAllByEvaluationAndEvaluatorAndSubmittedFalse(evaluation, currentUser);

        if (assignments.isEmpty()) {
            throw new ResourceNotFoundException("Pending evaluation not found");
        }

        assignments.forEach(a -> a.setArchivedByEvaluator(archived));
        evaluationAssignmentRepository.saveAll(assignments);
        }

    @Transactional(readOnly = true)
    public long getSubmittedThisMonthCount(String email) {
        User currentUser = getUser(email);
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfMonth = now.withDayOfMonth(1).toLocalDate().atStartOfDay();
        LocalDateTime endOfMonth = startOfMonth.plusMonths(1).minusNanos(1);

        return evaluationAssignmentRepository
                .countByEvaluatorAndSubmittedTrueAndSubmittedAtBetween(currentUser, startOfMonth, endOfMonth);
    }

    @Transactional(readOnly = true)
    public long getSubmittedCount(String email) {
        User currentUser = getUser(email);
        return evaluationAssignmentRepository.countByEvaluatorAndSubmittedTrue(currentUser);
    }

        @Transactional(readOnly = true)
        public CompletedFormsResponse getCompletedForms(String email, Boolean archived) {
        User currentUser = getUser(email);

        List<EvaluationAssignment> allSubmitted = evaluationAssignmentRepository
            .findAllByEvaluatorAndSubmittedTrue(currentUser);

        if (allSubmitted.isEmpty()) {
            return CompletedFormsResponse.builder()
                .totalSubmitted(0)
                .submittedThisMonth((int) getSubmittedThisMonthCount(email))
                .avgScoreGiven(0.0)
                .forms(List.of())
                .build();
        }

        List<EvaluationAssignment> filteredAssignments = archived == null
            ? allSubmitted
            : evaluationAssignmentRepository
                .findAllByEvaluatorAndSubmittedTrueAndArchivedByEvaluatorOrderByEvaluationDeadlineDesc(currentUser, archived);

        List<Rating> allRatings = ratingRepository.findAllByAssignmentInAndActiveTrue(allSubmitted);
        double avgScoreGiven = allRatings.isEmpty()
            ? 0.0
            : allRatings.stream().mapToInt(Rating::getRating).average().orElse(0);

        List<CompletedFormResponse> forms = buildCompletedForms(filteredAssignments);

        return CompletedFormsResponse.builder()
            .totalSubmitted(allSubmitted.size())
            .submittedThisMonth((int) getSubmittedThisMonthCount(email))
            .avgScoreGiven(avgScoreGiven)
            .forms(forms)
            .build();
        }

        @Transactional
        public void setCompletedArchived(Long evaluationId, String email, boolean archived) {
        User currentUser = getUser(email);
        EvaluationForm evaluation = evaluationFormRepository.findById(evaluationId)
            .orElseThrow(() -> new ResourceNotFoundException("Evaluation not found"));

        List<EvaluationAssignment> assignments = evaluationAssignmentRepository
            .findAllByEvaluationAndEvaluatorAndSubmittedTrue(evaluation, currentUser);

        if (assignments.isEmpty()) {
            throw new ResourceNotFoundException("Completed evaluation not found");
        }

        assignments.forEach(a -> a.setArchivedByEvaluator(archived));
        evaluationAssignmentRepository.saveAll(assignments);
        }

    @Transactional
    public void deleteCompletedForm(Long evaluationId, String email) {
        User currentUser = getUser(email);
        EvaluationForm evaluation = evaluationFormRepository.findById(evaluationId)
            .orElseThrow(() -> new ResourceNotFoundException("Evaluation not found"));

        List<EvaluationAssignment> assignments = evaluationAssignmentRepository
            .findAllByEvaluationAndEvaluatorAndSubmittedTrue(evaluation, currentUser);

        if (assignments.isEmpty()) {
            throw new ResourceNotFoundException("Completed evaluation not found");
        }

        ratingRepository.deleteAllByAssignmentIn(assignments);
        evaluationAssignmentRepository.deleteAll(assignments);
    }

    @Transactional
    public void submit(Long assignmentId, String email, SubmitEvaluationRequest request) {
        User currentUser = getUser(email);
        EvaluationAssignment assignment = evaluationAssignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Assignment not found"));

        if (!assignment.getEvaluator().getId().equals(currentUser.getId())) {
            throw new BusinessRuleException("EVAL-003", "You are not authorized to submit this evaluation");
        }

        if (assignment.isSubmitted()) {
            throw new BusinessRuleException("EVAL-002", "You have already submitted this evaluation");
        }

        EvaluationForm evaluation = assignment.getEvaluation();
        
        if (evaluation.getDeletedAt() != null) {
            throw new BusinessRuleException("EVAL-005", "This evaluation has been deleted and no longer accepts submissions");
        }
        
        // Prevent submissions on closed evaluations
        if ("CLOSED".equals(evaluation.getStatus())) {
            throw new BusinessRuleException("EVAL-004", "This evaluation is closed and no longer accepts submissions");
        }
        
        // Prevent submissions after the deadline
        if (evaluation.getDeadline().isBefore(LocalDateTime.now())) {
            throw new BusinessRuleException("EVAL-001", "Cannot submit evaluation after the deadline");
        }

        List<Long> criterionIds = request.getResponses().stream()
                .map(SubmitEvaluationRequest.ResponseItem::getCriteriaId).distinct().toList();

        Map<Long, Criterion> criterionMap = criterionRepository.findAllByIdInAndActiveTrue(criterionIds)
                .stream().collect(Collectors.toMap(Criterion::getId, c -> c));

        if (criterionMap.size() != criterionIds.size()) {
            throw new BusinessRuleException("VALID-001", "One or more criteria are invalid");
        }

        ratingRepository.saveAll(request.getResponses().stream()
                .map(item -> Rating.builder()
                        .assignment(assignment)
                        .criterion(criterionMap.get(item.getCriteriaId()))
                        .rating(item.getRating()).active(true).build())
                .toList());

        assignment.setSubmitted(true);
        assignment.setSubmittedAt(LocalDateTime.now());
        if (request.getComment() != null && !request.getComment().isBlank()) {
            assignment.setComment(request.getComment().trim());
        }
        
        evaluationAssignmentRepository.save(assignment);
    }

        private List<CompletedFormResponse> buildCompletedForms(List<EvaluationAssignment> assignments) {
        if (assignments.isEmpty()) return List.of();

        Map<Long, List<EvaluationAssignment>> byEvaluation = assignments.stream()
            .collect(Collectors.groupingBy(a -> a.getEvaluation().getId()));

        List<Rating> ratings = ratingRepository.findAllByAssignmentInAndActiveTrue(assignments);
        Map<Long, List<Rating>> ratingsByAssignment = ratings.stream()
            .collect(Collectors.groupingBy(r -> r.getAssignment().getId()));

        return byEvaluation.values().stream()
            .filter(group -> group.get(0).getEvaluation().getDeletedAt() == null) // BR-003: Skip deleted
            .map(group -> {
                EvaluationForm evaluation = group.get(0).getEvaluation();
                String creatorName = fullName(evaluation.getCreatedBy());

                List<CompletedEvaluateeResponse> evaluatees = group.stream()
                    .map(assignment -> {
                    List<CompletedCriterionResponse> criteria = ratingsByAssignment
                        .getOrDefault(assignment.getId(), List.of())
                        .stream()
                        .map(r -> CompletedCriterionResponse.builder()
                            .criteriaId(r.getCriterion().getId())
                            .criteriaName(r.getCriterion().getTitle())
                            .criteriaDescription(r.getCriterion().getDescription())
                            .rating(r.getRating())
                            .build())
                        .sorted((a, b) -> a.getCriteriaId().compareTo(b.getCriteriaId()))
                        .toList();

                    return CompletedEvaluateeResponse.builder()
                        .assignmentId(assignment.getId())
                        .evaluateeName(fullName(assignment.getEvaluatee()))
                        .submittedAt(assignment.getSubmittedAt())
                        .comment(assignment.getComment())
                        .criteria(criteria)
                        .build();
                    })
                    .sorted((a, b) -> b.getSubmittedAt().compareTo(a.getSubmittedAt()))
                    .toList();

                LocalDateTime latestSubmittedAt = group.stream()
                    .map(EvaluationAssignment::getSubmittedAt)
                    .filter(java.util.Objects::nonNull)
                    .max(LocalDateTime::compareTo)
                    .orElse(null);

                boolean archived = group.stream().allMatch(EvaluationAssignment::isArchivedByEvaluator);

                return CompletedFormResponse.builder()
                    .evaluationId(evaluation.getId())
                    .title(evaluation.getTitle())
                    .creatorName(creatorName)
                    .submittedAt(latestSubmittedAt)
                    .totalEvaluatees(group.size())
                    .archived(archived)
                    .evaluatees(evaluatees)
                    .build();
            })
            .sorted((a, b) -> {
                if (a.getSubmittedAt() == null) return 1;
                if (b.getSubmittedAt() == null) return -1;
                return b.getSubmittedAt().compareTo(a.getSubmittedAt());
            })
            .toList();
        }

    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private String fullName(User user) {
        return (user.getFirstName() + " " + user.getLastName()).trim();
    }
}
