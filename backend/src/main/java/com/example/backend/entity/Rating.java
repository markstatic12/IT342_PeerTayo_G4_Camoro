package com.example.backend.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "ratings")
public class Rating {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignment_id", nullable = false)
    private EvaluationAssignment assignment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "criterion_id", nullable = false)
    private Criteria criterion;

    /**
     * Rating value: 1–5 per the SDD specification.
     */
    @Column(nullable = false)
    private Integer rating;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    public Rating() {}

    public Rating(Long id, EvaluationAssignment assignment, Criteria criterion, Integer rating, Boolean isActive) {
        this.id = id;
        this.assignment = assignment;
        this.criterion = criterion;
        this.rating = rating;
        this.isActive = isActive != null ? isActive : true;
    }

    public Long getId() { return id; }
    public EvaluationAssignment getAssignment() { return assignment; }
    public Criteria getCriterion() { return criterion; }
    public Integer getRating() { return rating; }
    public Boolean getIsActive() { return isActive; }

    public void setId(Long id) { this.id = id; }
    public void setAssignment(EvaluationAssignment assignment) { this.assignment = assignment; }
    public void setCriterion(Criteria criterion) { this.criterion = criterion; }
    public void setRating(Integer rating) { this.rating = rating; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Long id;
        private EvaluationAssignment assignment;
        private Criteria criterion;
        private Integer rating;
        private Boolean isActive = true;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder assignment(EvaluationAssignment assignment) { this.assignment = assignment; return this; }
        public Builder criterion(Criteria criterion) { this.criterion = criterion; return this; }
        public Builder rating(Integer rating) { this.rating = rating; return this; }
        public Builder isActive(Boolean isActive) { this.isActive = isActive; return this; }

        public Rating build() { return new Rating(id, assignment, criterion, rating, isActive); }
    }
}
