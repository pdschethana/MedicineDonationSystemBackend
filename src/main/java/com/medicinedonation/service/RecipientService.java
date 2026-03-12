/*package com.medicinedonation.service;

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
}*/


/*package com.medicinedonation.service;

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

    public MatchResultResponse searchByBrandName(String brandName, String strength) {
        return medicineService.searchByBrandName(brandName, strength);
    }

    // ─────────────────────────────────────────
    // SEARCH BY API NAME
    // ─────────────────────────────────────────

    public MatchResultResponse searchByApiName(String apiName, String strength) {
        return medicineService.searchByApiName(apiName, strength);
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
}*/


/*package com.medicinedonation.service;

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

    public MatchResultResponse searchByBrandName(String brandName, String strength) {
        return medicineService.searchByBrandName(brandName, strength);
    }

    // ─────────────────────────────────────────
    // SEARCH BY API NAME
    // ─────────────────────────────────────────

    public MatchResultResponse searchByApiName(String apiName, String strength) {
        return medicineService.searchByApiName(apiName, strength);
    }

    // ─────────────────────────────────────────
    // REQUEST MEDICINE
    // Recipient requests a LIVE donation
    // Status: LIVE → REQUESTED
    // ─────────────────────────────────────────

    public DonationResponse requestMedicine(
            Long donationId, String recipientEmail, Integer requestedQuantity) {

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

        // Validate requested quantity
        if (requestedQuantity != null) {
            if (requestedQuantity < 1) {
                throw new RuntimeException("Requested quantity must be at least 1.");
            }
            if (requestedQuantity > donation.getQuantity()) {
                throw new RuntimeException(
                        "Requested quantity (" + requestedQuantity +
                                ") exceeds available quantity (" + donation.getQuantity() + ").");
            }
        }

        donation.setStatus(DonationStatus.REQUESTED);
        donation.setRecipient(recipient);
        // Store how many units recipient actually needs (null = all)
        donation.setRequestedQuantity(
                requestedQuantity != null ? requestedQuantity : donation.getQuantity());

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
}*/

/*package com.medicinedonation.service;

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

    public MatchResultResponse searchByBrandName(String brandName, String strength) {
        return medicineService.searchByBrandName(brandName, strength);
    }

    // ─────────────────────────────────────────
    // SEARCH BY API NAME
    // ─────────────────────────────────────────

    public MatchResultResponse searchByApiName(String apiName, String strength) {
        return medicineService.searchByApiName(apiName, strength);
    }

    // ─────────────────────────────────────────
    // REQUEST MEDICINE
    // Recipient requests a LIVE donation
    // Status: LIVE → REQUESTED
    // ─────────────────────────────────────────

    public DonationResponse requestMedicine(
            Long donationId, String recipientEmail, Integer requestedQuantity) {

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

        // Validate requested quantity
        int wantedQty = (requestedQuantity != null) ? requestedQuantity : donation.getQuantity();

        if (wantedQty < 1) {
            throw new RuntimeException("Requested quantity must be at least 1.");
        }
        if (wantedQty > donation.getQuantity()) {
            throw new RuntimeException(
                    "Requested quantity (" + wantedQty +
                            ") exceeds available quantity (" + donation.getQuantity() + ").");
        }

        int remaining = donation.getQuantity() - wantedQty;

        if (remaining > 0) {
            // Units left over — keep donation LIVE with reduced quantity
            // Create a new REQUESTED donation record for this recipient's portion
            Donation requestedPortion = Donation.builder()
                    .donor(donation.getDonor())
                    .medicine(donation.getMedicine())
                    .brandNameSubmitted(donation.getBrandNameSubmitted())
                    .quantity(wantedQty)
                    .expiryDate(donation.getExpiryDate())
                    .photoUrl(donation.getPhotoUrl())
                    .packageProofUrl(donation.getPackageProofUrl())
                    .dosageForm(donation.getDosageForm())
                    .strength(donation.getStrength())
                    .doctorCorrectedBrandName(donation.getDoctorCorrectedBrandName())
                    .doctorCorrectedStrength(donation.getDoctorCorrectedStrength())
                    .doctorCorrectedDosageForm(donation.getDoctorCorrectedDosageForm())
                    .doctorCorrectedQuantity(donation.getDoctorCorrectedQuantity())
                    .doctorCorrectedExpiryDate(donation.getDoctorCorrectedExpiryDate())
                    .approvedByDoctor(donation.getApprovedByDoctor())
                    .doctorNotes(donation.getDoctorNotes())
                    .collectionPoint(donation.getCollectionPoint())
                    .recipient(recipient)
                    .requestedQuantity(wantedQty)
                    .status(DonationStatus.REQUESTED)
                    .build();

            donationRepository.save(requestedPortion);

            // Reduce the original donation quantity — stays LIVE
            donation.setQuantity(remaining);
            donationRepository.save(donation);

            return donorService.mapToDonationResponse(requestedPortion);

        } else {
            // Recipient wants all units — move whole donation to REQUESTED
            donation.setStatus(DonationStatus.REQUESTED);
            donation.setRecipient(recipient);
            donation.setRequestedQuantity(wantedQty);
            return donorService.mapToDonationResponse(
                    donationRepository.save(donation));
        }
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
}*/

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

    public MatchResultResponse searchByBrandName(String brandName, String strength, Long collectionPointId) {
        return medicineService.searchByBrandName(brandName, strength, collectionPointId);
    }

    // ─────────────────────────────────────────
    // SEARCH BY API NAME
    // ─────────────────────────────────────────

    public MatchResultResponse searchByApiName(String apiName, String strength, Long collectionPointId) {
        return medicineService.searchByApiName(apiName, strength, collectionPointId);
    }

    // ─────────────────────────────────────────
    // REQUEST MEDICINE
    // Recipient requests a LIVE donation
    // Status: LIVE → REQUESTED
    // ─────────────────────────────────────────

    public DonationResponse requestMedicine(
            Long donationId, String recipientEmail, Integer requestedQuantity) {

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

        // Validate requested quantity
        int wantedQty = (requestedQuantity != null) ? requestedQuantity : donation.getQuantity();

        if (wantedQty < 1) {
            throw new RuntimeException("Requested quantity must be at least 1.");
        }
        if (wantedQty > donation.getQuantity()) {
            throw new RuntimeException(
                    "Requested quantity (" + wantedQty +
                            ") exceeds available quantity (" + donation.getQuantity() + ").");
        }

        int remaining = donation.getQuantity() - wantedQty;

        if (remaining > 0) {
            // Units left over — keep donation LIVE with reduced quantity
            // Create a new REQUESTED donation record for this recipient's portion
            Donation requestedPortion = Donation.builder()
                    .donor(donation.getDonor())
                    .medicine(donation.getMedicine())
                    .brandNameSubmitted(donation.getBrandNameSubmitted())
                    .quantity(wantedQty)
                    .expiryDate(donation.getExpiryDate())
                    .photoUrl(donation.getPhotoUrl())
                    .packageProofUrl(donation.getPackageProofUrl())
                    .dosageForm(donation.getDosageForm())
                    .strength(donation.getStrength())
                    .doctorCorrectedBrandName(donation.getDoctorCorrectedBrandName())
                    .doctorCorrectedStrength(donation.getDoctorCorrectedStrength())
                    .doctorCorrectedDosageForm(donation.getDoctorCorrectedDosageForm())
                    .doctorCorrectedQuantity(donation.getDoctorCorrectedQuantity())
                    .doctorCorrectedExpiryDate(donation.getDoctorCorrectedExpiryDate())
                    .approvedByDoctor(donation.getApprovedByDoctor())
                    .doctorNotes(donation.getDoctorNotes())
                    .collectionPoint(donation.getCollectionPoint())
                    .recipient(recipient)
                    .requestedQuantity(wantedQty)
                    .status(DonationStatus.REQUESTED)
                    .build();

            donationRepository.save(requestedPortion);

            // Reduce the original donation quantity — stays LIVE
            donation.setQuantity(remaining);
            donationRepository.save(donation);

            return donorService.mapToDonationResponse(requestedPortion);

        } else {
            // Recipient wants all units — move whole donation to REQUESTED
            donation.setStatus(DonationStatus.REQUESTED);
            donation.setRecipient(recipient);
            donation.setRequestedQuantity(wantedQty);
            return donorService.mapToDonationResponse(
                    donationRepository.save(donation));
        }
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

