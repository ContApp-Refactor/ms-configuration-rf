package co.unicauca.edu.co.contables.configuration.thirds.domain.utils;

/**
 * @brief Constantes centralizadas para funcionalidades de importación
 *
 * Clase de utilidad que centraliza todos los valores constantes utilizados
 * en el proceso de importación de terceros, facilitando mantenimiento y reutilización.
 * Incluye extensiones de archivo, encabezados Excel, códigos de error, patrones de validación
 * y valores por defecto.
 */
public final class ImportConstants {

    private ImportConstants() {
        throw new UnsupportedOperationException("ImportConstants es una clase de utilidad y no debe ser instanciada");
    }

    // ===== CONFIGURACIÓN DE ARCHIVOS =====


    public static final String[] SUPPORTED_EXTENSIONS = {".xlsx", ".xls"};

    // ===== ENCABEZADOS DE EXCEL =====

    /**
     * @brief Encabezados requeridos para importación de terceros
     *
     * Lista de columnas obligatorias que deben estar presentes en cualquier archivo
     * Excel de importación. Estos campos son imprescindibles para crear o actualizar terceros.
     */
    public static final String[] REQUIRED_HEADERS = {
        "Tipo Identificación",
        "Número Identificación",
        "Dígito Verificación",
        "Tipo Persona",
        "Nombres",
        "Apellidos",
        "Razón Social",
        "Estado",
        "Tipos de Tercero",
        "Dirección",
        "Teléfono",
        "Email"
    };

    /**
     * @brief Encabezados opcionales para importación de terceros
     *
     * Lista de columnas que pueden estar presentes en el archivo Excel pero no son obligatorias.
     * Si están presentes, serán procesadas; si no, se omitirán sin generar errores.
     */
    public static final String[] OPTIONAL_HEADERS = {
        "Género",
        "País",
        "Departamento",
        "Ciudad"
    };


    // ===== NOMBRES DE COLUMNAS =====

   
    public static final String TYPES_COLUMN = "Tipos de Tercero";
    public static final String COUNTRY_COLUMN = "País";
    public static final String STATE_COLUMN = "Departamento";
    public static final String CITY_COLUMN = "Ciudad";
    public static final String ADDRESS_COLUMN = "Dirección";
    public static final String PHONE_COLUMN = "Teléfono";
    public static final String EMAIL_COLUMN = "Email";
    

    // ===== MENSAJES DE ERROR COMUNES =====

    /**
     * @brief Mensajes de error estándar para importación
     *
     * Clase interna que contiene mensajes de error reutilizables para operaciones
     * de importación, manteniendo consistencia en la comunicación de errores.
     */
    public static final class ErrorMessages {
        public static final String SYSTEM_ERROR = "Error del sistema durante la importación";
        
        private ErrorMessages() {}
    }

    // ===== CÓDIGOS DE ERROR =====

    /**
     * @brief Códigos de error estandarizados para importación
     *
     * Clase interna que define códigos de error únicos para categorizar
     * diferentes tipos de problemas que pueden ocurrir durante la importación.
     */
    public static final class ErrorCodes {
        
        public static final String REQUIRED_FIELD_MISSING = "REQUIRED_FIELD_MISSING";
        public static final String INVALID_EMAIL_FORMAT = "INVALID_EMAIL_FORMAT";
        public static final String INVALID_PHONE_FORMAT = "INVALID_PHONE_FORMAT";
        public static final String BUSINESS_RULE_VIOLATION = "BUSINESS_RULE_VIOLATION";
        public static final String DUPLICATE_RECORD = "DUPLICATE_RECORD";
        public static final String MISSING_CITY_FOR_COMPLETE_ADDRESS = "MISSING_CITY_FOR_COMPLETE_ADDRESS";
        public static final String SYSTEM_ERROR = "SYSTEM_ERROR";
        
        private ErrorCodes() {}
    }

    // ===== PATRONES DE VALIDACIÓN =====

    /**
     * @brief Patrones regex para validaciones comunes
     *
     * Clase interna que contiene expresiones regulares reutilizables
     * para validar formatos de datos comunes en la importación.
     */
    public static final class ValidationPatterns {
        
        public static final String EMAIL_PATTERN = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        public static final String PHONE_PATTERN = "^[+]?[0-9]{7,15}$";
        public static final String NIT_START_PATTERN = "^[89].*";
        
        private ValidationPatterns() {}
    }

    // ===== VALORES POR DEFECTO =====

    public static final class Defaults {
        public static final int COLUMN_START_INDEX = 1;
        
        private Defaults() {}
    }
}
