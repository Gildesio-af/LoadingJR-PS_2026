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
import com.loandingjr.chat.shared.exception.ChatStatusException;
import com.loandingjr.chat.shared.exception.EntityNotFoundException;
import com.loandingjr.chat.shared.exception.UserChatAccessException;
import com.loandingjr.chat.shared.exception.UserAlreadyIsInChatException;
import com.loandingjr.chat.shared.utils.ChatConverter;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
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
                .orElseThrow(() -> new EntityNotFoundException("Chat", chatId));

        Page<MessageResponseDTO> messages = messageService.getChatHistory(chatId, pageable);

        return ChatConverter.modelToResponse(chat, messages);
    }

    public ChatResponseDTO requestChat(String initiatorId, ChatRequestDTO chatRequestDTO) {
        User recipient = userRepository.findById(chatRequestDTO.participantId())
                .orElseThrow(() -> new EntityNotFoundException("User", chatRequestDTO.participantId()));

        if (chatRepository.isUserBusy(initiatorId))
            throw new UserAlreadyIsInChatException("User with ID " + initiatorId + " is already in an active chat");
        else if (chatRepository.isUserBusy(chatRequestDTO.participantId()))
            throw new UserAlreadyIsInChatException("User with ID " + chatRequestDTO.participantId() + " is already in an active chat");

        User sender = userRepository.findById(initiatorId)
                .orElseThrow(() -> new EntityNotFoundException("User", initiatorId));


        Chat newChat = ChatConverter.requestToModel(initiatorId, chatRequestDTO.participantId());
        newChat.setInitiator(sender);
        newChat.setParticipant(recipient);

        return ChatConverter.modelToResponse(chatRepository.save(newChat));
    }

    public void acceptChat(String chatId, String participantId) {
        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new EntityNotFoundException("Chat with ID " + chatId + " does not exist"));

        if (chat.getStatus() != ChatStatus.PENDING)
            throw new ChatStatusException("Chat with ID " + chatId + " is not in a pending state");

        if (!chat.getParticipant().getId().equals(participantId))
            throw new UserChatAccessException("User with ID " + participantId + " is not the participant of this chat");

        if (chatRepository.isUserBusy(participantId))
            throw new UserAlreadyIsInChatException("User with ID " + participantId + " is already in an active chat");

        chat.setStatus(ChatStatus.ACTIVE);
        chatRepository.save(chat);
    }

    public void closeChat(String chatId, String userId) {
        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new EntityNotFoundException("Chat", chatId));

        if (chat.getStatus() != ChatStatus.ACTIVE)
            throw new ChatStatusException("Chat with ID " + chatId + " is not active and cannot be closed");

        if (!chat.getInitiator().getId().equals(userId) && !chat.getParticipant().getId().equals(userId))
            throw new UserChatAccessException("User with ID " + userId + " is not the participant of this chat");

        chat.setStatus(ChatStatus.CLOSED);
        chat.setClosedAt(LocalDateTime.now());
        aiIntegrationService.generateAndSaveReport(chatId);
        chatRepository.save(chat);
    }

    public void rejectChat(String chatId, String participantId) {
        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new EntityNotFoundException("Chat with ID " + chatId + " does not exist"));

        if (chat.getStatus() != ChatStatus.PENDING)
            throw new ChatStatusException("Chat with ID " + chatId + " is not in a pending state");

        if (!chat.getParticipant().getId().equals(participantId))
            throw new UserChatAccessException("User with ID " + participantId + " is not the participant of this chat");

        chat.setStatus(ChatStatus.CLOSED);
        chat.setAiReport("Chat request was rejected by the participant.");
        chat.setClosedAt(LocalDateTime.now());
        chatRepository.save(chat);
    }

    public @Nullable Page<ChatResponseDTO> getPendingChatForUser(String id, Pageable pageable) {
        return chatRepository.findPendingChatForUser(id, pageable)
                .map(chat -> ChatConverter.modelToResponse(chat, Page.empty()));
    }
    public Page<ChatResponseDTO> getChatHistoryForUser(String id, Pageable pageable) {
        return chatRepository.findHistoryForUser(id, pageable)
                .map(chat -> ChatConverter.modelToResponse(chat, Page.empty()));
    }
}
