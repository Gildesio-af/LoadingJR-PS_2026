package com.loandingjr.chat.controller;

import com.loandingjr.chat.dto.message.MessageRequestDTO;
import com.loandingjr.chat.dto.message.MessageResponseDTO;
import com.loandingjr.chat.security.CustomUserDetails;
import com.loandingjr.chat.service.ChatService;
import com.loandingjr.chat.service.MessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
@Tag(name = "Chat Message WebSocket Controller", description = "Controller for handling real-time WebSocket messages within a chat session.")
public class ChatMessageController {
    private final SimpMessagingTemplate messagingTemplate;
    private final ChatService chatService;
    private final MessageService messageService;

    @Operation(summary = "Send a message via WebSocket", description = "Receives a message payload via WebSocket and broadcasts it to subscribers of the chat topic. " +
            "This endpoint is not RESTful HTTP but part of the STOMP messaging flow.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Message processed and broadcasted successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid message payload"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - user must be authenticated"),
            @ApiResponse(responseCode = "403", description = "User not authorized to send messages to this chat")
    })
    @MessageMapping("/chat/{chatId}/sendMessage")
    public void sendMessage(
            @Parameter(description = "ID of the chat room where the message is being sent") @DestinationVariable String chatId,
            @Parameter(description = "The message payload containing the content") @Payload MessageRequestDTO messagePayload,
            @Parameter(hidden = true) Authentication authentication) {

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        String senderId = userDetails.getId();

        if (!chatService.isChatActive(chatId)) {
            throw new IllegalStateException("Chat with ID " + chatId + " is not active. Cannot send messages.");
        }

        MessageResponseDTO savedMessage = messageService.sendMessage(chatId, messagePayload, senderId);

        messagingTemplate.convertAndSend("/topic/chat/" + chatId, savedMessage);
    }
}
