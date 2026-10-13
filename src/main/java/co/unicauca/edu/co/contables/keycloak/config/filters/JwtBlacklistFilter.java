package co.unicauca.edu.co.contables.keycloak.config.filters;

import co.unicauca.edu.co.contables.keycloak.util.KeycloakJwtUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
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
 */
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
            if (jti != null && Boolean.TRUE.equals(redis.hasKey(prefix + jti))) {
                res.sendError(HttpStatus.UNAUTHORIZED.value(), "Token revocado");
                return;
            }
        }
        chain.doFilter(req, res);
    }
}
