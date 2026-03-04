package com.medicinedonation.service;

import com.medicinedonation.dto.request.LoginRequest;
import com.medicinedonation.dto.request.RegisterRequest;
import com.medicinedonation.dto.response.JwtResponse;
import com.medicinedonation.enums.Role;
import com.medicinedonation.model.User;
import com.medicinedonation.repository.UserRepository;
import com.medicinedonation.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    // Login — all roles
    public JwtResponse login(LoginRequest request) {

        // Check user exists
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException(
                        "User not found with email: " + request.getEmail()));

        // Check user is active
        if (!user.isActive()) {
            throw new RuntimeException(
                    "Your account has been deactivated. Contact admin.");
        }

        // Authenticate with Spring Security
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );

            // Generate JWT token
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String token = jwtUtil.generateToken(userDetails);

            // Return token + user info
            return JwtResponse.builder()
                    .token(token)
                    .type("Bearer")
                    .id(user.getId())
                    .name(user.getName())
                    .email(user.getEmail())
                    .role(user.getRole().name())
                    .build();

        } catch (BadCredentialsException e) {
            throw new RuntimeException("Invalid email or password");
        }
    }

    // Register — Donor and Recipient only
    public JwtResponse register(RegisterRequest request) {

        // Check email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException(
                    "Email already registered: " + request.getEmail());
        }

        // Create new user — only DONOR role allowed here
        // Recipients use separate endpoint
        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .phone(request.getPhone())
                .role(Role.DONOR)
                .active(true)
                .build();

        userRepository.save(user);

        // Auto login after register
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail(request.getEmail());
        loginRequest.setPassword(request.getPassword());

        return login(loginRequest);
    }

    // Register Recipient — separate endpoint
    public JwtResponse registerRecipient(RegisterRequest request) {

        // Check email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException(
                    "Email already registered: " + request.getEmail());
        }

        // Create recipient user
        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .phone(request.getPhone())
                .role(Role.RECIPIENT)
                .active(true)
                .build();

        userRepository.save(user);

        // Auto login after register
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail(request.getEmail());
        loginRequest.setPassword(request.getPassword());

        return login(loginRequest);
    }
}