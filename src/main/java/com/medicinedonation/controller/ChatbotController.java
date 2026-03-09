package com.medicinedonation.controller;

import com.medicinedonation.dto.request.ChatbotQARequest;
import com.medicinedonation.dto.request.ChatbotRequest;
import com.medicinedonation.dto.response.ApiResponse;
import com.medicinedonation.service.ChatbotService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chatbot")
public class ChatbotController {

    @Autowired
    private ChatbotService chatbotService;

    // ─────────────────────────────────────────
    // USER ENDPOINTS — all authenticated users
    // ─────────────────────────────────────────

    // Send message
    @PostMapping("/message")
    public ResponseEntity<?> sendMessage(
            @RequestBody ChatbotRequest request,
            Authentication authentication) {
        try {
            String email = authentication.getName();
            return ResponseEntity.ok(ApiResponse.success(
                    "Response generated",
                    chatbotService.sendMessage(request, email)));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    // Get my chat history
    @GetMapping("/history")
    public ResponseEntity<?> getMyChatHistory(
            Authentication authentication) {
        try {
            String email = authentication.getName();
            return ResponseEntity.ok(ApiResponse.success(
                    "Chat history retrieved",
                    chatbotService.getMyChatHistory(email)));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    // ─────────────────────────────────────────
    // ADMIN ENDPOINTS — manage Q&A database
    // ─────────────────────────────────────────

    // Get all Q&A
    @GetMapping("/qa")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getAllQA() {
        try {
            return ResponseEntity.ok(ApiResponse.success(
                    "Q&A list retrieved",
                    chatbotService.getAllQA()));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    // Add Q&A
    @PostMapping("/qa")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> addQA(
            @RequestBody ChatbotQARequest request) {
        try {
            return ResponseEntity.ok(ApiResponse.success(
                    "Q&A added successfully",
                    chatbotService.addQA(request)));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    // Edit Q&A
    @PutMapping("/qa/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> editQA(
            @PathVariable Long id,
            @RequestBody ChatbotQARequest request) {
        try {
            return ResponseEntity.ok(ApiResponse.success(
                    "Q&A updated successfully",
                    chatbotService.editQA(id, request)));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    // Delete Q&A
    @DeleteMapping("/qa/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteQA(@PathVariable Long id) {
        try {
            chatbotService.deleteQA(id);
            return ResponseEntity.ok(
                    ApiResponse.success("Q&A deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }
}