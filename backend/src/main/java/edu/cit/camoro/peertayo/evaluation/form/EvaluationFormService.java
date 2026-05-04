package edu.cit.camoro.peertayo.evaluation.form;

import edu.cit.camoro.peertayo.auth.entity.User;
import edu.cit.camoro.peertayo.auth.repository.UserRepository;
import edu.cit.camoro.peertayo.evaluation.entity.EvaluationAssignment;
import edu.cit.camoro.peertayo.evaluation.entity.EvaluationForm;
import edu.cit.camoro.peertayo.evaluation.repository.EvaluationAssignmentRepository;
import edu.cit.camoro.peertayo.evaluation.repository.EvaluationFormRepository;
import edu.cit.camoro.peertayo.notification.entity.Notification;
import edu.cit.camoro.peertayo.notification.repository.NotificationRepository;
import edu.cit.camoro.peertayo.shared.exception.BusinessRuleException;
import edu.cit.camoro.peertayo.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class EvaluationFormService {

    private final EvaluationFormRepository evaluationFormRepository;
    private final EvaluationAssignmentRepository evaluationAssignmentRepository;
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    @Transactional
    public CreatedEvaluationResponse create(CreateEvaluationRequest request, String creatorEmail) {
        User creator = getUser(creatorEmail);

        EvaluationForm saved = evaluationFormRepository.save(
                EvaluationForm.builder()
                        .title(request.getTitle().trim())
                        .description(request.getDescription().trim())
                        .deadline(request.getDeadline())
                        .status("ACTIVE")
                        .createdBy(creator)
                        .build());

        List<User> evaluatees = userRepository.findAllById(request.getEvaluateeIds());
        List<User> evaluators = userRepository.findAllById(request.getEvaluatorIds());

        if (evaluatees.size() != request.getEvaluateeIds().size())
            throw new ResourceNotFoundException("One or more evaluatees were not found");
        if (evaluators.size() != request.getEvaluatorIds().size())
            throw new ResourceNotFoundException("One or more evaluators were not found");

        List<EvaluationAssignment> assignments = new ArrayList<>();
        for (User evaluator : evaluators) {
            for (User evaluatee : evaluatees) {
                if (Objects.equals(evaluator.getId(), evaluatee.getId())) continue;
                assignments.add(EvaluationAssignment.builder()
                        .evaluation(saved).evaluator(evaluator)
                        .evaluatee(evaluatee).submitted(false).build());
            }
        }

        if (assignments.isEmpty())
            throw new BusinessRuleException("VALID-001", "No valid evaluator-evaluatee assignments could be created");

        evaluationAssignmentRepository.saveAll(assignments);

        notificationRepository.saveAll(evaluators.stream()
                .map(u -> Notification.builder()
                        .user(u).message("You have a new evaluation assigned.").read(false).build())
                .toList());

        return CreatedEvaluationResponse.builder()
                .id(saved.getId()).title(saved.getTitle())
                .deadline(saved.getDeadline()).createdBy(creator.getId())
                .status(saved.getStatus()).build();
    }

    @Transactional(readOnly = true)
    public List<CreatedEvaluationListItemResponse> getCreated(String email) {
        User creator = getUser(email);
        return evaluationFormRepository.findAllByCreatedByOrderByCreatedAtDesc(creator)
                .stream().map(ev -> {
                    long total = evaluationAssignmentRepository.countByEvaluation(ev);
                    long submitted = evaluationAssignmentRepository.countByEvaluationAndSubmittedTrue(ev);
                    return CreatedEvaluationListItemResponse.builder()
                            .id(ev.getId()).title(ev.getTitle()).deadline(ev.getDeadline())
                            .createdAt(ev.getCreatedAt()).description(ev.getDescription())
                            .status(ev.getStatus()).submissionProgress(submitted + "/" + total).build();
                }).toList();
    }

    @Transactional
    public CreatedEvaluationListItemResponse update(Long id, UpdateEvaluationRequest request, String email) {
        User creator = getUser(email);
        EvaluationForm ev = evaluationFormRepository.findByIdAndCreatedBy(id, creator)
                .orElseThrow(() -> new ResourceNotFoundException("Evaluation not found"));

        ev.setTitle(request.getTitle().trim());
        ev.setDescription(request.getDescription().trim());
        ev.setDeadline(request.getDeadline());
        if (request.getStatus() != null && !request.getStatus().isBlank())
            ev.setStatus(request.getStatus());

        EvaluationForm saved = evaluationFormRepository.save(ev);
        long total = evaluationAssignmentRepository.countByEvaluation(saved);
        long submitted = evaluationAssignmentRepository.countByEvaluationAndSubmittedTrue(saved);

        return CreatedEvaluationListItemResponse.builder()
                .id(saved.getId()).title(saved.getTitle()).deadline(saved.getDeadline())
                .createdAt(saved.getCreatedAt()).description(saved.getDescription())
                .status(saved.getStatus()).submissionProgress(submitted + "/" + total).build();
    }

    @Transactional
    public void delete(Long id, String email) {
        User creator = getUser(email);
        EvaluationForm ev = evaluationFormRepository.findByIdAndCreatedBy(id, creator)
                .orElseThrow(() -> new ResourceNotFoundException("Evaluation not found"));
        evaluationAssignmentRepository.deleteAllByEvaluation(ev);
        evaluationFormRepository.delete(ev);
    }

    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}
