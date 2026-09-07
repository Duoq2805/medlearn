package com.duoq.medlearn.knowledge.section.repository;

import com.duoq.medlearn.knowledge.section.entity.SectionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SectionTypeRepository extends JpaRepository<SectionType, Integer> {

    Optional<SectionType> findByName(String name);

    boolean existsByName(String name);
}