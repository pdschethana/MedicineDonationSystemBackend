/*package com.medicinedonation.controller;

import com.medicinedonation.dto.response.ApiResponse;
import com.medicinedonation.service.MedicineService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/medicines")
public class MedicineController {

    @Autowired
    private MedicineService medicineService;

    // Get all medicines — all authenticated users
    @GetMapping
    public ResponseEntity<?> getAllMedicines() {
        try {
            return ResponseEntity.ok(ApiResponse.success(
                    "Medicines retrieved",
                    medicineService.getAllMedicines()));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    // Get medicine by ID
    @GetMapping("/{id}")
    public ResponseEntity<?> getMedicineById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(ApiResponse.success(
                    "Medicine retrieved",
                    medicineService.getMedicineById(id)));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    // Search by brand name
    @GetMapping("/search/brand")
    public ResponseEntity<?> searchByBrandName(
            @RequestParam String name) {
        try {
            return ResponseEntity.ok(ApiResponse.success(
                    "Search completed",
                    medicineService.searchByBrandName(name)));
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
                    medicineService.searchByApiName(name)));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }
}*/

/*package com.medicinedonation.controller;

import com.medicinedonation.dto.response.ApiResponse;
import com.medicinedonation.service.MedicineService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/medicines")
public class MedicineController {

    @Autowired
    private MedicineService medicineService;

    // Get all medicines — all authenticated users
    @GetMapping
    public ResponseEntity<?> getAllMedicines() {
        try {
            return ResponseEntity.ok(ApiResponse.success(
                    "Medicines retrieved",
                    medicineService.getAllMedicines()));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    // Get medicine by ID
    @GetMapping("/{id}")
    public ResponseEntity<?> getMedicineById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(ApiResponse.success(
                    "Medicine retrieved",
                    medicineService.getMedicineById(id)));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    // Search by brand name + optional strength
    @GetMapping("/search/brand")
    public ResponseEntity<?> searchByBrandName(
            @RequestParam String name,
            @RequestParam(required = false) String strength) {
        try {
            return ResponseEntity.ok(ApiResponse.success(
                    "Search completed",
                    medicineService.searchByBrandName(name, strength)));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    // Search by API name + optional strength
    @GetMapping("/search/api")
    public ResponseEntity<?> searchByApiName(
            @RequestParam String name,
            @RequestParam(required = false) String strength) {
        try {
            return ResponseEntity.ok(ApiResponse.success(
                    "Search completed",
                    medicineService.searchByApiName(name, strength)));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }
}*/


package com.medicinedonation.controller;

import com.medicinedonation.dto.response.ApiResponse;
import com.medicinedonation.service.MedicineService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/medicines")
public class MedicineController {

    @Autowired
    private MedicineService medicineService;

    // Get all medicines — all authenticated users
    @GetMapping
    public ResponseEntity<?> getAllMedicines() {
        try {
            return ResponseEntity.ok(ApiResponse.success(
                    "Medicines retrieved",
                    medicineService.getAllMedicines()));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    // Get medicine by ID
    @GetMapping("/{id}")
    public ResponseEntity<?> getMedicineById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(ApiResponse.success(
                    "Medicine retrieved",
                    medicineService.getMedicineById(id)));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    // Search by brand name + optional strength
    @GetMapping("/search/brand")
    public ResponseEntity<?> searchByBrandName(
            @RequestParam String name,
            @RequestParam(required = false) String strength) {
        try {
            return ResponseEntity.ok(ApiResponse.success(
                    "Search completed",
                    medicineService.searchByBrandName(name, strength, null)));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    // Search by API name + optional strength
    @GetMapping("/search/api")
    public ResponseEntity<?> searchByApiName(
            @RequestParam String name,
            @RequestParam(required = false) String strength) {
        try {
            return ResponseEntity.ok(ApiResponse.success(
                    "Search completed",
                    medicineService.searchByApiName(name, strength, null)));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }
}