package co.unicauca.edu.co.contables.configuration.thirds.infrastructure.adapters.output.persistence.messageBroker.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ThirdUpdatedEventDto {
    private Long thirdId;       // El thId
    private String entId;        // El entId
    private String fullName;    // El nombre consolidado
    private String email;
    private Boolean state;      // Importante para saber si está activo
}
