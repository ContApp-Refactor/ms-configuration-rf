package co.unicauca.edu.co.contables.accounting.catalogue.taxes.application.services;

import co.unicauca.edu.co.contables.accounting.catalogue.taxes.application.input.ITaxSearchInputPort;
import co.unicauca.edu.co.contables.accounting.catalogue.taxes.application.output.ITaxSearchOutputPort;
import co.unicauca.edu.co.contables.accounting.catalogue.taxes.domain.models.Tax;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @brief Servicio de aplicación para consultas de impuestos
 *
 * Implementa la lógica de negocio para consultas paginadas y filtradas
 * de impuestos con validaciones de acceso.
 */
@Service
@AllArgsConstructor
public class TaxSearchService implements ITaxSearchInputPort {
    private final ITaxSearchOutputPort taxSearchOutputPort;
    private final TaxValidationService taxValidationService;

    /**
     * @brief Obtiene impuesto por código y empresa
     * @param code código del impuesto
     * @param idEnterprise ID de la empresa
     * @return impuesto encontrado
     */
    @Override
    public Tax getTax(String code, String idEnterprise) {
        // Validar que el impuesto existe
        taxValidationService.validateTaxExists(code, idEnterprise);
        
        // Si existe, obtenerlo y devolverlo
        return taxSearchOutputPort.getTax(code, idEnterprise);
    }

    /**
     * @brief Obtiene lista de impuestos activos por empresa
     * @param idEnterprise ID de la empresa
     * @return lista de impuestos activos
     */
    @Override
    public List<Tax> getActiveTaxes(String idEnterprise) {
        return taxSearchOutputPort.getActiveTaxes(idEnterprise);
    }

    /**
     * @brief Obtiene página de impuestos por empresa con paginación
     * @param idEnterprise ID de la empresa
     * @param page número de página
     * @param size tamaño de página
     * @param sortField campo de ordenamiento
     * @param sortOrder orden (asc/desc)
     * @return página de impuestos
     */
    @Override
    public Page<Tax> getTaxesPaginated(String idEnterprise, int page, int size, String sortField, String sortOrder) {
        return taxSearchOutputPort.getTaxesPaginated(idEnterprise, page, size, sortField, sortOrder);
    }

    /**
     * @brief Obtiene página de impuestos filtrados por código o descripción
     * @param idEnterprise ID de la empresa
     * @param search término de búsqueda en código o descripción
     * @param page número de página
     * @param size tamaño de página
     * @param sortField campo de ordenamiento
     * @param sortOrder orden (asc/desc)
     * @return página de impuestos filtrados
     */
    @Override
    public Page<Tax> getTaxesByCodeOrDescriptionPaginated(String idEnterprise, String search, int page, int size, String sortField, String sortOrder) {
        return taxSearchOutputPort.getTaxesByCodeOrDescriptionPaginated(idEnterprise, search, page, size, sortField, sortOrder);
    }

    /**
     * @brief Cuenta total de impuestos por empresa
     * @param idEnterprise ID de la empresa
     * @return cantidad total de impuestos
     */
    @Override
    public long countTaxesByEnterprise(String idEnterprise) {
        return taxSearchOutputPort.countTaxesByEnterprise(idEnterprise);
    }

    /**
     * @brief Cuenta impuestos filtrados por código o descripción
     * @param idEnterprise ID de la empresa
     * @param search término de búsqueda en código o descripción
     * @return cantidad de impuestos filtrados
     */
    @Override
    public long countTaxesByEnterpriseAndCodeOrDescription(String idEnterprise, String search) {
        return taxSearchOutputPort.countTaxesByEnterpriseAndCodeOrDescription(idEnterprise, search);
    }
}
