package co.unicauca.edu.co.contables.inventory.stock.infrastructure.adapters.input.rest.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.http.ResponseEntity;

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
  
    public ResponseEntity<ResponseDto<T>> of() {
        return ResponseEntity.status(this.status).body(this);
    }
}
