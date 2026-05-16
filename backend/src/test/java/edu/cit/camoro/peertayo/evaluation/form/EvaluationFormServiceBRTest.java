package edu.cit.camoro.peertayo.evaluation.form;

import edu.cit.camoro.peertayo.auth.entity.User;
import edu.cit.camoro.peertayo.auth.repository.UserRepository;
import edu.cit.camoro.peertayo.auth.repository.RoleRepository;
import edu.cit.camoro.peertayo.evaluation.repository.EvaluationAssignmentRepository;
import edu.cit.camoro.peertayo.evaluation.repository.EvaluationFormRepository;
import edu.cit.camoro.peertayo.notification.shared.NotificationService;
import edu.cit.camoro.peertayo.shared.mail.MailService;
import edu.cit.camoro.peertayo.shared.exception.BusinessRuleException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EvaluationFormServiceBRTest {

    @Mock private EvaluationFormRepository evaluationFormRepository;
    @Mock private EvaluationAssignmentRepository evaluationAssignmentRepository;
    @Mock private UserRepository userRepository;
    @Mock private RoleRepository roleRepository;
    @Mock private NotificationService notificationService;
    @Mock private MailService mailService;

    @InjectMocks
    private EvaluationFormService evaluationFormService;

    private User user1;

    @BeforeEach
    void setUp() {
        user1 = new User();
        user1.setId(1L);
        user1.setEmail("alice@example.com");
        user1.setRoles(new java.util.HashSet<>());
    }

    @Test
    void create_ShouldSkipSelfEvaluation_WhenOverlapExists() {
        // Given
        CreateEvaluationRequest request = new CreateEvaluationRequest();
        request.setTitle("Test");
        request.setDescription("Desc");
        request.setDeadline(java.time.LocalDateTime.parse("2026-12-31T23:59"));
        request.setEvaluatorIds(List.of(1L));
        request.setEvaluateeIds(List.of(1L));

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user1));
        when(userRepository.findAllById(List.of(1L))).thenReturn(List.of(user1));
        when(roleRepository.findByName(edu.cit.camoro.peertayo.auth.entity.ERole.FACILITATOR))
                .thenReturn(Optional.of(new edu.cit.camoro.peertayo.auth.entity.Role(null, edu.cit.camoro.peertayo.auth.entity.ERole.FACILITATOR)));
        
        edu.cit.camoro.peertayo.evaluation.entity.EvaluationForm dummyForm = new edu.cit.camoro.peertayo.evaluation.entity.EvaluationForm();
        dummyForm.setId(10L);
        dummyForm.setTitle("Test");
        when(evaluationFormRepository.save(org.mockito.ArgumentMatchers.any())).thenReturn(dummyForm);

        // When/Then
        // It should NOT throw BusinessRuleException, it should silently skip
        org.junit.jupiter.api.Assertions.assertDoesNotThrow(() -> 
            evaluationFormService.create(request, "creator@example.com")
        );
    }
}

