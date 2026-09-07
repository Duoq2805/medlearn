package com.duoq.medlearn.ai.generation.repository;

import com.duoq.medlearn.ai.enums.FeatureType;
import com.duoq.medlearn.ai.generation.entity.AiGeneration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AiGenerationRepository extends JpaRepository<AiGeneration, Long> {

    Optional<AiGeneration> findByFeatureTypeAndFeatureId(FeatureType featureType, Long featureId);

    List<AiGeneration> findAllByFeatureTypeAndFeatureIdOrderByCreatedAtDesc(FeatureType featureType, Long featureId);
}
