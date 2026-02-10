package co.unicauca.edu.co.contables.configuration.commons.security;

import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
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
    private String principleAtrribute;

    private Jwt jwtToken;

    /**
     * Convierte un JWT en un token de autenticación.
     *
     * @param jwt el JWT a convertir
     * @return el token de autenticación
     */
    @Override
    public AbstractAuthenticationToken convert(@NonNull Jwt jwt) {
        Collection<GrantedAuthority> authorities = Stream
                .concat(jwtGrantedAuthoritiesConverter.convert(jwt).stream(), extractPermissions(jwt).stream())
                .toList();

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

        if (principleAtrribute != null) {
            claimName = principleAtrribute;
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
}