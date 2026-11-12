package co.unicauca.edu.co.contables.commons.backup;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import co.unicauca.edu.co.contables.commons.multitenancy.utils.TenantContext;

/**
 * @brief Quién ejecuta la operación: tenant y usuario capturados en el hilo
 * de la petición HTTP.
 *
 * NO usa {@code JwtAuthConverter}/{@code IJwtUtils.getId()}: ese bean es
 * singleton y guarda el ÚLTIMO JWT convertido en un campo, así que con
 * concurrencia devuelve el {@code sub} de otro usuario. Aquí el tenant sale de
 * {@link TenantContext} (ThreadLocal) y el usuario del {@code sub} del JWT que
 * está en {@link SecurityContextHolder}. Debe capturarse en el controlador
 * (antes de cualquier {@code @Async}, que no propaga el SecurityContext).
 */
public record RestoreActor(String tenantId, String userId) {

    public RestoreActor {
        if (tenantId == null || tenantId.isBlank()) {
            throw new IllegalStateException("No hay tenant en el contexto de la petición");
        }
        if (userId == null || userId.isBlank()) {
            throw new IllegalStateException("No hay usuario autenticado en el contexto de la petición");
        }
    }

    /** Captura tenant (TenantContext) y usuario ({@code sub} del JWT del SecurityContext) del hilo actual. */
    public static RestoreActor fromCurrentContext() {
        return new RestoreActor(TenantContext.getTenantId(), currentUserId());
    }

    private static String currentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            return null;
        }
        if (auth instanceof JwtAuthenticationToken jwtAuth) {
            return jwtAuth.getToken().getSubject();
        }
        if (auth.getPrincipal() instanceof Jwt jwt) {
            return jwt.getSubject();
        }
        return auth.getName();
    }
}
