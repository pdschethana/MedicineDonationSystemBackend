/*package com.medicinedonation.service;

import com.medicinedonation.dto.request.PharmacistReviewRequest;
import com.medicinedonation.dto.response.DonationResponse;
import com.medicinedonation.dto.response.PendingMedicineResponse;
import com.medicinedonation.enums.DonationStatus;
import com.medicinedonation.model.Donation;
import com.medicinedonation.model.Medicine;
import com.medicinedonation.model.PendingMedicine;
import com.medicinedonation.model.User;
import com.medicinedonation.repository.DonationRepository;
import com.medicinedonation.repository.MedicineRepository;
import com.medicinedonation.repository.PendingMedicineRepository;
import com.medicinedonation.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class PharmacistService {

    @Autowired
    private PendingMedicineRepository pendingMedicineRepository;

    @Autowired
    private DonationRepository donationRepository;

    @Autowired
    private MedicineRepository medicineRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DonorService donorService;

    // ─────────────────────────────────────────
    // GET PENDING MEDICINES
    // All unresolved pending medicines
    // ─────────────────────────────────────────

    public List<PendingMedicineResponse> getPendingMedicines() {
        return pendingMedicineRepository
                .findByResolvedFalseAndRejectedFalse()
                .stream()
                .map(this::mapToPendingMedicineResponse)
                .collect(Collectors.toList());
    }

    // ─────────────────────────────────────────
    // GET PENDING MEDICINE BY ID
    // ─────────────────────────────────────────

    public PendingMedicineResponse getPendingMedicineById(Long id) {
        PendingMedicine pending = pendingMedicineRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(
                        "Pending medicine not found with id: " + id));
        return mapToPendingMedicineResponse(pending);
    }

    // ─────────────────────────────────────────
    // ADD MEDICINE TO DATABASE
    // Pharmacist verifies and adds to DB
    // Donation goes back to doctor (PENDING_DOCTOR_RECHECK)
    // ─────────────────────────────────────────

    public PendingMedicineResponse addMedicineToDatabase(
            Long pendingMedicineId,
            PharmacistReviewRequest request,
            String pharmacistEmail) {

        PendingMedicine pending =
                pendingMedicineRepository.findById(pendingMedicineId)
                        .orElseThrow(() -> new RuntimeException(
                                "Pending medicine not found with id: "
                                        + pendingMedicineId));

        // Check not already resolved
        if (pending.isResolved() || pending.isRejected()) {
            throw new RuntimeException(
                    "This pending medicine has already been reviewed.");
        }

        User pharmacist = userRepository.findByEmail(pharmacistEmail)
                .orElseThrow(() -> new RuntimeException(
                        "Pharmacist not found"));

        // Check duplicate before adding
        boolean exists = medicineRepository
                .existsByBrandNameIgnoreCaseAndApiNameIgnoreCaseAndStrengthIgnoreCaseAndDosageFormIgnoreCaseAndRouteIgnoreCase(
                        pending.getBrandName(),
                        request.getResolvedApiName(),
                        request.getResolvedStrength(),
                        request.getResolvedDosageForm(),
                        request.getResolvedRoute()
                );

        if (exists) {
            throw new RuntimeException(
                    "Medicine already exists in database: " +
                            pending.getBrandName() + " " +
                            request.getResolvedStrength() + " " +
                            request.getResolvedDosageForm());
        }

        // Add medicine to DB — pharmacist verified
        Medicine medicine = Medicine.builder()
                .brandName(pending.getBrandName())
                .apiName(request.getResolvedApiName())
                .strength(request.getResolvedStrength())
                .dosageForm(request.getResolvedDosageForm())
                .route(request.getResolvedRoute())
                .schedule(request.getResolvedSchedule())
                .pharmacistVerified(true)
                .verifiedBy(pharmacist)
                .build();

        Medicine savedMedicine = medicineRepository.save(medicine);

        // Update pending medicine record
        pending.setResolvedApiName(request.getResolvedApiName());
        pending.setResolvedStrength(request.getResolvedStrength());
        pending.setResolvedDosageForm(request.getResolvedDosageForm());
        pending.setResolvedRoute(request.getResolvedRoute());
        pending.setResolvedSchedule(request.getResolvedSchedule());
        pending.setResolved(true);
        pending.setReviewedByPharmacist(pharmacist);
        pending.setResolvedAt(LocalDateTime.now());

        pendingMedicineRepository.save(pending);

        // Update donation — link medicine + send back to doctor
        Donation donation = pending.getDonation();
        donation.setMedicine(savedMedicine);
        donation.setStatus(DonationStatus.PENDING_DOCTOR_RECHECK);
        donationRepository.save(donation);

        return mapToPendingMedicineResponse(pending);
    }

    // ─────────────────────────────────────────
    // REJECT MEDICINE
    // Pharmacist cannot verify
    // Donation goes to PHARMACIST_REJECTED
    // Doctor makes final decision
    // ─────────────────────────────────────────

    public PendingMedicineResponse rejectMedicine(
            Long pendingMedicineId,
            PharmacistReviewRequest request,
            String pharmacistEmail) {

        PendingMedicine pending =
                pendingMedicineRepository.findById(pendingMedicineId)
                        .orElseThrow(() -> new RuntimeException(
                                "Pending medicine not found with id: "
                                        + pendingMedicineId));

        // Check not already reviewed
        if (pending.isResolved() || pending.isRejected()) {
            throw new RuntimeException(
                    "This pending medicine has already been reviewed.");
        }

        User pharmacist = userRepository.findByEmail(pharmacistEmail)
                .orElseThrow(() -> new RuntimeException(
                        "Pharmacist not found"));

        // Update pending medicine record
        pending.setRejected(true);
        pending.setRejectionReason(request.getRejectionReason());
        pending.setReviewedByPharmacist(pharmacist);
        pending.setResolvedAt(LocalDateTime.now());

        pendingMedicineRepository.save(pending);

        // Update donation status
        Donation donation = pending.getDonation();
        donation.setPharmacistRejectionReason(
                request.getRejectionReason());
        donation.setStatus(DonationStatus.PHARMACIST_REJECTED);
        donationRepository.save(donation);

        return mapToPendingMedicineResponse(pending);
    }

    // ─────────────────────────────────────────
    // GET RESOLUTION HISTORY
    // All medicines pharmacist has reviewed
    // ─────────────────────────────────────────

    public List<PendingMedicineResponse> getResolutionHistory(
            String pharmacistEmail) {

        User pharmacist = userRepository.findByEmail(pharmacistEmail)
                .orElseThrow(() -> new RuntimeException(
                        "Pharmacist not found"));

        return pendingMedicineRepository.findAll()
                .stream()
                .filter(p -> p.getReviewedByPharmacist() != null &&
                        p.getReviewedByPharmacist().getId()
                                .equals(pharmacist.getId()))
                .map(this::mapToPendingMedicineResponse)
                .collect(Collectors.toList());
    }

    // ─────────────────────────────────────────
    // DASHBOARD COUNTS
    // ─────────────────────────────────────────

    public Map<String, Long> getDashboardCounts() {
        Map<String, Long> counts = new HashMap<>();

        counts.put("pendingMedicines",
                pendingMedicineRepository
                        .countByResolvedFalseAndRejectedFalse());

        return counts;
    }

    // ─────────────────────────────────────────
    // MAPPER
    // ─────────────────────────────────────────

    private PendingMedicineResponse mapToPendingMedicineResponse(
            PendingMedicine pending) {

        return PendingMedicineResponse.builder()
                .id(pending.getId())
                .donationId(pending.getDonation().getId())
                .donorName(pending.getDonation().getDonor().getName())
                .brandName(pending.getBrandName())
                .photoUrl(pending.getPhotoUrl())
                .packageInsertUrl(pending.getPackageInsertUrl())
                .doctorNotes(pending.getDoctorNotes())
                .resolvedApiName(pending.getResolvedApiName())
                .resolvedStrength(pending.getResolvedStrength())
                .resolvedDosageForm(pending.getResolvedDosageForm())
                .resolvedRoute(pending.getResolvedRoute())
                .resolvedSchedule(pending.getResolvedSchedule() != null ?
                        pending.getResolvedSchedule().name() : null)
                .resolved(pending.isResolved())
                .rejected(pending.isRejected())
                .rejectionReason(pending.getRejectionReason())
                .reviewedByPharmacistName(
                        pending.getReviewedByPharmacist() != null ?
                                pending.getReviewedByPharmacist().getName() : null)
                .submittedAt(pending.getSubmittedAt())
                .resolvedAt(pending.getResolvedAt())
                .build();
    }
}*/

/*package com.medicinedonation.service;

import com.medicinedonation.dto.request.PharmacistReviewRequest;
import com.medicinedonation.dto.response.PendingMedicineResponse;
import com.medicinedonation.enums.DonationStatus;
import com.medicinedonation.model.Donation;
import com.medicinedonation.model.Medicine;
import com.medicinedonation.model.PendingMedicine;
import com.medicinedonation.model.User;
import com.medicinedonation.repository.DonationRepository;
import com.medicinedonation.repository.MedicineRepository;
import com.medicinedonation.repository.PendingMedicineRepository;
import com.medicinedonation.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class PharmacistService {

    @Autowired
    private PendingMedicineRepository pendingMedicineRepository;

    @Autowired
    private DonationRepository donationRepository;

    @Autowired
    private MedicineRepository medicineRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DonorService donorService;

    // ─────────────────────────────────────────
    // GET PENDING MEDICINES
    // ─────────────────────────────────────────

    public List<PendingMedicineResponse> getPendingMedicines() {
        return pendingMedicineRepository
                .findByResolvedFalseAndRejectedFalse()
                .stream()
                .map(this::mapToPendingMedicineResponse)
                .collect(Collectors.toList());
    }

    // ─────────────────────────────────────────
    // GET PENDING MEDICINE BY ID
    // ─────────────────────────────────────────

    public PendingMedicineResponse getPendingMedicineById(Long id) {
        PendingMedicine pending = pendingMedicineRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(
                        "Pending medicine not found with id: " + id));
        return mapToPendingMedicineResponse(pending);
    }

    // ─────────────────────────────────────────
    // ADD MEDICINE TO DATABASE
    // ─────────────────────────────────────────

    public PendingMedicineResponse addMedicineToDatabase(
            Long pendingMedicineId,
            PharmacistReviewRequest request,
            String pharmacistEmail) {

        PendingMedicine pending =
                pendingMedicineRepository.findById(pendingMedicineId)
                        .orElseThrow(() -> new RuntimeException(
                                "Pending medicine not found with id: "
                                        + pendingMedicineId));

        if (pending.isResolved() || pending.isRejected()) {
            throw new RuntimeException(
                    "This pending medicine has already been reviewed.");
        }

        User pharmacist = userRepository.findByEmail(pharmacistEmail)
                .orElseThrow(() -> new RuntimeException(
                        "Pharmacist not found"));

        // ✅ Use pharmacist's corrected brand name if provided, else donor's
        String finalBrandName = (request.getResolvedBrandName() != null &&
                !request.getResolvedBrandName().isBlank())
                ? request.getResolvedBrandName()
                : pending.getBrandName();

        // Check duplicate using final brand name
        boolean exists = medicineRepository
                .existsByBrandNameIgnoreCaseAndApiNameIgnoreCaseAndStrengthIgnoreCaseAndDosageFormIgnoreCaseAndRouteIgnoreCase(
                        finalBrandName,
                        request.getResolvedApiName(),
                        request.getResolvedStrength(),
                        request.getResolvedDosageForm(),
                        request.getResolvedRoute()
                );

        if (exists) {
            throw new RuntimeException(
                    "Medicine already exists in database: " +
                            finalBrandName + " " +
                            request.getResolvedStrength() + " " +
                            request.getResolvedDosageForm());
        }

        // ✅ Build medicine with corrected brand name + API name
        Medicine medicine = Medicine.builder()
                .brandName(finalBrandName)
                .apiName(request.getResolvedApiName())
                .strength(request.getResolvedStrength())
                .dosageForm(request.getResolvedDosageForm())
                .route(request.getResolvedRoute())
                .schedule(request.getResolvedSchedule())
                .pharmacistVerified(true)
                .verifiedBy(pharmacist)
                .build();

        Medicine savedMedicine = medicineRepository.save(medicine);

        pending.setResolvedApiName(request.getResolvedApiName());
        pending.setResolvedStrength(request.getResolvedStrength());
        pending.setResolvedDosageForm(request.getResolvedDosageForm());
        pending.setResolvedRoute(request.getResolvedRoute());
        pending.setResolvedSchedule(request.getResolvedSchedule());
        pending.setResolved(true);
        pending.setReviewedByPharmacist(pharmacist);
        pending.setResolvedAt(LocalDateTime.now());

        pendingMedicineRepository.save(pending);

        Donation donation = pending.getDonation();
        donation.setMedicine(savedMedicine);
        donation.setStatus(DonationStatus.PENDING_DOCTOR_RECHECK);
        donationRepository.save(donation);

        return mapToPendingMedicineResponse(pending);
    }

    // ─────────────────────────────────────────
    // REJECT MEDICINE
    // ─────────────────────────────────────────

    public PendingMedicineResponse rejectMedicine(
            Long pendingMedicineId,
            PharmacistReviewRequest request,
            String pharmacistEmail) {

        PendingMedicine pending =
                pendingMedicineRepository.findById(pendingMedicineId)
                        .orElseThrow(() -> new RuntimeException(
                                "Pending medicine not found with id: "
                                        + pendingMedicineId));

        if (pending.isResolved() || pending.isRejected()) {
            throw new RuntimeException(
                    "This pending medicine has already been reviewed.");
        }

        User pharmacist = userRepository.findByEmail(pharmacistEmail)
                .orElseThrow(() -> new RuntimeException(
                        "Pharmacist not found"));

        pending.setRejected(true);
        pending.setRejectionReason(request.getRejectionReason());
        pending.setReviewedByPharmacist(pharmacist);
        pending.setResolvedAt(LocalDateTime.now());

        pendingMedicineRepository.save(pending);

        Donation donation = pending.getDonation();
        donation.setPharmacistRejectionReason(request.getRejectionReason());
        donation.setStatus(DonationStatus.PHARMACIST_REJECTED);
        donationRepository.save(donation);

        return mapToPendingMedicineResponse(pending);
    }

    // ─────────────────────────────────────────
    // GET RESOLUTION HISTORY
    // ─────────────────────────────────────────

    public List<PendingMedicineResponse> getResolutionHistory(
            String pharmacistEmail) {

        User pharmacist = userRepository.findByEmail(pharmacistEmail)
                .orElseThrow(() -> new RuntimeException(
                        "Pharmacist not found"));

        return pendingMedicineRepository.findAll()
                .stream()
                .filter(p -> p.getReviewedByPharmacist() != null &&
                        p.getReviewedByPharmacist().getId()
                                .equals(pharmacist.getId()))
                .map(this::mapToPendingMedicineResponse)
                .collect(Collectors.toList());
    }

    // ─────────────────────────────────────────
    // DASHBOARD COUNTS — FIXED
    // ─────────────────────────────────────────

    public Map<String, Long> getDashboardCounts() {
        Map<String, Long> counts = new HashMap<>();

        counts.put("pendingMedicines",
                pendingMedicineRepository
                        .countByResolvedFalseAndRejectedFalse());

        // ✅ NEW — total verified and rejected
        counts.put("totalVerified",
                pendingMedicineRepository.countByResolvedTrue());

        counts.put("totalRejected",
                pendingMedicineRepository.countByRejectedTrue());

        return counts;
    }

    // ─────────────────────────────────────────
    // MAPPER
    // ─────────────────────────────────────────

    private PendingMedicineResponse mapToPendingMedicineResponse(
            PendingMedicine pending) {

        return PendingMedicineResponse.builder()
                .id(pending.getId())
                .donationId(pending.getDonation().getId())
                .donorName(pending.getDonation().getDonor().getName())
                .brandName(pending.getBrandName())
                .photoUrl(pending.getPhotoUrl())
                .packageInsertUrl(pending.getPackageInsertUrl())
                .doctorNotes(pending.getDoctorNotes())
                .resolvedApiName(pending.getResolvedApiName())
                .resolvedStrength(pending.getResolvedStrength())
                .resolvedDosageForm(pending.getResolvedDosageForm())
                .resolvedRoute(pending.getResolvedRoute())
                .resolvedSchedule(pending.getResolvedSchedule() != null ?
                        pending.getResolvedSchedule().name() : null)
                .resolved(pending.isResolved())
                .rejected(pending.isRejected())
                .rejectionReason(pending.getRejectionReason())
                .reviewedByPharmacistName(
                        pending.getReviewedByPharmacist() != null ?
                                pending.getReviewedByPharmacist().getName() : null)
                .submittedAt(pending.getSubmittedAt())
                .resolvedAt(pending.getResolvedAt())
                .build();
    }
}*/

package com.medicinedonation.service;

import com.medicinedonation.dto.request.PharmacistReviewRequest;
import com.medicinedonation.dto.response.PendingMedicineResponse;
import com.medicinedonation.enums.DonationStatus;
import com.medicinedonation.model.Donation;
import com.medicinedonation.model.Medicine;
import com.medicinedonation.model.PendingMedicine;
import com.medicinedonation.model.User;
import com.medicinedonation.repository.DonationRepository;
import com.medicinedonation.repository.MedicineRepository;
import com.medicinedonation.repository.PendingMedicineRepository;
import com.medicinedonation.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class PharmacistService {

    @Autowired
    private PendingMedicineRepository pendingMedicineRepository;

    @Autowired
    private DonationRepository donationRepository;

    @Autowired
    private MedicineRepository medicineRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DonorService donorService;

    // ─────────────────────────────────────────
    // GET PENDING MEDICINES
    // ─────────────────────────────────────────

    public List<PendingMedicineResponse> getPendingMedicines() {
        return pendingMedicineRepository
                .findByResolvedFalseAndRejectedFalse()
                .stream()
                .map(this::mapToPendingMedicineResponse)
                .collect(Collectors.toList());
    }

    // ─────────────────────────────────────────
    // GET PENDING MEDICINE BY ID
    // ─────────────────────────────────────────

    public PendingMedicineResponse getPendingMedicineById(Long id) {
        PendingMedicine pending = pendingMedicineRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(
                        "Pending medicine not found with id: " + id));
        return mapToPendingMedicineResponse(pending);
    }

    // ─────────────────────────────────────────
    // ADD MEDICINE TO DATABASE
    // ─────────────────────────────────────────

    public PendingMedicineResponse addMedicineToDatabase(
            Long pendingMedicineId,
            PharmacistReviewRequest request,
            String pharmacistEmail) {

        PendingMedicine pending =
                pendingMedicineRepository.findById(pendingMedicineId)
                        .orElseThrow(() -> new RuntimeException(
                                "Pending medicine not found with id: "
                                        + pendingMedicineId));

        if (pending.isResolved() || pending.isRejected()) {
            throw new RuntimeException(
                    "This pending medicine has already been reviewed.");
        }

        User pharmacist = userRepository.findByEmail(pharmacistEmail)
                .orElseThrow(() -> new RuntimeException(
                        "Pharmacist not found"));

        // ✅ Use pharmacist's corrected brand name if provided, else donor's
        String finalBrandName = (request.getResolvedBrandName() != null &&
                !request.getResolvedBrandName().isBlank())
                ? request.getResolvedBrandName()
                : pending.getBrandName();

        // Check duplicate using final brand name
        boolean exists = medicineRepository
                .existsByBrandNameIgnoreCaseAndApiNameIgnoreCaseAndStrengthIgnoreCaseAndDosageFormIgnoreCaseAndRouteIgnoreCase(
                        finalBrandName,
                        request.getResolvedApiName(),
                        request.getResolvedStrength(),
                        request.getResolvedDosageForm(),
                        request.getResolvedRoute()
                );

        if (exists) {
            throw new RuntimeException(
                    "Medicine already exists in database: " +
                            finalBrandName + " " +
                            request.getResolvedStrength() + " " +
                            request.getResolvedDosageForm());
        }

        // ✅ Build medicine with corrected brand name + API name
        Medicine medicine = Medicine.builder()
                .brandName(finalBrandName)
                .apiName(request.getResolvedApiName())
                .strength(request.getResolvedStrength())
                .dosageForm(request.getResolvedDosageForm())
                .route(request.getResolvedRoute())
                .schedule(request.getResolvedSchedule())
                .pharmacistVerified(true)
                .verifiedBy(pharmacist)
                .build();

        Medicine savedMedicine = medicineRepository.save(medicine);

        pending.setResolvedApiName(request.getResolvedApiName());
        pending.setResolvedStrength(request.getResolvedStrength());
        pending.setResolvedDosageForm(request.getResolvedDosageForm());
        pending.setResolvedRoute(request.getResolvedRoute());
        pending.setResolvedSchedule(request.getResolvedSchedule());
        pending.setResolved(true);
        pending.setReviewedByPharmacist(pharmacist);
        pending.setResolvedAt(LocalDateTime.now());

        pendingMedicineRepository.save(pending);

        Donation donation = pending.getDonation();
        donation.setMedicine(savedMedicine);
        donation.setStatus(DonationStatus.PENDING_DOCTOR_RECHECK);
        donationRepository.save(donation);

        return mapToPendingMedicineResponse(pending);
    }

    // ─────────────────────────────────────────
    // REJECT MEDICINE
    // ─────────────────────────────────────────

    public PendingMedicineResponse rejectMedicine(
            Long pendingMedicineId,
            PharmacistReviewRequest request,
            String pharmacistEmail) {

        PendingMedicine pending =
                pendingMedicineRepository.findById(pendingMedicineId)
                        .orElseThrow(() -> new RuntimeException(
                                "Pending medicine not found with id: "
                                        + pendingMedicineId));

        if (pending.isResolved() || pending.isRejected()) {
            throw new RuntimeException(
                    "This pending medicine has already been reviewed.");
        }

        User pharmacist = userRepository.findByEmail(pharmacistEmail)
                .orElseThrow(() -> new RuntimeException(
                        "Pharmacist not found"));

        pending.setRejected(true);
        pending.setRejectionReason(request.getRejectionReason());
        pending.setReviewedByPharmacist(pharmacist);
        pending.setResolvedAt(LocalDateTime.now());

        pendingMedicineRepository.save(pending);

        Donation donation = pending.getDonation();
        donation.setPharmacistRejectionReason(request.getRejectionReason());
        donation.setStatus(DonationStatus.PHARMACIST_REJECTED);
        donationRepository.save(donation);

        return mapToPendingMedicineResponse(pending);
    }

    // ─────────────────────────────────────────
    // GET RESOLUTION HISTORY
    // ─────────────────────────────────────────

    public List<PendingMedicineResponse> getResolutionHistory(
            String pharmacistEmail) {

        User pharmacist = userRepository.findByEmail(pharmacistEmail)
                .orElseThrow(() -> new RuntimeException(
                        "Pharmacist not found"));

        return pendingMedicineRepository.findAll()
                .stream()
                .filter(p -> p.getReviewedByPharmacist() != null &&
                        p.getReviewedByPharmacist().getId()
                                .equals(pharmacist.getId()))
                .map(this::mapToPendingMedicineResponse)
                .collect(Collectors.toList());
    }

    // ─────────────────────────────────────────
    // DASHBOARD COUNTS
    // ─────────────────────────────────────────

    public Map<String, Long> getDashboardCounts() {
        Map<String, Long> counts = new HashMap<>();

        counts.put("pendingMedicines",
                pendingMedicineRepository
                        .countByResolvedFalseAndRejectedFalse());

        counts.put("totalVerified",
                pendingMedicineRepository.countByResolvedTrue());

        counts.put("totalRejected",
                pendingMedicineRepository.countByRejectedTrue());

        return counts;
    }

    // ─────────────────────────────────────────
    // MAPPER — ✅ UPDATED with donorStrength + donorDosageForm
    // ─────────────────────────────────────────

    private PendingMedicineResponse mapToPendingMedicineResponse(
            PendingMedicine pending) {

        return PendingMedicineResponse.builder()
                .id(pending.getId())
                .donationId(pending.getDonation().getId())
                .donorName(pending.getDonation().getDonor().getName())
                .brandName(pending.getBrandName())
                .photoUrl(pending.getPhotoUrl())
                .packageInsertUrl(pending.getPackageInsertUrl())
                .doctorNotes(pending.getDoctorNotes())
                // ✅ NEW — donor submitted dosage info from donation
                .donorStrength(pending.getDonation().getStrength())
                .donorDosageForm(pending.getDonation().getDosageForm())
                .resolvedApiName(pending.getResolvedApiName())
                .resolvedStrength(pending.getResolvedStrength())
                .resolvedDosageForm(pending.getResolvedDosageForm())
                .resolvedRoute(pending.getResolvedRoute())
                .resolvedSchedule(pending.getResolvedSchedule() != null ?
                        pending.getResolvedSchedule().name() : null)
                .resolved(pending.isResolved())
                .rejected(pending.isRejected())
                .rejectionReason(pending.getRejectionReason())
                .reviewedByPharmacistName(
                        pending.getReviewedByPharmacist() != null ?
                                pending.getReviewedByPharmacist().getName() : null)
                .submittedAt(pending.getSubmittedAt())
                .resolvedAt(pending.getResolvedAt())
                .build();
    }
}