package com.loandingjr.chat.repository;

import com.loandingjr.chat.model.Chat;
import com.loandingjr.chat.model.specifications.ChatResponseSpec;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ChatRepository extends JpaRepository<Chat, String> {
    boolean existsById(String id);

    @Query("""
        SELECT c.id,
                c.status,
                u1.username AS initiatorUsername,
                u2.username AS participantUsername,
                c.createdAt,
                c.closedAt,
                c.aiReport
        FROM Chat c
        JOIN c.initiator u1 ON c.initiator.id = u1.id
        JOIN c.participant u2 ON c.participant.id = u2.id
        WHERE c.id = :id
        """)
    Optional<ChatResponseSpec> findSpecById(String id);

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
