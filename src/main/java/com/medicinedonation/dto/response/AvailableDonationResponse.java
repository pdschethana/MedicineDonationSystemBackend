package com.medicinedonation.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AvailableDonationResponse {

    private Long donationId;
    private String brandName;
    private String apiName;
    private String strength;
    private String dosageForm;
    private String route;
    private String schedule;
    private int quantity;
    private LocalDate expiryDate;

    // Doctor verified
    private boolean doctorVerified;
    private String doctorName;

    // Collection point
    private Long collectionPointId;
    private String collectionPointName;
    private String collectionPointAddress;
    private String collectionPointDistrict;
    private String collectionPointPhone;

    // Match info
    private String matchTier;
}