package co.unicauca.edu.co.contables.inventory.products.application.service.importExport;

import co.unicauca.edu.co.contables.inventory.products.application.ports.output.IProductPersistencePort;
import co.unicauca.edu.co.contables.inventory.products.domain.model.ImportErrorDetail;
import co.unicauca.edu.co.contables.inventory.products.domain.model.ProductExcelData;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @brief Servicio para detección de duplicados en importación de productos
 *
 * Detecta duplicados tanto en el archivo Excel como en el sistema existente.
 * Los duplicados se cuentan pero NO generan errores, solo se omiten durante la importación.
 */
@Service
@RequiredArgsConstructor
public class ProductDuplicateDetectionService {

    private final IProductPersistencePort productPersistencePort;

    /**
     * @brief Detecta duplicados en una lista de productos
     *
     * Realiza verificación de duplicados tanto dentro del archivo Excel como contra
     * productos existentes en el sistema. Los duplicados se cuentan pero no generan
     * errores, simplemente se omiten durante el proceso de importación.
     *
     * @param productsData Lista de datos de productos a verificar
     * @param entId ID de la empresa para filtrar productos existentes
     * @return Resultado de la detección de duplicados con registros únicos y estadísticas
     */
    public DuplicateDetectionResult detectDuplicates(List<ProductExcelData> productsData, String entId) {
        List<ProductExcelData> uniqueRecords = new ArrayList<>();
        List<ImportErrorDetail> errors = new ArrayList<>();
        Set<String> processedReferences = new HashSet<>();
        int duplicateCount = 0;


        // Verificar duplicados en el sistema existente usando existsByReferenceAndEnterpriseId
        for (ProductExcelData productData : productsData) {
            String reference = productData.getReference();

            if (reference != null) {
                // Verificar duplicado en el sistema existente
                if (productPersistencePort.existsByReferenceAndEnterpriseId(reference, entId)) {
                    duplicateCount++;
                    continue;
                }

                // Verificar duplicado dentro del mismo archivo
                if (processedReferences.contains(reference)) {
                    duplicateCount++;
                    continue;
                }

                processedReferences.add(reference);
            }

            uniqueRecords.add(productData);
        }

        return DuplicateDetectionResult.builder()
                .uniqueRecords(uniqueRecords)
                .errors(errors)
                .duplicateCount(duplicateCount)
                .build();
    }

    /**
     * @brief Resultado de la detección de duplicados
     *
     * Contiene los resultados del proceso de detección de duplicados, incluyendo
     * los registros únicos válidos, errores encontrados y estadísticas de duplicados.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DuplicateDetectionResult {
        private List<ProductExcelData> uniqueRecords;
        private List<ImportErrorDetail> errors;
        private int duplicateCount;
    }
}