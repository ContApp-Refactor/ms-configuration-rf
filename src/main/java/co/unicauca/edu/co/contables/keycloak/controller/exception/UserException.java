package co.unicauca.edu.co.contables.keycloak.controller.exception;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
public class UserException extends RuntimeException {
    @Schema(description = "Estado", example = "400")
    private int status;

    public UserException(String message, int status) {
        super(message);
        this.status = status;
    }
}
