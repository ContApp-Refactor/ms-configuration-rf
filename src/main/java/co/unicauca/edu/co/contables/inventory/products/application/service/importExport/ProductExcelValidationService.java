package co.unicauca.edu.co.contables.inventory.products.application.service.importExport;

import co.unicauca.edu.co.contables.inventory.products.application.ports.input.ICategoryServicePort;
import co.unicauca.edu.co.contables.inventory.products.application.ports.input.IProductTypeServicePort;
import co.unicauca.edu.co.contables.inventory.products.application.ports.input.IUnitOfMeasureServicePort;
import co.unicauca.edu.co.contables.inventory.products.domain.exception.ErrorCode;
import co.unicauca.edu.co.contables.inventory.products.domain.exception.product.ExcelValidationException;
import co.unicauca.edu.co.contables.inventory.products.domain.model.Category;
import co.unicauca.edu.co.contables.inventory.products.domain.model.ProductType;
import co.unicauca.edu.co.contables.inventory.products.domain.model.UnitOfMeasure;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.xssf.usermodel.XSSFDataValidationHelper;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @brief Servicio centralizado para validaciones de Excel en productos
 *
 * Gestiona todas las validaciones de datos Excel para importación/exportación de productos.
 * Maneja obtención de datos de entidades relacionadas, aplicación de validaciones
 * de formato y creación de listas desplegables para garantizar integridad de datos.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProductExcelValidationService {

    private final ICategoryServicePort categoryServicePort;
    private final IProductTypeServicePort productTypeServicePort;
    private final IUnitOfMeasureServicePort unitOfMeasureServicePort;

    // Constantes para mensajes
    private static final String VALIDATION_ERROR_TITLE = "Error de Validación";
    private static final String ENTER_PROMPT_PREFIX = "Ingrese ";

    // ========== MÉTODOS DE OBTENCIÓN DE DATOS ==========

    /**
     * @brief Obtiene todas las categorías activas para una entidad
     *
     * Consulta el servicio de categorías para obtener la lista completa de categorías
     * activas de una empresa, ordenadas por nombre. Retorna lista vacía si no existen
     * categorías activas para la validación de listas desplegables en Excel.
     *
     * @param entId ID de la entidad (empresa) para filtrar categorías
     * @return Lista de nombres de categorías activas ordenadas alfabéticamente
     */
    public List<String> getCategoryOptions(String entId) {
        long totalActive = categoryServicePort.countActiveCategoriesByEntId(entId);
        if (totalActive == 0) {
            return List.of();
        }

        var page = categoryServicePort.getAllActiveCategoriesByWithSort(entId, 0, (int) totalActive, "name", "asc");
        return page.getContent().stream()
                .map(Category::getName)
                .toList();
    }

    /**
     * @brief Obtiene todos los tipos de producto activos para una entidad
     *
     * Consulta el servicio de tipos de producto para obtener la lista completa de tipos
     * activos de una empresa. Retorna lista vacía si no existen tipos activos
     * para la validación de listas desplegables en Excel.
     *
     * @param entId ID de la entidad (empresa) para filtrar tipos de producto
     * @return Lista de nombres de tipos de producto activos
     */
    public List<String> getProductTypeOptions(String entId) {
        long totalActive = productTypeServicePort.countActivatedByEnterpriseId(entId);
        if (totalActive == 0) {
            return List.of();
        }

        var page = productTypeServicePort.findActivatedWithPagination(entId, 0, (int) totalActive);
        return page.getContent().stream()
                .map(ProductType::getName)
                .toList();
    }

    /**
     * @brief Obtiene todas las unidades de medida activas para una entidad
     *
     * Consulta el servicio de unidades de medida para obtener la lista completa de unidades
     * activas de una empresa. Retorna lista vacía si no existen unidades activas
     * para la validación de listas desplegables en Excel.
     *
     * @param entId ID de la entidad (empresa) para filtrar unidades de medida
     * @return Lista de nombres de unidades de medida activas
     */
    public List<String> getUnitOfMeasureOptions(String entId) {
        long totalActive = unitOfMeasureServicePort.countActiveUnitOfMeasuresByEntId(entId);
        if (totalActive == 0) {
            return List.of();
        }

        var page = unitOfMeasureServicePort.getAllActiveUnitOfMeasuresBy(entId, 0, (int) totalActive);
        return page.getContent().stream()
                .map(UnitOfMeasure::getName)
                .toList();
    }

    /**
     * @brief Obtiene opciones de estado disponibles para productos
     *
     * Retorna las opciones fijas de estado que puede tener un producto:
     * ACTIVO para productos disponibles y INACTIVO para productos dados de baja.
     * Estas opciones se utilizan en validaciones de listas desplegables.
     *
     * @return Lista fija con las opciones "ACTIVO" e "INACTIVO"
     */
    public List<String> getStatusOptions() {
        return List.of("ACTIVO", "INACTIVO");
    }

    // ========== MÉTODOS DE APLICACIÓN DE VALIDACIONES ==========

    /**
     * @brief Aplica todas las validaciones de productos a una hoja Excel
     *
     * Orquesta la aplicación completa de validaciones para todas las columnas de productos:
     * textos obligatorios, números con formato específico, listas desplegables de entidades
     * relacionadas y validaciones de estado. Aplica cada tipo de validación en su columna
     * correspondiente dentro del rango especificado.
     *
     * @param sheet Hoja de trabajo Excel donde aplicar todas las validaciones
     * @param entId ID de la entidad para obtener datos de entidades relacionadas
     * @param startRow Fila inicial del rango donde aplicar validaciones
     * @param endRow Fila final del rango donde aplicar validaciones
     * @throws ExcelValidationException si ocurre error aplicando cualquier validación
     */
    public void applyProductValidations(Sheet sheet, String entId, int startRow, int endRow) throws ExcelValidationException {
        // Columna 0: Código (validación de texto personalizado)
        applyCodeValidation(sheet, 0, startRow, endRow);

        // Columna 1: Nombre (validación básica de texto)
        applyTextValidation(sheet, 1, startRow, endRow, "Nombre del producto");

        // Columna 2: Referencia/SKU (validación básica de texto)
        applyTextValidation(sheet, 2, startRow, endRow, "Referencia/SKU del producto");

        // Columna 3: Presentación (validación básica de texto)
        applyTextValidation(sheet, 3, startRow, endRow, "Presentación del producto");

        // Columna 4: Descripción (validación básica de texto)
        applyTextValidation(sheet, 4, startRow, endRow, "Descripción del producto");

        // Columna 5: Costo (validación numérica decimal)
        applyDecimalValidation(sheet, 5, startRow, endRow, "Costo del producto");

        // Columna 6: Cantidad (validación numérica entera)
        applyIntegerValidation(sheet, 6, startRow, endRow, "Cantidad del producto");

        // Columna 7: Unidad de Medida
        applyDropdownValidation(sheet, 7, startRow, endRow,
                getUnitOfMeasureOptions(entId),
                "Seleccione una unidad de medida válida");

        // Columna 8: Categoría
        applyDropdownValidation(sheet, 8, startRow, endRow,
                getCategoryOptions(entId),
                "Seleccione una categoría válida");

        // Columna 9: Tipo de Producto
        applyDropdownValidation(sheet, 9, startRow, endRow,
                getProductTypeOptions(entId),
                "Seleccione un tipo de producto válido");

        // Columna 10: Estado
        applyDropdownValidation(sheet, 10, startRow, endRow,
                getStatusOptions(),
                "Seleccione ACTIVO o INACTIVO");
    }

    /**
     * @brief Aplica validación de código personalizado a una columna
     *
     * Implementa validación personalizada usando fórmulas Excel para asegurar que
     * el código del producto no esté vacío. Utiliza TRIM() para ignorar espacios
     * en blanco y proporciona mensajes de error y ayuda contextual al usuario.
     *
     * @param sheet Hoja de trabajo Excel donde aplicar la validación
     * @param columnIndex Índice de la columna donde validar códigos de producto
     * @param startRow Fila inicial del rango de validación
     * @param endRow Fila final del rango de validación
     * @throws ExcelValidationException si ocurre error configurando la validación
     */
    public void applyCodeValidation(Sheet sheet, int columnIndex, int startRow, int endRow) throws ExcelValidationException {
        try {
            XSSFSheet xssfSheet = (XSSFSheet) sheet;
            XSSFDataValidationHelper validationHelper = new XSSFDataValidationHelper(xssfSheet);

            CellRangeAddressList addressList = new CellRangeAddressList(startRow, endRow, columnIndex, columnIndex);

            // Validación personalizada: texto no vacío
            // Convertir índice de columna a letra de columna (0=A, 1=B, etc.)
            String columnLetter = getColumnLetter(columnIndex);
            String formula = "LEN(TRIM(" + columnLetter + (startRow + 1) + ")) > 0";

            DataValidationConstraint constraint = validationHelper.createCustomConstraint(formula);

            DataValidation validation = validationHelper.createValidation(constraint, addressList);

            validation.setShowErrorBox(true);
            validation.setErrorStyle(DataValidation.ErrorStyle.STOP);
            validation.createErrorBox(VALIDATION_ERROR_TITLE,
                    "El código del producto es obligatorio");

            validation.setShowPromptBox(true);
            validation.createPromptBox("Código de Producto",
                    "Ingrese un código único para el producto");

            sheet.addValidationData(validation);

        } catch (Exception e) {
            throw new ExcelValidationException(ErrorCode.EXCEL_VALIDATION_ERROR, "Error al aplicar validación de código", e);
        }
    }

    /**
     * @brief Aplica validación de texto básico a una columna
     *
     * Configura validación de texto obligatoria usando fórmulas Excel personalizadas
     * para asegurar que campos de texto requeridos no estén vacíos. Maneja espacios
     * en blanco con TRIM() y proporciona mensajes de error específicos por campo.
     *
     * @param sheet Hoja de trabajo Excel donde aplicar la validación
     * @param columnIndex Índice de la columna donde validar texto obligatorio
     * @param startRow Fila inicial del rango de validación
     * @param endRow Fila final del rango de validación
     * @param fieldName Nombre del campo para personalizar mensajes de error y ayuda
     * @throws ExcelValidationException si ocurre error configurando la validación
     */
    public void applyTextValidation(Sheet sheet, int columnIndex, int startRow, int endRow, String fieldName) throws ExcelValidationException {
        try {
            XSSFSheet xssfSheet = (XSSFSheet) sheet;
            XSSFDataValidationHelper validationHelper = new XSSFDataValidationHelper(xssfSheet);

            CellRangeAddressList addressList = new CellRangeAddressList(startRow, endRow, columnIndex, columnIndex);

            // Validación personalizada: texto no vacío
            // Convertir índice de columna a letra de columna (0=A, 1=B, etc.)
            String columnLetter = getColumnLetter(columnIndex);
            String formula = "LEN(TRIM(" + columnLetter + (startRow + 1) + ")) > 0";

            DataValidationConstraint constraint = validationHelper.createCustomConstraint(formula);

            DataValidation validation = validationHelper.createValidation(constraint, addressList);

            validation.setShowErrorBox(true);
            validation.setErrorStyle(DataValidation.ErrorStyle.STOP);
            validation.createErrorBox(VALIDATION_ERROR_TITLE,
                    "El campo " + fieldName.toLowerCase() + " es obligatorio");

            validation.setShowPromptBox(true);
            validation.createPromptBox(fieldName,
                    ENTER_PROMPT_PREFIX + fieldName.toLowerCase());

            sheet.addValidationData(validation);

        } catch (Exception e) {
            throw new ExcelValidationException(ErrorCode.EXCEL_VALIDATION_ERROR, "Error al aplicar validación de texto en columna " + columnIndex, e);
        }
    }

    /**
     * @brief Aplica validación de números decimales a una columna
     *
     * Configura validación numérica decimal con rango específico (0.00 a 999999999.99)
     * para campos como costos. Permite celdas vacías ya que son campos opcionales,
     * pero valida formato y rango cuando se ingresa un valor.
     *
     * @param sheet Hoja de trabajo Excel donde aplicar la validación
     * @param columnIndex Índice de la columna donde validar números decimales
     * @param startRow Fila inicial del rango de validación
     * @param endRow Fila final del rango de validación
     * @param fieldName Nombre del campo para personalizar mensajes de error y ayuda
     * @throws ExcelValidationException si ocurre error configurando la validación
     */
    public void applyDecimalValidation(Sheet sheet, int columnIndex, int startRow, int endRow, String fieldName) throws ExcelValidationException {
        try {
            XSSFSheet xssfSheet = (XSSFSheet) sheet;
            XSSFDataValidationHelper validationHelper = new XSSFDataValidationHelper(xssfSheet);

            CellRangeAddressList addressList = new CellRangeAddressList(startRow, endRow, columnIndex, columnIndex);

            // Validación numérica decimal (mayor o igual a 0)
            DataValidationConstraint constraint = validationHelper.createDecimalConstraint(
                    DataValidationConstraint.OperatorType.GREATER_OR_EQUAL,
                    "0",
                    "999999999.99");

            DataValidation validation = validationHelper.createValidation(constraint, addressList);

            validation.setShowErrorBox(true);
            validation.setErrorStyle(DataValidation.ErrorStyle.STOP);
            validation.createErrorBox(VALIDATION_ERROR_TITLE,
                    "El " + fieldName.toLowerCase() + " debe ser un número mayor o igual a 0");

            validation.setShowPromptBox(true);
            validation.createPromptBox(fieldName,
                    ENTER_PROMPT_PREFIX + fieldName.toLowerCase() + " (ejemplo: 150.50)");

            // Permitir celdas vacías (campo opcional)
            validation.setEmptyCellAllowed(true);

            sheet.addValidationData(validation);

        } catch (Exception e) {
            throw new ExcelValidationException(ErrorCode.EXCEL_VALIDATION_ERROR, "Error al aplicar validación decimal en columna " + columnIndex, e);
        }
    }

    /**
     * @brief Aplica validación de números enteros a una columna
     *
     * Configura validación numérica entera con rango específico (0 a 999999999)
     * para campos como cantidades. Permite celdas vacías ya que son campos opcionales,
     * pero valida que sean números enteros positivos cuando se ingresa un valor.
     *
     * @param sheet Hoja de trabajo Excel donde aplicar la validación
     * @param columnIndex Índice de la columna donde validar números enteros
     * @param startRow Fila inicial del rango de validación
     * @param endRow Fila final del rango de validación
     * @param fieldName Nombre del campo para personalizar mensajes de error y ayuda
     * @throws ExcelValidationException si ocurre error configurando la validación
     */
    public void applyIntegerValidation(Sheet sheet, int columnIndex, int startRow, int endRow, String fieldName) throws ExcelValidationException {
        try {
            XSSFSheet xssfSheet = (XSSFSheet) sheet;
            XSSFDataValidationHelper validationHelper = new XSSFDataValidationHelper(xssfSheet);

            CellRangeAddressList addressList = new CellRangeAddressList(startRow, endRow, columnIndex, columnIndex);

            // Validación numérica entera (mayor o igual a 0)
            DataValidationConstraint constraint = validationHelper.createIntegerConstraint(
                    DataValidationConstraint.OperatorType.GREATER_OR_EQUAL,
                    "0",
                    "999999999");

            DataValidation validation = validationHelper.createValidation(constraint, addressList);

            validation.setShowErrorBox(true);
            validation.setErrorStyle(DataValidation.ErrorStyle.STOP);
            validation.createErrorBox(VALIDATION_ERROR_TITLE,
                    "La " + fieldName.toLowerCase() + " debe ser un número entero mayor o igual a 0");

            validation.setShowPromptBox(true);
            validation.createPromptBox(fieldName,
                    ENTER_PROMPT_PREFIX + fieldName.toLowerCase() + " (ejemplo: 100)");

            // Permitir celdas vacías (campo opcional)
            validation.setEmptyCellAllowed(true);

            sheet.addValidationData(validation);

        } catch (Exception e) {
            throw new ExcelValidationException(ErrorCode.EXCEL_VALIDATION_ERROR, "Error al aplicar validación entera en columna " + columnIndex, e);
        }
    }

    /**
     * @brief Aplica validación de lista desplegable a una columna específica
     *
     * Crea validaciones de listas desplegables usando las opciones proporcionadas.
     * Si no hay opciones disponibles, omite la validación silenciosamente.
     * Configura mensajes de error específicos y tooltips de ayuda para guiar al usuario.
     *
     * @param sheet Hoja de trabajo Excel donde aplicar la validación
     * @param columnIndex Índice de la columna donde aplicar la lista desplegable
     * @param startRow Fila inicial del rango de validación
     * @param endRow Fila final del rango de validación
     * @param options Lista de opciones disponibles para la lista desplegable
     * @param errorMessage Mensaje de error personalizado cuando se selecciona opción inválida
     * @throws ExcelValidationException si ocurre error configurando la validación
     */
    public void applyDropdownValidation(Sheet sheet, int columnIndex, int startRow, int endRow,
            List<String> options, String errorMessage) {
        if (options == null || options.isEmpty()) {
            return;
        }

        try {
            XSSFSheet xssfSheet = (XSSFSheet) sheet;
            XSSFDataValidationHelper validationHelper = new XSSFDataValidationHelper(xssfSheet);

            // Crear el rango de celdas donde aplicar la validación
            CellRangeAddressList addressList = new CellRangeAddressList(startRow, endRow, columnIndex, columnIndex);

            // Crear la lista de opciones
            String[] optionsArray = options.toArray(new String[0]);
            DataValidationConstraint constraint = validationHelper.createExplicitListConstraint(optionsArray);

            // Crear la validación
            DataValidation validation = validationHelper.createValidation(constraint, addressList);

            // Configurar propiedades de la validación
            validation.setShowErrorBox(true);
            validation.setErrorStyle(DataValidation.ErrorStyle.STOP);
            validation.createErrorBox(VALIDATION_ERROR_TITLE, errorMessage);

            // Mostrar lista desplegable
            validation.setShowPromptBox(true);
            validation.createPromptBox("Selección", "Seleccione una opción de la lista");

            // Aplicar la validación a la hoja
            sheet.addValidationData(validation);

        } catch (Exception e) {
            throw new ExcelValidationException(ErrorCode.EXCEL_VALIDATION_ERROR,
                    "Error al aplicar validación en columna " + columnIndex, e);
        }
    }

    /**
     * @brief Convierte índice de columna numérico a letra Excel (A, B, C, etc.)
     *
     * Transforma un índice de columna basado en cero (0=A, 1=B, 2=C) al formato
     * de letra de columna Excel estándar. Maneja columnas más allá de la Z
     * usando múltiples letras (AA, AB, etc.) para fórmulas de validación.
     *
     * @param columnIndex Índice numérico de la columna (0 = A, 1 = B, etc.)
     * @return Letra o combinación de letras correspondiente a la columna Excel
     */
    private String getColumnLetter(int columnIndex) {
        StringBuilder columnLetter = new StringBuilder();
        int temp = columnIndex;

        while (temp >= 0) {
            columnLetter.insert(0, (char) ('A' + (temp % 26)));
            temp = (temp / 26) - 1;
        }

        return columnLetter.toString();
    }
}
