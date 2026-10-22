package co.unicauca.edu.co.contables.accounting.catalogue.accounting.infraestructure.output.messageBroker.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import co.unicauca.edu.co.contables.accounting.catalogue.accounting.infraestructure.output.messageBroker.DTO.InvoiceSyncDto;
import co.unicauca.edu.co.contables.accounting.catalogue.accounting.infraestructure.output.messageBroker.DTO.PortfolioWriteOffResponse;
import co.unicauca.edu.co.contables.accounting.catalogue.accounting.infraestructure.output.messageBroker.DTO.ReceiptDetailEventDTO;
import co.unicauca.edu.co.contables.accounting.catalogue.accounting.infraestructure.output.messageBroker.DTO.ReceiptEventDTO;
import co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.input.rest.dto.response.WriteOffDetailResponse;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JsonUtils {

    /**
     * Convierte un InvoiceSyncDto a JSON, manejando campos nulos apropiadamente.
     * 
     * @param invoice El objeto InvoiceSyncDto a convertir
     * @return Representación JSON del objeto, con manejo de nulos
     */
    private static final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    public static String invoiceDtoToJsonWithNullHandling(InvoiceSyncDto dto) {
        // ... (Este método no cambia, se queda como en la respuesta anterior)
        try {
            if (dto == null)
                return "{\"error\": \"InvoiceSyncDto is null\"}";
            ObjectNode jsonNode = objectMapper.createObjectNode();
            if (dto.getFactCode() != null)
                jsonNode.put("factCode", dto.getFactCode());
            else
                jsonNode.putNull("factCode");
            if (dto.getEntId() != null)
                jsonNode.put("entId", dto.getEntId());
            else
                jsonNode.putNull("entId");
            if (dto.getThirdId() != null)
                jsonNode.put("thirdId", dto.getThirdId());
            else
                jsonNode.putNull("thirdId");
            if (dto.getTotalValue() != null)
                jsonNode.put("totalValue", dto.getTotalValue());
            else
                jsonNode.putNull("totalValue");
            if (dto.getTotalPay() != null)
                jsonNode.put("totalPay", dto.getTotalPay());
            else
                jsonNode.putNull("totalPay");
            if (dto.getPendingValue() != null)
                jsonNode.put("pendingValue", dto.getPendingValue());
            else
                jsonNode.putNull("pendingValue");
            if (dto.getExpirationDate() != null)
                jsonNode.put("expirationDate", dto.getExpirationDate().toString());
            else
                jsonNode.putNull("expirationDate");
            if (dto.getCreationDate() != null)
                jsonNode.put("creationDate", dto.getCreationDate().toString());
            else
                jsonNode.putNull("creationDate");
            jsonNode.put("active", dto.isActive());
            if (dto.getAccountingAccount() != null)
                jsonNode.put("accountingAccount", dto.getAccountingAccount());
            else
                jsonNode.putNull("accountingAccount");
            return objectMapper.writeValueAsString(jsonNode);
        } catch (Exception e) {
            log.error("Error converting InvoiceSyncDto to JSON", e);
            return "{\"error\": \"Failed to convert InvoiceSyncDto to JSON\"}";
        }
    }

    public static String writeOffDtoToJsonWithNullHandling(PortfolioWriteOffResponse dto) {
        try {
            if (dto == null)
                return "{\"error\": \"PortfolioWriteOffResponse is null\"}";
            ObjectNode jsonNode = objectMapper.createObjectNode();
            // ... (campos del objeto principal)
            if (dto.getId() != null)
                jsonNode.put("id", dto.getId());
            else
                jsonNode.putNull("id");
            if (dto.getCode() != null)
                jsonNode.put("code", dto.getCode());
            else
                jsonNode.putNull("code");
            // ... (resto de campos principales)

            // --- INICIO DE LA ACTUALIZACIÓN ---
            if (dto.getDetails() != null) {
                ArrayNode detailsArray = objectMapper.createArrayNode();
                for (WriteOffDetailResponse detail : dto.getDetails()) {
                    if (detail == null)
                        continue;
                    ObjectNode detailNode = objectMapper.createObjectNode();
                    if (detail.getAmountWrittenOff() != null)
                        detailNode.put("amountWrittenOff", detail.getAmountWrittenOff());
                    else
                        detailNode.putNull("amountWrittenOff");
                    // Para el objeto anidado 'invoice', usamos el método seguro genérico.
                    if (detail.getInvoice() != null)
                        detailNode.set("invoice", objectMapper.readTree(toJsonSafely(detail.getInvoice())));
                    else
                        detailNode.putNull("invoice");
                    detailsArray.add(detailNode);
                }
                jsonNode.set("details", detailsArray);
            } else {
                jsonNode.putNull("details");
            }
            // --- FIN DE LA ACTUALIZACIÓN ---

            return objectMapper.writeValueAsString(jsonNode);
        } catch (Exception e) {
            log.error("Error converting PortfolioWriteOffResponse to JSON", e);
            return "{\"error\": \"Failed to convert PortfolioWriteOffResponse to JSON\"}";
        }
    }

    public static String receiptDtoToJsonWithNullHandling(ReceiptEventDTO dto) {
        try {
            if (dto == null)
                return "{\"error\": \"ReceiptEventDTO is null\"}";
            ObjectNode jsonNode = objectMapper.createObjectNode();
            // ... (campos del objeto principal)
            if (dto.getId() != null)
                jsonNode.put("id", dto.getId());
            else
                jsonNode.putNull("id");
            if (dto.getReceiptCode() != null)
                jsonNode.put("receiptCode", dto.getReceiptCode());
            else
                jsonNode.putNull("receiptCode");
            // ... (resto de campos principales)

            // --- INICIO DE LA ACTUALIZACIÓN ---
            if (dto.getDetails() != null) {
                ArrayNode detailsArray = objectMapper.createArrayNode();
                for (ReceiptDetailEventDTO detail : dto.getDetails()) {
                    if (detail == null)
                        continue;
                    ObjectNode detailNode = objectMapper.createObjectNode();
                    if (detail.getInvoiceId() != null)
                        detailNode.put("invoiceId", detail.getInvoiceId());
                    else
                        detailNode.putNull("invoiceId");
                    if (detail.getAmountPaid() != null)
                        detailNode.put("amountPaid", detail.getAmountPaid());
                    else
                        detailNode.putNull("amountPaid");
                    if (detail.getInvoiceCode() != null)
                        detailNode.put("invoiceCode", detail.getInvoiceCode());
                    else
                        detailNode.putNull("invoiceCode");
                    if (detail.getAccountingAccount() != null)
                        detailNode.put("accountingAccount", detail.getAccountingAccount());
                    else
                        detailNode.putNull("accountingAccount");
                    detailsArray.add(detailNode);
                }
                jsonNode.set("details", detailsArray);
            } else {
                jsonNode.putNull("details");
            }
            // --- FIN DE LA ACTUALIZACIÓN ---

            return objectMapper.writeValueAsString(jsonNode);
        } catch (Exception e) {
            log.error("Error converting ReceiptEventDTO to JSON", e);
            return "{\"error\": \"Failed to convert ReceiptEventDTO to JSON\"}";
        }
    }

    public static String toJsonSafely(Object object) {
        // ... (Este método no cambia)
        try {
            if (object == null)
                return "{\"error\": \"Object is null\"}";
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            log.error("Error converting object to JSON: {}", e.getMessage());
            return "{\"error\": \"Failed to convert to JSON\", \"message\": \"" + e.getMessage() + "\"}";
        }
    }
}
