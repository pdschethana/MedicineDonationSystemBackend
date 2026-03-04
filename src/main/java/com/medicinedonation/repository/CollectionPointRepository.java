package com.medicinedonation.repository;

import com.medicinedonation.model.CollectionPoint;
import com.medicinedonation.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface CollectionPointRepository
        extends JpaRepository<CollectionPoint, Long> {

    Optional<CollectionPoint> findByAdmin(User admin);
    List<CollectionPoint> findByActive(boolean active);
    boolean existsByAdmin(User admin);
}