package co.unicauca.edu.co.contables.configuration.commons.exceptions;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * @brief Modelo de respuesta estándar para errores
 *
 * Modelo de respuesta estándar para errores en la API REST.
 * Proporciona información consistente sobre errores ocurridos.
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