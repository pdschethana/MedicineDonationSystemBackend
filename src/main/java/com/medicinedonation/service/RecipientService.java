package com.medicinedonation.service;

import com.medicinedonation.dto.response.AvailableDonationResponse;
import com.medicinedonation.dto.response.CollectionPointResponse;
import com.medicinedonation.dto.response.DonationResponse;
import com.medicinedonation.dto.response.MatchResultResponse;
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
public class RecipientService {

    @Autowired
    private DonationRepository donationRepository;

    @Autowired
    private CollectionPointRepository collectionPointRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MedicineService medicineService;

    @Autowired
    private DonorService donorService;

    // ─────────────────────────────────────────
    // SEARCH BY BRAND NAME
    // ─────────────────────────────────────────

    public MatchResultResponse searchByBrandName(String brandName) {
        return medicineService.searchByBrandName(brandName);
    }

    // ─────────────────────────────────────────
    // SEARCH BY API NAME
    // ─────────────────────────────────────────

    public MatchResultResponse searchByApiName(String apiName) {
        return medicineService.searchByApiName(apiName);
    }

    // ─────────────────────────────────────────
    // REQUEST MEDICINE
    // Recipient requests a LIVE donation
    // Status: LIVE → REQUESTED
    // ─────────────────────────────────────────

    public DonationResponse requestMedicine(
            Long donationId, String recipientEmail) {

        User recipient = userRepository.findByEmail(recipientEmail)
                .orElseThrow(() -> new RuntimeException(
                        "Recipient not found"));

        Donation donation = donationRepository.findById(donationId)
                .orElseThrow(() -> new RuntimeException(
                        "Donation not found with id: " + donationId));

        // Must be LIVE to request
        if (donation.getStatus() != DonationStatus.LIVE) {
            throw new RuntimeException(
                    "Cannot request this donation. Current status is: " +
                            donation.getStatus().name() +
                            ". Only LIVE donations can be requested.");
        }

        // Cannot request own donation
        if (donation.getDonor().getEmail().equals(recipientEmail)) {
            throw new RuntimeException(
                    "You cannot request your own donation.");
        }

        donation.setStatus(DonationStatus.REQUESTED);
        donation.setRecipient(recipient);

        return donorService.mapToDonationResponse(
                donationRepository.save(donation));
    }

    // ─────────────────────────────────────────
    // GET MY REQUESTS
    // All donations recipient has requested
    // ─────────────────────────────────────────

    public List<DonationResponse> getMyRequests(
            String recipientEmail) {

        User recipient = userRepository.findByEmail(recipientEmail)
                .orElseThrow(() -> new RuntimeException(
                        "Recipient not found"));

        return donationRepository.findByRecipient(recipient)
                .stream()
                .map(donorService::mapToDonationResponse)
                .collect(Collectors.toList());
    }

    // ─────────────────────────────────────────
    // GET REQUEST BY ID
    // ─────────────────────────────────────────

    public DonationResponse getRequestById(
            Long donationId, String recipientEmail) {

        Donation donation = donationRepository.findById(donationId)
                .orElseThrow(() -> new RuntimeException(
                        "Donation not found with id: " + donationId));

        // Must belong to this recipient
        if (donation.getRecipient() == null ||
                !donation.getRecipient().getEmail()
                        .equals(recipientEmail)) {
            throw new RuntimeException(
                    "You are not authorized to view this request.");
        }

        return donorService.mapToDonationResponse(donation);
    }

    // ─────────────────────────────────────────
    // GET ALL ACTIVE COLLECTION POINTS
    // With location details for recipient
    // ─────────────────────────────────────────

    public List<CollectionPointResponse> getCollectionPoints() {
        return collectionPointRepository.findByActive(true)
                .stream()
                .map(this::mapToCollectionPointResponse)
                .collect(Collectors.toList());
    }

    // ─────────────────────────────────────────
    // DASHBOARD COUNTS
    // ─────────────────────────────────────────

    public Map<String, Long> getDashboardCounts(
            String recipientEmail) {

        User recipient = userRepository.findByEmail(recipientEmail)
                .orElseThrow(() -> new RuntimeException(
                        "Recipient not found"));

        Map<String, Long> counts = new HashMap<>();

        // Count by status
        long requested = donationRepository.findByRecipient(recipient)
                .stream()
                .filter(d -> d.getStatus() == DonationStatus.REQUESTED)
                .count();

        long collected = donationRepository.findByRecipient(recipient)
                .stream()
                .filter(d -> d.getStatus() == DonationStatus.COLLECTED)
                .count();

        long total = donationRepository.findByRecipient(recipient)
                .size();

        counts.put("totalRequests", total);
        counts.put("pendingCollection", requested);
        counts.put("collected", collected);

        return counts;
    }

    // ─────────────────────────────────────────
    // MAPPER
    // ─────────────────────────────────────────

    private CollectionPointResponse mapToCollectionPointResponse(
            CollectionPoint point) {
        return CollectionPointResponse.builder()
                .id(point.getId())
                .locationName(point.getLocationName())
                .address(point.getAddress())
                .district(point.getDistrict())
                .phone(point.getPhone())
                .active(point.isActive())
                .adminName(point.getAdmin() != null ?
                        point.getAdmin().getName() : null)
                .adminEmail(point.getAdmin() != null ?
                        point.getAdmin().getEmail() : null)
                .build();
    }
}