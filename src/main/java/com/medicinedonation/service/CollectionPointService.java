package com.medicinedonation.service;

import com.medicinedonation.dto.response.DonationResponse;
import com.medicinedonation.enums.DonationStatus;
import com.medicinedonation.model.CollectionPoint;
import com.medicinedonation.model.Donation;
import com.medicinedonation.model.User;
import com.medicinedonation.repository.CollectionPointRepository;
import com.medicinedonation.repository.DonationRepository;
import com.medicinedonation.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CollectionPointService {

    @Autowired
    private DonationRepository donationRepository;

    @Autowired
    private CollectionPointRepository collectionPointRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DonorService donorService;

    // ─────────────────────────────────────────
    // GET COLLECTION POINT OF LOGGED IN ADMIN
    // ─────────────────────────────────────────

    private CollectionPoint getMyCollectionPoint(String adminEmail) {
        User admin = userRepository.findByEmail(adminEmail)
                .orElseThrow(() -> new RuntimeException(
                        "Admin not found"));

        return collectionPointRepository.findByAdmin(admin)
                .orElseThrow(() -> new RuntimeException(
                        "No collection point assigned to this admin. " +
                                "Please contact system admin."));
    }

    // ─────────────────────────────────────────
    // GET INCOMING DONATIONS
    // DOCTOR_APPROVED — waiting for physical receipt
    // ─────────────────────────────────────────

    public List<DonationResponse> getIncomingDonations(
            String adminEmail) {

        CollectionPoint myPoint = getMyCollectionPoint(adminEmail);

        return donationRepository
                .findByCollectionPointAndStatus(
                        myPoint, DonationStatus.DOCTOR_APPROVED)
                .stream()
                .map(donorService::mapToDonationResponse)
                .collect(Collectors.toList());
    }

    // ─────────────────────────────────────────
    // CONFIRM PHYSICAL RECEIPT
    // Collection point physically received medicine
    // Status: DOCTOR_APPROVED → LIVE
    // ─────────────────────────────────────────

    public DonationResponse confirmReceipt(
            Long donationId, String adminEmail) {

        CollectionPoint myPoint = getMyCollectionPoint(adminEmail);

        Donation donation = donationRepository.findById(donationId)
                .orElseThrow(() -> new RuntimeException(
                        "Donation not found with id: " + donationId));

        // Must belong to this collection point
        if (!donation.getCollectionPoint().getId()
                .equals(myPoint.getId())) {
            throw new RuntimeException(
                    "This donation does not belong to your collection point.");
        }

        // Must be DOCTOR_APPROVED
        if (donation.getStatus() != DonationStatus.DOCTOR_APPROVED) {
            throw new RuntimeException(
                    "Cannot confirm receipt. Current status is: " +
                            donation.getStatus().name() +
                            ". Donation must be DOCTOR_APPROVED.");
        }

        donation.setStatus(DonationStatus.LIVE);
        return donorService.mapToDonationResponse(
                donationRepository.save(donation));
    }

    // ─────────────────────────────────────────
    // GET CURRENT INVENTORY
    // All LIVE donations at this collection point
    // ─────────────────────────────────────────

    public List<DonationResponse> getCurrentInventory(
            String adminEmail) {

        CollectionPoint myPoint = getMyCollectionPoint(adminEmail);

        return donationRepository
                .findByCollectionPointAndStatus(
                        myPoint, DonationStatus.LIVE)
                .stream()
                .map(donorService::mapToDonationResponse)
                .collect(Collectors.toList());
    }

    // ─────────────────────────────────────────
    // GET REQUESTED DONATIONS
    // REQUESTED — recipient requested, waiting for collection
    // ─────────────────────────────────────────

    public List<DonationResponse> getRequestedDonations(
            String adminEmail) {

        CollectionPoint myPoint = getMyCollectionPoint(adminEmail);

        return donationRepository
                .findByCollectionPointAndStatus(
                        myPoint, DonationStatus.REQUESTED)
                .stream()
                .map(donorService::mapToDonationResponse)
                .collect(Collectors.toList());
    }

    // ─────────────────────────────────────────
    // CONFIRM COLLECTION
    // Recipient physically collected medicine
    // Status: REQUESTED → COLLECTED
    // ─────────────────────────────────────────

    public DonationResponse confirmCollection(
            Long donationId, String adminEmail) {

        CollectionPoint myPoint = getMyCollectionPoint(adminEmail);

        Donation donation = donationRepository.findById(donationId)
                .orElseThrow(() -> new RuntimeException(
                        "Donation not found with id: " + donationId));

        // Must belong to this collection point
        if (!donation.getCollectionPoint().getId()
                .equals(myPoint.getId())) {
            throw new RuntimeException(
                    "This donation does not belong to your collection point.");
        }

        // Must be REQUESTED
        if (donation.getStatus() != DonationStatus.REQUESTED) {
            throw new RuntimeException(
                    "Cannot confirm collection. Current status is: " +
                            donation.getStatus().name() +
                            ". Donation must be REQUESTED.");
        }

        donation.setStatus(DonationStatus.COLLECTED);
        return donorService.mapToDonationResponse(
                donationRepository.save(donation));
    }

    // ─────────────────────────────────────────
    // GET COLLECTION HISTORY
    // All COLLECTED donations at this point
    // ─────────────────────────────────────────

    public List<DonationResponse> getCollectionHistory(
            String adminEmail) {

        CollectionPoint myPoint = getMyCollectionPoint(adminEmail);

        return donationRepository
                .findByCollectionPointAndStatus(
                        myPoint, DonationStatus.COLLECTED)
                .stream()
                .map(donorService::mapToDonationResponse)
                .collect(Collectors.toList());
    }

    // ─────────────────────────────────────────
    // GET ALL DONATIONS AT MY POINT
    // All statuses
    // ─────────────────────────────────────────

    public List<DonationResponse> getAllMyDonations(
            String adminEmail) {

        CollectionPoint myPoint = getMyCollectionPoint(adminEmail);

        return donationRepository
                .findByCollectionPoint(myPoint)
                .stream()
                .map(donorService::mapToDonationResponse)
                .collect(Collectors.toList());
    }

    // ─────────────────────────────────────────
    // DASHBOARD COUNTS
    // ─────────────────────────────────────────

    public Map<String, Long> getDashboardCounts(String adminEmail) {

        CollectionPoint myPoint = getMyCollectionPoint(adminEmail);

        Map<String, Long> counts = new HashMap<>();

        counts.put("incomingDonations",
                donationRepository.countByCollectionPointAndStatus(
                        myPoint, DonationStatus.DOCTOR_APPROVED));
        counts.put("currentInventory",
                donationRepository.countByCollectionPointAndStatus(
                        myPoint, DonationStatus.LIVE));
        counts.put("requestedByRecipients",
                donationRepository.countByCollectionPointAndStatus(
                        myPoint, DonationStatus.REQUESTED));
        counts.put("totalCollected",
                donationRepository.countByCollectionPointAndStatus(
                        myPoint, DonationStatus.COLLECTED));

        return counts;
    }
}