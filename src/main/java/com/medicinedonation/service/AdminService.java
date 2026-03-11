/*package com.medicinedonation.service;

import com.medicinedonation.dto.request.AdminRegisterRequest;
import com.medicinedonation.dto.request.CollectionPointRequest;
import com.medicinedonation.dto.request.MedicineRequest;
import com.medicinedonation.dto.response.CollectionPointResponse;
import com.medicinedonation.dto.response.MedicineResponse;
import com.medicinedonation.dto.response.UserResponse;
import com.medicinedonation.enums.Role;
import com.medicinedonation.model.CollectionPoint;
import com.medicinedonation.model.Medicine;
import com.medicinedonation.model.User;
import com.medicinedonation.repository.CollectionPointRepository;
import com.medicinedonation.repository.MedicineRepository;
import com.medicinedonation.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AdminService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CollectionPointRepository collectionPointRepository;

    @Autowired
    private MedicineRepository medicineRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // ─────────────────────────────────────────
    // USER MANAGEMENT
    // ─────────────────────────────────────────

    // Register Doctor
    public UserResponse registerDoctor(AdminRegisterRequest request) {
        return registerUserWithRole(request, Role.DOCTOR);
    }

    // Register Pharmacist
    public UserResponse registerPharmacist(AdminRegisterRequest request) {
        return registerUserWithRole(request, Role.PHARMACIST);
    }

    // Register Collection Point Admin
    public UserResponse registerCollectionPointAdmin(
            AdminRegisterRequest request) {
        return registerUserWithRole(request, Role.COLLECTION_POINT);
    }

    // Common register logic
    private UserResponse registerUserWithRole(
            AdminRegisterRequest request, Role role) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException(
                    "Email already exists: " + request.getEmail());
        }

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .phone(request.getPhone())
                .role(role)
                .active(true)
                .build();

        User saved = userRepository.save(user);
        return mapToUserResponse(saved);
    }

    // Get all users by role
    public List<UserResponse> getUsersByRole(Role role) {
        return userRepository.findByRole(role)
                .stream()
                .map(this::mapToUserResponse)
                .collect(Collectors.toList());
    }

    // Get user by ID
    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(
                        "User not found with id: " + id));
        return mapToUserResponse(user);
    }

    // Delete user
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(
                        "User not found with id: " + id));

        // Cannot delete admin
        if (user.getRole() == Role.ADMIN) {
            throw new RuntimeException("Cannot delete admin user");
        }

        userRepository.delete(user);
    }

    // Activate user
    public UserResponse activateUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(
                        "User not found with id: " + id));
        user.setActive(true);
        return mapToUserResponse(userRepository.save(user));
    }

    // Deactivate user
    public UserResponse deactivateUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(
                        "User not found with id: " + id));

        if (user.getRole() == Role.ADMIN) {
            throw new RuntimeException("Cannot deactivate admin user");
        }

        user.setActive(false);
        return mapToUserResponse(userRepository.save(user));
    }

    // ─────────────────────────────────────────
    // COLLECTION POINT MANAGEMENT
    // ─────────────────────────────────────────

    // Add collection point
    public CollectionPointResponse addCollectionPoint(
            CollectionPointRequest request) {

        CollectionPoint point = CollectionPoint.builder()
                .locationName(request.getLocationName())
                .address(request.getAddress())
                .district(request.getDistrict())
                .phone(request.getPhone())
                .active(true)
                .build();

        return mapToCollectionPointResponse(
                collectionPointRepository.save(point));
    }

    // Edit collection point
    public CollectionPointResponse editCollectionPoint(
            Long id, CollectionPointRequest request) {

        CollectionPoint point = collectionPointRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(
                        "Collection point not found with id: " + id));

        point.setLocationName(request.getLocationName());
        point.setAddress(request.getAddress());
        point.setDistrict(request.getDistrict());
        point.setPhone(request.getPhone());

        return mapToCollectionPointResponse(
                collectionPointRepository.save(point));
    }

    // Delete collection point
    public void deleteCollectionPoint(Long id) {
        CollectionPoint point = collectionPointRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(
                        "Collection point not found with id: " + id));
        collectionPointRepository.delete(point);
    }

    // Get all collection points
    public List<CollectionPointResponse> getAllCollectionPoints() {
        return collectionPointRepository.findAll()
                .stream()
                .map(this::mapToCollectionPointResponse)
                .collect(Collectors.toList());
    }

    // Assign admin to collection point
    public CollectionPointResponse assignAdminToCollectionPoint(
            Long pointId, Long adminId) {

        CollectionPoint point = collectionPointRepository.findById(pointId)
                .orElseThrow(() -> new RuntimeException(
                        "Collection point not found"));

        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new RuntimeException(
                        "User not found"));

        // Check user is collection point role
        if (admin.getRole() != Role.COLLECTION_POINT) {
            throw new RuntimeException(
                    "User is not a collection point admin");
        }

        // Check admin not already assigned to another point
        if (collectionPointRepository.existsByAdmin(admin)) {
            throw new RuntimeException(
                    "This admin is already assigned to another collection point");
        }

        point.setAdmin(admin);
        return mapToCollectionPointResponse(
                collectionPointRepository.save(point));
    }

    // Remove admin from collection point
    public CollectionPointResponse removeAdminFromCollectionPoint(
            Long pointId) {

        CollectionPoint point = collectionPointRepository.findById(pointId)
                .orElseThrow(() -> new RuntimeException(
                        "Collection point not found"));

        point.setAdmin(null);
        return mapToCollectionPointResponse(
                collectionPointRepository.save(point));
    }

    // ─────────────────────────────────────────
    // MEDICINE DATABASE MANAGEMENT
    // ─────────────────────────────────────────

    // Add medicine
    public MedicineResponse addMedicine(MedicineRequest request) {

        Medicine medicine = Medicine.builder()
                .brandName(request.getBrandName())
                .apiName(request.getApiName())
                .strength(request.getStrength())
                .dosageForm(request.getDosageForm())
                .route(request.getRoute())
                .schedule(request.getSchedule())
                .pharmacistVerified(false)
                .build();

        return mapToMedicineResponse(medicineRepository.save(medicine));
    }

    // Edit medicine
    public MedicineResponse editMedicine(Long id, MedicineRequest request) {

        Medicine medicine = medicineRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(
                        "Medicine not found with id: " + id));

        medicine.setBrandName(request.getBrandName());
        medicine.setApiName(request.getApiName());
        medicine.setStrength(request.getStrength());
        medicine.setDosageForm(request.getDosageForm());
        medicine.setRoute(request.getRoute());
        medicine.setSchedule(request.getSchedule());

        return mapToMedicineResponse(medicineRepository.save(medicine));
    }

    // Delete medicine
    public void deleteMedicine(Long id) {
        Medicine medicine = medicineRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(
                        "Medicine not found with id: " + id));
        medicineRepository.delete(medicine);
    }

    // Get all medicines
    public List<MedicineResponse> getAllMedicines() {
        return medicineRepository.findAll()
                .stream()
                .map(this::mapToMedicineResponse)
                .collect(Collectors.toList());
    }

    // Get pharmacist added medicines
    public List<MedicineResponse> getPharmacistAddedMedicines() {
        return medicineRepository.findByPharmacistVerified(true)
                .stream()
                .map(this::mapToMedicineResponse)
                .collect(Collectors.toList());
    }

    // ─────────────────────────────────────────
    // MAPPERS
    // ─────────────────────────────────────────

    public UserResponse mapToUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole().name())
                .phone(user.getPhone())
                .active(user.isActive())
                .createdAt(user.getCreatedAt())
                .build();
    }

    public CollectionPointResponse mapToCollectionPointResponse(
            CollectionPoint point) {
        return CollectionPointResponse.builder()
                .id(point.getId())
                .locationName(point.getLocationName())
                .address(point.getAddress())
                .district(point.getDistrict())
                .phone(point.getPhone())
                .active(point.isActive())
                .adminName(point.getAdmin() != null ?
                        point.getAdmin().getName() : null)
                .adminEmail(point.getAdmin() != null ?
                        point.getAdmin().getEmail() : null)
                .build();
    }

    public MedicineResponse mapToMedicineResponse(Medicine medicine) {
        return MedicineResponse.builder()
                .id(medicine.getId())
                .brandName(medicine.getBrandName())
                .apiName(medicine.getApiName())
                .strength(medicine.getStrength())
                .dosageForm(medicine.getDosageForm())
                .route(medicine.getRoute())
                .schedule(medicine.getSchedule().name())
                .pharmacistVerified(medicine.isPharmacistVerified())
                .verifiedByName(medicine.getVerifiedBy() != null ?
                        medicine.getVerifiedBy().getName() : null)
                .addedAt(medicine.getAddedAt())
                .build();
    }
}*/

/*package com.medicinedonation.service;

import com.medicinedonation.dto.request.AdminRegisterRequest;
import com.medicinedonation.dto.request.CollectionPointRequest;
import com.medicinedonation.dto.request.MedicineRequest;
import com.medicinedonation.dto.response.CollectionPointResponse;
import com.medicinedonation.dto.response.MedicineResponse;
import com.medicinedonation.dto.response.UserResponse;
import com.medicinedonation.enums.Role;
import com.medicinedonation.model.CollectionPoint;
import com.medicinedonation.model.Medicine;
import com.medicinedonation.model.User;
import com.medicinedonation.repository.CollectionPointRepository;
import com.medicinedonation.repository.MedicineRepository;
import com.medicinedonation.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AdminService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CollectionPointRepository collectionPointRepository;

    @Autowired
    private MedicineRepository medicineRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // ─────────────────────────────────────────
    // USER MANAGEMENT
    // ─────────────────────────────────────────

    // Register Doctor
    public UserResponse registerDoctor(AdminRegisterRequest request) {
        return registerUserWithRole(request, Role.DOCTOR);
    }

    // Register Pharmacist
    public UserResponse registerPharmacist(AdminRegisterRequest request) {
        return registerUserWithRole(request, Role.PHARMACIST);
    }

    // Register Collection Point Admin
    public UserResponse registerCollectionPointAdmin(
            AdminRegisterRequest request) {
        return registerUserWithRole(request, Role.COLLECTION_POINT);
    }

    // Common register logic
    private UserResponse registerUserWithRole(
            AdminRegisterRequest request, Role role) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException(
                    "Email already exists: " + request.getEmail());
        }

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .phone(request.getPhone())
                .role(role)
                .active(true)
                .build();

        User saved = userRepository.save(user);
        return mapToUserResponse(saved);
    }

    // Get all users by role
    public List<UserResponse> getUsersByRole(Role role) {
        return userRepository.findByRole(role)
                .stream()
                .map(this::mapToUserResponse)
                .collect(Collectors.toList());
    }

    // Get user by ID
    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(
                        "User not found with id: " + id));
        return mapToUserResponse(user);
    }

    // Delete user
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(
                        "User not found with id: " + id));

        if (user.getRole() == Role.ADMIN) {
            throw new RuntimeException("Cannot delete admin user");
        }

        userRepository.delete(user);
    }

    // Activate user
    public UserResponse activateUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(
                        "User not found with id: " + id));
        user.setActive(true);
        return mapToUserResponse(userRepository.save(user));
    }

    // Deactivate user
    public UserResponse deactivateUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(
                        "User not found with id: " + id));

        if (user.getRole() == Role.ADMIN) {
            throw new RuntimeException("Cannot deactivate admin user");
        }

        user.setActive(false);
        return mapToUserResponse(userRepository.save(user));
    }

    // ─────────────────────────────────────────
    // COLLECTION POINT MANAGEMENT
    // ─────────────────────────────────────────

    // Add collection point
    public CollectionPointResponse addCollectionPoint(
            CollectionPointRequest request) {

        CollectionPoint point = CollectionPoint.builder()
                .locationName(request.getLocationName())
                .address(request.getAddress())
                .district(request.getDistrict())
                .phone(request.getPhone())
                .active(true)
                .build();

        return mapToCollectionPointResponse(
                collectionPointRepository.save(point));
    }

    // Edit collection point
    public CollectionPointResponse editCollectionPoint(
            Long id, CollectionPointRequest request) {

        CollectionPoint point = collectionPointRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(
                        "Collection point not found with id: " + id));

        point.setLocationName(request.getLocationName());
        point.setAddress(request.getAddress());
        point.setDistrict(request.getDistrict());
        point.setPhone(request.getPhone());

        return mapToCollectionPointResponse(
                collectionPointRepository.save(point));
    }

    // Delete collection point
    public void deleteCollectionPoint(Long id) {
        CollectionPoint point = collectionPointRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(
                        "Collection point not found with id: " + id));
        collectionPointRepository.delete(point);
    }

    // Get all collection points
    public List<CollectionPointResponse> getAllCollectionPoints() {
        return collectionPointRepository.findAll()
                .stream()
                .map(this::mapToCollectionPointResponse)
                .collect(Collectors.toList());
    }

    // Assign admin to collection point
    public CollectionPointResponse assignAdminToCollectionPoint(
            Long pointId, Long adminId) {

        CollectionPoint point = collectionPointRepository.findById(pointId)
                .orElseThrow(() -> new RuntimeException(
                        "Collection point not found"));

        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new RuntimeException(
                        "User not found"));

        if (admin.getRole() != Role.COLLECTION_POINT) {
            throw new RuntimeException(
                    "User is not a collection point admin");
        }

        if (collectionPointRepository.existsByAdmin(admin)) {
            throw new RuntimeException(
                    "This admin is already assigned to another collection point");
        }

        point.setAdmin(admin);
        return mapToCollectionPointResponse(
                collectionPointRepository.save(point));
    }

    // Remove admin from collection point
    public CollectionPointResponse removeAdminFromCollectionPoint(
            Long pointId) {

        CollectionPoint point = collectionPointRepository.findById(pointId)
                .orElseThrow(() -> new RuntimeException(
                        "Collection point not found"));

        point.setAdmin(null);
        return mapToCollectionPointResponse(
                collectionPointRepository.save(point));
    }

    // ─────────────────────────────────────────
    // MEDICINE DATABASE MANAGEMENT
    // ─────────────────────────────────────────

    // Add medicine — with duplicate check
    public MedicineResponse addMedicine(MedicineRequest request) {

        // Check duplicate
        boolean exists = medicineRepository
                .existsByBrandNameIgnoreCaseAndApiNameIgnoreCaseAndStrengthIgnoreCaseAndDosageFormIgnoreCaseAndRouteIgnoreCase(
                        request.getBrandName(),
                        request.getApiName(),
                        request.getStrength(),
                        request.getDosageForm(),
                        request.getRoute()
                );

        if (exists) {
            throw new RuntimeException(
                    "Medicine already exists in database: " +
                            request.getBrandName() + " " +
                            request.getStrength() + " " +
                            request.getDosageForm()
            );
        }

        Medicine medicine = Medicine.builder()
                .brandName(request.getBrandName())
                .apiName(request.getApiName())
                .strength(request.getStrength())
                .dosageForm(request.getDosageForm())
                .route(request.getRoute())
                .schedule(request.getSchedule())
                .pharmacistVerified(false)
                .build();

        return mapToMedicineResponse(medicineRepository.save(medicine));
    }

    // Edit medicine
    public MedicineResponse editMedicine(Long id, MedicineRequest request) {

        Medicine medicine = medicineRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(
                        "Medicine not found with id: " + id));

        // Check duplicate — exclude current medicine from check
        boolean exists = medicineRepository
                .existsByBrandNameIgnoreCaseAndApiNameIgnoreCaseAndStrengthIgnoreCaseAndDosageFormIgnoreCaseAndRouteIgnoreCase(
                        request.getBrandName(),
                        request.getApiName(),
                        request.getStrength(),
                        request.getDosageForm(),
                        request.getRoute()
                );

        // If duplicate found and it is not the same medicine being edited
        if (exists) {
            Medicine existing = medicineRepository
                    .findByBrandNameIgnoreCase(request.getBrandName())
                    .orElse(null);

            if (existing != null && !existing.getId().equals(id)) {
                throw new RuntimeException(
                        "Another medicine with same details already exists: " +
                                request.getBrandName() + " " +
                                request.getStrength() + " " +
                                request.getDosageForm()
                );
            }
        }

        medicine.setBrandName(request.getBrandName());
        medicine.setApiName(request.getApiName());
        medicine.setStrength(request.getStrength());
        medicine.setDosageForm(request.getDosageForm());
        medicine.setRoute(request.getRoute());
        medicine.setSchedule(request.getSchedule());

        return mapToMedicineResponse(medicineRepository.save(medicine));
    }

    // Delete medicine
    public void deleteMedicine(Long id) {
        Medicine medicine = medicineRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(
                        "Medicine not found with id: " + id));
        medicineRepository.delete(medicine);
    }

    // Get all medicines
    public List<MedicineResponse> getAllMedicines() {
        return medicineRepository.findAll()
                .stream()
                .map(this::mapToMedicineResponse)
                .collect(Collectors.toList());
    }

    // Get pharmacist added medicines
    public List<MedicineResponse> getPharmacistAddedMedicines() {
        return medicineRepository.findByPharmacistVerified(true)
                .stream()
                .map(this::mapToMedicineResponse)
                .collect(Collectors.toList());
    }

    // ─────────────────────────────────────────
    // MAPPERS
    // ─────────────────────────────────────────

    public UserResponse mapToUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole().name())
                .phone(user.getPhone())
                .active(user.isActive())
                .createdAt(user.getCreatedAt())
                .build();
    }

    public CollectionPointResponse mapToCollectionPointResponse(
            CollectionPoint point) {
        return CollectionPointResponse.builder()
                .id(point.getId())
                .locationName(point.getLocationName())
                .address(point.getAddress())
                .district(point.getDistrict())
                .phone(point.getPhone())
                .active(point.isActive())
                .adminName(point.getAdmin() != null ?
                        point.getAdmin().getName() : null)
                .adminEmail(point.getAdmin() != null ?
                        point.getAdmin().getEmail() : null)
                .build();
    }

    public MedicineResponse mapToMedicineResponse(Medicine medicine) {
        return MedicineResponse.builder()
                .id(medicine.getId())
                .brandName(medicine.getBrandName())
                .apiName(medicine.getApiName())
                .strength(medicine.getStrength())
                .dosageForm(medicine.getDosageForm())
                .route(medicine.getRoute())
                .schedule(medicine.getSchedule().name())
                .pharmacistVerified(medicine.isPharmacistVerified())
                .verifiedByName(medicine.getVerifiedBy() != null ?
                        medicine.getVerifiedBy().getName() : null)
                .addedAt(medicine.getAddedAt())
                .build();
    }
}*/


package com.medicinedonation.service;

import com.medicinedonation.dto.request.AdminRegisterRequest;
import com.medicinedonation.dto.request.CollectionPointRequest;
import com.medicinedonation.dto.request.MedicineRequest;
import com.medicinedonation.dto.response.CollectionPointResponse;
import com.medicinedonation.dto.response.MedicineResponse;
import com.medicinedonation.dto.response.UserResponse;
import com.medicinedonation.enums.Role;
import com.medicinedonation.model.CollectionPoint;
import com.medicinedonation.model.Medicine;
import com.medicinedonation.model.User;
import com.medicinedonation.repository.CollectionPointRepository;
import com.medicinedonation.repository.MedicineRepository;
import com.medicinedonation.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AdminService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CollectionPointRepository collectionPointRepository;

    @Autowired
    private MedicineRepository medicineRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // ─────────────────────────────────────────
    // USER MANAGEMENT
    // ─────────────────────────────────────────

    public UserResponse registerDoctor(AdminRegisterRequest request) {
        return registerUserWithRole(request, Role.DOCTOR);
    }

    public UserResponse registerPharmacist(AdminRegisterRequest request) {
        return registerUserWithRole(request, Role.PHARMACIST);
    }

    public UserResponse registerCollectionPointAdmin(
            AdminRegisterRequest request) {
        return registerUserWithRole(request, Role.COLLECTION_POINT);
    }

    private UserResponse registerUserWithRole(
            AdminRegisterRequest request, Role role) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException(
                    "Email already exists: " + request.getEmail());
        }

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .phone(request.getPhone())
                .role(role)
                .active(true)
                .build();

        User saved = userRepository.save(user);
        return mapToUserResponse(saved);
    }

    public List<UserResponse> getUsersByRole(Role role) {
        return userRepository.findByRole(role)
                .stream()
                .map(this::mapToUserResponse)
                .collect(Collectors.toList());
    }

    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(
                        "User not found with id: " + id));
        return mapToUserResponse(user);
    }

    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(
                        "User not found with id: " + id));

        if (user.getRole() == Role.ADMIN) {
            throw new RuntimeException("Cannot delete admin user");
        }

        userRepository.delete(user);
    }

    public UserResponse activateUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(
                        "User not found with id: " + id));
        user.setActive(true);
        return mapToUserResponse(userRepository.save(user));
    }

    public UserResponse deactivateUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(
                        "User not found with id: " + id));

        if (user.getRole() == Role.ADMIN) {
            throw new RuntimeException("Cannot deactivate admin user");
        }

        user.setActive(false);
        return mapToUserResponse(userRepository.save(user));
    }

    // ─────────────────────────────────────────
    // COLLECTION POINT MANAGEMENT
    // ─────────────────────────────────────────

    public CollectionPointResponse addCollectionPoint(
            CollectionPointRequest request) {

        CollectionPoint point = CollectionPoint.builder()
                .locationName(request.getLocationName())
                .address(request.getAddress())
                .district(request.getDistrict())
                .phone(request.getPhone())
                .active(true)
                .build();

        return mapToCollectionPointResponse(
                collectionPointRepository.save(point));
    }

    public CollectionPointResponse editCollectionPoint(
            Long id, CollectionPointRequest request) {

        CollectionPoint point = collectionPointRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(
                        "Collection point not found with id: " + id));

        point.setLocationName(request.getLocationName());
        point.setAddress(request.getAddress());
        point.setDistrict(request.getDistrict());
        point.setPhone(request.getPhone());

        return mapToCollectionPointResponse(
                collectionPointRepository.save(point));
    }

    public void deleteCollectionPoint(Long id) {
        CollectionPoint point = collectionPointRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(
                        "Collection point not found with id: " + id));
        collectionPointRepository.delete(point);
    }

    public List<CollectionPointResponse> getAllCollectionPoints() {
        return collectionPointRepository.findAll()
                .stream()
                .map(this::mapToCollectionPointResponse)
                .collect(Collectors.toList());
    }

    public CollectionPointResponse assignAdminToCollectionPoint(
            Long pointId, Long adminId) {

        CollectionPoint point = collectionPointRepository.findById(pointId)
                .orElseThrow(() -> new RuntimeException(
                        "Collection point not found"));

        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (admin.getRole() != Role.COLLECTION_POINT) {
            throw new RuntimeException("User is not a collection point admin");
        }

        if (collectionPointRepository.existsByAdmin(admin)) {
            throw new RuntimeException(
                    "This admin is already assigned to another collection point");
        }

        point.setAdmin(admin);
        return mapToCollectionPointResponse(
                collectionPointRepository.save(point));
    }

    public CollectionPointResponse removeAdminFromCollectionPoint(Long pointId) {
        CollectionPoint point = collectionPointRepository.findById(pointId)
                .orElseThrow(() -> new RuntimeException(
                        "Collection point not found"));

        point.setAdmin(null);
        return mapToCollectionPointResponse(
                collectionPointRepository.save(point));
    }

    // ─────────────────────────────────────────
    // MEDICINE DATABASE MANAGEMENT
    // ─────────────────────────────────────────

    public MedicineResponse addMedicine(MedicineRequest request) {

        boolean exists = medicineRepository
                .existsByBrandNameIgnoreCaseAndApiNameIgnoreCaseAndStrengthIgnoreCaseAndDosageFormIgnoreCaseAndRouteIgnoreCase(
                        request.getBrandName(),
                        request.getApiName(),
                        request.getStrength(),
                        request.getDosageForm(),
                        request.getRoute()
                );

        if (exists) {
            throw new RuntimeException(
                    "Medicine already exists in database: " +
                            request.getBrandName() + " " +
                            request.getStrength() + " " +
                            request.getDosageForm()
            );
        }

        Medicine medicine = Medicine.builder()
                .brandName(request.getBrandName())
                .apiName(request.getApiName())
                .strength(request.getStrength())
                .dosageForm(request.getDosageForm())
                .route(request.getRoute())
                .schedule(request.getSchedule())
                .pharmacistVerified(false)
                .build();

        return mapToMedicineResponse(medicineRepository.save(medicine));
    }

    public MedicineResponse editMedicine(Long id, MedicineRequest request) {

        Medicine medicine = medicineRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(
                        "Medicine not found with id: " + id));

        boolean exists = medicineRepository
                .existsByBrandNameIgnoreCaseAndApiNameIgnoreCaseAndStrengthIgnoreCaseAndDosageFormIgnoreCaseAndRouteIgnoreCase(
                        request.getBrandName(),
                        request.getApiName(),
                        request.getStrength(),
                        request.getDosageForm(),
                        request.getRoute()
                );

        // ✅ FIXED — findByBrandNameIgnoreCase now returns List, not Optional
        if (exists) {
            List<Medicine> existingList = medicineRepository
                    .findByBrandNameIgnoreCase(request.getBrandName());

            Medicine existing = existingList.isEmpty() ? null : existingList.get(0);

            if (existing != null && !existing.getId().equals(id)) {
                throw new RuntimeException(
                        "Another medicine with same details already exists: " +
                                request.getBrandName() + " " +
                                request.getStrength() + " " +
                                request.getDosageForm()
                );
            }
        }

        medicine.setBrandName(request.getBrandName());
        medicine.setApiName(request.getApiName());
        medicine.setStrength(request.getStrength());
        medicine.setDosageForm(request.getDosageForm());
        medicine.setRoute(request.getRoute());
        medicine.setSchedule(request.getSchedule());

        return mapToMedicineResponse(medicineRepository.save(medicine));
    }

    public void deleteMedicine(Long id) {
        Medicine medicine = medicineRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(
                        "Medicine not found with id: " + id));
        medicineRepository.delete(medicine);
    }

    public List<MedicineResponse> getAllMedicines() {
        return medicineRepository.findAll()
                .stream()
                .map(this::mapToMedicineResponse)
                .collect(Collectors.toList());
    }

    public List<MedicineResponse> getPharmacistAddedMedicines() {
        return medicineRepository.findByPharmacistVerified(true)
                .stream()
                .map(this::mapToMedicineResponse)
                .collect(Collectors.toList());
    }

    // ─────────────────────────────────────────
    // MAPPERS
    // ─────────────────────────────────────────

    public UserResponse mapToUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole().name())
                .phone(user.getPhone())
                .active(user.isActive())
                .createdAt(user.getCreatedAt())
                .build();
    }

    public CollectionPointResponse mapToCollectionPointResponse(
            CollectionPoint point) {
        return CollectionPointResponse.builder()
                .id(point.getId())
                .locationName(point.getLocationName())
                .address(point.getAddress())
                .district(point.getDistrict())
                .phone(point.getPhone())
                .active(point.isActive())
                .adminName(point.getAdmin() != null ?
                        point.getAdmin().getName() : null)
                .adminEmail(point.getAdmin() != null ?
                        point.getAdmin().getEmail() : null)
                .build();
    }

    public MedicineResponse mapToMedicineResponse(Medicine medicine) {
        return MedicineResponse.builder()
                .id(medicine.getId())
                .brandName(medicine.getBrandName())
                .apiName(medicine.getApiName())
                .strength(medicine.getStrength())
                .dosageForm(medicine.getDosageForm())
                .route(medicine.getRoute())
                .schedule(medicine.getSchedule().name())
                .pharmacistVerified(medicine.isPharmacistVerified())
                .verifiedByName(medicine.getVerifiedBy() != null ?
                        medicine.getVerifiedBy().getName() : null)
                .addedAt(medicine.getAddedAt())
                .build();
    }
}