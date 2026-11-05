package co.unicauca.edu.co.contables.inventory.stock.infrastructure.adapters.output.exception.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.http.ResponseEntity;


@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponseDto {
    private Integer status;
    private String message;
    private String url;
    private String method;

    public ResponseEntity<ErrorResponseDto> of() {
        return ResponseEntity.status(this.status).body(this);
    }
}
