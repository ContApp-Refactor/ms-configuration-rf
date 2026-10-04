package co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.output.exception.dto;

import org.springframework.http.ResponseEntity;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @brief Data Transfer Object for error responses
 * 
 * Standardizes the structure of error responses returned by the API,
 * including status, message, and request details.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponseDTO {
    private Integer status;
    private String message;
    private String url;
    private String method;

    /**
     * @brief Converts the DTO into a ResponseEntity
     * @return ResponseEntity containing this DTO with the appropriate status code
     */
    public ResponseEntity<ErrorResponseDTO> of() {
        return ResponseEntity.status(this.status).body(this);
    }
}
