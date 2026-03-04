package com.medicinedonation.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDate;

@Data
public class DonationRequest {

    @NotBlank(message = "Brand name is required")
    private String brandNameSubmitted;

    @Min(value = 1, message = "Quantity must be at least 1")
    private int quantity;

    @NotNull(message = "Expiry date is required")
    private LocalDate expiryDate;

    private String photoUrl;
    private String packageProofUrl;

    @NotNull(message = "Collection point is required")
    private Long collectionPointId;
}