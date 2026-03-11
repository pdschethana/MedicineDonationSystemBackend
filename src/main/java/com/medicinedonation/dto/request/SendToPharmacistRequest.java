/*package com.medicinedonation.dto.request;

import lombok.Data;

@Data
public class SendToPharmacistRequest {
    private String doctorNotes;
}*/

/*package com.medicinedonation.dto.request;

import lombok.Data;

@Data
public class SendToPharmacistRequest {

    // ✅ Frontend sends { notes: "..." }
    private String notes;

    // Keep doctorNotes as alias just in case
    private String doctorNotes;

    // Helper — returns whichever is not null
    public String getNotes() {
        if (notes != null) return notes;
        if (doctorNotes != null) return doctorNotes;
        return "Please verify this medicine";
    }
}*/

package com.medicinedonation.dto.request;

import lombok.Data;
import java.time.LocalDate;

@Data
public class SendToPharmacistRequest {

    private String notes;

    // ✅ NEW — doctor corrections passed with send-to-pharmacist
    private String correctedBrandName;
    private String correctedStrength;
    private String correctedDosageForm;
    private Integer correctedQuantity;
    private LocalDate correctedExpiryDate;
}