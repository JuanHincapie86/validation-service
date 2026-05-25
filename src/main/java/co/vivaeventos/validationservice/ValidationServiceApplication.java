package co.vivaeventos.validationservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
public class ValidationServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(ValidationServiceApplication.class, args);
        System.out.println("✅ Validation Service corriendo en puerto 8087");
        System.out.println("📱 Endpoint: POST /api/validate/scan");
        System.out.println("🔍 Endpoint: GET /api/validate/health");
    }
}