package com.example.backend.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "evaluations")
public class Evaluation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private LocalDateTime deadline;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private EStatus status = EStatus.ACTIVE;

    @OneToMany(mappedBy = "evaluation", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EvaluationAssignment> assignments = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public Evaluation() {}

    public Evaluation(Long id, String title, String description, LocalDateTime deadline,
                      User createdBy, EStatus status, List<EvaluationAssignment> assignments,
                      LocalDateTime createdAt) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.deadline = deadline;
        this.createdBy = createdBy;
        this.status = status != null ? status : EStatus.ACTIVE;
        this.assignments = assignments != null ? assignments : new ArrayList<>();
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public LocalDateTime getDeadline() { return deadline; }
    public User getCreatedBy() { return createdBy; }
    public EStatus getStatus() { return status; }
    public List<EvaluationAssignment> getAssignments() { return assignments; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public void setId(Long id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setDescription(String description) { this.description = description; }
    public void setDeadline(LocalDateTime deadline) { this.deadline = deadline; }
    public void setCreatedBy(User createdBy) { this.createdBy = createdBy; }
    public void setStatus(EStatus status) { this.status = status; }
    public void setAssignments(List<EvaluationAssignment> assignments) { this.assignments = assignments; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Long id;
        private String title;
        private String description;
        private LocalDateTime deadline;
        private User createdBy;
        private EStatus status = EStatus.ACTIVE;
        private List<EvaluationAssignment> assignments = new ArrayList<>();
        private LocalDateTime createdAt;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder title(String title) { this.title = title; return this; }
        public Builder description(String description) { this.description = description; return this; }
        public Builder deadline(LocalDateTime deadline) { this.deadline = deadline; return this; }
        public Builder createdBy(User createdBy) { this.createdBy = createdBy; return this; }
        public Builder status(EStatus status) { this.status = status; return this; }

        public Evaluation build() {
            return new Evaluation(id, title, description, deadline, createdBy, status, assignments, createdAt);
        }
    }
}
