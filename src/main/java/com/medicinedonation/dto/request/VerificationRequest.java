/*package com.medicinedonation.dto.request;

import lombok.Data;

@Data
public class VerificationRequest {

    // true = approve, false = reject
    private boolean approved;
    private String notes;
    private String rejectionReason;
}*/

package com.medicinedonation.dto.request;

import lombok.Data;
import java.time.LocalDate;

@Data
public class VerificationRequest {

    // true = approve, false = reject
    private boolean approved;
    private String notes;
    private String rejectionReason;

    // ✅ NEW — doctor corrections (optional, only set if doctor edited)
    private String correctedBrandName;
    private String correctedStrength;
    private String correctedDosageForm;
    private Integer correctedQuantity;
    private LocalDate correctedExpiryDate;
}