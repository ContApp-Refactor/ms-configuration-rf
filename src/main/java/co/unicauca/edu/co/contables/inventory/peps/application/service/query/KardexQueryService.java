package co.unicauca.edu.co.contables.inventory.peps.application.service.query;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import co.unicauca.edu.co.contables.inventory.peps.application.ports.input.IKardexQueryPort;
import co.unicauca.edu.co.contables.inventory.peps.domain.model.Balance;
import co.unicauca.edu.co.contables.inventory.peps.domain.model.InventoryQueue;
import co.unicauca.edu.co.contables.inventory.peps.domain.model.Kardex;
import co.unicauca.edu.co.contables.inventory.peps.domain.model.KardexMigration;
import co.unicauca.edu.co.contables.inventory.peps.domain.model.KardexReport;
import co.unicauca.edu.co.contables.inventory.peps.domain.model.SaleDetail;
import co.unicauca.edu.co.contables.inventory.peps.domain.port.output.query.IKardexQueryOutputPort;
import lombok.RequiredArgsConstructor;

/**
 * @brief Service implementation for Kardex query operations
 * 
 * Handles retrieval and reporting of inventory movements, including
 * generation of kardex reports with FIFO valuation and balance calculation.
 */
@Service
@RequiredArgsConstructor
public class KardexQueryService implements IKardexQueryPort {

     private final IKardexQueryOutputPort kardexQueryOutputPort;

    /**
     * @brief Generates a paginated kardex report for a product
     * 
     * Reconstructs the inventory state by processing historical movements
     * to calculate balances and valuations for the requested period.
     * 
     * @param productId Product identifier
     * @param start Start date of the report
     * @param end End date of the report
     * @param pageable Pagination parameters
     * @return Page of kardex reports
     */
    @Override
    public Page<KardexReport> getRecordsKardexByProduct(Long productId, LocalDate start, LocalDate end, Pageable pageable) {

        if(start==null || end==null) {
            start = LocalDate.of(2000,1,1);
            end = LocalDate.now();

        }

        // Paso 1: Construir estado inicial de la cola (no se puede paginar)
        List<Kardex> prevMovements = kardexQueryOutputPort.findMovementsByProductBeforeDate(productId, start);
        InventoryQueue inventoryQueue = new InventoryQueue();
        processInitialMovements(prevMovements, inventoryQueue);

        // Paso 2: Obtener TODOS los movimientos del período (necesario para calcular índices correctos)
        // Nota: No podemos paginar aquí porque cada reporte depende del estado acumulado
        List<Kardex> periodMovements = kardexQueryOutputPort.findMovementsByProductAndDateRange(productId, start, end);
        
        // Paso 3: Calcular índices de paginación
        int pageSize = pageable.getPageSize();
        int currentPage = pageable.getPageNumber();
        int totalElements = periodMovements.size();
        int startIndex = currentPage * pageSize;
        
        // Validar si la página existe
        if (startIndex >= totalElements && totalElements > 0) {
            return new PageImpl<>(Collections.emptyList(), pageable, totalElements);
        }
        
        // Paso 4: Procesar SOLO hasta el índice necesario (optimización)
        int endIndex = Math.min(startIndex + pageSize, totalElements);
        List<KardexReport> pageReports = new ArrayList<>();
        
        // Procesar todos los movimientos hasta el final de la página solicitada
        for (int i = 0; i < endIndex; i++) {
            Kardex movement = periodMovements.get(i);
            KardexReport report = processMovementToReport(movement, inventoryQueue);
            
            // Solo agregar al resultado si está en el rango de la página
            if (i >= startIndex) {
                pageReports.add(report);
            }
        }

        return new PageImpl<>(pageReports, pageable, totalElements);
    }

    /**
     * @brief Processes movements prior to the report period
     * 
     * Updates the inventory queue state to reflect the starting balance
     * for the report period.
     * 
     * @param movements List of historical movements
     * @param queue Inventory queue to update
     */
    private void processInitialMovements(List<Kardex> movements, InventoryQueue queue) {
        for (Kardex movement : movements) {
            processMovementForQueue(movement, queue);
        }
    }

    /**
     * @brief Generates reports for a list of movements
     * @param movements List of movements to process
     * @param queue Current inventory queue state
     * @return List of generated reports
     */
    private List<KardexReport> generateKardexReports(List<Kardex> movements, InventoryQueue queue) {
        List<KardexReport> reports = new ArrayList<>();

        for (Kardex movement : movements) {
            KardexReport report = processMovementToReport(movement, queue);
            reports.add(report);
        }

        return reports;
    }


    /**
     * @brief Converts a single movement into a report entry
     * 
     * Dispatches processing based on movement type (Entry, Output, Return).
     * 
     * @param movement Movement to process
     * @param queue Current inventory queue state
     * @return Generated kardex report
     */
    private KardexReport processMovementToReport(Kardex movement, InventoryQueue queue) {
        KardexReport report;

   
        if (movement.isPurchase() || movement.isSaleReturn() || movement.isNonCommercialEntry()) {
            report = handleEntryMovement(movement, queue);
        } 

        else if (movement.isSale() || movement.isNonCommercialExit()) {
            report = handleOutputMovement(movement, queue);
        } 
     
        else if (movement.isPurchaseReturn()) {
            report = handlePurchaseReturnMovement(movement, queue);
        } 
        else {
            // Fallback para tipos desconocidos
            report = handleUnknownMovement(movement, queue);
        }

        return report;
    }

  
    /**
     * @brief Handles entry movements (Purchase, Sale Return, etc.)
     * @param movement Entry movement
     * @param queue Inventory queue
     * @return Report for the entry
     */
    private KardexReport handleEntryMovement(Kardex movement, InventoryQueue queue) {
        Balance balance = Balance.fromMovement(movement.getQuantity(), movement.getUnitPrice());
        
       
        if (movement.isSaleReturn()) {
            queue.addBalanceFirst(balance);
        } else {
            queue.addBalance(balance);
        }
        
        return KardexReport.createEntryReport(movement, queue.getBalancesCopy());
    }

    /**
     * @brief Handles output movements (Sale, Non-Commercial Exit)
     * @param movement Output movement
     * @param queue Inventory queue
     * @return Report for the output
     */
    private KardexReport handleOutputMovement(Kardex movement, InventoryQueue queue) {
        List<SaleDetail> details = queue.consumeQuantityForReport(movement.getQuantity());
        return KardexReport.createOutputReport(movement, details, queue.getBalancesCopy());
    }

   
    /**
     * @brief Handles purchase return movements
     * @param movement Purchase return movement
     * @param queue Inventory queue
     * @return Report for the return
     */
    private KardexReport handlePurchaseReturnMovement(Kardex movement, InventoryQueue queue) {
        List<SaleDetail> returnDetails = queue.removeByUnitPrice(
            movement.getQuantity(), 
            movement.getUnitPrice()
        );
        return KardexReport.createOutputReport(movement, returnDetails, queue.getBalancesCopy());
    }

    
    /**
     * @brief Handles unknown movement types as a fallback
     * @param movement Unknown movement
     * @param queue Inventory queue
     * @return Generated report
     */
    private KardexReport handleUnknownMovement(Kardex movement, InventoryQueue queue) {
        // Determinar por availableQuantity como último recurso
        if (movement.getAvailableQuantity() > 0) {
            Balance balance = Balance.fromMovement(movement.getQuantity(), movement.getUnitPrice());
            queue.addBalance(balance);
            return KardexReport.createEntryReport(movement, queue.getBalancesCopy());
        } else {
            List<SaleDetail> details = queue.consumeQuantityForReport(movement.getQuantity());
            return KardexReport.createOutputReport(movement, details, queue.getBalancesCopy());
        }
    }

    /**
     * @brief Updates the inventory queue state without generating a report
     * 
     * Used for processing initial movements to establish starting balance.
     * 
     * @param movement Movement to process
     * @param queue Inventory queue to update
     */
    private void processMovementForQueue(Kardex movement, InventoryQueue queue) {
       
        if (movement.isPurchase() || movement.isNonCommercialEntry()) {
            Balance balance = Balance.fromMovement(movement.getQuantity(), movement.getUnitPrice());
            queue.addBalance(balance);
        } 
        else if (movement.isSaleReturn()) {
            Balance balance = Balance.fromMovement(movement.getQuantity(), movement.getUnitPrice());
            queue.addBalanceFirst(balance);
        }
        //  SALIDAS: Venta + Salida no comercial
        else if (movement.isSale() || movement.isNonCommercialExit()) {
            queue.consumeQuantityForReport(movement.getQuantity());
        } 
        //  Devolución de compra
        else if (movement.isPurchaseReturn()) {
            queue.removeByUnitPrice(movement.getQuantity(), movement.getUnitPrice());
        }
    }

    /**
     * @brief Validates enterprise identifier
     * @param enterpriseId Enterprise ID to validate
     * @throws IllegalArgumentException if ID is null or empty
     */
    private void validateEnterpriseId(String enterpriseId) {
        if (enterpriseId == null || enterpriseId.trim().isEmpty()) {
            throw new IllegalArgumentException("Enterprise ID cannot be null or empty");
        }
    }

    /**
     * @brief Retrieves available quantity details for a product
     * @param productId Product identifier
     * @return List of available purchase lots
     */
    @Override
    public List<Kardex> getKardexAvailableQuantityByProduct(Long productId) {
        return kardexQueryOutputPort.findAvailablePurchasesOrderedByDate(productId);
    }

    /**
     * @brief Retrieves the last kardex record for all products of an enterprise
     * @param enterpriseId Enterprise identifier
     * @return List of latest kardex records
     */
    @Override
    public List<KardexMigration> findLastKardexForAllProducts(String enterpriseId) {
        validateEnterpriseId(enterpriseId);
        return kardexQueryOutputPort.findLastKardexForAllProducts(enterpriseId);
    }

    
}