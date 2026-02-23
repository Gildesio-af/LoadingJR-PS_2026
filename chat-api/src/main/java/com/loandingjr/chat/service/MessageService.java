package com.loandingjr.chat.service;

import com.loandingjr.chat.dto.message.MessageRequestDTO;
import com.loandingjr.chat.dto.message.MessageResponseDTO;
import com.loandingjr.chat.model.Chat;
import com.loandingjr.chat.model.Message;
import com.loandingjr.chat.model.User;
import com.loandingjr.chat.model.enums.ChatStatus;
import com.loandingjr.chat.repository.ChatRepository;
import com.loandingjr.chat.repository.MessageRepository;
import com.loandingjr.chat.repository.UserRepository;
import com.loandingjr.chat.shared.utils.MessageConverter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MessageService {
    private final ChatRepository chatRepository;
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;

    public Page<MessageResponseDTO> getChatHistory(String chatId, Pageable pageable) {
        return messageRepository.findByChatId(chatId, pageable)
                .map(MessageConverter::modelToResponse);
    }


    public String getFormattedHistoryForAi(String chatId) {
        List<Message> messages = messageRepository.findByChatIdOrderBySentAtAsc(chatId);

        if (messages.isEmpty()) {
            return "No messages in this chat.";
        }

        StringBuilder formattedHistory = new StringBuilder();
        for (Message msg : messages) {
            formattedHistory.append(msg.getSender().getUsername())
                    .append(": ")
                    .append(msg.getContent())
                    .append("\n");
        }

        return formattedHistory.toString();
    }

    @Transactional
    public MessageResponseDTO sendMessage(String chatId, MessageRequestDTO request, String senderId) {
        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new IllegalArgumentException("Chat with ID " + chatId + " does not exist"));
        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new IllegalArgumentException("User with ID " + senderId + " does not exist"));

        if (chat.getStatus() != ChatStatus.ACTIVE)
            throw new IllegalStateException("Cannot send message to a chat that is not active");

        Message message = MessageConverter.requestToModel(request, chatId);
        message.setSender(sender);

        Message savedMessage = messageRepository.save(message);

        return MessageConverter.modelToResponse(savedMessage);
    }
}
