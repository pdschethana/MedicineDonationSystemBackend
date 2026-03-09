package com.medicinedonation.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class  MedicineResponse {

    private Long id;
    private String brandName;
    private String apiName;
    private String strength;
    private String dosageForm;
    private String route;
    private String schedule;
    private boolean pharmacistVerified;
    private String verifiedByName;
    private LocalDateTime addedAt;
}