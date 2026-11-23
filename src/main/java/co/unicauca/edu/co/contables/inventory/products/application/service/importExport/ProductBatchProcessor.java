package co.unicauca.edu.co.contables.inventory.products.application.service.importExport;

import co.unicauca.edu.co.contables.inventory.products.application.ports.output.IProductPersistencePort;
import co.unicauca.edu.co.contables.inventory.products.domain.enums.ImportErrorType;
import co.unicauca.edu.co.contables.inventory.products.domain.model.ImportErrorDetail;
import co.unicauca.edu.co.contables.inventory.products.domain.model.Product;
import co.unicauca.edu.co.contables.inventory.products.domain.model.ProductExcelData;
import co.unicauca.edu.co.contables.inventory.products.domain.utils.ImportConstants;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * @brief Servicio para procesamiento por lotes de productos importados.
 * Maneja la inserción en BD en lotes transaccionales.
 */
@Service
@RequiredArgsConstructor
public class ProductBatchProcessor {

    private final IProductPersistencePort productPersistencePort;

    /**
     * @brief Procesa lote de productos Excel dividiendo en sub-lotes para optimización
     * @param productsData lista de datos de productos a procesar
     * @param entId ID de la empresa
     * @return resultado del procesamiento por lotes
     */
    @Transactional
    public BatchProcessingResult processBatch(List<ProductExcelData> productsData, String entId) {
        List<ImportErrorDetail> errors = new ArrayList<>();
        int successCount = 0;
        int failureCount = 0;

        // Dividir en lotes más pequeños para procesamiento
        List<List<ProductExcelData>> batches = partitionList(productsData, ImportConstants.Defaults.BATCH_SIZE);

        for (List<ProductExcelData> batch : batches) {
            BatchProcessingResult batchResult = processSingleBatch(batch);
            successCount += batchResult.getSuccessCount();
            failureCount += batchResult.getFailureCount();
            errors.addAll(batchResult.getErrors());
        }

        return BatchProcessingResult.builder()
                .successCount(successCount)
                .failureCount(failureCount)
                .errors(errors)
                .build();
    }

    /**
     * @brief Procesa un sub-lote individual de productos usando saveAll para optimización
     * @details Convierte todos los productos del batch y los guarda en una sola operación
     * @param batch sub-lote de datos Excel a procesar
     * @return resultado del procesamiento del sub-lote
     */
    private BatchProcessingResult processSingleBatch(List<ProductExcelData> batch) {
        List<ImportErrorDetail> errors = new ArrayList<>();
        List<Product> productsToSave = new ArrayList<>();

        // Convertir todos los productos del batch
        for (ProductExcelData productData : batch) {
            try {
                Product product = convertToProduct(productData);
                productsToSave.add(product);
            } catch (Exception e) {
                errors.add(ImportErrorDetail.builder()
                        .rowNumber(productData.getRowNumber())
                        .errorCode(ImportConstants.ErrorCodes.SYSTEM_ERROR)
                        .errorMessage("Error convirtiendo producto '" + productData.getName() + "': " + e.getMessage())
                        .errorType(ImportErrorType.SYSTEM_ERROR)
                        .build());
            }
        }

        // Guardar todos los productos en una sola operación batch
        int successCount = 0;
        int failureCount = 0;

        if (!productsToSave.isEmpty()) {
            try {
                // Nota: El doble save es necesario porque generateCode() requiere el ID
                // que solo se asigna después del primer save
                
                // 1. Insertar todos los productos (obtienen IDs automáticamente)
                List<Product> savedProducts = productPersistencePort.saveAll(productsToSave);
                
                // 2. Generar códigos usando los IDs recién asignados
                for (Product savedProduct : savedProducts) {
                    savedProduct.generateCode();
                }
                
                // 3. Actualizar con códigos generados
                productPersistencePort.saveAll(savedProducts);
                successCount = savedProducts.size();

            } catch (Exception e) {
                failureCount = productsToSave.size();
                // Si falla el batch completo, agregar error por cada producto
                for (int i = 0; i < batch.size(); i++) {
                    errors.add(ImportErrorDetail.builder()
                            .rowNumber(batch.get(i).getRowNumber())
                            .errorCode(ImportConstants.ErrorCodes.SYSTEM_ERROR)
                            .errorMessage("Error guardando batch: " + e.getMessage())
                            .errorType(ImportErrorType.SYSTEM_ERROR)
                            .build());
                }
            }
        }

        return BatchProcessingResult.builder()
                .successCount(successCount)
                .failureCount(failureCount)
                .errors(errors)
                .build();
    }

    /**
     * @brief Convierte datos Excel a entidad Product con validaciones y normalización
     * @param excelData datos del producto desde Excel
     * @return entidad Product lista para persistir
     */
    private Product convertToProduct(ProductExcelData excelData) {
        return Product.builder()
                .name(excelData.getName())
                .description(excelData.getDescription())
                .quantity(excelData.getQuantity())
                .unitOfMeasureId(excelData.getUnitOfMeasureId())
                .categoryId(excelData.getCategoryId())
                .enterpriseId(excelData.getEnterpriseId())
                .cost(excelData.getCost() != null ? excelData.getCost() : 0.0)
                .state(true) // Por defecto activo
                .reference(excelData.getReference())
                .productTypeId(excelData.getProductTypeId())
                .presentation(excelData.getPresentation())
                .build();
    }

    /**
     * @brief Divide una lista grande en sub-listas más pequeñas de tamaño fijo
     * @param <T> tipo de elementos en la lista
     * @param list lista original a dividir
     * @param batchSize tamaño máximo de cada sub-lista
     * @return lista de sub-listas particionadas
     */
    private <T> List<List<T>> partitionList(List<T> list, int batchSize) {
        List<List<T>> batches = new ArrayList<>();
        for (int i = 0; i < list.size(); i += batchSize) {
            batches.add(list.subList(i, Math.min(i + batchSize, list.size())));
        }
        return batches;
    }

    /**
     * @brief Resultado del procesamiento por lotes.     * 
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BatchProcessingResult {
        private int successCount;
        private int failureCount;
        private List<ImportErrorDetail> errors;
    }
}