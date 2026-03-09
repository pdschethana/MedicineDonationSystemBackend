package com.medicinedonation.service;

import com.medicinedonation.dto.response.DashboardCountResponse;
import com.medicinedonation.enums.DonationStatus;
import com.medicinedonation.enums.Role;
import com.medicinedonation.model.CollectionPoint;
import com.medicinedonation.model.User;
import com.medicinedonation.repository.CollectionPointRepository;
import com.medicinedonation.repository.DonationRepository;
import com.medicinedonation.repository.MedicineRepository;
import com.medicinedonation.repository.PendingMedicineRepository;
import com.medicinedonation.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class DashboardService {

    @Autowired
    private DonationRepository donationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MedicineRepository medicineRepository;

    @Autowired
    private PendingMedicineRepository pendingMedicineRepository;

    @Autowired
    private CollectionPointRepository collectionPointRepository;

    // ─────────────────────────────────────────
    // ADMIN DASHBOARD
    // ─────────────────────────────────────────

    public DashboardCountResponse getAdminDashboard() {
        return DashboardCountResponse.builder()

                // Users — cast to long
                .totalDonors((long) userRepository
                        .findByRole(Role.DONOR).size())
                .totalRecipients((long) userRepository
                        .findByRole(Role.RECIPIENT).size())
                .totalDoctors((long) userRepository
                        .findByRole(Role.DOCTOR).size())
                .totalPharmacists((long) userRepository
                        .findByRole(Role.PHARMACIST).size())
                .totalCollectionPointAdmins((long) userRepository
                        .findByRole(Role.COLLECTION_POINT).size())

                // Donations — countByStatus returns Long already ✅
                .totalDonations(donationRepository.count())
                .pendingDoctor(donationRepository
                        .countByStatus(DonationStatus.PENDING_DOCTOR))
                .pendingPharmacist(donationRepository
                        .countByStatus(DonationStatus.PENDING_PHARMACIST))
                .pendingDoctorRecheck(donationRepository
                        .countByStatus(
                                DonationStatus.PENDING_DOCTOR_RECHECK))
                .pharmacistRejected(donationRepository
                        .countByStatus(DonationStatus.PHARMACIST_REJECTED))
                .doctorApproved(donationRepository
                        .countByStatus(DonationStatus.DOCTOR_APPROVED))
                .rejectedByDoctor(donationRepository
                        .countByStatus(DonationStatus.REJECTED_BY_DOCTOR))
                .live(donationRepository
                        .countByStatus(DonationStatus.LIVE))
                .requested(donationRepository
                        .countByStatus(DonationStatus.REQUESTED))
                .collected(donationRepository
                        .countByStatus(DonationStatus.COLLECTED))

                // Medicines — cast to long
                .totalMedicines(medicineRepository.count())
                .pharmacistAddedMedicines((long) medicineRepository
                        .findByPharmacistVerified(true).size())

                // Pending medicines — countBy returns Long already ✅
                .pendingMedicineReviews(
                        pendingMedicineRepository
                                .countByResolvedFalseAndRejectedFalse())

                // Collection points — cast to long
                .totalCollectionPoints(
                        collectionPointRepository.count())
                .activeCollectionPoints((long) collectionPointRepository
                        .findByActive(true).size())

                .build();
    }

    // ─────────────────────────────────────────
    // DOCTOR DASHBOARD
    // ─────────────────────────────────────────

    public DashboardCountResponse getDoctorDashboard(
            String doctorEmail) {

        User doctor = userRepository.findByEmail(doctorEmail)
                .orElseThrow(() -> new RuntimeException(
                        "Doctor not found"));

        // stream count returns long already ✅
        long totalActedOn = donationRepository.findAll()
                .stream()
                .filter(d -> d.getApprovedByDoctor() != null &&
                        d.getApprovedByDoctor().getId()
                                .equals(doctor.getId()))
                .count();

        return DashboardCountResponse.builder()
                .pendingDoctor(donationRepository
                        .countByStatus(DonationStatus.PENDING_DOCTOR))
                .pendingDoctorRecheck(donationRepository
                        .countByStatus(
                                DonationStatus.PENDING_DOCTOR_RECHECK))
                .pharmacistRejected(donationRepository
                        .countByStatus(DonationStatus.PHARMACIST_REJECTED))
                .totalActedOn(totalActedOn)
                .build();
    }

    // ─────────────────────────────────────────
    // PHARMACIST DASHBOARD
    // ─────────────────────────────────────────

    public DashboardCountResponse getPharmacistDashboard(
            String pharmacistEmail) {

        User pharmacist = userRepository.findByEmail(pharmacistEmail)
                .orElseThrow(() -> new RuntimeException(
                        "Pharmacist not found"));

        // cast to long — findByVerifiedBy returns List
        long medicinesAdded = (long) medicineRepository
                .findByVerifiedBy(pharmacist).size();

        // stream count returns long already ✅
        long totalReviewed = pendingMedicineRepository.findAll()
                .stream()
                .filter(p -> p.getReviewedByPharmacist() != null &&
                        p.getReviewedByPharmacist().getId()
                                .equals(pharmacist.getId()))
                .count();

        return DashboardCountResponse.builder()
                .pendingMedicineReviews(
                        pendingMedicineRepository
                                .countByResolvedFalseAndRejectedFalse())
                .pharmacistAddedMedicines(medicinesAdded)
                .totalActedOn(totalReviewed)
                .build();
    }

    // ─────────────────────────────────────────
    // DONOR DASHBOARD
    // ─────────────────────────────────────────

    public DashboardCountResponse getDonorDashboard(
            String donorEmail) {

        User donor = userRepository.findByEmail(donorEmail)
                .orElseThrow(() -> new RuntimeException(
                        "Donor not found"));

        return DashboardCountResponse.builder()
                // cast to long — findByDonor returns List
                .totalDonations((long) donationRepository
                        .findByDonor(donor).size())
                // countByDonorAndStatus returns Long already ✅
                .pendingDoctor(donationRepository
                        .countByDonorAndStatus(
                                donor, DonationStatus.PENDING_DOCTOR))
                .pendingPharmacist(donationRepository
                        .countByDonorAndStatus(
                                donor, DonationStatus.PENDING_PHARMACIST))
                .pendingDoctorRecheck(donationRepository
                        .countByDonorAndStatus(
                                donor,
                                DonationStatus.PENDING_DOCTOR_RECHECK))
                .doctorApproved(donationRepository
                        .countByDonorAndStatus(
                                donor, DonationStatus.DOCTOR_APPROVED))
                .rejectedByDoctor(donationRepository
                        .countByDonorAndStatus(
                                donor, DonationStatus.REJECTED_BY_DOCTOR))
                .pharmacistRejected(donationRepository
                        .countByDonorAndStatus(
                                donor, DonationStatus.PHARMACIST_REJECTED))
                .live(donationRepository
                        .countByDonorAndStatus(
                                donor, DonationStatus.LIVE))
                .requested(donationRepository
                        .countByDonorAndStatus(
                                donor, DonationStatus.REQUESTED))
                .collected(donationRepository
                        .countByDonorAndStatus(
                                donor, DonationStatus.COLLECTED))
                .build();
    }

    // ─────────────────────────────────────────
    // RECIPIENT DASHBOARD
    // ─────────────────────────────────────────

    public DashboardCountResponse getRecipientDashboard(
            String recipientEmail) {

        User recipient = userRepository.findByEmail(recipientEmail)
                .orElseThrow(() -> new RuntimeException(
                        "Recipient not found"));

        // cast to long — findByRecipient returns List
        long totalRequests = (long) donationRepository
                .findByRecipient(recipient).size();

        // stream count returns long already ✅
        long pendingCollection = donationRepository
                .findByRecipient(recipient)
                .stream()
                .filter(d -> d.getStatus() == DonationStatus.REQUESTED)
                .count();

        long collected = donationRepository
                .findByRecipient(recipient)
                .stream()
                .filter(d -> d.getStatus() == DonationStatus.COLLECTED)
                .count();

        return DashboardCountResponse.builder()
                .totalDonations(totalRequests)
                .requested(pendingCollection)
                .collected(collected)
                .build();
    }

    // ─────────────────────────────────────────
    // COLLECTION POINT DASHBOARD
    // ─────────────────────────────────────────

    public DashboardCountResponse getCollectionPointDashboard(
            String adminEmail) {

        User admin = userRepository.findByEmail(adminEmail)
                .orElseThrow(() -> new RuntimeException(
                        "Admin not found"));

        Optional<CollectionPoint> pointOpt =
                collectionPointRepository.findByAdmin(admin);

        if (pointOpt.isEmpty()) {
            throw new RuntimeException(
                    "No collection point assigned to this admin.");
        }

        CollectionPoint point = pointOpt.get();

        return DashboardCountResponse.builder()
                // countByCollectionPointAndStatus returns Long already ✅
                .doctorApproved(donationRepository
                        .countByCollectionPointAndStatus(
                                point, DonationStatus.DOCTOR_APPROVED))
                .live(donationRepository
                        .countByCollectionPointAndStatus(
                                point, DonationStatus.LIVE))
                .requested(donationRepository
                        .countByCollectionPointAndStatus(
                                point, DonationStatus.REQUESTED))
                .collected(donationRepository
                        .countByCollectionPointAndStatus(
                                point, DonationStatus.COLLECTED))
                // cast to long — findByCollectionPoint returns List
                .totalDonations((long) donationRepository
                        .findByCollectionPoint(point).size())
                .build();
    }
}