package co.unicauca.edu.co.contables.inventory.products.domain.exception;

import lombok.Getter;

/**
 * @brief Catálogo estandarizado de códigos de error
 *
 * Define códigos de error reutilizables para toda la aplicación,
 * organizados por dominio (productos, categorías, unidades de medida, etc.).
 */
@Getter
public enum ErrorCode implements ErrorCodeDefinition {

    GENERIC_ERROR("GENERIC_ERROR", "Ha ocurrido un error"),

    //Codigos de producto
    PRODUCT_NOT_FOUND("PRODUCT_NOT_FOUND", "Producto no encontrado"),
    INVALID_PRODUCT("INVALID_PRODUCT", "Producto inválido"),
    PRODUCT_NAME_ALREADY_EXISTS("PRODUCT_NAME_ALREADY_EXISTS", "Ya existe un producto con este nombre"),
    PRODUCT_REFERENCE_ALREADY_EXISTS("PRODUCT_REFERENCE_ALREADY_EXISTS", "Ya existe un producto con esta referencia"),
    PRODUCT_IN_USE("PRODUCT_IN_USE", "El producto está siendo usado y no puede ser modificado o eliminado"),

    //Codigos de unidad de medida
    UNITOFMEASURE_NOT_FOUND("UNITOFMEASURE_NOT_FOUND", "Unidad de medida no encontrada"),
    INVALID_UNITOFMEASURE("INVALID_UNITOFMEASURE", "Unidad de medida inválida"),
    UNITOFMEASURE_ASSOCIATED("UNITOFMEASURE_ASSOCIATED_WITH_PRODUCT", "La unidad de medida está asociada con un producto"),
    UNITOFMEASURE_IN_USE("UNITOFMEASURE_IN_USE", "La unidad de medida no se puede editar porque contiene productos con movimientos contables"),
    UNITOFMEASURE_NAME_ALREADY_EXISTS("UNITOFMEASURE_NAME_ALREADY_EXISTS", "Ya existe una unidad de medida con este nombre"),
    UNITOFMEASURE_ABBREVIATION_ALREADY_EXISTS("UNITOFMEASURE_ABBREVIATION_ALREADY_EXISTS", "Ya existe una unidad de medida con esta abreviación"),

    //Codigos de categoría
    CATEGORY_NOT_FOUND("CATEGORY_NOT_FOUND", "Categoría no encontrada"),
    INVALID_CATEGORY("INVALID_CATEGORY", "Categoría inválida"),
    CATEGORY_ASSOCIATED("CATEGORY_ASSOCIATED_WITH_PRODUCT", "La categoría está asociada con un producto"),
    CATEGORY_IN_USE("CATEGORY_IN_USE", "La categoría no se puede editar porque contiene productos con movimientos contables"),
    CATEGORY_NAME_ALREADY_EXISTS("CATEGORY_NAME_ALREADY_EXISTS", "Ya existe una categoría con este nombre"),

    //Codigos de tipo de producto
    PRODUCT_TYPE_NOT_FOUND("PRODUCT_TYPE_NOT_FOUND", "Tipo de producto no encontrado"),
    INVALID_PRODUCT_TYPE("INVALID_PRODUCT_TYPE", "Tipo de producto inválido"),
    PRODUCT_TYPE_ASSOCIATED("PRODUCT_TYPE_ASSOCIATED_WITH_PRODUCT", "El tipo de producto está asociado con un producto"),
    PRODUCT_TYPE_IN_USE("PRODUCT_TYPE_IN_USE", "El tipo de producto no se puede editar porque contiene productos con movimientos contables"),
    PRODUCT_TYPE_NAME_ALREADY_EXISTS("PRODUCT_TYPE_NAME_ALREADY_EXISTS", "Ya existe un tipo de producto con este nombre"),

    //Códigos de Excel
    EXCEL_VALIDATION_ERROR("EXCEL_VALIDATION_ERROR", "Error de validación en archivo Excel"),
    FILE_VALIDATION_ERROR("FILE_VALIDATION_ERROR", "Error de validación de archivo"),
    PRODUCT_EXPORT_NO_DATA("PRODUCT_EXPORT_NO_DATA", "No hay productos para exportar"),
    PRODUCT_EXPORT_ERROR("PRODUCT_EXPORT_ERROR", "Error al exportar productos");

    private final String code;
    private final String message;

    ErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }
}
