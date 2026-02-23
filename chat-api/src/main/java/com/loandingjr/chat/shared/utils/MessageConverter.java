package com.loandingjr.chat.shared.utils;

import com.loandingjr.chat.dto.message.MessageRequestDTO;
import com.loandingjr.chat.dto.message.MessageResponseDTO;
import com.loandingjr.chat.model.Message;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class MessageConverter {
    public static MessageResponseDTO modelToResponse(Message message) {
        return MessageResponseDTO.builder()
                .id(message.getId())
                .senderId(message.getSender().getId())
                .content(message.getContent())
                .sentAt(message.getSentAt())
                .build();
    }

    public static Message requestToModel(MessageRequestDTO requestDTO, String chatId) {
        return Message.builder()
                .chat(ChatConverter.idToModel(chatId))
                .content(requestDTO.content())
                .sentAt(LocalDateTime.now())
                .build();
    }
}
