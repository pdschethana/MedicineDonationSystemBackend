package com.medicinedonation.dto.request;

import com.medicinedonation.enums.NmraSchedule;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PharmacistReviewRequest {

    @NotBlank(message = "API name is required")
    private String resolvedApiName;

    @NotBlank(message = "Strength is required")
    private String resolvedStrength;

    @NotBlank(message = "Dosage form is required")
    private String resolvedDosageForm;

    @NotBlank(message = "Route is required")
    private String resolvedRoute;

    @NotNull(message = "NMRA schedule is required")
    private NmraSchedule resolvedSchedule;

    // Used when rejecting
    private String rejectionReason;
}