package co.unicauca.edu.co.contables.accounting.facture.application.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import co.unicauca.edu.co.contables.accounting.facture.application.ports.input.ListFactureUseCase;
import co.unicauca.edu.co.contables.accounting.facture.application.ports.output.FactureGetOutputPort;
import co.unicauca.edu.co.contables.accounting.facture.domain.exception.FacturesNotFound;
import co.unicauca.edu.co.contables.accounting.facture.domain.model.Facture;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class ListFacturesService implements ListFactureUseCase {
    private final FactureGetOutputPort factureGetOutputPort;

    /**
     * Recupera una lista paginada de todas las facturas asociadas a una empresa
     * determinada.
     *
     * @param entId    el ID de la empresa
     * @param pageable la configuración de paginación
     * @return una lista paginada de facturas
     * @throws FacturasNoEncontradas si no se encuentran facturas para la empresa
     *                               dada
     */
    @Override
    public Page<Facture> getAllFacturesBy(String entId, Pageable pageable) {
        Page<Facture> result = factureGetOutputPort.getAllFacturesBy(entId, pageable);
        if (result.isEmpty()) {
            throw new FacturesNotFound("No factures found for enterprise id " + entId);
        }
        return result;
    }

    /**
     * Recupera una lista paginada de todas las facturas de ventas asociadas a una
     * empresa determinada.
     *
     * @param entId    el ID de la empresa
     * @param pageable la configuraci n de paginaci n
     * @return una lista paginada de facturas de ventas
     * @throws FacturasNoEncontradas si no se encuentran facturas de ventas para la
     *                               empresa dada
     */
    @Override
    public Page<Facture> getAllSalesFacturesBy(String entId, Pageable pageable) {
        Page<Facture> result = factureGetOutputPort.getAllSalesFacturesBy(entId, pageable);
        if (result.isEmpty()) {
            throw new FacturesNotFound("No sales factures found for enterprise id " + entId);
        }
        return result;
    }

    /**
     * Recupera una lista paginada de todas las facturas de compras asociadas a una
     * empresa determinada.
     *
     * @param entId    el ID de la empresa
     * @param pageable la configuraci n de paginaci n
     * @return una lista paginada de facturas de compras
     * @throws FacturasNoEncontradas si no se encuentran facturas de compras para la
     *                               empresa dada
     */
    @Override
    public Page<Facture> getAllShoppingFacturesBy(String entId, Pageable pageable) {
        Page<Facture> result = factureGetOutputPort.getAllShoppingFacturesBy(entId, pageable);
        if (result.isEmpty()) {
            throw new FacturesNotFound("No shopping factures found for enterprise id " + entId);
        }
        return result;
    }
}
