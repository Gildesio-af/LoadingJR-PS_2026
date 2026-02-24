package com.loandingjr.chat.service;

import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;
import com.loandingjr.chat.model.Chat;
import com.loandingjr.chat.repository.ChatRepository;
import com.loandingjr.chat.shared.exception.AiResponseException;
import com.loandingjr.chat.shared.exception.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiIntegrationService {

    private final MessageService messageService;
    private final ChatRepository chatRepository;

//    @Value("${ai.gemini.api-key}")
    private String apiKey = "AIzaSyCxmydMRkc2qMtuJWgDjsBjBPH5viFhxBg";

    @Async
    @Transactional
    public void generateAndSaveReport(String chatId) {
        try {
            String history = messageService.getFormattedHistoryForAi(chatId);

            if (history.isBlank() || history.contains("No messages in this chat.")) {
                updateAiReport(chatId, "Chat ended without any messages. No AI report generated.");
                return;
            }

            String prompt = "Summarize the following chat conversation and provide insights on the main topics discussed, sentiment, and any action items:\n\n" + history;

            Client client = Client.builder()
                    .apiKey(apiKey)
                    .build();

            GenerateContentResponse response = client.models.generateContent(
                    "gemini-2.5-flash-lite",
                    prompt,
                    null
            );

            updateAiReport(chatId, response.text());

        } catch (Exception e) {
            log.error(e.getMessage());
            throw new AiResponseException("Failed to generate AI report for chat ID: " + chatId);
        }
    }

    private void updateAiReport(String chatId, String report) {
        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new EntityNotFoundException("Chat", chatId));
        chat.setAiReport(report);
        chatRepository.save(chat);
    }
}
