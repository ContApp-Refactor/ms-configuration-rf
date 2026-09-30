package co.unicauca.edu.co.contables.accounting.catalogue.taxes.application.output;

import java.util.List;
import org.springframework.data.domain.Page;

import co.unicauca.edu.co.contables.accounting.catalogue.taxes.domain.models.Tax;

/**
 * @brief Puerto de salida para operaciones de consulta de impuestos
 *
 * Define el contrato para acceder a datos de impuestos desde el repositorio
 * con soporte para consultas paginadas y filtros de búsqueda.
 */
public interface ITaxSearchOutputPort {

    /**
     * @brief Obtiene un impuesto por código y empresa
     * @param code código del impuesto
     * @param idEnterprise ID de la empresa
     * @return impuesto encontrado
     */
    Tax getTax(String code, String idEnterprise);

    /**
     * @brief Obtiene lista de impuestos activos por empresa
     * @param idEnterprise ID de la empresa
     * @return lista de impuestos activos
     */
    List<Tax> getActiveTaxes(String idEnterprise);

    /**
     * @brief Obtiene todos los impuestos por empresa
     * @param idEnterprise ID de la empresa
     * @return lista completa de impuestos
     */
    List<Tax> getTaxesByEnterprise(String idEnterprise);

    /**
     * @brief Obtiene impuestos paginados por empresa
     * @param idEnterprise ID de la empresa
     * @param page número de página (0-based)
     * @param size tamaño de página
     * @param sortField campo para ordenamiento
     * @param sortOrder dirección del ordenamiento (asc/desc)
     * @return página de impuestos
     */
    Page<Tax> getTaxesPaginated(String idEnterprise, int page, int size, String sortField, String sortOrder);

    /**
     * @brief Obtiene impuestos paginados filtrados por código o descripción
     * @param idEnterprise ID de la empresa
     * @param search término de búsqueda para código o descripción
     * @param page número de página (0-based)
     * @param size tamaño de página
     * @param sortField campo para ordenamiento
     * @param sortOrder dirección del ordenamiento (asc/desc)
     * @return página de impuestos filtrados
     */
    Page<Tax> getTaxesByCodeOrDescriptionPaginated(String idEnterprise, String search, int page, int size, String sortField, String sortOrder);

    /**
     * @brief Cuenta total de impuestos por empresa
     * @param idEnterprise ID de la empresa
     * @return cantidad total de impuestos
     */
    long countTaxesByEnterprise(String idEnterprise);

    /**
     * @brief Cuenta impuestos por empresa filtrados por código o descripción
     * @param idEnterprise ID de la empresa
     * @param search término de búsqueda para código o descripción
     * @return cantidad de impuestos filtrados
     */
    long countTaxesByEnterpriseAndCodeOrDescription(String idEnterprise, String search);

    /**
     * @brief Obtiene un impuesto por ID y empresa
     * @param id ID del impuesto
     * @param idEnterprise ID de la empresa
     * @return impuesto encontrado
     */
    Tax getTaxByIdAndEnterprise(Long id, String idEnterprise);

}
