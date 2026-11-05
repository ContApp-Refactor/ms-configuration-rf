package co.unicauca.edu.co.contables.inventory.stock.infrastructure.adapters.output.messageBroker.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import co.unicauca.edu.co.contables.inventory.stock.infrastructure.adapters.output.messageBroker.dto.ProductAsyncDto;

import lombok.extern.slf4j.Slf4j;

/**
 * Utilidad para conversión de objetos a JSON.
 */
@Slf4j
public class JsonUtils {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Convierte ProductAsyncDto a JSON, manejando posibles valores nulos.
     */
    public static String toJsonWithNullHandling(ProductAsyncDto dto) {
        try {
            if (dto == null) {
                return "{\"error\": \"ProductAsyncDto is null\"}";
            }

            ObjectNode jsonNode = objectMapper.createObjectNode();
            
            if (dto.getProductId() != null) {
                jsonNode.put("productId", dto.getProductId());
            } else {
                jsonNode.putNull("productId");
            }
            
            if (dto.getName() != null) {
                jsonNode.put("name", dto.getName());
            } else {
                jsonNode.putNull("name");
            }
            
            if (dto.getEnterpriseId() != null) {
                jsonNode.put("enterpriseId", dto.getEnterpriseId());
            } else {
                jsonNode.putNull("enterpriseId");
            }
            
            return objectMapper.writeValueAsString(jsonNode);
            
        } catch (JsonProcessingException e) {
            log.error("Error converting ProductAsyncDto to JSON: {}", e.getMessage());
            return "{\"error\": \"Failed to convert to JSON\", \"message\": \"" + e.getMessage() + "\"}";
        }
    }

    /**
     * Convierte cualquier objeto a JSON.
     */
    public static String toJsonSafely(Object object) {
        try {
            if (object == null) {
                return "{\"error\": \"Object is null\"}";
            }
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            log.error("Error converting object to JSON: {}", e.getMessage());
            return "{\"error\": \"Failed to convert to JSON\", \"message\": \"" + e.getMessage() + "\"}";
        }
    }
}
