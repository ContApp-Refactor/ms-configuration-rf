package co.unicauca.edu.co.contables.configuration.unit.commons.config.aspect;

import co.unicauca.edu.co.contables.commons.config.aspect.JwtTokenService;
import co.unicauca.edu.co.contables.commons.security.IJwtUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Unit tests for JwtTokenService")
public class JwtTokenServiceUnitTest {

    @Mock
    private IJwtUtils jwtUtils;

    @InjectMocks
    private JwtTokenService jwtTokenService;

    @AfterEach
    void cleanUp() {
        jwtTokenService.clearRabbitContext();
    }

    @Test
    @DisplayName("getToken should return RabbitMQ token when set")
    void shouldReturnRabbitTokenWhenAvailable() {
        // Arrange
        jwtTokenService.setRabbitJwtToken("rabbit-token");

        // Act
        String result = jwtTokenService.getToken();

        // Assert
        assertThat(result).isEqualTo("rabbit-token");
        verifyNoInteractions(jwtUtils);
    }

    @Test
    @DisplayName("getToken should fallback to HTTP token")
    void shouldFallbackToHttpToken() {
        // Arrange
        when(jwtUtils.getToken()).thenReturn("http-token");

        // Act
        String result = jwtTokenService.getToken();

        // Assert
        assertThat(result).isEqualTo("http-token");
    }

    @Test
    @DisplayName("getToken should throw when no token available")
    void shouldThrowWhenNoToken() {
        // Arrange
        when(jwtUtils.getToken()).thenThrow(new RuntimeException("No token"));

        // Act & Assert
        assertThatThrownBy(() -> jwtTokenService.getToken())
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("getTenantId should return RabbitMQ tenant when set")
    void shouldReturnRabbitTenantWhenAvailable() {
        // Arrange
        jwtTokenService.setRabbitTenantId("rabbit-tenant");

        // Act
        String result = jwtTokenService.getTenantId();

        // Assert
        assertThat(result).isEqualTo("rabbit-tenant");
        verifyNoInteractions(jwtUtils);
    }

    @Test
    @DisplayName("getTenantId should fallback to HTTP tenant")
    void shouldFallbackToHttpTenant() {
        // Arrange
        when(jwtUtils.getId()).thenReturn("http-tenant");

        // Act
        String result = jwtTokenService.getTenantId();

        // Assert
        assertThat(result).isEqualTo("http-tenant");
    }

    @Test
    @DisplayName("getTenantId should throw when no tenant available")
    void shouldThrowWhenNoTenant() {
        // Arrange
        when(jwtUtils.getId()).thenThrow(new RuntimeException("No tenant"));

        // Act & Assert
        assertThatThrownBy(() -> jwtTokenService.getTenantId())
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("clearRabbitContext should remove stored values")
    void shouldClearContext() {
        // Arrange
        jwtTokenService.setRabbitJwtToken("token");
        jwtTokenService.setRabbitTenantId("tenant");

        // Act
        jwtTokenService.clearRabbitContext();

        // Assert
        assertThat(jwtTokenService.isInRabbitContext()).isFalse();
    }

    @Test
    @DisplayName("isInRabbitContext should detect when token is set")
    void shouldDetectRabbitContext() {
        // Initially false
        assertThat(jwtTokenService.isInRabbitContext()).isFalse();

        // Set token
        jwtTokenService.setRabbitJwtToken("token");
        assertThat(jwtTokenService.isInRabbitContext()).isTrue();

        // Clear
        jwtTokenService.clearRabbitContext();
        assertThat(jwtTokenService.isInRabbitContext()).isFalse();
    }
}
