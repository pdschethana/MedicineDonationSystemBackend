package com.medicinedonation.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CollectionPointResponse {

    private Long id;
    private String locationName;
    private String address;
    private String district;
    private String phone;
    private boolean active;
    private String adminName;
    private String adminEmail;
}