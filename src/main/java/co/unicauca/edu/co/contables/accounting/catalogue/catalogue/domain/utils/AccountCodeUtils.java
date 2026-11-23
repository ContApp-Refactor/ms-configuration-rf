package co.unicauca.edu.co.contables.accounting.catalogue.catalogue.domain.utils;

import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.domain.models.AccountCatalogueExcelData;

import java.util.Comparator;
import java.util.List;

/**
 * @brief Utilidades para manejo de códigos jerárquicos de cuentas contables
 *
 * Proporciona métodos para extraer códigos padre, validar formatos,
 * ordenar por jerarquía y determinar tipos de cuenta.
 */
public final class AccountCodeUtils {

    private AccountCodeUtils() {
        throw new UnsupportedOperationException("AccountCodeUtils es una clase de utilidad y no debe ser instanciada");
    }

    /**
     * @brief Extrae código padre de código cuenta siguiendo jerarquía numérica
     *
     * Ejemplos: 1105 → 110, 110501 → 11050, 11050101 → 1105010, 11 → 1, 1 → null
     * @param code código de la cuenta hija
     * @return código del padre o null si es cuenta raíz
     */
    public static String extractParentCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            return null;
        }
        
        String trimmedCode = code.trim();
        int length = trimmedCode.length();
        
        // Si tiene solo 1 dígito, es raíz y no tiene padre
        if (length == 1) {
            return null;
        }
        
        // Determinar longitud del padre según la longitud actual
        int parentLength;
        if (length == 2) {
            parentLength = 1;
        } else if (length == 4) {
            parentLength = 2;
        } else if (length == 6) {
            parentLength = 4;
        } else if (length == 8) {
            parentLength = 6;
        } else {
            // Para códigos de longitud no estándar, retornar null
            return null;
        }
        
        return trimmedCode.substring(0, parentLength);
    }

    /**
     * @brief Determina nivel jerárquico basado en longitud del código
     *
     * 1 dígito = nivel 1, 2 dígitos = nivel 2, 4 dígitos = nivel 3,
     * 6 dígitos = nivel 4, 8 dígitos = nivel 5
     * @param code código de cuenta a evaluar
     * @return nivel jerárquico (1-5) o 0 si inválido
     */
    public static int getHierarchyLevel(String code) {
        if (code == null || code.trim().isEmpty()) {
            return 0;
        }
        
        int length = code.trim().length();
        return switch (length) {
            case 1 -> 1;
            case 2 -> 2;
            case 4 -> 3;
            case 6 -> 4;
            case 8 -> 5;
            default -> 0;
        };
    }

    /**
     * @brief Valida si longitud del código cumple con jerarquía contable
     *
     * Longitudes válidas: 1, 2, 4, 6 u 8 dígitos
     * @param code código a validar
     * @return true si longitud es válida para jerarquía contable
     */
    public static boolean isValidCodeLength(String code) {
        if (code == null || code.trim().isEmpty()) {
            return false;
        }
        
        int length = code.trim().length();
        return length == 1 || length == 2 || length == 4 || length == 6 || length == 8;
    }

    /**
     * @brief Ordena cuentas por jerarquía numérica para procesamiento secuencial
     *
     * Garantiza que padres se procesen antes que hijos siguiendo orden natural.
     * Ejemplo: 1, 11, 110, 1105, 110501, 11050101, 12, 1205, ...
     * @param accounts lista de cuentas a ordenar
     * @return lista ordenada por jerarquía (padres primero)
     */
    public static List<AccountCatalogueExcelData> sortByHierarchy(List<AccountCatalogueExcelData> accounts) {
        if (accounts == null || accounts.isEmpty()) {
            return accounts;
        }
        
        // Ordenar por código numéricamente (como entero) para mantener orden jerárquico correcto
        accounts.sort(Comparator.comparingLong(account -> {
            try {
                return Long.parseLong(account.getCode().trim());
            } catch (NumberFormatException e) {
                // Si no es numérico, usar 0 para que vaya al inicio
                return 0L;
            }
        }));
        
        return accounts;
    }

    /**
     * @brief Determina si cuenta es raíz de jerarquía (1 dígito)
     * @param code código de cuenta a evaluar
     * @return true si código tiene exactamente 1 dígito (cuenta raíz)
     */
    public static boolean isRootAccount(String code) {
        return code != null && code.trim().length() == 1;
    }

    /**
     * @brief Determina si cuenta es auxiliar (hoja de jerarquía - 8 dígitos)
     * @param code código de cuenta a evaluar
     * @return true si código tiene exactamente 8 dígitos (cuenta auxiliar)
     */
    public static boolean isAuxiliaryAccount(String code) {
        return code != null && code.trim().length() == 8;
    }
}

