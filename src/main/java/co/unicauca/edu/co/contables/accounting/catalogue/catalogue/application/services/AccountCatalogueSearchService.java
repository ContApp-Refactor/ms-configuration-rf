package co.unicauca.edu.co.contables.accounting.catalogue.catalogue.application.services;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.application.input.IAccountCatalogueSearchInputPort;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.application.output.IAccountCatalogueSearchOutputPort;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.application.services.validation.AccountCatalogueValidationService;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.domain.models.AccountCatalogue;
import co.unicauca.edu.co.contables.accounting.catalogue.commons.exceptions.catalogue.AccountCatalogueNotFoundException;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * @brief Servicio para operaciones de búsqueda y consulta de cuentas contables
 *
 * Proporciona métodos para buscar cuentas por diferentes criterios,
 * incluyendo búsqueda paginada y consultas jerárquicas.
 */
@Service
@AllArgsConstructor
public class AccountCatalogueSearchService implements IAccountCatalogueSearchInputPort {

    private final IAccountCatalogueSearchOutputPort accountCatalogueSearchOutputPort;
    private final AccountCatalogueValidationService validationService;

    /**
     * @brief Obtiene cuenta por código con validación de existencia
     * @param code código de la cuenta
     * @param idEnterprise ID de la empresa
     * @return cuenta encontrada
     */
    @Override
    public AccountCatalogue getAccountCatalogueByCode(String code, String idEnterprise) {
        return validationService.validateAccountExists(code, idEnterprise);
    }

    /**
     * @brief Obtiene árbol jerárquico por código con validación
     * @param code código de la cuenta raíz
     * @param idEnterprise ID de la empresa
     * @return árbol completo de cuentas
     */
    @Override
    public AccountCatalogue getAccountCatalogueTree(String code, String idEnterprise) {
        // Primero validar que existe
        validationService.validateAccountExists(code, idEnterprise);
        // Luego obtener el árbol
        return accountCatalogueSearchOutputPort.getAccountCatalogueTreeByCode(code, idEnterprise);
    }

    /**
     * @brief Obtiene árboles jerárquicos completos para todas las cuentas raíz (1-9)
     * @param idEnterprise ID de la empresa
     * @return lista de árboles jerárquicos para cuentas raíz existentes
     */
    @Override
    public List<AccountCatalogue> getAccountCatalogueTrees(String idEnterprise) {
        if (idEnterprise == null || idEnterprise.trim().isEmpty()) {
            throw new IllegalArgumentException("El ID de empresa es requerido para buscar árboles de catálogos");
        }

        List<AccountCatalogue> trees = new ArrayList<>();
        for (int i = 1; i <= 9; i++) {
            try {
                AccountCatalogue tree = getAccountCatalogueTree(String.valueOf(i), idEnterprise.trim());
                trees.add(tree);
            } catch (AccountCatalogueNotFoundException e) {
                // Si la cuenta con código 'i' no existe, simplemente continúa con el siguiente
                // No lanza excepción, solo omite la cuenta inexistente
            }
        }
        return trees;
    }

    /**
     * @brief Obtiene cuenta por ID con validación de existencia y empresa
     * @param id ID único de la cuenta contable
     * @param idEnterprise ID de la empresa para aislamiento de datos
     * @return cuenta encontrada con validación previa
     */
    @Override
    public AccountCatalogue getAccountCatalogueById(Long id, String idEnterprise) {
        return validationService.validateAccountExistsByIdAndEnterprise(id, idEnterprise);
    }

    /**
     * @brief Obtiene cuentas auxiliares activas (nivel más bajo de jerarquía)
     * @param idEnterprise ID de la empresa
     * @return lista de cuentas con códigos de 8 dígitos y estado activo
     */
    @Override
    public List<AccountCatalogue> getAuxiliaryAccounts(String idEnterprise) {
        if (idEnterprise == null || idEnterprise.trim().isEmpty()) {
            throw new IllegalArgumentException("El ID de empresa es requerido para buscar cuentas auxiliares");
        }
        
        return accountCatalogueSearchOutputPort.getAuxiliaryAccountsByIdEnterprise(idEnterprise.trim());
    }

    /**
     * @brief Obtiene cuentas auxiliares con funcionalidad de cruce habilitada
     * @param idEnterprise ID de la empresa
     * @return lista de cuentas auxiliares con campo crossing=true
     */
    @Override
    public List<AccountCatalogue> getAuxiliaryAccountsWithCrossing(String idEnterprise) {
        if (idEnterprise == null || idEnterprise.trim().isEmpty()) {
            throw new IllegalArgumentException("El ID de empresa es requerido para buscar cuentas auxiliares con crossing");
        }
        
        return accountCatalogueSearchOutputPort.getAuxiliaryAccountsWithCrossingByIdEnterprise(idEnterprise.trim());
    }

    /**
     * @brief Obtiene página paginada de todas las cuentas por empresa
     * @param idEnterprise ID de la empresa
     * @param pageable configuración de paginación y ordenamiento
     * @return página de cuentas con ordenamiento por código
     */
    @Override
    public Page<AccountCatalogue> getAllAccountCatalogues(String idEnterprise, Pageable pageable) {
        if (idEnterprise == null || idEnterprise.trim().isEmpty()) {
            throw new IllegalArgumentException("El ID de empresa es requerido para buscar catálogos de cuentas");
        }

        return accountCatalogueSearchOutputPort.getAllAccountCataloguesByIdEnterprise(idEnterprise.trim(), pageable);
    }

    /**
     * @brief Obtiene página paginada filtrada por estado de cuentas
     * @param idEnterprise ID de la empresa
     * @param status filtro por estado (true=activo, false=inactivo)
     * @param pageable configuración de paginación y ordenamiento
     * @return página de cuentas filtradas por estado
     */
    @Override
    public Page<AccountCatalogue> getAllAccountCataloguesByStatus(String idEnterprise, Boolean status, Pageable pageable) {
        if (idEnterprise == null || idEnterprise.trim().isEmpty()) {
            throw new IllegalArgumentException("El ID de empresa es requerido para buscar catálogos de cuentas");
        }

        return accountCatalogueSearchOutputPort.getAllAccountCataloguesByIdEnterpriseAndStatus(idEnterprise.trim(), status, pageable);
    }

    /**
     * @brief Obtiene cuentas con parent cargado eagerly para exportación
     * @param idEnterprise ID de la empresa
     * @param status filtro por estado (null = todos, true = activos, false = inactivos)
     * @param pageable configuración de paginación
     * @return página de cuentas con parent cargado
     */
    @Override
    public Page<AccountCatalogue> getAllAccountCataloguesForExport(String idEnterprise, Boolean status, Pageable pageable) {
        if (idEnterprise == null || idEnterprise.trim().isEmpty()) {
            throw new IllegalArgumentException("El ID de empresa es requerido para exportar catálogos de cuentas");
        }

        return ((co.unicauca.edu.co.contables.accounting.catalogue.catalogue.infraestructure.adapters.output.jpaAdapter.AccountCatalogueSearchJpaAdapter) 
                accountCatalogueSearchOutputPort).getAllAccountCataloguesForExport(idEnterprise.trim(), status, pageable);
    }

    /**
     * @brief Obtiene lista completa de cuentas sin paginación
     * @param idEnterprise ID de la empresa
     * @return lista completa de todas las cuentas ordenadas por código
     */
    @Override
    public List<AccountCatalogue> getAllAccountsByEnterprise(String idEnterprise) {
        if (idEnterprise == null || idEnterprise.trim().isEmpty()) {
            throw new IllegalArgumentException("El ID de empresa es requerido para obtener cuentas");
        }

        return accountCatalogueSearchOutputPort.getAllAccountsByEnterprise(idEnterprise.trim());
    }

    /**
     * @brief Realiza búsqueda inteligente por código o descripción
     * @param idEnterprise ID de la empresa
     * @param search término de búsqueda parcial (case-insensitive)
     * @return lista de cuentas que coinciden con código o descripción
     */
    @Override
    public List<AccountCatalogue> getAccountsByCodeOrDescription(String idEnterprise, String search) {
        if (idEnterprise == null || idEnterprise.trim().isEmpty()) {
            throw new IllegalArgumentException("El ID de empresa es requerido para buscar cuentas");
        }
        if (search == null || search.trim().isEmpty()) {
            throw new IllegalArgumentException("El término de búsqueda es requerido");
        }

        return accountCatalogueSearchOutputPort.getAccountsByCodeOrDescription(idEnterprise.trim(), search.trim());
    }

}
