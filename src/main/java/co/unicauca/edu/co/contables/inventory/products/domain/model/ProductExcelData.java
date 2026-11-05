package co.unicauca.edu.co.contables.inventory.products.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @brief Modelo para datos de producto extraídos de Excel
 *
 * Representa la estructura temporal de datos durante el proceso de importación,
 * incluyendo campos directos y nombres temporales para resolución de entidades relacionadas.
 */
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class ProductExcelData {

    private int rowNumber;
    private String enterpriseId;
    private String name;
    private String description;
    private Integer quantity;
    private Long unitOfMeasureId;
    private Long categoryId;
    private Long productTypeId;
    private Double cost;
    private String reference;
    private String presentation;
    private String unitOfMeasureName;
    private String categoryName;
    private String productTypeName;
}