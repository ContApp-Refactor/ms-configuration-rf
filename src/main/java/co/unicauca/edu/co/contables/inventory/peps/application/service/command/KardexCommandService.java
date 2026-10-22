package co.unicauca.edu.co.contables.inventory.peps.application.service.command;


import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Qualifier;
import jakarta.transaction.Transactional;
import co.unicauca.edu.co.contables.inventory.peps.application.ports.input.IKardexCommandPort;
import co.unicauca.edu.co.contables.inventory.peps.domain.model.DetailOutput;
import co.unicauca.edu.co.contables.inventory.peps.domain.model.Kardex;
import co.unicauca.edu.co.contables.inventory.peps.domain.model.Product;
import co.unicauca.edu.co.contables.inventory.peps.domain.model.Stock;
import co.unicauca.edu.co.contables.inventory.stock.domain.port.IFormatterResultOutputPort;
import co.unicauca.edu.co.contables.inventory.stock.domain.port.IMessageServicePort;
import co.unicauca.edu.co.contables.inventory.peps.domain.port.output.command.IKardexCommandOutputPort;
import co.unicauca.edu.co.contables.inventory.peps.domain.port.output.external.IProductEventPort;
import co.unicauca.edu.co.contables.inventory.peps.domain.port.output.query.IDetailQueryOutPutPort;
import co.unicauca.edu.co.contables.inventory.peps.domain.port.output.query.IKardexQueryOutputPort;
import co.unicauca.edu.co.contables.inventory.peps.domain.port.output.query.IProductQueryOutputPort;
import co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.config.i18n.MessageKeys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @brief Service implementation for Kardex command operations
 * 
 * Orchestrates the business logic for inventory movements including purchases,
 * sales, returns, and adjustments. Implements FIFO (First-In, First-Out)
 * valuation method and manages stock synchronization.
 */
@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class KardexCommandService implements IKardexCommandPort {
    private final IKardexCommandOutputPort kardexCommandOutputPort;
    private final IProductQueryOutputPort productQueryOutputPort;
    private final IKardexQueryOutputPort kardexQueryOutputPort;
    private final IDetailQueryOutPutPort detailQueryOutPutPort;
    private final IFormatterResultOutputPort formatterResultOutputPort;
    private final KardexAdjustmentValidationService kardexAdjustmentValidationService;
    @Qualifier("pepsProductEventPublisher")
    private final IProductEventPort productEventPort;

    private final StockIntegrationService stockIntegrationService;
    private final IMessageServicePort messageService;
    private final KardexDateValidationService kardexDateValidationService;

    /**
     * @brief Registers a new purchase movement
     * 
     * Validates the product, creates a purchase record, updates stock,
     * and persists the movement.
     * 
     * @param kardex Kardex object containing purchase details
     * @return Registered purchase record
     */
    @Override
    public Kardex registerPurchase(Kardex kardex) {
       
        Product product = getValidatedProduct(kardex.getProduct().getProductId());
        
        
        Kardex purchase = Kardex.createPurchase(
            kardex.getFactCode(),
            kardex.getDetails(),
            kardex.getQuantity(),
            kardex.getUnitPrice(),
            product
        );
        kardexDateValidationService.validateAndSetDate(purchase, product.getEnterpriseId());

        Stock stock= stockIntegrationService.createStock(purchase);
        stockIntegrationService.callApiStockService(stock, true);
       
        
        if(!kardexQueryOutputPort.existsByProduct_ProductId(product.getProductId())){
            //Publicar evento de uso de producto
            productEventPort.publishUsedProductEvent(kardex.getProduct().getProductId(), 1);
        }
        Kardex savedKardex= kardexCommandOutputPort.registerPurchase(purchase);
        log.info("Method registerPurchase productId"+kardex.getProduct().getProductId()+" registered successfully.");
        return savedKardex;
    }
    
    /**
     * @brief Registers a new sale movement
     * 
     * Validates stock availability, applies FIFO logic to determine cost,
     * updates affected purchase lots, and persists the sale.
     * 
     * @param kardex Kardex object containing sale details
     * @return Registered sale record
     */
    @Override
    public Kardex registerSale(Kardex kardex) {

        // Obtener y validar producto
        Product product = getValidatedProduct(kardex.getProduct().getProductId());
        
        // Obtener lotes disponibles
        List<Kardex> availablePurchases = kardexQueryOutputPort
            .findAvailablePurchasesOrderedByDate(product.getProductId());
        
        //Validar stock total disponible usando el dominio
        validateSufficientStock(availablePurchases, kardex.getQuantity());
        
        //Crear movimiento de venta usando 
        Kardex saleMovement = Kardex.createSale(
            kardex.getFactCode(),
            kardex.getDetails(),
            kardex.getQuantity(),
            kardex.getUnitPrice(),
            product
        );

        kardexDateValidationService.validateAndSetDate(saleMovement, product.getEnterpriseId());
        Stock stock= stockIntegrationService.createStock(saleMovement);
        stockIntegrationService.callApiStockService(stock, false);
        
        //Procesar lógica FIFO usando métodos de dominio
        FIFOResult fifoResult = processFIFOLogic(availablePurchases, saleMovement);
        
        // Agregar detalles al movimiento de venta usando método de dominio
        fifoResult.detailsToCreate.forEach(saleMovement::addOutputDetail);

        Kardex savedKardex= kardexCommandOutputPort.registerSale(saleMovement, fifoResult.lotsToUpdate);
        log.info("Method registerSale productId=" + kardex.getProduct().getProductId() + " registered successfully.");
        return savedKardex;

    }



    /**
     * @brief Registers a purchase return
     * 
     * Validates the original purchase, updates available quantity of the lot,
     * and records the return movement.
     * 
     * @param kardex Kardex object containing return details
     * @return Registered return record
     */
    @Override
    public Kardex registerPurchaseReturn(Kardex kardex) {
        // Obtener compra original
        Kardex originalPurchase = kardexQueryOutputPort
            .findByRefFacture(kardex.getFactCode(), kardex.getProduct().getProductId())
            .orElseThrow(() -> {
                log.info("Original purchase not found for return");
                formatterResultOutputPort.returnEntityDoesNotExistErrorResponse(404, "Original purchase not found");
                return new IllegalArgumentException("Original purchase not found");
            });
        
        // Validar usando método de dominio
        try {
            originalPurchase.validateForPurchaseReturn(kardex.getFactCode(), kardex.getQuantity());
        } catch (IllegalArgumentException e) {
            log.info("Purchase return validation failed: {}", e.getMessage());
            formatterResultOutputPort.returnBusinessRuleErrorResponse(400, e.getMessage());
            throw e;
        }
        
        //Actualizar disponibilidad del lote 
        kardexCommandOutputPort.updateAvaliableAmount(originalPurchase.getIdKardex(), originalPurchase.getAvailableQuantity()-kardex.getQuantity());
        
        //  Obtener producto
        Product product = getValidatedProduct(kardex.getProduct().getProductId());
        
        // Crear devolución usando Factory Method
        Kardex returnMovement = Kardex.createPurchaseReturn(
            originalPurchase.getFactCode(),
            kardex.getDetails(),
            kardex.getQuantity(),
            originalPurchase.getUnitPrice(),
            product
        );
        kardexDateValidationService.validateAndSetDate(returnMovement, product.getEnterpriseId());
        Stock stock= stockIntegrationService.createStock(returnMovement);
        stockIntegrationService.callApiStockService(stock, false);
        Kardex savedKardex= kardexCommandOutputPort.registerPurchaseReturn(returnMovement);
        log.info("Method registerPurchaseReturn productId=" + kardex.getProduct().getProductId() + " registered successfully.");
        return savedKardex;
    }

  



    /**
     * @brief Registers a sale return
     * 
     * Validates the original sale, restores stock using LIFO (Last-In, First-Out) logic
     * to reverse the FIFO sale process, and updates inventory records.
     * 
     * @param kardex Kardex object containing return details
     * @return List of created return movements (one per affected lot)
     */
    @Override
    public List<Kardex> registerSaleReturn(Kardex kardex) {
        // 1. Obtener venta original
        Kardex originalSale = kardexQueryOutputPort
            .findByRefFacture(kardex.getFactCode(), kardex.getProduct().getProductId())
            .orElseThrow(() -> {
                log.info("Sale to be returned not found");
                formatterResultOutputPort.returnBusinessRuleErrorResponse(400, "Sale to be returned not found");
                return new IllegalArgumentException("Sale to be returned not found");
            });
        
        // 2.Validar usando método de dominio actualizado
        try {
            originalSale.validateForSaleReturn(kardex.getFactCode(), kardex.getQuantity());
        } catch (IllegalArgumentException e) {
            log.info("Sale return validation failed: {}", e.getMessage());
            formatterResultOutputPort.returnBusinessRuleErrorResponse(400, e.getMessage());
            throw e;
        }
        
        // 3. Obtener detalles de la venta (ordenados por ID para obtener LIFO)
        List<DetailOutput> detailsOfSale = detailQueryOutPutPort.findByMovementSaleOrderedDesc(originalSale.getIdKardex());
        
        if (detailsOfSale.isEmpty()) {
            log.info("Sale has no details to return");
            formatterResultOutputPort.returnBusinessRuleErrorResponse(400, "Sale has no details to return");
            return new ArrayList<>();
        }
        
        // 4.Procesar devolución LIFO (desde el último lote vendido)
        return processSaleReturnLIFO(kardex, originalSale, detailsOfSale);
    }

    /**
     * @brief Processes sale return using LIFO logic
     * 
     * Iterates through sale details in reverse order (LIFO) to restore
     * quantities to the original purchase lots.
     * 
     * @param kardex Return request details
     * @param originalSale Original sale record
     * @param detailsOfSale List of sale details ordered by ID descending
     * @return List of created return movements
     */
    private List<Kardex> processSaleReturnLIFO(Kardex kardex, Kardex originalSale, 
                                            List<DetailOutput> detailsOfSale) {
        List<Kardex> createdReturnMovements = new ArrayList<>();
        Product product = getValidatedProduct(kardex.getProduct().getProductId());
        
        int remainingToReturn = kardex.getQuantity();
        
        //  Recorrer detalles desde el último vendido (LIFO)
        for (DetailOutput detail : detailsOfSale) {
            if (remainingToReturn <= 0) {
                break; // Ya devolvimos todo
            }
            
            // Cantidad a devolver de este lote específico
            int amountFromThisLot = Math.min(remainingToReturn, detail.getQuantityUsed());
            
            // Restaurar cantidad en lote original
            Kardex originalPurchaseLot = detail.getMovementOrigin();
            originalPurchaseLot.restoreAvailableQuantity(amountFromThisLot);
            kardexCommandOutputPort.updateAvaliableAmount(
                originalPurchaseLot.getIdKardex(), 
                originalPurchaseLot.getAvailableQuantity()
            );
            
            //Crear movimiento de devolución para este lote
            Kardex returnMovement = Kardex.createSaleReturn(
                originalSale.getFactCode(),
                kardex.getDetails(),
                amountFromThisLot, 
                detail.getUnitPrice(),
                product
            );

            kardexDateValidationService.validateAndSetDate(returnMovement, product.getEnterpriseId());
            Stock stock= stockIntegrationService.createStock(returnMovement);
            stockIntegrationService.callApiStockService(stock, true);
            
            Kardex createdReturn = kardexCommandOutputPort.registerSaleReturn(returnMovement);
            createdReturnMovements.add(createdReturn);
            
            //Actualizar o eliminar el detalle original
            if (amountFromThisLot == detail.getQuantityUsed()) {
                // Se devolvió todo el lote
                detailQueryOutPutPort.deleteById(detail.getIdDetailOutput());
            } else {
                // Devolución parcial - actualiza solo cantidad, unitPrice se mantiene igual
                int newAmount = detail.getQuantityUsed() - amountFromThisLot;
                detailQueryOutPutPort.updateQuantityAndPrice(detail.getIdDetailOutput(), newAmount, detail.getUnitPrice());
            }
            
            remainingToReturn -= amountFromThisLot;
        }
        
        return createdReturnMovements;
    }



    /**
     * @brief Registers a non-commercial exit (e.g., internal consumption, loss)
     * 
     * Processes the exit similar to a sale but for non-commercial reasons.
     * Applies FIFO logic to reduce inventory.
     * 
     * @param kardex Kardex object containing exit details
     * @return Registered exit record
     */
    @Override
    public Kardex registerNonCommercialExit(Kardex kardex) {
         //Obtener y validar producto
        Product product = getValidatedProduct(kardex.getProduct().getProductId());
        
        //Obtener lotes disponibles
        List<Kardex> availablePurchases = kardexQueryOutputPort
            .findAvailablePurchasesOrderedByDate(product.getProductId());
        
        //Validar stock disponible
        validateSufficientStock(availablePurchases, kardex.getQuantity());
        
        //Crear salida no comercial usando Factory Method
        Kardex nonCommercialExit = Kardex.createNonCommercialExit(
            kardex.getFactCode(),
            kardex.getDetails(),
            kardex.getQuantity(),
            kardex.getUnitPrice(),
            product
        );
        kardexDateValidationService.validateAndSetDate(nonCommercialExit, product.getEnterpriseId());
        Stock stock= stockIntegrationService.createStock(nonCommercialExit);
        stockIntegrationService.callApiStockService(stock, false);

        // Procesar lógica FIFO
        FIFOResult fifoResult = processFIFOLogic(availablePurchases, nonCommercialExit);
        
        //Agregar detalles usando método de dominio
        fifoResult.detailsToCreate.forEach(nonCommercialExit::addOutputDetail);
        
        Kardex savedKardex= kardexCommandOutputPort.registerNonCommercialExit(nonCommercialExit, fifoResult.lotsToUpdate);
        log.info("Method registerNonCommercialExit productId=" + kardex.getProduct().getProductId() + " registered successfully.");
        return savedKardex;

    }

    /**
     * @brief Registers a non-commercial entry (e.g., gift, bonus)
     * 
     * Records an increase in inventory that is not a purchase.
     * 
     * @param kardex Kardex object containing entry details
     * @return Registered entry record
     */
    @Override
    public Kardex registerNonCommercialEntry(Kardex kardex) {
       
         //Obtener y validar producto
        Product product = getValidatedProduct(kardex.getProduct().getProductId());
        
        //Crear entrada no comercial usando Factory Method (incluye validaciones)
        Kardex nonCommercialEntry = Kardex.createNonCommercialEntry(
            kardex.getFactCode(),
            kardex.getDetails(),
            kardex.getQuantity(),
            kardex.getUnitPrice(),
            product
        );

        Stock stock= stockIntegrationService.createStock(nonCommercialEntry);
        stockIntegrationService.callApiStockService(stock, true);
        
      
        if(!kardexQueryOutputPort.existsByProduct_ProductId(product.getProductId()) ){
            //Publicar evento de uso de producto
             productEventPort.publishUsedProductEvent(kardex.getProduct().getProductId(), 1);
           
        }  
        Kardex savedKardex= kardexCommandOutputPort.registerNonCommercialEntry(nonCommercialEntry);
        log.info("Method registerNonCommercialEntry productId=" + kardex.getProduct().getProductId() + " registered successfully.");
        return savedKardex;
    }

    /**
     * @brief Registers an inventory adjustment entry
     * 
     * Records a positive adjustment to inventory, treating it as a new purchase lot.
     * Validates adjustment date against business rules.
     * 
     * @param kardex Kardex object containing adjustment details
     * @return Registered adjustment record
     */
    @Override
    public Kardex registerAdjustmentEntry(Kardex kardex) {
        Product product = getValidatedProduct(kardex.getProduct().getProductId());
        
        kardexAdjustmentValidationService.validateDateForAdjustment(kardex,product.getEnterpriseId());

       
        Kardex purchaseAdjustment = Kardex.createPurchaseAdjustment(
            kardex.generateAdjustmentFactCode(),
            kardex.getDetails(),
            kardex.getQuantity(),
            kardex.getUnitPrice(),
            product,
            kardex.getDate()
        );
        kardexDateValidationService.validateAndSetDate(purchaseAdjustment, product.getEnterpriseId());
        Stock stock= stockIntegrationService.createStock(purchaseAdjustment);
        stockIntegrationService.callApiStockService(stock, true);
       
        if(!kardexQueryOutputPort.existsByProduct_ProductId(kardex.getProduct().getProductId())){
            //Publicar evento de uso de producto
             productEventPort.publishUsedProductEvent(kardex.getProduct().getProductId(), 1);
        
        }
        Kardex savedKardex= kardexCommandOutputPort.registerPurchase(purchaseAdjustment);  
        log.info("Method registerPurchase productId=" + kardex.getProduct().getProductId() + " registered successfully.");
        return savedKardex;
    }

    /**
     * @brief Registers an inventory adjustment exit
     * 
     * Records a negative adjustment to inventory, treating it as a sale/exit.
     * Applies FIFO logic and validates adjustment date.
     * 
     * @param kardex Kardex object containing adjustment details
     * @return Registered adjustment record
     */
    @Override
    public Kardex registerAdjustmentExit(Kardex kardex) {
        // Obtener y validar producto
        Product product = getValidatedProduct(kardex.getProduct().getProductId());
        
        // Obtener lotes disponibles
        List<Kardex> availablePurchases = kardexQueryOutputPort
            .findAvailablePurchasesOrderedByDate(product.getProductId());
        
        //Validar stock total disponible usando el dominio
        validateSufficientStock(availablePurchases, kardex.getQuantity());
        
         kardexAdjustmentValidationService.validateDateForAdjustment(kardex,product.getEnterpriseId());
    

        //Crear movimiento de venta usando 
        Kardex saleMovement = Kardex.createSaleAdjustment(
            kardex.generateAdjustmentFactCode(),
            kardex.getDetails(),
            kardex.getQuantity(),
            kardex.getUnitPrice(),
            product,
            kardex.getDate()
        );

        Stock stock= stockIntegrationService.createStock(saleMovement);
        stockIntegrationService.callApiStockService(stock, false);
        
        //Procesar lógica FIFO usando métodos de dominio
        FIFOResult fifoResult = processFIFOLogic(availablePurchases, saleMovement);
        
        // Agregar detalles al movimiento de venta usando método de dominio
        fifoResult.detailsToCreate.forEach(saleMovement::addOutputDetail);
       

        Kardex savedKardex = kardexCommandOutputPort.registerSale(saleMovement, fifoResult.lotsToUpdate);
        log.info("Method registerSale productId=" + kardex.getProduct().getProductId() + " registered successfully.");
        return savedKardex;


    }

  

    /**
     * @brief Deletes all kardex records
     * 
     * Removes all inventory movement records from the system.
     * Use with caution.
     */
    @Override
    public void deleteAll() {
       log.info("Deleting all kardex records");
        try {
            kardexCommandOutputPort.deleteAll();
            log.info("All kardex records deleted successfully");
        } catch (Exception e) {
            log.error("Error deleting all kardex records: {}", e.getMessage());
        }

    }


    /**
     * @brief Retrieves and validates a product
     * 
     * Checks if the product exists and is active.
     * 
     * @param productId Product identifier
     * @return Validated Product object
     * @throws IllegalArgumentException if product not found or inactive
     */
    private Product getValidatedProduct(Long productId) {
        Product product = productQueryOutputPort.getProductByProductId(productId)
            .orElseThrow(() -> {
                log.info("Product not found: {}", productId);
                String message = messageService.getMessage(MessageKeys.ERROR_NOT_FOUND_PRODUCT, productId);
                formatterResultOutputPort.returnEntityDoesNotExistErrorResponse(404, message);
                return new IllegalArgumentException(message);
            });
        
        // Validar que el producto esté activo
        if (!product.isActive()) {
            String message = messageService.getMessage(MessageKeys.ERROR_PRODUCT_NOT_ACTIVE, productId);
            log.info(message);
            formatterResultOutputPort.returnBusinessRuleErrorResponse(400, message);
            throw new IllegalArgumentException(message);
        }
        
        return product;
    }

    /**
     * @brief Validates sufficient stock availability
     * 
     * Calculates total available quantity from purchase lots and compares
     * with requested quantity.
     * 
     * @param availablePurchases List of available purchase lots
     * @param quantityRequested Quantity required
     * @throws IllegalArgumentException if stock is insufficient
     */
    private void validateSufficientStock(List<Kardex> availablePurchases, int quantityRequested) {
        int totalAvailable = availablePurchases.stream()
            .mapToInt(Kardex::getAvailableQuantity)
            .sum();
            
        if (totalAvailable < quantityRequested) {
            String message = messageService.getMessage(MessageKeys.ERROR_INSUFFICIENT_STOCK, quantityRequested, totalAvailable);
            log.info(message);
            formatterResultOutputPort.returnBusinessRuleErrorResponse(400, message);
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * @brief Processes FIFO logic for inventory valuation
     * 
     * Iterates through available purchase lots (ordered by date) and allocates
     * quantities to the output movement until the required quantity is met.
     * 
     * @param availablePurchases List of available purchase lots
     * @param outputMovement The output movement (sale/exit) being processed
     * @return Result containing created details and updated lots
     */
    private FIFOResult processFIFOLogic(List<Kardex> availablePurchases, Kardex outputMovement) {
        List<DetailOutput> detailsToCreate = new ArrayList<>();
        List<Kardex> lotsToUpdate = new ArrayList<>();
        int remainingQuantity = outputMovement.getQuantity();

        for (Kardex purchaseLot : availablePurchases) {
            if (remainingQuantity <= 0) {
                break;
            }

            int amountFromThisLot = Math.min(remainingQuantity, purchaseLot.getAvailableQuantity());

            if (amountFromThisLot > 0) {
                //Usar método para reducir cantidad (incluye validaciones)
                purchaseLot.reduceAvailableQuantity(amountFromThisLot);
                lotsToUpdate.add(purchaseLot);

                //Crear detalle usando Factory Method de dominio
                DetailOutput detail = DetailOutput.create(
                    amountFromThisLot,
                    purchaseLot.getUnitPrice(),
                    outputMovement,
                    purchaseLot
                );
                detailsToCreate.add(detail);

                remainingQuantity -= amountFromThisLot;
            }
        }

        return new FIFOResult(detailsToCreate, lotsToUpdate);
    }

   

 

    /**
     * @brief Container for FIFO processing results
     * @param detailsToCreate List of output details to be persisted
     * @param lotsToUpdate List of purchase lots with updated available quantities
     */
    private record FIFOResult(List<DetailOutput> detailsToCreate, List<Kardex> lotsToUpdate) {}


}
   

   