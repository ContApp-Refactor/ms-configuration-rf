package co.unicauca.edu.co.contables.keycloak.audit;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import co.unicauca.edu.co.contables.commons.audit.annotation.Auditable;
import co.unicauca.edu.co.contables.commons.audit.annotation.OperationType;
import co.unicauca.edu.co.contables.commons.audit.aspect.BaseAuditAspect;
import co.unicauca.edu.co.contables.commons.audit.builder.AuditEventBuilder;
import co.unicauca.edu.co.contables.commons.audit.publisher.AuditEventPublisher;
import co.unicauca.edu.co.contables.keycloak.dtos.ProfileDTO;
import co.unicauca.edu.co.contables.keycloak.dtos.UserDTO;
import co.unicauca.edu.co.contables.keycloak.service.IProfileKeycloakService;
import co.unicauca.edu.co.contables.keycloak.service.IUserKeycloakService;

import lombok.extern.slf4j.Slf4j;

/**
 * Aspecto único de auditoría del módulo keycloak (D-12.3).
 * <p>
 * Sustituye a los cuatro aspectos del origen
 * ({@code AuditAspectUsers}, {@code AuditAspectProfiles},
 * {@code AuditAspectPermissions} y su {@code BaseAuditAspect}) con un solo
 * aspecto que sigue el precedente del monolito
 * ({@code AuditAspectClassesDocuments}): extiende el
 * {@code BaseAuditAspect} de commons y se apoya en
 * {@code AuditEventBuilder} / {@code AuditEventPublisher} ya existentes.
 * </p>
 * <p>
 * Diferencias respecto del origen, todas forzadas por la base común:
 * </p>
 * <ul>
 * <li>El {@code enterpriseId} no existe en Keycloak (no hay entidad con
 * enterprise); se resuelve como {@code "SYSTEM"}.</li>
 * <li>El {@code moduleName} de la anotación es {@code "KEYCLOAK"}.</li>
 * <li>El {@code dataObject} de PERMISSION no usa el
 * {@code buildSpecialDataObject} del origen (no existe en commons): se
 * sobrecribe {@link #buildDataObject} para producir el mismo mapa
 * {@code {roleName, permissions}}.</li>
 * </ul>
 */
@Aspect
@Component
@Slf4j
public class KeycloakAuditAspect extends BaseAuditAspect {

    /** Roles técnicos de Keycloak que no interesan auditar. */
    private static final Set<String> TECHNICAL_ROLES = Set.of(
            "offline_access",
            "uma_authorization");

    @Lazy
    private final IUserKeycloakService userKeycloakService;

    @Lazy
    private final IProfileKeycloakService profileKeycloakService;

    public KeycloakAuditAspect(AuditEventBuilder auditEventBuilder,
            @Lazy AuditEventPublisher auditEventPublisher,
            @Lazy IUserKeycloakService userKeycloakService,
            @Lazy IProfileKeycloakService profileKeycloakService) {
        super(auditEventBuilder, auditEventPublisher);
        this.userKeycloakService = userKeycloakService;
        this.profileKeycloakService = profileKeycloakService;
    }

    /**
     * Punto de corte: cualquier método anotado con {@link Auditable} dentro
     * de los servicios del módulo keycloak.
     *
     * @param joinPoint punto de unión.
     * @param auditable metadatos de la anotación.
     * @return resultado original del método interceptado.
     * @throws Throwable si el método lanza una excepción.
     */
    @Around("@annotation(auditable) && within(co.unicauca.edu.co.contables.keycloak.service..*)")
    public Object audit(ProceedingJoinPoint joinPoint, Auditable auditable) throws Throwable {
        return executeAudit(joinPoint, auditable);
    }

    /**
     * Obtiene el estado previo de la entidad en Keycloak (solo UPDATE/DELETE).
     *
     * @param auditable metadatos de la anotación.
     * @param args      argumentos del método interceptado.
     * @return mapa con el estado previo, vacío si no aplica.
     */
    @Override
    protected Map<String, Object> fetchCurrentState(Auditable auditable, Object[] args) {
        return switch (auditable.affectedTable()) {
            case "USER" -> {
                try {
                    String userId = (String) args[0];
                    yield entityToMap(userKeycloakService.findUserById(userId));
                } catch (Exception e) {
                    log.warn("No se pudo obtener estado previo del usuario: {}", e.getMessage());
                    yield Map.of();
                }
            }
            case "PROFILE" -> {
                if (auditable.operationType() == OperationType.CREATE) {
                    yield Map.of();
                }
                try {
                    String profileId = (String) args[0];
                    yield entityToMap(profileKeycloakService.findProfileById(profileId));
                } catch (Exception e) {
                    log.warn("No se pudo obtener estado previo del perfil: {}", e.getMessage());
                    yield Map.of();
                }
            }
            // PERMISSION no tiene entidad única: el servicio calcula internamente
            // los permisos a agregar/quitar, como en el origen.
            default -> Map.of();
        };
    }

    /**
     * Serializa {@link UserDTO} / {@link ProfileDTO} a un mapa plano.
     * La contraseña nunca se incluye.
     *
     * @param object entidad a serializar.
     * @return mapa plano, o vacío si el tipo no coincide.
     */
    @Override
    protected Map<String, Object> entityToMap(Object object) {
        if (object instanceof UserDTO user) {
            List<String> cleanRoles = user.getRoles() == null ? List.of()
                    : user.getRoles().stream()
                            .filter(Objects::nonNull)
                            .filter(role -> !TECHNICAL_ROLES.contains(role))
                            .filter(role -> !role.startsWith("default-roles-"))
                            .distinct()
                            .sorted()
                            .toList();

            Map<String, Object> map = new LinkedHashMap<>();
            map.put("email", user.getEmail());
            map.put("firstName", user.getFirstName());
            map.put("lastName", user.getLastName());
            map.put("roles", cleanRoles);
            map.entrySet().removeIf(entry -> entry.getValue() == null);
            return map;
        }
        if (object instanceof ProfileDTO profile) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("name", profile.getName());
            map.put("description", profile.getDescription());
            map.entrySet().removeIf(entry -> entry.getValue() == null);
            return map;
        }
        return Map.of();
    }

    /**
     * Keycloak no tiene el concepto de empresa del monolito: el identificador
     * de empresa se resuelve como {@code "SYSTEM"} en todos los eventos.
     */
    @Override
    protected String resolveEnterpriseId(Auditable auditable, Object[] args, Object result,
            Map<String, Object> beforeData) {
        return "SYSTEM";
    }

    /**
     * Resuelve el registro afectado: para CREATE se toma del resultado
     * (UserDTO/ProfileDTO); para UPDATE/DELETE de {@code args[0]}; para
     * PERMISSION, del nombre de rol ({@code args[1]}), como en el origen.
     */
    @Override
    protected String resolveRegisterId(Auditable auditable, Object[] args, Object result,
            Map<String, Object> beforeData) {
        return switch (auditable.operationType()) {
            case CREATE -> {
                if (result instanceof UserDTO user && user.getId() != null) {
                    yield user.getId();
                }
                if (result instanceof ProfileDTO profile && profile.getId() != null) {
                    yield profile.getId();
                }
                yield args != null && args.length > 1 && args[1] instanceof String roleName
                        ? roleName
                        : "UNKNOWN";
            }
            case UPDATE, DELETE -> args != null && args.length > 0 && args[0] instanceof String id
                    ? id
                    : "UNKNOWN";
            default -> "UNKNOWN";
        };
    }

    /**
     * El {@code dataObject} de PERMISSION no puede usar la rama CREATE de
     * commons (el resultado es un {@code boolean}, no una entidad): aquí se
     * construye el mismo mapa que el {@code buildSpecialDataObject} del
     * origen.
     */
    @Override
    @SuppressWarnings("unchecked")
    protected Map<String, Object> buildDataObject(OperationType operationType, Object[] args,
            Object result, Map<String, Object> beforeData, Auditable auditable) {
        if ("PERMISSION".equals(auditable.affectedTable())) {
            List<String> permissions = args != null && args.length > 0 && args[0] instanceof List<?>
                    ? ((List<String>) args[0])
                    : List.of();
            String roleName = args != null && args.length > 1 && args[1] instanceof String
                    ? (String) args[1]
                    : "UNKNOWN";

            Map<String, Object> entity = new LinkedHashMap<>();
            entity.put("roleName", roleName);
            entity.put("permissions", permissions);
            return Map.of("entity", entity);
        }
        return super.buildDataObject(operationType, args, result, beforeData, auditable);
    }
}
