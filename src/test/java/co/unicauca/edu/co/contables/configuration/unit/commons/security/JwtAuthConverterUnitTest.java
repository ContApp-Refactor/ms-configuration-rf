package co.unicauca.edu.co.contables.configuration.unit.commons.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.test.util.ReflectionTestUtils;

import co.unicauca.edu.co.contables.configuration.commons.security.JwtAuthConverter;

import java.time.Instant;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitarios para JwtAuthConverter.
 * 
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("JwtAuthConverter - Tests de conversión JWT")
class JwtAuthConverterUnitTest {

    @InjectMocks
    private JwtAuthConverter jwtAuthConverter;

    private Jwt mockJwt;
    private Map<String, Object> claims;
    private Map<String, Object> headers;

    @BeforeEach
    void setUp() {
        // Configurar propiedades inyectadas
        ReflectionTestUtils.setField(jwtAuthConverter, "principleAttribute", null);
        ReflectionTestUtils.setField(jwtAuthConverter, "resourceId", "kardex-app");
        
        // Inicializar claims comunes
        claims = new HashMap<>();
        claims.put("sub", "user-123");
        claims.put("preferred_username", "john.doe");
        claims.put("email", "john@example.com");
        
        headers = new HashMap<>();
        headers.put("alg", "RS256");
        headers.put("typ", "JWT");
    }

    // ==================== Tests para convert() ====================

    @Test
    @DisplayName("convert - JWT válido sin roles retorna token con authorities básicas")
    void convert_ValidJwtWithoutRoles_ReturnsTokenWithBasicAuthorities() {
        // Arrange
        mockJwt = createJwt(claims, headers);
        
        // Act
        AbstractAuthenticationToken result = jwtAuthConverter.convert(mockJwt);
        
        // Assert
        assertNotNull(result);
        assertInstanceOf(JwtAuthenticationToken.class, result);
        assertEquals("user-123", result.getName());
        assertNotNull(result.getAuthorities());
    }

    @Test
    @DisplayName("convert - JWT con roles en resource_access retorna token con authorities correctas")
    void convert_JwtWithResourceRoles_ReturnsTokenWithCorrectAuthorities() {
        // Arrange
        Map<String, Object> resourceAccess = new HashMap<>();
        Map<String, Object> kardexResource = new HashMap<>();
        kardexResource.put("roles", Arrays.asList("ADMIN", "USER", "MANAGER"));
        resourceAccess.put("kardex-app", kardexResource);
        claims.put("resource_access", resourceAccess);
        
        mockJwt = createJwt(claims, headers);
        
        // Act
        AbstractAuthenticationToken result = jwtAuthConverter.convert(mockJwt);
        
        // Assert
        assertNotNull(result);
        Collection<? extends GrantedAuthority> authorities = result.getAuthorities();
        
        // Verificar que contiene los roles con prefijo ROLE_
        List<String> authorityStrings = authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .filter(auth -> auth.startsWith("ROLE_"))
                .toList();
        
        assertTrue(authorityStrings.contains("ROLE_ADMIN"));
        assertTrue(authorityStrings.contains("ROLE_USER"));
        assertTrue(authorityStrings.contains("ROLE_MANAGER"));
    }

    @Test
    @DisplayName("convert - JWT sin resource_access retorna token sin roles adicionales")
    void convert_JwtWithoutResourceAccess_ReturnsTokenWithoutAdditionalRoles() {
        // Arrange
        mockJwt = createJwt(claims, headers);
        
        // Act
        AbstractAuthenticationToken result = jwtAuthConverter.convert(mockJwt);
        
        // Assert
        assertNotNull(result);
        Collection<? extends GrantedAuthority> authorities = result.getAuthorities();
        
        // No debe contener roles con prefijo ROLE_ del resource_access
        List<String> roleAuthorities = authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .filter(auth -> auth.startsWith("ROLE_"))
                .toList();
        
        assertTrue(roleAuthorities.isEmpty());
    }

    @Test
    @DisplayName("convert - JWT con resource_access pero sin el resourceId configurado retorna sin roles adicionales")
    void convert_JwtWithResourceAccessButDifferentResourceId_ReturnsWithoutAdditionalRoles() {
        // Arrange
        Map<String, Object> resourceAccess = new HashMap<>();
        Map<String, Object> otherResource = new HashMap<>();
        otherResource.put("roles", Arrays.asList("ADMIN"));
        resourceAccess.put("other-app", otherResource); // Diferente resourceId
        claims.put("resource_access", resourceAccess);
        
        mockJwt = createJwt(claims, headers);
        
        // Act
        AbstractAuthenticationToken result = jwtAuthConverter.convert(mockJwt);
        
        // Assert
        assertNotNull(result);
        Collection<? extends GrantedAuthority> authorities = result.getAuthorities();
        
        List<String> roleAuthorities = authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .filter(auth -> auth.startsWith("ROLE_"))
                .toList();
        
        assertTrue(roleAuthorities.isEmpty());
    }

    @Test
    @DisplayName("convert - JWT con resource sin campo roles retorna sin roles adicionales")
    void convert_JwtWithResourceWithoutRoles_ReturnsWithoutAdditionalRoles() {
        // Arrange
        Map<String, Object> resourceAccess = new HashMap<>();
        Map<String, Object> kardexResource = new HashMap<>();
        // No incluir "roles"
        resourceAccess.put("kardex-app", kardexResource);
        claims.put("resource_access", resourceAccess);
        
        mockJwt = createJwt(claims, headers);
        
        // Act
        AbstractAuthenticationToken result = jwtAuthConverter.convert(mockJwt);
        
        // Assert
        assertNotNull(result);
        Collection<? extends GrantedAuthority> authorities = result.getAuthorities();
        
        List<String> roleAuthorities = authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .filter(auth -> auth.startsWith("ROLE_"))
                .toList();
        
        assertTrue(roleAuthorities.isEmpty());
    }

    @Test
    @DisplayName("convert - JWT con principleAttribute configurado usa ese claim como nombre")
    void convert_JwtWithCustomPrincipleAttribute_UsesConfiguredClaim() {
        // Arrange
        ReflectionTestUtils.setField(jwtAuthConverter, "principleAttribute", "preferred_username");
        claims.put("preferred_username", "john.doe");
        mockJwt = createJwt(claims, headers);
        
        // Act
        AbstractAuthenticationToken result = jwtAuthConverter.convert(mockJwt);
        
        // Assert
        assertNotNull(result);
        assertEquals("john.doe", result.getName());
    }

    @Test
    @DisplayName("convert - JWT con principleAttribute null usa 'sub' por defecto")
    void convert_JwtWithNullPrincipleAttribute_UsesSubClaimByDefault() {
        // Arrange
        ReflectionTestUtils.setField(jwtAuthConverter, "principleAttribute", null);
        mockJwt = createJwt(claims, headers);
        
        // Act
        AbstractAuthenticationToken result = jwtAuthConverter.convert(mockJwt);
        
        // Assert
        assertNotNull(result);
        assertEquals("user-123", result.getName()); // Usa 'sub'
    }

    @Test
    @DisplayName("convert - JWT con múltiples roles retorna todas las authorities correctamente")
    void convert_JwtWithMultipleRoles_ReturnsAllAuthoritiesCorrectly() {
        // Arrange
        Map<String, Object> resourceAccess = new HashMap<>();
        Map<String, Object> kardexResource = new HashMap<>();
        kardexResource.put("roles", Arrays.asList("ADMIN", "USER", "VIEWER", "EDITOR"));
        resourceAccess.put("kardex-app", kardexResource);
        claims.put("resource_access", resourceAccess);
        
        mockJwt = createJwt(claims, headers);
        
        // Act
        AbstractAuthenticationToken result = jwtAuthConverter.convert(mockJwt);
        
        // Assert
        Collection<? extends GrantedAuthority> authorities = result.getAuthorities();
        List<String> roleAuthorities = authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .filter(auth -> auth.startsWith("ROLE_"))
                .toList();
        
        assertEquals(4, roleAuthorities.size());
        assertTrue(roleAuthorities.containsAll(Arrays.asList(
            "ROLE_ADMIN", "ROLE_USER", "ROLE_VIEWER", "ROLE_EDITOR"
        )));
    }

    @Test
    @DisplayName("convert - JWT con lista de roles vacía retorna sin roles adicionales")
    void convert_JwtWithEmptyRolesList_ReturnsWithoutAdditionalRoles() {
        // Arrange
        Map<String, Object> resourceAccess = new HashMap<>();
        Map<String, Object> kardexResource = new HashMap<>();
        kardexResource.put("roles", Collections.emptyList());
        resourceAccess.put("kardex-app", kardexResource);
        claims.put("resource_access", resourceAccess);
        
        mockJwt = createJwt(claims, headers);
        
        // Act
        AbstractAuthenticationToken result = jwtAuthConverter.convert(mockJwt);
        
        // Assert
        Collection<? extends GrantedAuthority> authorities = result.getAuthorities();
        List<String> roleAuthorities = authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .filter(auth -> auth.startsWith("ROLE_"))
                .toList();
        
        assertTrue(roleAuthorities.isEmpty());
    }

    // ==================== Tests para getId() ====================

    @Test
    @DisplayName("getId - Después de convert retorna el sub claim correctamente")
    void getId_AfterConvert_ReturnsSubClaim() {
        // Arrange
        mockJwt = createJwt(claims, headers);
        jwtAuthConverter.convert(mockJwt); // Necesario para inicializar jwtToken
        
        // Act
        String result = jwtAuthConverter.getId();
        
        // Assert
        assertEquals("user-123", result);
    }

    @Test
    @DisplayName("getId - Con sub claim diferente retorna el valor correcto")
    void getId_WithDifferentSubClaim_ReturnsCorrectValue() {
        // Arrange
        claims.put("sub", "tenant-456");
        mockJwt = createJwt(claims, headers);
        jwtAuthConverter.convert(mockJwt);
        
        // Act
        String result = jwtAuthConverter.getId();
        
        // Assert
        assertEquals("tenant-456", result);
    }

    // ==================== Tests para getToken() ====================

    @Test
    @DisplayName("getToken - Después de convert retorna el token value correctamente")
    void getToken_AfterConvert_ReturnsTokenValue() {
        // Arrange
        mockJwt = createJwt(claims, headers);
        String expectedTokenValue = mockJwt.getTokenValue();
        jwtAuthConverter.convert(mockJwt);
        
        // Act
        String result = jwtAuthConverter.getToken();
        
        // Assert
        assertEquals(expectedTokenValue, result);
    }

    // ==================== Tests de casos edge ====================

    @Test
    @DisplayName("convert - JWT con resource_access null retorna sin excepción")
    void convert_JwtWithNullResourceAccess_ReturnsWithoutException() {
        // Arrange
        claims.put("resource_access", null);
        mockJwt = createJwt(claims, headers);
        
        // Act & Assert
        assertDoesNotThrow(() -> {
            AbstractAuthenticationToken result = jwtAuthConverter.convert(mockJwt);
            assertNotNull(result);
        });
    }

    @Test
    @DisplayName("convert - JWT con roles que contienen espacios retorna con prefijo ROLE_ correctamente")
    void convert_JwtWithRolesContainingSpaces_ReturnsWithCorrectPrefix() {
        // Arrange
        Map<String, Object> resourceAccess = new HashMap<>();
        Map<String, Object> kardexResource = new HashMap<>();
        kardexResource.put("roles", Arrays.asList("SUPER ADMIN", "POWER USER"));
        resourceAccess.put("kardex-app", kardexResource);
        claims.put("resource_access", resourceAccess);
        
        mockJwt = createJwt(claims, headers);
        
        // Act
        AbstractAuthenticationToken result = jwtAuthConverter.convert(mockJwt);
        
        // Assert
        Collection<? extends GrantedAuthority> authorities = result.getAuthorities();
        List<String> roleAuthorities = authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .filter(auth -> auth.startsWith("ROLE_"))
                .toList();
        
        assertTrue(roleAuthorities.contains("ROLE_SUPER ADMIN"));
        assertTrue(roleAuthorities.contains("ROLE_POWER USER"));
    }

    // ==================== Métodos auxiliares ====================

    /**
     * Crea un JWT mock con los claims y headers especificados.
     */
    private Jwt createJwt(Map<String, Object> claims, Map<String, Object> headers) {
        return new Jwt(
            "mock-token-value",
            Instant.now(),
            Instant.now().plusSeconds(3600),
            headers,
            claims
        );
    }
}
