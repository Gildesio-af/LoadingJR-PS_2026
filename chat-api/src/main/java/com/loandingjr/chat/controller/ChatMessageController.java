package com.loandingjr.chat.controller;

import com.loandingjr.chat.dto.message.MessageRequestDTO;
import com.loandingjr.chat.dto.message.MessageResponseDTO;
import com.loandingjr.chat.security.CustomUserDetails;
import com.loandingjr.chat.service.ChatService;
import com.loandingjr.chat.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class ChatMessageController {
    private final SimpMessagingTemplate messagingTemplate;
    private final ChatService chatService;
    private final MessageService messageService;

    @MessageMapping("/chat/{chatId}/sendMessage")
    public void sendMessage(
            @DestinationVariable String chatId,
            @Payload MessageRequestDTO messagePayload,
            Authentication authentication) {

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        String senderId = userDetails.getId();

        if (!chatService.isChatActive(chatId)) {
            throw new IllegalStateException("Chat with ID " + chatId + " is not active. Cannot send messages.");
        }

        MessageResponseDTO savedMessage = messageService.sendMessage(chatId, messagePayload, senderId);

        messagingTemplate.convertAndSend("/topic/chat/" + chatId, savedMessage);
    }
}
