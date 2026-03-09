package com.medicinedonation.controller;

import com.medicinedonation.dto.request.DonationRequest;
import com.medicinedonation.dto.response.ApiResponse;
import com.medicinedonation.service.DonorService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/donor")
@PreAuthorize("hasRole('DONOR')")
public class DonorController {

    @Autowired
    private DonorService donorService;

    // Submit donation
    @PostMapping("/donations")
    public ResponseEntity<?> submitDonation(
            @Valid @RequestBody DonationRequest request,
            Authentication authentication) {
        try {
            String email = authentication.getName();
            return ResponseEntity.ok(ApiResponse.success(
                    "Donation submitted successfully",
                    donorService.submitDonation(request, email)));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    // Get my donations
    @GetMapping("/donations")
    public ResponseEntity<?> getMyDonations(
            Authentication authentication) {
        try {
            String email = authentication.getName();
            return ResponseEntity.ok(ApiResponse.success(
                    "Donations retrieved",
                    donorService.getMyDonations(email)));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    // Get donation by ID
    @GetMapping("/donations/{id}")
    public ResponseEntity<?> getDonationById(
            @PathVariable Long id,
            Authentication authentication) {
        try {
            String email = authentication.getName();
            return ResponseEntity.ok(ApiResponse.success(
                    "Donation retrieved",
                    donorService.getDonationById(id, email)));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    // Get active collection points
    @GetMapping("/collection-points")
    public ResponseEntity<?> getCollectionPoints() {
        try {
            return ResponseEntity.ok(ApiResponse.success(
                    "Collection points retrieved",
                    donorService.getActiveCollectionPoints()));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    // Get donation counts — for dashboard
    @GetMapping("/dashboard/counts")
    public ResponseEntity<?> getDashboardCounts(
            Authentication authentication) {
        try {
            String email = authentication.getName();
            return ResponseEntity.ok(ApiResponse.success(
                    "Counts retrieved",
                    donorService.getDonationCounts(email)));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }
}