package edu.cit.camoro.peertayo.evaluation.entity;

import edu.cit.camoro.peertayo.auth.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "evaluation_assignments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EvaluationAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "evaluation_id", nullable = false)
    private EvaluationForm evaluation;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "evaluator_id", nullable = false)
    private User evaluator;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "evaluatee_id", nullable = false)
    private User evaluatee;

    @Column(name = "is_submitted", nullable = false)
    private boolean submitted;

    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;

    @Column(name = "comment", length = 1000)
    private String comment;
}
