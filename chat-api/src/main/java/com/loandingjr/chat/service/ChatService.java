package com.loandingjr.chat.service;

import com.loandingjr.chat.dto.chat.ChatRequestDTO;
import com.loandingjr.chat.dto.chat.ChatResponseDTO;
import com.loandingjr.chat.model.Chat;
import com.loandingjr.chat.model.enums.ChatStatus;
import com.loandingjr.chat.repository.ChatRepository;
import com.loandingjr.chat.shared.utils.ChatConverter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class ChatService {
    private final ChatRepository chatRepository;

    public ChatResponseDTO requestChat(String initiatorId, ChatRequestDTO chatRequestDTO) {
        if (chatRepository.isUserBusy(initiatorId))
            throw new IllegalStateException("User with ID " + initiatorId + " is already in an active chat");
        else if (chatRepository.isUserBusy(chatRequestDTO.participantId()))
            throw new IllegalStateException("User with ID " + chatRequestDTO.participantId() + " is already in an active chat");

        Chat newChat = ChatConverter.requestToModel(initiatorId, chatRequestDTO.participantId());
        return ChatConverter.modelToResponse(chatRepository.save(newChat));
    }

    public void acceptChat(String chatId, String participantId) {
        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new IllegalArgumentException("Chat with ID " + chatId + " does not exist"));

        if (chat.getStatus() != ChatStatus.PENDING)
            throw new IllegalStateException("Chat with ID " + chatId + " is not in a pending state");

        if (!chat.getParticipant().getId().equals(participantId))
            throw new IllegalArgumentException("User with ID " + participantId + " is not the participant of this chat");

        chat.setStatus(ChatStatus.ACTIVE);
        chatRepository.save(chat);
    }

    public void closeChat(String chatId, String userId) {
        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new IllegalArgumentException("Chat with ID " + chatId + " does not exist"));

        if (chat.getStatus() != ChatStatus.ACTIVE)
            throw new IllegalStateException("Chat with ID " + chatId + " is not in an active state");

        if (!chat.getInitiator().getId().equals(userId) && !chat.getParticipant().getId().equals(userId))
            throw new IllegalArgumentException("User with ID " + userId + " is not a participant of this chat");

        chat.setStatus(ChatStatus.CLOSED);
        chat.setClosedAt(LocalDateTime.now());
        //TODO: trigger AI report generation asynchronously here
        chatRepository.save(chat);
    }

    public boolean isChatActive(String chatId) {
        return chatRepository.isChatActive(chatId);
    }
}
