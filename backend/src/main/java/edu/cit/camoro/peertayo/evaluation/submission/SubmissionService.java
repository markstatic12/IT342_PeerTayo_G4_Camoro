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

    @Transactional
    public void submit(Long evaluationId, String email, SubmitEvaluationRequest request) {
        User currentUser = getUser(email);
        EvaluationForm evaluation = evaluationFormRepository.findById(evaluationId)
                .orElseThrow(() -> new ResourceNotFoundException("Evaluation not found"));

        if (evaluation.getDeadline().isBefore(LocalDateTime.now()))
            throw new BusinessRuleException("EVAL-001", "Cannot submit evaluation after the deadline");

        List<EvaluationAssignment> assignments = evaluationAssignmentRepository
                .findAllByEvaluationAndEvaluatorAndSubmittedFalse(evaluation, currentUser);

        if (assignments.isEmpty())
            throw new BusinessRuleException("EVAL-002", "You have already submitted this evaluation");

        List<Long> criterionIds = request.getResponses().stream()
                .map(SubmitEvaluationRequest.ResponseItem::getCriteriaId).distinct().toList();

        Map<Long, Criterion> criterionMap = criterionRepository.findAllByIdInAndActiveTrue(criterionIds)
                .stream().collect(Collectors.toMap(Criterion::getId, c -> c));

        if (criterionMap.size() != criterionIds.size())
            throw new BusinessRuleException("VALID-001", "One or more criteria are invalid");

        for (EvaluationAssignment assignment : assignments) {
            ratingRepository.saveAll(request.getResponses().stream()
                    .map(item -> Rating.builder()
                            .assignment(assignment)
                            .criterion(criterionMap.get(item.getCriteriaId()))
                            .rating(item.getRating()).active(true).build())
                    .toList());
            assignment.setSubmitted(true);
            assignment.setSubmittedAt(LocalDateTime.now());
            if (request.getComment() != null && !request.getComment().isBlank())
                assignment.setComment(request.getComment().trim());
        }
        evaluationAssignmentRepository.saveAll(assignments);
    }

    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private String fullName(User user) {
        return (user.getFirstName() + " " + user.getLastName()).trim();
    }
}
