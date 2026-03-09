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
}