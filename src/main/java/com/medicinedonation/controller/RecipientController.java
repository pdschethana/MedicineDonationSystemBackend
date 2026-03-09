/*package com.medicinedonation.controller;

import com.medicinedonation.dto.response.ApiResponse;
import com.medicinedonation.service.RecipientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/recipient")
@PreAuthorize("hasRole('RECIPIENT')")
public class RecipientController {

    @Autowired
    private RecipientService recipientService;

    // Search by brand name
    @GetMapping("/search/brand")
    public ResponseEntity<?> searchByBrandName(
            @RequestParam String name) {
        try {
            return ResponseEntity.ok(ApiResponse.success(
                    "Search completed",
                    recipientService.searchByBrandName(name)));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    // Search by API name
    @GetMapping("/search/api")
    public ResponseEntity<?> searchByApiName(
            @RequestParam String name) {
        try {
            return ResponseEntity.ok(ApiResponse.success(
                    "Search completed",
                    recipientService.searchByApiName(name)));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    // Request medicine
    @PostMapping("/request/{donationId}")
    public ResponseEntity<?> requestMedicine(
            @PathVariable Long donationId,
            Authentication authentication) {
        try {
            String email = authentication.getName();
            return ResponseEntity.ok(ApiResponse.success(
                    "Medicine requested successfully. " +
                            "Please collect from collection point.",
                    recipientService.requestMedicine(
                            donationId, email)));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    // Get my requests
    @GetMapping("/requests")
    public ResponseEntity<?> getMyRequests(
            Authentication authentication) {
        try {
            String email = authentication.getName();
            return ResponseEntity.ok(ApiResponse.success(
                    "Requests retrieved",
                    recipientService.getMyRequests(email)));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    // Get request by ID
    @GetMapping("/requests/{donationId}")
    public ResponseEntity<?> getRequestById(
            @PathVariable Long donationId,
            Authentication authentication) {
        try {
            String email = authentication.getName();
            return ResponseEntity.ok(ApiResponse.success(
                    "Request retrieved",
                    recipientService.getRequestById(
                            donationId, email)));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    // Get collection points
    @GetMapping("/collection-points")
    public ResponseEntity<?> getCollectionPoints() {
        try {
            return ResponseEntity.ok(ApiResponse.success(
                    "Collection points retrieved",
                    recipientService.getCollectionPoints()));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    // Get dashboard counts
    @GetMapping("/dashboard/counts")
    public ResponseEntity<?> getDashboardCounts(
            Authentication authentication) {
        try {
            String email = authentication.getName();
            return ResponseEntity.ok(ApiResponse.success(
                    "Counts retrieved",
                    recipientService.getDashboardCounts(email)));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }
}*/

package com.medicinedonation.controller;

import com.medicinedonation.dto.response.ApiResponse;
import com.medicinedonation.service.RecipientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/recipient")
@PreAuthorize("hasRole('RECIPIENT')")
public class RecipientController {

    @Autowired
    private RecipientService recipientService;

    // Search by brand name
    @GetMapping("/search/brand")
    public ResponseEntity<?> searchByBrandName(
            @RequestParam String name) {
        try {
            return ResponseEntity.ok(ApiResponse.success(
                    "Search completed",
                    recipientService.searchByBrandName(name)));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    // Search by API name
    @GetMapping("/search/api")
    public ResponseEntity<?> searchByApiName(
            @RequestParam String name) {
        try {
            return ResponseEntity.ok(ApiResponse.success(
                    "Search completed",
                    recipientService.searchByApiName(name)));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    // Request medicine
    @PostMapping("/request/{donationId}")
    public ResponseEntity<?> requestMedicine(
            @PathVariable Long donationId,
            Authentication authentication) {
        try {
            String email = authentication.getName();
            return ResponseEntity.ok(ApiResponse.success(
                    "Medicine requested successfully. " +
                            "Please collect from collection point.",
                    recipientService.requestMedicine(
                            donationId, email)));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    // Get my requests
    @GetMapping("/requests")
    public ResponseEntity<?> getMyRequests(
            Authentication authentication) {
        try {
            String email = authentication.getName();
            return ResponseEntity.ok(ApiResponse.success(
                    "Requests retrieved",
                    recipientService.getMyRequests(email)));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    // Get request by ID
    @GetMapping("/requests/{donationId}")
    public ResponseEntity<?> getRequestById(
            @PathVariable Long donationId,
            Authentication authentication) {
        try {
            String email = authentication.getName();
            return ResponseEntity.ok(ApiResponse.success(
                    "Request retrieved",
                    recipientService.getRequestById(
                            donationId, email)));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    // Get collection points — accessible by ALL authenticated roles
    @GetMapping("/collection-points")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getCollectionPoints() {
        try {
            return ResponseEntity.ok(ApiResponse.success(
                    "Collection points retrieved",
                    recipientService.getCollectionPoints()));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    // Get dashboard counts
    @GetMapping("/dashboard/counts")
    public ResponseEntity<?> getDashboardCounts(
            Authentication authentication) {
        try {
            String email = authentication.getName();
            return ResponseEntity.ok(ApiResponse.success(
                    "Counts retrieved",
                    recipientService.getDashboardCounts(email)));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }
}
