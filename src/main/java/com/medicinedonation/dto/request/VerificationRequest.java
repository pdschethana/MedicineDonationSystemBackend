package com.medicinedonation.dto.request;

import lombok.Data;

@Data
public class VerificationRequest {

    // true = approve, false = reject
    private boolean approved;
    private String notes;
    private String rejectionReason;
}