package co.vivaeventos.validationservice.controller;

import co.vivaeventos.validationservice.dto.ScanRequest;
import co.vivaeventos.validationservice.dto.ValidationResponse;
import co.vivaeventos.validationservice.service.ValidationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/validate")
@RequiredArgsConstructor
@Slf4j
public class ValidationController {
    
    private final ValidationService validationService;
    
    @PostMapping("/scan")
    public ResponseEntity<ValidationResponse> scanQR(
            @Valid @RequestBody ScanRequest request,
            @RequestHeader("Authorization") String token) {
        
        log.info("📱 Solicitud de validación - QR: {}, Puerta: {}", 
            request.getQrData().substring(0, Math.min(50, request.getQrData().length())), 
            request.getGateId());
        
        ValidationResponse response = validationService.validateQR(
            request.getQrData(), 
            token,
            request.getGateId()
        );
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("✅ Validation Service is running on port 8087");
    }
    
    @GetMapping("/info")
    public ResponseEntity<String> info() {
        return ResponseEntity.ok("Validation Service - v1.0.0 - Para escanear y validar boletas QR");
    }
}