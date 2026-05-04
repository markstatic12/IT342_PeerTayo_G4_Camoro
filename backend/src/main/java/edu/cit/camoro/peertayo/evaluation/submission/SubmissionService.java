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
    public List<PendingEvaluationResponse> getPending(String email) {
        User currentUser = getUser(email);
        LocalDateTime now = LocalDateTime.now();

        return evaluationAssignmentRepository
                .findAllByEvaluatorAndSubmittedFalseOrderByEvaluationDeadlineAsc(currentUser)
                .stream()
                .filter(item -> item.getEvaluation().getDeadline().isAfter(now))
                .map(item -> PendingEvaluationResponse.builder()
                        .id(item.getEvaluation().getId())
                        .assignmentId(item.getId())
                        .title(item.getEvaluation().getTitle())
                        .deadline(item.getEvaluation().getDeadline())
                        .evaluateeName(fullName(item.getEvaluatee()))
                        .build())
                .toList();
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

    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private String fullName(User user) {
        return (user.getFirstName() + " " + user.getLastName()).trim();
    }
}
