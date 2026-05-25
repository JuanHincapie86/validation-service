package co.vivaeventos.validationservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ScanRequest {
    @NotBlank(message = "El codigo QR es obligatorio")
    private String qrData;
    private String gateId;
    private String deviceId;
}