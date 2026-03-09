package com.medicinedonation.controller;

import com.medicinedonation.dto.request.PharmacistReviewRequest;
import com.medicinedonation.dto.response.ApiResponse;
import com.medicinedonation.service.PharmacistService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/pharmacist")
@PreAuthorize("hasRole('PHARMACIST')")
public class PharmacistController {

    @Autowired
    private PharmacistService pharmacistService;

    // Get dashboard counts
    @GetMapping("/dashboard/counts")
    public ResponseEntity<?> getDashboardCounts() {
        try {
            return ResponseEntity.ok(ApiResponse.success(
                    "Counts retrieved",
                    pharmacistService.getDashboardCounts()));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    // Get pending medicines list
    @GetMapping("/pending-medicines")
    public ResponseEntity<?> getPendingMedicines() {
        try {
            return ResponseEntity.ok(ApiResponse.success(
                    "Pending medicines retrieved",
                    pharmacistService.getPendingMedicines()));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    // Get pending medicine by ID
    @GetMapping("/pending-medicines/{id}")
    public ResponseEntity<?> getPendingMedicineById(
            @PathVariable Long id) {
        try {
            return ResponseEntity.ok(ApiResponse.success(
                    "Pending medicine retrieved",
                    pharmacistService.getPendingMedicineById(id)));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    // Add medicine to database
    @PostMapping("/pending-medicines/{id}/approve")
    public ResponseEntity<?> addMedicineToDatabase(
            @PathVariable Long id,
            @RequestBody PharmacistReviewRequest request,
            Authentication authentication) {
        try {
            String email = authentication.getName();
            return ResponseEntity.ok(ApiResponse.success(
                    "Medicine added to database successfully. " +
                            "Donation sent back to doctor for recheck.",
                    pharmacistService.addMedicineToDatabase(
                            id, request, email)));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    // Reject medicine
    @PostMapping("/pending-medicines/{id}/reject")
    public ResponseEntity<?> rejectMedicine(
            @PathVariable Long id,
            @RequestBody PharmacistReviewRequest request,
            Authentication authentication) {
        try {
            String email = authentication.getName();
            return ResponseEntity.ok(ApiResponse.success(
                    "Medicine rejected. Doctor will make final decision.",
                    pharmacistService.rejectMedicine(
                            id, request, email)));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    // Get resolution history
    @GetMapping("/history")
    public ResponseEntity<?> getResolutionHistory(
            Authentication authentication) {
        try {
            String email = authentication.getName();
            return ResponseEntity.ok(ApiResponse.success(
                    "History retrieved",
                    pharmacistService.getResolutionHistory(email)));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }
}