package co.unicauca.edu.co.contables.auxiliary.infrastructure.out.exception.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.http.ResponseEntity;

/**
 * @brief DTO para las respuestas de error.
 *
 * Representa la estructura de error devuelta al cliente e incluye
 * código HTTP, mensaje, URL de la petición y método HTTP. Provee un
 * helper {@link #of()} para construir directamente un {@link ResponseEntity}.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponseDTO {
    /**
     * @brief Código HTTP de la respuesta de error.
     */
    private Integer status;

    /**
     * @brief Mensaje descriptivo del problema.
     */
    private String message;

    /**
     * @brief URL de la petición que originó el error.
     */
    private String url;

    /**
     * @brief Método HTTP de la petición que originó el error.
     */
    private String method;

    /**
     * @brief Construye un ResponseEntity con esta respuesta de error.
     * @return ResponseEntity con la respuesta y el código HTTP apropiado.
     */
    public ResponseEntity<ErrorResponseDTO> of() {
        return ResponseEntity.status(this.status).body(this);
    }
}
