package com.medicinedonation.dto.request;

import com.medicinedonation.enums.NmraSchedule;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MedicineRequest {

    @NotBlank(message = "Brand name is required")
    private String brandName;

    @NotBlank(message = "API name is required")
    private String apiName;

    @NotBlank(message = "Strength is required")
    private String strength;

    @NotBlank(message = "Dosage form is required")
    private String dosageForm;

    @NotBlank(message = "Route is required")
    private String route;

    @NotNull(message = "NMRA schedule is required")
    private NmraSchedule schedule;
}