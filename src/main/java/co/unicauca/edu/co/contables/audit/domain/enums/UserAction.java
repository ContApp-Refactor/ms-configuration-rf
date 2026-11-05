package co.unicauca.edu.co.contables.audit.domain.enums;

public enum UserAction {
    LOGIN("Inicio de sesión"),
    LOGOUT("Cierre de sesión");

    private final String description;

    UserAction(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
