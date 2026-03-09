/*package com.medicinedonation.repository;

import com.medicinedonation.model.PendingMedicine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PendingMedicineRepository
        extends JpaRepository<PendingMedicine, Long> {

    List<PendingMedicine> findByResolved(boolean resolved);
    List<PendingMedicine> findByRejected(boolean rejected);
    List<PendingMedicine> findByResolvedFalseAndRejectedFalse();
    long countByResolvedFalseAndRejectedFalse();
}*/


package com.medicinedonation.repository;

import com.medicinedonation.model.PendingMedicine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PendingMedicineRepository
        extends JpaRepository<PendingMedicine, Long> {

    List<PendingMedicine> findByResolved(boolean resolved);
    List<PendingMedicine> findByRejected(boolean rejected);
    List<PendingMedicine> findByResolvedFalseAndRejectedFalse();

    long countByResolvedFalseAndRejectedFalse();
    long countByResolvedTrue();   // ✅ NEW
    long countByRejectedTrue();   // ✅ NEW
}
