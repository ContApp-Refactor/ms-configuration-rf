package co.unicauca.edu.co.contables.audit.domain.enums;

public enum OperationType {
    CREATE("Creación"),
    UPDATE("Actualización"),
    DELETE("Eliminación"),
    ACTIVATE("Activación"),
    INACTIVATE("Inactivación");

    private final String description;

    OperationType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
