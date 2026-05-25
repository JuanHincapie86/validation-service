package co.vivaeventos.validationservice.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class TicketStatusResponse {
    private boolean valid;
    private String ticketNumber;
    private Integer quantity;
    private String eventName;
    private String location;
    private String ticketType;
    private String eventDate;
    private boolean used;
    
    // Campos adicionales que pueden venir
    private Long orderId;
    private Long id;
    private String userEmail;
    
    public String getTicketId() {
        return ticketNumber;
    }
    
    public String getStatus() {
        return used ? "USED" : "VALID";
    }
    
    public String getAttendeeName() {
        return userEmail != null ? userEmail : "";
    }
    
    public String getMessage() {
        if (used) return "Ticket already used";
        if (!valid) return "Invalid ticket";
        return "Ticket is valid";
    }
    
    public boolean isValid() {
        return valid && !used;
    }
}