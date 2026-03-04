package com.medicinedonation.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CollectionPointRequest {

    @NotBlank(message = "Location name is required")
    private String locationName;

    @NotBlank(message = "Address is required")
    private String address;

    private String district;
    private String phone;
}