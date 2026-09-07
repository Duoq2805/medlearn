package com.duoq.medlearn.knowledge.disease.entity;
import com.duoq.medlearn.knowledge.version.entity.DiseaseVersion;
import com.duoq.medlearn.knowledge.category.entity.Category;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.NaturalId;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "disease")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Disease {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 255)
    private String name;

    @NaturalId
    @Column(nullable = false, unique = true, length = 255)
    private String slug;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "current_version_id")
    private DiseaseVersion currentVersion;

    @OneToMany(mappedBy = "disease", cascade = CascadeType.ALL)
    @Builder.Default
    private Set<DiseaseVersion> versions = new LinkedHashSet<>();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    @Column(name = "deleted_at")
    private OffsetDateTime deletedAt;

    @Version
    @Column(nullable = false)
    @Builder.Default
    private Integer version = 0;

    public boolean hasCurrentVersion() {
        return currentVersion != null;
    }

    public void setCurrentVersion(DiseaseVersion version) {
        this.currentVersion = version;
    }
}
