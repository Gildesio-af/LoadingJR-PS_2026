package com.loandingjr.chat.shared.utils;

import com.loandingjr.chat.dto.chat.ChatResponseDTO;
import com.loandingjr.chat.dto.message.MessageResponseDTO;
import com.loandingjr.chat.model.Chat;
import com.loandingjr.chat.model.enums.ChatStatus;
import com.loandingjr.chat.model.specifications.ChatResponseSpec;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class ChatConverter {
    public static ChatResponseDTO modelToResponse(ChatResponseSpec spec, Page<MessageResponseDTO> messages) {
        return ChatResponseDTO.builder()
                .id(spec.getId())
                .initiatorUsername(spec.getInitiatorUsername())
                .participantUsername(spec.getParticipantUsername())
                .messages(messages)
                .aiReport(spec.getAiReport())
                .createdAt(spec.getCreatedAt())
                .closedAt(spec.getClosedAt())
                .build();
    }

    public static ChatResponseDTO modelToResponse(Chat chat) {
        return ChatResponseDTO.builder()
                .id(chat.getId())
                .initiatorUsername(chat.getInitiator().getUsername())
                .participantUsername(chat.getParticipant().getUsername())
                .aiReport(chat.getAiReport())
                .createdAt(chat.getCreatedAt())
                .closedAt(chat.getClosedAt())
                .build();
    }

    public static Chat requestToModel(String initiatorId, String participantId) {
        return Chat.builder()
                .initiator(UserConverter.idToModel(initiatorId))
                .participant(UserConverter.idToModel(participantId))
                .status(ChatStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();
    }

    public static Chat idToModel(String chatId) {
        return Chat.builder()
                .id(chatId)
                .build();
    }
}
