package com.medicinedonation.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardCountResponse {

    // Admin counts
    private Long totalUsers;
    private Long totalDoctors;
    private Long totalPharmacists;
    private Long totalDonors;
    private Long totalRecipients;
    private Long totalCollectionPoints;
    private Long totalMedicines;

    // Donation counts
    private Long totalDonations;
    private Long pendingDoctor;
    private Long pendingPharmacist;
    private Long pendingDoctorRecheck;
    private Long pharmacistRejected;
    private Long doctorApproved;
    private Long live;
    private Long collected;

    // Pharmacist counts
    private Long pendingMedicines;

    // Collection point counts
    private Long incomingDonations;
    private Long currentInventory;
}