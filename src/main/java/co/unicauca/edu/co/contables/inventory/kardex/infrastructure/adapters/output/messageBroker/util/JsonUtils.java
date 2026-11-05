package co.unicauca.edu.co.contables.inventory.kardex.infrastructure.adapters.output.messageBroker.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import co.unicauca.edu.co.contables.inventory.kardex.infrastructure.adapters.output.messageBroker.dto.ProductAsyncDto;
import co.unicauca.edu.co.contables.inventory.kardex.infrastructure.adapters.output.messageBroker.dto.KardexRabbitDto;

import lombok.extern.slf4j.Slf4j;

/**
 * Utilidad para conversión de objetos a JSON.
 */
@Slf4j
public class JsonUtils {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Convierte un ProductAsyncDto a JSON, manejando campos nulos apropiadamente.
     */
    public static String toJsonWithNullHandling(ProductAsyncDto product) {
        try {
            if (product == null) {
                return "{\"error\": \"Product data is null\"}";
            }

            ObjectNode jsonNode = objectMapper.createObjectNode();
            
            if (product.getProductId() != null) {
                jsonNode.put("productId", product.getProductId());
            } else {
                jsonNode.putNull("productId");
            }
            
            if (product.getName() != null) {
                jsonNode.put("name", product.getName());
            } else {
                jsonNode.putNull("name");
            }
            
            if (product.getReference() != null) {
                jsonNode.put("reference", product.getReference());
            } else {
                jsonNode.putNull("reference");
            }
            
            if (product.getEnterpriseId() != null) {
                jsonNode.put("enterpriseId", product.getEnterpriseId());
            } else {
                jsonNode.putNull("enterpriseId");
            }
            
            if (product.getPresentation() != null) {
                jsonNode.put("presentation", product.getPresentation());
            } else {
                jsonNode.putNull("presentation");
            }
            
            jsonNode.put("state", product.isState());
            
            return objectMapper.writeValueAsString(jsonNode);
            
        } catch (JsonProcessingException e) {
            log.error("Error converting ProductAsyncDto to JSON: {}", e.getMessage());
            return "{\"error\": \"Failed to convert to JSON\", \"message\": \"" + e.getMessage() + "\"}";
        }
    }

    /**
     * Convierte un KardexRabbitDto a JSON, manejando campos nulos apropiadamente.
     */
    public static String toJsonWithNullHandling(KardexRabbitDto kardex) {
        try {
            if (kardex == null) {
                return "{\"error\": \"Kardex data is null\"}";
            }

            ObjectNode jsonNode = objectMapper.createObjectNode();
            
            if (kardex.getQuantity() != null) {
                jsonNode.put("quantity", kardex.getQuantity());
            } else {
                jsonNode.putNull("quantity");
            }
            
            if (kardex.getFactCode() != null) {
                jsonNode.put("factCode", kardex.getFactCode());
            } else {
                jsonNode.putNull("factCode");
            }
            
            if (kardex.getUnitPrice() != null) {
                jsonNode.put("unitPrice", kardex.getUnitPrice());
            } else {
                jsonNode.putNull("unitPrice");
            }
            
            if (kardex.getProductId() != null) {
                jsonNode.put("productId", kardex.getProductId());
            } else {
                jsonNode.putNull("productId");
            }
            
            if (kardex.getDetails() != null) {
                jsonNode.put("details", kardex.getDetails());
            } else {
                jsonNode.putNull("details");
            }
            
            return objectMapper.writeValueAsString(jsonNode);
            
        } catch (JsonProcessingException e) {
            log.error("Error converting KardexRabbitDto to JSON: {}", e.getMessage());
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
