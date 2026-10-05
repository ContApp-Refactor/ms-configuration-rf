package co.unicauca.edu.co.contables.inventory.kardex.application.service.kardex.command;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;

import co.unicauca.edu.co.contables.inventory.kardex.domain.model.Product;

import co.unicauca.edu.co.contables.inventory.kardex.application.ports.input.kardex.IKardexCommandPort;
import co.unicauca.edu.co.contables.inventory.kardex.domain.model.Kardex;
import co.unicauca.edu.co.contables.inventory.kardex.domain.model.MovementType;
import co.unicauca.edu.co.contables.inventory.kardex.domain.model.Stock;
import co.unicauca.edu.co.contables.inventory.stock.domain.port.IFormatterResultOutputPort;
import co.unicauca.edu.co.contables.inventory.stock.domain.port.IMessageServicePort;
import co.unicauca.edu.co.contables.inventory.kardex.domain.port.kardex.IKardexCommandRepositoryPort;
import co.unicauca.edu.co.contables.inventory.kardex.domain.port.kardex.IKardexQueryRepositoryPort;
import co.unicauca.edu.co.contables.inventory.kardex.domain.port.product.IProductEventPort;
import co.unicauca.edu.co.contables.inventory.kardex.infrastructure.adapters.config.i18n.MessageKeys;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @brief Service implementation for Kardex command operations
 * 
 * Processes inventory movements using weighted average cost method.
 * Handles purchases, sales, and returns with stock integration.
 */
@Service("kardexKardexCommandService")@RequiredArgsConstructor
@Transactional
@Slf4j
public class KardexCommandService implements IKardexCommandPort{
    
    private final IKardexCommandRepositoryPort kardexCommandRepositoryPort;
    private final IKardexQueryRepositoryPort kardexQueryRepositoryPort;
    private final IFormatterResultOutputPort formatterResultOutputPort;
    private final IMessageServicePort messageService;
    private final IProductEventPort productEventPort;
    
    // Servicios auxiliares
    private final KardexValidationService validationService;
    private final KardexReturnService returnService;
    private final StockIntegrationService stockIntegrationService;
    private final KardexDateValidationService kardexDateValidationService;
    private final KardexAdjustmentDateValidationService kardexAdjustmentDateValidationService;

    /**
     * @brief Registers a purchase transaction with weighted average calculation
     * @param kardex Purchase details to process
     * @return Processed kardex with updated inventory balances
     */
    @Override
    public Kardex registerPurchase(Kardex kardex) {  
        Product product = validationService.validateBusinessRulesAndGetProduct(
            kardex.getFactCode(), kardex.getProductId(), kardex.getType());
        
        Kardex lastRegisteredKardex = kardexQueryRepositoryPort.getLatestKardexByProductId(kardex.getProductId());
        if (lastRegisteredKardex == null) {
            kardex.setBalanceQuantity(kardex.getQuantity());
            kardex.setBalanceUnitPrice(kardex.getUnitPrice());
            kardex.setTotalBalance(kardex.getUnitPrice().multiply(BigDecimal.valueOf(kardex.getQuantity())));
            kardexDateValidationService.validateAndSetDate(kardex, product.getEnterpriseId());
            kardex.generateAdjustmentFactCode();
            kardex.updateDetailIfNotNull();
        } else {
            kardexDateValidationService.validateAndSetDate(kardex, product.getEnterpriseId());
            kardex.addPurchase(lastRegisteredKardex.getBalanceQuantity(), lastRegisteredKardex.getTotalBalance());
        }
        
        if(kardex.getBalanceUnitPrice().compareTo(BigDecimal.ZERO) == 0) {
            formatterResultOutputPort.returnBusinessRuleErrorResponse(400, 
                messageService.getMessage(MessageKeys.ERROR_INVALID_VALUE, "balance unit price"));
        }

        Stock stock = stockIntegrationService.createStock(kardex);
        stockIntegrationService.callApiStockService(stock, true);

        Kardex savedKardex = kardexCommandRepositoryPort.registerPurchase(kardex);

        log.info("Method registerPurchase productId=" + savedKardex.getProductId());
        return savedKardex;
    }

    /**
     * @brief Registers a sale transaction with inventory validation
     * @param kardex Sale details to process
     * @return Processed kardex with updated inventory balances
     */
    @Override
    public Kardex registerSale(Kardex kardex) {
        Product product = validationService.validateBusinessRulesAndGetProduct(
            kardex.getFactCode(), kardex.getProductId(), MovementType.SALE);
        
        Kardex lastRegisteredKardex = kardexQueryRepositoryPort.getLatestKardexByProductId(kardex.getProductId());
        validationService.validatePreviousKardexExists(lastRegisteredKardex, "sale");
        
        kardexDateValidationService.validateAndSetDate(kardex, product.getEnterpriseId());
        kardex.addSale(lastRegisteredKardex.getBalanceQuantity(), lastRegisteredKardex.getBalanceUnitPrice(), lastRegisteredKardex.getTotalBalance());

        Stock stock = stockIntegrationService.createStock(kardex);
        stockIntegrationService.callApiStockService(stock, false);

        Kardex savedKardex = kardexCommandRepositoryPort.registerSale(kardex);

        log.info("Method registerSale productId=" + savedKardex.getProductId());
        return savedKardex;
    }


    /**
     * @brief Registers a purchase return with original price validation
     * @param kardex Return details to process
     * @return Processed kardex with updated inventory balances
     */
    @Override
    public Kardex registerReturnOnPurchase(Kardex kardex) {        
        Product product = validationService.validateBusinessRulesAndGetProduct(
            kardex.getFactCode(), kardex.getProductId(), MovementType.PURCHASERETURN);
        BigDecimal unitPrice = returnService.getUnitPriceIfReturnAllowed(kardex.getFactCode(), kardex.getQuantity(), kardex.getProductId(), MovementType.PURCHASE);
        kardex.setUnitPrice(unitPrice);
        Kardex lastRegisteredKardex = kardexQueryRepositoryPort.getLatestKardexByProductId(kardex.getProductId());

        validationService.validatePreviousKardexExists(lastRegisteredKardex, "purchase return");
        kardexDateValidationService.validateAndSetDate(kardex, product.getEnterpriseId());
        kardex.returnOnPurchase(lastRegisteredKardex.getBalanceQuantity(), lastRegisteredKardex.getTotalBalance());

        Stock stock = stockIntegrationService.createStock(kardex);
        stockIntegrationService.callApiStockService(stock, false);

        Kardex savedKardex = kardexCommandRepositoryPort.registerReturnOnPurchase(kardex);
        
        log.info("Method registerReturnOnPurchase productId=" + savedKardex.getProductId());
        return savedKardex;
    }

    /**
     * @brief Registers a sale return with original price validation
     * @param kardex Return details to process
     * @return Processed kardex with updated inventory balances
     */
    @Override
    public Kardex registerReturnOnSale(Kardex kardex) {   
        Product product = validationService.validateBusinessRulesAndGetProduct(
            kardex.getFactCode(), kardex.getProductId(), MovementType.SALESRETURN);
        BigDecimal unitPrice = returnService.getUnitPriceIfReturnAllowed(kardex.getFactCode(), kardex.getQuantity(), kardex.getProductId(), MovementType.SALE);
        kardex.setUnitPrice(unitPrice);
        Kardex lastRegisteredKardex = kardexQueryRepositoryPort.getLatestKardexByProductId(kardex.getProductId());

        validationService.validatePreviousKardexExists(lastRegisteredKardex, "sale return");
        kardexDateValidationService.validateAndSetDate(kardex, product.getEnterpriseId());

        kardex.returnOnSale(lastRegisteredKardex.getBalanceQuantity(), lastRegisteredKardex.getBalanceUnitPrice(), lastRegisteredKardex.getTotalBalance());

        Stock stock = stockIntegrationService.createStock(kardex);
        stockIntegrationService.callApiStockService(stock, true);

        Kardex savedKardex = kardexCommandRepositoryPort.registerReturnOnSale(kardex);
        
        log.info(" Method registerReturnOnSale productId=" + savedKardex.getProductId());
        return savedKardex;
    }

    /**
     * @brief Deletes all kardex records
     */
    @Override
    public void deleteAll() {
        log.info("Deleting all kardex records");
        try {
            kardexCommandRepositoryPort.deleteAll();
            log.info("All kardex records deleted successfully");
        } catch (Exception e) {
            log.error("Error deleting all kardex records: {}", e.getMessage());
        }
    }

    /**
     * @brief Registers an adjustment entry transaction with custom date validation
     * Handles special date logic: assigns current date if null, validates against last record,
     * adds one second if same day, and validates it's not in the future
     * @param kardex Adjustment entry details to process
     * @return Processed kardex with updated inventory balances
     */
    @Override
    public Kardex registerAdjustmentEntry(Kardex kardex) {
        Product product = validationService.validateBusinessRulesAndGetProduct(
            kardex.getFactCode(), kardex.getProductId(), kardex.getType());
        
        Kardex lastRegisteredKardex = kardexQueryRepositoryPort.getLatestKardexByProductId(kardex.getProductId());
        
        // Validar fecha con lógica especial de ajustes
        kardexAdjustmentDateValidationService.validateAndSetDateForAdjustment(kardex, product.getEnterpriseId());
        
        if (lastRegisteredKardex == null) {
            kardex.setBalanceQuantity(kardex.getQuantity());
            kardex.setBalanceUnitPrice(kardex.getUnitPrice());
            kardex.setTotalBalance(kardex.getUnitPrice().multiply(BigDecimal.valueOf(kardex.getQuantity())));
            kardex.generateAdjustmentFactCode();
            kardex.updateDetailIfNotNull();
            productEventPort.publishUsedProductEvent(kardex.getProductId(),1);
        } else {
            kardex.addPurchase(lastRegisteredKardex.getBalanceQuantity(), lastRegisteredKardex.getTotalBalance());
        }
        
        if(kardex.getBalanceUnitPrice().compareTo(BigDecimal.ZERO) == 0) {
            formatterResultOutputPort.returnBusinessRuleErrorResponse(400, 
                messageService.getMessage(MessageKeys.ERROR_INVALID_VALUE, "balance unit price"));
        }

        Stock stock = stockIntegrationService.createStock(kardex);
        stockIntegrationService.callApiStockService(stock, true);

        Kardex savedKardex = kardexCommandRepositoryPort.registerPurchase(kardex);

        log.info("Method registerAdjustmentEntry productId=" + savedKardex.getProductId());
        return savedKardex;
    }

    /**
     * @brief Registers an adjustment exit transaction with custom date validation
     * Handles special date logic: assigns current date if null, validates against last record,
     * adds one second if same day, and validates it's not in the future
     * @param kardex Adjustment exit details to process
     * @return Processed kardex with updated inventory balances
     */
    @Override
    public Kardex registerAdjustmentExit(Kardex kardex) {
        Product product = validationService.validateBusinessRulesAndGetProduct(
            kardex.getFactCode(), kardex.getProductId(), MovementType.ADJUSTMENTEXIT);
        
        Kardex lastRegisteredKardex = kardexQueryRepositoryPort.getLatestKardexByProductId(kardex.getProductId());
        validationService.validatePreviousKardexExists(lastRegisteredKardex, "adjustment exit");
        
        // Validar fecha con lógica especial de ajustes
        kardexAdjustmentDateValidationService.validateAndSetDateForAdjustment(kardex, product.getEnterpriseId());
        
        kardex.addSale(lastRegisteredKardex.getBalanceQuantity(), lastRegisteredKardex.getBalanceUnitPrice(), lastRegisteredKardex.getTotalBalance());

        Stock stock = stockIntegrationService.createStock(kardex);
        stockIntegrationService.callApiStockService(stock, false);

        Kardex savedKardex = kardexCommandRepositoryPort.registerSale(kardex);

        log.info("Method registerAdjustmentExit productId=" + savedKardex.getProductId());
        return savedKardex;
    }

}
