package com.medicinedonation.repository;

import com.medicinedonation.enums.DonationStatus;
import com.medicinedonation.model.CollectionPoint;
import com.medicinedonation.model.Donation;
import com.medicinedonation.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface DonationRepository extends JpaRepository<Donation, Long> {

    // By donor
    List<Donation> findByDonor(User donor);
    List<Donation> findByDonorAndStatus(User donor, DonationStatus status);

    // By status
    List<Donation> findByStatus(DonationStatus status);

    // By collection point
    List<Donation> findByCollectionPoint(CollectionPoint collectionPoint);
    List<Donation> findByCollectionPointAndStatus(
            CollectionPoint collectionPoint, DonationStatus status);

    // By recipient
    List<Donation> findByRecipient(User recipient);

    // Counts
    long countByStatus(DonationStatus status);
    long countByDonorAndStatus(User donor, DonationStatus status);
    long countByCollectionPointAndStatus(
            CollectionPoint collectionPoint, DonationStatus status);
}