package com.medicinedonation.controller;

import com.medicinedonation.dto.request.LoginRequest;
import com.medicinedonation.dto.request.RegisterRequest;
import com.medicinedonation.dto.response.ApiResponse;
import com.medicinedonation.dto.response.JwtResponse;
import com.medicinedonation.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    // Login — all roles
    @PostMapping("/login")
    public ResponseEntity<?> login(
            @Valid @RequestBody LoginRequest request) {
        try {
            JwtResponse response = authService.login(request);
            return ResponseEntity.ok(
                    ApiResponse.success("Login successful", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    // Register Donor
    @PostMapping("/register/donor")
    public ResponseEntity<?> registerDonor(
            @Valid @RequestBody RegisterRequest request) {
        try {
            JwtResponse response = authService.register(request);
            return ResponseEntity.ok(
                    ApiResponse.success("Donor registered successfully", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    // Register Recipient
    @PostMapping("/register/recipient")
    public ResponseEntity<?> registerRecipient(
            @Valid @RequestBody RegisterRequest request) {
        try {
            JwtResponse response = authService.registerRecipient(request);
            return ResponseEntity.ok(
                    ApiResponse.success("Recipient registered successfully", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }
}