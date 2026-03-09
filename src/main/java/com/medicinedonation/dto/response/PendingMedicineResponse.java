/*package com.medicinedonation.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PendingMedicineResponse {

    private Long id;

    // Donation info
    private Long donationId;
    private String donorName;

    // Medicine details submitted
    private String brandName;
    private String photoUrl;
    private String packageInsertUrl;
    private String doctorNotes;

    // Pharmacist resolution
    private String resolvedApiName;
    private String resolvedStrength;
    private String resolvedDosageForm;
    private String resolvedRoute;
    private String resolvedSchedule;

    // Status
    private boolean resolved;
    private boolean rejected;
    private String rejectionReason;

    // Pharmacist
    private String reviewedByPharmacistName;

    // Timestamps
    private LocalDateTime submittedAt;
    private LocalDateTime resolvedAt;
}*/
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
public class PendingMedicineResponse {

    private Long id;

    // Donation info
    private Long donationId;
    private String donorName;

    // Medicine details submitted by donor
    private String brandName;
    private String photoUrl;
    private String packageInsertUrl;
    private String doctorNotes;

    // ✅ NEW — donor's submitted dosage info
    private String donorStrength;    // e.g. 500mg
    private String donorDosageForm;  // e.g. Tablet

    // Pharmacist resolution
    private String resolvedApiName;
    private String resolvedStrength;
    private String resolvedDosageForm;
    private String resolvedRoute;
    private String resolvedSchedule;

    // Status
    private boolean resolved;
    private boolean rejected;
    private String rejectionReason;

    // Pharmacist
    private String reviewedByPharmacistName;

    // Timestamps
    private LocalDateTime submittedAt;
    private LocalDateTime resolvedAt;
}