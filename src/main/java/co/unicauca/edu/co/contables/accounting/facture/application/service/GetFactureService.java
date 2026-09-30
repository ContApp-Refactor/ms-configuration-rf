package co.unicauca.edu.co.contables.accounting.facture.application.service;

import co.unicauca.edu.co.contables.accounting.facture.application.ports.input.GetFactureUseCase;
import co.unicauca.edu.co.contables.accounting.facture.application.ports.output.FactureGetOutputPort;
import co.unicauca.edu.co.contables.accounting.facture.domain.exception.FactureNotFound;
import co.unicauca.edu.co.contables.accounting.facture.domain.model.Facture;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class GetFactureService implements GetFactureUseCase {
    private final FactureGetOutputPort factureGetOutputPort;

    /**
     * Recupera una Factura por su ID.
     *
     * @param factId el ID de la Factura a recuperar
     * @return el objeto Factura si se encuentra
     * @throws FacturaNoEncontrada si no se encuentra ninguna Factura con el ID dado
     */
    @Override
    public Facture getFactureBy(Long factId) {
        return factureGetOutputPort.getFactureById(factId)
                .orElseThrow(() -> new FactureNotFound("Facture not found with id " + factId));
    }

}
