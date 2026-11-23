package co.unicauca.edu.co.contables.audit.infrastructure.adapters.output.exception.dto;

import java.time.ZonedDateTime;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponseDto {

    private ZonedDateTime timestamp;
    private int status;
    private String error;
    private String message;
    private String path;
    
    // Errores de validación específicos por campo
    private Map<String, String> validationErrors;
    
}
