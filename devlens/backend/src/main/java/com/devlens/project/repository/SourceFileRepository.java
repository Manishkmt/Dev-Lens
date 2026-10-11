package com.devlens.project.repository;

import com.devlens.project.entity.SourceFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for SourceFile entity
 * Enforces owner-level data isolation via project association.
 */
@Repository
public interface SourceFileRepository extends JpaRepository<SourceFile, Long> {
    
    @Query("SELECT f FROM SourceFile f WHERE f.project.id = :projectId AND f.project.owner.id = :ownerId ORDER BY f.createdAt ASC")
    List<SourceFile> findByProjectIdAndOwnerId(@Param("projectId") Long projectId, @Param("ownerId") Long ownerId);

    @Query("SELECT f FROM SourceFile f WHERE f.id = :id AND f.project.owner.id = :ownerId")
    Optional<SourceFile> findByIdAndOwnerId(@Param("id") Long id, @Param("ownerId") Long ownerId);

    long countByProjectId(Long projectId);
}
