package com.medicinedonation.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MatchResultResponse {

    // TIER_1, TIER_2, NO_MATCH
    private String matchTier;

    private String searchedBrandName;
    private String searchedApiName;

    private List<AvailableDonationResponse> matches;

    private String message;
}