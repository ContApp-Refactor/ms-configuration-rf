package co.unicauca.edu.co.contables.configuration.thirds.domain.utils;

import java.util.regex.Pattern;

/**
 * @brief Utilidades de validación comunes para el sistema de terceros
 *
 * Clase de utilidad que centraliza todas las validaciones reutilizables del sistema,
 * incluyendo validaciones de formato, contenido, compatibilidad y reglas de negocio.
 * Facilita el mantenimiento y asegura consistencia en todas las validaciones.
 */
public final class ValidationUtils {

    private ValidationUtils() {
        throw new UnsupportedOperationException("ValidationUtils es una clase de utilidad y no debe ser instanciada");
    }

    private static final Pattern EMAIL_PATTERN = Pattern.compile(ImportConstants.ValidationPatterns.EMAIL_PATTERN);
    private static final Pattern PHONE_PATTERN = Pattern.compile(ImportConstants.ValidationPatterns.PHONE_PATTERN);
    private static final Pattern NIT_START_PATTERN = Pattern
            .compile(ImportConstants.ValidationPatterns.NIT_START_PATTERN);

    // ===== VALIDACIONES DE FORMATO =====

    /**
     * @brief Valida si un email tiene formato válido
     *
     * Verifica que el email cumpla con un formato básico de dirección de correo electrónico,
     * incluyendo dominio y estructura correcta.
     * @param email el email a validar
     * @return true si el formato es válido, false en caso contrario
     */
    public static boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    /**
     * @brief Valida si un número de teléfono tiene formato válido
     *
     * Acepta solo números y opcionalmente signo + al inicio.
     * La longitud debe estar entre 7 y 15 dígitos.
     * @param phone el número de teléfono a validar (ya limpio, sin espacios)
     * @return true si el formato es válido, false en caso contrario
     */
    public static boolean isValidPhoneNumber(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return false;
        }
        String cleanPhone = phone.trim();
        return cleanPhone.length() >= 7 && cleanPhone.length() <= 15 &&
                PHONE_PATTERN.matcher(cleanPhone).matches();
    }

    /**
     * @brief Valida si un NIT tiene el formato correcto para personas jurídicas
     *
     * El NIT debe empezar por 8 o 9 según regulaciones colombianas.
     * Solo valida el formato inicial, no el dígito de verificación.
     * 
     * @param nit el número NIT a validar
     * @return true si el formato es válido, false en caso contrario
     */
    public static boolean isValidNitFormat(String nit) {
        if (nit == null || nit.trim().isEmpty()) {
            return false;
        }
        return NIT_START_PATTERN.matcher(nit.trim()).matches();
    }

    // ===== VALIDACIONES DE CONTENIDO =====

    /**
     * @brief Verifica si una cadena no está vacía ni es solo espacios en blanco
     *
     * Valida que la cadena tenga contenido significativo, no siendo null,
     * vacía o compuesta únicamente de espacios en blanco.
     * @param value la cadena a verificar
     * @return true si tiene contenido válido, false en caso contrario
     */
    public static boolean hasContent(String value) {
        return value != null && !value.trim().isEmpty();
    }

    /**
     * @brief Verifica si una cadena tiene una longitud específica
     *
     * Compara la longitud de la cadena (sin espacios en blanco) con un valor esperado.
     * @param value la cadena a verificar
     * @param expectedLength la longitud esperada
     * @return true si la longitud coincide, false en caso contrario
     */
    public static boolean hasLength(String value, int expectedLength) {
        return value != null && value.trim().length() == expectedLength;
    }

    /**
     * @brief Verifica si un valor tiene una longitud válida dentro de un rango
     *
     * Valida que la longitud de la cadena esté dentro de los límites mínimo y máximo especificados.
     * @param value el valor a validar
     * @param minLength longitud mínima permitida
     * @param maxLength longitud máxima permitida
     * @return true si la longitud está dentro del rango, false en caso contrario
     */
    public static boolean isValidLength(String value, int minLength, int maxLength) {
        if (value == null) {
            return false;
        }
        int length = value.trim().length();
        return length >= minLength && length <= maxLength;
    }

    /**
     * @brief Valida que el dígito de verificación sea un solo dígito (0-9)
     *
     * Verifica que el dígito de verificación sea null (opcional) o un número
     * entre 0 y 9 (un solo dígito).
     * @param verificationNumber el número de verificación a validar
     * @return true si es válido (null o un dígito 0-9), false en caso contrario
     */
    public static boolean isValidVerificationDigit(Long verificationNumber) {
        if (verificationNumber == null) {
            return true; // null es válido (campo opcional)
        }

        // Debe ser un número entre 0 y 9 (un solo dígito)
        return verificationNumber >= 0 && verificationNumber <= 9;
    }

    /**
     * @brief Valida que el NIT tenga exactamente 9 dígitos
     *
     * Verifica que el número NIT tenga exactamente 9 dígitos,
     * según el formato estándar colombiano.
     * @param nitNumber el número de NIT a validar
     * @return true si tiene exactamente 9 dígitos, false en caso contrario
     */
    public static boolean isValidNitLength(Long nitNumber) {
        if (nitNumber == null) {
            return false;
        }

        String nitString = nitNumber.toString();
        return nitString.length() == 9;
    }

    // ===== VALIDACIONES DE COMPATIBILIDAD ======

    /**
     * @brief Verifica si un mensaje de excepción indica un error de duplicado
     *
     * Analiza el mensaje de error para determinar si representa una violación
     * de unicidad o duplicación de datos en la base de datos.
     * @param errorMessage el mensaje de error a analizar
     * @return true si es un error de duplicado, false en caso contrario
     */
    public static boolean isDuplicateError(String errorMessage) {
        if (errorMessage == null) {
            return false;
        }

        String lowerMessage = errorMessage.toLowerCase();
        return lowerMessage.contains("ya existe") ||
                lowerMessage.contains("duplicate") ||
                lowerMessage.contains("duplicado") ||
                lowerMessage.contains("unique constraint") ||
                (lowerMessage.contains("número de identificación") && lowerMessage.contains("existe"));
    }

    /**
     * @brief Verifica si un mensaje de excepción indica un error geográfico
     *
     * Analiza el mensaje de error para determinar si está relacionado con
     * validaciones de datos geográficos (país, estado, ciudad).
     * @param errorMessage el mensaje de error a analizar
     * @return true si es un error geográfico, false en caso contrario
     */
    public static boolean isGeographyError(String errorMessage) {
        if (errorMessage == null) {
            return false;
        }

        String lowerMessage = errorMessage.toLowerCase();
        return lowerMessage.contains("código del país") ||
                lowerMessage.contains("código del estado") ||
                lowerMessage.contains("código de la ciudad") ||
                lowerMessage.contains("código del departamento") ||
                (lowerMessage.contains("obligatorio") && (lowerMessage.contains("país") ||
                        lowerMessage.contains("estado") ||
                        lowerMessage.contains("ciudad") ||
                        lowerMessage.contains("departamento")));
    }

    // ===== VALIDACIONES DE NEGOCIO =====

    /**
     * @brief Detecta el campo específico basado en un mensaje de error de regla de negocio
     *
     * Analiza el mensaje de error para identificar qué campo específico violó
     * una regla de negocio, permitiendo una categorización más precisa de los errores.
     * @param errorMessage el mensaje de error a analizar
     * @return el nombre del campo detectado o un valor genérico si no se identifica
     */
    public static String detectFieldFromBusinessRuleError(String errorMessage) {
        if (errorMessage == null) {
            return "Reglas Negocio";
        }

        String lowerMessage = errorMessage.toLowerCase();

        // Casos específicos primero (más específicos)
        if (lowerMessage.contains("dígito de verificación") || lowerMessage.contains("digito de verificacion")) {
            return "Dígito Verificación";
        } else if (lowerMessage.contains("nit")) {
            return "Número Identificación";
        } else if (lowerMessage.contains("no se permite el campo género")) {
            return "Género";
        } else if (lowerMessage.contains("no se permite el campo apellidos")) {
            return "Apellidos";
        } else if (lowerMessage.contains("no se permite el campo nombres")) {
            return "Nombres";
        } else if (lowerMessage.contains("género")) {
            return "Género";
        } else if (lowerMessage.contains("apellidos")) {
            return "Apellidos";
        } else if (lowerMessage.contains("razón social")) {
            return "Razón Social";
        } else if (lowerMessage.contains("nombres")) {
            return "Nombres";
        } else if (lowerMessage.contains("tipo de persona") ||
                lowerMessage.contains("persona natural") ||
                lowerMessage.contains("persona jurídica")) {
            return "Tipo Persona";
        } else if (lowerMessage.contains("tipo de identificación") ||
                lowerMessage.contains("identificación")) {
            return "Tipo Identificación";
        } else {
            return "Reglas Negocio";
        }
    }

    // ===== NORMALIZACIONES =====

    /**
     * @brief Normaliza un texto para comparaciones case-insensitive
     *
     * Convierte el texto a mayúsculas y elimina espacios en blanco al inicio y final
     * para facilitar comparaciones insensibles a mayúsculas/minúsculas.
     * @param text el texto a normalizar
     * @return el texto normalizado o null si el input es null
     */
    public static String normalizeForComparison(String text) {
        return text != null ? text.trim().toUpperCase() : null;
    }

    /**
     * @brief Limpia un número telefónico removiendo caracteres no numéricos excepto +
     *
     * Elimina todos los caracteres no numéricos del teléfono, preservando solo
     * números y el signo + al inicio para formato internacional.
     * @param phone el número a limpiar
     * @return el número limpio o null si el input es null
     */
    public static String cleanPhoneNumber(String phone) {
        if (phone == null) {
            return null;
        }
        return phone.replaceAll("[^+0-9]", "");
    }

    // ===== CONVERSIONES DE PARÁMETROS =====

    /**
     * @brief Convierte un parámetro String a Boolean
     *
     * Maneja strings vacíos y null como null. Útil para parámetros opcionales
     * de endpoints REST donde el valor puede no estar presente.
     * @param value el valor del parámetro como String
     * @return Boolean o null si el valor es null o vacío
     */
    public static Boolean parseOptionalBoolean(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return Boolean.parseBoolean(value);
    }
}
