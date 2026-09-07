package com.duoq.medlearn.ai.feedback.repository;

import com.duoq.medlearn.ai.enums.FeatureType;
import com.duoq.medlearn.ai.feedback.entity.AiFeedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AiFeedbackRepository extends JpaRepository<AiFeedback, Long> {

    Optional<AiFeedback> findByFeatureTypeAndFeatureIdAndUserId(FeatureType featureType, Long featureId, Long userId);

    List<AiFeedback> findAllByFeatureTypeAndFeatureId(FeatureType featureType, Long featureId);

    boolean existsByFeatureTypeAndFeatureIdAndUserId(FeatureType featureType, Long featureId, Long userId);
}
