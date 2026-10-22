package co.unicauca.edu.co.contables.inventory.kardex.infrastructure.adapters.input.rest.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.http.ResponseEntity;

/**
 * @brief Generic response wrapper for REST API endpoints
 * 
 * Provides standardized response format with data payload,
 * status code, and message for consistent API responses.
 * 
 * @param <T> Type of the data payload
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResponseDto<T> {
    private T data;
    private Integer status;
    private String message;
  
    /**
     * @brief Creates ResponseEntity with this DTO as body and status
     * @return ResponseEntity with proper HTTP status and response body
     */
    public ResponseEntity<ResponseDto<T>> of() {
        return ResponseEntity.status(this.status).body(this);
    }
}
