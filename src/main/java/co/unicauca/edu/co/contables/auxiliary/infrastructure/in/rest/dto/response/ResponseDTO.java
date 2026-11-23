package co.unicauca.edu.co.contables.auxiliary.infrastructure.in.rest.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.ResponseEntity;

/**
 * @brief Envoltorio genérico de respuesta REST.
 *
 * Encapsula el resultado de una operación con un payload genérico,
 * el código HTTP y un mensaje descriptivo. Provee el método {@link #of()}
 * como helper para construir un {@link ResponseEntity} con el código
 * HTTP correspondiente.
 *
 * @param <T> tipo de dato contenido en la respuesta.
 */
@Data
@AllArgsConstructor @NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResponseDTO<T> {
    private T data;
    private Integer statusCode;
    private String message;

    public ResponseEntity<ResponseDTO<T>> of() {
        return ResponseEntity.status(this.statusCode).body(this);
    }
}
