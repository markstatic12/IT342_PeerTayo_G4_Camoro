package com.example.backend.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "evaluation_assignments")
public class EvaluationAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "evaluation_id", nullable = false)
    private Evaluation evaluation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "evaluator_id", nullable = false)
    private User evaluator;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "evaluatee_id", nullable = false)
    private User evaluatee;

    @Column(name = "is_submitted", nullable = false)
    private Boolean isSubmitted = false;

    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;

    @Column(name = "comment", columnDefinition = "TEXT")
    private String comment;

    @OneToMany(mappedBy = "assignment", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Rating> ratings = new ArrayList<>();

    public EvaluationAssignment() {}

    public EvaluationAssignment(Long id, Evaluation evaluation, User evaluator, User evaluatee,
                                Boolean isSubmitted, LocalDateTime submittedAt, String comment,
                                List<Rating> ratings) {
        this.id = id;
        this.evaluation = evaluation;
        this.evaluator = evaluator;
        this.evaluatee = evaluatee;
        this.isSubmitted = isSubmitted != null ? isSubmitted : false;
        this.submittedAt = submittedAt;
        this.comment = comment;
        this.ratings = ratings != null ? ratings : new ArrayList<>();
    }

    public Long getId() { return id; }
    public Evaluation getEvaluation() { return evaluation; }
    public User getEvaluator() { return evaluator; }
    public User getEvaluatee() { return evaluatee; }
    public Boolean getIsSubmitted() { return isSubmitted; }
    public LocalDateTime getSubmittedAt() { return submittedAt; }
    public String getComment() { return comment; }
    public List<Rating> getRatings() { return ratings; }

    public void setId(Long id) { this.id = id; }
    public void setEvaluation(Evaluation evaluation) { this.evaluation = evaluation; }
    public void setEvaluator(User evaluator) { this.evaluator = evaluator; }
    public void setEvaluatee(User evaluatee) { this.evaluatee = evaluatee; }
    public void setIsSubmitted(Boolean isSubmitted) { this.isSubmitted = isSubmitted; }
    public void setSubmittedAt(LocalDateTime submittedAt) { this.submittedAt = submittedAt; }
    public void setComment(String comment) { this.comment = comment; }
    public void setRatings(List<Rating> ratings) { this.ratings = ratings; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Long id;
        private Evaluation evaluation;
        private User evaluator;
        private User evaluatee;
        private Boolean isSubmitted = false;
        private LocalDateTime submittedAt;
        private String comment;
        private List<Rating> ratings = new ArrayList<>();

        public Builder id(Long id) { this.id = id; return this; }
        public Builder evaluation(Evaluation evaluation) { this.evaluation = evaluation; return this; }
        public Builder evaluator(User evaluator) { this.evaluator = evaluator; return this; }
        public Builder evaluatee(User evaluatee) { this.evaluatee = evaluatee; return this; }
        public Builder isSubmitted(Boolean isSubmitted) { this.isSubmitted = isSubmitted; return this; }
        public Builder submittedAt(LocalDateTime submittedAt) { this.submittedAt = submittedAt; return this; }
        public Builder comment(String comment) { this.comment = comment; return this; }

        public EvaluationAssignment build() {
            return new EvaluationAssignment(id, evaluation, evaluator, evaluatee, isSubmitted, submittedAt, comment, ratings);
        }
    }
}
