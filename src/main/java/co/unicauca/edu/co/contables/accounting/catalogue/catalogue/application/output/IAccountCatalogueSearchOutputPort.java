package co.unicauca.edu.co.contables.accounting.catalogue.catalogue.application.output;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.domain.models.AccountCatalogue;

/**
 * @brief Puerto de salida para operaciones de consulta de catálogo de cuentas
 *
 *        Define el contrato para acceder a datos de cuentas contables desde el
 *        repositorio
 *        con soporte para consultas jerárquicas, paginadas y filtros de
 *        búsqueda.
 */
public interface IAccountCatalogueSearchOutputPort {

    /**
     * @brief Obtiene cuenta contable por código y empresa
     * @param code         código de la cuenta
     * @param idEnterprise ID de la empresa
     * @return cuenta encontrada o null
     */
    AccountCatalogue getAccountCatalogueByCode(String code, String idEnterprise);

    /**
     * @brief Obtiene árbol jerárquico completo por código de cuenta raíz
     * @param code         código de la cuenta raíz
     * @param idEnterprise ID de la empresa
     * @return cuenta raíz con toda su jerarquía cargada
     */
    AccountCatalogue getAccountCatalogueTreeByCode(String code, String idEnterprise);

    /**
     * @brief Obtiene cuenta contable por ID y empresa
     * @param id           ID de la cuenta
     * @param idEnterprise ID de la empresa
     * @return cuenta encontrada o null
     */
    AccountCatalogue getAccountCatalogueById(Long id, String idEnterprise);

    /**
     * @brief Obtiene cuenta contable por ID y empresa (método alternativo)
     * @param id           ID de la cuenta
     * @param idEnterprise ID de la empresa
     * @return cuenta encontrada o null
     */
    AccountCatalogue getAccountCatalogueByIdAndIdEnterprise(Long id, String idEnterprise);

    /**
     * @brief Obtiene árbol jerárquico completo por ID de cuenta raíz
     * @param id           ID de la cuenta raíz
     * @param idEnterprise ID de la empresa
     * @return cuenta raíz con toda su jerarquía cargada
     */
    AccountCatalogue getAccountCatalogueTreeByIdAndIdEnterprise(Long id, String idEnterprise);

    /**
     * @brief Busca cuenta por descripción (ignorando mayúsculas) y empresa
     * @param description  descripción de la cuenta
     * @param idEnterprise ID de la empresa
     * @return primera cuenta que coincida con la descripción
     */
    AccountCatalogue getAccountCatalogueByDescriptionIgnoreCaseAndIdEnterprise(String description, String idEnterprise);

    /**
     * @brief Obtiene cuentas auxiliares por empresa
     * @param idEnterprise ID de la empresa
     * @return lista de cuentas auxiliares (hojas del árbol)
     */
    List<AccountCatalogue> getAuxiliaryAccountsByIdEnterprise(String idEnterprise);

    /**
     * @brief Obtiene cuentas auxiliares con información de cruce
     * @param idEnterprise ID de la empresa
     * @return lista de cuentas auxiliares con datos adicionales de cruce
     */
    List<AccountCatalogue> getAuxiliaryAccountsWithCrossingByIdEnterprise(String idEnterprise);

    /**
     * @brief Obtiene página paginada de todas las cuentas por empresa
     * @param idEnterprise ID de la empresa
     * @param pageable     configuración de paginación
     * @return página de cuentas contables
     */
    Page<AccountCatalogue> getAllAccountCataloguesByIdEnterprise(String idEnterprise, Pageable pageable);

    /**
     * @brief Obtiene todas las cuentas por empresa sin paginación
     * @param idEnterprise ID de la empresa
     * @return lista completa de cuentas contables
     */
    List<AccountCatalogue> getAllAccountsByEnterprise(String idEnterprise);

    /**
     * @brief Busca cuentas por código o descripción
     * @param idEnterprise ID de la empresa
     * @param search       término de búsqueda parcial
     * @return lista de cuentas que coincidan con la búsqueda
     */
    List<AccountCatalogue> getAccountsByCodeOrDescription(String idEnterprise, String search);

    /**
     * @brief Obtiene página paginada filtrada por estado
     * @param idEnterprise ID de la empresa
     * @param status       estado de las cuentas (true=activo, false=inactivo)
     * @param pageable     configuración de paginación
     * @return página de cuentas filtradas por estado
     */
    Page<AccountCatalogue> getAllAccountCataloguesByIdEnterpriseAndStatus(String idEnterprise, Boolean status,
            Pageable pageable);
}
