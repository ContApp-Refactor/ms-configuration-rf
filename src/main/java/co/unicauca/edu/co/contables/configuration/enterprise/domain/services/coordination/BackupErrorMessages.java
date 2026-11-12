package co.unicauca.edu.co.contables.configuration.enterprise.domain.services.coordination;

import java.sql.SQLException;

/**
 * @brief Textos de error del restore: el técnico (con la causa real de la BD,
 * para el log) y el legible (en español, para el front).
 */
public final class BackupErrorMessages {

    private BackupErrorMessages() {
    }

    /**
     * Mensaje técnico con la causa real de una violación de integridad. El texto
     * orientado a ubicación/catálogos solo se usa cuando la causa es de verdad
     * una llave foránea (SQLState 23503 o mensaje "foreign key").
     */
    public static String integrityMessage(Throwable e) {
        Throwable root = rootCause(e);
        String cause = root != null && root.getMessage() != null ? root.getMessage() : e.getMessage();
        if (isForeignKey(e, cause)) {
            return "El restore violó una llave foránea; revise los ids de ciudad/departamento/país y "
                    + "catálogos (responsabilidad tributaria, tipo de contribuyente, tipo de empresa) "
                    + "contenidos en el backup. Causa: " + cause;
        }
        return "El restore violó una restricción de integridad de la base de datos. Causa: " + cause;
    }

    /** Mensaje para el usuario (sin texto de la BD). */
    public static String integrityUserMessage(String moduleKey, Throwable e) {
        Throwable root = rootCause(e);
        String cause = root != null && root.getMessage() != null ? root.getMessage() : e.getMessage();
        String where = moduleKey == null ? "la copia" : "el módulo '" + moduleKey + "'";
        if (isForeignKey(e, cause)) {
            return "No se pudo restaurar " + where + ": la copia referencia datos que no existen en este "
                    + "entorno (ciudad, departamento, país u otro catálogo).";
        }
        return "No se pudo restaurar " + where + ": la copia contiene datos que la base de datos rechazó "
                + "(registro duplicado o dato inválido).";
    }

    private static boolean isForeignKey(Throwable e, String cause) {
        for (Throwable t = e; t != null; t = t.getCause() == t ? null : t.getCause()) {
            if (t instanceof SQLException sql && "23503".equals(sql.getSQLState())) {
                return true;
            }
        }
        return cause != null && cause.toLowerCase().contains("foreign key");
    }

    private static Throwable rootCause(Throwable e) {
        Throwable root = e;
        while (root != null && root.getCause() != null && root.getCause() != root) {
            root = root.getCause();
        }
        return root;
    }
}
