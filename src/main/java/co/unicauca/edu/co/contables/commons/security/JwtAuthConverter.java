package co.unicauca.edu.co.contables.commons.security;

import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimNames;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.lang.NonNull;

/**
 * Clase que implementa la conversión de un JWT en un token de autenticación.
 * También proporciona métodos utilitarios relacionados con JWT.
 */
@Component
@Slf4j
public class JwtAuthConverter implements Converter<Jwt, AbstractAuthenticationToken>, IJwtUtils {

    private final JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();

    @Value("${jwt.auth.converter.principle-attribute}")
    private String principleAttribute;

    @Value("${jwt.auth.converter.resource-id}")
    private String resourceId;

    private Jwt jwtToken;

    /**
     * Convierte un JWT en un token de autenticación.
     *
     * @param jwt el JWT a convertir
     * @return el token de autenticación
     */
    @Override
    public AbstractAuthenticationToken convert(@NonNull Jwt jwt) {
        Set<GrantedAuthority> authorities = new HashSet<>();
        authorities.addAll(jwtGrantedAuthoritiesConverter.convert(jwt));
        authorities.addAll(extractResourceRoles(jwt));
        authorities.addAll(extractRealmRolesAuthorities(jwt));
        authorities.addAll(extractPermissions(jwt));

        this.jwtToken = jwt;
        return new JwtAuthenticationToken(jwt, authorities, getPrincipleName(jwt));
    }

    /**
     * Obtiene el nombre principal del JWT.
     *
     * @param jwt el JWT
     * @return el nombre principal
     */
    private String getPrincipleName(Jwt jwt) {
        String claimName = JwtClaimNames.SUB;

        if (principleAttribute != null) {
            claimName = principleAttribute;
        }

        return jwt.getClaim(claimName);
    }

    /**
     * Extrae los roles de recursos del JWT.
     *
     * @param jwt el JWT
     * @return una colección de autoridades concedidas
     */
    @SuppressWarnings("unchecked")
    private Collection<? extends GrantedAuthority> extractPermissions(Jwt jwt) {
        Map<String, Object> authorization = jwt.getClaim("authorization");
        if (authorization == null)
            return Set.of();

        Object permsObj = authorization.get("permissions");
        if (!(permsObj instanceof List<?> permissionsList))
            return Set.of();

        Set<GrantedAuthority> authorities = new HashSet<>();

        for (Object permObj : permissionsList) {
            if (!(permObj instanceof Map<?, ?> permission))
                continue;

            Object rsnameObj = permission.get("rsname");
            if (!(rsnameObj instanceof String rsnameRaw))
                continue;

            String resourceName = rsnameRaw.trim();
            if (resourceName.isEmpty())
                continue;

            // Siempre agrega el recurso "plano" (sirve para recursos sin scopes)
            authorities.add(new SimpleGrantedAuthority(resourceName));

            // Si tiene scopes, agrega resource#scope por cada uno
            Object scopesObj = permission.get("scopes");
            if (scopesObj instanceof Collection<?> scopes) {
                for (Object s : scopes) {
                    if (s instanceof String scopeRaw) {
                        String scope = scopeRaw.trim();
                        if (!scope.isEmpty()) {
                            authorities.add(new SimpleGrantedAuthority(resourceName + "#" + scope));
                        }
                    }
                }
            }
        }

        return authorities;
    }

    @SuppressWarnings("unchecked")
    private Collection<? extends GrantedAuthority> extractResourceRoles(Jwt jwt) {
        if (resourceId == null) {
            return Set.of();
        }
        Map<String, Object> resourceAccess = jwt.getClaim("resource_access");
        if (resourceAccess == null) {
            return Set.of();
        }
        Object resourceObj = resourceAccess.get(resourceId);
        if (!(resourceObj instanceof Map<?, ?> resource)) {
            return Set.of();
        }
        Object rolesObj = ((Map<String, Object>) resource).get("roles");
        if (!(rolesObj instanceof Collection<?> roles)) {
            return Set.of();
        }
        return roles.stream()
                .filter(String.class::isInstance)
                .map(String.class::cast)
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                .toList();
    }

    @SuppressWarnings("unchecked")
    private Collection<? extends GrantedAuthority> extractRealmRolesAuthorities(Jwt jwt) {
        Map<String, Object> realmAccess = jwt.getClaim("realm_access");
        if (realmAccess == null) {
            return Set.of();
        }
        Object rolesObj = realmAccess.get("roles");
        if (!(rolesObj instanceof List<?> rolesList)) {
            return Set.of();
        }
        return rolesList.stream()
                .filter(String.class::isInstance)
                .map(String.class::cast)
                .filter(role -> !SYSTEM_ROLES.contains(role))
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                .toList();
    }

    /**
     * Devuelve el valor del claim "sub" del JWT, que se
     * utiliza como identificador del usuario autenticado.
     * 
     * @return el identificador del usuario autenticado
     */
    @Override
    public String getId() {
        return (String) jwtToken.getClaims().get("sub");
    }

    @Override
    public String getToken() {
        return jwtToken.getTokenValue();
    }

    @Override
    public String getUsername() {
        return (String) jwtToken.getClaims().get("preferred_username");
    }

    private static final Set<String> SYSTEM_ROLES = Set.of(
            "offline_access",
            "uma_authorization",
            "default-roles-oauth2-realm");

    @SuppressWarnings("unchecked")
    @Override
    public List<String> getRealmRoles() {
        Map<String, Object> realmAccess = (Map<String, Object>) jwtToken.getClaims().get("realm_access");
        if (realmAccess == null)
            return List.of();
        Object rolesObj = realmAccess.get("roles");
        if (!(rolesObj instanceof List<?> rolesList))
            return List.of();
        return rolesList.stream()
                .filter(String.class::isInstance)
                .map(String.class::cast)
                .filter(role -> !SYSTEM_ROLES.contains(role))
                .toList();
    }
}