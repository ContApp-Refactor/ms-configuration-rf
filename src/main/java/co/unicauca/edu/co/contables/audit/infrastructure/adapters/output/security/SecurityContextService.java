package co.unicauca.edu.co.contables.audit.infrastructure.adapters.output.security;

import java.util.Collection;
import java.util.Optional;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;

import co.unicauca.edu.co.contables.audit.infrastructure.adapters.output.exception.security.AuthenticationRequiredException;
import co.unicauca.edu.co.contables.audit.infrastructure.adapters.output.exception.security.InvalidJwtAuthenticationException;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class SecurityContextService {

    public String getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AuthenticationRequiredException("No authenticated user found");
        }

        if (authentication instanceof JwtAuthenticationToken jwtAuth) {
            Jwt jwt = jwtAuth.getToken();
            String userId = jwt.getClaim("sub");
            log.debug("Extracted user ID from JWT: {}", userId);
            return userId;
        }

        throw new InvalidJwtAuthenticationException("Authentication is not JWT-based");
    }

    public String getCurrentUserRole() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AuthenticationRequiredException("No authenticated user found");
        }

        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();

        Optional<String> role = authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .filter(auth -> auth.startsWith("ROLE_"))
                .map(auth -> auth.substring(5))
                .findFirst();

        if (role.isPresent()) {
            log.debug("Extracted user role: {}", role.get());
            return role.get();
        }

        throw new InvalidJwtAuthenticationException("No role found for current user");
    }

    public String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AuthenticationRequiredException("No authenticated user found");
        }

        if (authentication instanceof JwtAuthenticationToken jwtAuth) {
            Jwt jwt = jwtAuth.getToken();
            String username = jwt.getClaim("preferred_username");

            if (username == null) {
                username = jwt.getClaim("sub");
            }

            log.debug("Extracted username from JWT: {}", username);
            return username;
        }

        return authentication.getName();
    }

    public String getJwtToken() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication instanceof JwtAuthenticationToken jwtAuth) {
            return jwtAuth.getToken().getTokenValue();
        }

        throw new InvalidJwtAuthenticationException("No JWT token found in security context");
    }

    public String extractClaim(String claimName) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication instanceof JwtAuthenticationToken jwtAuth) {
            Jwt jwt = jwtAuth.getToken();
            Object claim = jwt.getClaim(claimName);

            if (claim != null) {
                log.debug("Extracted claim '{}': {}", claimName, claim);
                return claim.toString();
            }

            log.warn("Claim '{}' not found in JWT", claimName);
            return null;
        }

        throw new InvalidJwtAuthenticationException("Authentication is not JWT-based");
    }

    /**
     * Retorna el token JWT como string puro
     */
    public String getToken() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication instanceof JwtAuthenticationToken jwtAuth) {
            return jwtAuth.getToken().getTokenValue();
        }

        throw new InvalidJwtAuthenticationException("No JWT token found in security context");
    }

    /**
     * Retorna el token JWT con prefijo "Bearer "
     */
    public String getTokenWithBearer() {
        return "Bearer " + getToken();
    }
}
