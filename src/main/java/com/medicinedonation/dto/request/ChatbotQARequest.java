package com.medicinedonation.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ChatbotQARequest {

    @NotBlank(message = "Keyword is required")
    private String keyword;

    @NotBlank(message = "Question is required")
    private String question;

    @NotBlank(message = "Answer is required")
    private String answer;
}