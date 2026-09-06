package com.duoq.medlearn.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

@NoRepositoryBean
public interface BaseRepository<T, ID> extends JpaRepository<T, ID> {

    @Query("SELECT e FROM #{#entityName} e WHERE e.slug = :slug AND e.deletedAt IS NULL")
    Optional<T> findBySlug(@Param("slug") String slug);

    @Query("SELECT COUNT(e) > 0 FROM #{#entityName} e WHERE e.slug = :slug AND e.deletedAt IS NULL")
    boolean existsBySlug(@Param("slug") String slug);

    // Check if slug exists excluding a specific entity (for update operations)
    @Query("SELECT COUNT(e) > 0 FROM #{#entityName} e WHERE e.slug = :slug AND e.id != :id AND e.deletedAt IS NULL")
    boolean existsBySlugAndIdNot(@Param("slug") String slug, @Param("id") ID id);
}