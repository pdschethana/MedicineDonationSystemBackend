package com.medicinedonation.model;

import com.medicinedonation.enums.NmraSchedule;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "pending_medicines")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PendingMedicine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "donation_id", nullable = false)
    private Donation donation;

    @Column(nullable = false)
    private String brandName;

    private String photoUrl;
    private String packageInsertUrl;
    private String doctorNotes;

    // Pharmacist fills these
    private String resolvedApiName;
    private String resolvedStrength;
    private String resolvedDosageForm;
    private String resolvedRoute;

    @Enumerated(EnumType.STRING)
    private NmraSchedule resolvedSchedule;

    @Column(nullable = false)
    private boolean resolved = false;

    @Column(nullable = false)
    private boolean rejected = false;

    private String rejectionReason;

    @ManyToOne
    @JoinColumn(name = "pharmacist_id")
    private User reviewedByPharmacist;

    @Column(updatable = false)
    private LocalDateTime submittedAt;

    private LocalDateTime resolvedAt;

    @PrePersist
    protected void onCreate() {
        submittedAt = LocalDateTime.now();
    }
}