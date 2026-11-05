package co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.input.rest.dto;

import org.springframework.http.ResponseEntity;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @brief Generic response wrapper for REST APIs
 * 
 * Standardizes the structure of HTTP responses, including data, status code, and message.
 * @param <T> The type of the data payload
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResponseDTO<T> {
    private T data;
    private Integer status;
    private String message;

    /**
     * @brief Converts the DTO to a ResponseEntity
     * @return ResponseEntity containing this DTO
     */
    public ResponseEntity<ResponseDTO<T>> of() {
        return ResponseEntity.status(this.status).body(this);
    }
    
}
