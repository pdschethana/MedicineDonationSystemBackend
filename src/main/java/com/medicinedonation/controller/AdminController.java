package com.medicinedonation.controller;

import com.medicinedonation.dto.request.AdminRegisterRequest;
import com.medicinedonation.dto.request.CollectionPointRequest;
import com.medicinedonation.dto.request.MedicineRequest;
import com.medicinedonation.dto.response.ApiResponse;
import com.medicinedonation.enums.Role;
import com.medicinedonation.service.AdminService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    @Autowired
    private AdminService adminService;

    // ─────────────────────────────────────────
    // USER MANAGEMENT
    // ─────────────────────────────────────────

    // Register Doctor
    @PostMapping("/doctors")
    public ResponseEntity<?> registerDoctor(
            @Valid @RequestBody AdminRegisterRequest request) {
        try {
            return ResponseEntity.ok(ApiResponse.success(
                    "Doctor registered successfully",
                    adminService.registerDoctor(request)));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    // Register Pharmacist
    @PostMapping("/pharmacists")
    public ResponseEntity<?> registerPharmacist(
            @Valid @RequestBody AdminRegisterRequest request) {
        try {
            return ResponseEntity.ok(ApiResponse.success(
                    "Pharmacist registered successfully",
                    adminService.registerPharmacist(request)));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    // Register Collection Point Admin
    @PostMapping("/collection-point-admins")
    public ResponseEntity<?> registerCollectionPointAdmin(
            @Valid @RequestBody AdminRegisterRequest request) {
        try {
            return ResponseEntity.ok(ApiResponse.success(
                    "Collection point admin registered successfully",
                    adminService.registerCollectionPointAdmin(request)));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    // Get all users by role
    @GetMapping("/users/{role}")
    public ResponseEntity<?> getUsersByRole(
            @PathVariable String role) {
        try {
            Role userRole = Role.valueOf(role.toUpperCase());
            return ResponseEntity.ok(ApiResponse.success(
                    "Users retrieved",
                    adminService.getUsersByRole(userRole)));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    // Get user by ID
    @GetMapping("/users/detail/{id}")
    public ResponseEntity<?> getUserById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(ApiResponse.success(
                    "User retrieved",
                    adminService.getUserById(id)));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    // Delete user
    @DeleteMapping("/users/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        try {
            adminService.deleteUser(id);
            return ResponseEntity.ok(
                    ApiResponse.success("User deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    // Activate user
    @PutMapping("/users/{id}/activate")
    public ResponseEntity<?> activateUser(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(ApiResponse.success(
                    "User activated",
                    adminService.activateUser(id)));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    // Deactivate user
    @PutMapping("/users/{id}/deactivate")
    public ResponseEntity<?> deactivateUser(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(ApiResponse.success(
                    "User deactivated",
                    adminService.deactivateUser(id)));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    // ─────────────────────────────────────────
    // COLLECTION POINT MANAGEMENT
    // ─────────────────────────────────────────

    // Add collection point
    @PostMapping("/collection-points")
    public ResponseEntity<?> addCollectionPoint(
            @Valid @RequestBody CollectionPointRequest request) {
        try {
            return ResponseEntity.ok(ApiResponse.success(
                    "Collection point added successfully",
                    adminService.addCollectionPoint(request)));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    // Edit collection point
    @PutMapping("/collection-points/{id}")
    public ResponseEntity<?> editCollectionPoint(
            @PathVariable Long id,
            @Valid @RequestBody CollectionPointRequest request) {
        try {
            return ResponseEntity.ok(ApiResponse.success(
                    "Collection point updated",
                    adminService.editCollectionPoint(id, request)));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    // Delete collection point
    @DeleteMapping("/collection-points/{id}")
    public ResponseEntity<?> deleteCollectionPoint(
            @PathVariable Long id) {
        try {
            adminService.deleteCollectionPoint(id);
            return ResponseEntity.ok(
                    ApiResponse.success(
                            "Collection point deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    // Get all collection points
    @GetMapping("/collection-points")
    public ResponseEntity<?> getAllCollectionPoints() {
        try {
            return ResponseEntity.ok(ApiResponse.success(
                    "Collection points retrieved",
                    adminService.getAllCollectionPoints()));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    // Assign admin to collection point
    @PutMapping("/collection-points/{pointId}/assign/{adminId}")
    public ResponseEntity<?> assignAdmin(
            @PathVariable Long pointId,
            @PathVariable Long adminId) {
        try {
            return ResponseEntity.ok(ApiResponse.success(
                    "Admin assigned to collection point",
                    adminService.assignAdminToCollectionPoint(
                            pointId, adminId)));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    // Remove admin from collection point
    @PutMapping("/collection-points/{pointId}/remove-admin")
    public ResponseEntity<?> removeAdmin(
            @PathVariable Long pointId) {
        try {
            return ResponseEntity.ok(ApiResponse.success(
                    "Admin removed from collection point",
                    adminService.removeAdminFromCollectionPoint(pointId)));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    // ─────────────────────────────────────────
    // MEDICINE DATABASE MANAGEMENT
    // ─────────────────────────────────────────

    // Add medicine
    @PostMapping("/medicines")
    public ResponseEntity<?> addMedicine(
            @Valid @RequestBody MedicineRequest request) {
        try {
            return ResponseEntity.ok(ApiResponse.success(
                    "Medicine added to database",
                    adminService.addMedicine(request)));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    // Edit medicine
    @PutMapping("/medicines/{id}")
    public ResponseEntity<?> editMedicine(
            @PathVariable Long id,
            @Valid @RequestBody MedicineRequest request) {
        try {
            return ResponseEntity.ok(ApiResponse.success(
                    "Medicine updated",
                    adminService.editMedicine(id, request)));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    // Delete medicine
    @DeleteMapping("/medicines/{id}")
    public ResponseEntity<?> deleteMedicine(@PathVariable Long id) {
        try {
            adminService.deleteMedicine(id);
            return ResponseEntity.ok(
                    ApiResponse.success("Medicine deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    // Get all medicines
    @GetMapping("/medicines")
    public ResponseEntity<?> getAllMedicines() {
        try {
            return ResponseEntity.ok(ApiResponse.success(
                    "Medicines retrieved",
                    adminService.getAllMedicines()));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    // Get pharmacist added medicines
    @GetMapping("/medicines/pharmacist-added")
    public ResponseEntity<?> getPharmacistAddedMedicines() {
        try {
            return ResponseEntity.ok(ApiResponse.success(
                    "Pharmacist added medicines retrieved",
                    adminService.getPharmacistAddedMedicines()));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }
}