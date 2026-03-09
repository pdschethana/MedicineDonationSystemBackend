package com.medicinedonation.controller;

import com.medicinedonation.dto.response.ApiResponse;
import com.medicinedonation.service.CollectionPointService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/collection-point")
@PreAuthorize("hasRole('COLLECTION_POINT')")
public class CollectionPointController {

    @Autowired
    private CollectionPointService collectionPointService;

    // Get dashboard counts
    @GetMapping("/dashboard/counts")
    public ResponseEntity<?> getDashboardCounts(
            Authentication authentication) {
        try {
            String email = authentication.getName();
            return ResponseEntity.ok(ApiResponse.success(
                    "Counts retrieved",
                    collectionPointService.getDashboardCounts(email)));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    // Get incoming donations — DOCTOR_APPROVED
    @GetMapping("/incoming")
    public ResponseEntity<?> getIncomingDonations(
            Authentication authentication) {
        try {
            String email = authentication.getName();
            return ResponseEntity.ok(ApiResponse.success(
                    "Incoming donations retrieved",
                    collectionPointService
                            .getIncomingDonations(email)));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    // Confirm physical receipt → LIVE
    @PostMapping("/confirm-receipt/{donationId}")
    public ResponseEntity<?> confirmReceipt(
            @PathVariable Long donationId,
            Authentication authentication) {
        try {
            String email = authentication.getName();
            return ResponseEntity.ok(ApiResponse.success(
                    "Receipt confirmed. Donation is now LIVE.",
                    collectionPointService
                            .confirmReceipt(donationId, email)));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    // Get current inventory — LIVE donations
    @GetMapping("/inventory")
    public ResponseEntity<?> getCurrentInventory(
            Authentication authentication) {
        try {
            String email = authentication.getName();
            return ResponseEntity.ok(ApiResponse.success(
                    "Inventory retrieved",
                    collectionPointService
                            .getCurrentInventory(email)));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    // Get requested donations — REQUESTED
    @GetMapping("/requested")
    public ResponseEntity<?> getRequestedDonations(
            Authentication authentication) {
        try {
            String email = authentication.getName();
            return ResponseEntity.ok(ApiResponse.success(
                    "Requested donations retrieved",
                    collectionPointService
                            .getRequestedDonations(email)));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    // Confirm collection → COLLECTED
    @PostMapping("/confirm-collected/{donationId}")
    public ResponseEntity<?> confirmCollection(
            @PathVariable Long donationId,
            Authentication authentication) {
        try {
            String email = authentication.getName();
            return ResponseEntity.ok(ApiResponse.success(
                    "Collection confirmed. Donation marked as COLLECTED.",
                    collectionPointService
                            .confirmCollection(donationId, email)));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    // Get collection history — COLLECTED
    @GetMapping("/history")
    public ResponseEntity<?> getCollectionHistory(
            Authentication authentication) {
        try {
            String email = authentication.getName();
            return ResponseEntity.ok(ApiResponse.success(
                    "History retrieved",
                    collectionPointService
                            .getCollectionHistory(email)));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    // Get all donations at my point
    @GetMapping("/all")
    public ResponseEntity<?> getAllMyDonations(
            Authentication authentication) {
        try {
            String email = authentication.getName();
            return ResponseEntity.ok(ApiResponse.success(
                    "All donations retrieved",
                    collectionPointService
                            .getAllMyDonations(email)));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }
}