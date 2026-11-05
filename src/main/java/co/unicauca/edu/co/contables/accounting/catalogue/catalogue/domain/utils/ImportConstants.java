package co.unicauca.edu.co.contables.accounting.catalogue.catalogue.domain.utils;

/**
 * @brief Constantes centralizadas para proceso de importación Excel
 *
 * Centraliza todos los valores constantes utilizados en el proceso de importación
 * de catálogo de cuentas para facilitar mantenimiento y reutilización.
 */
public final class ImportConstants {

    private ImportConstants() {
        throw new UnsupportedOperationException("ImportConstants es una clase de utilidad y no debe ser instanciada");
    }
    
    public static final String[] SUPPORTED_EXTENSIONS = {".xlsx", ".xls"};
    public static final long MAX_FILE_SIZE = 5242880L; // 5 * 1024 * 1024

    public static final String[] REQUIRED_HEADERS = {
        "Código",
        "Nombre",
        "Naturaleza",
        "Estado Financiero",
        "Clasificación"
    };

    public static final String[] OPTIONAL_HEADERS = {
        "Cruce",
        "Centro de Costo"
    };
    
       
    public static final String CODE_COLUMN = "Código";
    public static final String NAME_COLUMN = "Nombre";
    public static final String NATURE_COLUMN = "Naturaleza";
    public static final String FINANCIAL_STATUS_COLUMN = "Estado Financiero";
    public static final String CLASSIFICATION_COLUMN = "Clasificación";
    public static final String CROSSING_COLUMN = "Cruce";
    public static final String COST_CENTER_COLUMN = "Centro de Costo";
    
    public static final String[] BOOLEAN_TRUE_VALUES = {"SI", "SÍ", "S", "TRUE", "1"};
    public static final String[] BOOLEAN_FALSE_VALUES = {"NO", "N", "FALSE", "0"};

    /**
     * @brief Códigos de error estandarizados para importación.
     */
    public static final class ErrorCodes {
        public static final String REQUIRED_FIELD_MISSING = "REQUIRED_FIELD_MISSING";
        public static final String INVALID_CODE_FORMAT = "INVALID_CODE_FORMAT";
        public static final String INVALID_CODE_LENGTH = "INVALID_CODE_LENGTH";
        public static final String INVALID_ENUM_VALUE = "INVALID_ENUM_VALUE";
        public static final String INVALID_BOOLEAN_VALUE = "INVALID_BOOLEAN_VALUE";
        public static final String BUSINESS_RULE_VIOLATION = "BUSINESS_RULE_VIOLATION";
        public static final String ORPHAN_ACCOUNT = "ORPHAN_ACCOUNT";
        public static final String PARENT_NOT_FOUND = "PARENT_NOT_FOUND";
        public static final String SYSTEM_ERROR = "SYSTEM_ERROR";
        
        private ErrorCodes() {}
    }

    /**
     * @brief Mensajes de error estándar para importación.
     */    
    public static final class ErrorMessages {
        public static final String SYSTEM_ERROR = "Error del sistema durante la importación";
        public static final String EMPTY_FILE = "El archivo no contiene datos para importar";
        public static final String INVALID_HEADERS = "El archivo no contiene los encabezados requeridos";
        
        private ErrorMessages() {}
    }

   
    /**
     * @brief Valores por defecto para configuraciones.
     */
    public static final class Defaults {
        public static final int COLUMN_START_INDEX = 1;
        public static final int BATCH_SIZE = 1000;
        public static final boolean SKIP_DUPLICATES = true;
        public static final boolean CONTINUE_ON_ERROR = true;
        
        private Defaults() {}
    }
}

