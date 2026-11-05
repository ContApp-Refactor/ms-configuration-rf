package co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.output.remoteSync.adapter;

import org.springframework.stereotype.Component;

import co.unicauca.edu.co.contables.inventory.peps.domain.model.Stock;
import co.unicauca.edu.co.contables.inventory.stock.domain.port.IFormatterResultOutputPort;
import co.unicauca.edu.co.contables.inventory.stock.infrastructure.adapters.output.exception.customized.BaseException;
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
 *
 * Contrato de errores: los errores de negocio del módulo de stock ya NO se
 * ocultan como 500 genérico (facturación necesita distinguir "stock
 * insuficiente" de "servicio caído"): {@link BaseException} (p. ej. stock
 * inexistente, 404) se propaga tal cual; las reglas del dominio de stock
 * ({@code IllegalStateException} stock inactivo, {@code IllegalArgumentException}
 * cantidad inválida, es decir stock insuficiente en una salida) se convierten
 * en error de regla de negocio 400; cualquier otra cosa sigue siendo el 500
 * genérico.
 */
@Component("pepsStockClientAdapter")
@RequiredArgsConstructor
@Slf4j
public class StockClientAdapter implements IStockClientPort{

    static final String GENERIC_ERROR = "Unexpected error communicating with stock service";
    static final String INSUFFICIENT_STOCK = "Insufficient stock in stock module: ";

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
        } catch (BaseException e) {
            throw e;
        } catch (IllegalStateException | IllegalArgumentException e) {
            log.warn("Stock module rejected the purchase of product {}: {}", stock.getProductId(), e.getMessage());
            formatterResultOutputPort.returnBusinessRuleErrorResponse(400, e.getMessage());
        } catch(Exception e){
            log.error("Unexpected error when calling stock service: {}", e.getMessage());
            formatterResultOutputPort.returnErrorGenericResponse(500, GENERIC_ERROR);
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
        } catch (BaseException e) {
            throw e;
        } catch (IllegalArgumentException e) {
            log.warn("Stock module rejected the sale of product {}: {}", stock.getProductId(), e.getMessage());
            formatterResultOutputPort.returnBusinessRuleErrorResponse(400, INSUFFICIENT_STOCK + e.getMessage());
        } catch (IllegalStateException e) {
            log.warn("Stock module rejected the sale of product {}: {}", stock.getProductId(), e.getMessage());
            formatterResultOutputPort.returnBusinessRuleErrorResponse(400, e.getMessage());
        } catch(Exception e){
            log.error("Unexpected error when calling stock service: {}", e.getMessage());
            formatterResultOutputPort.returnErrorGenericResponse(500, GENERIC_ERROR);
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
