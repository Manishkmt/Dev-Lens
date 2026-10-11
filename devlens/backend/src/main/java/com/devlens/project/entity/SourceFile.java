package com.devlens.project.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * SourceFile Entity
 * Represents code files within a DevLens project.
 */
@Entity
@Table(name = "source_files")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SourceFile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "file_name", nullable = false)
    private String fileName;

    @Column(name = "file_path")
    private String path;

    @Column(nullable = false, length = 50)
    private String language;

    @Column(columnDefinition = "LONGTEXT")
    private String content;

    @Column(name = "quality_score")
    private Integer qualityScore;

    @Builder.Default
    @Column(name = "issues_count")
    private Integer issuesCount = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    @JsonIgnore
    private Project project;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.path == null) {
            this.path = this.fileName;
        }
        if (this.issuesCount == null) {
            this.issuesCount = 0;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
