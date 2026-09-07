package com.duoq.medlearn.knowledge.version.entity;
import com.duoq.medlearn.knowledge.symptom.entity.Symptom;
import com.duoq.medlearn.knowledge.version.entity.DiseaseVersion;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "disease_version_symptom",
        uniqueConstraints = @UniqueConstraint(columnNames = {"disease_version_id", "symptom_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DiseaseVersionSymptom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "disease_version_id", nullable = false)
    private DiseaseVersion diseaseVersion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "symptom_id", nullable = false)
    private Symptom symptom;

    // Weight score để tính matching score trong Symptom Checker
    @Column(name = "weight_score", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal weightScore = BigDecimal.ONE;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;
}