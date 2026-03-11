/*package com.medicinedonation.service;

import com.medicinedonation.dto.request.DonationRequest;
import com.medicinedonation.dto.response.ApiResponse;
import com.medicinedonation.dto.response.CollectionPointResponse;
import com.medicinedonation.dto.response.DonationResponse;
import com.medicinedonation.enums.DonationStatus;
import com.medicinedonation.model.CollectionPoint;
import com.medicinedonation.model.Donation;
import com.medicinedonation.model.Medicine;
import com.medicinedonation.model.PendingMedicine;
import com.medicinedonation.model.User;
import com.medicinedonation.repository.CollectionPointRepository;
import com.medicinedonation.repository.DonationRepository;
import com.medicinedonation.repository.PendingMedicineRepository;
import com.medicinedonation.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class DonorService {

    @Autowired
    private DonationRepository donationRepository;

    @Autowired
    private CollectionPointRepository collectionPointRepository;

    @Autowired
    private PendingMedicineRepository pendingMedicineRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MedicineService medicineService;

    // ─────────────────────────────────────────
    // SUBMIT DONATION
    // ─────────────────────────────────────────

    public DonationResponse submitDonation(
            DonationRequest request, String donorEmail) {

        // Get donor from DB
        User donor = userRepository.findByEmail(donorEmail)
                .orElseThrow(() -> new RuntimeException(
                        "Donor not found"));

        // Validate expiry date — must be more than 6 months
        LocalDate minExpiry = LocalDate.now().plusMonths(6);
        if (request.getExpiryDate().isBefore(minExpiry)) {
            throw new RuntimeException(
                    "Medicine must have more than 6 months before expiry. " +
                            "Expiry date must be after: " + minExpiry);
        }

        // Get collection point
        CollectionPoint collectionPoint =
                collectionPointRepository
                        .findById(request.getCollectionPointId())
                        .orElseThrow(() -> new RuntimeException(
                                "Collection point not found"));

        // Check if medicine exists in DB
        Optional<Medicine> medicineOpt =
                medicineService.findByBrandName(
                        request.getBrandNameSubmitted());

        // Build donation
        Donation donation = Donation.builder()
                .donor(donor)
                .brandNameSubmitted(request.getBrandNameSubmitted())
                .quantity(request.getQuantity())
                .expiryDate(request.getExpiryDate())
                .photoUrl(request.getPhotoUrl())
                .packageProofUrl(request.getPackageProofUrl())
                .collectionPoint(collectionPoint)
                .status(DonationStatus.PENDING_DOCTOR)
                .build();

        // If medicine found in DB — link it
        if (medicineOpt.isPresent()) {
            donation.setMedicine(medicineOpt.get());
        }

        // Save donation
        Donation saved = donationRepository.save(donation);

        return mapToDonationResponse(saved);
    }

    // ─────────────────────────────────────────
    // GET MY DONATIONS
    // ─────────────────────────────────────────

    public List<DonationResponse> getMyDonations(String donorEmail) {

        User donor = userRepository.findByEmail(donorEmail)
                .orElseThrow(() -> new RuntimeException(
                        "Donor not found"));

        return donationRepository.findByDonor(donor)
                .stream()
                .map(this::mapToDonationResponse)
                .collect(Collectors.toList());
    }

    // ─────────────────────────────────────────
    // GET DONATION BY ID
    // ─────────────────────────────────────────

    public DonationResponse getDonationById(Long id, String donorEmail) {

        Donation donation = donationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(
                        "Donation not found with id: " + id));

        // Make sure this donation belongs to this donor
        if (!donation.getDonor().getEmail().equals(donorEmail)) {
            throw new RuntimeException(
                    "You are not authorized to view this donation");
        }

        return mapToDonationResponse(donation);
    }

    // ─────────────────────────────────────────
    // GET ACTIVE COLLECTION POINTS
    // For donor to select when submitting
    // ─────────────────────────────────────────

    public List<CollectionPointResponse> getActiveCollectionPoints() {
        return collectionPointRepository.findByActive(true)
                .stream()
                .map(this::mapToCollectionPointResponse)
                .collect(Collectors.toList());
    }

    // ─────────────────────────────────────────
    // GET DONATION COUNTS BY STATUS
    // For donor dashboard
    // ─────────────────────────────────────────

    public java.util.Map<String, Long> getDonationCounts(
            String donorEmail) {

        User donor = userRepository.findByEmail(donorEmail)
                .orElseThrow(() -> new RuntimeException(
                        "Donor not found"));

        java.util.Map<String, Long> counts = new java.util.HashMap<>();

        counts.put("pendingDoctor",
                donationRepository.countByDonorAndStatus(
                        donor, DonationStatus.PENDING_DOCTOR));
        counts.put("pendingPharmacist",
                donationRepository.countByDonorAndStatus(
                        donor, DonationStatus.PENDING_PHARMACIST));
        counts.put("pendingDoctorRecheck",
                donationRepository.countByDonorAndStatus(
                        donor, DonationStatus.PENDING_DOCTOR_RECHECK));
        counts.put("pharmacistRejected",
                donationRepository.countByDonorAndStatus(
                        donor, DonationStatus.PHARMACIST_REJECTED));
        counts.put("doctorApproved",
                donationRepository.countByDonorAndStatus(
                        donor, DonationStatus.DOCTOR_APPROVED));
        counts.put("rejectedByDoctor",
                donationRepository.countByDonorAndStatus(
                        donor, DonationStatus.REJECTED_BY_DOCTOR));
        counts.put("pendingCustody",
                donationRepository.countByDonorAndStatus(
                        donor, DonationStatus.PENDING_CUSTODY));
        counts.put("live",
                donationRepository.countByDonorAndStatus(
                        donor, DonationStatus.LIVE));
        counts.put("collected",
                donationRepository.countByDonorAndStatus(
                        donor, DonationStatus.COLLECTED));

        return counts;
    }

    // ─────────────────────────────────────────
    // MAPPERS
    // ─────────────────────────────────────────

    public DonationResponse mapToDonationResponse(Donation donation) {
        return DonationResponse.builder()
                .id(donation.getId())
                .donorName(donation.getDonor().getName())
                .donorEmail(donation.getDonor().getEmail())
                .brandNameSubmitted(donation.getBrandNameSubmitted())
                .medicineId(donation.getMedicine() != null ?
                        donation.getMedicine().getId() : null)
                .medicineApiName(donation.getMedicine() != null ?
                        donation.getMedicine().getApiName() : null)
                .medicineStrength(donation.getMedicine() != null ?
                        donation.getMedicine().getStrength() : null)
                .medicineDosageForm(donation.getMedicine() != null ?
                        donation.getMedicine().getDosageForm() : null)
                .medicineSchedule(donation.getMedicine() != null ?
                        donation.getMedicine().getSchedule().name() : null)
                .quantity(donation.getQuantity())
                .expiryDate(donation.getExpiryDate())
                .photoUrl(donation.getPhotoUrl())
                .packageProofUrl(donation.getPackageProofUrl())
                .status(donation.getStatus().name())
                .doctorName(donation.getApprovedByDoctor() != null ?
                        donation.getApprovedByDoctor().getName() : null)
                .doctorNotes(donation.getDoctorNotes())
                .rejectionReason(donation.getRejectionReason())
                .pharmacistRejectionReason(
                        donation.getPharmacistRejectionReason())
                .collectionPointId(donation.getCollectionPoint() != null ?
                        donation.getCollectionPoint().getId() : null)
                .collectionPointName(donation.getCollectionPoint() != null ?
                        donation.getCollectionPoint().getLocationName() : null)
                .collectionPointAddress(
                        donation.getCollectionPoint() != null ?
                                donation.getCollectionPoint().getAddress() : null)
                .recipientName(donation.getRecipient() != null ?
                        donation.getRecipient().getName() : null)
                .createdAt(donation.getCreatedAt())
                .updatedAt(donation.getUpdatedAt())
                .build();
    }

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

import com.medicinedonation.dto.request.DonationRequest;
import com.medicinedonation.dto.response.CollectionPointResponse;
import com.medicinedonation.dto.response.DonationResponse;
import com.medicinedonation.enums.DonationStatus;
import com.medicinedonation.model.CollectionPoint;
import com.medicinedonation.model.Donation;
import com.medicinedonation.model.Medicine;
import com.medicinedonation.model.PendingMedicine;
import com.medicinedonation.model.User;
import com.medicinedonation.repository.CollectionPointRepository;
import com.medicinedonation.repository.DonationRepository;
import com.medicinedonation.repository.PendingMedicineRepository;
import com.medicinedonation.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class DonorService {

    @Autowired
    private DonationRepository donationRepository;

    @Autowired
    private CollectionPointRepository collectionPointRepository;

    @Autowired
    private PendingMedicineRepository pendingMedicineRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MedicineService medicineService;

    // ─────────────────────────────────────────
    // SUBMIT DONATION
    // ─────────────────────────────────────────

    public DonationResponse submitDonation(
            DonationRequest request, String donorEmail) {

        User donor = userRepository.findByEmail(donorEmail)
                .orElseThrow(() -> new RuntimeException("Donor not found"));

        // Validate expiry date — must be more than 6 months
        LocalDate minExpiry = LocalDate.now().plusMonths(6);
        if (request.getExpiryDate().isBefore(minExpiry)) {
            throw new RuntimeException(
                    "Medicine must have more than 6 months before expiry. " +
                            "Expiry date must be after: " + minExpiry);
        }

        CollectionPoint collectionPoint =
                collectionPointRepository
                        .findById(request.getCollectionPointId())
                        .orElseThrow(() -> new RuntimeException(
                                "Collection point not found"));

        Optional<Medicine> medicineOpt =
                medicineService.findByBrandName(
                        request.getBrandNameSubmitted());

        // ✅ Build donation — includes dosageForm and strength
        Donation donation = Donation.builder()
                .donor(donor)
                .brandNameSubmitted(request.getBrandNameSubmitted())
                .quantity(request.getQuantity())
                .expiryDate(request.getExpiryDate())
                .photoUrl(request.getPhotoUrl())
                .packageProofUrl(request.getPackageProofUrl())
                .dosageForm(request.getDosageForm())
                .strength(request.getStrength())
                .collectionPoint(collectionPoint)
                .status(DonationStatus.PENDING_DOCTOR)
                .build();

        if (medicineOpt.isPresent()) {
            donation.setMedicine(medicineOpt.get());
        }

        Donation saved = donationRepository.save(donation);
        return mapToDonationResponse(saved);
    }

    // ─────────────────────────────────────────
    // GET MY DONATIONS
    // ─────────────────────────────────────────

    public List<DonationResponse> getMyDonations(String donorEmail) {
        User donor = userRepository.findByEmail(donorEmail)
                .orElseThrow(() -> new RuntimeException("Donor not found"));

        return donationRepository.findByDonor(donor)
                .stream()
                .map(this::mapToDonationResponse)
                .collect(Collectors.toList());
    }

    // ─────────────────────────────────────────
    // GET DONATION BY ID
    // ─────────────────────────────────────────

    public DonationResponse getDonationById(Long id, String donorEmail) {
        Donation donation = donationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(
                        "Donation not found with id: " + id));

        if (!donation.getDonor().getEmail().equals(donorEmail)) {
            throw new RuntimeException(
                    "You are not authorized to view this donation");
        }

        return mapToDonationResponse(donation);
    }

    // ─────────────────────────────────────────
    // GET ACTIVE COLLECTION POINTS
    // ─────────────────────────────────────────

    public List<CollectionPointResponse> getActiveCollectionPoints() {
        return collectionPointRepository.findByActive(true)
                .stream()
                .map(this::mapToCollectionPointResponse)
                .collect(Collectors.toList());
    }

    // ─────────────────────────────────────────
    // GET DONATION COUNTS — donor dashboard
    // ─────────────────────────────────────────

    public java.util.Map<String, Long> getDonationCounts(String donorEmail) {

        User donor = userRepository.findByEmail(donorEmail)
                .orElseThrow(() -> new RuntimeException("Donor not found"));

        java.util.Map<String, Long> counts = new java.util.HashMap<>();

        // ✅ Total donations count
        counts.put("totalDonations",
                donationRepository.countByDonor(donor));

        counts.put("pendingDoctor",
                donationRepository.countByDonorAndStatus(
                        donor, DonationStatus.PENDING_DOCTOR));
        counts.put("pendingPharmacist",
                donationRepository.countByDonorAndStatus(
                        donor, DonationStatus.PENDING_PHARMACIST));
        counts.put("pendingDoctorRecheck",
                donationRepository.countByDonorAndStatus(
                        donor, DonationStatus.PENDING_DOCTOR_RECHECK));
        counts.put("pharmacistRejected",
                donationRepository.countByDonorAndStatus(
                        donor, DonationStatus.PHARMACIST_REJECTED));
        counts.put("doctorApproved",
                donationRepository.countByDonorAndStatus(
                        donor, DonationStatus.DOCTOR_APPROVED));
        counts.put("rejectedByDoctor",
                donationRepository.countByDonorAndStatus(
                        donor, DonationStatus.REJECTED_BY_DOCTOR));
        counts.put("pendingCustody",
                donationRepository.countByDonorAndStatus(
                        donor, DonationStatus.PENDING_CUSTODY));
        counts.put("live",
                donationRepository.countByDonorAndStatus(
                        donor, DonationStatus.LIVE));
        counts.put("collected",
                donationRepository.countByDonorAndStatus(
                        donor, DonationStatus.COLLECTED));

        return counts;
    }

    // ─────────────────────────────────────────
    // MAPPERS
    // ─────────────────────────────────────────

    public DonationResponse mapToDonationResponse(Donation donation) {
        return DonationResponse.builder()
                .id(donation.getId())
                .donorName(donation.getDonor().getName())
                .donorEmail(donation.getDonor().getEmail())
                .brandNameSubmitted(donation.getBrandNameSubmitted())
                .medicineId(donation.getMedicine() != null ?
                        donation.getMedicine().getId() : null)
                .medicineApiName(donation.getMedicine() != null ?
                        donation.getMedicine().getApiName() : null)
                .medicineStrength(donation.getMedicine() != null ?
                        donation.getMedicine().getStrength() : null)
                .medicineDosageForm(donation.getMedicine() != null ?
                        donation.getMedicine().getDosageForm() : null)
                .medicineSchedule(donation.getMedicine() != null ?
                        donation.getMedicine().getSchedule().name() : null)
                // ✅ Map donor provided dosage info
                .dosageForm(donation.getDosageForm())
                .strength(donation.getStrength())
                .quantity(donation.getQuantity())
                .expiryDate(donation.getExpiryDate())
                .photoUrl(donation.getPhotoUrl())
                .packageProofUrl(donation.getPackageProofUrl())
                .status(donation.getStatus().name())
                .doctorName(donation.getApprovedByDoctor() != null ?
                        donation.getApprovedByDoctor().getName() : null)
                .doctorNotes(donation.getDoctorNotes())
                .rejectionReason(donation.getRejectionReason())
                .pharmacistRejectionReason(
                        donation.getPharmacistRejectionReason())
                .collectionPointId(donation.getCollectionPoint() != null ?
                        donation.getCollectionPoint().getId() : null)
                .collectionPointName(donation.getCollectionPoint() != null ?
                        donation.getCollectionPoint().getLocationName() : null)
                .collectionPointAddress(donation.getCollectionPoint() != null ?
                        donation.getCollectionPoint().getAddress() : null)
                .recipientName(donation.getRecipient() != null ?
                        donation.getRecipient().getName() : null)
                .createdAt(donation.getCreatedAt())
                .updatedAt(donation.getUpdatedAt())
                .build();
    }

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

import com.medicinedonation.dto.request.DonationRequest;
import com.medicinedonation.dto.response.CollectionPointResponse;
import com.medicinedonation.dto.response.DonationResponse;
import com.medicinedonation.enums.DonationStatus;
import com.medicinedonation.model.CollectionPoint;
import com.medicinedonation.model.Donation;
import com.medicinedonation.model.Medicine;
import com.medicinedonation.model.PendingMedicine;
import com.medicinedonation.model.User;
import com.medicinedonation.repository.CollectionPointRepository;
import com.medicinedonation.repository.DonationRepository;
import com.medicinedonation.repository.MedicineRepository;
import com.medicinedonation.repository.PendingMedicineRepository;
import com.medicinedonation.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class DonorService {

    @Autowired
    private DonationRepository donationRepository;

    @Autowired
    private CollectionPointRepository collectionPointRepository;

    @Autowired
    private PendingMedicineRepository pendingMedicineRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MedicineService medicineService;

    @Autowired
    private MedicineRepository medicineRepository; // ✅ NEW

    // ─────────────────────────────────────────
    // SUBMIT DONATION
    // ─────────────────────────────────────────

    public DonationResponse submitDonation(
            DonationRequest request, String donorEmail) {

        User donor = userRepository.findByEmail(donorEmail)
                .orElseThrow(() -> new RuntimeException("Donor not found"));

        LocalDate minExpiry = LocalDate.now().plusMonths(6);
        if (request.getExpiryDate().isBefore(minExpiry)) {
            throw new RuntimeException(
                    "Medicine must have more than 6 months before expiry. " +
                            "Expiry date must be after: " + minExpiry);
        }

        CollectionPoint collectionPoint =
                collectionPointRepository
                        .findById(request.getCollectionPointId())
                        .orElseThrow(() -> new RuntimeException(
                                "Collection point not found"));

        Optional<Medicine> medicineOpt =
                medicineService.findByBrandName(
                        request.getBrandNameSubmitted());

        Donation donation = Donation.builder()
                .donor(donor)
                .brandNameSubmitted(request.getBrandNameSubmitted())
                .quantity(request.getQuantity())
                .expiryDate(request.getExpiryDate())
                .photoUrl(request.getPhotoUrl())
                .packageProofUrl(request.getPackageProofUrl())
                .dosageForm(request.getDosageForm())
                .strength(request.getStrength())
                .collectionPoint(collectionPoint)
                .status(DonationStatus.PENDING_DOCTOR)
                .build();

        if (medicineOpt.isPresent()) {
            donation.setMedicine(medicineOpt.get());
        }

        Donation saved = donationRepository.save(donation);
        return mapToDonationResponse(saved);
    }

    // ─────────────────────────────────────────
    // GET MY DONATIONS
    // ─────────────────────────────────────────

    public List<DonationResponse> getMyDonations(String donorEmail) {
        User donor = userRepository.findByEmail(donorEmail)
                .orElseThrow(() -> new RuntimeException("Donor not found"));

        return donationRepository.findByDonor(donor)
                .stream()
                .map(this::mapToDonationResponse)
                .collect(Collectors.toList());
    }

    // ─────────────────────────────────────────
    // GET DONATION BY ID
    // ─────────────────────────────────────────

    public DonationResponse getDonationById(Long id, String donorEmail) {
        Donation donation = donationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(
                        "Donation not found with id: " + id));

        if (!donation.getDonor().getEmail().equals(donorEmail)) {
            throw new RuntimeException(
                    "You are not authorized to view this donation");
        }

        return mapToDonationResponse(donation);
    }

    // ─────────────────────────────────────────
    // GET ACTIVE COLLECTION POINTS
    // ─────────────────────────────────────────

    public List<CollectionPointResponse> getActiveCollectionPoints() {
        return collectionPointRepository.findByActive(true)
                .stream()
                .map(this::mapToCollectionPointResponse)
                .collect(Collectors.toList());
    }

    // ─────────────────────────────────────────
    // GET DONATION COUNTS
    // ─────────────────────────────────────────

    public java.util.Map<String, Long> getDonationCounts(String donorEmail) {

        User donor = userRepository.findByEmail(donorEmail)
                .orElseThrow(() -> new RuntimeException("Donor not found"));

        java.util.Map<String, Long> counts = new java.util.HashMap<>();

        counts.put("totalDonations",
                donationRepository.countByDonor(donor));
        counts.put("pendingDoctor",
                donationRepository.countByDonorAndStatus(
                        donor, DonationStatus.PENDING_DOCTOR));
        counts.put("pendingPharmacist",
                donationRepository.countByDonorAndStatus(
                        donor, DonationStatus.PENDING_PHARMACIST));
        counts.put("pendingDoctorRecheck",
                donationRepository.countByDonorAndStatus(
                        donor, DonationStatus.PENDING_DOCTOR_RECHECK));
        counts.put("pharmacistRejected",
                donationRepository.countByDonorAndStatus(
                        donor, DonationStatus.PHARMACIST_REJECTED));
        counts.put("doctorApproved",
                donationRepository.countByDonorAndStatus(
                        donor, DonationStatus.DOCTOR_APPROVED));
        counts.put("rejectedByDoctor",
                donationRepository.countByDonorAndStatus(
                        donor, DonationStatus.REJECTED_BY_DOCTOR));
        counts.put("pendingCustody",
                donationRepository.countByDonorAndStatus(
                        donor, DonationStatus.PENDING_CUSTODY));
        counts.put("live",
                donationRepository.countByDonorAndStatus(
                        donor, DonationStatus.LIVE));
        counts.put("collected",
                donationRepository.countByDonorAndStatus(
                        donor, DonationStatus.COLLECTED));

        return counts;
    }

    // ─────────────────────────────────────────
    // COMPUTE DB MATCH STATUS
    //
    // FULL_MATCH    — brand + strength + dosageForm all match
    // PARTIAL_MATCH — brand found but strength or dosageForm differ
    // NO_MATCH      — brand not found at all
    // ─────────────────────────────────────────

    private String computeDbMatchStatus(Donation donation) {

        // If medicine already linked and pharmacist verified → always FULL_MATCH
        // (this is the recheck case — pharmacist verified it)
        if (donation.getMedicine() != null) {
            String dbStrength   = donation.getMedicine().getStrength();
            String dbDosageForm = donation.getMedicine().getDosageForm();
            String donorStrength   = donation.getStrength();
            String donorDosageForm = donation.getDosageForm();

            // Check if strength AND dosageForm match the linked medicine
            boolean strengthMatch = dbStrength != null && donorStrength != null &&
                    dbStrength.equalsIgnoreCase(donorStrength.trim());
            boolean dosageFormMatch = dbDosageForm != null && donorDosageForm != null &&
                    dbDosageForm.equalsIgnoreCase(donorDosageForm.trim());

            if (strengthMatch && dosageFormMatch) {
                return "FULL_MATCH";
            } else {
                return "PARTIAL_MATCH";
            }
        }

        // Medicine not linked — check brand name in DB
        String brandName = donation.getBrandNameSubmitted();
        if (brandName == null || brandName.isBlank()) {
            return "NO_MATCH";
        }

        Optional<Medicine> byBrand =
                medicineRepository.findByBrandNameIgnoreCase(brandName.trim());

        if (byBrand.isEmpty()) {
            // Brand not found at all
            return "NO_MATCH";
        }

        // Brand found — now check strength + dosageForm
        Medicine found = byBrand.get();
        String dbStrength   = found.getStrength();
        String dbDosageForm = found.getDosageForm();
        String donorStrength   = donation.getStrength();
        String donorDosageForm = donation.getDosageForm();

        boolean strengthMatch = dbStrength != null && donorStrength != null &&
                dbStrength.equalsIgnoreCase(donorStrength.trim());
        boolean dosageFormMatch = dbDosageForm != null && donorDosageForm != null &&
                dbDosageForm.equalsIgnoreCase(donorDosageForm.trim());

        if (strengthMatch && dosageFormMatch) {
            return "FULL_MATCH";
        } else {
            // Brand found but strength or dosageForm differs
            return "PARTIAL_MATCH";
        }
    }

    // ─────────────────────────────────────────
    // MAPPER
    // ─────────────────────────────────────────

    public DonationResponse mapToDonationResponse(Donation donation) {
        return DonationResponse.builder()
                .id(donation.getId())
                .donorName(donation.getDonor().getName())
                .donorEmail(donation.getDonor().getEmail())
                .brandNameSubmitted(donation.getBrandNameSubmitted())
                .medicineId(donation.getMedicine() != null ?
                        donation.getMedicine().getId() : null)
                .medicineApiName(donation.getMedicine() != null ?
                        donation.getMedicine().getApiName() : null)
                .medicineStrength(donation.getMedicine() != null ?
                        donation.getMedicine().getStrength() : null)
                .medicineDosageForm(donation.getMedicine() != null ?
                        donation.getMedicine().getDosageForm() : null)
                .medicineSchedule(donation.getMedicine() != null ?
                        donation.getMedicine().getSchedule().name() : null)
                .dosageForm(donation.getDosageForm())
                .strength(donation.getStrength())
                // ✅ NEW — computed match status
                .dbMatchStatus(computeDbMatchStatus(donation))
                .quantity(donation.getQuantity())
                .expiryDate(donation.getExpiryDate())
                .photoUrl(donation.getPhotoUrl())
                .packageProofUrl(donation.getPackageProofUrl())
                .status(donation.getStatus().name())
                .doctorName(donation.getApprovedByDoctor() != null ?
                        donation.getApprovedByDoctor().getName() : null)
                .doctorNotes(donation.getDoctorNotes())
                .rejectionReason(donation.getRejectionReason())
                .pharmacistRejectionReason(
                        donation.getPharmacistRejectionReason())
                .collectionPointId(donation.getCollectionPoint() != null ?
                        donation.getCollectionPoint().getId() : null)
                .collectionPointName(donation.getCollectionPoint() != null ?
                        donation.getCollectionPoint().getLocationName() : null)
                .collectionPointAddress(donation.getCollectionPoint() != null ?
                        donation.getCollectionPoint().getAddress() : null)
                .recipientName(donation.getRecipient() != null ?
                        donation.getRecipient().getName() : null)
                .createdAt(donation.getCreatedAt())
                .updatedAt(donation.getUpdatedAt())
                .build();
    }

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

import com.medicinedonation.dto.request.DonationRequest;
import com.medicinedonation.dto.response.CollectionPointResponse;
import com.medicinedonation.dto.response.DonationResponse;
import com.medicinedonation.enums.DonationStatus;
import com.medicinedonation.model.CollectionPoint;
import com.medicinedonation.model.Donation;
import com.medicinedonation.model.Medicine;
import com.medicinedonation.model.PendingMedicine;
import com.medicinedonation.model.User;
import com.medicinedonation.repository.CollectionPointRepository;
import com.medicinedonation.repository.DonationRepository;
import com.medicinedonation.repository.MedicineRepository;
import com.medicinedonation.repository.PendingMedicineRepository;
import com.medicinedonation.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class DonorService {

    @Autowired
    private DonationRepository donationRepository;

    @Autowired
    private CollectionPointRepository collectionPointRepository;

    @Autowired
    private PendingMedicineRepository pendingMedicineRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MedicineService medicineService;

    @Autowired
    private MedicineRepository medicineRepository;

    // ─────────────────────────────────────────
    // SUBMIT DONATION
    // ─────────────────────────────────────────

    public DonationResponse submitDonation(
            DonationRequest request, String donorEmail) {

        User donor = userRepository.findByEmail(donorEmail)
                .orElseThrow(() -> new RuntimeException("Donor not found"));

        LocalDate minExpiry = LocalDate.now().plusMonths(6);
        if (request.getExpiryDate().isBefore(minExpiry)) {
            throw new RuntimeException(
                    "Medicine must have more than 6 months before expiry. " +
                            "Expiry date must be after: " + minExpiry);
        }

        CollectionPoint collectionPoint =
                collectionPointRepository
                        .findById(request.getCollectionPointId())
                        .orElseThrow(() -> new RuntimeException(
                                "Collection point not found"));

        Optional<Medicine> medicineOpt =
                medicineService.findByBrandName(
                        request.getBrandNameSubmitted());

        Donation donation = Donation.builder()
                .donor(donor)
                .brandNameSubmitted(request.getBrandNameSubmitted())
                .quantity(request.getQuantity())
                .expiryDate(request.getExpiryDate())
                .photoUrl(request.getPhotoUrl())
                .packageProofUrl(request.getPackageProofUrl())
                .dosageForm(request.getDosageForm())
                .strength(request.getStrength())
                .collectionPoint(collectionPoint)
                .status(DonationStatus.PENDING_DOCTOR)
                .build();

        if (medicineOpt.isPresent()) {
            donation.setMedicine(medicineOpt.get());
        }

        Donation saved = donationRepository.save(donation);
        return mapToDonationResponse(saved);
    }

    // ─────────────────────────────────────────
    // GET MY DONATIONS
    // ─────────────────────────────────────────

    public List<DonationResponse> getMyDonations(String donorEmail) {
        User donor = userRepository.findByEmail(donorEmail)
                .orElseThrow(() -> new RuntimeException("Donor not found"));

        return donationRepository.findByDonor(donor)
                .stream()
                .map(this::mapToDonationResponse)
                .collect(Collectors.toList());
    }

    // ─────────────────────────────────────────
    // GET DONATION BY ID
    // ─────────────────────────────────────────

    public DonationResponse getDonationById(Long id, String donorEmail) {
        Donation donation = donationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(
                        "Donation not found with id: " + id));

        if (!donation.getDonor().getEmail().equals(donorEmail)) {
            throw new RuntimeException(
                    "You are not authorized to view this donation");
        }

        return mapToDonationResponse(donation);
    }

    // ─────────────────────────────────────────
    // GET ACTIVE COLLECTION POINTS
    // ─────────────────────────────────────────

    public List<CollectionPointResponse> getActiveCollectionPoints() {
        return collectionPointRepository.findByActive(true)
                .stream()
                .map(this::mapToCollectionPointResponse)
                .collect(Collectors.toList());
    }

    // ─────────────────────────────────────────
    // GET DONATION COUNTS
    // ─────────────────────────────────────────

    public java.util.Map<String, Long> getDonationCounts(String donorEmail) {

        User donor = userRepository.findByEmail(donorEmail)
                .orElseThrow(() -> new RuntimeException("Donor not found"));

        java.util.Map<String, Long> counts = new java.util.HashMap<>();

        counts.put("totalDonations",
                donationRepository.countByDonor(donor));
        counts.put("pendingDoctor",
                donationRepository.countByDonorAndStatus(
                        donor, DonationStatus.PENDING_DOCTOR));
        counts.put("pendingPharmacist",
                donationRepository.countByDonorAndStatus(
                        donor, DonationStatus.PENDING_PHARMACIST));
        counts.put("pendingDoctorRecheck",
                donationRepository.countByDonorAndStatus(
                        donor, DonationStatus.PENDING_DOCTOR_RECHECK));
        counts.put("pharmacistRejected",
                donationRepository.countByDonorAndStatus(
                        donor, DonationStatus.PHARMACIST_REJECTED));
        counts.put("doctorApproved",
                donationRepository.countByDonorAndStatus(
                        donor, DonationStatus.DOCTOR_APPROVED));
        counts.put("rejectedByDoctor",
                donationRepository.countByDonorAndStatus(
                        donor, DonationStatus.REJECTED_BY_DOCTOR));
        counts.put("pendingCustody",
                donationRepository.countByDonorAndStatus(
                        donor, DonationStatus.PENDING_CUSTODY));
        counts.put("live",
                donationRepository.countByDonorAndStatus(
                        donor, DonationStatus.LIVE));
        counts.put("collected",
                donationRepository.countByDonorAndStatus(
                        donor, DonationStatus.COLLECTED));

        return counts;
    }

    // ─────────────────────────────────────────
    // COMPUTE DB MATCH STATUS
    //
    // FULL_MATCH    — brand + strength + dosageForm all match
    // PARTIAL_MATCH — brand found but strength or dosageForm differ
    // NO_MATCH      — brand not found at all
    // ─────────────────────────────────────────

    private String computeDbMatchStatus(Donation donation) {

        // If medicine already linked by pharmacist → check corrected or donor details
        if (donation.getMedicine() != null) {
            String dbStrength   = donation.getMedicine().getStrength();
            String dbDosageForm = donation.getMedicine().getDosageForm();

            // ✅ Use doctor's corrected values if available, else donor's original
            String checkStrength = (donation.getDoctorCorrectedStrength() != null &&
                    !donation.getDoctorCorrectedStrength().isBlank())
                    ? donation.getDoctorCorrectedStrength()
                    : donation.getStrength();
            String checkDosageForm = (donation.getDoctorCorrectedDosageForm() != null &&
                    !donation.getDoctorCorrectedDosageForm().isBlank())
                    ? donation.getDoctorCorrectedDosageForm()
                    : donation.getDosageForm();

            boolean strengthMatch = dbStrength != null && checkStrength != null &&
                    dbStrength.equalsIgnoreCase(checkStrength.trim());
            boolean dosageFormMatch = dbDosageForm != null && checkDosageForm != null &&
                    dbDosageForm.equalsIgnoreCase(checkDosageForm.trim());

            return (strengthMatch && dosageFormMatch) ? "FULL_MATCH" : "PARTIAL_MATCH";
        }

        // ✅ Use doctor's corrected brand name if available, else donor's original
        String brandName = (donation.getDoctorCorrectedBrandName() != null &&
                !donation.getDoctorCorrectedBrandName().isBlank())
                ? donation.getDoctorCorrectedBrandName()
                : donation.getBrandNameSubmitted();

        if (brandName == null || brandName.isBlank()) {
            return "NO_MATCH";
        }

        Optional<Medicine> byBrand =
                medicineRepository.findByBrandNameIgnoreCase(brandName.trim());

        if (byBrand.isEmpty()) {
            return "NO_MATCH";
        }

        // Brand found — check corrected or donor's strength + dosageForm
        Medicine found = byBrand.get();
        String dbStrength   = found.getStrength();
        String dbDosageForm = found.getDosageForm();

        String checkStrength = (donation.getDoctorCorrectedStrength() != null &&
                !donation.getDoctorCorrectedStrength().isBlank())
                ? donation.getDoctorCorrectedStrength()
                : donation.getStrength();
        String checkDosageForm = (donation.getDoctorCorrectedDosageForm() != null &&
                !donation.getDoctorCorrectedDosageForm().isBlank())
                ? donation.getDoctorCorrectedDosageForm()
                : donation.getDosageForm();

        boolean strengthMatch = dbStrength != null && checkStrength != null &&
                dbStrength.equalsIgnoreCase(checkStrength.trim());
        boolean dosageFormMatch = dbDosageForm != null && checkDosageForm != null &&
                dbDosageForm.equalsIgnoreCase(checkDosageForm.trim());

        return (strengthMatch && dosageFormMatch) ? "FULL_MATCH" : "PARTIAL_MATCH";
    }

    // ─────────────────────────────────────────
    // MAPPER
    // ─────────────────────────────────────────

    public DonationResponse mapToDonationResponse(Donation donation) {
        return DonationResponse.builder()
                .id(donation.getId())
                .donorName(donation.getDonor().getName())
                .donorEmail(donation.getDonor().getEmail())
                .brandNameSubmitted(donation.getBrandNameSubmitted())
                .medicineId(donation.getMedicine() != null ?
                        donation.getMedicine().getId() : null)
                .medicineApiName(donation.getMedicine() != null ?
                        donation.getMedicine().getApiName() : null)
                .medicineStrength(donation.getMedicine() != null ?
                        donation.getMedicine().getStrength() : null)
                .medicineDosageForm(donation.getMedicine() != null ?
                        donation.getMedicine().getDosageForm() : null)
                .medicineSchedule(donation.getMedicine() != null ?
                        donation.getMedicine().getSchedule().name() : null)
                .dosageForm(donation.getDosageForm())
                .strength(donation.getStrength())
                // ✅ Doctor correction fields
                .doctorCorrectedBrandName(donation.getDoctorCorrectedBrandName())
                .doctorCorrectedStrength(donation.getDoctorCorrectedStrength())
                .doctorCorrectedDosageForm(donation.getDoctorCorrectedDosageForm())
                .doctorCorrectedQuantity(donation.getDoctorCorrectedQuantity())
                .doctorCorrectedExpiryDate(donation.getDoctorCorrectedExpiryDate())
                // ✅ DB match status
                .dbMatchStatus(computeDbMatchStatus(donation))
                .quantity(donation.getQuantity())
                .expiryDate(donation.getExpiryDate())
                .photoUrl(donation.getPhotoUrl())
                .packageProofUrl(donation.getPackageProofUrl())
                .status(donation.getStatus().name())
                .doctorName(donation.getApprovedByDoctor() != null ?
                        donation.getApprovedByDoctor().getName() : null)
                .doctorNotes(donation.getDoctorNotes())
                .rejectionReason(donation.getRejectionReason())
                .pharmacistRejectionReason(
                        donation.getPharmacistRejectionReason())
                .collectionPointId(donation.getCollectionPoint() != null ?
                        donation.getCollectionPoint().getId() : null)
                .collectionPointName(donation.getCollectionPoint() != null ?
                        donation.getCollectionPoint().getLocationName() : null)
                .collectionPointAddress(donation.getCollectionPoint() != null ?
                        donation.getCollectionPoint().getAddress() : null)
                .recipientName(donation.getRecipient() != null ?
                        donation.getRecipient().getName() : null)
                .createdAt(donation.getCreatedAt())
                .updatedAt(donation.getUpdatedAt())
                .build();
    }

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

import com.medicinedonation.dto.request.DonationRequest;
import com.medicinedonation.dto.response.CollectionPointResponse;
import com.medicinedonation.dto.response.DonationResponse;
import com.medicinedonation.enums.DonationStatus;
import com.medicinedonation.model.CollectionPoint;
import com.medicinedonation.model.Donation;
import com.medicinedonation.model.Medicine;
import com.medicinedonation.model.PendingMedicine;
import com.medicinedonation.model.User;
import com.medicinedonation.repository.CollectionPointRepository;
import com.medicinedonation.repository.DonationRepository;
import com.medicinedonation.repository.MedicineRepository;
import com.medicinedonation.repository.PendingMedicineRepository;
import com.medicinedonation.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class DonorService {

    @Autowired
    private DonationRepository donationRepository;

    @Autowired
    private CollectionPointRepository collectionPointRepository;

    @Autowired
    private PendingMedicineRepository pendingMedicineRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MedicineService medicineService;

    @Autowired
    private MedicineRepository medicineRepository;

    // ─────────────────────────────────────────
    // SUBMIT DONATION
    // ─────────────────────────────────────────

    public DonationResponse submitDonation(
            DonationRequest request, String donorEmail) {

        User donor = userRepository.findByEmail(donorEmail)
                .orElseThrow(() -> new RuntimeException("Donor not found"));

        LocalDate minExpiry = LocalDate.now().plusMonths(6);
        if (request.getExpiryDate().isBefore(minExpiry)) {
            throw new RuntimeException(
                    "Medicine must have more than 6 months before expiry. " +
                            "Expiry date must be after: " + minExpiry);
        }

        CollectionPoint collectionPoint =
                collectionPointRepository
                        .findById(request.getCollectionPointId())
                        .orElseThrow(() -> new RuntimeException(
                                "Collection point not found"));

        Optional<Medicine> medicineOpt =
                medicineService.findByBrandName(
                        request.getBrandNameSubmitted());

        Donation donation = Donation.builder()
                .donor(donor)
                .brandNameSubmitted(request.getBrandNameSubmitted())
                .quantity(request.getQuantity())
                .expiryDate(request.getExpiryDate())
                .photoUrl(request.getPhotoUrl())
                .packageProofUrl(request.getPackageProofUrl())
                .dosageForm(request.getDosageForm())
                .strength(request.getStrength())
                .collectionPoint(collectionPoint)
                .status(DonationStatus.PENDING_DOCTOR)
                .build();

        if (medicineOpt.isPresent()) {
            donation.setMedicine(medicineOpt.get());
        }

        Donation saved = donationRepository.save(donation);
        return mapToDonationResponse(saved);
    }

    // ─────────────────────────────────────────
    // GET MY DONATIONS
    // ─────────────────────────────────────────

    public List<DonationResponse> getMyDonations(String donorEmail) {
        User donor = userRepository.findByEmail(donorEmail)
                .orElseThrow(() -> new RuntimeException("Donor not found"));

        return donationRepository.findByDonor(donor)
                .stream()
                .map(this::mapToDonationResponse)
                .collect(Collectors.toList());
    }

    // ─────────────────────────────────────────
    // GET DONATION BY ID
    // ─────────────────────────────────────────

    public DonationResponse getDonationById(Long id, String donorEmail) {
        Donation donation = donationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(
                        "Donation not found with id: " + id));

        if (!donation.getDonor().getEmail().equals(donorEmail)) {
            throw new RuntimeException(
                    "You are not authorized to view this donation");
        }

        return mapToDonationResponse(donation);
    }

    // ─────────────────────────────────────────
    // GET ACTIVE COLLECTION POINTS
    // ─────────────────────────────────────────

    public List<CollectionPointResponse> getActiveCollectionPoints() {
        return collectionPointRepository.findByActive(true)
                .stream()
                .map(this::mapToCollectionPointResponse)
                .collect(Collectors.toList());
    }

    // ─────────────────────────────────────────
    // GET DONATION COUNTS
    // ─────────────────────────────────────────

    public java.util.Map<String, Long> getDonationCounts(String donorEmail) {

        User donor = userRepository.findByEmail(donorEmail)
                .orElseThrow(() -> new RuntimeException("Donor not found"));

        java.util.Map<String, Long> counts = new java.util.HashMap<>();

        counts.put("totalDonations",
                donationRepository.countByDonor(donor));
        counts.put("pendingDoctor",
                donationRepository.countByDonorAndStatus(
                        donor, DonationStatus.PENDING_DOCTOR));
        counts.put("pendingPharmacist",
                donationRepository.countByDonorAndStatus(
                        donor, DonationStatus.PENDING_PHARMACIST));
        counts.put("pendingDoctorRecheck",
                donationRepository.countByDonorAndStatus(
                        donor, DonationStatus.PENDING_DOCTOR_RECHECK));
        counts.put("pharmacistRejected",
                donationRepository.countByDonorAndStatus(
                        donor, DonationStatus.PHARMACIST_REJECTED));
        counts.put("doctorApproved",
                donationRepository.countByDonorAndStatus(
                        donor, DonationStatus.DOCTOR_APPROVED));
        counts.put("rejectedByDoctor",
                donationRepository.countByDonorAndStatus(
                        donor, DonationStatus.REJECTED_BY_DOCTOR));
        counts.put("pendingCustody",
                donationRepository.countByDonorAndStatus(
                        donor, DonationStatus.PENDING_CUSTODY));
        counts.put("live",
                donationRepository.countByDonorAndStatus(
                        donor, DonationStatus.LIVE));
        counts.put("collected",
                donationRepository.countByDonorAndStatus(
                        donor, DonationStatus.COLLECTED));

        return counts;
    }

    // ─────────────────────────────────────────
    // COMPUTE DB MATCH STATUS
    //
    // FULL_MATCH    — brand + strength + dosageForm all match
    // PARTIAL_MATCH — brand found but strength or dosageForm differ
    // NO_MATCH      — brand not found at all
    // ─────────────────────────────────────────

    private String computeDbMatchStatus(Donation donation) {

        // If medicine already linked by pharmacist → check corrected or donor details
        if (donation.getMedicine() != null) {
            String dbStrength   = donation.getMedicine().getStrength();
            String dbDosageForm = donation.getMedicine().getDosageForm();

            // ✅ Use doctor's corrected values if available, else donor's original
            String checkStrength = (donation.getDoctorCorrectedStrength() != null &&
                    !donation.getDoctorCorrectedStrength().isBlank())
                    ? donation.getDoctorCorrectedStrength()
                    : donation.getStrength();
            String checkDosageForm = (donation.getDoctorCorrectedDosageForm() != null &&
                    !donation.getDoctorCorrectedDosageForm().isBlank())
                    ? donation.getDoctorCorrectedDosageForm()
                    : donation.getDosageForm();

            boolean strengthMatch = dbStrength != null && checkStrength != null &&
                    dbStrength.equalsIgnoreCase(checkStrength.trim());
            boolean dosageFormMatch = dbDosageForm != null && checkDosageForm != null &&
                    dbDosageForm.equalsIgnoreCase(checkDosageForm.trim());

            return (strengthMatch && dosageFormMatch) ? "FULL_MATCH" : "PARTIAL_MATCH";
        }

        // ✅ Use doctor's corrected brand name if available, else donor's original
        String brandName = (donation.getDoctorCorrectedBrandName() != null &&
                !donation.getDoctorCorrectedBrandName().isBlank())
                ? donation.getDoctorCorrectedBrandName()
                : donation.getBrandNameSubmitted();

        if (brandName == null || brandName.isBlank()) {
            return "NO_MATCH";
        }

        // ✅ FIXED — List instead of Optional, safe for multiple entries (e.g. Panadol 500mg + Panadol Extra)
        List<Medicine> byBrand =
                medicineRepository.findByBrandNameIgnoreCase(brandName.trim());

        if (byBrand.isEmpty()) {
            return "NO_MATCH";
        }

        String checkStrength = (donation.getDoctorCorrectedStrength() != null &&
                !donation.getDoctorCorrectedStrength().isBlank())
                ? donation.getDoctorCorrectedStrength()
                : donation.getStrength();
        String checkDosageForm = (donation.getDoctorCorrectedDosageForm() != null &&
                !donation.getDoctorCorrectedDosageForm().isBlank())
                ? donation.getDoctorCorrectedDosageForm()
                : donation.getDosageForm();

        // ✅ Check if ANY entry with this brand has matching strength + dosageForm
        boolean exactMatch = byBrand.stream().anyMatch(m -> {
            boolean strengthMatch = m.getStrength() != null && checkStrength != null &&
                    m.getStrength().equalsIgnoreCase(checkStrength.trim());
            boolean dosageFormMatch = m.getDosageForm() != null && checkDosageForm != null &&
                    m.getDosageForm().equalsIgnoreCase(checkDosageForm.trim());
            return strengthMatch && dosageFormMatch;
        });

        return exactMatch ? "FULL_MATCH" : "PARTIAL_MATCH";
    }

    // ─────────────────────────────────────────
    // MAPPER
    // ─────────────────────────────────────────

    public DonationResponse mapToDonationResponse(Donation donation) {
        return DonationResponse.builder()
                .id(donation.getId())
                .donorName(donation.getDonor().getName())
                .donorEmail(donation.getDonor().getEmail())
                .brandNameSubmitted(donation.getBrandNameSubmitted())
                .medicineId(donation.getMedicine() != null ?
                        donation.getMedicine().getId() : null)
                .medicineApiName(donation.getMedicine() != null ?
                        donation.getMedicine().getApiName() : null)
                .medicineStrength(donation.getMedicine() != null ?
                        donation.getMedicine().getStrength() : null)
                .medicineDosageForm(donation.getMedicine() != null ?
                        donation.getMedicine().getDosageForm() : null)
                .medicineSchedule(donation.getMedicine() != null ?
                        donation.getMedicine().getSchedule().name() : null)
                .dosageForm(donation.getDosageForm())
                .strength(donation.getStrength())
                // ✅ Doctor correction fields
                .doctorCorrectedBrandName(donation.getDoctorCorrectedBrandName())
                .doctorCorrectedStrength(donation.getDoctorCorrectedStrength())
                .doctorCorrectedDosageForm(donation.getDoctorCorrectedDosageForm())
                .doctorCorrectedQuantity(donation.getDoctorCorrectedQuantity())
                .doctorCorrectedExpiryDate(donation.getDoctorCorrectedExpiryDate())
                // ✅ DB match status
                .dbMatchStatus(computeDbMatchStatus(donation))
                .quantity(donation.getQuantity())
                .expiryDate(donation.getExpiryDate())
                .photoUrl(donation.getPhotoUrl())
                .packageProofUrl(donation.getPackageProofUrl())
                .status(donation.getStatus().name())
                .doctorName(donation.getApprovedByDoctor() != null ?
                        donation.getApprovedByDoctor().getName() : null)
                .doctorNotes(donation.getDoctorNotes())
                .rejectionReason(donation.getRejectionReason())
                .pharmacistRejectionReason(
                        donation.getPharmacistRejectionReason())
                .collectionPointId(donation.getCollectionPoint() != null ?
                        donation.getCollectionPoint().getId() : null)
                .collectionPointName(donation.getCollectionPoint() != null ?
                        donation.getCollectionPoint().getLocationName() : null)
                .collectionPointAddress(donation.getCollectionPoint() != null ?
                        donation.getCollectionPoint().getAddress() : null)
                .recipientName(donation.getRecipient() != null ?
                        donation.getRecipient().getName() : null)
                .createdAt(donation.getCreatedAt())
                .updatedAt(donation.getUpdatedAt())
                .build();
    }

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

import com.medicinedonation.dto.request.DonationRequest;
import com.medicinedonation.dto.response.CollectionPointResponse;
import com.medicinedonation.dto.response.DonationResponse;
import com.medicinedonation.enums.DonationStatus;
import com.medicinedonation.model.CollectionPoint;
import com.medicinedonation.model.Donation;
import com.medicinedonation.model.Medicine;
import com.medicinedonation.model.PendingMedicine;
import com.medicinedonation.model.User;
import com.medicinedonation.repository.CollectionPointRepository;
import com.medicinedonation.repository.DonationRepository;
import com.medicinedonation.repository.MedicineRepository;
import com.medicinedonation.repository.PendingMedicineRepository;
import com.medicinedonation.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class DonorService {

    @Autowired
    private DonationRepository donationRepository;

    @Autowired
    private CollectionPointRepository collectionPointRepository;

    @Autowired
    private PendingMedicineRepository pendingMedicineRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MedicineService medicineService;

    @Autowired
    private MedicineRepository medicineRepository;

    // ─────────────────────────────────────────
    // SUBMIT DONATION
    // ─────────────────────────────────────────

    public DonationResponse submitDonation(
            DonationRequest request, String donorEmail) {

        User donor = userRepository.findByEmail(donorEmail)
                .orElseThrow(() -> new RuntimeException("Donor not found"));

        LocalDate minExpiry = LocalDate.now().plusMonths(6);
        if (request.getExpiryDate().isBefore(minExpiry)) {
            throw new RuntimeException(
                    "Medicine must have more than 6 months before expiry. " +
                            "Expiry date must be after: " + minExpiry);
        }

        CollectionPoint collectionPoint =
                collectionPointRepository
                        .findById(request.getCollectionPointId())
                        .orElseThrow(() -> new RuntimeException(
                                "Collection point not found"));

        Optional<Medicine> medicineOpt =
                medicineService.findByBrandName(
                        request.getBrandNameSubmitted());

        Donation donation = Donation.builder()
                .donor(donor)
                .brandNameSubmitted(request.getBrandNameSubmitted())
                .quantity(request.getQuantity())
                .expiryDate(request.getExpiryDate())
                .photoUrl(request.getPhotoUrl())
                .packageProofUrl(request.getPackageProofUrl())
                .dosageForm(request.getDosageForm())
                .strength(request.getStrength())
                .collectionPoint(collectionPoint)
                .status(DonationStatus.PENDING_DOCTOR)
                .build();

        if (medicineOpt.isPresent()) {
            donation.setMedicine(medicineOpt.get());
        }

        Donation saved = donationRepository.save(donation);
        return mapToDonationResponse(saved);
    }

    // ─────────────────────────────────────────
    // GET MY DONATIONS
    // ─────────────────────────────────────────

    public List<DonationResponse> getMyDonations(String donorEmail) {
        User donor = userRepository.findByEmail(donorEmail)
                .orElseThrow(() -> new RuntimeException("Donor not found"));

        return donationRepository.findByDonor(donor)
                .stream()
                .map(this::mapToDonationResponse)
                .collect(Collectors.toList());
    }

    // ─────────────────────────────────────────
    // GET DONATION BY ID
    // ─────────────────────────────────────────

    public DonationResponse getDonationById(Long id, String donorEmail) {
        Donation donation = donationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(
                        "Donation not found with id: " + id));

        if (!donation.getDonor().getEmail().equals(donorEmail)) {
            throw new RuntimeException(
                    "You are not authorized to view this donation");
        }

        return mapToDonationResponse(donation);
    }

    // ─────────────────────────────────────────
    // GET ACTIVE COLLECTION POINTS
    // ─────────────────────────────────────────

    public List<CollectionPointResponse> getActiveCollectionPoints() {
        return collectionPointRepository.findByActive(true)
                .stream()
                .map(this::mapToCollectionPointResponse)
                .collect(Collectors.toList());
    }

    // ─────────────────────────────────────────
    // GET DONATION COUNTS
    // ─────────────────────────────────────────

    public java.util.Map<String, Long> getDonationCounts(String donorEmail) {

        User donor = userRepository.findByEmail(donorEmail)
                .orElseThrow(() -> new RuntimeException("Donor not found"));

        java.util.Map<String, Long> counts = new java.util.HashMap<>();

        counts.put("totalDonations",
                donationRepository.countByDonor(donor));
        counts.put("pendingDoctor",
                donationRepository.countByDonorAndStatus(
                        donor, DonationStatus.PENDING_DOCTOR));
        counts.put("pendingPharmacist",
                donationRepository.countByDonorAndStatus(
                        donor, DonationStatus.PENDING_PHARMACIST));
        counts.put("pendingDoctorRecheck",
                donationRepository.countByDonorAndStatus(
                        donor, DonationStatus.PENDING_DOCTOR_RECHECK));
        counts.put("pharmacistRejected",
                donationRepository.countByDonorAndStatus(
                        donor, DonationStatus.PHARMACIST_REJECTED));
        counts.put("doctorApproved",
                donationRepository.countByDonorAndStatus(
                        donor, DonationStatus.DOCTOR_APPROVED));
        counts.put("rejectedByDoctor",
                donationRepository.countByDonorAndStatus(
                        donor, DonationStatus.REJECTED_BY_DOCTOR));
        counts.put("pendingCustody",
                donationRepository.countByDonorAndStatus(
                        donor, DonationStatus.PENDING_CUSTODY));
        counts.put("live",
                donationRepository.countByDonorAndStatus(
                        donor, DonationStatus.LIVE));
        counts.put("collected",
                donationRepository.countByDonorAndStatus(
                        donor, DonationStatus.COLLECTED));

        return counts;
    }

    // ─────────────────────────────────────────
    // COMPUTE DB MATCH STATUS
    //
    // FULL_MATCH    — brand + strength + dosageForm all match
    // PARTIAL_MATCH — brand found but strength or dosageForm differ
    // NO_MATCH      — brand not found at all
    // ─────────────────────────────────────────

    private String computeDbMatchStatus(Donation donation) {

        // If medicine already linked by pharmacist → check corrected or donor details
        if (donation.getMedicine() != null) {
            String dbStrength   = donation.getMedicine().getStrength();
            String dbDosageForm = donation.getMedicine().getDosageForm();

            // ✅ Use doctor's corrected values if available, else donor's original
            String checkStrength = (donation.getDoctorCorrectedStrength() != null &&
                    !donation.getDoctorCorrectedStrength().isBlank())
                    ? donation.getDoctorCorrectedStrength()
                    : donation.getStrength();
            String checkDosageForm = (donation.getDoctorCorrectedDosageForm() != null &&
                    !donation.getDoctorCorrectedDosageForm().isBlank())
                    ? donation.getDoctorCorrectedDosageForm()
                    : donation.getDosageForm();

            boolean strengthMatch = dbStrength != null && checkStrength != null &&
                    dbStrength.replaceAll("\\s+", "").equalsIgnoreCase(
                            checkStrength.trim().replaceAll("\\s+", ""));
            boolean dosageFormMatch = dbDosageForm != null && checkDosageForm != null &&
                    dbDosageForm.equalsIgnoreCase(checkDosageForm.trim());

            return (strengthMatch && dosageFormMatch) ? "FULL_MATCH" : "PARTIAL_MATCH";
        }

        // ✅ Use doctor's corrected brand name if available, else donor's original
        String brandName = (donation.getDoctorCorrectedBrandName() != null &&
                !donation.getDoctorCorrectedBrandName().isBlank())
                ? donation.getDoctorCorrectedBrandName()
                : donation.getBrandNameSubmitted();

        if (brandName == null || brandName.isBlank()) {
            return "NO_MATCH";
        }

        // ✅ FIXED — List instead of Optional, safe for multiple entries (e.g. Panadol 500mg + Panadol Extra)
        List<Medicine> byBrand =
                medicineRepository.findByBrandNameIgnoreCase(brandName.trim());

        if (byBrand.isEmpty()) {
            return "NO_MATCH";
        }

        String checkStrength = (donation.getDoctorCorrectedStrength() != null &&
                !donation.getDoctorCorrectedStrength().isBlank())
                ? donation.getDoctorCorrectedStrength()
                : donation.getStrength();
        String checkDosageForm = (donation.getDoctorCorrectedDosageForm() != null &&
                !donation.getDoctorCorrectedDosageForm().isBlank())
                ? donation.getDoctorCorrectedDosageForm()
                : donation.getDosageForm();

        // ✅ Check if ANY entry with this brand has matching strength + dosageForm
        // ✅ Normalize spaces e.g. "500mg" == "500 mg"
        boolean exactMatch = byBrand.stream().anyMatch(m -> {
            boolean strengthMatch = m.getStrength() != null && checkStrength != null &&
                    m.getStrength().replaceAll("\\s+", "").equalsIgnoreCase(
                            checkStrength.trim().replaceAll("\\s+", ""));
            boolean dosageFormMatch = m.getDosageForm() != null && checkDosageForm != null &&
                    m.getDosageForm().equalsIgnoreCase(checkDosageForm.trim());
            return strengthMatch && dosageFormMatch;
        });

        return exactMatch ? "FULL_MATCH" : "PARTIAL_MATCH";
    }

    // ─────────────────────────────────────────
    // MAPPER
    // ─────────────────────────────────────────

    public DonationResponse mapToDonationResponse(Donation donation) {
        return DonationResponse.builder()
                .id(donation.getId())
                .donorName(donation.getDonor().getName())
                .donorEmail(donation.getDonor().getEmail())
                .brandNameSubmitted(donation.getBrandNameSubmitted())
                .medicineId(donation.getMedicine() != null ?
                        donation.getMedicine().getId() : null)
                .medicineApiName(donation.getMedicine() != null ?
                        donation.getMedicine().getApiName() : null)
                .medicineStrength(donation.getMedicine() != null ?
                        donation.getMedicine().getStrength() : null)
                .medicineDosageForm(donation.getMedicine() != null ?
                        donation.getMedicine().getDosageForm() : null)
                .medicineSchedule(donation.getMedicine() != null ?
                        donation.getMedicine().getSchedule().name() : null)
                .dosageForm(donation.getDosageForm())
                .strength(donation.getStrength())
                // ✅ Doctor correction fields
                .doctorCorrectedBrandName(donation.getDoctorCorrectedBrandName())
                .doctorCorrectedStrength(donation.getDoctorCorrectedStrength())
                .doctorCorrectedDosageForm(donation.getDoctorCorrectedDosageForm())
                .doctorCorrectedQuantity(donation.getDoctorCorrectedQuantity())
                .doctorCorrectedExpiryDate(donation.getDoctorCorrectedExpiryDate())
                // ✅ DB match status
                .dbMatchStatus(computeDbMatchStatus(donation))
                .quantity(donation.getQuantity())
                .expiryDate(donation.getExpiryDate())
                .photoUrl(donation.getPhotoUrl())
                .packageProofUrl(donation.getPackageProofUrl())
                .status(donation.getStatus().name())
                .doctorName(donation.getApprovedByDoctor() != null ?
                        donation.getApprovedByDoctor().getName() : null)
                .doctorNotes(donation.getDoctorNotes())
                .rejectionReason(donation.getRejectionReason())
                .pharmacistRejectionReason(
                        donation.getPharmacistRejectionReason())
                .collectionPointId(donation.getCollectionPoint() != null ?
                        donation.getCollectionPoint().getId() : null)
                .collectionPointName(donation.getCollectionPoint() != null ?
                        donation.getCollectionPoint().getLocationName() : null)
                .collectionPointAddress(donation.getCollectionPoint() != null ?
                        donation.getCollectionPoint().getAddress() : null)
                .recipientName(donation.getRecipient() != null ?
                        donation.getRecipient().getName() : null)
                .createdAt(donation.getCreatedAt())
                .updatedAt(donation.getUpdatedAt())
                .build();
    }

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

import com.medicinedonation.dto.request.DonationRequest;
import com.medicinedonation.dto.response.CollectionPointResponse;
import com.medicinedonation.dto.response.DonationResponse;
import com.medicinedonation.enums.DonationStatus;
import com.medicinedonation.model.CollectionPoint;
import com.medicinedonation.model.Donation;
import com.medicinedonation.model.Medicine;
import com.medicinedonation.model.PendingMedicine;
import com.medicinedonation.model.User;
import com.medicinedonation.repository.CollectionPointRepository;
import com.medicinedonation.repository.DonationRepository;
import com.medicinedonation.repository.MedicineRepository;
import com.medicinedonation.repository.PendingMedicineRepository;
import com.medicinedonation.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Collectors;

@Service
public class DonorService {

    @Autowired
    private DonationRepository donationRepository;

    @Autowired
    private CollectionPointRepository collectionPointRepository;

    @Autowired
    private PendingMedicineRepository pendingMedicineRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MedicineService medicineService;

    @Autowired
    private MedicineRepository medicineRepository;

    // ─────────────────────────────────────────
    // SUBMIT DONATION
    // ─────────────────────────────────────────

    public DonationResponse submitDonation(
            DonationRequest request, String donorEmail) {

        User donor = userRepository.findByEmail(donorEmail)
                .orElseThrow(() -> new RuntimeException("Donor not found"));

        LocalDate minExpiry = LocalDate.now().plusMonths(6);
        if (request.getExpiryDate().isBefore(minExpiry)) {
            throw new RuntimeException(
                    "Medicine must have more than 6 months before expiry. " +
                            "Expiry date must be after: " + minExpiry);
        }

        CollectionPoint collectionPoint =
                collectionPointRepository
                        .findById(request.getCollectionPointId())
                        .orElseThrow(() -> new RuntimeException(
                                "Collection point not found"));

        Optional<Medicine> medicineOpt =
                medicineService.findByBrandName(
                        request.getBrandNameSubmitted());

        Donation donation = Donation.builder()
                .donor(donor)
                .brandNameSubmitted(request.getBrandNameSubmitted())
                .quantity(request.getQuantity())
                .expiryDate(request.getExpiryDate())
                .photoUrl(request.getPhotoUrl())
                .packageProofUrl(request.getPackageProofUrl())
                .dosageForm(request.getDosageForm())
                .strength(request.getStrength())
                .collectionPoint(collectionPoint)
                .status(DonationStatus.PENDING_DOCTOR)
                .build();

        if (medicineOpt.isPresent()) {
            donation.setMedicine(medicineOpt.get());
        }

        Donation saved = donationRepository.save(donation);
        return mapToDonationResponse(saved);
    }

    // ─────────────────────────────────────────
    // GET MY DONATIONS
    // ─────────────────────────────────────────

    public List<DonationResponse> getMyDonations(String donorEmail) {
        User donor = userRepository.findByEmail(donorEmail)
                .orElseThrow(() -> new RuntimeException("Donor not found"));

        return donationRepository.findByDonor(donor)
                .stream()
                .map(this::mapToDonationResponse)
                .collect(Collectors.toList());
    }

    // ─────────────────────────────────────────
    // GET DONATION BY ID
    // ─────────────────────────────────────────

    public DonationResponse getDonationById(Long id, String donorEmail) {
        Donation donation = donationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(
                        "Donation not found with id: " + id));

        if (!donation.getDonor().getEmail().equals(donorEmail)) {
            throw new RuntimeException(
                    "You are not authorized to view this donation");
        }

        return mapToDonationResponse(donation);
    }

    // ─────────────────────────────────────────
    // GET ACTIVE COLLECTION POINTS
    // ─────────────────────────────────────────

    public List<CollectionPointResponse> getActiveCollectionPoints() {
        return collectionPointRepository.findByActive(true)
                .stream()
                .map(this::mapToCollectionPointResponse)
                .collect(Collectors.toList());
    }

    // ─────────────────────────────────────────
    // GET DONATION COUNTS
    // ─────────────────────────────────────────

    public java.util.Map<String, Long> getDonationCounts(String donorEmail) {

        User donor = userRepository.findByEmail(donorEmail)
                .orElseThrow(() -> new RuntimeException("Donor not found"));

        java.util.Map<String, Long> counts = new java.util.HashMap<>();

        counts.put("totalDonations",
                donationRepository.countByDonor(donor));
        counts.put("pendingDoctor",
                donationRepository.countByDonorAndStatus(
                        donor, DonationStatus.PENDING_DOCTOR));
        counts.put("pendingPharmacist",
                donationRepository.countByDonorAndStatus(
                        donor, DonationStatus.PENDING_PHARMACIST));
        counts.put("pendingDoctorRecheck",
                donationRepository.countByDonorAndStatus(
                        donor, DonationStatus.PENDING_DOCTOR_RECHECK));
        counts.put("pharmacistRejected",
                donationRepository.countByDonorAndStatus(
                        donor, DonationStatus.PHARMACIST_REJECTED));
        counts.put("doctorApproved",
                donationRepository.countByDonorAndStatus(
                        donor, DonationStatus.DOCTOR_APPROVED));
        counts.put("rejectedByDoctor",
                donationRepository.countByDonorAndStatus(
                        donor, DonationStatus.REJECTED_BY_DOCTOR));
        counts.put("pendingCustody",
                donationRepository.countByDonorAndStatus(
                        donor, DonationStatus.PENDING_CUSTODY));
        counts.put("live",
                donationRepository.countByDonorAndStatus(
                        donor, DonationStatus.LIVE));
        counts.put("collected",
                donationRepository.countByDonorAndStatus(
                        donor, DonationStatus.COLLECTED));

        return counts;
    }

    // ─────────────────────────────────────────
    // COMPUTE DB MATCH STATUS
    //
    // FULL_MATCH    — brand + strength + dosageForm all match
    // PARTIAL_MATCH — brand found but strength or dosageForm differ
    // NO_MATCH      — brand not found at all
    // ─────────────────────────────────────────

    private String computeDbMatchStatus(Donation donation) {

        // If medicine already linked by pharmacist → check corrected or donor details
        if (donation.getMedicine() != null) {
            String dbStrength   = donation.getMedicine().getStrength();
            String dbDosageForm = donation.getMedicine().getDosageForm();

            // ✅ Use doctor's corrected values if available, else donor's original
            String checkStrength = (donation.getDoctorCorrectedStrength() != null &&
                    !donation.getDoctorCorrectedStrength().isBlank())
                    ? donation.getDoctorCorrectedStrength()
                    : donation.getStrength();
            String checkDosageForm = (donation.getDoctorCorrectedDosageForm() != null &&
                    !donation.getDoctorCorrectedDosageForm().isBlank())
                    ? donation.getDoctorCorrectedDosageForm()
                    : donation.getDosageForm();

            boolean strengthMatch = dbStrength != null && checkStrength != null &&
                    dbStrength.replaceAll("\\s+", "").equalsIgnoreCase(
                            checkStrength.trim().replaceAll("\\s+", ""));
            boolean dosageFormMatch = dbDosageForm != null && checkDosageForm != null &&
                    dbDosageForm.equalsIgnoreCase(checkDosageForm.trim());

            return (strengthMatch && dosageFormMatch) ? "FULL_MATCH" : "PARTIAL_MATCH";
        }

        // ✅ Use doctor's corrected brand name if available, else donor's original
        String brandName = (donation.getDoctorCorrectedBrandName() != null &&
                !donation.getDoctorCorrectedBrandName().isBlank())
                ? donation.getDoctorCorrectedBrandName()
                : donation.getBrandNameSubmitted();

        if (brandName == null || brandName.isBlank()) {
            return "NO_MATCH";
        }

        // ✅ FIXED — List instead of Optional, safe for multiple entries (e.g. Panadol 500mg + Panadol Extra)
        List<Medicine> byBrand =
                medicineRepository.findByBrandNameIgnoreCase(brandName.trim());

        if (byBrand.isEmpty()) {
            return "NO_MATCH";
        }

        String checkStrength = (donation.getDoctorCorrectedStrength() != null &&
                !donation.getDoctorCorrectedStrength().isBlank())
                ? donation.getDoctorCorrectedStrength()
                : donation.getStrength();
        String checkDosageForm = (donation.getDoctorCorrectedDosageForm() != null &&
                !donation.getDoctorCorrectedDosageForm().isBlank())
                ? donation.getDoctorCorrectedDosageForm()
                : donation.getDosageForm();

        // ✅ Check if ANY entry with this brand has matching strength + dosageForm
        // ✅ Normalize spaces e.g. "500mg" == "500 mg"
        boolean exactMatch = byBrand.stream().anyMatch(m -> {
            boolean strengthMatch = m.getStrength() != null && checkStrength != null &&
                    m.getStrength().replaceAll("\\s+", "").equalsIgnoreCase(
                            checkStrength.trim().replaceAll("\\s+", ""));
            boolean dosageFormMatch = m.getDosageForm() != null && checkDosageForm != null &&
                    m.getDosageForm().equalsIgnoreCase(checkDosageForm.trim());
            return strengthMatch && dosageFormMatch;
        });

        return exactMatch ? "FULL_MATCH" : "PARTIAL_MATCH";
    }

    // ─────────────────────────────────────────
    // MAPPER
    // ─────────────────────────────────────────

    public DonationResponse mapToDonationResponse(Donation donation) {
        return DonationResponse.builder()
                .id(donation.getId())
                .donorName(donation.getDonor().getName())
                .donorEmail(donation.getDonor().getEmail())
                .brandNameSubmitted(donation.getBrandNameSubmitted())
                .medicineId(donation.getMedicine() != null ?
                        donation.getMedicine().getId() : null)
                .medicineApiName(donation.getMedicine() != null ?
                        donation.getMedicine().getApiName() : null)
                .medicineStrength(donation.getMedicine() != null ?
                        donation.getMedicine().getStrength() : null)
                .medicineDosageForm(donation.getMedicine() != null ?
                        donation.getMedicine().getDosageForm() : null)
                .medicineSchedule(donation.getMedicine() != null ?
                        donation.getMedicine().getSchedule().name() : null)
                .dosageForm(donation.getDosageForm())
                .strength(donation.getStrength())
                // ✅ Doctor correction fields
                .doctorCorrectedBrandName(donation.getDoctorCorrectedBrandName())
                .doctorCorrectedStrength(donation.getDoctorCorrectedStrength())
                .doctorCorrectedDosageForm(donation.getDoctorCorrectedDosageForm())
                .doctorCorrectedQuantity(donation.getDoctorCorrectedQuantity())
                .doctorCorrectedExpiryDate(donation.getDoctorCorrectedExpiryDate())
                // ✅ DB match status
                .dbMatchStatus(computeDbMatchStatus(donation))
                // ✅ All DB records for the brand — shows all variants (e.g. Panadol 500mg + Panadol 250mg)
                .dbMatchingRecords(buildDbMatchingRecords(donation))
                .quantity(donation.getQuantity())
                .expiryDate(donation.getExpiryDate())
                .photoUrl(donation.getPhotoUrl())
                .packageProofUrl(donation.getPackageProofUrl())
                .status(donation.getStatus().name())
                .doctorName(donation.getApprovedByDoctor() != null ?
                        donation.getApprovedByDoctor().getName() : null)
                .doctorNotes(donation.getDoctorNotes())
                .rejectionReason(donation.getRejectionReason())
                .pharmacistRejectionReason(
                        donation.getPharmacistRejectionReason())
                .collectionPointId(donation.getCollectionPoint() != null ?
                        donation.getCollectionPoint().getId() : null)
                .collectionPointName(donation.getCollectionPoint() != null ?
                        donation.getCollectionPoint().getLocationName() : null)
                .collectionPointAddress(donation.getCollectionPoint() != null ?
                        donation.getCollectionPoint().getAddress() : null)
                .recipientName(donation.getRecipient() != null ?
                        donation.getRecipient().getName() : null)
                .createdAt(donation.getCreatedAt())
                .updatedAt(donation.getUpdatedAt())
                .build();
    }

    // ─────────────────────────────────────────
    // BUILD DB MATCHING RECORDS
    // Returns ALL DB entries for the brand name
    // e.g. Panadol 500mg Tablet + Panadol 250mg Tablet
    // ─────────────────────────────────────────

    private List<DonationResponse.DbMedicineRecord> buildDbMatchingRecords(Donation donation) {
        // Determine brand name to look up
        String brandName = (donation.getDoctorCorrectedBrandName() != null &&
                !donation.getDoctorCorrectedBrandName().isBlank())
                ? donation.getDoctorCorrectedBrandName()
                : donation.getBrandNameSubmitted();

        if (brandName == null || brandName.isBlank()) {
            return Collections.emptyList();
        }

        List<Medicine> matches =
                medicineRepository.findByBrandNameIgnoreCase(brandName.trim());

        return matches.stream()
                .map(m -> DonationResponse.DbMedicineRecord.builder()
                        .id(m.getId())
                        .apiName(m.getApiName())
                        .strength(m.getStrength())
                        .dosageForm(m.getDosageForm())
                        .route(m.getRoute())
                        .schedule(m.getSchedule() != null ? m.getSchedule().name() : null)
                        .pharmacistVerified(m.isPharmacistVerified())
                        .build())
                .collect(Collectors.toList());
    }

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

import com.medicinedonation.dto.request.DonationRequest;
import com.medicinedonation.dto.response.CollectionPointResponse;
import com.medicinedonation.dto.response.DonationResponse;
import com.medicinedonation.enums.DonationStatus;
import com.medicinedonation.model.CollectionPoint;
import com.medicinedonation.model.Donation;
import com.medicinedonation.model.Medicine;
import com.medicinedonation.model.PendingMedicine;
import com.medicinedonation.model.User;
import com.medicinedonation.repository.CollectionPointRepository;
import com.medicinedonation.repository.DonationRepository;
import com.medicinedonation.repository.MedicineRepository;
import com.medicinedonation.repository.PendingMedicineRepository;
import com.medicinedonation.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Collectors;

@Service
public class DonorService {

    @Autowired
    private DonationRepository donationRepository;

    @Autowired
    private CollectionPointRepository collectionPointRepository;

    @Autowired
    private PendingMedicineRepository pendingMedicineRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MedicineService medicineService;

    @Autowired
    private MedicineRepository medicineRepository;

    // ─────────────────────────────────────────
    // SUBMIT DONATION
    // ─────────────────────────────────────────

    public DonationResponse submitDonation(
            DonationRequest request, String donorEmail) {

        User donor = userRepository.findByEmail(donorEmail)
                .orElseThrow(() -> new RuntimeException("Donor not found"));

        LocalDate minExpiry = LocalDate.now().plusMonths(6);
        if (request.getExpiryDate().isBefore(minExpiry)) {
            throw new RuntimeException(
                    "Medicine must have more than 6 months before expiry. " +
                            "Expiry date must be after: " + minExpiry);
        }

        CollectionPoint collectionPoint =
                collectionPointRepository
                        .findById(request.getCollectionPointId())
                        .orElseThrow(() -> new RuntimeException(
                                "Collection point not found"));

        Optional<Medicine> medicineOpt =
                medicineService.findByBrandName(
                        request.getBrandNameSubmitted());

        Donation donation = Donation.builder()
                .donor(donor)
                .brandNameSubmitted(request.getBrandNameSubmitted())
                .quantity(request.getQuantity())
                .expiryDate(request.getExpiryDate())
                .photoUrl(request.getPhotoUrl())
                .packageProofUrl(request.getPackageProofUrl())
                .dosageForm(request.getDosageForm())
                .strength(request.getStrength())
                .collectionPoint(collectionPoint)
                .status(DonationStatus.PENDING_DOCTOR)
                .build();

        if (medicineOpt.isPresent()) {
            donation.setMedicine(medicineOpt.get());
        }

        Donation saved = donationRepository.save(donation);
        return mapToDonationResponse(saved);
    }

    // ─────────────────────────────────────────
    // GET MY DONATIONS
    // ─────────────────────────────────────────

    public List<DonationResponse> getMyDonations(String donorEmail) {
        User donor = userRepository.findByEmail(donorEmail)
                .orElseThrow(() -> new RuntimeException("Donor not found"));

        return donationRepository.findByDonor(donor)
                .stream()
                .map(this::mapToDonationResponse)
                .collect(Collectors.toList());
    }

    // ─────────────────────────────────────────
    // GET DONATION BY ID
    // ─────────────────────────────────────────

    public DonationResponse getDonationById(Long id, String donorEmail) {
        Donation donation = donationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(
                        "Donation not found with id: " + id));

        if (!donation.getDonor().getEmail().equals(donorEmail)) {
            throw new RuntimeException(
                    "You are not authorized to view this donation");
        }

        return mapToDonationResponse(donation);
    }

    // ─────────────────────────────────────────
    // GET ACTIVE COLLECTION POINTS
    // ─────────────────────────────────────────

    public List<CollectionPointResponse> getActiveCollectionPoints() {
        return collectionPointRepository.findByActive(true)
                .stream()
                .map(this::mapToCollectionPointResponse)
                .collect(Collectors.toList());
    }

    // ─────────────────────────────────────────
    // GET DONATION COUNTS
    // ─────────────────────────────────────────

    public java.util.Map<String, Long> getDonationCounts(String donorEmail) {

        User donor = userRepository.findByEmail(donorEmail)
                .orElseThrow(() -> new RuntimeException("Donor not found"));

        java.util.Map<String, Long> counts = new java.util.HashMap<>();

        counts.put("totalDonations",
                donationRepository.countByDonor(donor));
        counts.put("pendingDoctor",
                donationRepository.countByDonorAndStatus(
                        donor, DonationStatus.PENDING_DOCTOR));
        counts.put("pendingPharmacist",
                donationRepository.countByDonorAndStatus(
                        donor, DonationStatus.PENDING_PHARMACIST));
        counts.put("pendingDoctorRecheck",
                donationRepository.countByDonorAndStatus(
                        donor, DonationStatus.PENDING_DOCTOR_RECHECK));
        counts.put("pharmacistRejected",
                donationRepository.countByDonorAndStatus(
                        donor, DonationStatus.PHARMACIST_REJECTED));
        counts.put("doctorApproved",
                donationRepository.countByDonorAndStatus(
                        donor, DonationStatus.DOCTOR_APPROVED));
        counts.put("rejectedByDoctor",
                donationRepository.countByDonorAndStatus(
                        donor, DonationStatus.REJECTED_BY_DOCTOR));
        counts.put("pendingCustody",
                donationRepository.countByDonorAndStatus(
                        donor, DonationStatus.PENDING_CUSTODY));
        counts.put("live",
                donationRepository.countByDonorAndStatus(
                        donor, DonationStatus.LIVE));
        counts.put("collected",
                donationRepository.countByDonorAndStatus(
                        donor, DonationStatus.COLLECTED));

        return counts;
    }

    // ─────────────────────────────────────────
    // COMPUTE DB MATCH STATUS
    //
    // FULL_MATCH    — brand + strength + dosageForm all match
    // PARTIAL_MATCH — brand found but strength or dosageForm differ
    // NO_MATCH      — brand not found at all
    // ─────────────────────────────────────────

    private String computeDbMatchStatus(Donation donation) {

        // Determine what the doctor wants to match against
        String checkStrength = (donation.getDoctorCorrectedStrength() != null &&
                !donation.getDoctorCorrectedStrength().isBlank())
                ? donation.getDoctorCorrectedStrength()
                : donation.getStrength();
        String checkDosageForm = (donation.getDoctorCorrectedDosageForm() != null &&
                !donation.getDoctorCorrectedDosageForm().isBlank())
                ? donation.getDoctorCorrectedDosageForm()
                : donation.getDosageForm();

        // If medicine already linked — check if linked medicine matches
        // But ALSO check all other DB records for same brand (e.g. Panadol 500mg vs Panadol 1mg)
        if (donation.getMedicine() != null) {
            // First check the linked medicine
            String dbStrength   = donation.getMedicine().getStrength();
            String dbDosageForm = donation.getMedicine().getDosageForm();

            boolean linkedStrengthMatch = dbStrength != null && checkStrength != null &&
                    dbStrength.replaceAll("\\s+", "").equalsIgnoreCase(
                            checkStrength.trim().replaceAll("\\s+", ""));
            boolean linkedDosageFormMatch = dbDosageForm != null && checkDosageForm != null &&
                    dbDosageForm.equalsIgnoreCase(checkDosageForm.trim());

            if (linkedStrengthMatch && linkedDosageFormMatch) {
                return "FULL_MATCH";
            }

            // Linked medicine doesn't match — check ALL records for this brand
            // e.g. donor submitted Panadol 1mg, linked was Panadol 500mg,
            // but Panadol 1mg also exists in DB → should be FULL_MATCH
            String brandName = (donation.getDoctorCorrectedBrandName() != null &&
                    !donation.getDoctorCorrectedBrandName().isBlank())
                    ? donation.getDoctorCorrectedBrandName()
                    : donation.getBrandNameSubmitted();

            if (brandName != null && !brandName.isBlank()) {
                List<Medicine> allByBrand =
                        medicineRepository.findByBrandNameIgnoreCase(brandName.trim());
                boolean anyMatch = allByBrand.stream().anyMatch(m -> {
                    boolean sm = m.getStrength() != null && checkStrength != null &&
                            m.getStrength().replaceAll("\\s+", "").equalsIgnoreCase(
                                    checkStrength.trim().replaceAll("\\s+", ""));
                    boolean dm = m.getDosageForm() != null && checkDosageForm != null &&
                            m.getDosageForm().equalsIgnoreCase(checkDosageForm.trim());
                    return sm && dm;
                });
                if (anyMatch) return "FULL_MATCH";
            }

            return "PARTIAL_MATCH";
        }

        // ✅ Use doctor's corrected brand name if available, else donor's original
        String brandName = (donation.getDoctorCorrectedBrandName() != null &&
                !donation.getDoctorCorrectedBrandName().isBlank())
                ? donation.getDoctorCorrectedBrandName()
                : donation.getBrandNameSubmitted();

        if (brandName == null || brandName.isBlank()) {
            return "NO_MATCH";
        }

        // ✅ FIXED — List instead of Optional, safe for multiple entries (e.g. Panadol 500mg + Panadol Extra)
        List<Medicine> byBrand =
                medicineRepository.findByBrandNameIgnoreCase(brandName.trim());

        if (byBrand.isEmpty()) {
            return "NO_MATCH";
        }

        // ✅ Check if ANY entry with this brand has matching strength + dosageForm
        // ✅ Normalize spaces e.g. "500mg" == "500 mg"
        boolean exactMatch = byBrand.stream().anyMatch(m -> {
            boolean strengthMatch = m.getStrength() != null && checkStrength != null &&
                    m.getStrength().replaceAll("\\s+", "").equalsIgnoreCase(
                            checkStrength.trim().replaceAll("\\s+", ""));
            boolean dosageFormMatch = m.getDosageForm() != null && checkDosageForm != null &&
                    m.getDosageForm().equalsIgnoreCase(checkDosageForm.trim());
            return strengthMatch && dosageFormMatch;
        });

        return exactMatch ? "FULL_MATCH" : "PARTIAL_MATCH";
    }

    // ─────────────────────────────────────────
    // MAPPER
    // ─────────────────────────────────────────

    public DonationResponse mapToDonationResponse(Donation donation) {
        return DonationResponse.builder()
                .id(donation.getId())
                .donorName(donation.getDonor().getName())
                .donorEmail(donation.getDonor().getEmail())
                .brandNameSubmitted(donation.getBrandNameSubmitted())
                .medicineId(donation.getMedicine() != null ?
                        donation.getMedicine().getId() : null)
                .medicineApiName(donation.getMedicine() != null ?
                        donation.getMedicine().getApiName() : null)
                .medicineStrength(donation.getMedicine() != null ?
                        donation.getMedicine().getStrength() : null)
                .medicineDosageForm(donation.getMedicine() != null ?
                        donation.getMedicine().getDosageForm() : null)
                .medicineSchedule(donation.getMedicine() != null ?
                        donation.getMedicine().getSchedule().name() : null)
                .dosageForm(donation.getDosageForm())
                .strength(donation.getStrength())
                // ✅ Doctor correction fields
                .doctorCorrectedBrandName(donation.getDoctorCorrectedBrandName())
                .doctorCorrectedStrength(donation.getDoctorCorrectedStrength())
                .doctorCorrectedDosageForm(donation.getDoctorCorrectedDosageForm())
                .doctorCorrectedQuantity(donation.getDoctorCorrectedQuantity())
                .doctorCorrectedExpiryDate(donation.getDoctorCorrectedExpiryDate())
                // ✅ DB match status
                .dbMatchStatus(computeDbMatchStatus(donation))
                // ✅ All DB records for the brand — shows all variants (e.g. Panadol 500mg + Panadol 250mg)
                .dbMatchingRecords(buildDbMatchingRecords(donation))
                .quantity(donation.getQuantity())
                .expiryDate(donation.getExpiryDate())
                .photoUrl(donation.getPhotoUrl())
                .packageProofUrl(donation.getPackageProofUrl())
                .status(donation.getStatus().name())
                .doctorName(donation.getApprovedByDoctor() != null ?
                        donation.getApprovedByDoctor().getName() : null)
                .doctorNotes(donation.getDoctorNotes())
                .rejectionReason(donation.getRejectionReason())
                .pharmacistRejectionReason(
                        donation.getPharmacistRejectionReason())
                .collectionPointId(donation.getCollectionPoint() != null ?
                        donation.getCollectionPoint().getId() : null)
                .collectionPointName(donation.getCollectionPoint() != null ?
                        donation.getCollectionPoint().getLocationName() : null)
                .collectionPointAddress(donation.getCollectionPoint() != null ?
                        donation.getCollectionPoint().getAddress() : null)
                .recipientName(donation.getRecipient() != null ?
                        donation.getRecipient().getName() : null)
                .createdAt(donation.getCreatedAt())
                .updatedAt(donation.getUpdatedAt())
                .build();
    }

    // ─────────────────────────────────────────
    // BUILD DB MATCHING RECORDS
    // Returns ALL DB entries for the brand name
    // e.g. Panadol 500mg Tablet + Panadol 250mg Tablet
    // ─────────────────────────────────────────

    private List<DonationResponse.DbMedicineRecord> buildDbMatchingRecords(Donation donation) {
        // Determine brand name to look up
        String brandName = (donation.getDoctorCorrectedBrandName() != null &&
                !donation.getDoctorCorrectedBrandName().isBlank())
                ? donation.getDoctorCorrectedBrandName()
                : donation.getBrandNameSubmitted();

        if (brandName == null || brandName.isBlank()) {
            return Collections.emptyList();
        }

        List<Medicine> matches =
                medicineRepository.findByBrandNameIgnoreCase(brandName.trim());

        return matches.stream()
                .map(m -> DonationResponse.DbMedicineRecord.builder()
                        .id(m.getId())
                        .apiName(m.getApiName())
                        .strength(m.getStrength())
                        .dosageForm(m.getDosageForm())
                        .route(m.getRoute())
                        .schedule(m.getSchedule() != null ? m.getSchedule().name() : null)
                        .pharmacistVerified(m.isPharmacistVerified())
                        .build())
                .collect(Collectors.toList());
    }

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

import com.medicinedonation.dto.request.DonationRequest;
import com.medicinedonation.dto.response.CollectionPointResponse;
import com.medicinedonation.dto.response.DonationResponse;
import com.medicinedonation.enums.DonationStatus;
import com.medicinedonation.model.CollectionPoint;
import com.medicinedonation.model.Donation;
import com.medicinedonation.model.Medicine;
import com.medicinedonation.model.PendingMedicine;
import com.medicinedonation.model.User;
import com.medicinedonation.repository.CollectionPointRepository;
import com.medicinedonation.repository.DonationRepository;
import com.medicinedonation.repository.MedicineRepository;
import com.medicinedonation.repository.PendingMedicineRepository;
import com.medicinedonation.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Collectors;

@Service
public class DonorService {

    @Autowired
    private DonationRepository donationRepository;

    @Autowired
    private CollectionPointRepository collectionPointRepository;

    @Autowired
    private PendingMedicineRepository pendingMedicineRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MedicineService medicineService;

    @Autowired
    private MedicineRepository medicineRepository;

    // ─────────────────────────────────────────
    // SUBMIT DONATION
    // ─────────────────────────────────────────

    public DonationResponse submitDonation(
            DonationRequest request, String donorEmail) {

        User donor = userRepository.findByEmail(donorEmail)
                .orElseThrow(() -> new RuntimeException("Donor not found"));

        LocalDate minExpiry = LocalDate.now().plusMonths(6);
        if (request.getExpiryDate().isBefore(minExpiry)) {
            throw new RuntimeException(
                    "Medicine must have more than 6 months before expiry. " +
                            "Expiry date must be after: " + minExpiry);
        }

        CollectionPoint collectionPoint =
                collectionPointRepository
                        .findById(request.getCollectionPointId())
                        .orElseThrow(() -> new RuntimeException(
                                "Collection point not found"));

        Optional<Medicine> medicineOpt =
                medicineService.findByBrandName(
                        request.getBrandNameSubmitted());

        Donation donation = Donation.builder()
                .donor(donor)
                .brandNameSubmitted(request.getBrandNameSubmitted())
                .quantity(request.getQuantity())
                .expiryDate(request.getExpiryDate())
                .photoUrl(request.getPhotoUrl())
                .packageProofUrl(request.getPackageProofUrl())
                .dosageForm(request.getDosageForm())
                .strength(request.getStrength())
                .collectionPoint(collectionPoint)
                .status(DonationStatus.PENDING_DOCTOR)
                .build();

        if (medicineOpt.isPresent()) {
            donation.setMedicine(medicineOpt.get());
        }

        Donation saved = donationRepository.save(donation);
        return mapToDonationResponse(saved);
    }

    // ─────────────────────────────────────────
    // GET MY DONATIONS
    // ─────────────────────────────────────────

    public List<DonationResponse> getMyDonations(String donorEmail) {
        User donor = userRepository.findByEmail(donorEmail)
                .orElseThrow(() -> new RuntimeException("Donor not found"));

        return donationRepository.findByDonor(donor)
                .stream()
                .map(this::mapToDonationResponse)
                .collect(Collectors.toList());
    }

    // ─────────────────────────────────────────
    // GET DONATION BY ID
    // ─────────────────────────────────────────

    public DonationResponse getDonationById(Long id, String donorEmail) {
        Donation donation = donationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(
                        "Donation not found with id: " + id));

        if (!donation.getDonor().getEmail().equals(donorEmail)) {
            throw new RuntimeException(
                    "You are not authorized to view this donation");
        }

        return mapToDonationResponse(donation);
    }

    // ─────────────────────────────────────────
    // GET ACTIVE COLLECTION POINTS
    // ─────────────────────────────────────────

    public List<CollectionPointResponse> getActiveCollectionPoints() {
        return collectionPointRepository.findByActive(true)
                .stream()
                .map(this::mapToCollectionPointResponse)
                .collect(Collectors.toList());
    }

    // ─────────────────────────────────────────
    // GET DONATION COUNTS
    // ─────────────────────────────────────────

    public java.util.Map<String, Long> getDonationCounts(String donorEmail) {

        User donor = userRepository.findByEmail(donorEmail)
                .orElseThrow(() -> new RuntimeException("Donor not found"));

        java.util.Map<String, Long> counts = new java.util.HashMap<>();

        counts.put("totalDonations",
                donationRepository.countByDonor(donor));
        counts.put("pendingDoctor",
                donationRepository.countByDonorAndStatus(
                        donor, DonationStatus.PENDING_DOCTOR));
        counts.put("pendingPharmacist",
                donationRepository.countByDonorAndStatus(
                        donor, DonationStatus.PENDING_PHARMACIST));
        counts.put("pendingDoctorRecheck",
                donationRepository.countByDonorAndStatus(
                        donor, DonationStatus.PENDING_DOCTOR_RECHECK));
        counts.put("pharmacistRejected",
                donationRepository.countByDonorAndStatus(
                        donor, DonationStatus.PHARMACIST_REJECTED));
        counts.put("doctorApproved",
                donationRepository.countByDonorAndStatus(
                        donor, DonationStatus.DOCTOR_APPROVED));
        counts.put("rejectedByDoctor",
                donationRepository.countByDonorAndStatus(
                        donor, DonationStatus.REJECTED_BY_DOCTOR));
        counts.put("pendingCustody",
                donationRepository.countByDonorAndStatus(
                        donor, DonationStatus.PENDING_CUSTODY));
        counts.put("live",
                donationRepository.countByDonorAndStatus(
                        donor, DonationStatus.LIVE));
        counts.put("collected",
                donationRepository.countByDonorAndStatus(
                        donor, DonationStatus.COLLECTED));

        return counts;
    }

    // ─────────────────────────────────────────
    // COMPUTE DB MATCH STATUS
    //
    // FULL_MATCH    — brand + strength + dosageForm all match
    // PARTIAL_MATCH — brand found but strength or dosageForm differ
    // NO_MATCH      — brand not found at all
    // ─────────────────────────────────────────

    private String computeDbMatchStatus(Donation donation) {

        // Determine what the doctor wants to match against
        String checkStrength = (donation.getDoctorCorrectedStrength() != null &&
                !donation.getDoctorCorrectedStrength().isBlank())
                ? donation.getDoctorCorrectedStrength()
                : donation.getStrength();
        String checkDosageForm = (donation.getDoctorCorrectedDosageForm() != null &&
                !donation.getDoctorCorrectedDosageForm().isBlank())
                ? donation.getDoctorCorrectedDosageForm()
                : donation.getDosageForm();

        // If medicine already linked — check if linked medicine matches
        // But ALSO check all other DB records for same brand (e.g. Panadol 500mg vs Panadol 1mg)
        if (donation.getMedicine() != null) {
            // First check the linked medicine
            String dbStrength   = donation.getMedicine().getStrength();
            String dbDosageForm = donation.getMedicine().getDosageForm();

            boolean linkedStrengthMatch = dbStrength != null && checkStrength != null &&
                    dbStrength.replaceAll("\\s+", "").equalsIgnoreCase(
                            checkStrength.trim().replaceAll("\\s+", ""));
            boolean linkedDosageFormMatch = dbDosageForm != null && checkDosageForm != null &&
                    dbDosageForm.equalsIgnoreCase(checkDosageForm.trim());

            if (linkedStrengthMatch && linkedDosageFormMatch) {
                return "FULL_MATCH";
            }

            // Linked medicine doesn't match — check ALL records for this brand
            // e.g. donor submitted Panadol 1mg, linked was Panadol 500mg,
            // but Panadol 1mg also exists in DB → should be FULL_MATCH
            String brandName = (donation.getDoctorCorrectedBrandName() != null &&
                    !donation.getDoctorCorrectedBrandName().isBlank())
                    ? donation.getDoctorCorrectedBrandName()
                    : donation.getBrandNameSubmitted();

            if (brandName != null && !brandName.isBlank()) {
                List<Medicine> allByBrand =
                        medicineRepository.findByBrandNameIgnoreCase(brandName.trim());
                boolean anyMatch = allByBrand.stream().anyMatch(m -> {
                    boolean sm = m.getStrength() != null && checkStrength != null &&
                            m.getStrength().replaceAll("\\s+", "").equalsIgnoreCase(
                                    checkStrength.trim().replaceAll("\\s+", ""));
                    boolean dm = m.getDosageForm() != null && checkDosageForm != null &&
                            m.getDosageForm().equalsIgnoreCase(checkDosageForm.trim());
                    return sm && dm;
                });
                if (anyMatch) return "FULL_MATCH";
            }

            return "PARTIAL_MATCH";
        }

        // ✅ Use doctor's corrected brand name if available, else donor's original
        String brandName = (donation.getDoctorCorrectedBrandName() != null &&
                !donation.getDoctorCorrectedBrandName().isBlank())
                ? donation.getDoctorCorrectedBrandName()
                : donation.getBrandNameSubmitted();

        if (brandName == null || brandName.isBlank()) {
            return "NO_MATCH";
        }

        // ✅ FIXED — List instead of Optional, safe for multiple entries (e.g. Panadol 500mg + Panadol Extra)
        List<Medicine> byBrand =
                medicineRepository.findByBrandNameIgnoreCase(brandName.trim());

        if (byBrand.isEmpty()) {
            return "NO_MATCH";
        }

        // ✅ Check if ANY entry with this brand has matching strength + dosageForm
        // ✅ Normalize spaces e.g. "500mg" == "500 mg"
        boolean exactMatch = byBrand.stream().anyMatch(m -> {
            boolean strengthMatch = m.getStrength() != null && checkStrength != null &&
                    m.getStrength().replaceAll("\\s+", "").equalsIgnoreCase(
                            checkStrength.trim().replaceAll("\\s+", ""));
            boolean dosageFormMatch = m.getDosageForm() != null && checkDosageForm != null &&
                    m.getDosageForm().equalsIgnoreCase(checkDosageForm.trim());
            return strengthMatch && dosageFormMatch;
        });

        return exactMatch ? "FULL_MATCH" : "PARTIAL_MATCH";
    }

    // ─────────────────────────────────────────
    // MAPPER
    // ─────────────────────────────────────────

    public DonationResponse mapToDonationResponse(Donation donation) {
        return DonationResponse.builder()
                .id(donation.getId())
                .donorName(donation.getDonor().getName())
                .donorEmail(donation.getDonor().getEmail())
                .brandNameSubmitted(donation.getBrandNameSubmitted())
                .medicineId(donation.getMedicine() != null ?
                        donation.getMedicine().getId() : null)
                .medicineApiName(donation.getMedicine() != null ?
                        donation.getMedicine().getApiName() : null)
                .medicineStrength(donation.getMedicine() != null ?
                        donation.getMedicine().getStrength() : null)
                .medicineDosageForm(donation.getMedicine() != null ?
                        donation.getMedicine().getDosageForm() : null)
                .medicineSchedule(donation.getMedicine() != null ?
                        donation.getMedicine().getSchedule().name() : null)
                .dosageForm(donation.getDosageForm())
                .strength(donation.getStrength())
                // ✅ Doctor correction fields
                .doctorCorrectedBrandName(donation.getDoctorCorrectedBrandName())
                .doctorCorrectedStrength(donation.getDoctorCorrectedStrength())
                .doctorCorrectedDosageForm(donation.getDoctorCorrectedDosageForm())
                .doctorCorrectedQuantity(donation.getDoctorCorrectedQuantity())
                .doctorCorrectedExpiryDate(donation.getDoctorCorrectedExpiryDate())
                // ✅ DB match status
                .dbMatchStatus(computeDbMatchStatus(donation))
                // ✅ All DB records for the brand — shows all variants (e.g. Panadol 500mg + Panadol 250mg)
                .dbMatchingRecords(buildDbMatchingRecords(donation))
                .quantity(donation.getQuantity())
                .expiryDate(donation.getExpiryDate())
                .photoUrl(donation.getPhotoUrl())
                .packageProofUrl(donation.getPackageProofUrl())
                .status(donation.getStatus().name())
                .doctorName(donation.getApprovedByDoctor() != null ?
                        donation.getApprovedByDoctor().getName() : null)
                .doctorNotes(donation.getDoctorNotes())
                .rejectionReason(donation.getRejectionReason())
                .pharmacistRejectionReason(
                        donation.getPharmacistRejectionReason())
                .collectionPointId(donation.getCollectionPoint() != null ?
                        donation.getCollectionPoint().getId() : null)
                .collectionPointName(donation.getCollectionPoint() != null ?
                        donation.getCollectionPoint().getLocationName() : null)
                .collectionPointAddress(donation.getCollectionPoint() != null ?
                        donation.getCollectionPoint().getAddress() : null)
                .recipientName(donation.getRecipient() != null ?
                        donation.getRecipient().getName() : null)
                .createdAt(donation.getCreatedAt())
                .updatedAt(donation.getUpdatedAt())
                .build();
    }

    // ─────────────────────────────────────────
    // BUILD DB MATCHING RECORDS
    // Returns ALL DB entries for the brand name
    // e.g. Panadol 500mg Tablet + Panadol 250mg Tablet
    // ─────────────────────────────────────────

    private List<DonationResponse.DbMedicineRecord> buildDbMatchingRecords(Donation donation) {
        // Determine brand name to look up
        String brandName = (donation.getDoctorCorrectedBrandName() != null &&
                !donation.getDoctorCorrectedBrandName().isBlank())
                ? donation.getDoctorCorrectedBrandName()
                : donation.getBrandNameSubmitted();

        if (brandName == null || brandName.isBlank()) {
            return Collections.emptyList();
        }

        List<Medicine> matches =
                medicineRepository.findByBrandNameIgnoreCase(brandName.trim());

        return matches.stream()
                .map(m -> DonationResponse.DbMedicineRecord.builder()
                        .id(m.getId())
                        .apiName(m.getApiName())
                        .strength(m.getStrength())
                        .dosageForm(m.getDosageForm())
                        .route(m.getRoute())
                        .schedule(m.getSchedule() != null ? m.getSchedule().name() : null)
                        .pharmacistVerified(m.isPharmacistVerified())
                        .build())
                .collect(Collectors.toList());
    }

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

import com.medicinedonation.dto.request.DonationRequest;
import com.medicinedonation.dto.response.CollectionPointResponse;
import com.medicinedonation.dto.response.DonationResponse;
import com.medicinedonation.enums.DonationStatus;
import com.medicinedonation.model.CollectionPoint;
import com.medicinedonation.model.Donation;
import com.medicinedonation.model.Medicine;
import com.medicinedonation.model.PendingMedicine;
import com.medicinedonation.model.User;
import com.medicinedonation.repository.CollectionPointRepository;
import com.medicinedonation.repository.DonationRepository;
import com.medicinedonation.repository.MedicineRepository;
import com.medicinedonation.repository.PendingMedicineRepository;
import com.medicinedonation.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Collectors;

@Service
public class DonorService {

    @Autowired
    private DonationRepository donationRepository;

    @Autowired
    private CollectionPointRepository collectionPointRepository;

    @Autowired
    private PendingMedicineRepository pendingMedicineRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MedicineService medicineService;

    @Autowired
    private MedicineRepository medicineRepository;

    // ─────────────────────────────────────────
    // SUBMIT DONATION
    // ─────────────────────────────────────────

    public DonationResponse submitDonation(
            DonationRequest request, String donorEmail) {

        User donor = userRepository.findByEmail(donorEmail)
                .orElseThrow(() -> new RuntimeException("Donor not found"));

        LocalDate minExpiry = LocalDate.now().plusMonths(6);
        if (request.getExpiryDate().isBefore(minExpiry)) {
            throw new RuntimeException(
                    "Medicine must have more than 6 months before expiry. " +
                            "Expiry date must be after: " + minExpiry);
        }

        CollectionPoint collectionPoint =
                collectionPointRepository
                        .findById(request.getCollectionPointId())
                        .orElseThrow(() -> new RuntimeException(
                                "Collection point not found"));

        Optional<Medicine> medicineOpt =
                medicineService.findByBrandName(
                        request.getBrandNameSubmitted());

        Donation donation = Donation.builder()
                .donor(donor)
                .brandNameSubmitted(request.getBrandNameSubmitted())
                .quantity(request.getQuantity())
                .expiryDate(request.getExpiryDate())
                .photoUrl(request.getPhotoUrl())
                .packageProofUrl(request.getPackageProofUrl())
                .dosageForm(request.getDosageForm())
                .strength(request.getStrength())
                .collectionPoint(collectionPoint)
                .status(DonationStatus.PENDING_DOCTOR)
                .build();

        if (medicineOpt.isPresent()) {
            donation.setMedicine(medicineOpt.get());
        }

        Donation saved = donationRepository.save(donation);
        return mapToDonationResponse(saved);
    }

    // ─────────────────────────────────────────
    // GET MY DONATIONS
    // ─────────────────────────────────────────

    public List<DonationResponse> getMyDonations(String donorEmail) {
        User donor = userRepository.findByEmail(donorEmail)
                .orElseThrow(() -> new RuntimeException("Donor not found"));

        return donationRepository.findByDonor(donor)
                .stream()
                .map(this::mapToDonationResponse)
                .collect(Collectors.toList());
    }

    // ─────────────────────────────────────────
    // GET DONATION BY ID
    // ─────────────────────────────────────────

    public DonationResponse getDonationById(Long id, String donorEmail) {
        Donation donation = donationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(
                        "Donation not found with id: " + id));

        if (!donation.getDonor().getEmail().equals(donorEmail)) {
            throw new RuntimeException(
                    "You are not authorized to view this donation");
        }

        return mapToDonationResponse(donation);
    }

    // ─────────────────────────────────────────
    // GET ACTIVE COLLECTION POINTS
    // ─────────────────────────────────────────

    public List<CollectionPointResponse> getActiveCollectionPoints() {
        return collectionPointRepository.findByActive(true)
                .stream()
                .map(this::mapToCollectionPointResponse)
                .collect(Collectors.toList());
    }

    // ─────────────────────────────────────────
    // GET DONATION COUNTS
    // ─────────────────────────────────────────

    public java.util.Map<String, Long> getDonationCounts(String donorEmail) {

        User donor = userRepository.findByEmail(donorEmail)
                .orElseThrow(() -> new RuntimeException("Donor not found"));

        java.util.Map<String, Long> counts = new java.util.HashMap<>();

        counts.put("totalDonations",
                donationRepository.countByDonor(donor));
        counts.put("pendingDoctor",
                donationRepository.countByDonorAndStatus(
                        donor, DonationStatus.PENDING_DOCTOR));
        counts.put("pendingPharmacist",
                donationRepository.countByDonorAndStatus(
                        donor, DonationStatus.PENDING_PHARMACIST));
        counts.put("pendingDoctorRecheck",
                donationRepository.countByDonorAndStatus(
                        donor, DonationStatus.PENDING_DOCTOR_RECHECK));
        counts.put("pharmacistRejected",
                donationRepository.countByDonorAndStatus(
                        donor, DonationStatus.PHARMACIST_REJECTED));
        counts.put("doctorApproved",
                donationRepository.countByDonorAndStatus(
                        donor, DonationStatus.DOCTOR_APPROVED));
        counts.put("rejectedByDoctor",
                donationRepository.countByDonorAndStatus(
                        donor, DonationStatus.REJECTED_BY_DOCTOR));
        counts.put("pendingCustody",
                donationRepository.countByDonorAndStatus(
                        donor, DonationStatus.PENDING_CUSTODY));
        counts.put("live",
                donationRepository.countByDonorAndStatus(
                        donor, DonationStatus.LIVE));
        counts.put("collected",
                donationRepository.countByDonorAndStatus(
                        donor, DonationStatus.COLLECTED));

        return counts;
    }

    // ─────────────────────────────────────────
    // COMPUTE DB MATCH STATUS
    //
    // FULL_MATCH    — brand + strength + dosageForm all match
    // PARTIAL_MATCH — brand found but strength or dosageForm differ
    // NO_MATCH      — brand not found at all
    // ─────────────────────────────────────────

    private String computeDbMatchStatus(Donation donation) {

        // Determine what the doctor wants to match against
        String checkStrength = (donation.getDoctorCorrectedStrength() != null &&
                !donation.getDoctorCorrectedStrength().isBlank())
                ? donation.getDoctorCorrectedStrength()
                : donation.getStrength();
        String checkDosageForm = (donation.getDoctorCorrectedDosageForm() != null &&
                !donation.getDoctorCorrectedDosageForm().isBlank())
                ? donation.getDoctorCorrectedDosageForm()
                : donation.getDosageForm();

        // If medicine already linked — check if linked medicine matches
        // But ALSO check all other DB records for same brand (e.g. Panadol 500mg vs Panadol 1mg)
        if (donation.getMedicine() != null) {
            // First check the linked medicine
            String dbStrength   = donation.getMedicine().getStrength();
            String dbDosageForm = donation.getMedicine().getDosageForm();

            boolean linkedStrengthMatch = dbStrength != null && checkStrength != null &&
                    dbStrength.replaceAll("\\s+", "").equalsIgnoreCase(
                            checkStrength.trim().replaceAll("\\s+", ""));
            boolean linkedDosageFormMatch = dbDosageForm != null && checkDosageForm != null &&
                    dbDosageForm.equalsIgnoreCase(checkDosageForm.trim());

            if (linkedStrengthMatch && linkedDosageFormMatch) {
                return "FULL_MATCH";
            }

            // Linked medicine doesn't match — check ALL records for this brand
            // e.g. donor submitted Panadol 1mg, linked was Panadol 500mg,
            // but Panadol 1mg also exists in DB → should be FULL_MATCH
            String brandName = (donation.getDoctorCorrectedBrandName() != null &&
                    !donation.getDoctorCorrectedBrandName().isBlank())
                    ? donation.getDoctorCorrectedBrandName()
                    : donation.getBrandNameSubmitted();

            if (brandName != null && !brandName.isBlank()) {
                List<Medicine> allByBrand =
                        medicineRepository.findByBrandNameIgnoreCase(brandName.trim());
                boolean anyMatch = allByBrand.stream().anyMatch(m -> {
                    boolean sm = m.getStrength() != null && checkStrength != null &&
                            m.getStrength().replaceAll("\\s+", "").equalsIgnoreCase(
                                    checkStrength.trim().replaceAll("\\s+", ""));
                    boolean dm = m.getDosageForm() != null && checkDosageForm != null &&
                            m.getDosageForm().equalsIgnoreCase(checkDosageForm.trim());
                    return sm && dm;
                });
                if (anyMatch) return "FULL_MATCH";
            }

            return "PARTIAL_MATCH";
        }

        // ✅ Use doctor's corrected brand name if available, else donor's original
        String brandName = (donation.getDoctorCorrectedBrandName() != null &&
                !donation.getDoctorCorrectedBrandName().isBlank())
                ? donation.getDoctorCorrectedBrandName()
                : donation.getBrandNameSubmitted();

        if (brandName == null || brandName.isBlank()) {
            return "NO_MATCH";
        }

        // ✅ FIXED — List instead of Optional, safe for multiple entries (e.g. Panadol 500mg + Panadol Extra)
        List<Medicine> byBrand =
                medicineRepository.findByBrandNameIgnoreCase(brandName.trim());

        if (byBrand.isEmpty()) {
            return "NO_MATCH";
        }

        // ✅ Check if ANY entry with this brand has matching strength + dosageForm
        // ✅ Normalize spaces e.g. "500mg" == "500 mg"
        boolean exactMatch = byBrand.stream().anyMatch(m -> {
            boolean strengthMatch = m.getStrength() != null && checkStrength != null &&
                    m.getStrength().replaceAll("\\s+", "").equalsIgnoreCase(
                            checkStrength.trim().replaceAll("\\s+", ""));
            boolean dosageFormMatch = m.getDosageForm() != null && checkDosageForm != null &&
                    m.getDosageForm().equalsIgnoreCase(checkDosageForm.trim());
            return strengthMatch && dosageFormMatch;
        });

        return exactMatch ? "FULL_MATCH" : "PARTIAL_MATCH";
    }

    // ─────────────────────────────────────────
    // MAPPER
    // ─────────────────────────────────────────

    public DonationResponse mapToDonationResponse(Donation donation) {
        return DonationResponse.builder()
                .id(donation.getId())
                .donorName(donation.getDonor().getName())
                .donorEmail(donation.getDonor().getEmail())
                .brandNameSubmitted(donation.getBrandNameSubmitted())
                .medicineId(donation.getMedicine() != null ?
                        donation.getMedicine().getId() : null)
                .medicineBrandName(donation.getMedicine() != null ?
                        donation.getMedicine().getBrandName() : null)
                .medicineApiName(donation.getMedicine() != null ?
                        donation.getMedicine().getApiName() : null)
                .medicineStrength(donation.getMedicine() != null ?
                        donation.getMedicine().getStrength() : null)
                .medicineDosageForm(donation.getMedicine() != null ?
                        donation.getMedicine().getDosageForm() : null)
                .medicineSchedule(donation.getMedicine() != null ?
                        donation.getMedicine().getSchedule().name() : null)
                .dosageForm(donation.getDosageForm())
                .strength(donation.getStrength())
                // ✅ Doctor correction fields
                .doctorCorrectedBrandName(donation.getDoctorCorrectedBrandName())
                .doctorCorrectedStrength(donation.getDoctorCorrectedStrength())
                .doctorCorrectedDosageForm(donation.getDoctorCorrectedDosageForm())
                .doctorCorrectedQuantity(donation.getDoctorCorrectedQuantity())
                .doctorCorrectedExpiryDate(donation.getDoctorCorrectedExpiryDate())
                // ✅ DB match status
                .dbMatchStatus(computeDbMatchStatus(donation))
                // ✅ All DB records for the brand — shows all variants (e.g. Panadol 500mg + Panadol 250mg)
                .dbMatchingRecords(buildDbMatchingRecords(donation))
                .quantity(donation.getQuantity())
                .expiryDate(donation.getExpiryDate())
                .photoUrl(donation.getPhotoUrl())
                .packageProofUrl(donation.getPackageProofUrl())
                .status(donation.getStatus().name())
                .doctorName(donation.getApprovedByDoctor() != null ?
                        donation.getApprovedByDoctor().getName() : null)
                .doctorNotes(donation.getDoctorNotes())
                .rejectionReason(donation.getRejectionReason())
                .pharmacistRejectionReason(
                        donation.getPharmacistRejectionReason())
                .collectionPointId(donation.getCollectionPoint() != null ?
                        donation.getCollectionPoint().getId() : null)
                .collectionPointName(donation.getCollectionPoint() != null ?
                        donation.getCollectionPoint().getLocationName() : null)
                .collectionPointAddress(donation.getCollectionPoint() != null ?
                        donation.getCollectionPoint().getAddress() : null)
                .recipientName(donation.getRecipient() != null ?
                        donation.getRecipient().getName() : null)
                .createdAt(donation.getCreatedAt())
                .updatedAt(donation.getUpdatedAt())
                .build();
    }

    // ─────────────────────────────────────────
    // BUILD DB MATCHING RECORDS
    // Returns ALL DB entries for the brand name
    // e.g. Panadol 500mg Tablet + Panadol 250mg Tablet
    // ─────────────────────────────────────────

    private List<DonationResponse.DbMedicineRecord> buildDbMatchingRecords(Donation donation) {
        // Determine brand name to look up
        String brandName = (donation.getDoctorCorrectedBrandName() != null &&
                !donation.getDoctorCorrectedBrandName().isBlank())
                ? donation.getDoctorCorrectedBrandName()
                : donation.getBrandNameSubmitted();

        if (brandName == null || brandName.isBlank()) {
            return Collections.emptyList();
        }

        List<Medicine> matches =
                medicineRepository.findByBrandNameIgnoreCase(brandName.trim());

        return matches.stream()
                .map(m -> DonationResponse.DbMedicineRecord.builder()
                        .id(m.getId())
                        .apiName(m.getApiName())
                        .strength(m.getStrength())
                        .dosageForm(m.getDosageForm())
                        .route(m.getRoute())
                        .schedule(m.getSchedule() != null ? m.getSchedule().name() : null)
                        .pharmacistVerified(m.isPharmacistVerified())
                        .build())
                .collect(Collectors.toList());
    }

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

import com.medicinedonation.dto.request.DonationRequest;
import com.medicinedonation.dto.response.CollectionPointResponse;
import com.medicinedonation.dto.response.DonationResponse;
import com.medicinedonation.enums.DonationStatus;
import com.medicinedonation.model.CollectionPoint;
import com.medicinedonation.model.Donation;
import com.medicinedonation.model.Medicine;
import com.medicinedonation.model.PendingMedicine;
import com.medicinedonation.model.User;
import com.medicinedonation.repository.CollectionPointRepository;
import com.medicinedonation.repository.DonationRepository;
import com.medicinedonation.repository.MedicineRepository;
import com.medicinedonation.repository.PendingMedicineRepository;
import com.medicinedonation.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class DonorService {

    @Autowired
    private DonationRepository donationRepository;

    @Autowired
    private CollectionPointRepository collectionPointRepository;

    @Autowired
    private PendingMedicineRepository pendingMedicineRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MedicineService medicineService;

    @Autowired
    private MedicineRepository medicineRepository;

    // ─────────────────────────────────────────
    // SUBMIT DONATION
    // ─────────────────────────────────────────

    public DonationResponse submitDonation(
            DonationRequest request, String donorEmail) {

        User donor = userRepository.findByEmail(donorEmail)
                .orElseThrow(() -> new RuntimeException("Donor not found"));

        LocalDate minExpiry = LocalDate.now().plusMonths(6);
        if (request.getExpiryDate().isBefore(minExpiry)) {
            throw new RuntimeException(
                    "Medicine must have more than 6 months before expiry. " +
                            "Expiry date must be after: " + minExpiry);
        }

        CollectionPoint collectionPoint =
                collectionPointRepository
                        .findById(request.getCollectionPointId())
                        .orElseThrow(() -> new RuntimeException(
                                "Collection point not found"));

        Optional<Medicine> medicineOpt =
                medicineService.findByBrandName(
                        normalize(request.getBrandNameSubmitted()));

        Donation donation = Donation.builder()
                .donor(donor)
                .brandNameSubmitted(normalize(request.getBrandNameSubmitted()))
                .quantity(request.getQuantity())
                .expiryDate(request.getExpiryDate())
                .photoUrl(request.getPhotoUrl())
                .packageProofUrl(request.getPackageProofUrl())
                .dosageForm(request.getDosageForm())
                .strength(request.getStrength())
                .collectionPoint(collectionPoint)
                .status(DonationStatus.PENDING_DOCTOR)
                .build();

        if (medicineOpt.isPresent()) {
            donation.setMedicine(medicineOpt.get());
        }

        Donation saved = donationRepository.save(donation);
        return mapToDonationResponse(saved);
    }

    // ─────────────────────────────────────────
    // GET MY DONATIONS
    // ─────────────────────────────────────────

    public List<DonationResponse> getMyDonations(String donorEmail) {
        User donor = userRepository.findByEmail(donorEmail)
                .orElseThrow(() -> new RuntimeException("Donor not found"));

        return donationRepository.findByDonor(donor)
                .stream()
                .map(this::mapToDonationResponse)
                .collect(Collectors.toList());
    }

    // ─────────────────────────────────────────
    // GET DONATION BY ID
    // ─────────────────────────────────────────

    public DonationResponse getDonationById(Long id, String donorEmail) {
        Donation donation = donationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(
                        "Donation not found with id: " + id));

        if (!donation.getDonor().getEmail().equals(donorEmail)) {
            throw new RuntimeException(
                    "You are not authorized to view this donation");
        }

        return mapToDonationResponse(donation);
    }

    // ─────────────────────────────────────────
    // GET ACTIVE COLLECTION POINTS
    // ─────────────────────────────────────────

    public List<CollectionPointResponse> getActiveCollectionPoints() {
        return collectionPointRepository.findByActive(true)
                .stream()
                .map(this::mapToCollectionPointResponse)
                .collect(Collectors.toList());
    }

    // ─────────────────────────────────────────
    // GET DONATION COUNTS
    // ─────────────────────────────────────────

    public java.util.Map<String, Long> getDonationCounts(String donorEmail) {

        User donor = userRepository.findByEmail(donorEmail)
                .orElseThrow(() -> new RuntimeException("Donor not found"));

        java.util.Map<String, Long> counts = new java.util.HashMap<>();

        counts.put("totalDonations",
                donationRepository.countByDonor(donor));
        counts.put("pendingDoctor",
                donationRepository.countByDonorAndStatus(
                        donor, DonationStatus.PENDING_DOCTOR));
        counts.put("pendingPharmacist",
                donationRepository.countByDonorAndStatus(
                        donor, DonationStatus.PENDING_PHARMACIST));
        counts.put("pendingDoctorRecheck",
                donationRepository.countByDonorAndStatus(
                        donor, DonationStatus.PENDING_DOCTOR_RECHECK));
        counts.put("pharmacistRejected",
                donationRepository.countByDonorAndStatus(
                        donor, DonationStatus.PHARMACIST_REJECTED));
        counts.put("doctorApproved",
                donationRepository.countByDonorAndStatus(
                        donor, DonationStatus.DOCTOR_APPROVED));
        counts.put("rejectedByDoctor",
                donationRepository.countByDonorAndStatus(
                        donor, DonationStatus.REJECTED_BY_DOCTOR));
        counts.put("pendingCustody",
                donationRepository.countByDonorAndStatus(
                        donor, DonationStatus.PENDING_CUSTODY));
        counts.put("live",
                donationRepository.countByDonorAndStatus(
                        donor, DonationStatus.LIVE));
        counts.put("collected",
                donationRepository.countByDonorAndStatus(
                        donor, DonationStatus.COLLECTED));

        return counts;
    }

    // ─────────────────────────────────────────
    // NORMALIZE INPUT
    // Collapses multiple spaces → single space, trims edges
    // "Vitamin  B1" → "Vitamin B1", " 500 mg " → "500 mg"
    // ─────────────────────────────────────────

    private String normalize(String input) {
        if (input == null) return null;
        return input.trim().replaceAll("\\s+", " ");
    }

    // Same but also removes ALL spaces for strength comparison
    // "500 mg" == "500mg"
    private String normalizeStrength(String input) {
        if (input == null) return null;
        return input.trim().replaceAll("\\s+", "").toLowerCase();
    }

    // ─────────────────────────────────────────
    // COMPUTE DB MATCH STATUS
    //
    // FULL_MATCH    — brand + strength + dosageForm all match
    // PARTIAL_MATCH — brand found but strength or dosageForm differ
    // NO_MATCH      — brand not found at all
    // ─────────────────────────────────────────

    private String computeDbMatchStatus(Donation donation) {

        // Determine what the doctor wants to match against
        String checkStrength = (donation.getDoctorCorrectedStrength() != null &&
                !donation.getDoctorCorrectedStrength().isBlank())
                ? donation.getDoctorCorrectedStrength()
                : donation.getStrength();
        String checkDosageForm = (donation.getDoctorCorrectedDosageForm() != null &&
                !donation.getDoctorCorrectedDosageForm().isBlank())
                ? donation.getDoctorCorrectedDosageForm()
                : donation.getDosageForm();

        // If medicine already linked — check if linked medicine matches
        // But ALSO check all other DB records for same brand (e.g. Panadol 500mg vs Panadol 1mg)
        if (donation.getMedicine() != null) {
            // First check the linked medicine
            String dbStrength   = donation.getMedicine().getStrength();
            String dbDosageForm = donation.getMedicine().getDosageForm();

            boolean linkedStrengthMatch = dbStrength != null && checkStrength != null &&
                    normalizeStrength(dbStrength).equals(normalizeStrength(checkStrength));
            boolean linkedDosageFormMatch = dbDosageForm != null && checkDosageForm != null &&
                    dbDosageForm.equalsIgnoreCase(checkDosageForm.trim());

            if (linkedStrengthMatch && linkedDosageFormMatch) {
                return "FULL_MATCH";
            }

            // Linked medicine doesn't match — check ALL records for this brand
            // e.g. donor submitted Panadol 1mg, linked was Panadol 500mg,
            // but Panadol 1mg also exists in DB → should be FULL_MATCH
            String brandName = (donation.getDoctorCorrectedBrandName() != null &&
                    !donation.getDoctorCorrectedBrandName().isBlank())
                    ? donation.getDoctorCorrectedBrandName()
                    : donation.getBrandNameSubmitted();

            if (brandName != null && !brandName.isBlank()) {
                List<Medicine> allByBrand =
                        findByBrandSafe(brandName);
                boolean anyMatch = allByBrand.stream().anyMatch(m -> {
                    boolean sm = m.getStrength() != null && checkStrength != null &&
                            m.getStrength().replaceAll("\\s+", "").equalsIgnoreCase(
                                    checkStrength.trim().replaceAll("\\s+", ""));
                    boolean dm = m.getDosageForm() != null && checkDosageForm != null &&
                            m.getDosageForm().equalsIgnoreCase(checkDosageForm.trim());
                    return sm && dm;
                });
                if (anyMatch) return "FULL_MATCH";
            }

            return "PARTIAL_MATCH";
        }

        // ✅ Use doctor's corrected brand name if available, else donor's original
        String brandName = (donation.getDoctorCorrectedBrandName() != null &&
                !donation.getDoctorCorrectedBrandName().isBlank())
                ? donation.getDoctorCorrectedBrandName()
                : donation.getBrandNameSubmitted();

        if (brandName == null || brandName.isBlank()) {
            return "NO_MATCH";
        }

        // ✅ FIXED — List instead of Optional, safe for multiple entries (e.g. Panadol 500mg + Panadol Extra)
        List<Medicine> byBrand =
                findByBrandSafe(brandName);

        if (byBrand.isEmpty()) {
            return "NO_MATCH";
        }

        // ✅ Check if ANY entry with this brand has matching strength + dosageForm
        // ✅ Normalize spaces e.g. "500mg" == "500 mg"
        boolean exactMatch = byBrand.stream().anyMatch(m -> {
            boolean strengthMatch = m.getStrength() != null && checkStrength != null &&
                    normalizeStrength(m.getStrength()).equals(normalizeStrength(checkStrength));
            boolean dosageFormMatch = m.getDosageForm() != null && checkDosageForm != null &&
                    m.getDosageForm().equalsIgnoreCase(checkDosageForm.trim());
            return strengthMatch && dosageFormMatch;
        });

        return exactMatch ? "FULL_MATCH" : "PARTIAL_MATCH";
    }

    // ─────────────────────────────────────────
    // MAPPER
    // ─────────────────────────────────────────

    public DonationResponse mapToDonationResponse(Donation donation) {
        return DonationResponse.builder()
                .id(donation.getId())
                .donorName(donation.getDonor().getName())
                .donorEmail(donation.getDonor().getEmail())
                .brandNameSubmitted(donation.getBrandNameSubmitted())
                .medicineId(donation.getMedicine() != null ?
                        donation.getMedicine().getId() : null)
                .medicineBrandName(donation.getMedicine() != null ?
                        donation.getMedicine().getBrandName() : null)
                .medicineApiName(donation.getMedicine() != null ?
                        donation.getMedicine().getApiName() : null)
                .medicineStrength(donation.getMedicine() != null ?
                        donation.getMedicine().getStrength() : null)
                .medicineDosageForm(donation.getMedicine() != null ?
                        donation.getMedicine().getDosageForm() : null)
                .medicineSchedule(donation.getMedicine() != null ?
                        donation.getMedicine().getSchedule().name() : null)
                .dosageForm(donation.getDosageForm())
                .strength(donation.getStrength())
                // ✅ Doctor correction fields
                .doctorCorrectedBrandName(donation.getDoctorCorrectedBrandName())
                .doctorCorrectedStrength(donation.getDoctorCorrectedStrength())
                .doctorCorrectedDosageForm(donation.getDoctorCorrectedDosageForm())
                .doctorCorrectedQuantity(donation.getDoctorCorrectedQuantity())
                .doctorCorrectedExpiryDate(donation.getDoctorCorrectedExpiryDate())
                // ✅ DB match status
                .dbMatchStatus(computeDbMatchStatus(donation))
                // ✅ All DB records for the brand — shows all variants (e.g. Panadol 500mg + Panadol 250mg)
                .dbMatchingRecords(buildDbMatchingRecords(donation))
                .quantity(donation.getQuantity())
                .expiryDate(donation.getExpiryDate())
                .photoUrl(donation.getPhotoUrl())
                .packageProofUrl(donation.getPackageProofUrl())
                .status(donation.getStatus().name())
                .doctorName(donation.getApprovedByDoctor() != null ?
                        donation.getApprovedByDoctor().getName() : null)
                .doctorNotes(donation.getDoctorNotes())
                .rejectionReason(donation.getRejectionReason())
                .pharmacistRejectionReason(
                        donation.getPharmacistRejectionReason())
                .collectionPointId(donation.getCollectionPoint() != null ?
                        donation.getCollectionPoint().getId() : null)
                .collectionPointName(donation.getCollectionPoint() != null ?
                        donation.getCollectionPoint().getLocationName() : null)
                .collectionPointAddress(donation.getCollectionPoint() != null ?
                        donation.getCollectionPoint().getAddress() : null)
                .recipientName(donation.getRecipient() != null ?
                        donation.getRecipient().getName() : null)
                .createdAt(donation.getCreatedAt())
                .updatedAt(donation.getUpdatedAt())
                .build();
    }

    // ─────────────────────────────────────────
    // BUILD DB MATCHING RECORDS
    // Returns ALL DB entries for the brand name
    // e.g. Panadol 500mg Tablet + Panadol 250mg Tablet
    // ─────────────────────────────────────────

    private List<DonationResponse.DbMedicineRecord> buildDbMatchingRecords(Donation donation) {
        // Determine brand name to look up
        String brandName = (donation.getDoctorCorrectedBrandName() != null &&
                !donation.getDoctorCorrectedBrandName().isBlank())
                ? donation.getDoctorCorrectedBrandName()
                : donation.getBrandNameSubmitted();

        if (brandName == null || brandName.isBlank()) {
            return Collections.emptyList();
        }

        List<Medicine> matches =
                findByBrandSafe(brandName);

        return matches.stream()
                .map(m -> DonationResponse.DbMedicineRecord.builder()
                        .id(m.getId())
                        .apiName(m.getApiName())
                        .strength(m.getStrength())
                        .dosageForm(m.getDosageForm())
                        .route(m.getRoute())
                        .schedule(m.getSchedule() != null ? m.getSchedule().name() : null)
                        .pharmacistVerified(m.isPharmacistVerified())
                        .build())
                .collect(Collectors.toList());
    }
    // ✅ Safe brand lookup — normalizes all spaces before DB lookup
    // "Vitamin  B1" → "Vitamin B1" before hitting findByBrandNameIgnoreCase
    private List<Medicine> findByBrandSafe(String brandName) {
        if (brandName == null || brandName.isBlank()) return Collections.emptyList();
        return medicineRepository.findByBrandNameIgnoreCase(normalize(brandName));
    }

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

import com.medicinedonation.dto.request.DonationRequest;
import com.medicinedonation.dto.response.CollectionPointResponse;
import com.medicinedonation.dto.response.DonationResponse;
import com.medicinedonation.enums.DonationStatus;
import com.medicinedonation.model.CollectionPoint;
import com.medicinedonation.model.Donation;
import com.medicinedonation.model.Medicine;
import com.medicinedonation.model.PendingMedicine;
import com.medicinedonation.model.User;
import com.medicinedonation.repository.CollectionPointRepository;
import com.medicinedonation.repository.DonationRepository;
import com.medicinedonation.repository.MedicineRepository;
import com.medicinedonation.repository.PendingMedicineRepository;
import com.medicinedonation.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class DonorService {

    @Autowired
    private DonationRepository donationRepository;

    @Autowired
    private CollectionPointRepository collectionPointRepository;

    @Autowired
    private PendingMedicineRepository pendingMedicineRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MedicineService medicineService;

    @Autowired
    private MedicineRepository medicineRepository;

    // ─────────────────────────────────────────
    // SUBMIT DONATION
    // ─────────────────────────────────────────

    public DonationResponse submitDonation(
            DonationRequest request, String donorEmail) {

        User donor = userRepository.findByEmail(donorEmail)
                .orElseThrow(() -> new RuntimeException("Donor not found"));

        LocalDate minExpiry = LocalDate.now().plusMonths(6);
        if (request.getExpiryDate().isBefore(minExpiry)) {
            throw new RuntimeException(
                    "Medicine must have more than 6 months before expiry. " +
                            "Expiry date must be after: " + minExpiry);
        }

        CollectionPoint collectionPoint =
                collectionPointRepository
                        .findById(request.getCollectionPointId())
                        .orElseThrow(() -> new RuntimeException(
                                "Collection point not found"));

        // ✅ Only link medicine if brand + strength + dosageForm ALL match exactly
        // Do NOT link by brand alone — Clarityn 5mg should NOT link to Clarityn 10mg
        String normBrand    = normalize(request.getBrandNameSubmitted());
        String normStrength = normalize(request.getStrength());
        String normDosage   = normalize(request.getDosageForm());

        Medicine exactMatch = findByBrandSafe(normBrand).stream()
                .filter(m -> {
                    String mStr = m.getStrength() != null
                            ? m.getStrength().trim().replaceAll("\\s+", "").toLowerCase() : "";
                    String rStr = normStrength != null
                            ? normStrength.trim().replaceAll("\\s+", "").toLowerCase() : "";
                    String mDos = m.getDosageForm() != null
                            ? m.getDosageForm().trim().toLowerCase() : "";
                    String rDos = normDosage != null
                            ? normDosage.trim().toLowerCase() : "";
                    return mStr.equals(rStr) && mDos.equals(rDos);
                })
                .findFirst()
                .orElse(null);

        Donation donation = Donation.builder()
                .donor(donor)
                .brandNameSubmitted(normBrand)
                .quantity(request.getQuantity())
                .expiryDate(request.getExpiryDate())
                .photoUrl(request.getPhotoUrl())
                .packageProofUrl(request.getPackageProofUrl())
                .dosageForm(request.getDosageForm())
                .strength(request.getStrength())
                .collectionPoint(collectionPoint)
                .status(DonationStatus.PENDING_DOCTOR)
                .build();

        if (exactMatch != null) {
            donation.setMedicine(exactMatch);
        }

        Donation saved = donationRepository.save(donation);
        return mapToDonationResponse(saved);
    }

    // ─────────────────────────────────────────
    // GET MY DONATIONS
    // ─────────────────────────────────────────

    public List<DonationResponse> getMyDonations(String donorEmail) {
        User donor = userRepository.findByEmail(donorEmail)
                .orElseThrow(() -> new RuntimeException("Donor not found"));

        return donationRepository.findByDonor(donor)
                .stream()
                .map(this::mapToDonationResponse)
                .collect(Collectors.toList());
    }

    // ─────────────────────────────────────────
    // GET DONATION BY ID
    // ─────────────────────────────────────────

    public DonationResponse getDonationById(Long id, String donorEmail) {
        Donation donation = donationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(
                        "Donation not found with id: " + id));

        if (!donation.getDonor().getEmail().equals(donorEmail)) {
            throw new RuntimeException(
                    "You are not authorized to view this donation");
        }

        return mapToDonationResponse(donation);
    }

    // ─────────────────────────────────────────
    // GET ACTIVE COLLECTION POINTS
    // ─────────────────────────────────────────

    public List<CollectionPointResponse> getActiveCollectionPoints() {
        return collectionPointRepository.findByActive(true)
                .stream()
                .map(this::mapToCollectionPointResponse)
                .collect(Collectors.toList());
    }

    // ─────────────────────────────────────────
    // GET DONATION COUNTS
    // ─────────────────────────────────────────

    public java.util.Map<String, Long> getDonationCounts(String donorEmail) {

        User donor = userRepository.findByEmail(donorEmail)
                .orElseThrow(() -> new RuntimeException("Donor not found"));

        java.util.Map<String, Long> counts = new java.util.HashMap<>();

        counts.put("totalDonations",
                donationRepository.countByDonor(donor));
        counts.put("pendingDoctor",
                donationRepository.countByDonorAndStatus(
                        donor, DonationStatus.PENDING_DOCTOR));
        counts.put("pendingPharmacist",
                donationRepository.countByDonorAndStatus(
                        donor, DonationStatus.PENDING_PHARMACIST));
        counts.put("pendingDoctorRecheck",
                donationRepository.countByDonorAndStatus(
                        donor, DonationStatus.PENDING_DOCTOR_RECHECK));
        counts.put("pharmacistRejected",
                donationRepository.countByDonorAndStatus(
                        donor, DonationStatus.PHARMACIST_REJECTED));
        counts.put("doctorApproved",
                donationRepository.countByDonorAndStatus(
                        donor, DonationStatus.DOCTOR_APPROVED));
        counts.put("rejectedByDoctor",
                donationRepository.countByDonorAndStatus(
                        donor, DonationStatus.REJECTED_BY_DOCTOR));
        counts.put("pendingCustody",
                donationRepository.countByDonorAndStatus(
                        donor, DonationStatus.PENDING_CUSTODY));
        counts.put("live",
                donationRepository.countByDonorAndStatus(
                        donor, DonationStatus.LIVE));
        counts.put("collected",
                donationRepository.countByDonorAndStatus(
                        donor, DonationStatus.COLLECTED));

        return counts;
    }

    // ─────────────────────────────────────────
    // NORMALIZE INPUT
    // Collapses multiple spaces → single space, trims edges
    // "Vitamin  B1" → "Vitamin B1", " 500 mg " → "500 mg"
    // ─────────────────────────────────────────

    private String normalize(String input) {
        if (input == null) return null;
        return input.trim().replaceAll("\\s+", " ");
    }

    // Same but also removes ALL spaces for strength comparison
    // "500 mg" == "500mg"
    private String normalizeStrength(String input) {
        if (input == null) return null;
        return input.trim().replaceAll("\\s+", "").toLowerCase();
    }

    // ─────────────────────────────────────────
    // COMPUTE DB MATCH STATUS
    //
    // FULL_MATCH    — brand + strength + dosageForm all match
    // PARTIAL_MATCH — brand found but strength or dosageForm differ
    // NO_MATCH      — brand not found at all
    // ─────────────────────────────────────────

    private String computeDbMatchStatus(Donation donation) {

        // Determine what the doctor wants to match against
        String checkStrength = (donation.getDoctorCorrectedStrength() != null &&
                !donation.getDoctorCorrectedStrength().isBlank())
                ? donation.getDoctorCorrectedStrength()
                : donation.getStrength();
        String checkDosageForm = (donation.getDoctorCorrectedDosageForm() != null &&
                !donation.getDoctorCorrectedDosageForm().isBlank())
                ? donation.getDoctorCorrectedDosageForm()
                : donation.getDosageForm();

        // If medicine already linked — check if linked medicine matches
        // But ALSO check all other DB records for same brand (e.g. Panadol 500mg vs Panadol 1mg)
        if (donation.getMedicine() != null) {
            // First check the linked medicine
            String dbStrength   = donation.getMedicine().getStrength();
            String dbDosageForm = donation.getMedicine().getDosageForm();

            boolean linkedStrengthMatch = dbStrength != null && checkStrength != null &&
                    normalizeStrength(dbStrength).equals(normalizeStrength(checkStrength));
            boolean linkedDosageFormMatch = dbDosageForm != null && checkDosageForm != null &&
                    dbDosageForm.equalsIgnoreCase(checkDosageForm.trim());

            if (linkedStrengthMatch && linkedDosageFormMatch) {
                return "FULL_MATCH";
            }

            // Linked medicine doesn't match — check ALL records for this brand
            // e.g. donor submitted Panadol 1mg, linked was Panadol 500mg,
            // but Panadol 1mg also exists in DB → should be FULL_MATCH
            String brandName = (donation.getDoctorCorrectedBrandName() != null &&
                    !donation.getDoctorCorrectedBrandName().isBlank())
                    ? donation.getDoctorCorrectedBrandName()
                    : donation.getBrandNameSubmitted();

            if (brandName != null && !brandName.isBlank()) {
                List<Medicine> allByBrand =
                        findByBrandSafe(brandName);
                boolean anyMatch = allByBrand.stream().anyMatch(m -> {
                    boolean sm = m.getStrength() != null && checkStrength != null &&
                            m.getStrength().replaceAll("\\s+", "").equalsIgnoreCase(
                                    checkStrength.trim().replaceAll("\\s+", ""));
                    boolean dm = m.getDosageForm() != null && checkDosageForm != null &&
                            m.getDosageForm().equalsIgnoreCase(checkDosageForm.trim());
                    return sm && dm;
                });
                if (anyMatch) return "FULL_MATCH";
            }

            return "PARTIAL_MATCH";
        }

        // ✅ Use doctor's corrected brand name if available, else donor's original
        String brandName = (donation.getDoctorCorrectedBrandName() != null &&
                !donation.getDoctorCorrectedBrandName().isBlank())
                ? donation.getDoctorCorrectedBrandName()
                : donation.getBrandNameSubmitted();

        if (brandName == null || brandName.isBlank()) {
            return "NO_MATCH";
        }

        // ✅ FIXED — List instead of Optional, safe for multiple entries (e.g. Panadol 500mg + Panadol Extra)
        List<Medicine> byBrand =
                findByBrandSafe(brandName);

        if (byBrand.isEmpty()) {
            return "NO_MATCH";
        }

        // ✅ Check if ANY entry with this brand has matching strength + dosageForm
        // ✅ Normalize spaces e.g. "500mg" == "500 mg"
        boolean exactMatch = byBrand.stream().anyMatch(m -> {
            boolean strengthMatch = m.getStrength() != null && checkStrength != null &&
                    normalizeStrength(m.getStrength()).equals(normalizeStrength(checkStrength));
            boolean dosageFormMatch = m.getDosageForm() != null && checkDosageForm != null &&
                    m.getDosageForm().equalsIgnoreCase(checkDosageForm.trim());
            return strengthMatch && dosageFormMatch;
        });

        return exactMatch ? "FULL_MATCH" : "PARTIAL_MATCH";
    }

    // ─────────────────────────────────────────
    // MAPPER
    // ─────────────────────────────────────────

    public DonationResponse mapToDonationResponse(Donation donation) {
        return DonationResponse.builder()
                .id(donation.getId())
                .donorName(donation.getDonor().getName())
                .donorEmail(donation.getDonor().getEmail())
                .brandNameSubmitted(donation.getBrandNameSubmitted())
                .medicineId(donation.getMedicine() != null ?
                        donation.getMedicine().getId() : null)
                .medicineBrandName(donation.getMedicine() != null ?
                        donation.getMedicine().getBrandName() : null)
                .medicineApiName(donation.getMedicine() != null ?
                        donation.getMedicine().getApiName() : null)
                .medicineStrength(donation.getMedicine() != null ?
                        donation.getMedicine().getStrength() : null)
                .medicineDosageForm(donation.getMedicine() != null ?
                        donation.getMedicine().getDosageForm() : null)
                .medicineSchedule(donation.getMedicine() != null ?
                        donation.getMedicine().getSchedule().name() : null)
                .dosageForm(donation.getDosageForm())
                .strength(donation.getStrength())
                // ✅ Doctor correction fields
                .doctorCorrectedBrandName(donation.getDoctorCorrectedBrandName())
                .doctorCorrectedStrength(donation.getDoctorCorrectedStrength())
                .doctorCorrectedDosageForm(donation.getDoctorCorrectedDosageForm())
                .doctorCorrectedQuantity(donation.getDoctorCorrectedQuantity())
                .doctorCorrectedExpiryDate(donation.getDoctorCorrectedExpiryDate())
                // ✅ DB match status
                .dbMatchStatus(computeDbMatchStatus(donation))
                // ✅ All DB records for the brand — shows all variants (e.g. Panadol 500mg + Panadol 250mg)
                .dbMatchingRecords(buildDbMatchingRecords(donation))
                .quantity(donation.getQuantity())
                .expiryDate(donation.getExpiryDate())
                .photoUrl(donation.getPhotoUrl())
                .packageProofUrl(donation.getPackageProofUrl())
                .status(donation.getStatus().name())
                .doctorName(donation.getApprovedByDoctor() != null ?
                        donation.getApprovedByDoctor().getName() : null)
                .doctorNotes(donation.getDoctorNotes())
                .rejectionReason(donation.getRejectionReason())
                .pharmacistRejectionReason(
                        donation.getPharmacistRejectionReason())
                .collectionPointId(donation.getCollectionPoint() != null ?
                        donation.getCollectionPoint().getId() : null)
                .collectionPointName(donation.getCollectionPoint() != null ?
                        donation.getCollectionPoint().getLocationName() : null)
                .collectionPointAddress(donation.getCollectionPoint() != null ?
                        donation.getCollectionPoint().getAddress() : null)
                .recipientName(donation.getRecipient() != null ?
                        donation.getRecipient().getName() : null)
                .createdAt(donation.getCreatedAt())
                .updatedAt(donation.getUpdatedAt())
                .build();
    }

    // ─────────────────────────────────────────
    // BUILD DB MATCHING RECORDS
    // Returns ALL DB entries for the brand name
    // e.g. Panadol 500mg Tablet + Panadol 250mg Tablet
    // ─────────────────────────────────────────

    private List<DonationResponse.DbMedicineRecord> buildDbMatchingRecords(Donation donation) {
        // Determine brand name to look up
        String brandName = (donation.getDoctorCorrectedBrandName() != null &&
                !donation.getDoctorCorrectedBrandName().isBlank())
                ? donation.getDoctorCorrectedBrandName()
                : donation.getBrandNameSubmitted();

        if (brandName == null || brandName.isBlank()) {
            return Collections.emptyList();
        }

        List<Medicine> matches =
                findByBrandSafe(brandName);

        return matches.stream()
                .map(m -> DonationResponse.DbMedicineRecord.builder()
                        .id(m.getId())
                        .apiName(m.getApiName())
                        .strength(m.getStrength())
                        .dosageForm(m.getDosageForm())
                        .route(m.getRoute())
                        .schedule(m.getSchedule() != null ? m.getSchedule().name() : null)
                        .pharmacistVerified(m.isPharmacistVerified())
                        .build())
                .collect(Collectors.toList());
    }
    // ✅ Safe brand lookup — normalizes all spaces before DB lookup
    // "Vitamin  B1" → "Vitamin B1" before hitting findByBrandNameIgnoreCase
    private List<Medicine> findByBrandSafe(String brandName) {
        if (brandName == null || brandName.isBlank()) return Collections.emptyList();
        return medicineRepository.findByBrandNameIgnoreCase(normalize(brandName));
    }

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