package co.unicauca.edu.co.contables.accounting.catalogue.catalogue.application.services;

import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.domain.enums.ImportErrorType;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.domain.models.AccountCatalogue;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.domain.models.AccountCatalogueExcelData;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.domain.models.ImportErrorDetail;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.domain.utils.AccountCodeUtils;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.domain.utils.ImportConstants;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.infraestructure.adapters.output.jpaAdapter.entity.AccountCatalogueEntity;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.infraestructure.adapters.output.jpaAdapter.repository.IAccountCatalogueRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @brief Servicio para procesamiento de jerarquías de cuentas contables
 *
 * Maneja el ordenamiento y procesamiento de cuentas según su jerarquía,
 * asegurando que las cuentas padre se procesen antes que las hijas.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AccountCatalogueHierarchyProcessor {

    private final IAccountCatalogueRepository accountCatalogueRepository;

    /**
     * @brief Ordena cuentas por jerarquía para procesamiento secuencial
     * @param accountsData lista de cuentas a ordenar
     * @return lista ordenada por jerarquía (padres antes que hijos)
     */
    public List<AccountCatalogueExcelData> sortByHierarchy(List<AccountCatalogueExcelData> accountsData) {
        if (accountsData == null || accountsData.isEmpty()) {
            return new ArrayList<>();
        }

        // Ordenar por jerarquía (código natural)
        return AccountCodeUtils.sortByHierarchy(accountsData);
    }

    /**
     * @brief Valida jerarquía de cuentas con detalles de errores
     * @param accountsData lista de cuentas a validar
     * @param entId ID de la empresa
     * @return lista de errores encontrados en la jerarquía
     */
    public List<ImportErrorDetail> validateHierarchyWithDetails(
            List<AccountCatalogueExcelData> accountsData, String entId) {
        List<ImportErrorDetail> errors = new ArrayList<>();

        Map<String, AccountCatalogueExcelData> accountMap = buildHierarchyMap(accountsData);

        for (AccountCatalogueExcelData account : accountsData) {
            String code = account.getCode();

            // Si es cuenta raíz (1 dígito), no necesita padre
            if (AccountCodeUtils.isRootAccount(code)) {
                continue;
            }

            String parentCode = AccountCodeUtils.extractParentCode(code);
            if (parentCode == null) {
                continue;
            }

            boolean parentInExcel = accountMap.containsKey(parentCode);

            boolean parentInDatabase = false;
            if (!parentInExcel) {
                parentInDatabase = existsInDatabase(parentCode, entId);
            }

            if (!parentInExcel && !parentInDatabase) {
                errors.add(ImportErrorDetail.builder()
                        .rowNumber(account.getRowNumber())
                        .columnNumber(1)
                        .columnName("Código")
                        .fieldValue(code)
                        .errorCode(ImportConstants.ErrorCodes.ORPHAN_ACCOUNT)
                        .errorMessage(String.format(
                                "La cuenta '%s' requiere una cuenta padre '%s' que no existe ni en el Excel ni en el sistema",
                                code, parentCode))
                        .errorType(ImportErrorType.HIERARCHY_ERROR)
                        .build());
            }
        }

        return errors;
    }

    /**
     * @brief Construye mapa de jerarquía a partir de datos Excel
     */
    private Map<String, AccountCatalogueExcelData> buildHierarchyMap(List<AccountCatalogueExcelData> accountsData) {
        return accountsData.stream()
                .filter(account -> account.getCode() != null && !account.getCode().trim().isEmpty())
                .collect(Collectors.toMap(
                        account -> account.getCode().trim(),
                        account -> account,
                        (existing, replacement) -> existing // En caso de duplicados, mantener el primero
                ));
    }

    /**
     * @brief Verifica existencia de cuenta en base de datos
     */
    private boolean existsInDatabase(String code, String entId) {
        try {
            AccountCatalogueEntity existing = accountCatalogueRepository.findByCode(code, entId);
            return existing != null;
        } catch (Exception e) {
            return false;
        }
    }

   
    /**
     * @brief Construye mapa de cuentas padre desde BD con jerarquía completa
     * @details Usa findByCodeWithFullHierarchy para cargar toda la cadena de padres eagerly,
     * evitando LazyInitializationException en procesamiento asíncrono batch.
     * Crítico para importación donde las entidades se usan fuera del contexto transaccional original.
     * @param parentCodes conjunto de códigos de cuentas padre a buscar
     * @param entId ID de empresa para filtrado
     * @return mapa con códigos como keys y entidades completamente cargadas como values
     */
    public Map<String, AccountCatalogueEntity> buildParentMapFromDatabase(Set<String> parentCodes, String entId) {
        Map<String, AccountCatalogueEntity> parentMap = new HashMap<>();

        for (String parentCode : parentCodes) {
            if (parentCode != null && !parentMap.containsKey(parentCode)) {
                try {
                    // Usar findByCodeWithFullHierarchy en lugar de findByCode para cargar toda la jerarquía
                    AccountCatalogueEntity parent = accountCatalogueRepository.findByCodeWithFullHierarchy(parentCode, entId);
                    if (parent != null) {
                        parentMap.put(parentCode, parent);
                    }
                } catch (Exception e) {
                    log.error("Error al obtener cuenta padre con código {}: {}", parentCode, e.getMessage(), e);
                    throw new RuntimeException("Error al obtener cuenta padre con código: " + parentCode, e);
                }
            }
        }
        
        return parentMap;
    }

    /**
     * @brief Recarga una cuenta con toda su jerarquía de padres
     * @details Usa findByCodeWithFullHierarchy para cargar toda la cadena de padres,
     * evitando LazyInitializationException. Convierte entity a dominio.
     * @param code código de la cuenta a recargar
     * @param entId ID de empresa
     * @return cuenta de dominio con jerarquía completa cargada, o null si no existe
     */
    public AccountCatalogue reloadAccountWithFullHierarchy(String code, String entId) {
        AccountCatalogueEntity entity = accountCatalogueRepository.findByCodeWithFullHierarchy(code, entId);
        if (entity == null) {
            return null;
        }
        
        // Convertir a dominio (necesitamos un mapper aquí)
        return convertEntityToDomain(entity);
    }

    /**
     * @brief Convierte entity a dominio con parent si existe
     * @param entity entidad JPA a convertir
     * @return modelo de dominio
     */
    private AccountCatalogue convertEntityToDomain(AccountCatalogueEntity entity) {
        if (entity == null) {
            return null;
        }
        
        AccountCatalogue parent = null;
        if (entity.getParent() != null) {
            parent = convertEntityToDomain(entity.getParent());
        }
        
        return AccountCatalogue.builder()
                .id(entity.getId())
                .idEnterprise(entity.getIdEnterprise())
                .code(entity.getCode())
                .description(entity.getDescription())
                .nature(entity.getNature())
                .financialStatus(entity.getFinancialStatus())
                .classification(entity.getClassification())
                .parent(parent)
                .crossing(entity.getCrossing())
                .costCenter(entity.getCostCenter())
                .status(entity.getStatus())
                .build();
    }
}
