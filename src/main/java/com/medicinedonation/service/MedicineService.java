/*package com.medicinedonation.service;

import com.medicinedonation.dto.request.MedicineRequest;
import com.medicinedonation.dto.response.AvailableDonationResponse;
import com.medicinedonation.dto.response.MatchResultResponse;
import com.medicinedonation.dto.response.MedicineResponse;
import com.medicinedonation.enums.DonationStatus;
import com.medicinedonation.model.Donation;
import com.medicinedonation.model.Medicine;
import com.medicinedonation.repository.DonationRepository;
import com.medicinedonation.repository.MedicineRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class MedicineService {

    @Autowired
    private MedicineRepository medicineRepository;

    @Autowired
    private DonationRepository donationRepository;

    // ─────────────────────────────────────────
    // SEARCH BY BRAND NAME
    // ─────────────────────────────────────────

    public MatchResultResponse searchByBrandName(String brandName) {

        // Find medicine in DB by brand name
        Optional<Medicine> medicineOpt =
                medicineRepository.findByBrandNameIgnoreCase(brandName);

        // Medicine not found in DB at all
        if (medicineOpt.isEmpty()) {
            return MatchResultResponse.builder()
                    .matchTier("NO_MATCH")
                    .searchedBrandName(brandName)
                    .matches(new ArrayList<>())
                    .message("No medicine found with brand name: "
                            + brandName)
                    .build();
        }

        Medicine medicine = medicineOpt.get();

        // Run matching engine using API name from found medicine
        return runMatchingEngine(
                medicine.getApiName(),
                medicine.getStrength(),
                medicine.getDosageForm(),
                medicine.getRoute(),
                brandName,
                null
        );
    }

    // ─────────────────────────────────────────
    // SEARCH BY API NAME
    // ─────────────────────────────────────────

    public MatchResultResponse searchByApiName(String apiName) {

        // Check any medicine exists with this API name
        List<Medicine> medicines =
                medicineRepository.findByApiNameIgnoreCase(apiName);

        if (medicines.isEmpty()) {
            return MatchResultResponse.builder()
                    .matchTier("NO_MATCH")
                    .searchedApiName(apiName)
                    .matches(new ArrayList<>())
                    .message("No medicine found with API name: " + apiName)
                    .build();
        }

        // Use first match to get dosage form and route for Tier 2
        Medicine first = medicines.get(0);

        return runMatchingEngine(
                apiName,
                first.getStrength(),
                first.getDosageForm(),
                first.getRoute(),
                null,
                apiName
        );
    }

    // ─────────────────────────────────────────
    // MATCHING ENGINE — CORE LOGIC
    // ─────────────────────────────────────────

    private MatchResultResponse runMatchingEngine(
            String apiName,
            String strength,
            String dosageForm,
            String route,
            String searchedBrandName,
            String searchedApiName) {

        // ── TIER 1 — Exact Match ──────────────
        // Same API + same strength + same dosageForm + same route
        List<Medicine> tier1Medicines =
                medicineRepository
                        .findByApiNameIgnoreCaseAndStrengthIgnoreCaseAndDosageFormIgnoreCaseAndRouteIgnoreCase(
                                apiName, strength, dosageForm, route);

        // Get LIVE donations for tier 1 medicines
        List<AvailableDonationResponse> tier1Results =
                getLiveDonationsForMedicines(tier1Medicines, "TIER_1");

        if (!tier1Results.isEmpty()) {
            return MatchResultResponse.builder()
                    .matchTier("TIER_1")
                    .searchedBrandName(searchedBrandName)
                    .searchedApiName(searchedApiName)
                    .matches(tier1Results)
                    .message("Exact match found — " +
                            tier1Results.size() + " available")
                    .build();
        }

        // ── TIER 2 — Same API Different Strength ──
        // Same API + same dosageForm — different strength detected & displayed
        // NO auto conversion — user selects per prescription
        List<Medicine> tier2Medicines =
                medicineRepository
                        .findByApiNameIgnoreCaseAndDosageFormIgnoreCase(
                                apiName, dosageForm);

        // Remove exact strength match (already checked in Tier 1)
        tier2Medicines = tier2Medicines.stream()
                .filter(m -> !m.getStrength()
                        .equalsIgnoreCase(strength))
                .collect(Collectors.toList());

        List<AvailableDonationResponse> tier2Results =
                getLiveDonationsForMedicines(tier2Medicines, "TIER_2");

        if (!tier2Results.isEmpty()) {
            return MatchResultResponse.builder()
                    .matchTier("TIER_2")
                    .searchedBrandName(searchedBrandName)
                    .searchedApiName(searchedApiName)
                    .matches(tier2Results)
                    .message("Similar medicines found (different strength) — " +
                            tier2Results.size() +
                            " available. Please select per your prescription.")
                    .build();
        }

        // ── NO MATCH ──────────────────────────
        return MatchResultResponse.builder()
                .matchTier("NO_MATCH")
                .searchedBrandName(searchedBrandName)
                .searchedApiName(searchedApiName)
                .matches(new ArrayList<>())
                .message("No available donations found for: " +
                        apiName)
                .build();
    }

    // ─────────────────────────────────────────
    // GET LIVE DONATIONS FOR MEDICINES
    // ─────────────────────────────────────────

    private List<AvailableDonationResponse> getLiveDonationsForMedicines(
            List<Medicine> medicines, String tier) {

        List<AvailableDonationResponse> results = new ArrayList<>();

        for (Medicine medicine : medicines) {
            // Get all LIVE donations for this medicine
            List<Donation> liveDonations =
                    donationRepository.findByStatus(DonationStatus.LIVE)
                            .stream()
                            .filter(d -> d.getMedicine() != null &&
                                    d.getMedicine().getId()
                                            .equals(medicine.getId()))
                            .collect(Collectors.toList());

            for (Donation donation : liveDonations) {
                results.add(mapToAvailableDonation(donation, tier));
            }
        }

        return results;
    }

    // ─────────────────────────────────────────
    // CHECK IF MEDICINE EXISTS IN DB
    // Used by donation submission flow
    // ─────────────────────────────────────────

    public Optional<Medicine> findByBrandName(String brandName) {
        return medicineRepository.findByBrandNameIgnoreCase(brandName);
    }

    // ─────────────────────────────────────────
    // ADD MEDICINE — used by pharmacist service
    // ─────────────────────────────────────────

    public Medicine addMedicineFromPharmacist(
            MedicineRequest request, com.medicinedonation.model.User pharmacist) {

        // Duplicate check
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
                .pharmacistVerified(true)
                .verifiedBy(pharmacist)
                .build();

        return medicineRepository.save(medicine);
    }

    // ─────────────────────────────────────────
    // GET ALL MEDICINES — public listing
    // ─────────────────────────────────────────

    public List<MedicineResponse> getAllMedicines() {
        return medicineRepository.findAll()
                .stream()
                .map(this::mapToMedicineResponse)
                .collect(Collectors.toList());
    }

    // Get medicine by ID
    public MedicineResponse getMedicineById(Long id) {
        Medicine medicine = medicineRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(
                        "Medicine not found with id: " + id));
        return mapToMedicineResponse(medicine);
    }

    // ─────────────────────────────────────────
    // MAPPERS
    // ─────────────────────────────────────────

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

    private AvailableDonationResponse mapToAvailableDonation(
            Donation donation, String tier) {

        Medicine m = donation.getMedicine();

        return AvailableDonationResponse.builder()
                .donationId(donation.getId())
                .brandName(m.getBrandName())
                .apiName(m.getApiName())
                .strength(m.getStrength())
                .dosageForm(m.getDosageForm())
                .route(m.getRoute())
                .schedule(m.getSchedule().name())
                .quantity(donation.getQuantity())
                .expiryDate(donation.getExpiryDate())
                .doctorVerified(donation.getApprovedByDoctor() != null)
                .doctorName(donation.getApprovedByDoctor() != null ?
                        donation.getApprovedByDoctor().getName() : null)
                .collectionPointId(donation.getCollectionPoint() != null ?
                        donation.getCollectionPoint().getId() : null)
                .collectionPointName(donation.getCollectionPoint() != null ?
                        donation.getCollectionPoint().getLocationName() : null)
                .collectionPointAddress(donation.getCollectionPoint() != null ?
                        donation.getCollectionPoint().getAddress() : null)
                .collectionPointDistrict(donation.getCollectionPoint() != null ?
                        donation.getCollectionPoint().getDistrict() : null)
                .collectionPointPhone(donation.getCollectionPoint() != null ?
                        donation.getCollectionPoint().getPhone() : null)
                .matchTier(tier)
                .build();
    }
}*/

/*package com.medicinedonation.service;

import com.medicinedonation.dto.request.MedicineRequest;
import com.medicinedonation.dto.response.AvailableDonationResponse;
import com.medicinedonation.dto.response.MatchResultResponse;
import com.medicinedonation.dto.response.MedicineResponse;
import com.medicinedonation.enums.DonationStatus;
import com.medicinedonation.model.Donation;
import com.medicinedonation.model.Medicine;
import com.medicinedonation.repository.DonationRepository;
import com.medicinedonation.repository.MedicineRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class MedicineService {

    @Autowired
    private MedicineRepository medicineRepository;

    @Autowired
    private DonationRepository donationRepository;

    // ─────────────────────────────────────────
    // SEARCH BY BRAND NAME
    // ─────────────────────────────────────────

    public MatchResultResponse searchByBrandName(String brandName) {

        // Find medicine in DB by brand name
        Optional<Medicine> medicineOpt =
                medicineRepository.findByBrandNameIgnoreCase(brandName);

        // Medicine not found in DB at all
        if (medicineOpt.isEmpty()) {
            return MatchResultResponse.builder()
                    .matchTier("NO_MATCH")
                    .searchedBrandName(brandName)
                    .matches(new ArrayList<>())
                    .message("No medicine found with brand name: "
                            + brandName)
                    .build();
        }

        Medicine medicine = medicineOpt.get();

        // Run matching engine using API name from found medicine
        return runMatchingEngine(
                medicine.getApiName(),
                medicine.getStrength(),
                medicine.getDosageForm(),
                medicine.getRoute(),
                brandName,
                null
        );
    }

    // ─────────────────────────────────────────
    // SEARCH BY API NAME
    // ─────────────────────────────────────────

    public MatchResultResponse searchByApiName(String apiName) {

        // Check any medicine exists with this API name
        List<Medicine> medicines =
                medicineRepository.findByApiNameIgnoreCase(apiName);

        if (medicines.isEmpty()) {
            return MatchResultResponse.builder()
                    .matchTier("NO_MATCH")
                    .searchedApiName(apiName)
                    .matches(new ArrayList<>())
                    .message("No medicine found with API name: " + apiName)
                    .build();
        }

        // Use first match to get dosage form and route for Tier 2
        Medicine first = medicines.get(0);

        return runMatchingEngine(
                apiName,
                first.getStrength(),
                first.getDosageForm(),
                first.getRoute(),
                null,
                apiName
        );
    }

    // ─────────────────────────────────────────
    // MATCHING ENGINE — CORE LOGIC
    // ─────────────────────────────────────────

    private MatchResultResponse runMatchingEngine(
            String apiName,
            String strength,
            String dosageForm,
            String route,
            String searchedBrandName,
            String searchedApiName) {

        // ── TIER 1 — Exact Match ──────────────
        // Same API + same strength + same dosageForm + same route
        List<Medicine> tier1Medicines =
                medicineRepository
                        .findByApiNameIgnoreCaseAndStrengthIgnoreCaseAndDosageFormIgnoreCaseAndRouteIgnoreCase(
                                apiName, strength, dosageForm, route);

        // Get LIVE donations for tier 1 medicines
        List<AvailableDonationResponse> tier1Results =
                getLiveDonationsForMedicines(tier1Medicines, "TIER_1");

        if (!tier1Results.isEmpty()) {
            return MatchResultResponse.builder()
                    .matchTier("TIER_1")
                    .searchedBrandName(searchedBrandName)
                    .searchedApiName(searchedApiName)
                    .matches(tier1Results)
                    .message("Exact match found — " +
                            tier1Results.size() + " available")
                    .build();
        }

        // ── TIER 2 — Same API Different Strength ──
        // Same API + same dosageForm — different strength
        // NO auto conversion — user selects per prescription
        List<Medicine> tier2Medicines =
                medicineRepository
                        .findByApiNameIgnoreCaseAndDosageFormIgnoreCase(
                                apiName, dosageForm);

        // Remove exact strength match (already checked in Tier 1)
        tier2Medicines = tier2Medicines.stream()
                .filter(m -> !m.getStrength()
                        .equalsIgnoreCase(strength))
                .collect(Collectors.toList());

        List<AvailableDonationResponse> tier2Results =
                getLiveDonationsForMedicines(tier2Medicines, "TIER_2");

        if (!tier2Results.isEmpty()) {

            // Collect distinct available strengths to show clearly
            String availableStrengths = tier2Results.stream()
                    .map(AvailableDonationResponse::getStrength)
                    .distinct()
                    .collect(Collectors.joining(", "));

            return MatchResultResponse.builder()
                    .matchTier("TIER_2")
                    .searchedBrandName(searchedBrandName)
                    .searchedApiName(searchedApiName)
                    .matches(tier2Results)
                    .message("Exact strength (" + strength + ") is not " +
                            "currently available. Similar medicines found " +
                            "in different strengths: " + availableStrengths +
                            ". Please select as per your prescription.")
                    .build();
        }

        // ── NO MATCH ──────────────────────────
        return MatchResultResponse.builder()
                .matchTier("NO_MATCH")
                .searchedBrandName(searchedBrandName)
                .searchedApiName(searchedApiName)
                .matches(new ArrayList<>())
                .message("No available donations found for: " + apiName)
                .build();
    }

    // ─────────────────────────────────────────
    // GET LIVE DONATIONS FOR MEDICINES
    // ─────────────────────────────────────────

    private List<AvailableDonationResponse> getLiveDonationsForMedicines(
            List<Medicine> medicines, String tier) {

        List<AvailableDonationResponse> results = new ArrayList<>();

        for (Medicine medicine : medicines) {
            // Get all LIVE donations for this medicine
            List<Donation> liveDonations =
                    donationRepository.findByStatus(DonationStatus.LIVE)
                            .stream()
                            .filter(d -> d.getMedicine() != null &&
                                    d.getMedicine().getId()
                                            .equals(medicine.getId()))
                            .collect(Collectors.toList());

            for (Donation donation : liveDonations) {
                results.add(mapToAvailableDonation(donation, tier));
            }
        }

        return results;
    }

    // ─────────────────────────────────────────
    // CHECK IF MEDICINE EXISTS IN DB
    // Used by donation submission flow
    // ─────────────────────────────────────────

    public Optional<Medicine> findByBrandName(String brandName) {
        return medicineRepository.findByBrandNameIgnoreCase(brandName);
    }

    // ─────────────────────────────────────────
    // ADD MEDICINE — used by pharmacist service
    // ─────────────────────────────────────────

    public Medicine addMedicineFromPharmacist(
            MedicineRequest request,
            com.medicinedonation.model.User pharmacist) {

        // Duplicate check
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
                .pharmacistVerified(true)
                .verifiedBy(pharmacist)
                .build();

        return medicineRepository.save(medicine);
    }

    // ─────────────────────────────────────────
    // GET ALL MEDICINES — public listing
    // ─────────────────────────────────────────

    public List<MedicineResponse> getAllMedicines() {
        return medicineRepository.findAll()
                .stream()
                .map(this::mapToMedicineResponse)
                .collect(Collectors.toList());
    }

    // Get medicine by ID
    public MedicineResponse getMedicineById(Long id) {
        Medicine medicine = medicineRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(
                        "Medicine not found with id: " + id));
        return mapToMedicineResponse(medicine);
    }

    // ─────────────────────────────────────────
    // MAPPERS
    // ─────────────────────────────────────────

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

    private AvailableDonationResponse mapToAvailableDonation(
            Donation donation, String tier) {

        Medicine m = donation.getMedicine();

        return AvailableDonationResponse.builder()
                .donationId(donation.getId())
                .brandName(m.getBrandName())
                .apiName(m.getApiName())
                .strength(m.getStrength())
                .dosageForm(m.getDosageForm())
                .route(m.getRoute())
                .schedule(m.getSchedule().name())
                .quantity(donation.getQuantity())
                .expiryDate(donation.getExpiryDate())
                .doctorVerified(donation.getApprovedByDoctor() != null)
                .doctorName(donation.getApprovedByDoctor() != null ?
                        donation.getApprovedByDoctor().getName() : null)
                .collectionPointId(donation.getCollectionPoint() != null ?
                        donation.getCollectionPoint().getId() : null)
                .collectionPointName(donation.getCollectionPoint() != null ?
                        donation.getCollectionPoint().getLocationName() : null)
                .collectionPointAddress(
                        donation.getCollectionPoint() != null ?
                                donation.getCollectionPoint().getAddress() : null)
                .collectionPointDistrict(
                        donation.getCollectionPoint() != null ?
                                donation.getCollectionPoint().getDistrict() : null)
                .collectionPointPhone(
                        donation.getCollectionPoint() != null ?
                                donation.getCollectionPoint().getPhone() : null)
                .matchTier(tier)
                .build();
    }
}*/


/*package com.medicinedonation.service;

import com.medicinedonation.dto.request.MedicineRequest;
import com.medicinedonation.dto.response.AvailableDonationResponse;
import com.medicinedonation.dto.response.MatchResultResponse;
import com.medicinedonation.dto.response.MedicineResponse;
import com.medicinedonation.enums.DonationStatus;
import com.medicinedonation.model.Donation;
import com.medicinedonation.model.Medicine;
import com.medicinedonation.repository.DonationRepository;
import com.medicinedonation.repository.MedicineRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class MedicineService {

    @Autowired
    private MedicineRepository medicineRepository;

    @Autowired
    private DonationRepository donationRepository;

    // ─────────────────────────────────────────
    // SEARCH BY BRAND NAME
    // ─────────────────────────────────────────

    public MatchResultResponse searchByBrandName(String brandName) {

        // ✅ FIXED — now returns List, pick first match
        List<Medicine> medicines =
                medicineRepository.findByBrandNameIgnoreCase(brandName);

        if (medicines.isEmpty()) {
            return MatchResultResponse.builder()
                    .matchTier("NO_MATCH")
                    .searchedBrandName(brandName)
                    .matches(new ArrayList<>())
                    .message("No medicine found with brand name: " + brandName)
                    .build();
        }

        // Use first match to drive the matching engine
        Medicine medicine = medicines.get(0);

        return runMatchingEngine(
                medicine.getApiName(),
                medicine.getStrength(),
                medicine.getDosageForm(),
                medicine.getRoute(),
                brandName,
                null
        );
    }

    // ─────────────────────────────────────────
    // SEARCH BY API NAME
    // ─────────────────────────────────────────

    public MatchResultResponse searchByApiName(String apiName) {

        List<Medicine> medicines =
                medicineRepository.findByApiNameIgnoreCase(apiName);

        if (medicines.isEmpty()) {
            return MatchResultResponse.builder()
                    .matchTier("NO_MATCH")
                    .searchedApiName(apiName)
                    .matches(new ArrayList<>())
                    .message("No medicine found with API name: " + apiName)
                    .build();
        }

        Medicine first = medicines.get(0);

        return runMatchingEngine(
                apiName,
                first.getStrength(),
                first.getDosageForm(),
                first.getRoute(),
                null,
                apiName
        );
    }

    // ─────────────────────────────────────────
    // MATCHING ENGINE — CORE LOGIC
    // ─────────────────────────────────────────

    private MatchResultResponse runMatchingEngine(
            String apiName,
            String strength,
            String dosageForm,
            String route,
            String searchedBrandName,
            String searchedApiName) {

        // ── TIER 1 — Exact Match ──────────────
        List<Medicine> tier1Medicines =
                medicineRepository
                        .findByApiNameIgnoreCaseAndStrengthIgnoreCaseAndDosageFormIgnoreCaseAndRouteIgnoreCase(
                                apiName, strength, dosageForm, route);

        List<AvailableDonationResponse> tier1Results =
                getLiveDonationsForMedicines(tier1Medicines, "TIER_1");

        if (!tier1Results.isEmpty()) {
            return MatchResultResponse.builder()
                    .matchTier("TIER_1")
                    .searchedBrandName(searchedBrandName)
                    .searchedApiName(searchedApiName)
                    .matches(tier1Results)
                    .message("Exact match found — " +
                            tier1Results.size() + " available")
                    .build();
        }

        // ── TIER 2 — Same API Different Strength ──
        List<Medicine> tier2Medicines =
                medicineRepository
                        .findByApiNameIgnoreCaseAndDosageFormIgnoreCase(
                                apiName, dosageForm);

        tier2Medicines = tier2Medicines.stream()
                .filter(m -> !m.getStrength().equalsIgnoreCase(strength))
                .collect(Collectors.toList());

        List<AvailableDonationResponse> tier2Results =
                getLiveDonationsForMedicines(tier2Medicines, "TIER_2");

        if (!tier2Results.isEmpty()) {
            String availableStrengths = tier2Results.stream()
                    .map(AvailableDonationResponse::getStrength)
                    .distinct()
                    .collect(Collectors.joining(", "));

            return MatchResultResponse.builder()
                    .matchTier("TIER_2")
                    .searchedBrandName(searchedBrandName)
                    .searchedApiName(searchedApiName)
                    .matches(tier2Results)
                    .message("Exact strength (" + strength + ") is not " +
                            "currently available. Similar medicines found " +
                            "in different strengths: " + availableStrengths +
                            ". Please select as per your prescription.")
                    .build();
        }

        // ── NO MATCH ──────────────────────────
        return MatchResultResponse.builder()
                .matchTier("NO_MATCH")
                .searchedBrandName(searchedBrandName)
                .searchedApiName(searchedApiName)
                .matches(new ArrayList<>())
                .message("No available donations found for: " + apiName)
                .build();
    }

    // ─────────────────────────────────────────
    // GET LIVE DONATIONS FOR MEDICINES
    // ─────────────────────────────────────────

    private List<AvailableDonationResponse> getLiveDonationsForMedicines(
            List<Medicine> medicines, String tier) {

        List<AvailableDonationResponse> results = new ArrayList<>();

        for (Medicine medicine : medicines) {
            List<Donation> liveDonations =
                    donationRepository.findByStatus(DonationStatus.LIVE)
                            .stream()
                            .filter(d -> d.getMedicine() != null &&
                                    d.getMedicine().getId()
                                            .equals(medicine.getId()))
                            .collect(Collectors.toList());

            for (Donation donation : liveDonations) {
                results.add(mapToAvailableDonation(donation, tier));
            }
        }

        return results;
    }

    // ─────────────────────────────────────────
    // CHECK IF MEDICINE EXISTS IN DB
    // Used by donation submission flow
    // ✅ FIXED — returns Optional by checking if list is non-empty
    // ─────────────────────────────────────────

    public Optional<Medicine> findByBrandName(String brandName) {
        List<Medicine> matches =
                medicineRepository.findByBrandNameIgnoreCase(brandName);
        return matches.isEmpty() ? Optional.empty() : Optional.of(matches.get(0));
    }

    // ─────────────────────────────────────────
    // ADD MEDICINE — used by pharmacist service
    // ─────────────────────────────────────────

    public Medicine addMedicineFromPharmacist(
            MedicineRequest request,
            com.medicinedonation.model.User pharmacist) {

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
                .pharmacistVerified(true)
                .verifiedBy(pharmacist)
                .build();

        return medicineRepository.save(medicine);
    }

    // ─────────────────────────────────────────
    // GET ALL MEDICINES — public listing
    // ─────────────────────────────────────────

    public List<MedicineResponse> getAllMedicines() {
        return medicineRepository.findAll()
                .stream()
                .map(this::mapToMedicineResponse)
                .collect(Collectors.toList());
    }

    public MedicineResponse getMedicineById(Long id) {
        Medicine medicine = medicineRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(
                        "Medicine not found with id: " + id));
        return mapToMedicineResponse(medicine);
    }

    // ─────────────────────────────────────────
    // MAPPERS
    // ─────────────────────────────────────────

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

    private AvailableDonationResponse mapToAvailableDonation(
            Donation donation, String tier) {

        Medicine m = donation.getMedicine();

        return AvailableDonationResponse.builder()
                .donationId(donation.getId())
                .brandName(m.getBrandName())
                .apiName(m.getApiName())
                .strength(m.getStrength())
                .dosageForm(m.getDosageForm())
                .route(m.getRoute())
                .schedule(m.getSchedule().name())
                .quantity(donation.getQuantity())
                .expiryDate(donation.getExpiryDate())
                .doctorVerified(donation.getApprovedByDoctor() != null)
                .doctorName(donation.getApprovedByDoctor() != null ?
                        donation.getApprovedByDoctor().getName() : null)
                .collectionPointId(donation.getCollectionPoint() != null ?
                        donation.getCollectionPoint().getId() : null)
                .collectionPointName(donation.getCollectionPoint() != null ?
                        donation.getCollectionPoint().getLocationName() : null)
                .collectionPointAddress(
                        donation.getCollectionPoint() != null ?
                                donation.getCollectionPoint().getAddress() : null)
                .collectionPointDistrict(
                        donation.getCollectionPoint() != null ?
                                donation.getCollectionPoint().getDistrict() : null)
                .collectionPointPhone(
                        donation.getCollectionPoint() != null ?
                                donation.getCollectionPoint().getPhone() : null)
                .matchTier(tier)
                .build();
    }
}*/


/*package com.medicinedonation.service;

import com.medicinedonation.dto.request.MedicineRequest;
import com.medicinedonation.dto.response.AvailableDonationResponse;
import com.medicinedonation.dto.response.MatchResultResponse;
import com.medicinedonation.dto.response.MedicineResponse;
import com.medicinedonation.enums.DonationStatus;
import com.medicinedonation.model.Donation;
import com.medicinedonation.model.Medicine;
import com.medicinedonation.repository.DonationRepository;
import com.medicinedonation.repository.MedicineRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class MedicineService {

    @Autowired
    private MedicineRepository medicineRepository;

    @Autowired
    private DonationRepository donationRepository;

    // ─────────────────────────────────────────
    // NORMALIZE
    // ─────────────────────────────────────────

    private String normalize(String s) {
        return s == null ? null : s.trim().replaceAll("\\s+", " ");
    }

    private String normalizeStrength(String s) {
        return s == null ? null : s.trim().replaceAll("\\s+", "").toLowerCase();
    }

    // ─────────────────────────────────────────
    // SEARCH BY BRAND NAME + OPTIONAL STRENGTH
    // e.g. "Panadol" or "Panadol 500mg"
    // ─────────────────────────────────────────

    public MatchResultResponse searchByBrandName(String brandName, String strength) {

        String normBrand = normalize(brandName);

        // Find all medicines in DB with this brand name
        List<Medicine> byBrand = medicineRepository.findByBrandNameIgnoreCase(normBrand);

        if (byBrand.isEmpty()) {
            return MatchResultResponse.builder()
                    .matchTier("NO_MATCH")
                    .searchedBrandName(normBrand)
                    .matches(new ArrayList<>())
                    .message("No medicine found with brand name: " + normBrand)
                    .build();
        }

        String apiName = byBrand.get(0).getApiName();

        // If strength provided → use Tier 1/2 matching logic
        // If NO strength → show ALL live donations for this API (all strengths, all brands)
        if (strength == null || strength.isBlank()) {
            return searchAllStrengths(apiName, normBrand, null);
        }

        String dosageForm = byBrand.get(0).getDosageForm();
        String route = byBrand.get(0).getRoute();
        return runMatchingEngine(apiName, normalize(strength), dosageForm, route, normBrand, null);
    }

    // ─────────────────────────────────────────
    // SEARCH BY API NAME + OPTIONAL STRENGTH
    // e.g. "Paracetamol" or "Paracetamol 500mg"
    // ─────────────────────────────────────────

    public MatchResultResponse searchByApiName(String apiName, String strength) {

        String normApi = normalize(apiName);
        List<Medicine> medicines = medicineRepository.findByApiNameIgnoreCase(normApi);

        if (medicines.isEmpty()) {
            return MatchResultResponse.builder()
                    .matchTier("NO_MATCH")
                    .searchedApiName(normApi)
                    .matches(new ArrayList<>())
                    .message("No medicine found with generic name: " + normApi)
                    .build();
        }

        // If strength provided → use Tier 1/2 matching logic
        // If NO strength → show ALL live donations for this API (all strengths, all brands)
        if (strength == null || strength.isBlank()) {
            return searchAllStrengths(normApi, null, normApi);
        }

        Medicine first = medicines.get(0);
        String dosageForm = first.getDosageForm();
        String route = first.getRoute();
        return runMatchingEngine(normApi, normalize(strength), dosageForm, route, null, normApi);
    }

    // ─────────────────────────────────────────
    // SEARCH ALL STRENGTHS — no strength filter
    // Shows ALL live donations for this API name
    // Used when recipient searches without specifying strength
    // ─────────────────────────────────────────

    private MatchResultResponse searchAllStrengths(
            String apiName, String searchedBrandName, String searchedApiName) {

        List<Medicine> allForApi = medicineRepository.findByApiNameIgnoreCase(apiName);
        List<AvailableDonationResponse> allResults =
                getLiveDonationsForMedicines(allForApi, "TIER_1");

        if (!allResults.isEmpty()) {
            String strengths = allResults.stream()
                    .map(AvailableDonationResponse::getStrength)
                    .distinct()
                    .collect(Collectors.joining(", "));
            return MatchResultResponse.builder()
                    .matchTier("TIER_1")
                    .searchedBrandName(searchedBrandName)
                    .searchedApiName(searchedApiName)
                    .matches(allResults)
                    .message("Found " + allResults.size() + " donation"
                            + (allResults.size() > 1 ? "s" : "")
                            + " — available strengths: " + strengths)
                    .build();
        }

        return MatchResultResponse.builder()
                .matchTier("NO_MATCH")
                .searchedBrandName(searchedBrandName)
                .searchedApiName(searchedApiName)
                .matches(new ArrayList<>())
                .message("No available donations found for: " + apiName)
                .build();
    }

    // ─────────────────────────────────────────
    // MATCHING ENGINE — CORE LOGIC
    // Tier 1: same API + same strength (all brands)
    // Tier 2: same API + different strength (all brands)
    // ─────────────────────────────────────────

    private MatchResultResponse runMatchingEngine(
            String apiName,
            String strength,
            String dosageForm,
            String route,
            String searchedBrandName,
            String searchedApiName) {

        String normStrength = normalizeStrength(strength);

        // ── TIER 1 — Same API + Same Strength (any brand, any dosage form) ──
        // e.g. search "Panadol 500mg" → show Panadol 500mg, Calpol 500mg, Crocin 500mg...
        List<Medicine> allSameApi = medicineRepository.findByApiNameIgnoreCase(apiName);

        List<Medicine> tier1Medicines = allSameApi.stream()
                .filter(m -> normalizeStrength(m.getStrength()).equals(normStrength))
                .collect(Collectors.toList());

        List<AvailableDonationResponse> tier1Results =
                getLiveDonationsForMedicines(tier1Medicines, "TIER_1");

        if (!tier1Results.isEmpty()) {
            return MatchResultResponse.builder()
                    .matchTier("TIER_1")
                    .searchedBrandName(searchedBrandName)
                    .searchedApiName(searchedApiName)
                    .matches(tier1Results)
                    .message("Found " + tier1Results.size() + " donation"
                            + (tier1Results.size() > 1 ? "s" : "")
                            + " with exact strength (" + strength + ")")
                    .build();
        }

        // ── TIER 2 — Same API + Different Strength ──
        // e.g. 500mg not available → show 250mg, 125mg, etc.
        List<Medicine> tier2Medicines = allSameApi.stream()
                .filter(m -> !normalizeStrength(m.getStrength()).equals(normStrength))
                .collect(Collectors.toList());

        List<AvailableDonationResponse> tier2Results =
                getLiveDonationsForMedicines(tier2Medicines, "TIER_2");

        if (!tier2Results.isEmpty()) {
            String availableStrengths = tier2Results.stream()
                    .map(AvailableDonationResponse::getStrength)
                    .distinct()
                    .collect(Collectors.joining(", "));

            return MatchResultResponse.builder()
                    .matchTier("TIER_2")
                    .searchedBrandName(searchedBrandName)
                    .searchedApiName(searchedApiName)
                    .matches(tier2Results)
                    .message("Exact strength (" + strength + ") not available. "
                            + "Similar medicines found in: " + availableStrengths
                            + " — please check with your doctor before substituting.")
                    .build();
        }

        // ── NO MATCH ──────────────────────────
        return MatchResultResponse.builder()
                .matchTier("NO_MATCH")
                .searchedBrandName(searchedBrandName)
                .searchedApiName(searchedApiName)
                .matches(new ArrayList<>())
                .message("No available donations found for: " + apiName)
                .build();
    }

    // ─────────────────────────────────────────
    // GET LIVE DONATIONS FOR MEDICINES
    // ─────────────────────────────────────────

    private List<AvailableDonationResponse> getLiveDonationsForMedicines(
            List<Medicine> medicines, String tier) {

        List<AvailableDonationResponse> results = new ArrayList<>();

        for (Medicine medicine : medicines) {
            donationRepository.findByStatus(DonationStatus.LIVE)
                    .stream()
                    .filter(d -> d.getMedicine() != null &&
                            d.getMedicine().getId().equals(medicine.getId()))
                    .forEach(donation -> results.add(
                            mapToAvailableDonation(donation, tier)));
        }

        return results;
    }

    // ─────────────────────────────────────────
    // CHECK IF MEDICINE EXISTS IN DB
    // ─────────────────────────────────────────

    public Optional<Medicine> findByBrandName(String brandName) {
        return medicineRepository.findByBrandNameIgnoreCase(normalize(brandName))
                .stream().findFirst();
    }

    public List<Medicine> findAllByBrandName(String brandName) {
        return medicineRepository.findByBrandNameIgnoreCase(normalize(brandName));
    }

    // ─────────────────────────────────────────
    // ADD MEDICINE — used by pharmacist service
    // ─────────────────────────────────────────

    public Medicine addMedicineFromPharmacist(
            MedicineRequest request,
            com.medicinedonation.model.User pharmacist) {

        String normBrand    = normalize(request.getBrandName());
        String normApi      = normalize(request.getApiName());
        String normStrength = normalize(request.getStrength());
        String normDosage   = normalize(request.getDosageForm());
        String normRoute    = normalize(request.getRoute());

        boolean exists = medicineRepository
                .existsByBrandNameIgnoreCaseAndApiNameIgnoreCaseAndStrengthIgnoreCaseAndDosageFormIgnoreCaseAndRouteIgnoreCase(
                        normBrand, normApi, normStrength, normDosage, normRoute);

        if (exists) {
            throw new RuntimeException(
                    "Medicine already exists in database: " +
                            normBrand + " " + normStrength + " " + normDosage);
        }

        Medicine medicine = Medicine.builder()
                .brandName(normBrand)
                .apiName(normApi)
                .strength(normStrength)
                .dosageForm(normDosage)
                .route(normRoute)
                .schedule(request.getSchedule())
                .pharmacistVerified(true)
                .verifiedBy(pharmacist)
                .build();

        return medicineRepository.save(medicine);
    }

    // ─────────────────────────────────────────
    // GET ALL MEDICINES
    // ─────────────────────────────────────────

    public List<MedicineResponse> getAllMedicines() {
        return medicineRepository.findAll()
                .stream()
                .map(this::mapToMedicineResponse)
                .collect(Collectors.toList());
    }

    public MedicineResponse getMedicineById(Long id) {
        Medicine medicine = medicineRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(
                        "Medicine not found with id: " + id));
        return mapToMedicineResponse(medicine);
    }

    // ─────────────────────────────────────────
    // MAPPERS
    // ─────────────────────────────────────────

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

    private AvailableDonationResponse mapToAvailableDonation(
            Donation donation, String tier) {

        Medicine m = donation.getMedicine();

        return AvailableDonationResponse.builder()
                .donationId(donation.getId())
                .brandName(m.getBrandName())
                .apiName(m.getApiName())
                .strength(m.getStrength())
                .dosageForm(m.getDosageForm())
                .route(m.getRoute())
                .schedule(m.getSchedule().name())
                .quantity(donation.getQuantity())
                .expiryDate(donation.getExpiryDate())
                .doctorVerified(donation.getApprovedByDoctor() != null)
                .doctorName(donation.getApprovedByDoctor() != null ?
                        donation.getApprovedByDoctor().getName() : null)
                .collectionPointId(donation.getCollectionPoint() != null ?
                        donation.getCollectionPoint().getId() : null)
                .collectionPointName(donation.getCollectionPoint() != null ?
                        donation.getCollectionPoint().getLocationName() : null)
                .collectionPointAddress(donation.getCollectionPoint() != null ?
                        donation.getCollectionPoint().getAddress() : null)
                .collectionPointDistrict(donation.getCollectionPoint() != null ?
                        donation.getCollectionPoint().getDistrict() : null)
                .collectionPointPhone(donation.getCollectionPoint() != null ?
                        donation.getCollectionPoint().getPhone() : null)
                .matchTier(tier)
                .build();
    }
}*/

package com.medicinedonation.service;

import com.medicinedonation.dto.request.MedicineRequest;
import com.medicinedonation.dto.response.AvailableDonationResponse;
import com.medicinedonation.dto.response.MatchResultResponse;
import com.medicinedonation.dto.response.MedicineResponse;
import com.medicinedonation.enums.DonationStatus;
import com.medicinedonation.model.Donation;
import com.medicinedonation.model.Medicine;
import com.medicinedonation.repository.DonationRepository;
import com.medicinedonation.repository.MedicineRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class MedicineService {

    @Autowired
    private MedicineRepository medicineRepository;

    @Autowired
    private DonationRepository donationRepository;

    // ─────────────────────────────────────────
    // NORMALIZE
    // ─────────────────────────────────────────

    private String normalize(String s) {
        return s == null ? null : s.trim().replaceAll("\\s+", " ");
    }

    private String normalizeStrength(String s) {
        return s == null ? null : s.trim().replaceAll("\\s+", "").toLowerCase();
    }

    // ─────────────────────────────────────────
    // SEARCH BY BRAND NAME + OPTIONAL STRENGTH
    // e.g. "Panadol" or "Panadol 500mg"
    // ─────────────────────────────────────────

    public MatchResultResponse searchByBrandName(String brandName, String strength, Long collectionPointId) {

        String normBrand = normalize(brandName);

        List<Medicine> byBrand = medicineRepository.findByBrandNameIgnoreCase(normBrand);

        if (byBrand.isEmpty()) {
            return MatchResultResponse.builder()
                    .matchTier("NO_MATCH")
                    .searchedBrandName(normBrand)
                    .matches(new ArrayList<>())
                    .message("No medicine found with brand name: " + normBrand)
                    .build();
        }

        String apiName = byBrand.get(0).getApiName();

        if (strength == null || strength.isBlank()) {
            return searchAllStrengths(apiName, normBrand, null, collectionPointId);
        }

        String dosageForm = byBrand.get(0).getDosageForm();
        String route = byBrand.get(0).getRoute();
        return runMatchingEngine(apiName, normalize(strength), dosageForm, route, normBrand, null, collectionPointId);
    }

    // ─────────────────────────────────────────
    // SEARCH BY API NAME + OPTIONAL STRENGTH
    // e.g. "Paracetamol" or "Paracetamol 500mg"
    // ─────────────────────────────────────────

    public MatchResultResponse searchByApiName(String apiName, String strength, Long collectionPointId) {

        String normApi = normalize(apiName);
        List<Medicine> medicines = medicineRepository.findByApiNameIgnoreCase(normApi);

        if (medicines.isEmpty()) {
            return MatchResultResponse.builder()
                    .matchTier("NO_MATCH")
                    .searchedApiName(normApi)
                    .matches(new ArrayList<>())
                    .message("No medicine found with generic name: " + normApi)
                    .build();
        }

        if (strength == null || strength.isBlank()) {
            return searchAllStrengths(normApi, null, normApi, collectionPointId);
        }

        Medicine first = medicines.get(0);
        String dosageForm = first.getDosageForm();
        String route = first.getRoute();
        return runMatchingEngine(normApi, normalize(strength), dosageForm, route, null, normApi, collectionPointId);
    }

    // ─────────────────────────────────────────
    // SEARCH ALL STRENGTHS — no strength filter
    // Shows ALL live donations for this API name
    // Used when recipient searches without specifying strength
    // ─────────────────────────────────────────

    private MatchResultResponse searchAllStrengths(
            String apiName, String searchedBrandName, String searchedApiName,
            Long collectionPointId) {

        List<Medicine> allForApi = medicineRepository.findByApiNameIgnoreCase(apiName);
        List<AvailableDonationResponse> allResults =
                getLiveDonationsForMedicines(allForApi, "TIER_1", collectionPointId);

        if (!allResults.isEmpty()) {
            String strengths = allResults.stream()
                    .map(AvailableDonationResponse::getStrength)
                    .distinct()
                    .collect(Collectors.joining(", "));
            return MatchResultResponse.builder()
                    .matchTier("TIER_1")
                    .searchedBrandName(searchedBrandName)
                    .searchedApiName(searchedApiName)
                    .matches(allResults)
                    .message("Found " + allResults.size() + " donation"
                            + (allResults.size() > 1 ? "s" : "")
                            + " — available strengths: " + strengths)
                    .build();
        }

        return MatchResultResponse.builder()
                .matchTier("NO_MATCH")
                .searchedBrandName(searchedBrandName)
                .searchedApiName(searchedApiName)
                .matches(new ArrayList<>())
                .message("No available donations found for: " + apiName)
                .build();
    }

    // ─────────────────────────────────────────
    // MATCHING ENGINE — CORE LOGIC
    // Tier 1: same API + same strength (all brands)
    // Tier 2: same API + different strength (all brands)
    // ─────────────────────────────────────────

    private MatchResultResponse runMatchingEngine(
            String apiName,
            String strength,
            String dosageForm,
            String route,
            String searchedBrandName,
            String searchedApiName,
            Long collectionPointId) {

        String normStrength = normalizeStrength(strength);

        List<Medicine> allSameApi = medicineRepository.findByApiNameIgnoreCase(apiName);

        List<Medicine> tier1Medicines = allSameApi.stream()
                .filter(m -> normalizeStrength(m.getStrength()).equals(normStrength))
                .collect(Collectors.toList());

        List<AvailableDonationResponse> tier1Results =
                getLiveDonationsForMedicines(tier1Medicines, "TIER_1", collectionPointId);

        if (!tier1Results.isEmpty()) {
            return MatchResultResponse.builder()
                    .matchTier("TIER_1")
                    .searchedBrandName(searchedBrandName)
                    .searchedApiName(searchedApiName)
                    .matches(tier1Results)
                    .message("Found " + tier1Results.size() + " donation"
                            + (tier1Results.size() > 1 ? "s" : "")
                            + " with exact strength (" + strength + ")")
                    .build();
        }

        List<Medicine> tier2Medicines = allSameApi.stream()
                .filter(m -> !normalizeStrength(m.getStrength()).equals(normStrength))
                .collect(Collectors.toList());

        List<AvailableDonationResponse> tier2Results =
                getLiveDonationsForMedicines(tier2Medicines, "TIER_2", collectionPointId);

        if (!tier2Results.isEmpty()) {
            String availableStrengths = tier2Results.stream()
                    .map(AvailableDonationResponse::getStrength)
                    .distinct()
                    .collect(Collectors.joining(", "));

            return MatchResultResponse.builder()
                    .matchTier("TIER_2")
                    .searchedBrandName(searchedBrandName)
                    .searchedApiName(searchedApiName)
                    .matches(tier2Results)
                    .message("Exact strength (" + strength + ") not available. "
                            + "Similar medicines found in: " + availableStrengths
                            + " — please check with your doctor before substituting.")
                    .build();
        }

        // ── NO MATCH ──────────────────────────
        return MatchResultResponse.builder()
                .matchTier("NO_MATCH")
                .searchedBrandName(searchedBrandName)
                .searchedApiName(searchedApiName)
                .matches(new ArrayList<>())
                .message("No available donations found for: " + apiName)
                .build();
    }

    // ─────────────────────────────────────────
    // GET LIVE DONATIONS FOR MEDICINES
    // ─────────────────────────────────────────

    private List<AvailableDonationResponse> getLiveDonationsForMedicines(
            List<Medicine> medicines, String tier, Long collectionPointId) {

        List<AvailableDonationResponse> results = new ArrayList<>();

        for (Medicine medicine : medicines) {
            donationRepository.findByStatus(DonationStatus.LIVE)
                    .stream()
                    .filter(d -> d.getMedicine() != null &&
                            d.getMedicine().getId().equals(medicine.getId()))
                    .filter(d -> collectionPointId == null ||
                            (d.getCollectionPoint() != null &&
                                    d.getCollectionPoint().getId().equals(collectionPointId)))
                    .forEach(donation -> results.add(
                            mapToAvailableDonation(donation, tier)));
        }

        return results;
    }

    // ─────────────────────────────────────────
    // CHECK IF MEDICINE EXISTS IN DB
    // ─────────────────────────────────────────

    public Optional<Medicine> findByBrandName(String brandName) {
        return medicineRepository.findByBrandNameIgnoreCase(normalize(brandName))
                .stream().findFirst();
    }

    public List<Medicine> findAllByBrandName(String brandName) {
        return medicineRepository.findByBrandNameIgnoreCase(normalize(brandName));
    }

    // ─────────────────────────────────────────
    // ADD MEDICINE — used by pharmacist service
    // ─────────────────────────────────────────

    public Medicine addMedicineFromPharmacist(
            MedicineRequest request,
            com.medicinedonation.model.User pharmacist) {

        String normBrand    = normalize(request.getBrandName());
        String normApi      = normalize(request.getApiName());
        String normStrength = normalize(request.getStrength());
        String normDosage   = normalize(request.getDosageForm());
        String normRoute    = normalize(request.getRoute());

        boolean exists = medicineRepository
                .existsByBrandNameIgnoreCaseAndApiNameIgnoreCaseAndStrengthIgnoreCaseAndDosageFormIgnoreCaseAndRouteIgnoreCase(
                        normBrand, normApi, normStrength, normDosage, normRoute);

        if (exists) {
            throw new RuntimeException(
                    "Medicine already exists in database: " +
                            normBrand + " " + normStrength + " " + normDosage);
        }

        Medicine medicine = Medicine.builder()
                .brandName(normBrand)
                .apiName(normApi)
                .strength(normStrength)
                .dosageForm(normDosage)
                .route(normRoute)
                .schedule(request.getSchedule())
                .pharmacistVerified(true)
                .verifiedBy(pharmacist)
                .build();

        return medicineRepository.save(medicine);
    }

    // ─────────────────────────────────────────
    // GET ALL MEDICINES
    // ─────────────────────────────────────────

    public List<MedicineResponse> getAllMedicines() {
        return medicineRepository.findAll()
                .stream()
                .map(this::mapToMedicineResponse)
                .collect(Collectors.toList());
    }

    public MedicineResponse getMedicineById(Long id) {
        Medicine medicine = medicineRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(
                        "Medicine not found with id: " + id));
        return mapToMedicineResponse(medicine);
    }

    // ─────────────────────────────────────────
    // MAPPERS
    // ─────────────────────────────────────────

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

    private AvailableDonationResponse mapToAvailableDonation(
            Donation donation, String tier) {

        Medicine m = donation.getMedicine();

        return AvailableDonationResponse.builder()
                .donationId(donation.getId())
                .brandName(m.getBrandName())
                .apiName(m.getApiName())
                .strength(m.getStrength())
                .dosageForm(m.getDosageForm())
                .route(m.getRoute())
                .schedule(m.getSchedule().name())
                .quantity(donation.getQuantity())
                .expiryDate(donation.getExpiryDate())
                .doctorVerified(donation.getApprovedByDoctor() != null)
                .doctorName(donation.getApprovedByDoctor() != null ?
                        donation.getApprovedByDoctor().getName() : null)
                .collectionPointId(donation.getCollectionPoint() != null ?
                        donation.getCollectionPoint().getId() : null)
                .collectionPointName(donation.getCollectionPoint() != null ?
                        donation.getCollectionPoint().getLocationName() : null)
                .collectionPointAddress(donation.getCollectionPoint() != null ?
                        donation.getCollectionPoint().getAddress() : null)
                .collectionPointDistrict(donation.getCollectionPoint() != null ?
                        donation.getCollectionPoint().getDistrict() : null)
                .collectionPointPhone(donation.getCollectionPoint() != null ?
                        donation.getCollectionPoint().getPhone() : null)
                .matchTier(tier)
                .build();
    }
}