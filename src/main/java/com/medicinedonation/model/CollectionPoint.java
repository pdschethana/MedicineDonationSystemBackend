package com.medicinedonation.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "collection_points")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CollectionPoint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String locationName;

    @Column(nullable = false)
    private String address;

    private String district;
    private String phone;

    @Column(nullable = false)
    private boolean active = true;

    @OneToOne
    @JoinColumn(name = "admin_user_id")
    private User admin;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}