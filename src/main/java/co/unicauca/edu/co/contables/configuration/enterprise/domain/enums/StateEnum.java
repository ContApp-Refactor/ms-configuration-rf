package co.unicauca.edu.co.contables.configuration.enterprise.domain.enums;

import lombok.Getter;

/**
 * @brief Estados posibles de una empresa
 */
@Getter
public enum StateEnum {
    ACTIVE("Activo"),
    INACTIVE("Inactivo"),
    SUSPENDED("Suspendido");

    private final String state;

    StateEnum(String state) {
        this.state = state;
    }
}
