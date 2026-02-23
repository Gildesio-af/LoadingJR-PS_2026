package com.loandingjr.chat.repository;

import com.loandingjr.chat.model.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, String> {
    Page<Message> findByChatId(String chatId, Pageable pageable);

    List<Message> findByChatIdOrderBySentAtAsc(String chatId);
}
