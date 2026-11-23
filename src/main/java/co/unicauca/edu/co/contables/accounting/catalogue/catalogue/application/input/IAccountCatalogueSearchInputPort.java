package co.unicauca.edu.co.contables.accounting.catalogue.catalogue.application.input;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.domain.models.AccountCatalogue;

/**
 * @brief Puerto de entrada para operaciones de consulta de catálogo de cuentas
 *
 * Define el contrato para consultar cuentas contables por código, jerarquía,
 * estado y filtros de búsqueda con soporte para paginación.
 */
public interface IAccountCatalogueSearchInputPort {

    /**
     * @brief Obtiene cuenta contable por código y empresa
     * @param code código de la cuenta contable
     * @param idEnterprise ID de la empresa
     * @return cuenta contable encontrada
     */
    AccountCatalogue getAccountCatalogueByCode(String code, String idEnterprise);

    /**
     * @brief Obtiene árbol jerárquico de cuenta contable por código
     * @param code código de la cuenta raíz
     * @param idEnterprise ID de la empresa
     * @return cuenta con toda su jerarquía de subcuentas
     */
    AccountCatalogue getAccountCatalogueTree(String code, String idEnterprise);

    /**
     * @brief Obtiene lista de árboles jerárquicos de cuentas por empresa
     * @param idEnterprise ID de la empresa
     * @return lista completa de cuentas con sus jerarquías
     */
    List<AccountCatalogue> getAccountCatalogueTrees(String idEnterprise);

    /**
     * @brief Obtiene cuenta contable por ID y empresa
     * @param id ID de la cuenta contable
     * @param idEnterprise ID de la empresa
     * @return cuenta contable encontrada
     */
    AccountCatalogue getAccountCatalogueById(Long id, String idEnterprise);

    /**
     * @brief Obtiene cuentas auxiliares por empresa
     * @param idEnterprise ID de la empresa
     * @return lista de cuentas auxiliares (hojas del árbol)
     */
    List<AccountCatalogue> getAuxiliaryAccounts(String idEnterprise);

    /**
     * @brief Obtiene cuentas auxiliares con cruce por empresa
     * @param idEnterprise ID de la empresa
     * @return lista de cuentas auxiliares con información de cruce
     */
    List<AccountCatalogue> getAuxiliaryAccountsWithCrossing(String idEnterprise);

    /**
     * @brief Obtiene página paginada de todas las cuentas por empresa
     * @param idEnterprise ID de la empresa
     * @param pageable configuración de paginación
     * @return página de cuentas contables
     */
    Page<AccountCatalogue> getAllAccountCatalogues(String idEnterprise, Pageable pageable);

    /**
     * @brief Obtiene página paginada de cuentas filtradas por estado
     * @param idEnterprise ID de la empresa
     * @param status estado de las cuentas (activo/inactivo)
     * @param pageable configuración de paginación
     * @return página de cuentas filtradas por estado
     */
    Page<AccountCatalogue> getAllAccountCataloguesByStatus(String idEnterprise, Boolean status, Pageable pageable);

    /**
     * @brief Obtiene cuentas con parent cargado eagerly para exportación
     * @param idEnterprise ID de la empresa
     * @param status estado de las cuentas (null = todos, true = activos, false = inactivos)
     * @param pageable configuración de paginación
     * @return página de cuentas con parent cargado
     */
    Page<AccountCatalogue> getAllAccountCataloguesForExport(String idEnterprise, Boolean status, Pageable pageable);

    /**
     * @brief Obtiene todas las cuentas por empresa sin paginación
     * @param idEnterprise ID de la empresa
     * @return lista completa de cuentas contables
     */
    List<AccountCatalogue> getAllAccountsByEnterprise(String idEnterprise);


    /**
     * @brief Obtiene cuentas filtradas por código o descripción
     * @param idEnterprise ID de la empresa
     * @param search término de búsqueda para código o descripción
     * @return lista de cuentas que coinciden con la búsqueda
     */
    List<AccountCatalogue> getAccountsByCodeOrDescription(String idEnterprise, String search);
}
