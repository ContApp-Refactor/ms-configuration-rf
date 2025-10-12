package co.unicauca.edu.co.contables.configuration.commons.utils;

import java.util.Optional;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

/**
 * Utilidad para manejar la lógica de paginación flexible.
 * Proporciona métodos para crear objetos Pageable basados en parámetros opcionales
 * y maneja de forma segura la conversión de long a int.
 */
@Component
public class PaginationHelper {

    /**
     * Crea un objeto Pageable flexible basado en parámetros opcionales.
     * Si no se especifican parámetros de paginación, crea una página que contiene todos los registros.
     * 
     * @param numPage Número de página opcional
     * @param size Tamaño de página opcional
     * @param totalRecords Total de registros disponibles
     * @return Objeto Pageable configurado
     */
    public Pageable createFlexiblePageable(Optional<Integer> numPage, 
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
     * Convierte de forma segura un long a int, evitando overflow.
     * Si el valor excede el rango de int, retorna Integer.MAX_VALUE.
     * 
     * @param value Valor long a convertir
     * @return Valor int seguro
     */
    private int safeIntCast(long value) {
        if (value > Integer.MAX_VALUE) {
            return Integer.MAX_VALUE;
        }
        return (int) value;
    }
}
