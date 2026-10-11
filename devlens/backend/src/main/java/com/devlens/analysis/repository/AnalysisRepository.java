package com.devlens.analysis.repository;

import com.devlens.analysis.entity.Analysis;
import com.devlens.analysis.entity.AnalysisType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Analysis entity
 * Enforces user-level isolation across all analysis lookups and aggregations.
 */
@Repository
public interface AnalysisRepository extends JpaRepository<Analysis, Long>, JpaSpecificationExecutor<Analysis> {

    Optional<Analysis> findByIdAndUserId(Long id, Long userId);

    long countByUserId(Long userId);

    @Query("SELECT AVG(a.qualityScore) FROM Analysis a WHERE a.user.id = :userId AND a.qualityScore IS NOT NULL")
    Double getAverageQualityScoreByUserId(@Param("userId") Long userId);

    @Query("SELECT DISTINCT a.language FROM Analysis a WHERE a.user.id = :userId ORDER BY a.language ASC")
    List<String> findDistinctLanguagesByUserId(@Param("userId") Long userId);

    @Query("SELECT a.type, COUNT(a) FROM Analysis a WHERE a.user.id = :userId GROUP BY a.type")
    List<Object[]> countAnalysesGroupedByType(@Param("userId") Long userId);

    List<Analysis> findTop10ByUserIdOrderByCreatedAtDesc(Long userId);

    @Query("SELECT a FROM Analysis a WHERE a.user.id = :userId AND a.createdAt >= :since ORDER BY a.createdAt ASC")
    List<Analysis> findRecentAnalysesSince(@Param("userId") Long userId, @Param("since") LocalDateTime since);
}
