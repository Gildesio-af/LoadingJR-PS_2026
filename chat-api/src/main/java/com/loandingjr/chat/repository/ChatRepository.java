package com.loandingjr.chat.repository;

import com.loandingjr.chat.model.Chat;
import com.loandingjr.chat.model.specifications.ChatResponseProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ChatRepository extends JpaRepository<Chat, String> {
    @Query("""
        SELECT c.id AS id,
                c.status AS status,
                u1.username AS initiatorUsername,
                u2.username AS participantUsername,
                c.createdAt AS createdAt,
                c.closedAt AS closedAt,
                c.aiReport AS aiReport
        FROM Chat c
        JOIN c.initiator u1 ON c.initiator.id = u1.id
        JOIN c.participant u2 ON c.participant.id = u2.id
        WHERE c.id = :id
        """)
    Optional<ChatResponseProjection> findSpecById(String id);

    @Query("""
        SELECT c.id AS id,
                c.status AS status,
                u1.username AS initiatorUsername,
                u2.username AS participantUsername,
                c.createdAt AS createdAt,
                c.closedAt AS closedAt,
                c.aiReport AS aiReport
        FROM Chat c
        JOIN c.initiator u1 ON c.initiator.id = u1.id
        JOIN c.participant u2 ON c.participant.id = u2.id
        WHERE (c.initiator.id = :userId OR c.participant.id = :userId)
        AND c.status IN ('ACTIVE', 'PENDING')
        """)
    Page<ChatResponseProjection> findPendingChatForUser(@Param("userId") String id, Pageable pageable);

    @Query("""
        SELECT COUNT(c) > 0 FROM Chat c 
            WHERE (c.initiator.id = :userId OR c.participant.id = :userId)
            AND c.status IN ('ACTIVE')
        """)
    boolean isUserBusy(@Param("userId") String userId);

    @Query("""
        SELECT COUNT(c) > 0 FROM Chat c 
            WHERE c.id = :chatId
            AND c.status IN ('ACTIVE')
        """)
    boolean isChatActive(String chatId);
}
