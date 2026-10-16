package co.unicauca.edu.co.contables.auxiliary.infrastructure.out.security;

import org.springframework.stereotype.Component;
import java.util.Collection;
import java.util.List;
import java.util.Map;
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

/**
 * @brief Convertidor de tokens JWT a autenticación de Spring Security.
 *
 * Convierte un {@link Jwt} en un {@link AbstractAuthenticationToken}
 * combinando autoridades estándar con los roles extraídos del claim
 * {@code resource_access} para el {@code resource-id} configurado.
 * También implementa {@link IJwtUtils} para exponer el identificador
 * del usuario autenticado ({@code sub}).
 */
@Component("auxiliaryJwtAuthConverter")
public class JwtAuthConverter implements Converter<Jwt, AbstractAuthenticationToken>, IJwtUtils {

    private final JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();

    @Value("${jwt.auth.converter.principle-attribute}")
    private String principleAtrribute;

    @Value("${jwt.auth.converter.resource-id}")
    private String resourceId;

    Jwt jwtToken;

    /**
     * @brief Convierte un JWT en un {@link AbstractAuthenticationToken}.
     * @param jwt JWT a convertir.
     * @return Token de autenticación con autoridades consolidadas y principal.
     */
    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        Collection<GrantedAuthority> authorities = Stream
                .concat(jwtGrantedAuthoritiesConverter.convert(jwt).stream(), extractResourceRoles(jwt).stream())
                .toList();

        this.jwtToken = jwt;

        return new JwtAuthenticationToken(jwt, authorities, getPrincipleName(jwt));
    }

    /**
     * @brief Obtiene el nombre del principal desde el JWT.
     *
     * Por defecto usa el claim "sub"; si se configura
     * {@code principle-attribute}, utiliza ese claim alternativo.
     *
     * @param jwt JWT del cual extraer el principal.
     * @return Nombre del principal autenticado.
     */
    private String getPrincipleName(Jwt jwt) {
        String claimName = JwtClaimNames.SUB;

        if (principleAtrribute != null) {
            claimName = principleAtrribute;
        }

        return jwt.getClaim(claimName);
    }

    /**
     * @brief Extrae los roles del claim "resource_access" para el resource-id configurado.
     *
     * Cada rol se convierte a {@link SimpleGrantedAuthority} con el
     * prefijo "ROLE_". Retorna una colección vacía si el claim o los
     * roles no están presentes.
     *
     * @param jwt JWT del cual extraer los roles.
     * @return Colección de {@link GrantedAuthority} con los roles del recurso.
     */
    @SuppressWarnings("unchecked")
    private Collection<? extends GrantedAuthority> extractResourceRoles(Jwt jwt) {
        Map<String, Object> resourceAccess;
        Map<String, Object> resource;
        Collection<String> resourceRoles;

        if (jwt.getClaim("resource_access") == null) {
            return List.of();
        }

        resourceAccess = jwt.getClaim("resource_access");

        if (resourceAccess.get(resourceId) == null) {
            return List.of();
        }

        resource = (Map<String, Object>) resourceAccess.get(resourceId);

        if (resource.get("roles") == null) {
            return List.of();
        }

        resourceRoles = (Collection<String>) resource.get("roles");

        return resourceRoles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_".concat(role)))
                .toList();
    }

    /**
     * @brief Obtiene el claim "sub" del JWT como identificador del usuario.
     * @return Identificador del usuario autenticado.
     */
    @Override
    public String getId() {
        return (String) jwtToken.getClaims().get("sub");
    }

}
