package co.unicauca.edu.co.contables.configuration.enterprise.domain.enums;

import lombok.Getter;

/**
 * Enumeración que define los posibles estados de una empresa en el sistema.
 * Proporciona una lista controlada de estados válidos junto con sus
 * representaciones en texto.
 *
 * @author CONTAPP
 * @version 1.0
 * @since 1.0.0
 */
@Getter
public enum StateEnum {
    /**
     * Estado que indica que la empresa está operativa y funcionando normalmente.
     */
    ACTIVE("Activo"),

    /**
     * Estado que indica que la empresa no está operativa actualmente.
     */
    INACTIVE("Inactivo"),

    /**
     * Estado que indica que la empresa está temporalmente suspendida.
     */
    SUSPENDED("Suspendido");

    /**
     * Representación en texto del estado.
     */
    private final String state;

    /**
     * Constructor del enum.
     *
     * @param state Texto que representa el estado
     */
    StateEnum(String state) {
        this.state = state;
    }
}

