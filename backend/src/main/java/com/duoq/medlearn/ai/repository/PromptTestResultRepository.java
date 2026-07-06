package com.duoq.medlearn.ai.repository;

import com.duoq.medlearn.ai.prompt.entity.PromptTestResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PromptTestResultRepository extends JpaRepository<PromptTestResult, Long> {

    List<PromptTestResult> findByPromptCodeOrderByExecutedAtDesc(String promptCode);

    List<PromptTestResult> findByPromptCodeAndVersionOrderByExecutedAtDesc(String promptCode, String version);
}
