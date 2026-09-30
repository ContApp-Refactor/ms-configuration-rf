package co.unicauca.edu.co.contables.accounting.catalogue.catalogue.application.services;

import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.domain.models.AccountCatalogue;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.domain.models.AccountCatalogueExcelData;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.domain.utils.AccountCodeUtils;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.domain.utils.StringNormalizer;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.infraestructure.adapters.output.jpaAdapter.entity.AccountCatalogueEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * @brief Servicio para conversión de datos de Excel a entidades de dominio
 *
 * Convierte datos extraídos de archivos Excel a entidades del dominio
 * de cuentas contables, manejando relaciones jerárquicas y normalización.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AccountCatalogueDataConverter {

    /**
     * Convierte datos de Excel a entidad de dominio AccountCatalogue.
     * 
     * @param excelData datos parseados desde Excel
     * @param parentsMap mapa de códigos padre a entidades padre (para establecer relación)
     * @param processedAccountsMap mapa de cuentas ya procesadas en este lote
     * @return entidad de dominio lista para persistir
     */
    public AccountCatalogue convertToAccountCatalogue(AccountCatalogueExcelData excelData, 
                                                     Map<String, AccountCatalogueEntity> parentsMap,
                                                     Map<String, AccountCatalogue> processedAccountsMap) {
        AccountCatalogue.AccountCatalogueBuilder builder = AccountCatalogue.builder();

        builder.code(StringNormalizer.normalizeCode(excelData.getCode()));
        builder.description(StringNormalizer.normalizeDescription(excelData.getDescription()));
        builder.idEnterprise(excelData.getIdEnterprise());

        builder.nature(excelData.getNature());
        builder.financialStatus(excelData.getFinancialStatus());
        builder.classification(excelData.getClassification());

        builder.crossing(excelData.getCrossing() != null ? excelData.getCrossing() : false);
        builder.costCenter(excelData.getCostCenter() != null ? excelData.getCostCenter() : false);

        builder.status(true);

        AccountCatalogue parent = resolveParent(excelData, parentsMap, processedAccountsMap);
        if (parent != null) {
            builder.parent(parent);
        }

        return builder.build();
    }

    /**
     * @brief Resuelve padre jerárquico de cuenta Excel con búsqueda en múltiples fuentes
     * 
     * @param excelData datos de Excel a resolver
     * @param parentsMap mapa de códigos padre a entidades padre (para establecer relación)
     * @param processedAccountsMap mapa de cuentas ya procesadas en este lote
     * @return entidad de dominio padre resuelta o null si no se encuentra
     */
    private AccountCatalogue resolveParent(AccountCatalogueExcelData excelData, 
                                          Map<String, AccountCatalogueEntity> parentsMap,
                                          Map<String, AccountCatalogue> processedAccountsMap) {
        String code = excelData.getCode();
        if (code == null || code.trim().isEmpty()) {
            return null;
        }

        // Determinar código del padre
        String parentCode = AccountCodeUtils.extractParentCode(code);
        if (parentCode == null) {
            return null;
        }

        // Buscar primero en cuentas procesadas en este lote
        if (processedAccountsMap.containsKey(parentCode)) {
            AccountCatalogue parent = processedAccountsMap.get(parentCode);
            return parent;
        }

        // Buscar en el mapa de padres de la BD
        if (parentsMap.containsKey(parentCode)) {
            AccountCatalogue parent = convertEntityToDomain(parentsMap.get(parentCode));
            return parent;
        }

        // No se encontró el padre (esto no debería pasar si la validación jerárquica funcionó)
        return null;
    }

    /**
     * @brief Convierte entidad JPA completa a modelo de dominio con relaciones
     * 
     * @param entity entidad JPA a convertir
     * @return modelo de dominio con relaciones resueltas
     */
    private AccountCatalogue convertEntityToDomain(AccountCatalogueEntity entity) {
        if (entity == null) {
            return null;
        }

        AccountCatalogue.AccountCatalogueBuilder builder = AccountCatalogue.builder()
                .id(entity.getId())  // CRÍTICO: ID necesario para FK
                .code(entity.getCode())
                .description(entity.getDescription())
                .nature(entity.getNature())
                .financialStatus(entity.getFinancialStatus())
                .classification(entity.getClassification())
                .crossing(entity.getCrossing())
                .costCenter(entity.getCostCenter())
                .status(entity.getStatus())
                .idEnterprise(entity.getIdEnterprise());        
        
        // Incluir parent si existe (para jerarquías anidadas)
        if (entity.getParent() != null) {
            builder.parent(AccountCatalogue.builder()
                    .id(entity.getParent().getId())
                    .code(entity.getParent().getCode())
                    .build());
        }
        
        AccountCatalogue result = builder.build();
        return result;
    }

}

