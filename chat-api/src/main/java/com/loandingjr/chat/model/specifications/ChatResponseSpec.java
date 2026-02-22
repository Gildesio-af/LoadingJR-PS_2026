package com.loandingjr.chat.model.specifications;

import com.loandingjr.chat.model.enums.ChatStatus;

import java.time.LocalDateTime;

public interface ChatResponseSpec {
        String getId();
        ChatStatus getStatus();
        String getParticipantUsername();
        String getInitiatorUsername();
        LocalDateTime getCreatedAt();
        LocalDateTime getClosedAt();
        String getAiReport();
}
