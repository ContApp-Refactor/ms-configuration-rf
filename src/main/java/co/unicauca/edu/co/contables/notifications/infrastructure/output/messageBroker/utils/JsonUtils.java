package co.unicauca.edu.co.contables.notifications.infrastructure.output.messageBroker.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import co.unicauca.edu.co.contables.notifications.infrastructure.output.messageBroker.dto.InvoiceDetailEventDto;
import co.unicauca.edu.co.contables.notifications.infrastructure.output.messageBroker.dto.InvoiceDueReminderEventDto;
import co.unicauca.edu.co.contables.notifications.infrastructure.output.messageBroker.dto.ThirdUpdatedEventDto;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JsonUtils {

    private static final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    /**
     * Convierte un InvoiceDueReminderEventDto a JSON, manejando campos nulos apropiadamente.
     * Replica el comportamiento exacto de debt-payments, incluyendo campos nulos explícitamente.
     * @param dto El objeto InvoiceDueReminderEventDto a convertir.
     * @return Representación JSON del objeto, con manejo explícito de nulos.
     */
    public static String reminderDtoToJsonWithNullHandling(InvoiceDueReminderEventDto dto) {
        try {
            if (dto == null) {
                return "{\"error\": \"InvoiceDueReminderEventDto data is null\"}";
            }

            ObjectNode jsonNode = objectMapper.createObjectNode();

            if (dto.getThirdPartyId() != null) {
                jsonNode.put("thirdPartyId", dto.getThirdPartyId());
            } else {
                jsonNode.putNull("thirdPartyId");
            }

            if (dto.getInvoiceDetails() != null) {
                ArrayNode invoicesArray = objectMapper.createArrayNode();
                for (InvoiceDetailEventDto detail : dto.getInvoiceDetails()) {
                    if (detail == null) continue;
                    ObjectNode detailNode = objectMapper.createObjectNode();
                    
                    if (detail.getInvoiceId() != null) detailNode.put("invoiceId", detail.getInvoiceId()); else detailNode.putNull("invoiceId");
                    if (detail.getInvoiceCode() != null) detailNode.put("invoiceCode", detail.getInvoiceCode()); else detailNode.putNull("invoiceCode");
                    if (detail.getExpirationDate() != null) detailNode.put("expirationDate", detail.getExpirationDate().toString()); else detailNode.putNull("expirationDate");
                    if (detail.getTotalAmount() != null) detailNode.put("totalAmount", detail.getTotalAmount()); else detailNode.putNull("totalAmount");
                    if (detail.getPendingValue() != null) detailNode.put("pendingValue", detail.getPendingValue()); else detailNode.putNull("pendingValue");
                    
                    invoicesArray.add(detailNode);
                }
                jsonNode.set("invoiceDetails", invoicesArray);
            } else {
                jsonNode.putNull("invoiceDetails");
            }

            return objectMapper.writeValueAsString(jsonNode);
        } catch (Exception e) {
            log.error("Error converting InvoiceDueReminderEventDto to JSON", e);
            return "{\"error\": \"Failed to convert reminder DTO to JSON\"}";
        }
    }

    /**
     * Convierte un ThirdUpdatedEventDto a JSON, manejando campos nulos apropiadamente.
     * Replica el comportamiento exacto de debt-payments, incluyendo campos nulos explícitamente.
     * @param dto El objeto ThirdUpdatedEventDto a convertir.
     * @return Representación JSON del objeto, con manejo explícito de nulos.
     */
    public static String thirdDtoToJsonWithNullHandling(ThirdUpdatedEventDto dto) {
        try {
            if (dto == null) {
                return "{\"error\": \"ThirdUpdatedEventDto data is null\"}";
            }

            ObjectNode jsonNode = objectMapper.createObjectNode();
            
            if (dto.getThirdId() != null) jsonNode.put("thirdId", dto.getThirdId()); else jsonNode.putNull("thirdId");
            if (dto.getEntId() != null) jsonNode.put("entId", dto.getEntId()); else jsonNode.putNull("entId");
            if (dto.getFullName() != null) jsonNode.put("fullName", dto.getFullName()); else jsonNode.putNull("fullName");
            if (dto.getEmail() != null) jsonNode.put("email", dto.getEmail()); else jsonNode.putNull("email");
            if (dto.getState() != null) jsonNode.put("state", dto.getState()); else jsonNode.putNull("state");

            return objectMapper.writeValueAsString(jsonNode);
        } catch (Exception e) {
            log.error("Error converting ThirdUpdatedEventDto to JSON", e);
            return "{\"error\": \"Failed to convert third DTO to JSON\"}";
        }
    }

    /**
     * Convierte cualquier objeto a JSON. Idéntico al de debt-payments.
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