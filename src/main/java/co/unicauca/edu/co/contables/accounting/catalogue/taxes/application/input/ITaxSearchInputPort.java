package co.unicauca.edu.co.contables.accounting.catalogue.taxes.application.input;

import java.util.List;
import org.springframework.data.domain.Page;

import co.unicauca.edu.co.contables.accounting.catalogue.taxes.domain.models.Tax;

/**
 * @brief Puerto de entrada para operaciones de consulta de impuestos
 *
 * Define el contrato para consultar impuestos por código, estado,
 * paginación y filtros de búsqueda.
 */
public interface ITaxSearchInputPort {
    /**
     * @brief Obtiene un impuesto por código y empresa
     * @param code Código del impuesto
     * @param idEnterprise ID de la empresa
     * @return Impuesto encontrado
     */
    Tax getTax(String code, String idEnterprise);

    /**
     * @brief Obtiene lista de impuestos activos por empresa
     * @param idEnterprise ID de la empresa
     * @return Lista de impuestos activos
     */
    List<Tax> getActiveTaxes(String idEnterprise);

    /**
     * @brief Obtiene impuestos paginados por empresa
     * @param idEnterprise ID de la empresa
     * @param page Número de página (0-based)
     * @param size Tamaño de página
     * @param sortField Campo para ordenamiento
     * @param sortOrder Dirección del ordenamiento (asc/desc)
     * @return Página de impuestos
     */
    Page<Tax> getTaxesPaginated(String idEnterprise, int page, int size, String sortField, String sortOrder);

    /**
     * @brief Obtiene impuestos paginados filtrados por código o descripción
     * @param idEnterprise ID de la empresa
     * @param search Término de búsqueda para código o descripción
     * @param page Número de página (0-based)
     * @param size Tamaño de página
     * @param sortField Campo para ordenamiento
     * @param sortOrder Dirección del ordenamiento (asc/desc)
     * @return Página de impuestos filtrados
     */
    Page<Tax> getTaxesByCodeOrDescriptionPaginated(String idEnterprise, String search, int page, int size, String sortField, String sortOrder);

    /**
     * @brief Cuenta total de impuestos por empresa
     * @param idEnterprise ID de la empresa
     * @return Cantidad total de impuestos
     */
    long countTaxesByEnterprise(String idEnterprise);

    /**
     * @brief Cuenta impuestos por empresa filtrados por código o descripción
     * @param idEnterprise ID de la empresa
     * @param search Término de búsqueda para código o descripción
     * @return Cantidad de impuestos filtrados
     */
    long countTaxesByEnterpriseAndCodeOrDescription(String idEnterprise, String search);
}
