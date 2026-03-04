package com.medicinedonation.model;

import com.medicinedonation.enums.NmraSchedule;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "medicines")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Medicine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String brandName;

    @Column(nullable = false)
    private String apiName;

    @Column(nullable = false)
    private String strength;

    @Column(nullable = false)
    private String dosageForm;

    @Column(nullable = false)
    private String route;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NmraSchedule schedule;

    @Column(nullable = false)
    private boolean pharmacistVerified = false;

    @ManyToOne
    @JoinColumn(name = "verified_by")
    private User verifiedBy;

    @Column(updatable = false)
    private LocalDateTime addedAt;

    @PrePersist
    protected void onCreate() {
        addedAt = LocalDateTime.now();
    }
}