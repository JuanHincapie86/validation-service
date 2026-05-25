package co.vivaeventos.validationservice.service;

import co.vivaeventos.validationservice.client.TicketServiceClient;
import co.vivaeventos.validationservice.dto.TicketStatusResponse;
import co.vivaeventos.validationservice.dto.ValidationResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class ValidationService {
    
    private final QRDecoderService qrDecoderService;
    private final TicketServiceClient ticketServiceClient;
    
    // Cache temporal para almacenar la relación ticketNumber -> orderId
    private final Map<String, Long> ticketToOrderCache = new ConcurrentHashMap<>();
    
    public ValidationResponse validateQR(String qrData, String token, String gateId) {
        log.info("Iniciando validación de QR para puerta: {}", gateId);
        
        // 1. Decodificar QR
        String ticketNumber;
        try {
            String decodedContent = qrDecoderService.decodeQR(qrData);
            ticketNumber = qrDecoderService.extractTicketId(decodedContent);
            log.info("Ticket Number extraído: {}", ticketNumber);
        } catch (Exception e) {
            log.error("Error decodificando QR: {}", e.getMessage());
            return ValidationResponse.error("QR inválido - No se pudo leer", gateId);
        }
        
        // 2. Validar ticket con Ticket Service
        TicketStatusResponse ticketStatus;
        try {
            TicketServiceClient.ValidateTicketRequest request = 
                new TicketServiceClient.ValidateTicketRequest(ticketNumber);
            ticketStatus = ticketServiceClient.validateTicket(request);
            log.info("Respuesta de Ticket Service - válido: {}, usado: {}", 
                ticketStatus.isValid(), ticketStatus.isUsed());
        } catch (Exception e) {
            log.error("Error consultando ticket: {}", e.getMessage());
            return ValidationResponse.error("Error de comunicación con el servidor", gateId);
        }
        
        // 3. Verificar si ya está usado (por el campo 'used' o por cache)
        if (ticketStatus.isUsed() || isTicketAlreadyUsed(ticketNumber)) {
            log.warn("Ticket ya usado: {}", ticketNumber);
            return ValidationResponse.alreadyUsed(ticketNumber, gateId);
        }
        
        // 4. Obtener o generar un orderId
        Long orderId = getOrCreateOrderId(ticketNumber, ticketStatus);
        if (orderId == null) {
            log.error("No se pudo obtener orderId para ticket: {}", ticketNumber);
            return ValidationResponse.error("No se pudo identificar el ticket", gateId);
        }
        
        // 5. Marcar como usado
        try {
            ticketServiceClient.markAsUsed(orderId);
            markTicketAsUsed(ticketNumber);
            log.info("Ticket {} (orderId: {}) marcado como usado exitosamente", ticketNumber, orderId);
            
            return ValidationResponse.success(
                ticketNumber,
                ticketStatus.getEventName(),
                ticketStatus.getAttendeeName(),
                gateId
            );
        } catch (Exception e) {
            log.error("Error marcando ticket como usado: {}", e.getMessage());
            return ValidationResponse.error("Error al procesar la validación", gateId);
        }
    }
    
    private Long getOrCreateOrderId(String ticketNumber, TicketStatusResponse ticketStatus) {
        // Primero verificar en cache
        if (ticketToOrderCache.containsKey(ticketNumber)) {
            return ticketToOrderCache.get(ticketNumber);
        }
        
        // Intentar obtener orderId del response de validación
        if (ticketStatus.getOrderId() != null) {
            Long orderId = ticketStatus.getOrderId();
            ticketToOrderCache.put(ticketNumber, orderId);
            return orderId;
        }
        
        // Intentar obtener por el endpoint /order/{orderId} (necesitamos saber el orderId)
        // Como no lo sabemos, generamos uno basado en el ticketNumber
        Long generatedOrderId = generateOrderIdFromTicketNumber(ticketNumber);
        if (generatedOrderId != null) {
            ticketToOrderCache.put(ticketNumber, generatedOrderId);
            return generatedOrderId;
        }
        
        return null;
    }
    
    private Long generateOrderIdFromTicketNumber(String ticketNumber) {
        try {
            // Si ticketNumber tiene formato TKT-12345, extraemos el número
            if (ticketNumber.contains("-")) {
                String numberPart = ticketNumber.split("-")[1];
                return Long.parseLong(numberPart);
            }
            // Si es solo número
            return Long.parseLong(ticketNumber);
        } catch (Exception e) {
            log.warn("No se pudo generar orderId desde ticketNumber: {}", ticketNumber);
            return null;
        }
    }
    
    private boolean isTicketAlreadyUsed(String ticketNumber) {
        return ticketToOrderCache.containsKey(ticketNumber) && 
               ticketToOrderCache.get(ticketNumber) == -1L;
    }
    
    private void markTicketAsUsed(String ticketNumber) {
        // Marcar como usado en cache (valor -1 indica usado)
        ticketToOrderCache.put(ticketNumber, -1L);
    }
}