package com.duoq.medlearn.repository;

import com.duoq.medlearn.domain.entity.Symptom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SymptomRepository extends JpaRepository<Symptom, Long> {

    Optional<Symptom> findBySlug(String slug);

    boolean existsBySlug(String slug);

    boolean existsByName(String name);

    List<Symptom> findAllByIdIn(List<Long> ids);
}