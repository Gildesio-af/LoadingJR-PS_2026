package com.loandingjr.chat.controller;

import com.loandingjr.chat.dto.chat.ChatRequestDTO;
import com.loandingjr.chat.dto.chat.ChatResponseDTO;
import com.loandingjr.chat.security.CustomUserDetails;
import com.loandingjr.chat.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/chats")
@RequiredArgsConstructor
public class ChatController {
    private final ChatService chatService;

    @PostMapping
    public ResponseEntity<ChatResponseDTO> createChat(@RequestBody ChatRequestDTO request,
                                                      @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(chatService.requestChat(userDetails.getId(), request));
    }

    @PatchMapping("/{chatId}/accept")
    public ResponseEntity<Void> acceptChat(@PathVariable String chatId,
                                           @AuthenticationPrincipal CustomUserDetails userDetails) {
        chatService.acceptChat(chatId, userDetails.getId());
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{chatId}/close")
    public ResponseEntity<Void> closeChat(@PathVariable String chatId,
                                         @AuthenticationPrincipal CustomUserDetails userDetails) {
        chatService.closeChat(chatId, userDetails.getId());
        return ResponseEntity.ok().build();
    }
}
