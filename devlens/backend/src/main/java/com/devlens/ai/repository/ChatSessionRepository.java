package com.devlens.ai.repository;

import com.devlens.ai.entity.ChatSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * ChatSession Repository with strict user tenant scoping
 */
@Repository
public interface ChatSessionRepository extends JpaRepository<ChatSession, String> {

    List<ChatSession> findByUserIdOrderByUpdatedAtDesc(Long userId);

    Optional<ChatSession> findByIdAndUserId(String id, Long userId);
}
