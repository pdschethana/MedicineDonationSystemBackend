/*package com.medicinedonation.model;

import com.medicinedonation.enums.DonationStatus;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "donations")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Donation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "donor_id", nullable = false)
    private User donor;

    @ManyToOne
    @JoinColumn(name = "medicine_id")
    private Medicine medicine;

    @Column(nullable = false)
    private String brandNameSubmitted;

    @Column(nullable = false)
    private int quantity;

    @Column(nullable = false)
    private LocalDate expiryDate;

    private String photoUrl;
    private String packageProofUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DonationStatus status = DonationStatus.PENDING_DOCTOR;

    @ManyToOne
    @JoinColumn(name = "doctor_id")
    private User approvedByDoctor;

    private String doctorNotes;
    private String pharmacistRejectionReason;
    private String rejectionReason;

    @ManyToOne
    @JoinColumn(name = "collection_point_id")
    private CollectionPoint collectionPoint;

    @ManyToOne
    @JoinColumn(name = "recipient_id")
    private User recipient;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}*/
/*package com.medicinedonation.model;

import com.medicinedonation.enums.DonationStatus;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "donations")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Donation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "donor_id", nullable = false)
    private User donor;

    @ManyToOne
    @JoinColumn(name = "medicine_id")
    private Medicine medicine;

    @Column(nullable = false)
    private String brandNameSubmitted;

    @Column(nullable = false)
    private int quantity;

    @Column(nullable = false)
    private LocalDate expiryDate;

    private String photoUrl;
    private String packageProofUrl;

    // ✅ NEW — donor provided dosage info
    private String dosageForm;   // Tablet, Capsule, Syrup etc
    private String strength;     // 500mg, 10mg/5ml etc

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DonationStatus status = DonationStatus.PENDING_DOCTOR;

    @ManyToOne
    @JoinColumn(name = "doctor_id")
    private User approvedByDoctor;

    private String doctorNotes;
    private String pharmacistRejectionReason;
    private String rejectionReason;

    @ManyToOne
    @JoinColumn(name = "collection_point_id")
    private CollectionPoint collectionPoint;

    @ManyToOne
    @JoinColumn(name = "recipient_id")
    private User recipient;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}*/

package com.medicinedonation.model;

import com.medicinedonation.enums.DonationStatus;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "donations")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Donation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "donor_id", nullable = false)
    private User donor;

    @ManyToOne
    @JoinColumn(name = "medicine_id")
    private Medicine medicine;

    @Column(nullable = false)
    private String brandNameSubmitted;

    @Column(nullable = false)
    private int quantity;

    @Column(nullable = false)
    private LocalDate expiryDate;

    private String photoUrl;
    private String packageProofUrl;

    // Donor provided dosage info
    private String dosageForm;
    private String strength;

    // ✅ NEW — doctor corrected fields (null if doctor made no corrections)
    private String doctorCorrectedBrandName;
    private String doctorCorrectedStrength;
    private String doctorCorrectedDosageForm;
    private Integer doctorCorrectedQuantity;
    private LocalDate doctorCorrectedExpiryDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DonationStatus status = DonationStatus.PENDING_DOCTOR;

    @ManyToOne
    @JoinColumn(name = "doctor_id")
    private User approvedByDoctor;

    private String doctorNotes;
    private String pharmacistRejectionReason;
    private String rejectionReason;

    @ManyToOne
    @JoinColumn(name = "collection_point_id")
    private CollectionPoint collectionPoint;

    @ManyToOne
    @JoinColumn(name = "recipient_id")
    private User recipient;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}