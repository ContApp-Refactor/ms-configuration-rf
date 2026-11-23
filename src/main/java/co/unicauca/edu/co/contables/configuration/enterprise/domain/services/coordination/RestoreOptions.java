package co.unicauca.edu.co.contables.configuration.enterprise.domain.services.coordination;

import java.util.UUID;

import co.unicauca.edu.co.contables.commons.backup.RestoreMode;

/**
 * @brief Qué pidió el usuario para un restore del coordinador: crear una
 * empresa nueva ({@link RestoreMode#NEW_ENTERPRISE}/{@link RestoreMode#DUPLICATE})
 * o restaurar sobre una existente ({@link RestoreMode#IN_PLACE}, D4 actualizado).
 *
 * @param mode               modo de restore
 * @param targetName         nombre de la empresa; {@code null} = el de la copia
 *                           (en DUPLICATE, {@code "<nombre> (copia)"})
 * @param targetNit          NIT de la empresa; {@code null} = el de la copia (D3)
 * @param skipUnknownModules ignorar módulos del ZIP sin participante en vez de fallar
 * @param targetEnterpriseId empresa existente a sobrescribir (solo IN_PLACE; null en los demás modos)
 */
public record RestoreOptions(RestoreMode mode, String targetName, String targetNit, boolean skipUnknownModules,
        UUID targetEnterpriseId) {

    public RestoreOptions {
        if (mode == null) {
            throw new IllegalArgumentException("mode es obligatorio");
        }
        targetName = blankToNull(targetName);
        targetNit = blankToNull(targetNit);
    }

    /** Constructor para los modos que crean empresa (sin empresa destino previa). */
    public RestoreOptions(RestoreMode mode, String targetName, String targetNit, boolean skipUnknownModules) {
        this(mode, targetName, targetNit, skipUnknownModules, null);
    }

    public static RestoreOptions newEnterprise(String targetName, String targetNit, boolean skipUnknownModules) {
        return new RestoreOptions(RestoreMode.NEW_ENTERPRISE, targetName, targetNit, skipUnknownModules, null);
    }

    public static RestoreOptions duplicate(String targetName) {
        return new RestoreOptions(RestoreMode.DUPLICATE, targetName, null, false, null);
    }

    /** Restaurar sobre {@code targetEnterpriseId} (purga + restore). Nombre/NIT null = los de la copia. */
    public static RestoreOptions inPlace(UUID targetEnterpriseId, String targetName, String targetNit,
            boolean skipUnknownModules) {
        return new RestoreOptions(RestoreMode.IN_PLACE, targetName, targetNit, skipUnknownModules,
                targetEnterpriseId);
    }

    private static String blankToNull(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }
}
