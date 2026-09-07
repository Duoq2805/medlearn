package com.duoq.medlearn.knowledge.section.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;

@Entity
@Table(name = "section_type")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SectionType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // e.g. "definition", "symptoms", "causes", "diagnosis", "treatment", "prevention"
    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;
}