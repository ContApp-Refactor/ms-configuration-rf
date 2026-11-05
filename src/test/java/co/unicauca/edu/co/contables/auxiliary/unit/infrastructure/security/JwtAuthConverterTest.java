package co.unicauca.edu.co.contables.auxiliary.unit.infrastructure.security;

import co.unicauca.edu.co.contables.auxiliary.infrastructure.out.security.JwtAuthConverter;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * @brief Pruebas unitarias para {@link JwtAuthConverter}.
 */
class JwtAuthConverterTest {

    private JwtAuthConverter converter;

    @BeforeEach
    void setUp() {
        converter = new JwtAuthConverter();
        ReflectionTestUtils.setField(converter, "resourceId", "auxiliary-book");
        ReflectionTestUtils.setField(converter, "principleAtrribute", null);
    }

    private Jwt buildJwt(Map<String, Object> claims) {
        return new Jwt(
                "token-value",
                Instant.now(),
                Instant.now().plusSeconds(60),
                Map.of("alg", "none"),
                claims
        );
    }

    @Test
    @DisplayName("convert debe retornar JwtAuthenticationToken con roles ROLE_* del resource-id configurado")
    void convertIncludesResourceRoles() {
        Jwt jwt = buildJwt(Map.of(
                "sub", "user-123",
                "resource_access", Map.of(
                        "auxiliary-book", Map.of("roles", List.of("admin", "user"))
                )
        ));

        AbstractAuthenticationToken token = converter.convert(jwt);

        Assertions.assertThat(token).isNotNull();
        Assertions.assertThat(token.getName()).isEqualTo("user-123");
        List<String> authorities = token.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();
        Assertions.assertThat(authorities).contains("ROLE_admin", "ROLE_user");
    }

    @Test
    @DisplayName("convert debe retornar token sin roles de recurso cuando resource_access está ausente")
    void convertWithoutResourceAccess() {
        Jwt jwt = buildJwt(Map.of("sub", "user-xyz"));

        AbstractAuthenticationToken token = converter.convert(jwt);

        Assertions.assertThat(token).isNotNull();
        Assertions.assertThat(token.getName()).isEqualTo("user-xyz");
        Assertions.assertThat(token.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList())
                .noneMatch(a -> a.startsWith("ROLE_"));
    }

    @Test
    @DisplayName("convert debe retornar token sin roles cuando el resource-id no está en resource_access")
    void convertResourceIdMissing() {
        Jwt jwt = buildJwt(Map.of(
                "sub", "user-1",
                "resource_access", Map.of(
                        "otro-servicio", Map.of("roles", List.of("admin"))
                )
        ));

        AbstractAuthenticationToken token = converter.convert(jwt);

        Assertions.assertThat(token.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList())
                .noneMatch(a -> a.startsWith("ROLE_"));
    }

    @Test
    @DisplayName("convert debe retornar token sin roles cuando el claim roles está ausente")
    void convertRolesMissing() {
        Jwt jwt = buildJwt(Map.of(
                "sub", "user-1",
                "resource_access", Map.of(
                        "auxiliary-book", Map.of()
                )
        ));

        AbstractAuthenticationToken token = converter.convert(jwt);

        Assertions.assertThat(token.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList())
                .noneMatch(a -> a.startsWith("ROLE_"));
    }

    @Test
    @DisplayName("getId debe retornar el claim sub del último JWT convertido")
    void getIdReturnsSubClaim() {
        Jwt jwt = buildJwt(Map.of("sub", "identificador-usuario"));
        converter.convert(jwt);

        Assertions.assertThat(converter.getId()).isEqualTo("identificador-usuario");
    }

    @Test
    @DisplayName("convert debe usar principle-attribute alternativo cuando está configurado")
    void convertUsesAlternativePrincipleAttribute() {
        ReflectionTestUtils.setField(converter, "principleAtrribute", "preferred_username");
        Jwt jwt = buildJwt(Map.of(
                "sub", "uuid-1",
                "preferred_username", "alej.fernandez"
        ));

        AbstractAuthenticationToken token = converter.convert(jwt);

        Assertions.assertThat(token.getName()).isEqualTo("alej.fernandez");
    }
}
