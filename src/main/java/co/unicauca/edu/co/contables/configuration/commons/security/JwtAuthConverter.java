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
        if (authorization == null || !authorization.containsKey("permissions")) {
            return Set.of();
        }

        List<Map<String, Object>> permissions =
                (List<Map<String, Object>>) authorization.get("permissions");

        Set<GrantedAuthority> authorities = new HashSet<>();

        for (Map<String, Object> permission : permissions) {
            Object rsnameObj = permission.get("rsname");
            if (!(rsnameObj instanceof String rsnameRaw) || rsnameRaw.isBlank()) {
                continue;
            }
            String resourceName = rsnameRaw;
            authorities.add(new SimpleGrantedAuthority(resourceName));
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