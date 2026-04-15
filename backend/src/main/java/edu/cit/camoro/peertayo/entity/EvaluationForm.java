package edu.cit.camoro.peertayo.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "evaluation_forms")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EvaluationForm {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, length = 4000)
    private String description;

    @Column(name = "deadline", nullable = false)
    private LocalDateTime deadline;

    @Column(nullable = false, length = 30)
    private String status;

    @Lob
    @Column(name = "criteria_json")
    private String criteriaJson;

    @Lob
    @Column(name = "questions_json")
    private String questionsJson;

    @Lob
    @Column(name = "rating_fields_json")
    private String ratingFieldsJson;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
