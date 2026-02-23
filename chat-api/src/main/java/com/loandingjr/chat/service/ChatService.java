package com.loandingjr.chat.service;

import com.loandingjr.chat.dto.chat.ChatRequestDTO;
import com.loandingjr.chat.dto.chat.ChatResponseDTO;
import com.loandingjr.chat.dto.message.MessageResponseDTO;
import com.loandingjr.chat.model.Chat;
import com.loandingjr.chat.model.User;
import com.loandingjr.chat.model.enums.ChatStatus;
import com.loandingjr.chat.model.specifications.ChatResponseProjection;
import com.loandingjr.chat.repository.ChatRepository;
import com.loandingjr.chat.repository.UserRepository;
import com.loandingjr.chat.shared.utils.ChatConverter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class ChatService {
    private final ChatRepository chatRepository;
    private final UserRepository userRepository;
    private final MessageService messageService;
    private final AiIntegrationService aiIntegrationService;

    @Transactional(readOnly = true)
    public boolean isChatActive(String chatId) {
        return chatRepository.isChatActive(chatId);
    }

    public ChatResponseDTO getChatById(String chatId, Pageable pageable) {
        ChatResponseProjection chat = chatRepository.findSpecById(chatId)
                .orElseThrow(() -> new IllegalArgumentException("Chat with ID " + chatId + " does not exist"));

        Page<MessageResponseDTO> messages = messageService.getChatHistory(chatId, pageable);

        return ChatConverter.modelToResponse(chat, messages);
    }

    public ChatResponseDTO requestChat(String initiatorId, ChatRequestDTO chatRequestDTO) {
        if (chatRepository.isUserBusy(initiatorId))
            throw new IllegalStateException("User with ID " + initiatorId + " is already in an active chat");
        else if (chatRepository.isUserBusy(chatRequestDTO.participantId()))
            throw new IllegalStateException("User with ID " + chatRequestDTO.participantId() + " is already in an active chat");

        User sender = userRepository.findById(initiatorId)
                .orElseThrow(() -> new IllegalArgumentException("User with ID " + initiatorId + " does not exist"));
        User recipient = userRepository.findById(chatRequestDTO.participantId())
                .orElseThrow(() -> new IllegalArgumentException("User with ID " + chatRequestDTO.participantId() + " does not exist"));

        Chat newChat = ChatConverter.requestToModel(initiatorId, chatRequestDTO.participantId());
        newChat.setInitiator(sender);
        newChat.setParticipant(recipient);

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
        aiIntegrationService.generateAndSaveReport(chatId);
        chatRepository.save(chat);
    }
}
