package com.medicinedonation.controller;

import com.medicinedonation.dto.response.ApiResponse;
import com.medicinedonation.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    // Admin dashboard
    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getAdminDashboard() {
        try {
            return ResponseEntity.ok(ApiResponse.success(
                    "Admin dashboard retrieved",
                    dashboardService.getAdminDashboard()));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    // Doctor dashboard
    @GetMapping("/doctor")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<?> getDoctorDashboard(
            Authentication authentication) {
        try {
            String email = authentication.getName();
            return ResponseEntity.ok(ApiResponse.success(
                    "Doctor dashboard retrieved",
                    dashboardService.getDoctorDashboard(email)));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    // Pharmacist dashboard
    @GetMapping("/pharmacist")
    @PreAuthorize("hasRole('PHARMACIST')")
    public ResponseEntity<?> getPharmacistDashboard(
            Authentication authentication) {
        try {
            String email = authentication.getName();
            return ResponseEntity.ok(ApiResponse.success(
                    "Pharmacist dashboard retrieved",
                    dashboardService.getPharmacistDashboard(email)));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    // Donor dashboard
    @GetMapping("/donor")
    @PreAuthorize("hasRole('DONOR')")
    public ResponseEntity<?> getDonorDashboard(
            Authentication authentication) {
        try {
            String email = authentication.getName();
            return ResponseEntity.ok(ApiResponse.success(
                    "Donor dashboard retrieved",
                    dashboardService.getDonorDashboard(email)));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    // Recipient dashboard
    @GetMapping("/recipient")
    @PreAuthorize("hasRole('RECIPIENT')")
    public ResponseEntity<?> getRecipientDashboard(
            Authentication authentication) {
        try {
            String email = authentication.getName();
            return ResponseEntity.ok(ApiResponse.success(
                    "Recipient dashboard retrieved",
                    dashboardService.getRecipientDashboard(email)));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    // Collection point dashboard
    @GetMapping("/collection-point")
    @PreAuthorize("hasRole('COLLECTION_POINT')")
    public ResponseEntity<?> getCollectionPointDashboard(
            Authentication authentication) {
        try {
            String email = authentication.getName();
            return ResponseEntity.ok(ApiResponse.success(
                    "Collection point dashboard retrieved",
                    dashboardService
                            .getCollectionPointDashboard(email)));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }
}