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
import com.loandingjr.chat.shared.exception.UserAlreadyIsInChatException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatServiceTest {

    @Mock
    private ChatRepository chatRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private MessageService messageService;
    @Mock
    private AiIntegrationService aiIntegrationService;

    @InjectMocks
    private ChatService chatService;

    private User initiator;
    private User participant;
    private Chat chat;

    @BeforeEach
    void setUp() {
        initiator = User.builder()
                .id("init-1")
                .username("initiator")
                .email("init@test.com")
                .build();

        participant = User.builder()
                .id("part-1")
                .username("participant")
                .email("part@test.com")
                .build();

        chat = Chat.builder()
                .id("chat-1")
                .initiator(initiator)
                .participant(participant)
                .status(ChatStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void requestChat_shouldCreateWhenUsersNotBusy() {
        ChatRequestDTO dto = new ChatRequestDTO(participant.getId());

        when(chatRepository.isUserBusy(initiator.getId())).thenReturn(false);
        when(chatRepository.isUserBusy(participant.getId())).thenReturn(false);
        when(userRepository.findById(initiator.getId())).thenReturn(Optional.of(initiator));
        when(userRepository.findById(participant.getId())).thenReturn(Optional.of(participant));
        when(chatRepository.save(any(Chat.class))).thenAnswer(invocation -> {
            Chat toSave = invocation.getArgument(0);
            toSave.setId("chat-new");
            return toSave;
        });

        ChatResponseDTO response = chatService.requestChat(initiator.getId(), dto);

        assertThat(response.id()).isEqualTo("chat-new");
        assertThat(response.status()).isEqualTo(ChatStatus.PENDING);
        verify(chatRepository, times(1)).save(any(Chat.class));
    }

    @Test
    void requestChat_shouldFailWhenParticipantBusy() {
        ChatRequestDTO dto = new ChatRequestDTO(participant.getId());

        when(chatRepository.isUserBusy(initiator.getId())).thenReturn(false);
        when(chatRepository.isUserBusy(participant.getId())).thenReturn(true);
        when(userRepository.findById(participant.getId())).thenReturn(Optional.of(participant));

        assertThatThrownBy(() -> chatService.requestChat(initiator.getId(), dto))
                .isInstanceOf(UserAlreadyIsInChatException.class);
    }

    @Test
    void acceptChat_shouldActivateWhenPendingAndNotBusy() {
        when(chatRepository.findById(chat.getId())).thenReturn(Optional.of(chat));
        when(chatRepository.isUserBusy(participant.getId())).thenReturn(false);

        chatService.acceptChat(chat.getId(), participant.getId());

        ArgumentCaptor<Chat> captor = ArgumentCaptor.forClass(Chat.class);
        verify(chatRepository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(ChatStatus.ACTIVE);
    }

    @Test
    void acceptChat_shouldThrowWhenNotPending() {
        chat.setStatus(ChatStatus.ACTIVE);
        when(chatRepository.findById(chat.getId())).thenReturn(Optional.of(chat));

        assertThatThrownBy(() -> chatService.acceptChat(chat.getId(), participant.getId()))
                .isInstanceOf(ChatStatusException.class);
    }

    @Test
    void closeChat_shouldCloseAndTriggerReport() {
        chat.setStatus(ChatStatus.ACTIVE);
        when(chatRepository.findById(chat.getId())).thenReturn(Optional.of(chat));

        chatService.closeChat(chat.getId(), initiator.getId());

        ArgumentCaptor<Chat> captor = ArgumentCaptor.forClass(Chat.class);
        verify(aiIntegrationService).generateAndSaveReport(chat.getId());
        verify(chatRepository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(ChatStatus.CLOSED);
        assertThat(captor.getValue().getClosedAt()).isNotNull();
    }

    @Test
    void rejectChat_shouldSetClosedStatusAndReason() {
        when(chatRepository.findById(chat.getId())).thenReturn(Optional.of(chat));

        chatService.rejectChat(chat.getId(), participant.getId());

        ArgumentCaptor<Chat> captor = ArgumentCaptor.forClass(Chat.class);
        verify(chatRepository).save(captor.capture());
        Chat saved = captor.getValue();
        assertThat(saved.getStatus()).isEqualTo(ChatStatus.CLOSED);
        assertThat(saved.getAiReport()).contains("rejected");
        assertThat(saved.getClosedAt()).isNotNull();
    }

    @Test
    void getChatById_shouldReturnMessages() {
        ChatResponseProjection projection = mock(ChatResponseProjection.class);
        when(projection.getId()).thenReturn(chat.getId());
        when(projection.getInitiatorUsername()).thenReturn(initiator.getUsername());
        when(projection.getParticipantUsername()).thenReturn(participant.getUsername());
        when(projection.getStatus()).thenReturn(ChatStatus.PENDING);
        when(projection.getCreatedAt()).thenReturn(LocalDateTime.now());
        when(chatRepository.findSpecById(chat.getId())).thenReturn(Optional.of(projection));

        Page<MessageResponseDTO> messages = new PageImpl<>(java.util.List.of());
        when(messageService.getChatHistory(eq(chat.getId()), any(Pageable.class))).thenReturn(messages);

        ChatResponseDTO response = chatService.getChatById(chat.getId(), Pageable.unpaged());

        assertThat(response.id()).isEqualTo(chat.getId());
        assertThat(response.initiatorUsername()).isEqualTo(initiator.getUsername());
        assertThat(response.messages()).isEqualTo(messages);
    }
}
