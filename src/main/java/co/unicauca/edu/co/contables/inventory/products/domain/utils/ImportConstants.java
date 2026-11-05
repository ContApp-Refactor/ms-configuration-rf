package co.unicauca.edu.co.contables.inventory.products.domain.utils;

/**
 * @brief Constantes centralizadas para importación de productos
 *
 * Define todas las constantes utilizadas en el proceso de importación desde Excel:
 * nombres de columnas, validaciones, códigos de error y valores por defecto.
 * Centraliza configuración para facilitar mantenimiento.
 */
public final class ImportConstants {

    private ImportConstants() {
        throw new UnsupportedOperationException("ImportConstants es una clase de utilidad y no debe ser instanciada");
    }

    public static final String[] SUPPORTED_EXTENSIONS = {".xlsx", ".xls"};
    public static final long MAX_FILE_SIZE = 5242880L; // 5 * 1024 * 1024


    public static final String NAME_COLUMN = "Nombre";
    public static final String DESCRIPTION_COLUMN = "Descripción";
    public static final String UNIT_MEASURE_COLUMN = "Unidad de Medida";
    public static final String CATEGORY_COLUMN = "Categoría";
    public static final String PRODUCT_TYPE_COLUMN = "Tipo de Producto";
    public static final String REFERENCE_COLUMN = "Referencia";
    public static final String PRESENTATION_COLUMN = "Presentación";
    public static final String QUANTITY_COLUMN = "Cantidad";
    public static final String COST_COLUMN = "Costo";

    // ===== ENCABEZADOS DE EXCEL =====

    public static final String[] REQUIRED_HEADERS = {
        NAME_COLUMN,
        DESCRIPTION_COLUMN,
        UNIT_MEASURE_COLUMN,
        CATEGORY_COLUMN,
        PRODUCT_TYPE_COLUMN,
        REFERENCE_COLUMN,
        PRESENTATION_COLUMN
    };
   
    public static final String[] OPTIONAL_HEADERS = {
        QUANTITY_COLUMN,
        COST_COLUMN
    };


    /**
     * @brief Códigos de error estandarizados para importación
     *
     * Define códigos únicos para diferentes tipos de errores que pueden ocurrir
     * durante el proceso de importación, facilitando el manejo y logging de errores.
     */
    public static final class ErrorCodes {
        public static final String REQUIRED_FIELD_MISSING = "REQUIRED_FIELD_MISSING";
        public static final String INVALID_FORMAT = "INVALID_FORMAT";
        public static final String INVALID_NUMBER = "INVALID_NUMBER";
        public static final String INVALID_REFERENCE = "INVALID_REFERENCE";
        public static final String DUPLICATE_PRODUCT = "DUPLICATE_PRODUCT";
        public static final String BUSINESS_RULE_VIOLATION = "BUSINESS_RULE_VIOLATION";
        public static final String SYSTEM_ERROR = "SYSTEM_ERROR";

        private ErrorCodes() {}
    }


    /**
     * @brief Mensajes de error estándar para importación
     *
     * Define mensajes de error reutilizables que proporcionan información clara
     * al usuario sobre problemas comunes durante la importación de archivos.
     */
    public static final class ErrorMessages {
        public static final String SYSTEM_ERROR = "Error del sistema durante la importación";
        public static final String EMPTY_FILE = "El archivo no contiene datos para importar";
        public static final String INVALID_HEADERS = "El archivo no contiene los encabezados requeridos";

        private ErrorMessages() {}
    }


    /**
     * @brief Valores por defecto para configuraciones de importación
     *
     * Define valores predeterminados para parámetros de configuración que pueden
     * ser ajustados según necesidades específicas del proceso de importación.
     */
    public static final class Defaults {
        public static final int COLUMN_START_INDEX = 1;
        public static final int BATCH_SIZE = 1000;
        public static final boolean SKIP_DUPLICATES = true;
        public static final boolean CONTINUE_ON_ERROR = true;

        private Defaults() {}
    }


    /**
     * @brief Validaciones específicas para campos de importación
     *
     * Define límites y restricciones para campos numéricos y de texto
     * durante el proceso de importación, asegurando integridad de datos.
     */
    public static final class Validations {
        public static final long MAX_QUANTITY = Long.MAX_VALUE;
        public static final double MAX_COST = Double.MAX_VALUE;
        public static final int MAX_REFERENCE_LENGTH = 255;
        public static final int MAX_PRESENTATION_LENGTH = 255;

        private Validations() {}
    }
}
