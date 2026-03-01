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
import com.loandingjr.chat.shared.exception.ChatStatusException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MessageServiceTest {

    @Mock
    private ChatRepository chatRepository;
    @Mock
    private MessageRepository messageRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private MessageService messageService;

    private Chat chat;
    private User sender;

    @BeforeEach
    void setUp() {
        sender = User.builder()
                .id("user-1")
                .username("john")
                .email("john@test.com")
                .build();

        chat = Chat.builder()
                .id("chat-1")
                .status(ChatStatus.ACTIVE)
                .initiator(sender)
                .participant(User.builder().id("user-2").username("jane").email("jane@test.com").build())
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void getChatHistory_shouldMapMessages() {
        Message message = Message.builder()
                .id("msg-1")
                .content("hello")
                .sentAt(LocalDateTime.now())
                .sender(sender)
                .build();

        when(messageRepository.findByChatId(eq(chat.getId()), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(message)));

        MessageResponseDTO response = messageService.getChatHistory(chat.getId(), Pageable.unpaged()).getContent().getFirst();

        assertThat(response.id()).isEqualTo(message.getId());
        assertThat(response.content()).isEqualTo(message.getContent());
        assertThat(response.senderUsername()).isEqualTo(sender.getUsername());
    }

    @Test
    void getFormattedHistoryForAi_shouldReturnFormattedText() {
        Message message = Message.builder()
                .id("msg-1")
                .content("hello")
                .sentAt(LocalDateTime.now())
                .sender(sender)
                .build();

        when(messageRepository.findByChatIdOrderBySentAtAsc(chat.getId()))
                .thenReturn(List.of(message));

        String formatted = messageService.getFormattedHistoryForAi(chat.getId());

        assertThat(formatted).contains("john: hello");
    }

    @Test
    void sendMessage_shouldThrowWhenChatNotActive() {
        chat.setStatus(ChatStatus.CLOSED);
        when(chatRepository.findById(chat.getId())).thenReturn(Optional.of(chat));
        when(userRepository.findById(sender.getId())).thenReturn(Optional.of(sender));

        assertThatThrownBy(() -> messageService.sendMessage(chat.getId(), new MessageRequestDTO("hi"), sender.getId()))
                .isInstanceOf(ChatStatusException.class);
    }

    @Test
    void sendMessage_shouldSaveWhenActive() {
        when(chatRepository.findById(chat.getId())).thenReturn(Optional.of(chat));
        when(userRepository.findById(sender.getId())).thenReturn(Optional.of(sender));
        when(messageRepository.save(any(Message.class))).thenAnswer(invocation -> {
            Message saved = invocation.getArgument(0);
            saved.setId("msg-new");
            return saved;
        });

        MessageResponseDTO response = messageService.sendMessage(chat.getId(), new MessageRequestDTO("hi"), sender.getId());

        assertThat(response.id()).isEqualTo("msg-new");
        assertThat(response.senderUsername()).isEqualTo(sender.getUsername());
    }
}
