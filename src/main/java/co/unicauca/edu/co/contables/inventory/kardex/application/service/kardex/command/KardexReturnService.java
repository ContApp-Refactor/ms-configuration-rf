package co.unicauca.edu.co.contables.inventory.kardex.application.service.kardex.command;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;

import co.unicauca.edu.co.contables.inventory.kardex.domain.model.Kardex;
import co.unicauca.edu.co.contables.inventory.kardex.domain.model.MovementType;
import co.unicauca.edu.co.contables.inventory.stock.domain.port.IFormatterResultOutputPort;
import co.unicauca.edu.co.contables.inventory.stock.domain.port.IMessageServicePort;
import co.unicauca.edu.co.contables.inventory.kardex.domain.port.kardex.IKardexQueryRepositoryPort;
import co.unicauca.edu.co.contables.inventory.kardex.infrastructure.adapters.config.i18n.MessageKeys;

import lombok.RequiredArgsConstructor;

/**
 * @brief Service for handling inventory return operations and validations
 * 
 * Manages return transaction logic including quantity validation
 * and original price retrieval for consistent pricing.
 */
@Service
@RequiredArgsConstructor
public class KardexReturnService {
    
    private final IKardexQueryRepositoryPort kardexQueryRepositoryPort;
    private final IFormatterResultOutputPort formatterResultOutputPort;
    private final IMessageServicePort messageService;

    /**
     * @brief Validates return and retrieves original unit price
     * @param factCode Original invoice/document code
     * @param quantity Quantity to return
     * @param productId Product identifier
     * @param originalMovementType Original movement type (PURCHASE or SALE)
     * @return Original unit price if return is valid
     */
    public BigDecimal getUnitPriceIfReturnAllowed(String factCode, int quantity, Long productId, MovementType originalMovementType) {
        // Search for the original movement by factCode, productId, and type
        List<Kardex> originalKardexList = kardexQueryRepositoryPort.findByFactCodeAndProductIdAndType(factCode, productId, originalMovementType);
        if (originalKardexList.isEmpty()) {
            formatterResultOutputPort.returnEntityDoesNotExistErrorResponse(404, 
                messageService.getMessage(MessageKeys.ERROR_NOT_FOUND, "factCode=" + factCode + ", productId=" + productId + ", type=" + originalMovementType));
        }

        // The first record must be the original operation
        Kardex originalKardex = originalKardexList.get(0);
        int initialInvoiceQuantity = originalKardex.getQuantity();
        BigDecimal unitPrice = originalKardex.getUnitPrice();

        // Now search for all previous returns of this same factCode and productId
        MovementType returnType = getReturnType(originalMovementType);
        List<Kardex> returnKardexList = kardexQueryRepositoryPort.findByFactCodeAndProductIdAndType(factCode, productId, returnType);

        // Calculate the total of previous returns plus the current quantity
        int totalReturnQuantity = returnKardexList.stream().mapToInt(Kardex::getQuantity).sum() + quantity;
        
        if (totalReturnQuantity <= initialInvoiceQuantity) {
            return unitPrice;
        }
        formatterResultOutputPort.returnBusinessRuleErrorResponse(400, 
            messageService.getMessage(MessageKeys.ERROR_QUANTITY_EXCEEDED, "Quantity"));
        return BigDecimal.ZERO;
    }

    /**
     * @brief Maps original movement type to corresponding return type
     * @param originalType Original movement type
     * @return Corresponding return movement type
     */
    public MovementType getReturnType(MovementType originalType) {
        switch (originalType) {
            case PURCHASE:
                return MovementType.PURCHASERETURN;
            case SALE:
                return MovementType.SALESRETURN;
            default:
                throw new IllegalArgumentException(
                    messageService.getMessage(MessageKeys.ERROR_INVALID_TYPE,"MovementType", originalType));
        }
    }
}
