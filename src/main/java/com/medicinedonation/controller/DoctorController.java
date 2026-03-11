/*package com.medicinedonation.controller;

import com.medicinedonation.dto.request.SendToPharmacistRequest;
import com.medicinedonation.dto.request.VerificationRequest;
import com.medicinedonation.dto.response.ApiResponse;
import com.medicinedonation.service.DoctorService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/doctor")
@PreAuthorize("hasRole('DOCTOR')")
public class DoctorController {

    @Autowired
    private DoctorService doctorService;

    // Get dashboard counts
    @GetMapping("/dashboard/counts")
    public ResponseEntity<?> getDashboardCounts() {
        try {
            return ResponseEntity.ok(ApiResponse.success(
                    "Counts retrieved",
                    doctorService.getDashboardCounts()));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    // Get pending donations — first review
    @GetMapping("/donations/pending")
    public ResponseEntity<?> getPendingDonations() {
        try {
            return ResponseEntity.ok(ApiResponse.success(
                    "Pending donations retrieved",
                    doctorService.getPendingDonations()));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    // Get recheck list — pharmacist added to DB
    @GetMapping("/donations/recheck")
    public ResponseEntity<?> getRecheckList() {
        try {
            return ResponseEntity.ok(ApiResponse.success(
                    "Recheck list retrieved",
                    doctorService.getRecheckList()));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    // Get pharmacist rejected list
    @GetMapping("/donations/pharmacist-rejected")
    public ResponseEntity<?> getPharmacistRejectedList() {
        try {
            return ResponseEntity.ok(ApiResponse.success(
                    "Pharmacist rejected list retrieved",
                    doctorService.getPharmacistRejectedList()));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    // Get donation by ID
    @GetMapping("/donations/{id}")
    public ResponseEntity<?> getDonationById(
            @PathVariable Long id) {
        try {
            return ResponseEntity.ok(ApiResponse.success(
                    "Donation retrieved",
                    doctorService.getDonationById(id)));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    // Approve donation
    @PostMapping("/donations/{id}/approve")
    public ResponseEntity<?> approveDonation(
            @PathVariable Long id,
            @RequestBody VerificationRequest request,
            Authentication authentication) {
        try {
            String email = authentication.getName();
            return ResponseEntity.ok(ApiResponse.success(
                    "Donation approved successfully",
                    doctorService.approveDonation(id, request, email)));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    // Reject donation
    @PostMapping("/donations/{id}/reject")
    public ResponseEntity<?> rejectDonation(
            @PathVariable Long id,
            @RequestBody VerificationRequest request,
            Authentication authentication) {
        try {
            String email = authentication.getName();
            return ResponseEntity.ok(ApiResponse.success(
                    "Donation rejected",
                    doctorService.rejectDonation(id, request, email)));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    // Send to pharmacist
    @PostMapping("/donations/{id}/send-to-pharmacist")
    public ResponseEntity<?> sendToPharmacist(
            @PathVariable Long id,
            @RequestBody SendToPharmacistRequest request,
            Authentication authentication) {
        try {
            String email = authentication.getName();
            return ResponseEntity.ok(ApiResponse.success(
                    "Donation sent to pharmacist",
                    doctorService.sendToPharmacist(id, request, email)));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    // Get verification history
    @GetMapping("/donations/history")
    public ResponseEntity<?> getVerificationHistory(
            Authentication authentication) {
        try {
            String email = authentication.getName();
            return ResponseEntity.ok(ApiResponse.success(
                    "History retrieved",
                    doctorService.getVerificationHistory(email)));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }
}*/


package com.medicinedonation.controller;

import com.medicinedonation.dto.request.SendToPharmacistRequest;
import com.medicinedonation.dto.request.VerificationRequest;
import com.medicinedonation.dto.response.ApiResponse;
import com.medicinedonation.service.DoctorService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/doctor")
@PreAuthorize("hasRole('DOCTOR')")
public class DoctorController {

    @Autowired
    private DoctorService doctorService;

    // Get dashboard counts
    @GetMapping("/dashboard/counts")
    public ResponseEntity<?> getDashboardCounts() {
        try {
            return ResponseEntity.ok(ApiResponse.success(
                    "Counts retrieved",
                    doctorService.getDashboardCounts()));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    // Get pending donations — first review
    @GetMapping("/donations/pending")
    public ResponseEntity<?> getPendingDonations() {
        try {
            return ResponseEntity.ok(ApiResponse.success(
                    "Pending donations retrieved",
                    doctorService.getPendingDonations()));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    // Get recheck list — pharmacist added to DB
    @GetMapping("/donations/recheck")
    public ResponseEntity<?> getRecheckList() {
        try {
            return ResponseEntity.ok(ApiResponse.success(
                    "Recheck list retrieved",
                    doctorService.getRecheckList()));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    // Get pharmacist rejected list
    @GetMapping("/donations/pharmacist-rejected")
    public ResponseEntity<?> getPharmacistRejectedList() {
        try {
            return ResponseEntity.ok(ApiResponse.success(
                    "Pharmacist rejected list retrieved",
                    doctorService.getPharmacistRejectedList()));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    // Get donation by ID
    @GetMapping("/donations/{id}")
    public ResponseEntity<?> getDonationById(
            @PathVariable Long id) {
        try {
            return ResponseEntity.ok(ApiResponse.success(
                    "Donation retrieved",
                    doctorService.getDonationById(id)));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    // Approve donation
    @PostMapping("/donations/{id}/approve")
    public ResponseEntity<?> approveDonation(
            @PathVariable Long id,
            @RequestBody VerificationRequest request,
            Authentication authentication) {
        try {
            String email = authentication.getName();
            return ResponseEntity.ok(ApiResponse.success(
                    "Donation approved successfully",
                    doctorService.approveDonation(id, request, email)));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    // Reject donation
    @PostMapping("/donations/{id}/reject")
    public ResponseEntity<?> rejectDonation(
            @PathVariable Long id,
            @RequestBody VerificationRequest request,
            Authentication authentication) {
        try {
            String email = authentication.getName();
            return ResponseEntity.ok(ApiResponse.success(
                    "Donation rejected",
                    doctorService.rejectDonation(id, request, email)));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    // Send to pharmacist
    @PostMapping("/donations/{id}/send-to-pharmacist")
    public ResponseEntity<?> sendToPharmacist(
            @PathVariable Long id,
            @RequestBody SendToPharmacistRequest request,
            Authentication authentication) {
        try {
            String email = authentication.getName();
            return ResponseEntity.ok(ApiResponse.success(
                    "Donation sent to pharmacist",
                    doctorService.sendToPharmacist(id, request, email)));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    // ✅ NEW — Save corrections and re-check DB match status
    // Doctor edits details → clicks "Re-check Database"
    // Returns updated DonationResponse with new dbMatchStatus
    @PostMapping("/donations/{id}/recheck-corrections")
    public ResponseEntity<?> recheckCorrections(
            @PathVariable Long id,
            @RequestBody VerificationRequest request,
            Authentication authentication) {
        try {
            String email = authentication.getName();
            return ResponseEntity.ok(ApiResponse.success(
                    "Corrections saved and re-checked",
                    doctorService.recheckCorrections(id, request, email)));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    // Get verification history
    @GetMapping("/donations/history")
    public ResponseEntity<?> getVerificationHistory(
            Authentication authentication) {
        try {
            String email = authentication.getName();
            return ResponseEntity.ok(ApiResponse.success(
                    "History retrieved",
                    doctorService.getVerificationHistory(email)));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }
}