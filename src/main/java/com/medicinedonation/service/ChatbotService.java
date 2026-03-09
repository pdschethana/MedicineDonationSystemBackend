package com.medicinedonation.service;

import com.medicinedonation.dto.request.ChatbotQARequest;
import com.medicinedonation.dto.request.ChatbotRequest;
import com.medicinedonation.dto.response.ChatbotResponse;
import com.medicinedonation.model.ChatHistory;
import com.medicinedonation.model.ChatbotQA;
import com.medicinedonation.model.User;
import com.medicinedonation.repository.ChatHistoryRepository;
import com.medicinedonation.repository.ChatbotQARepository;
import com.medicinedonation.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ChatbotService {

    @Autowired
    private ChatbotQARepository chatbotQARepository;

    @Autowired
    private ChatHistoryRepository chatHistoryRepository;

    @Autowired
    private UserRepository userRepository;

    // ─────────────────────────────────────────
    // SEND MESSAGE
    // User sends message — system matches keyword
    // ─────────────────────────────────────────

    public ChatbotResponse sendMessage(
            ChatbotRequest request, String userEmail) {

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException(
                        "User not found"));

        String userMessage = request.getUserMessage().trim();
        String botResponse = findAnswer(userMessage);

        // Save chat history
        ChatHistory history = ChatHistory.builder()
                .user(user)
                .userMessage(userMessage)
                .botResponse(botResponse)
                .build();

        chatHistoryRepository.save(history);

        return ChatbotResponse.builder()
                .userMessage(userMessage)
                .botResponse(botResponse)
                .timestamp(history.getTimestamp())
                .build();
    }

    // ─────────────────────────────────────────
    // FIND ANSWER — keyword matching logic
    // ─────────────────────────────────────────

    private String findAnswer(String userMessage) {

        String lowerMessage = userMessage.toLowerCase();

        // Try exact keyword match first
        Optional<ChatbotQA> exactMatch =
                chatbotQARepository.findByKeywordIgnoreCase(
                        userMessage);

        if (exactMatch.isPresent()) {
            return exactMatch.get().getAnswer();
        }

        // Try partial keyword match — check if message contains keyword
        List<ChatbotQA> allQA = chatbotQARepository.findAll();

        for (ChatbotQA qa : allQA) {
            if (lowerMessage.contains(
                    qa.getKeyword().toLowerCase())) {
                return qa.getAnswer();
            }
        }

        // No match found — default response
        return "I'm sorry, I couldn't find an answer to your question. " +
                "Please contact our support team or try asking " +
                "with different keywords. " +
                "You can ask about: donation process, medicine verification, " +
                "collection points, how to register, or eligibility.";
    }

    // ─────────────────────────────────────────
    // GET MY CHAT HISTORY
    // ─────────────────────────────────────────

    public List<ChatbotResponse> getMyChatHistory(String userEmail) {

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException(
                        "User not found"));

        return chatHistoryRepository
                .findByUserOrderByTimestampDesc(user)
                .stream()
                .map(h -> ChatbotResponse.builder()
                        .userMessage(h.getUserMessage())
                        .botResponse(h.getBotResponse())
                        .timestamp(h.getTimestamp())
                        .build())
                .collect(Collectors.toList());
    }

    // ─────────────────────────────────────────
    // ADMIN — ADD Q&A
    // ─────────────────────────────────────────

    public ChatbotQA addQA(ChatbotQARequest request) {

        // Check duplicate keyword
        Optional<ChatbotQA> existing =
                chatbotQARepository.findByKeywordIgnoreCase(
                        request.getKeyword());

        if (existing.isPresent()) {
            throw new RuntimeException(
                    "Keyword already exists: " + request.getKeyword() +
                            ". Please edit existing Q&A instead.");
        }

        ChatbotQA qa = ChatbotQA.builder()
                .keyword(request.getKeyword())
                .question(request.getQuestion())
                .answer(request.getAnswer())
                .build();

        return chatbotQARepository.save(qa);
    }

    // ─────────────────────────────────────────
    // ADMIN — EDIT Q&A
    // ─────────────────────────────────────────

    public ChatbotQA editQA(Long id, ChatbotQARequest request) {

        ChatbotQA qa = chatbotQARepository.findById(id)
                .orElseThrow(() -> new RuntimeException(
                        "Q&A not found with id: " + id));

        qa.setKeyword(request.getKeyword());
        qa.setQuestion(request.getQuestion());
        qa.setAnswer(request.getAnswer());

        return chatbotQARepository.save(qa);
    }

    // ─────────────────────────────────────────
    // ADMIN — DELETE Q&A
    // ─────────────────────────────────────────

    public void deleteQA(Long id) {
        ChatbotQA qa = chatbotQARepository.findById(id)
                .orElseThrow(() -> new RuntimeException(
                        "Q&A not found with id: " + id));
        chatbotQARepository.delete(qa);
    }

    // ─────────────────────────────────────────
    // ADMIN — GET ALL Q&A
    // ─────────────────────────────────────────

    public List<ChatbotQA> getAllQA() {
        return chatbotQARepository.findAll();
    }
}