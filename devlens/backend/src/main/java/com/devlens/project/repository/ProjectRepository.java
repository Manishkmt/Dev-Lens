package com.devlens.project.repository;

import com.devlens.project.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Project entity
 * All methods enforce owner-level data isolation.
 */
@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
    List<Project> findByOwnerIdOrderByUpdatedAtDesc(Long ownerId);
    Optional<Project> findByIdAndOwnerId(Long id, Long ownerId);
    long countByOwnerId(Long ownerId);
}
