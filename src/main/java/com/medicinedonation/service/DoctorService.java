/*package com.medicinedonation.service;

import com.medicinedonation.dto.request.SendToPharmacistRequest;
import com.medicinedonation.dto.request.VerificationRequest;
import com.medicinedonation.dto.response.DonationResponse;
import com.medicinedonation.enums.DonationStatus;
import com.medicinedonation.model.Donation;
import com.medicinedonation.model.PendingMedicine;
import com.medicinedonation.model.User;
import com.medicinedonation.repository.DonationRepository;
import com.medicinedonation.repository.PendingMedicineRepository;
import com.medicinedonation.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

@Service
public class DoctorService {

    @Autowired
    private DonationRepository donationRepository;

    @Autowired
    private PendingMedicineRepository pendingMedicineRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DonorService donorService;

    // ─────────────────────────────────────────
    // GET PENDING DONATIONS
    // First review — PENDING_DOCTOR status
    // ─────────────────────────────────────────

    public List<DonationResponse> getPendingDonations() {
        return donationRepository
                .findByStatus(DonationStatus.PENDING_DOCTOR)
                .stream()
                .map(this::mapToDonationResponseWithFlag)
                .collect(Collectors.toList());
    }

    // ─────────────────────────────────────────
    // GET RECHECK LIST
    // Pharmacist added medicine to DB
    // Back to doctor for final approval
    // ─────────────────────────────────────────

    public List<DonationResponse> getRecheckList() {
        return donationRepository
                .findByStatus(DonationStatus.PENDING_DOCTOR_RECHECK)
                .stream()
                .map(this::mapToDonationResponseWithFlag)
                .collect(Collectors.toList());
    }

    // ─────────────────────────────────────────
    // GET PHARMACIST REJECTED LIST
    // Pharmacist rejected — doctor decides
    // ─────────────────────────────────────────

    public List<DonationResponse> getPharmacistRejectedList() {
        return donationRepository
                .findByStatus(DonationStatus.PHARMACIST_REJECTED)
                .stream()
                .map(this::mapToDonationResponseWithFlag)
                .collect(Collectors.toList());
    }

    // ─────────────────────────────────────────
    // GET DONATION BY ID
    // ─────────────────────────────────────────

    public DonationResponse getDonationById(Long id) {
        Donation donation = donationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(
                        "Donation not found with id: " + id));
        return mapToDonationResponseWithFlag(donation);
    }

    // ─────────────────────────────────────────
    // APPROVE DONATION
    // ─────────────────────────────────────────

    public DonationResponse approveDonation(
            Long donationId,
            VerificationRequest request,
            String doctorEmail) {

        Donation donation = donationRepository.findById(donationId)
                .orElseThrow(() -> new RuntimeException(
                        "Donation not found"));

        // Only allow approve from these statuses
        if (donation.getStatus() != DonationStatus.PENDING_DOCTOR &&
                donation.getStatus() != DonationStatus.PENDING_DOCTOR_RECHECK) {
            throw new RuntimeException(
                    "Donation cannot be approved from status: " +
                            donation.getStatus());
        }

        // Medicine must be in DB before approving
        if (donation.getMedicine() == null) {
            throw new RuntimeException(
                    "Cannot approve — medicine not in database. " +
                            "Please send to pharmacist first.");
        }

        User doctor = userRepository.findByEmail(doctorEmail)
                .orElseThrow(() -> new RuntimeException(
                        "Doctor not found"));

        donation.setStatus(DonationStatus.DOCTOR_APPROVED);
        donation.setApprovedByDoctor(doctor);
        donation.setDoctorNotes(request.getNotes());

        return mapToDonationResponseWithFlag(
                donationRepository.save(donation));
    }

    // ─────────────────────────────────────────
    // REJECT DONATION
    // ─────────────────────────────────────────

    public DonationResponse rejectDonation(
            Long donationId,
            VerificationRequest request,
            String doctorEmail) {

        Donation donation = donationRepository.findById(donationId)
                .orElseThrow(() -> new RuntimeException(
                        "Donation not found"));

        // Allow reject from multiple statuses
        if (donation.getStatus() != DonationStatus.PENDING_DOCTOR &&
                donation.getStatus() != DonationStatus.PENDING_DOCTOR_RECHECK &&
                donation.getStatus() != DonationStatus.PHARMACIST_REJECTED) {
            throw new RuntimeException(
                    "Donation cannot be rejected from status: " +
                            donation.getStatus());
        }

        User doctor = userRepository.findByEmail(doctorEmail)
                .orElseThrow(() -> new RuntimeException(
                        "Doctor not found"));

        donation.setStatus(DonationStatus.REJECTED_BY_DOCTOR);
        donation.setApprovedByDoctor(doctor);
        donation.setRejectionReason(request.getRejectionReason());
        donation.setDoctorNotes(request.getNotes());

        return mapToDonationResponseWithFlag(
                donationRepository.save(donation));
    }

    // ─────────────────────────────────────────
    // SEND TO PHARMACIST
    // Only when medicine NOT in DB
    // ─────────────────────────────────────────

    public DonationResponse sendToPharmacist(
            Long donationId,
            SendToPharmacistRequest request,
            String doctorEmail) {

        Donation donation = donationRepository.findById(donationId)
                .orElseThrow(() -> new RuntimeException(
                        "Donation not found"));

        // Only from PENDING_DOCTOR status
        if (donation.getStatus() != DonationStatus.PENDING_DOCTOR) {
            throw new RuntimeException(
                    "Can only send to pharmacist from PENDING_DOCTOR status");
        }

        // Medicine must NOT be in DB
        if (donation.getMedicine() != null) {
            throw new RuntimeException(
                    "Medicine is already in database. " +
                            "Please approve or reject directly.");
        }

        User doctor = userRepository.findByEmail(doctorEmail)
                .orElseThrow(() -> new RuntimeException(
                        "Doctor not found"));

        // Create pending medicine entry for pharmacist
        PendingMedicine pendingMedicine = PendingMedicine.builder()
                .donation(donation)
                .brandName(donation.getBrandNameSubmitted())
                .photoUrl(donation.getPhotoUrl())
                .packageInsertUrl(donation.getPackageProofUrl())
                .doctorNotes(request.getDoctorNotes())
                .resolved(false)
                .rejected(false)
                .build();

        pendingMedicineRepository.save(pendingMedicine);

        // Update donation status
        donation.setStatus(DonationStatus.PENDING_PHARMACIST);
        donation.setApprovedByDoctor(doctor);
        donation.setDoctorNotes(request.getDoctorNotes());

        return mapToDonationResponseWithFlag(
                donationRepository.save(donation));
    }

    // ─────────────────────────────────────────
    // GET VERIFICATION HISTORY
    // All donations doctor has acted on
    // ─────────────────────────────────────────

    public List<DonationResponse> getVerificationHistory(
            String doctorEmail) {

        User doctor = userRepository.findByEmail(doctorEmail)
                .orElseThrow(() -> new RuntimeException(
                        "Doctor not found"));

        return donationRepository.findAll()
                .stream()
                .filter(d -> d.getApprovedByDoctor() != null &&
                        d.getApprovedByDoctor().getId()
                                .equals(doctor.getId()))
                .map(this::mapToDonationResponseWithFlag)
                .collect(Collectors.toList());
    }

    // ─────────────────────────────────────────
    // DASHBOARD COUNTS
    // ─────────────────────────────────────────

    public Map<String, Long> getDashboardCounts() {
        Map<String, Long> counts = new HashMap<>();

        counts.put("pendingApprovals",
                donationRepository.countByStatus(
                        DonationStatus.PENDING_DOCTOR));
        counts.put("needsRecheck",
                donationRepository.countByStatus(
                        DonationStatus.PENDING_DOCTOR_RECHECK));
        counts.put("pharmacistRejected",
                donationRepository.countByStatus(
                        DonationStatus.PHARMACIST_REJECTED));

        return counts;
    }

    // ─────────────────────────────────────────
    // MAPPER — with not in DB flag
    // ─────────────────────────────────────────

    private DonationResponse mapToDonationResponseWithFlag(
            Donation donation) {

        DonationResponse response =
                donorService.mapToDonationResponse(donation);

        // Add not in DB warning flag
        // If medicine is null → not in DB
        if (donation.getMedicine() == null) {
            response.setDoctorNotes(
                    (response.getDoctorNotes() != null ?
                            response.getDoctorNotes() + " | " : "") +
                            "⚠️ MEDICINE NOT IN DATABASE"
            );
        }

        return response;
    }
}*/

/*package com.medicinedonation.service;

import com.medicinedonation.dto.request.SendToPharmacistRequest;
import com.medicinedonation.dto.request.VerificationRequest;
import com.medicinedonation.dto.response.DonationResponse;
import com.medicinedonation.enums.DonationStatus;
import com.medicinedonation.model.Donation;
import com.medicinedonation.model.PendingMedicine;
import com.medicinedonation.model.User;
import com.medicinedonation.repository.DonationRepository;
import com.medicinedonation.repository.PendingMedicineRepository;
import com.medicinedonation.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

@Service
public class DoctorService {

    @Autowired
    private DonationRepository donationRepository;

    @Autowired
    private PendingMedicineRepository pendingMedicineRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DonorService donorService;

    // ─────────────────────────────────────────
    // GET PENDING DONATIONS
    // First review — PENDING_DOCTOR status
    // ─────────────────────────────────────────

    public List<DonationResponse> getPendingDonations() {
        return donationRepository
                .findByStatus(DonationStatus.PENDING_DOCTOR)
                .stream()
                .map(this::mapToDonationResponseWithFlag)
                .collect(Collectors.toList());
    }

    // ─────────────────────────────────────────
    // GET RECHECK LIST
    // Pharmacist added medicine to DB
    // Back to doctor for final approval
    // ─────────────────────────────────────────

    public List<DonationResponse> getRecheckList() {
        return donationRepository
                .findByStatus(DonationStatus.PENDING_DOCTOR_RECHECK)
                .stream()
                .map(this::mapToDonationResponseWithFlag)
                .collect(Collectors.toList());
    }

    // ─────────────────────────────────────────
    // GET PHARMACIST REJECTED LIST
    // Pharmacist rejected — doctor decides
    // ─────────────────────────────────────────

    public List<DonationResponse> getPharmacistRejectedList() {
        return donationRepository
                .findByStatus(DonationStatus.PHARMACIST_REJECTED)
                .stream()
                .map(this::mapToDonationResponseWithFlag)
                .collect(Collectors.toList());
    }

    // ─────────────────────────────────────────
    // GET DONATION BY ID
    // ─────────────────────────────────────────

    public DonationResponse getDonationById(Long id) {
        Donation donation = donationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(
                        "Donation not found with id: " + id));
        return mapToDonationResponseWithFlag(donation);
    }

    // ─────────────────────────────────────────
    // APPROVE DONATION
    // ─────────────────────────────────────────

    public DonationResponse approveDonation(
            Long donationId,
            VerificationRequest request,
            String doctorEmail) {

        // Find donation first — with clear error message
        Donation donation = donationRepository.findById(donationId)
                .orElseThrow(() -> new RuntimeException(
                        "Donation not found with id: " + donationId));

        // Check status — only allow from these two statuses
        if (donation.getStatus() != DonationStatus.PENDING_DOCTOR &&
                donation.getStatus() != DonationStatus.PENDING_DOCTOR_RECHECK) {
            throw new RuntimeException(
                    "Cannot approve donation. Current status is: " +
                            donation.getStatus().name() +
                            ". Donation must be in PENDING_DOCTOR or " +
                            "PENDING_DOCTOR_RECHECK status.");
        }

        // Medicine must be in DB before approving
        if (donation.getMedicine() == null) {
            throw new RuntimeException(
                    "Cannot approve — medicine not in database. " +
                            "Please send to pharmacist first.");
        }

        User doctor = userRepository.findByEmail(doctorEmail)
                .orElseThrow(() -> new RuntimeException(
                        "Doctor not found"));

        donation.setStatus(DonationStatus.DOCTOR_APPROVED);
        donation.setApprovedByDoctor(doctor);
        donation.setDoctorNotes(request.getNotes());

        return mapToDonationResponseWithFlag(
                donationRepository.save(donation));
    }

    // ─────────────────────────────────────────
    // REJECT DONATION
    // ─────────────────────────────────────────

    public DonationResponse rejectDonation(
            Long donationId,
            VerificationRequest request,
            String doctorEmail) {

        // Find donation — with clear error message
        Donation donation = donationRepository.findById(donationId)
                .orElseThrow(() -> new RuntimeException(
                        "Donation not found with id: " + donationId));

        // Allow reject from multiple statuses
        if (donation.getStatus() != DonationStatus.PENDING_DOCTOR &&
                donation.getStatus() != DonationStatus.PENDING_DOCTOR_RECHECK &&
                donation.getStatus() != DonationStatus.PHARMACIST_REJECTED) {
            throw new RuntimeException(
                    "Cannot reject donation. Current status is: " +
                            donation.getStatus().name() +
                            ". Donation must be in PENDING_DOCTOR, " +
                            "PENDING_DOCTOR_RECHECK or PHARMACIST_REJECTED status.");
        }

        User doctor = userRepository.findByEmail(doctorEmail)
                .orElseThrow(() -> new RuntimeException(
                        "Doctor not found"));

        donation.setStatus(DonationStatus.REJECTED_BY_DOCTOR);
        donation.setApprovedByDoctor(doctor);
        donation.setRejectionReason(request.getRejectionReason());
        donation.setDoctorNotes(request.getNotes());

        return mapToDonationResponseWithFlag(
                donationRepository.save(donation));
    }

    // ─────────────────────────────────────────
    // SEND TO PHARMACIST
    // Only when medicine NOT in DB
    // ─────────────────────────────────────────

    public DonationResponse sendToPharmacist(
            Long donationId,
            SendToPharmacistRequest request,
            String doctorEmail) {

        // Find donation — with clear error message
        Donation donation = donationRepository.findById(donationId)
                .orElseThrow(() -> new RuntimeException(
                        "Donation not found with id: " + donationId));

        // Only from PENDING_DOCTOR status
        if (donation.getStatus() != DonationStatus.PENDING_DOCTOR) {
            throw new RuntimeException(
                    "Cannot send to pharmacist. Current status is: " +
                            donation.getStatus().name() +
                            ". Donation must be in PENDING_DOCTOR status.");
        }

        // Medicine must NOT be in DB
        if (donation.getMedicine() != null) {
            throw new RuntimeException(
                    "Medicine is already in database. " +
                            "Please approve or reject directly.");
        }

        User doctor = userRepository.findByEmail(doctorEmail)
                .orElseThrow(() -> new RuntimeException(
                        "Doctor not found"));

        // Create pending medicine entry for pharmacist
        PendingMedicine pendingMedicine = PendingMedicine.builder()
                .donation(donation)
                .brandName(donation.getBrandNameSubmitted())
                .photoUrl(donation.getPhotoUrl())
                .packageInsertUrl(donation.getPackageProofUrl())
                .doctorNotes(request.getDoctorNotes())
                .resolved(false)
                .rejected(false)
                .build();

        pendingMedicineRepository.save(pendingMedicine);

        // Update donation status
        donation.setStatus(DonationStatus.PENDING_PHARMACIST);
        donation.setApprovedByDoctor(doctor);
        donation.setDoctorNotes(request.getDoctorNotes());

        return mapToDonationResponseWithFlag(
                donationRepository.save(donation));
    }

    // ─────────────────────────────────────────
    // GET VERIFICATION HISTORY
    // All donations doctor has acted on
    // ─────────────────────────────────────────

    public List<DonationResponse> getVerificationHistory(
            String doctorEmail) {

        User doctor = userRepository.findByEmail(doctorEmail)
                .orElseThrow(() -> new RuntimeException(
                        "Doctor not found"));

        return donationRepository.findAll()
                .stream()
                .filter(d -> d.getApprovedByDoctor() != null &&
                        d.getApprovedByDoctor().getId()
                                .equals(doctor.getId()))
                .map(this::mapToDonationResponseWithFlag)
                .collect(Collectors.toList());
    }

    // ─────────────────────────────────────────
    // DASHBOARD COUNTS
    // ─────────────────────────────────────────

    public Map<String, Long> getDashboardCounts() {
        Map<String, Long> counts = new HashMap<>();

        counts.put("pendingApprovals",
                donationRepository.countByStatus(
                        DonationStatus.PENDING_DOCTOR));
        counts.put("needsRecheck",
                donationRepository.countByStatus(
                        DonationStatus.PENDING_DOCTOR_RECHECK));
        counts.put("pharmacistRejected",
                donationRepository.countByStatus(
                        DonationStatus.PHARMACIST_REJECTED));

        return counts;
    }

    // ─────────────────────────────────────────
    // MAPPER — with not in DB flag
    // ─────────────────────────────────────────

    private DonationResponse mapToDonationResponseWithFlag(
            Donation donation) {

        DonationResponse response =
                donorService.mapToDonationResponse(donation);

        // Add not in DB warning flag
        // If medicine is null → not in DB
        if (donation.getMedicine() == null) {
            response.setDoctorNotes(
                    (response.getDoctorNotes() != null ?
                            response.getDoctorNotes() + " | " : "") +
                            "⚠️ MEDICINE NOT IN DATABASE"
            );
        }

        return response;
    }
}*/

/*package com.medicinedonation.service;

import com.medicinedonation.dto.request.SendToPharmacistRequest;
import com.medicinedonation.dto.request.VerificationRequest;
import com.medicinedonation.dto.response.DonationResponse;
import com.medicinedonation.enums.DonationStatus;
import com.medicinedonation.model.Donation;
import com.medicinedonation.model.PendingMedicine;
import com.medicinedonation.model.User;
import com.medicinedonation.repository.DonationRepository;
import com.medicinedonation.repository.PendingMedicineRepository;
import com.medicinedonation.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

@Service
public class DoctorService {

    @Autowired
    private DonationRepository donationRepository;

    @Autowired
    private PendingMedicineRepository pendingMedicineRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DonorService donorService;

    // ─────────────────────────────────────────
    // GET PENDING DONATIONS
    // ─────────────────────────────────────────

    public List<DonationResponse> getPendingDonations() {
        return donationRepository
                .findByStatus(DonationStatus.PENDING_DOCTOR)
                .stream()
                .map(this::mapToDonationResponseWithFlag)
                .collect(Collectors.toList());
    }

    // ─────────────────────────────────────────
    // GET RECHECK LIST
    // ─────────────────────────────────────────

    public List<DonationResponse> getRecheckList() {
        return donationRepository
                .findByStatus(DonationStatus.PENDING_DOCTOR_RECHECK)
                .stream()
                .map(this::mapToDonationResponseWithFlag)
                .collect(Collectors.toList());
    }

    // ─────────────────────────────────────────
    // GET PHARMACIST REJECTED LIST
    // ─────────────────────────────────────────

    public List<DonationResponse> getPharmacistRejectedList() {
        return donationRepository
                .findByStatus(DonationStatus.PHARMACIST_REJECTED)
                .stream()
                .map(this::mapToDonationResponseWithFlag)
                .collect(Collectors.toList());
    }

    // ─────────────────────────────────────────
    // GET DONATION BY ID
    // ─────────────────────────────────────────

    public DonationResponse getDonationById(Long id) {
        Donation donation = donationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(
                        "Donation not found with id: " + id));
        return mapToDonationResponseWithFlag(donation);
    }

    // ─────────────────────────────────────────
    // APPROVE DONATION
    // ─────────────────────────────────────────

    public DonationResponse approveDonation(
            Long donationId,
            VerificationRequest request,
            String doctorEmail) {

        Donation donation = donationRepository.findById(donationId)
                .orElseThrow(() -> new RuntimeException(
                        "Donation not found with id: " + donationId));

        if (donation.getStatus() != DonationStatus.PENDING_DOCTOR &&
                donation.getStatus() != DonationStatus.PENDING_DOCTOR_RECHECK) {
            throw new RuntimeException(
                    "Cannot approve donation. Current status is: " +
                            donation.getStatus().name());
        }

        if (donation.getMedicine() == null) {
            throw new RuntimeException(
                    "Cannot approve — medicine not in database. " +
                            "Please send to pharmacist first.");
        }

        User doctor = userRepository.findByEmail(doctorEmail)
                .orElseThrow(() -> new RuntimeException("Doctor not found"));

        donation.setStatus(DonationStatus.DOCTOR_APPROVED);
        donation.setApprovedByDoctor(doctor);
        donation.setDoctorNotes(request.getNotes());

        return mapToDonationResponseWithFlag(donationRepository.save(donation));
    }

    // ─────────────────────────────────────────
    // REJECT DONATION
    // ─────────────────────────────────────────

    public DonationResponse rejectDonation(
            Long donationId,
            VerificationRequest request,
            String doctorEmail) {

        Donation donation = donationRepository.findById(donationId)
                .orElseThrow(() -> new RuntimeException(
                        "Donation not found with id: " + donationId));

        if (donation.getStatus() != DonationStatus.PENDING_DOCTOR &&
                donation.getStatus() != DonationStatus.PENDING_DOCTOR_RECHECK &&
                donation.getStatus() != DonationStatus.PHARMACIST_REJECTED) {
            throw new RuntimeException(
                    "Cannot reject donation. Current status is: " +
                            donation.getStatus().name());
        }

        User doctor = userRepository.findByEmail(doctorEmail)
                .orElseThrow(() -> new RuntimeException("Doctor not found"));

        donation.setStatus(DonationStatus.REJECTED_BY_DOCTOR);
        donation.setApprovedByDoctor(doctor);
        donation.setRejectionReason(request.getRejectionReason());
        donation.setDoctorNotes(request.getNotes());

        return mapToDonationResponseWithFlag(donationRepository.save(donation));
    }

    // ─────────────────────────────────────────
    // SEND TO PHARMACIST
    // ─────────────────────────────────────────

    public DonationResponse sendToPharmacist(
            Long donationId,
            SendToPharmacistRequest request,
            String doctorEmail) {

        Donation donation = donationRepository.findById(donationId)
                .orElseThrow(() -> new RuntimeException(
                        "Donation not found with id: " + donationId));

        if (donation.getStatus() != DonationStatus.PENDING_DOCTOR) {
            throw new RuntimeException(
                    "Cannot send to pharmacist. Current status is: " +
                            donation.getStatus().name());
        }

        if (donation.getMedicine() != null) {
            throw new RuntimeException(
                    "Medicine is already in database. " +
                            "Please approve or reject directly.");
        }

        User doctor = userRepository.findByEmail(doctorEmail)
                .orElseThrow(() -> new RuntimeException("Doctor not found"));

        // ✅ Use getNotes() — matches frontend sending { notes: "..." }
        String doctorNotes = request.getNotes() != null
                ? request.getNotes()
                : "Please verify this medicine";

        PendingMedicine pendingMedicine = PendingMedicine.builder()
                .donation(donation)
                .brandName(donation.getBrandNameSubmitted())
                .photoUrl(donation.getPhotoUrl())
                .packageInsertUrl(donation.getPackageProofUrl())
                .doctorNotes(doctorNotes)
                .resolved(false)
                .rejected(false)
                .build();

        pendingMedicineRepository.save(pendingMedicine);

        donation.setStatus(DonationStatus.PENDING_PHARMACIST);
        donation.setApprovedByDoctor(doctor);
        donation.setDoctorNotes(doctorNotes);

        return mapToDonationResponseWithFlag(donationRepository.save(donation));
    }

    // ─────────────────────────────────────────
    // GET VERIFICATION HISTORY
    // ─────────────────────────────────────────

    public List<DonationResponse> getVerificationHistory(String doctorEmail) {
        User doctor = userRepository.findByEmail(doctorEmail)
                .orElseThrow(() -> new RuntimeException("Doctor not found"));

        return donationRepository.findAll()
                .stream()
                .filter(d -> d.getApprovedByDoctor() != null &&
                        d.getApprovedByDoctor().getId().equals(doctor.getId()))
                .map(this::mapToDonationResponseWithFlag)
                .collect(Collectors.toList());
    }

    // ─────────────────────────────────────────
    // DASHBOARD COUNTS
    // ✅ FIXED — key names now match frontend
    // ─────────────────────────────────────────

    public Map<String, Long> getDashboardCounts() {
        Map<String, Long> counts = new HashMap<>();

        // ✅ These keys match DashboardCounts interface in types/index.ts
        counts.put("pendingDoctor",
                donationRepository.countByStatus(
                        DonationStatus.PENDING_DOCTOR));

        counts.put("pendingDoctorRecheck",
                donationRepository.countByStatus(
                        DonationStatus.PENDING_DOCTOR_RECHECK));

        counts.put("pharmacistRejected",
                donationRepository.countByStatus(
                        DonationStatus.PHARMACIST_REJECTED));

        // Total acted on by any doctor
        counts.put("totalActedOn",
                donationRepository.countByStatus(DonationStatus.DOCTOR_APPROVED) +
                        donationRepository.countByStatus(DonationStatus.REJECTED_BY_DOCTOR) +
                        donationRepository.countByStatus(DonationStatus.LIVE) +
                        donationRepository.countByStatus(DonationStatus.COLLECTED));

        return counts;
    }

    // ─────────────────────────────────────────
    // MAPPER
    // ─────────────────────────────────────────

    private DonationResponse mapToDonationResponseWithFlag(Donation donation) {
        DonationResponse response = donorService.mapToDonationResponse(donation);

        if (donation.getMedicine() == null) {
            response.setDoctorNotes(
                    (response.getDoctorNotes() != null ?
                            response.getDoctorNotes() + " | " : "") +
                            "⚠️ MEDICINE NOT IN DATABASE");
        }

        return response;
    }
}*/

/*package com.medicinedonation.service;

import com.medicinedonation.dto.request.SendToPharmacistRequest;
import com.medicinedonation.dto.request.VerificationRequest;
import com.medicinedonation.dto.response.DonationResponse;
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

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class DoctorService {

    @Autowired
    private DonationRepository donationRepository;

    @Autowired
    private PendingMedicineRepository pendingMedicineRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DonorService donorService;

    @Autowired
    private MedicineRepository medicineRepository;  // ✅ NEW

    // ─────────────────────────────────────────
    // GET PENDING DONATIONS
    // ─────────────────────────────────────────

    public List<DonationResponse> getPendingDonations() {
        return donationRepository
                .findByStatus(DonationStatus.PENDING_DOCTOR)
                .stream()
                .map(this::mapToDonationResponseWithFlag)
                .collect(Collectors.toList());
    }

    // ─────────────────────────────────────────
    // GET RECHECK LIST
    // ─────────────────────────────────────────

    public List<DonationResponse> getRecheckList() {
        return donationRepository
                .findByStatus(DonationStatus.PENDING_DOCTOR_RECHECK)
                .stream()
                .map(this::mapToDonationResponseWithFlag)
                .collect(Collectors.toList());
    }

    // ─────────────────────────────────────────
    // GET PHARMACIST REJECTED LIST
    // ─────────────────────────────────────────

    public List<DonationResponse> getPharmacistRejectedList() {
        return donationRepository
                .findByStatus(DonationStatus.PHARMACIST_REJECTED)
                .stream()
                .map(this::mapToDonationResponseWithFlag)
                .collect(Collectors.toList());
    }

    // ─────────────────────────────────────────
    // GET DONATION BY ID
    // ─────────────────────────────────────────

    public DonationResponse getDonationById(Long id) {
        Donation donation = donationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(
                        "Donation not found with id: " + id));
        return mapToDonationResponseWithFlag(donation);
    }

    // ─────────────────────────────────────────
    // APPROVE DONATION — FIXED
    //
    // Flow:
    // 1. If medicine already linked (pharmacist verified) → approve directly
    // 2. Check brand + strength + dosageForm in DB
    //    → All match → link medicine + approve directly
    //    → No match  → auto send to pharmacist
    // ─────────────────────────────────────────

    public DonationResponse approveDonation(
            Long donationId,
            VerificationRequest request,
            String doctorEmail) {

        Donation donation = donationRepository.findById(donationId)
                .orElseThrow(() -> new RuntimeException(
                        "Donation not found with id: " + donationId));

        if (donation.getStatus() != DonationStatus.PENDING_DOCTOR &&
                donation.getStatus() != DonationStatus.PENDING_DOCTOR_RECHECK) {
            throw new RuntimeException(
                    "Cannot approve donation. Current status is: " +
                            donation.getStatus().name());
        }

        User doctor = userRepository.findByEmail(doctorEmail)
                .orElseThrow(() -> new RuntimeException("Doctor not found"));

        // ── Case 1: Medicine already linked by pharmacist → approve directly
        if (donation.getMedicine() != null) {
            donation.setStatus(DonationStatus.DOCTOR_APPROVED);
            donation.setApprovedByDoctor(doctor);
            donation.setDoctorNotes(request.getNotes());
            return mapToDonationResponseWithFlag(donationRepository.save(donation));
        }

        // ── Case 2: Check brand name + strength + dosage form in DB
        String brandName  = donation.getBrandNameSubmitted();
        String strength   = donation.getStrength();
        String dosageForm = donation.getDosageForm();

        Optional<Medicine> matched =
                medicineRepository
                        .findByBrandNameIgnoreCaseAndStrengthIgnoreCaseAndDosageFormIgnoreCase(
                                brandName, strength, dosageForm);

        if (matched.isPresent()) {
            // ✅ Exact match — link medicine and approve directly
            donation.setMedicine(matched.get());
            donation.setStatus(DonationStatus.DOCTOR_APPROVED);
            donation.setApprovedByDoctor(doctor);
            donation.setDoctorNotes(request.getNotes());
            return mapToDonationResponseWithFlag(donationRepository.save(donation));
        }

        // ── Case 3: No match → auto send to pharmacist
        String doctorNotes = (request.getNotes() != null && !request.getNotes().isBlank())
                ? request.getNotes()
                : "Brand name, strength or dosage form not found in database — please verify";

        // Avoid duplicate pending entries
        boolean alreadyPending = pendingMedicineRepository
                .findByResolvedFalseAndRejectedFalse()
                .stream()
                .anyMatch(p -> p.getDonation().getId().equals(donationId));

        if (!alreadyPending) {
            PendingMedicine pendingMedicine = PendingMedicine.builder()
                    .donation(donation)
                    .brandName(brandName)
                    .photoUrl(donation.getPhotoUrl())
                    .packageInsertUrl(donation.getPackageProofUrl())
                    .doctorNotes(doctorNotes)
                    .resolved(false)
                    .rejected(false)
                    .build();
            pendingMedicineRepository.save(pendingMedicine);
        }

        donation.setStatus(DonationStatus.PENDING_PHARMACIST);
        donation.setApprovedByDoctor(doctor);
        donation.setDoctorNotes(doctorNotes);

        return mapToDonationResponseWithFlag(donationRepository.save(donation));
    }

    // ─────────────────────────────────────────
    // REJECT DONATION
    // ─────────────────────────────────────────

    public DonationResponse rejectDonation(
            Long donationId,
            VerificationRequest request,
            String doctorEmail) {

        Donation donation = donationRepository.findById(donationId)
                .orElseThrow(() -> new RuntimeException(
                        "Donation not found with id: " + donationId));

        if (donation.getStatus() != DonationStatus.PENDING_DOCTOR &&
                donation.getStatus() != DonationStatus.PENDING_DOCTOR_RECHECK &&
                donation.getStatus() != DonationStatus.PHARMACIST_REJECTED) {
            throw new RuntimeException(
                    "Cannot reject donation. Current status is: " +
                            donation.getStatus().name());
        }

        User doctor = userRepository.findByEmail(doctorEmail)
                .orElseThrow(() -> new RuntimeException("Doctor not found"));

        donation.setStatus(DonationStatus.REJECTED_BY_DOCTOR);
        donation.setApprovedByDoctor(doctor);
        donation.setRejectionReason(request.getRejectionReason());
        donation.setDoctorNotes(request.getNotes());

        return mapToDonationResponseWithFlag(donationRepository.save(donation));
    }

    // ─────────────────────────────────────────
    // SEND TO PHARMACIST — manual override
    // Doctor can still manually send if needed
    // ─────────────────────────────────────────

    public DonationResponse sendToPharmacist(
            Long donationId,
            SendToPharmacistRequest request,
            String doctorEmail) {

        Donation donation = donationRepository.findById(donationId)
                .orElseThrow(() -> new RuntimeException(
                        "Donation not found with id: " + donationId));

        if (donation.getStatus() != DonationStatus.PENDING_DOCTOR) {
            throw new RuntimeException(
                    "Cannot send to pharmacist. Current status is: " +
                            donation.getStatus().name());
        }

        if (donation.getMedicine() != null) {
            throw new RuntimeException(
                    "Medicine is already in database. " +
                            "Please approve or reject directly.");
        }

        User doctor = userRepository.findByEmail(doctorEmail)
                .orElseThrow(() -> new RuntimeException("Doctor not found"));

        String doctorNotes = request.getNotes() != null
                ? request.getNotes()
                : "Please verify this medicine";

        // Avoid duplicate pending entries
        boolean alreadyPending = pendingMedicineRepository
                .findByResolvedFalseAndRejectedFalse()
                .stream()
                .anyMatch(p -> p.getDonation().getId().equals(donationId));

        if (!alreadyPending) {
            PendingMedicine pendingMedicine = PendingMedicine.builder()
                    .donation(donation)
                    .brandName(donation.getBrandNameSubmitted())
                    .photoUrl(donation.getPhotoUrl())
                    .packageInsertUrl(donation.getPackageProofUrl())
                    .doctorNotes(doctorNotes)
                    .resolved(false)
                    .rejected(false)
                    .build();
            pendingMedicineRepository.save(pendingMedicine);
        }

        donation.setStatus(DonationStatus.PENDING_PHARMACIST);
        donation.setApprovedByDoctor(doctor);
        donation.setDoctorNotes(doctorNotes);

        return mapToDonationResponseWithFlag(donationRepository.save(donation));
    }

    // ─────────────────────────────────────────
    // GET VERIFICATION HISTORY
    // ─────────────────────────────────────────

    public List<DonationResponse> getVerificationHistory(String doctorEmail) {
        User doctor = userRepository.findByEmail(doctorEmail)
                .orElseThrow(() -> new RuntimeException("Doctor not found"));

        return donationRepository.findAll()
                .stream()
                .filter(d -> d.getApprovedByDoctor() != null &&
                        d.getApprovedByDoctor().getId().equals(doctor.getId()))
                .map(this::mapToDonationResponseWithFlag)
                .collect(Collectors.toList());
    }

    // ─────────────────────────────────────────
    // DASHBOARD COUNTS
    // ─────────────────────────────────────────

    public Map<String, Long> getDashboardCounts() {
        Map<String, Long> counts = new HashMap<>();

        counts.put("pendingDoctor",
                donationRepository.countByStatus(DonationStatus.PENDING_DOCTOR));

        counts.put("pendingDoctorRecheck",
                donationRepository.countByStatus(DonationStatus.PENDING_DOCTOR_RECHECK));

        counts.put("pharmacistRejected",
                donationRepository.countByStatus(DonationStatus.PHARMACIST_REJECTED));

        counts.put("totalActedOn",
                donationRepository.countByStatus(DonationStatus.DOCTOR_APPROVED) +
                        donationRepository.countByStatus(DonationStatus.REJECTED_BY_DOCTOR) +
                        donationRepository.countByStatus(DonationStatus.LIVE) +
                        donationRepository.countByStatus(DonationStatus.COLLECTED));

        return counts;
    }

    // ─────────────────────────────────────────
    // MAPPER
    // ─────────────────────────────────────────

    private DonationResponse mapToDonationResponseWithFlag(Donation donation) {
        DonationResponse response = donorService.mapToDonationResponse(donation);

        // Show warning flag if medicine not yet linked
        if (donation.getMedicine() == null) {
            response.setDoctorNotes(
                    (response.getDoctorNotes() != null ?
                            response.getDoctorNotes() + " | " : "") +
                            "⚠️ MEDICINE NOT IN DATABASE");
        }

        return response;
    }
}*/


package com.medicinedonation.service;

import com.medicinedonation.dto.request.SendToPharmacistRequest;
import com.medicinedonation.dto.request.VerificationRequest;
import com.medicinedonation.dto.response.DonationResponse;
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

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class DoctorService {

    @Autowired
    private DonationRepository donationRepository;

    @Autowired
    private PendingMedicineRepository pendingMedicineRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DonorService donorService;

    @Autowired
    private MedicineRepository medicineRepository;

    // ─────────────────────────────────────────
    // GET PENDING DONATIONS
    // ─────────────────────────────────────────

    public List<DonationResponse> getPendingDonations() {
        return donationRepository
                .findByStatus(DonationStatus.PENDING_DOCTOR)
                .stream()
                .map(this::mapToDonationResponseWithFlag)
                .collect(Collectors.toList());
    }

    // ─────────────────────────────────────────
    // GET RECHECK LIST
    // ─────────────────────────────────────────

    public List<DonationResponse> getRecheckList() {
        return donationRepository
                .findByStatus(DonationStatus.PENDING_DOCTOR_RECHECK)
                .stream()
                .map(this::mapToDonationResponseWithFlag)
                .collect(Collectors.toList());
    }

    // ─────────────────────────────────────────
    // GET PHARMACIST REJECTED LIST
    // ─────────────────────────────────────────

    public List<DonationResponse> getPharmacistRejectedList() {
        return donationRepository
                .findByStatus(DonationStatus.PHARMACIST_REJECTED)
                .stream()
                .map(this::mapToDonationResponseWithFlag)
                .collect(Collectors.toList());
    }

    // ─────────────────────────────────────────
    // GET DONATION BY ID
    // ─────────────────────────────────────────

    public DonationResponse getDonationById(Long id) {
        Donation donation = donationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(
                        "Donation not found with id: " + id));
        return mapToDonationResponseWithFlag(donation);
    }

    // ─────────────────────────────────────────
    // APPROVE DONATION
    //
    // 1. Medicine already linked (pharmacist verified) → approve directly
    // 2. Brand + strength + dosageForm exact match in DB → link + approve
    // 3. No match → auto send to pharmacist
    // ─────────────────────────────────────────

    public DonationResponse approveDonation(
            Long donationId,
            VerificationRequest request,
            String doctorEmail) {

        Donation donation = donationRepository.findById(donationId)
                .orElseThrow(() -> new RuntimeException(
                        "Donation not found with id: " + donationId));

        if (donation.getStatus() != DonationStatus.PENDING_DOCTOR &&
                donation.getStatus() != DonationStatus.PENDING_DOCTOR_RECHECK) {
            throw new RuntimeException(
                    "Cannot approve donation. Current status is: " +
                            donation.getStatus().name());
        }

        User doctor = userRepository.findByEmail(doctorEmail)
                .orElseThrow(() -> new RuntimeException("Doctor not found"));

        // ── Case 1: Medicine already linked by pharmacist → approve directly
        if (donation.getMedicine() != null) {
            donation.setStatus(DonationStatus.DOCTOR_APPROVED);
            donation.setApprovedByDoctor(doctor);
            donation.setDoctorNotes(request.getNotes());
            return mapToDonationResponseWithFlag(donationRepository.save(donation));
        }

        // ── Case 2: Check brand + strength + dosageForm exact match in DB
        String brandName  = donation.getBrandNameSubmitted();
        String strength   = donation.getStrength();
        String dosageForm = donation.getDosageForm();

        Optional<Medicine> matched =
                medicineRepository
                        .findByBrandNameIgnoreCaseAndStrengthIgnoreCaseAndDosageFormIgnoreCase(
                                brandName, strength, dosageForm);

        if (matched.isPresent()) {
            donation.setMedicine(matched.get());
            donation.setStatus(DonationStatus.DOCTOR_APPROVED);
            donation.setApprovedByDoctor(doctor);
            donation.setDoctorNotes(request.getNotes());
            return mapToDonationResponseWithFlag(donationRepository.save(donation));
        }

        // ── Case 3: No exact match → auto send to pharmacist
        String doctorNotes = (request.getNotes() != null && !request.getNotes().isBlank())
                ? request.getNotes()
                : "Brand name, strength or dosage form not found in database — please verify";

        boolean alreadyPending = pendingMedicineRepository
                .findByResolvedFalseAndRejectedFalse()
                .stream()
                .anyMatch(p -> p.getDonation().getId().equals(donationId));

        if (!alreadyPending) {
            PendingMedicine pendingMedicine = PendingMedicine.builder()
                    .donation(donation)
                    .brandName(brandName)
                    .photoUrl(donation.getPhotoUrl())
                    .packageInsertUrl(donation.getPackageProofUrl())
                    .doctorNotes(doctorNotes)
                    .resolved(false)
                    .rejected(false)
                    .build();
            pendingMedicineRepository.save(pendingMedicine);
        }

        donation.setStatus(DonationStatus.PENDING_PHARMACIST);
        donation.setApprovedByDoctor(doctor);
        donation.setDoctorNotes(doctorNotes);

        return mapToDonationResponseWithFlag(donationRepository.save(donation));
    }

    // ─────────────────────────────────────────
    // REJECT DONATION
    // ─────────────────────────────────────────

    public DonationResponse rejectDonation(
            Long donationId,
            VerificationRequest request,
            String doctorEmail) {

        Donation donation = donationRepository.findById(donationId)
                .orElseThrow(() -> new RuntimeException(
                        "Donation not found with id: " + donationId));

        if (donation.getStatus() != DonationStatus.PENDING_DOCTOR &&
                donation.getStatus() != DonationStatus.PENDING_DOCTOR_RECHECK &&
                donation.getStatus() != DonationStatus.PHARMACIST_REJECTED) {
            throw new RuntimeException(
                    "Cannot reject donation. Current status is: " +
                            donation.getStatus().name());
        }

        User doctor = userRepository.findByEmail(doctorEmail)
                .orElseThrow(() -> new RuntimeException("Doctor not found"));

        donation.setStatus(DonationStatus.REJECTED_BY_DOCTOR);
        donation.setApprovedByDoctor(doctor);
        donation.setRejectionReason(request.getRejectionReason());
        donation.setDoctorNotes(request.getNotes());

        return mapToDonationResponseWithFlag(donationRepository.save(donation));
    }

    // ─────────────────────────────────────────
    // SEND TO PHARMACIST
    //
    // ✅ FIXED — removed the getMedicine() != null block
    // PARTIAL_MATCH has medicine linked (brand found) but
    // strength/dosageForm differ — pharmacist still needs to verify
    // ─────────────────────────────────────────

    public DonationResponse sendToPharmacist(
            Long donationId,
            SendToPharmacistRequest request,
            String doctorEmail) {

        Donation donation = donationRepository.findById(donationId)
                .orElseThrow(() -> new RuntimeException(
                        "Donation not found with id: " + donationId));

        if (donation.getStatus() != DonationStatus.PENDING_DOCTOR) {
            throw new RuntimeException(
                    "Cannot send to pharmacist. Current status is: " +
                            donation.getStatus().name());
        }

        // ✅ REMOVED: the old check that blocked PARTIAL_MATCH from going to pharmacist:
        // if (donation.getMedicine() != null) { throw new RuntimeException(...) }

        User doctor = userRepository.findByEmail(doctorEmail)
                .orElseThrow(() -> new RuntimeException("Doctor not found"));

        String doctorNotes = (request.getNotes() != null && !request.getNotes().isBlank())
                ? request.getNotes()
                : "Please verify this medicine";

        // Avoid duplicate pending entries for same donation
        boolean alreadyPending = pendingMedicineRepository
                .findByResolvedFalseAndRejectedFalse()
                .stream()
                .anyMatch(p -> p.getDonation().getId().equals(donationId));

        if (!alreadyPending) {
            PendingMedicine pendingMedicine = PendingMedicine.builder()
                    .donation(donation)
                    .brandName(donation.getBrandNameSubmitted())
                    .photoUrl(donation.getPhotoUrl())
                    .packageInsertUrl(donation.getPackageProofUrl())
                    .doctorNotes(doctorNotes)
                    .resolved(false)
                    .rejected(false)
                    .build();
            pendingMedicineRepository.save(pendingMedicine);
        }

        donation.setStatus(DonationStatus.PENDING_PHARMACIST);
        donation.setApprovedByDoctor(doctor);
        donation.setDoctorNotes(doctorNotes);

        return mapToDonationResponseWithFlag(donationRepository.save(donation));
    }

    // ─────────────────────────────────────────
    // GET VERIFICATION HISTORY
    // ─────────────────────────────────────────

    public List<DonationResponse> getVerificationHistory(String doctorEmail) {
        User doctor = userRepository.findByEmail(doctorEmail)
                .orElseThrow(() -> new RuntimeException("Doctor not found"));

        return donationRepository.findAll()
                .stream()
                .filter(d -> d.getApprovedByDoctor() != null &&
                        d.getApprovedByDoctor().getId().equals(doctor.getId()))
                .map(this::mapToDonationResponseWithFlag)
                .collect(Collectors.toList());
    }

    // ─────────────────────────────────────────
    // DASHBOARD COUNTS
    // ─────────────────────────────────────────

    public Map<String, Long> getDashboardCounts() {
        Map<String, Long> counts = new HashMap<>();

        counts.put("pendingDoctor",
                donationRepository.countByStatus(DonationStatus.PENDING_DOCTOR));

        counts.put("pendingDoctorRecheck",
                donationRepository.countByStatus(DonationStatus.PENDING_DOCTOR_RECHECK));

        counts.put("pharmacistRejected",
                donationRepository.countByStatus(DonationStatus.PHARMACIST_REJECTED));

        counts.put("totalActedOn",
                donationRepository.countByStatus(DonationStatus.DOCTOR_APPROVED) +
                        donationRepository.countByStatus(DonationStatus.REJECTED_BY_DOCTOR) +
                        donationRepository.countByStatus(DonationStatus.LIVE) +
                        donationRepository.countByStatus(DonationStatus.COLLECTED));

        return counts;
    }

    // ─────────────────────────────────────────
    // MAPPER
    // ─────────────────────────────────────────

    private DonationResponse mapToDonationResponseWithFlag(Donation donation) {
        DonationResponse response = donorService.mapToDonationResponse(donation);

        if (donation.getMedicine() == null) {
            response.setDoctorNotes(
                    (response.getDoctorNotes() != null ?
                            response.getDoctorNotes() + " | " : "") +
                            "⚠️ MEDICINE NOT IN DATABASE");
        }

        return response;
    }
}