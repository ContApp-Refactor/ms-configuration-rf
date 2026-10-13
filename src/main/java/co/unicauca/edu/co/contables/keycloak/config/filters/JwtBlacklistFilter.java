package co.unicauca.edu.co.contables.keycloak.config.filters;

import co.unicauca.edu.co.contables.keycloak.util.KeycloakJwtUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filtro que rechaza tokens JWT revocados (presentes en la blacklist de Redis).
 * <p>
 * Se registra como {@code @Component}, por lo que Spring Boot lo monta como
 * filtro servlet sobre todas las peticiones sin tocar la
 * {@code SecurityFilterChain} de commons (D-12.1).
 * </p>
 * <p>
 * Si Redis no está disponible el filtro <strong>no bloquea la petición</strong>:
 * deja pasar la cadena y deja constancia en el log. Un filtro de seguridad no
 * debe tumbar toda la autenticación porque falle una dependencia de caché; el
 * origen evitaba el 500 de forma accidental (su parseo de JWT fallaba la
 * verificación de firma y devolvía {@code null}, de modo que nunca llegaba a
 * consultar Redis). Con este comportamiento explícito, en local —sin Redis— se
 * conserva la paridad funcional y, cuando Redis esté disponible, la blacklist
 * sí opera.
 * </p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtBlacklistFilter extends OncePerRequestFilter {

    private final StringRedisTemplate redis;

    @Value("${app.jwt.blacklist-prefix:jwt:black:}")
    private String prefix;

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {
        String hdr = req.getHeader(HttpHeaders.AUTHORIZATION);
        if (hdr != null && hdr.startsWith("Bearer ")) {
            String token = hdr.substring(7);
            String jti = KeycloakJwtUtils.getJti(token);
            if (jti != null && isBlacklisted(jti)) {
                res.sendError(HttpStatus.UNAUTHORIZED.value(), "Token revocado");
                return;
            }
        }
        chain.doFilter(req, res);
    }

    /**
     * Consulta la blacklist tolerando la indisponibilidad de Redis.
     *
     * @param jti identificador del token a consultar
     * @return {@code true} si el token está revocado; {@code false} si no lo
     *         está o si Redis no respondió
     */
    private boolean isBlacklisted(String jti) {
        try {
            return Boolean.TRUE.equals(redis.hasKey(prefix + jti));
        } catch (RedisConnectionFailureException e) {
            log.warn("Redis no disponible, blacklist de JWT omitida para esta peticion: {}",
                    e.getMessage());
            return false;
        }
    }
}
