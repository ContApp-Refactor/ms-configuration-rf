package co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.output.remoteSync.adapter;

import org.springframework.stereotype.Component;

import co.unicauca.edu.co.contables.inventory.peps.domain.model.Stock;
import co.unicauca.edu.co.contables.inventory.stock.domain.port.IFormatterResultOutputPort;
import co.unicauca.edu.co.contables.inventory.peps.domain.port.output.external.IStockClientPort;
import co.unicauca.edu.co.contables.inventory.stock.application.ports.input.IStockCommandPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @brief Adapter for Stock Service communication
 * 
 * Antes de la migración llamaba por HTTP a {@code lb://STOCK}
 * (IStockClient + ClientConfig + WebClient con propagación de JWT). Como el
 * monolito tiene el código de stock en proceso, ahora inyecta el puerto de
 * aplicación {@link IStockCommandPort}, que aplica la misma validación que el
 * controller remoto (stock inexistente, stock inactivo, cantidad/precio
 * inválidos) antes de persistir.
 *
 * Solo se copian {@code productId}, {@code quantity} y {@code price}: eran los
 * únicos campos del DTO que viajaban por HTTP y los únicos que usa
 * {@code StockCommandService.registerPurchase/registerSale}. El estado y el
 * registro a persistir los resuelve stock a partir del stock ya existente.
 */
@Component("pepsStockClientAdapter")
@RequiredArgsConstructor
@Slf4j
public class StockClientAdapter implements IStockClientPort{

    private final IStockCommandPort stockCommandPort;
    private final IFormatterResultOutputPort formatterResultOutputPort;

    /**
     * @brief Updates external stock for a purchase or entry
     * 
     * Registra la entrada de stock en el módulo de stock del monolito.
     * 
     * @param stock The stock domain object containing quantity and product details
     */
    @Override
    public void buyStock(Stock stock) {

        try {
            stockCommandPort.registerPurchase(toStockModuleDomain(stock));
        } catch(Exception e){
            log.error("Unexpected error when calling stock service: {}", e.getMessage());
            formatterResultOutputPort.returnErrorGenericResponse(500,"Unexpected error communicating with stock service");
        }
        
       
    }

    /**
     * @brief Updates external stock for a sale or exit
     * 
     * Registra la salida de stock en el módulo de stock del monolito.
     * 
     * @param stock The stock domain object containing quantity and product details
     */
    @Override
    public void sellStock(Stock stock) {
        try {
            stockCommandPort.registerSale(toStockModuleDomain(stock));
        }catch(Exception e){
            log.error("Unexpected error when calling stock service: {}", e.getMessage());
            formatterResultOutputPort.returnErrorGenericResponse(500,"Unexpected error communicating with stock service");
        }
    }

    /**
     * Proyecta el modelo de dominio de peps sobre el de stock. Coinciden en los
     * tres campos que viajaban por HTTP ({@code productId}, {@code quantity},
     * {@code price}); el resto (id, name, enterpriseId, state) lo resuelve el
     * módulo de stock a partir del registro existente.
     */
    private co.unicauca.edu.co.contables.inventory.stock.domain.model.Stock toStockModuleDomain(Stock source) {
        co.unicauca.edu.co.contables.inventory.stock.domain.model.Stock target =
                new co.unicauca.edu.co.contables.inventory.stock.domain.model.Stock();
        target.setProductId(source.getProductId());
        target.setQuantity(source.getQuantity());
        target.setPrice(source.getPrice());
        return target;
    }
    
}
