package co.vivaeventos.validationservice.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.util.Base64;

@Service
@Slf4j
public class QRDecoderService {
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    public String decodeQR(String qrData) {
        if (qrData == null || qrData.isEmpty()) {
            throw new RuntimeException("QR data vacío");
        }
        
        // Si el QR viene como texto plano
        if (!qrData.startsWith("data:image") && !qrData.contains("base64")) {
            log.info("QR es texto plano: {}", qrData);
            return qrData;
        }
        
        // Si el QR viene como imagen Base64
        try {
            String base64Image = qrData;
            if (base64Image.contains(",")) {
                base64Image = base64Image.split(",")[1];
            }
            
            byte[] imageBytes = Base64.getDecoder().decode(base64Image);
            ByteArrayInputStream bis = new ByteArrayInputStream(imageBytes);
            BufferedImage image = ImageIO.read(bis);
            
            if (image == null) {
                throw new RuntimeException("No se pudo leer la imagen");
            }
            
            BinaryBitmap bitmap = new BinaryBitmap(
                new HybridBinarizer(new BufferedImageLuminanceSource(image))
            );
            
            String decodedText = new MultiFormatReader().decode(bitmap).getText();
            log.info("QR decodificado exitosamente");
            return decodedText;
            
        } catch (Exception e) {
            log.error("Error decodificando QR: {}", e.getMessage());
            throw new RuntimeException("QR inválido o no se pudo leer: " + e.getMessage());
        }
    }
    
    public String extractTicketId(String qrContent) {
        try {
            // Intentar parsear como JSON
            JsonNode json = objectMapper.readTree(qrContent);
            if (json.has("ticketId")) {
                return json.get("ticketId").asText();
            }
            if (json.has("ticket_id")) {
                return json.get("ticket_id").asText();
            }
        } catch (Exception e) {
            // No es JSON, tratar como texto plano
            log.debug("QR no es JSON, tratando como texto plano");
        }
        
        // Si es texto plano, asumir que es el ticket ID
        return qrContent.trim();
    }
}