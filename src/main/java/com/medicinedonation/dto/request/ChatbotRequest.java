package com.medicinedonation.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ChatbotRequest {

    @NotBlank(message = "Message is required")
    private String userMessage;
}