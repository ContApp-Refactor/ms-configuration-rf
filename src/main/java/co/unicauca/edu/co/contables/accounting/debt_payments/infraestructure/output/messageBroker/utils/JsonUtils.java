package co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.output.messageBroker.utils;

import co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.output.messageBroker.dto.InvoiceSyncDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JsonUtils {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Convierte un InvoiceSyncDto a JSON, manejando campos nulos apropiadamente.
     * @param invoice El objeto InvoiceSyncDto a convertir
     * @return Representación JSON del objeto, con manejo de nulos
     */
    public static String toJsonWithNullHandling(InvoiceSyncDto invoice) {
        try {
            if (invoice == null) {
                return "{\"error\": \"Invoice data is null\"}";
            }

            ObjectNode jsonNode = objectMapper.createObjectNode();
            
            if (invoice.getFactCode() != null) {
                jsonNode.put("factCode", invoice.getFactCode());
            } else {
                jsonNode.putNull("factCode");
            }
            
            if (invoice.getAccountingAccount() != null) {
                jsonNode.put("accountingAccount", invoice.getAccountingAccount());
            } else {
                jsonNode.putNull("accountingAccount");
            }
            
            if (invoice.getEntId() != null) {
                jsonNode.put("entId", invoice.getEntId());
            } else {
                jsonNode.putNull("entId");
            }
            
            if (invoice.getExpirationDate() != null) {
                jsonNode.put("expirationDate", invoice.getExpirationDate().toString());
            } else {
                jsonNode.putNull("expirationDate");
            }
            
            if (invoice.getPendingValue() != null) {
                jsonNode.put("pendingValue", invoice.getPendingValue());
            } else {
                jsonNode.putNull("pendingValue");
            }
            
            if (invoice.getThirdId() != null) {
                jsonNode.put("thirdId", invoice.getThirdId());
            } else {
                jsonNode.putNull("thirdId");
            }
            
            if (invoice.getTotalPay() != null) {
                jsonNode.put("totalPay", invoice.getTotalPay());
            } else {
                jsonNode.putNull("totalPay");
            }

            if (invoice.getTotalValue() != null) {
                jsonNode.put("totalValue", invoice.getTotalValue());
            } else {
                jsonNode.putNull("totalValue");
            }
            
            return objectMapper.writeValueAsString(jsonNode);
        } catch (Exception e) {
            log.error("Error converting InvoiceSyncDto to JSON", e);
            return "{\"error\": \"Failed to convert invoice to JSON\"}";
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
