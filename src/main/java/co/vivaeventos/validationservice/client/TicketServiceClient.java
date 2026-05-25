package co.vivaeventos.validationservice.client;

import co.vivaeventos.validationservice.dto.TicketStatusResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "ticket-service", url = "http://localhost:8086")
public interface TicketServiceClient {
    
    // Endpoint para validar ticket
    @PostMapping("/api/v1/tickets/validate")
    TicketStatusResponse validateTicket(@RequestBody ValidateTicketRequest request);
    
    // Endpoint para marcar como usado por orderId
    @PostMapping("/api/v1/tickets/use/{orderId}")
    TicketStatusResponse markAsUsed(@PathVariable("orderId") Long orderId);
    
    // Endpoint para obtener ticket por orderId
    @GetMapping("/api/v1/tickets/order/{orderId}")
    TicketStatusResponse getTicketByOrderId(@PathVariable("orderId") Long orderId);
    
    class ValidateTicketRequest {
        private String ticketNumber;
        
        public ValidateTicketRequest() {}
        
        public ValidateTicketRequest(String ticketNumber) {
            this.ticketNumber = ticketNumber;
        }
        
        public String getTicketNumber() { return ticketNumber; }
        public void setTicketNumber(String ticketNumber) { this.ticketNumber = ticketNumber; }
    }
}