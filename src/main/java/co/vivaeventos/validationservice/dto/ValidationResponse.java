package co.vivaeventos.validationservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ValidationResponse {
    private boolean valid;
    private String message;
    private String ticketId;
    private String eventName;
    private String attendeeName;
    private LocalDateTime validationTime;
    private String gateId;
    
    public static ValidationResponse success(String ticketId, String eventName, String attendeeName, String gateId) {
        return ValidationResponse.builder()
                .valid(true)
                .message("✅ BOLETA VÁLIDA - Acceso permitido")
                .ticketId(ticketId)
                .eventName(eventName)
                .attendeeName(attendeeName)
                .validationTime(LocalDateTime.now())
                .gateId(gateId)
                .build();
    }
    
    public static ValidationResponse alreadyUsed(String ticketId, String gateId) {
        return ValidationResponse.builder()
                .valid(false)
                .message("❌ BOLETA YA UTILIZADA - Acceso denegado")
                .ticketId(ticketId)
                .validationTime(LocalDateTime.now())
                .gateId(gateId)
                .build();
    }
    
    public static ValidationResponse notFound(String ticketId, String gateId) {
        return ValidationResponse.builder()
                .valid(false)
                .message("❌ BOLETA INVÁLIDA - No existe en el sistema")
                .ticketId(ticketId)
                .validationTime(LocalDateTime.now())
                .gateId(gateId)
                .build();
    }
    
    public static ValidationResponse error(String message, String gateId) {
        return ValidationResponse.builder()
                .valid(false)
                .message(message)
                .validationTime(LocalDateTime.now())
                .gateId(gateId)
                .build();
    }
}