package co.unicauca.edu.co.contables.configuration.unit.commons.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import co.unicauca.edu.co.contables.configuration.commons.security.JwtDecoder;

import java.util.Base64;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitarios para JwtDecoder.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("JwtDecoder - Tests de decodificación JWT")
class JwtDecoderUnitTest {

    private JwtDecoder jwtDecoder;

    // Tokens de prueba
    private String validJwtToken;
    private String validJwtTokenWithBearer;
    private String jwtTokenWithoutSub;
    private String invalidFormatToken;
    private String invalidBase64Token;
    private String jwtTokenWithMultipleClaims;

    @BeforeEach
    void setUp() {
        jwtDecoder = new JwtDecoder();
        
        // Token JWT válido con claim "sub": "tenant123"
        // Payload: {"sub":"tenant123","name":"John Doe","iat":1516239022}
        String header = base64Encode("{\"alg\":\"HS256\",\"typ\":\"JWT\"}");
        String payload = base64Encode("{\"sub\":\"tenant123\",\"name\":\"John Doe\",\"iat\":1516239022}");
        String signature = "fake-signature";
        validJwtToken = header + "." + payload + "." + signature;
        validJwtTokenWithBearer = "Bearer " + validJwtToken;
        
        // Token JWT sin claim "sub"
        // Payload: {"name":"Jane Doe","iat":1516239022}
        String payloadWithoutSub = base64Encode("{\"name\":\"Jane Doe\",\"iat\":1516239022}");
        jwtTokenWithoutSub = header + "." + payloadWithoutSub + "." + signature;
        
        // Token con formato inválido (solo 2 partes)
        invalidFormatToken = header + "." + payload;
        
        // Token con Base64 inválido en el payload
        invalidBase64Token = header + ".invalid@base64!content." + signature;
        
        // Token con múltiples claims
        // Payload: {"sub":"tenant456","name":"Alice","role":"admin","email":"alice@example.com"}
        String payloadMultiple = base64Encode("{\"sub\":\"tenant456\",\"name\":\"Alice\",\"role\":\"admin\",\"email\":\"alice@example.com\"}");
        jwtTokenWithMultipleClaims = header + "." + payloadMultiple + "." + signature;
    }

    // ==================== Tests para extractTenantId ====================

    @Test
    @DisplayName("extractTenantId - Token válido sin prefijo Bearer extrae tenantId correctamente")
    void extractTenantId_ValidTokenWithoutBearer_ReturnsTenantId() {
        // Act
        String result = jwtDecoder.extractTenantId(validJwtToken);
        
        // Assert
        assertNotNull(result);
        assertEquals("tenant123", result);
    }

    @Test
    @DisplayName("extractTenantId - Token válido con prefijo Bearer extrae tenantId correctamente")
    void extractTenantId_ValidTokenWithBearer_ReturnsTenantId() {
        // Act
        String result = jwtDecoder.extractTenantId(validJwtTokenWithBearer);
        
        // Assert
        assertNotNull(result);
        assertEquals("tenant123", result);
    }

    @Test
    @DisplayName("extractTenantId - Token con formato inválido retorna null")
    void extractTenantId_InvalidFormatToken_ReturnsNull() {
        // Act
        String result = jwtDecoder.extractTenantId(invalidFormatToken);
        
        // Assert
        assertNull(result);
    }

    @Test
    @DisplayName("extractTenantId - Token sin claim 'sub' retorna null")
    void extractTenantId_TokenWithoutSubClaim_ReturnsNull() {
        // Act
        String result = jwtDecoder.extractTenantId(jwtTokenWithoutSub);
        
        // Assert
        assertNull(result);
    }

    @Test
    @DisplayName("extractTenantId - Token con Base64 inválido retorna null")
    void extractTenantId_InvalidBase64Token_ReturnsNull() {
        // Act
        String result = jwtDecoder.extractTenantId(invalidBase64Token);
        
        // Assert
        assertNull(result);
    }

    @Test
    @DisplayName("extractTenantId - Token vacío retorna null")
    void extractTenantId_EmptyToken_ReturnsNull() {
        // Act
        String result = jwtDecoder.extractTenantId("");
        
        // Assert
        assertNull(result);
    }

    @Test
    @DisplayName("extractTenantId - Token con un solo punto retorna null")
    void extractTenantId_TokenWithSingleDot_ReturnsNull() {
        // Act
        String result = jwtDecoder.extractTenantId("header.payload");
        
        // Assert
        assertNull(result);
    }

    @Test
    @DisplayName("extractTenantId - Token con payload JSON malformado retorna null")
    void extractTenantId_MalformedJsonPayload_ReturnsNull() {
        // Arrange
        String header = base64Encode("{\"alg\":\"HS256\"}");
        String malformedPayload = base64Encode("{\"sub\":\"tenant\",invalid}"); // JSON inválido
        String token = header + "." + malformedPayload + ".signature";
        
        // Act
        String result = jwtDecoder.extractTenantId(token);
        
        // Assert
        assertNull(result);
    }

    // ==================== Tests para extractClaim ====================

    @Test
    @DisplayName("extractClaim - Token válido con claim existente retorna el valor correcto")
    void extractClaim_ValidTokenWithExistingClaim_ReturnsClaimValue() {
        // Act
        String result = jwtDecoder.extractClaim(validJwtToken, "name");
        
        // Assert
        assertNotNull(result);
        assertEquals("John Doe", result);
    }

    @Test
    @DisplayName("extractClaim - Token válido con prefijo Bearer extrae claim correctamente")
    void extractClaim_ValidTokenWithBearerPrefix_ReturnsClaimValue() {
        // Act
        String result = jwtDecoder.extractClaim(validJwtTokenWithBearer, "name");
        
        // Assert
        assertNotNull(result);
        assertEquals("John Doe", result);
    }

    @Test
    @DisplayName("extractClaim - Token válido sin el claim solicitado retorna null")
    void extractClaim_ValidTokenWithoutRequestedClaim_ReturnsNull() {
        // Act
        String result = jwtDecoder.extractClaim(validJwtToken, "nonexistent");
        
        // Assert
        assertNull(result);
    }

    @Test
    @DisplayName("extractClaim - Token con formato inválido retorna null")
    void extractClaim_InvalidFormatToken_ReturnsNull() {
        // Act
        String result = jwtDecoder.extractClaim(invalidFormatToken, "name");
        
        // Assert
        assertNull(result);
    }

    @Test
    @DisplayName("extractClaim - Token con Base64 inválido retorna null")
    void extractClaim_InvalidBase64Token_ReturnsNull() {
        // Act
        String result = jwtDecoder.extractClaim(invalidBase64Token, "name");
        
        // Assert
        assertNull(result);
    }

    @Test
    @DisplayName("extractClaim - Token con múltiples claims extrae el claim correcto")
    void extractClaim_TokenWithMultipleClaims_ReturnsCorrectClaim() {
        // Act
        String sub = jwtDecoder.extractClaim(jwtTokenWithMultipleClaims, "sub");
        String name = jwtDecoder.extractClaim(jwtTokenWithMultipleClaims, "name");
        String role = jwtDecoder.extractClaim(jwtTokenWithMultipleClaims, "role");
        String email = jwtDecoder.extractClaim(jwtTokenWithMultipleClaims, "email");
        
        // Assert
        assertEquals("tenant456", sub);
        assertEquals("Alice", name);
        assertEquals("admin", role);
        assertEquals("alice@example.com", email);
    }

    @Test
    @DisplayName("extractClaim - Token con claim de tipo numérico retorna como string")
    void extractClaim_NumericClaim_ReturnsAsString() {
        // Arrange
        String header = base64Encode("{\"alg\":\"HS256\"}");
        String payload = base64Encode("{\"sub\":\"tenant\",\"iat\":1516239022}");
        String token = header + "." + payload + ".signature";
        
        // Act
        String result = jwtDecoder.extractClaim(token, "iat");
        
        // Assert
        assertNotNull(result);
        assertEquals("1516239022", result);
    }

    @Test
    @DisplayName("extractClaim - Token vacío retorna null")
    void extractClaim_EmptyToken_ReturnsNull() {
        // Act
        String result = jwtDecoder.extractClaim("", "name");
        
        // Assert
        assertNull(result);
    }

    @Test
    @DisplayName("extractClaim - Token con payload JSON malformado retorna null")
    void extractClaim_MalformedJsonPayload_ReturnsNull() {
        // Arrange
        String header = base64Encode("{\"alg\":\"HS256\"}");
        String malformedPayload = base64Encode("{invalid-json}");
        String token = header + "." + malformedPayload + ".signature";
        
        // Act
        String result = jwtDecoder.extractClaim(token, "name");
        
        // Assert
        assertNull(result);
    }

    @Test
    @DisplayName("extractClaim - Extrae claim 'sub' igual que extractTenantId")
    void extractClaim_ExtractSubClaim_MatchesExtractTenantId() {
        // Act
        String tenantIdViaExtractTenantId = jwtDecoder.extractTenantId(validJwtToken);
        String tenantIdViaExtractClaim = jwtDecoder.extractClaim(validJwtToken, "sub");
        
        // Assert
        assertEquals(tenantIdViaExtractTenantId, tenantIdViaExtractClaim);
    }

    // ==================== Métodos auxiliares ====================

    /**
     * Codifica un string en Base64 URL-safe sin padding.
     */
    private String base64Encode(String value) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(value.getBytes());
    }
}
