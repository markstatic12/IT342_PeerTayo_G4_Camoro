package edu.cit.camoro.peertayo.evaluation.service;

import edu.cit.camoro.peertayo.auth.entity.User;
import edu.cit.camoro.peertayo.auth.repository.UserRepository;
import edu.cit.camoro.peertayo.evaluation.dto.request.CreateEvaluationRequest;
import edu.cit.camoro.peertayo.evaluation.dto.request.SubmitEvaluationRequest;
import edu.cit.camoro.peertayo.evaluation.dto.request.UpdateEvaluationRequest;
import edu.cit.camoro.peertayo.evaluation.dto.response.*;
import edu.cit.camoro.peertayo.evaluation.entity.*;
import edu.cit.camoro.peertayo.evaluation.repository.*;
import edu.cit.camoro.peertayo.notification.entity.Notification;
import edu.cit.camoro.peertayo.notification.repository.NotificationRepository;
import edu.cit.camoro.peertayo.shared.exception.BusinessRuleException;
import edu.cit.camoro.peertayo.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EvaluationService {

    private final EvaluationFormRepository evaluationFormRepository;
    private final EvaluationAssignmentRepository evaluationAssignmentRepository;
    private final CriterionRepository criterionRepository;
    private final RatingRepository ratingRepository;
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    @Transactional
    public CreatedEvaluationResponse create(CreateEvaluationRequest request, String creatorEmail) {
        User creator = getUserByEmail(creatorEmail);

        EvaluationForm evaluation = EvaluationForm.builder()
                .title(request.getTitle().trim())
                .description(request.getDescription().trim())
                .deadline(request.getDeadline())
                .status("ACTIVE")
                .createdBy(creator)
                .build();

        EvaluationForm saved = evaluationFormRepository.save(evaluation);

        List<User> evaluatees = userRepository.findAllById(request.getEvaluateeIds());
        List<User> evaluators = userRepository.findAllById(request.getEvaluatorIds());

        if (evaluatees.size() != request.getEvaluateeIds().size()) {
            throw new ResourceNotFoundException("One or more evaluatees were not found");
        }
        if (evaluators.size() != request.getEvaluatorIds().size()) {
            throw new ResourceNotFoundException("One or more evaluators were not found");
        }

        List<EvaluationAssignment> assignments = new ArrayList<>();
        for (User evaluator : evaluators) {
            for (User evaluatee : evaluatees) {
                if (Objects.equals(evaluator.getId(), evaluatee.getId())) continue;
                assignments.add(EvaluationAssignment.builder()
                        .evaluation(saved)
                        .evaluator(evaluator)
                        .evaluatee(evaluatee)
                        .submitted(false)
                        .build());
            }
        }

        if (assignments.isEmpty()) {
            throw new BusinessRuleException("VALID-001", "No valid evaluator-evaluatee assignments could be created");
        }

        evaluationAssignmentRepository.saveAll(assignments);

        List<Notification> notifications = evaluators.stream()
                .map(user -> Notification.builder()
                        .user(user)
                        .message("You have a new evaluation assigned.")
                        .read(false)
                        .build())
                .toList();
        notificationRepository.saveAll(notifications);

        return CreatedEvaluationResponse.builder()
                .id(saved.getId())
                .title(saved.getTitle())
                .deadline(saved.getDeadline())
                .createdBy(creator.getId())
                .status(saved.getStatus())
                .build();
    }

    @Transactional(readOnly = true)
    public List<PendingEvaluationResponse> getPendingEvaluations(String email) {
        User currentUser = getUserByEmail(email);
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
    public void submitEvaluation(Long evaluationId, String email, SubmitEvaluationRequest request) {
        User currentUser = getUserByEmail(email);
        EvaluationForm evaluation = evaluationFormRepository.findById(evaluationId)
                .orElseThrow(() -> new ResourceNotFoundException("Evaluation not found"));

        if (evaluation.getDeadline().isBefore(LocalDateTime.now())) {
            throw new BusinessRuleException("EVAL-001", "Cannot submit evaluation after the deadline");
        }

        List<EvaluationAssignment> assignments = evaluationAssignmentRepository
                .findAllByEvaluationAndEvaluatorAndSubmittedFalse(evaluation, currentUser);

        if (assignments.isEmpty()) {
            throw new BusinessRuleException("EVAL-002", "You have already submitted this evaluation");
        }

        List<Long> criterionIds = request.getResponses().stream()
                .map(SubmitEvaluationRequest.ResponseItem::getCriteriaId)
                .distinct()
                .toList();

        Map<Long, Criterion> criterionMap = criterionRepository.findAllByIdInAndActiveTrue(criterionIds)
                .stream()
                .collect(Collectors.toMap(Criterion::getId, c -> c));

        if (criterionMap.size() != criterionIds.size()) {
            throw new BusinessRuleException("VALID-001", "One or more criteria are invalid");
        }

        for (EvaluationAssignment assignment : assignments) {
            List<Rating> ratings = request.getResponses().stream()
                    .map(item -> Rating.builder()
                            .assignment(assignment)
                            .criterion(criterionMap.get(item.getCriteriaId()))
                            .rating(item.getRating())
                            .active(true)
                            .build())
                    .toList();
            ratingRepository.saveAll(ratings);
            assignment.setSubmitted(true);
            assignment.setSubmittedAt(LocalDateTime.now());
            if (request.getComment() != null && !request.getComment().isBlank()) {
                assignment.setComment(request.getComment().trim());
            }
        }

        evaluationAssignmentRepository.saveAll(assignments);
    }

    @Transactional(readOnly = true)
    public MyResultsResponse getMyResults(String email) {
        User me = getUserByEmail(email);

        List<EvaluationAssignment> submittedAssignments = evaluationAssignmentRepository.findAll().stream()
                .filter(a -> Objects.equals(a.getEvaluatee().getId(), me.getId()) && a.isSubmitted())
                .toList();

        if (submittedAssignments.isEmpty()) {
            return MyResultsResponse.builder()
                    .overallAverage(0.0)
                    .totalResponses(0)
                    .questionAverages(Collections.emptyList())
                    .comments(Collections.emptyList())
                    .build();
        }

        List<Rating> ratings = ratingRepository.findAllByAssignmentInAndActiveTrue(submittedAssignments);

        Map<Long, List<Rating>> groupedByCriteria = ratings.stream()
                .collect(Collectors.groupingBy(r -> r.getCriterion().getId()));

        List<CriterionAverageResponse> criterionAverages = groupedByCriteria.entrySet().stream()
                .map(entry -> CriterionAverageResponse.builder()
                        .criteriaId(entry.getKey())
                        .average(entry.getValue().stream().mapToInt(Rating::getRating).average().orElse(0))
                        .build())
                .sorted(Comparator.comparing(CriterionAverageResponse::getCriteriaId))
                .toList();

        double overall = ratings.stream().mapToInt(Rating::getRating).average().orElse(0);

        List<String> comments = submittedAssignments.stream()
                .map(EvaluationAssignment::getComment)
                .filter(c -> c != null && !c.isBlank())
                .toList();

        return MyResultsResponse.builder()
                .overallAverage(overall)
                .totalResponses(submittedAssignments.size())
                .questionAverages(criterionAverages)
                .comments(comments)
                .build();
    }

    @Transactional(readOnly = true)
    public List<CreatedEvaluationListItemResponse> getCreatedEvaluations(String email) {
        User creator = getUserByEmail(email);
        return evaluationFormRepository.findAllByCreatedByOrderByCreatedAtDesc(creator)
                .stream()
                .map(evaluation -> {
                    long total = evaluationAssignmentRepository.countByEvaluation(evaluation);
                    long submitted = evaluationAssignmentRepository.countByEvaluationAndSubmittedTrue(evaluation);
                    return CreatedEvaluationListItemResponse.builder()
                            .id(evaluation.getId())
                            .title(evaluation.getTitle())
                            .deadline(evaluation.getDeadline())
                            .createdAt(evaluation.getCreatedAt())
                            .description(evaluation.getDescription())
                            .status(evaluation.getStatus())
                            .submissionProgress(submitted + "/" + total)
                            .build();
                })
                .toList();
    }

    @Transactional
    public CreatedEvaluationListItemResponse updateEvaluation(Long id, UpdateEvaluationRequest request, String email) {
        User creator = getUserByEmail(email);
        EvaluationForm evaluation = evaluationFormRepository.findByIdAndCreatedBy(id, creator)
                .orElseThrow(() -> new ResourceNotFoundException("Evaluation not found"));

        evaluation.setTitle(request.getTitle().trim());
        evaluation.setDescription(request.getDescription().trim());
        evaluation.setDeadline(request.getDeadline());
        if (request.getStatus() != null && !request.getStatus().isBlank()) {
            evaluation.setStatus(request.getStatus());
        }

        EvaluationForm saved = evaluationFormRepository.save(evaluation);
        long total = evaluationAssignmentRepository.countByEvaluation(saved);
        long submitted = evaluationAssignmentRepository.countByEvaluationAndSubmittedTrue(saved);

        return CreatedEvaluationListItemResponse.builder()
                .id(saved.getId())
                .title(saved.getTitle())
                .deadline(saved.getDeadline())
                .createdAt(saved.getCreatedAt())
                .description(saved.getDescription())
                .status(saved.getStatus())
                .submissionProgress(submitted + "/" + total)
                .build();
    }

    @Transactional
    public void deleteEvaluation(Long id, String email) {
        User creator = getUserByEmail(email);
        EvaluationForm evaluation = evaluationFormRepository.findByIdAndCreatedBy(id, creator)
                .orElseThrow(() -> new ResourceNotFoundException("Evaluation not found"));
        evaluationAssignmentRepository.deleteAllByEvaluation(evaluation);
        evaluationFormRepository.delete(evaluation);
    }

    @Transactional(readOnly = true)
    public EvaluationResultsResponse getEvaluationResults(Long evaluationId, String email) {
        User creator = getUserByEmail(email);
        EvaluationForm evaluation = evaluationFormRepository.findByIdAndCreatedBy(evaluationId, creator)
                .orElseThrow(() -> new ResourceNotFoundException("Evaluation not found"));

        List<EvaluationAssignment> allAssignments = evaluationAssignmentRepository.findAllByEvaluation(evaluation);

        Map<Long, List<EvaluationAssignment>> allByEvaluatee = allAssignments.stream()
                .collect(Collectors.groupingBy(a -> a.getEvaluatee().getId()));

        Map<Long, List<EvaluationAssignment>> submittedByEvaluatee = allAssignments.stream()
                .filter(EvaluationAssignment::isSubmitted)
                .collect(Collectors.groupingBy(a -> a.getEvaluatee().getId()));

        List<EvaluateeResultResponse> evaluatees = new ArrayList<>();
        for (Map.Entry<Long, List<EvaluationAssignment>> entry : allByEvaluatee.entrySet()) {
            Long evaluateeId = entry.getKey();
            User evaluatee = entry.getValue().get(0).getEvaluatee();
            int total = entry.getValue().size();

            List<EvaluationAssignment> submitted = submittedByEvaluatee.getOrDefault(evaluateeId, Collections.emptyList());
            int submittedCount = submitted.size();

            List<Rating> ratings = submitted.isEmpty()
                    ? Collections.emptyList()
                    : ratingRepository.findAllByAssignmentInAndActiveTrue(submitted);

            Map<Long, List<Rating>> byCriterion = ratings.stream()
                    .collect(Collectors.groupingBy(r -> r.getCriterion().getId()));

            List<CriterionAverageResponse> criterionAverages = byCriterion.entrySet().stream()
                    .map(c -> CriterionAverageResponse.builder()
                            .criteriaId(c.getKey())
                            .average(c.getValue().stream().mapToInt(Rating::getRating).average().orElse(0))
                            .build())
                    .sorted(Comparator.comparing(CriterionAverageResponse::getCriteriaId))
                    .toList();

            evaluatees.add(EvaluateeResultResponse.builder()
                    .userId(evaluateeId)
                    .evaluateeName(fullName(evaluatee))
                    .overallAverage(ratings.isEmpty() ? 0.0 : ratings.stream().mapToInt(Rating::getRating).average().orElse(0))
                    .submittedResponses(submittedCount)
                    .totalResponses(total)
                    .criteriaAverages(criterionAverages)
                    .build());
        }

        return EvaluationResultsResponse.builder()
                .evaluationId(evaluation.getId())
                .evaluatees(evaluatees)
                .build();
    }

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private String fullName(User user) {
        return (user.getFirstName() + " " + user.getLastName()).trim();
    }
}
