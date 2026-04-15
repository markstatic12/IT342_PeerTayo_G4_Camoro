package edu.cit.camoro.peertayo.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.cit.camoro.peertayo.dto.request.CreateEvaluationRequest;
import edu.cit.camoro.peertayo.dto.request.UpdateEvaluationRequest;
import edu.cit.camoro.peertayo.dto.response.EvaluationResponse;
import edu.cit.camoro.peertayo.entity.EvaluationForm;
import edu.cit.camoro.peertayo.entity.User;
import edu.cit.camoro.peertayo.exception.ResourceNotFoundException;
import edu.cit.camoro.peertayo.repository.EvaluationFormRepository;
import edu.cit.camoro.peertayo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EvaluationService {

    private static final TypeReference<List<String>> STRING_LIST = new TypeReference<>() {};

    private final EvaluationFormRepository evaluationFormRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public EvaluationResponse create(CreateEvaluationRequest request, String creatorEmail) {
        User creator = getUserByEmail(creatorEmail);

        EvaluationForm form = EvaluationForm.builder()
                .title(request.getTitle().trim())
                .description(request.getDescription().trim())
                .deadline(request.getDeadline())
                .status(normalizeStatus(request.getStatus()))
                .criteriaJson(toJson(request.getCriteria()))
                .questionsJson(toJson(request.getQuestions()))
                .ratingFieldsJson(toJson(request.getRatingFields()))
                .createdBy(creator)
                .build();

        EvaluationForm saved = evaluationFormRepository.save(form);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<EvaluationResponse> findAllByCreator(String creatorEmail) {
        User creator = getUserByEmail(creatorEmail);
        return evaluationFormRepository.findAllByCreatedByOrderByCreatedAtDesc(creator)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public EvaluationResponse findById(Long id, String creatorEmail) {
        User creator = getUserByEmail(creatorEmail);
        EvaluationForm form = evaluationFormRepository.findByIdAndCreatedBy(id, creator)
                .orElseThrow(() -> new ResourceNotFoundException("Evaluation not found"));
        return toResponse(form);
    }

    @Transactional
    public EvaluationResponse update(Long id, UpdateEvaluationRequest request, String creatorEmail) {
        User creator = getUserByEmail(creatorEmail);
        EvaluationForm form = evaluationFormRepository.findByIdAndCreatedBy(id, creator)
                .orElseThrow(() -> new ResourceNotFoundException("Evaluation not found"));

        form.setTitle(request.getTitle().trim());
        form.setDescription(request.getDescription().trim());
        form.setDeadline(request.getDeadline());
        form.setStatus(normalizeStatus(request.getStatus()));
        form.setCriteriaJson(toJson(request.getCriteria()));
        form.setQuestionsJson(toJson(request.getQuestions()));
        form.setRatingFieldsJson(toJson(request.getRatingFields()));

        EvaluationForm updated = evaluationFormRepository.save(form);
        return toResponse(updated);
    }

    @Transactional
    public void delete(Long id, String creatorEmail) {
        User creator = getUserByEmail(creatorEmail);
        EvaluationForm form = evaluationFormRepository.findByIdAndCreatedBy(id, creator)
                .orElseThrow(() -> new ResourceNotFoundException("Evaluation not found"));
        evaluationFormRepository.delete(form);
    }

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private String normalizeStatus(String status) {
        String normalized = status == null || status.isBlank() ? "DRAFT" : status.trim().toUpperCase();
        return switch (normalized) {
            case "DRAFT", "ACTIVE", "CLOSED" -> normalized;
            default -> "DRAFT";
        };
    }

    private String toJson(List<String> values) {
        try {
            return objectMapper.writeValueAsString(values == null ? Collections.emptyList() : values);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Unable to serialize evaluation list fields");
        }
    }

    private List<String> fromJson(String json) {
        if (json == null || json.isBlank()) {
            return Collections.emptyList();
        }
        try {
            return objectMapper.readValue(json, STRING_LIST);
        } catch (JsonProcessingException e) {
            return Collections.emptyList();
        }
    }

    private EvaluationResponse toResponse(EvaluationForm form) {
        User creator = form.getCreatedBy();
        String fullName = (creator.getFirstName() + " " + creator.getLastName()).trim();

        return EvaluationResponse.builder()
                .id(form.getId())
                .title(form.getTitle())
                .description(form.getDescription())
                .deadline(form.getDeadline())
                .status(form.getStatus())
                .criteria(fromJson(form.getCriteriaJson()))
                .questions(fromJson(form.getQuestionsJson()))
                .ratingFields(fromJson(form.getRatingFieldsJson()))
                .createdByEmail(creator.getEmail())
                .createdByName(fullName)
                .createdAt(form.getCreatedAt())
                .updatedAt(form.getUpdatedAt())
                .build();
    }
}
