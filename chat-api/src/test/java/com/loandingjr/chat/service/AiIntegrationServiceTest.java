package com.loandingjr.chat.service;

import com.google.genai.Client;
import com.google.genai.Models;
import com.google.genai.types.GenerateContentResponse;
import com.loandingjr.chat.model.Chat;
import com.loandingjr.chat.repository.ChatRepository;
import com.loandingjr.chat.shared.exception.AiResponseException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import java.lang.reflect.Field;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
class AiIntegrationServiceTest {

    @Mock
    private MessageService messageService;
    @Mock
    private ChatRepository chatRepository;
    @Mock
    private Client client;
    @Mock
    private Models models;
    @Mock
    private GenerateContentResponse response;

    @InjectMocks
    private AiIntegrationService aiIntegrationService;

    private Chat chat;

    @BeforeEach
    void setUp() {
        chat = Chat.builder().id("chat-1").build();
    }

    @Test
    void generateAndSaveReport_shouldPersistReport() {
        when(messageService.getFormattedHistoryForAi(chat.getId())).thenReturn("user: hi");
        when(chatRepository.findById(chat.getId())).thenReturn(Optional.of(chat));
        when(response.text()).thenReturn("summary");

        try (var mocked = mockStatic(Client.class)) {
            Client.Builder builder = mock(Client.Builder.class);
            when(builder.apiKey(any())).thenReturn(builder);
            when(builder.build()).thenReturn(client);
            mocked.when(Client::builder).thenReturn(builder);

            Field modelsField = Client.class.getField("models");
            modelsField.setAccessible(true);
            modelsField.set(client, models);

            when(models.generateContent(any(String.class), any(String.class), any())).thenReturn(response);

            aiIntegrationService.generateAndSaveReport(chat.getId());
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }

        verify(chatRepository, atLeastOnce()).save(any(Chat.class));
    }

    @Test
    void generateAndSaveReport_shouldThrowOnError() {
        when(messageService.getFormattedHistoryForAi(chat.getId())).thenThrow(new RuntimeException("boom"));

        assertThatThrownBy(() -> aiIntegrationService.generateAndSaveReport(chat.getId()))
                .isInstanceOf(AiResponseException.class);
    }
}
