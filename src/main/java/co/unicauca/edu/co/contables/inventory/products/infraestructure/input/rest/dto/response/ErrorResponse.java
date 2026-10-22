package co.unicauca.edu.co.contables.inventory.products.infraestructure.input.rest.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * @brief DTO de respuesta para errores de la API REST
 *
 * Estructura estandarizada para comunicar errores ocurridos en la API,
 * incluyendo códigos HTTP, mensajes descriptivos y contexto de la solicitud.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {

    private LocalDateTime timestamp;
    private int status;
    private String error;
    private String message;
    private String code;
    private String path;
}