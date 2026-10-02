package co.unicauca.edu.co.contables.inventory.products.infraestructure.utils;

import java.util.Optional;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

/**
 * @brief Utilidad para gestión flexible de paginación Spring Data
 *
 * Proporciona métodos estáticos para crear objetos Pageable basados en parámetros opcionales
 * y maneja conversiones seguras de long a int para evitar overflow.
 */
public class PaginationHelper {

    private PaginationHelper() {
        throw new UnsupportedOperationException("PaginationHelper es una clase de utilidad y no debe ser instanciada");
    }

    /**
     * @brief Crea Pageable flexible con parámetros opcionales
     * @param numPage número de página opcional
     * @param size tamaño de página opcional
     * @param totalRecords total de registros para cálculo de página completa
     * @return Pageable configurado o página completa si no hay parámetros
     */
    public static Pageable createFlexiblePageable(Optional<Integer> numPage, 
                                                   Optional<Integer> size, 
                                                   long totalRecords) {
        if (numPage.isEmpty() || size.isEmpty()) {
            // Si no se especifican parámetros de paginación, traer todos los registros
            int safeSize = safeIntCast(totalRecords);
            return PageRequest.of(0, Math.max(1, safeSize));
        } else {
            // Usar los parámetros especificados
            return PageRequest.of(numPage.get(), size.get());
        }
    }

    /**
     * @brief Convierte long a int de forma segura evitando overflow
     * @param value valor long a convertir
     * @return valor int seguro o Integer.MAX_VALUE si excede rango
     */
    private static int safeIntCast(long value) {
        if (value > Integer.MAX_VALUE) {
            return Integer.MAX_VALUE;
        }
        return (int) value;
    }
}
