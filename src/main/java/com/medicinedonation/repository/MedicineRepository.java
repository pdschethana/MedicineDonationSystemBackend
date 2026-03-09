/*package com.medicinedonation.repository;

import com.medicinedonation.model.Medicine;
import com.medicinedonation.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface MedicineRepository extends JpaRepository<Medicine, Long> {

    // Tier 1 — exact match
    List<Medicine> findByApiNameIgnoreCaseAndStrengthIgnoreCaseAndDosageFormIgnoreCaseAndRouteIgnoreCase(
            String apiName, String strength, String dosageForm, String route);

    // Tier 2 — same API different strength
    List<Medicine> findByApiNameIgnoreCaseAndDosageFormIgnoreCase(
            String apiName, String dosageForm);

    // Search by brand name
    Optional<Medicine> findByBrandNameIgnoreCase(String brandName);

    // Search by API name
    List<Medicine> findByApiNameIgnoreCase(String apiName);

    // Pharmacist verified
    List<Medicine> findByPharmacistVerified(boolean pharmacistVerified);

    // By verifier
    List<Medicine> findByVerifiedBy(User verifiedBy);
}*/

/*package com.medicinedonation.repository;

import com.medicinedonation.model.Medicine;
import com.medicinedonation.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface MedicineRepository extends JpaRepository<Medicine, Long> {

    // ─────────────────────────────────────────
    // MATCHING ENGINE
    // ─────────────────────────────────────────

    // Tier 1 — exact match
    List<Medicine> findByApiNameIgnoreCaseAndStrengthIgnoreCaseAndDosageFormIgnoreCaseAndRouteIgnoreCase(
            String apiName,
            String strength,
            String dosageForm,
            String route
    );

    // Tier 2 — same API different strength
    List<Medicine> findByApiNameIgnoreCaseAndDosageFormIgnoreCase(
            String apiName,
            String dosageForm
    );

    // ─────────────────────────────────────────
    // SEARCH
    // ─────────────────────────────────────────

    // Search by brand name
    Optional<Medicine> findByBrandNameIgnoreCase(String brandName);

    // Search by API name
    List<Medicine> findByApiNameIgnoreCase(String apiName);

    // ─────────────────────────────────────────
    // FILTERS
    // ─────────────────────────────────────────

    // Pharmacist verified medicines
    List<Medicine> findByPharmacistVerified(boolean pharmacistVerified);

    // By verifier
    List<Medicine> findByVerifiedBy(User verifiedBy);

    // ─────────────────────────────────────────
    // DUPLICATE CHECK
    // ─────────────────────────────────────────

    // Check exact duplicate — brand + api + strength + dosageForm + route
    boolean existsByBrandNameIgnoreCaseAndApiNameIgnoreCaseAndStrengthIgnoreCaseAndDosageFormIgnoreCaseAndRouteIgnoreCase(
            String brandName,
            String apiName,
            String strength,
            String dosageForm,
            String route
    );
}*/

package com.medicinedonation.repository;

import com.medicinedonation.model.Medicine;
import com.medicinedonation.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface MedicineRepository extends JpaRepository<Medicine, Long> {

    // ─────────────────────────────────────────
    // MATCHING ENGINE
    // ─────────────────────────────────────────

    // Tier 1 — exact match
    List<Medicine> findByApiNameIgnoreCaseAndStrengthIgnoreCaseAndDosageFormIgnoreCaseAndRouteIgnoreCase(
            String apiName,
            String strength,
            String dosageForm,
            String route
    );

    // Tier 2 — same API different strength
    List<Medicine> findByApiNameIgnoreCaseAndDosageFormIgnoreCase(
            String apiName,
            String dosageForm
    );

    // ─────────────────────────────────────────
    // SEARCH
    // ─────────────────────────────────────────

    Optional<Medicine> findByBrandNameIgnoreCase(String brandName);
    List<Medicine> findByApiNameIgnoreCase(String apiName);

    // ─────────────────────────────────────────
    // FILTERS
    // ─────────────────────────────────────────

    List<Medicine> findByPharmacistVerified(boolean pharmacistVerified);
    List<Medicine> findByVerifiedBy(User verifiedBy);

    // ─────────────────────────────────────────
    // DUPLICATE CHECK
    // ─────────────────────────────────────────

    boolean existsByBrandNameIgnoreCaseAndApiNameIgnoreCaseAndStrengthIgnoreCaseAndDosageFormIgnoreCaseAndRouteIgnoreCase(
            String brandName,
            String apiName,
            String strength,
            String dosageForm,
            String route
    );

    // ✅ NEW — used by doctor to check exact match before approving
    // Checks brand name + strength + dosage form (3-way match)
    Optional<Medicine> findByBrandNameIgnoreCaseAndStrengthIgnoreCaseAndDosageFormIgnoreCase(
            String brandName,
            String strength,
            String dosageForm
    );
}