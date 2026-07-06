package com.duoq.medlearn.ai.repository;

import com.duoq.medlearn.ai.prompt.entity.PromptTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PromptTemplateRepository extends JpaRepository<PromptTemplate, Long> {

    Optional<PromptTemplate> findByCodeAndVersion(String code, String version);

    List<PromptTemplate> findByCodeOrderByCreatedAtDesc(String code);

    Optional<PromptTemplate> findTopByCodeAndStatusOrderByCreatedAtDesc(String code, String status);

    List<PromptTemplate> findByStatus(String status);

    boolean existsByCodeAndVersion(String code, String version);
}
