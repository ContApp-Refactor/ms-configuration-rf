package co.unicauca.edu.co.contables.keycloak.dtos;

import co.unicauca.edu.co.contables.keycloak.validation.ISanitize;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class AuthDTO {

    @NotBlank(message = "Username es obligatorio")
    @ISanitize
    String username;

    @NotBlank(message = "Password es obligatorio")
    String password;
}
