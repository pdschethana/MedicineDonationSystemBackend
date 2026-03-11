/*package com.medicinedonation.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DonationResponse {

    private Long id;

    // Donor info
    private String donorName;
    private String donorEmail;

    // Medicine info
    private String brandNameSubmitted;
    private Long medicineId;
    private String medicineApiName;
    private String medicineStrength;
    private String medicineDosageForm;
    private String medicineSchedule;

    // Donation details
    private int quantity;
    private LocalDate expiryDate;
    private String photoUrl;
    private String packageProofUrl;

    // Status
    private String status;

    // Doctor info
    private String doctorName;
    private String doctorNotes;
    private String rejectionReason;
    private String pharmacistRejectionReason;

    // Collection point
    private Long collectionPointId;
    private String collectionPointName;
    private String collectionPointAddress;

    // Recipient
    private String recipientName;

    // Timestamps
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}*/
/*package com.medicinedonation.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DonationResponse {

    private Long id;

    // Donor info
    private String donorName;
    private String donorEmail;

    // Medicine info
    private String brandNameSubmitted;
    private Long medicineId;
    private String medicineApiName;
    private String medicineStrength;
    private String medicineDosageForm;
    private String medicineSchedule;

    // ✅ NEW — donor provided dosage info
    private String dosageForm;
    private String strength;

    // Donation details
    private int quantity;
    private LocalDate expiryDate;
    private String photoUrl;
    private String packageProofUrl;

    // Status
    private String status;

    // Doctor info
    private String doctorName;
    private String doctorNotes;
    private String rejectionReason;
    private String pharmacistRejectionReason;

    // Collection point
    private Long collectionPointId;
    private String collectionPointName;
    private String collectionPointAddress;

    // Recipient
    private String recipientName;

    // Timestamps
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}*/

/*package com.medicinedonation.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DonationResponse {

    private Long id;

    // Donor info
    private String donorName;
    private String donorEmail;

    // Medicine info
    private String brandNameSubmitted;
    private Long medicineId;
    private String medicineApiName;
    private String medicineStrength;
    private String medicineDosageForm;
    private String medicineSchedule;

    // Donor provided dosage info
    private String dosageForm;
    private String strength;

    // ✅ NEW — DB match status for doctor warning
    // Values: "FULL_MATCH" | "PARTIAL_MATCH" | "NO_MATCH"
    private String dbMatchStatus;

    // Donation details
    private int quantity;
    private LocalDate expiryDate;
    private String photoUrl;
    private String packageProofUrl;

    // Status
    private String status;

    // Doctor info
    private String doctorName;
    private String doctorNotes;
    private String rejectionReason;
    private String pharmacistRejectionReason;

    // Collection point
    private Long collectionPointId;
    private String collectionPointName;
    private String collectionPointAddress;

    // Recipient
    private String recipientName;

    // Timestamps
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}*/

/*package com.medicinedonation.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DonationResponse {

    private Long id;

    // Donor info
    private String donorName;
    private String donorEmail;

    // Medicine info — donor submitted
    private String brandNameSubmitted;
    private Long medicineId;
    private String medicineApiName;
    private String medicineStrength;
    private String medicineDosageForm;
    private String medicineSchedule;

    // Donor provided dosage info
    private String dosageForm;
    private String strength;

    // ✅ NEW — doctor corrected fields (null if no corrections made)
    private String doctorCorrectedBrandName;
    private String doctorCorrectedStrength;
    private String doctorCorrectedDosageForm;
    private Integer doctorCorrectedQuantity;
    private LocalDate doctorCorrectedExpiryDate;

    // DB match status
    private String dbMatchStatus;

    // Donation details
    private int quantity;
    private LocalDate expiryDate;
    private String photoUrl;
    private String packageProofUrl;

    // Status
    private String status;

    // Doctor info
    private String doctorName;
    private String doctorNotes;
    private String rejectionReason;
    private String pharmacistRejectionReason;

    // Collection point
    private Long collectionPointId;
    private String collectionPointName;
    private String collectionPointAddress;

    // Recipient
    private String recipientName;

    // Timestamps
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}*/

/*package com.medicinedonation.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DonationResponse {

    private Long id;

    // Donor info
    private String donorName;
    private String donorEmail;

    // Medicine info (linked medicine if in DB)
    private String brandNameSubmitted;
    private Long medicineId;
    private String medicineApiName;
    private String medicineStrength;
    private String medicineDosageForm;
    private String medicineSchedule;

    // ✅ NEW — all DB records matching the brand name
    // Shows doctor ALL variants (e.g. Panadol 500mg + Panadol 250mg)
    private List<DbMedicineRecord> dbMatchingRecords;

    // Donor submitted details
    private String dosageForm;
    private String strength;

    // Doctor corrections
    private String doctorCorrectedBrandName;
    private String doctorCorrectedStrength;
    private String doctorCorrectedDosageForm;
    private Integer doctorCorrectedQuantity;
    private LocalDate doctorCorrectedExpiryDate;

    // DB match status
    private String dbMatchStatus;

    // Donation details
    private int quantity;
    private LocalDate expiryDate;
    private String photoUrl;
    private String packageProofUrl;

    // Status
    private String status;

    // Doctor info
    private String doctorName;
    private String doctorNotes;
    private String rejectionReason;
    private String pharmacistRejectionReason;

    // Collection point
    private Long collectionPointId;
    private String collectionPointName;
    private String collectionPointAddress;

    // Recipient
    private String recipientName;

    // Timestamps
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // ─────────────────────────────────────────
    // INNER CLASS — each DB record for a brand
    // ─────────────────────────────────────────

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DbMedicineRecord {
        private Long id;
        private String apiName;
        private String strength;
        private String dosageForm;
        private String route;
        private String schedule;
        private boolean pharmacistVerified;
    }
}*/

package com.medicinedonation.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DonationResponse {

    private Long id;

    // Donor info
    private String donorName;
    private String donorEmail;

    // Medicine info (linked medicine if in DB)
    private String brandNameSubmitted;
    private Long medicineId;
    private String medicineBrandName;   // ✅ actual brand name from DB (may differ from brandNameSubmitted)
    private String medicineApiName;
    private String medicineStrength;
    private String medicineDosageForm;
    private String medicineSchedule;

    // ✅ NEW — all DB records matching the brand name
    // Shows doctor ALL variants (e.g. Panadol 500mg + Panadol 250mg)
    private List<DbMedicineRecord> dbMatchingRecords;

    // Donor submitted details
    private String dosageForm;
    private String strength;

    // Doctor corrections
    private String doctorCorrectedBrandName;
    private String doctorCorrectedStrength;
    private String doctorCorrectedDosageForm;
    private Integer doctorCorrectedQuantity;
    private LocalDate doctorCorrectedExpiryDate;

    // DB match status
    private String dbMatchStatus;

    // Donation details
    private int quantity;
    private LocalDate expiryDate;
    private String photoUrl;
    private String packageProofUrl;

    // Status
    private String status;

    // Doctor info
    private String doctorName;
    private String doctorNotes;
    private String rejectionReason;
    private String pharmacistRejectionReason;

    // Collection point
    private Long collectionPointId;
    private String collectionPointName;
    private String collectionPointAddress;

    // Recipient
    private String recipientName;

    // Timestamps
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // ─────────────────────────────────────────
    // INNER CLASS — each DB record for a brand
    // ─────────────────────────────────────────

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DbMedicineRecord {
        private Long id;
        private String apiName;
        private String strength;
        private String dosageForm;
        private String route;
        private String schedule;
        private boolean pharmacistVerified;
    }
}