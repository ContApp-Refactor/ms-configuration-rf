package co.unicauca.edu.co.contables.configuration.unit.commons.config.aspect;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;

import co.unicauca.edu.co.contables.commons.config.aspect.JWTContextRabbitMqAspect;
import co.unicauca.edu.co.contables.commons.config.aspect.JwtTokenService;
import co.unicauca.edu.co.contables.commons.multitenancy.utils.TenantContext;
import co.unicauca.edu.co.contables.commons.security.JwtDecoder;
import com.rabbitmq.client.impl.LongStringHelper;


@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class JWTContextRabbitMqAspectUnitTest {

    @Mock
    private JwtTokenService jwtTokenService;

    @Mock
    private JwtDecoder jwtDecoder;

    @Mock
    private ProceedingJoinPoint joinPoint;

    @Mock
    private Signature signature;

    private JWTContextRabbitMqAspect aspect;

    @BeforeEach
    void setUp() throws Exception {
        aspect = new JWTContextRabbitMqAspect();

        Field jwtTokenServiceField = JWTContextRabbitMqAspect.class.getDeclaredField("jwtTokenService");
        jwtTokenServiceField.setAccessible(true);
        jwtTokenServiceField.set(aspect, jwtTokenService);

        Field jwtDecoderField = JWTContextRabbitMqAspect.class.getDeclaredField("jwtDecoder");
        jwtDecoderField.setAccessible(true);
        jwtDecoderField.set(aspect, jwtDecoder);

        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.getName()).thenReturn("testListener");
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    private Message createMessageWithToken(String token) {
        MessageProperties props = new MessageProperties();
        Map<String, Object> headers = new HashMap<>();
        headers.put("x-jwt-token", token);
        props.setHeaders(headers);
        return new Message("test body".getBytes(), props);
    }

    private Message createMessageWithLongStringToken(String token) {
        MessageProperties props = new MessageProperties();
        Map<String, Object> headers = new HashMap<>();
        headers.put("x-jwt-token", LongStringHelper.asLongString(token));
        props.setHeaders(headers);
        return new Message("test body".getBytes(), props);
    }

    private Message createMessageWithoutToken() {
        MessageProperties props = new MessageProperties();
        props.setHeaders(new HashMap<>());
        return new Message("test body".getBytes(), props);
    }

    @Test
    @DisplayName("Debe configurar contexto de tenant correctamente con JWT válido como String")
    void testSetTenantContextWithValidJwtAsString() throws Throwable {
        // Arrange
        String jwtToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJURU5BTlQxMjMifQ.test";
        String tenantId = "TENANT123";
        Message message = createMessageWithToken(jwtToken);

        when(joinPoint.getArgs()).thenReturn(new Object[]{message});
        when(jwtDecoder.extractTenantId(jwtToken)).thenReturn(tenantId);
        when(joinPoint.proceed()).thenReturn("success");

        // Act
        Object result = aspect.setTenantContext(joinPoint);

        // Assert
        assertEquals("success", result);
        verify(jwtDecoder).extractTenantId(jwtToken);
        verify(jwtTokenService).setRabbitJwtToken(jwtToken);
        verify(jwtTokenService).setRabbitTenantId(tenantId);
        verify(joinPoint).proceed();
        verify(jwtTokenService).clearRabbitContext();
    }

    @Test
    @DisplayName("Debe configurar contexto de tenant correctamente con JWT válido como LongString")
    void testSetTenantContextWithValidJwtAsLongString() throws Throwable {
        // Arrange
        String jwtToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJURU5BTlQxMjMifQ.test";
        String tenantId = "TENANT123";
        Message message = createMessageWithLongStringToken(jwtToken);

        when(joinPoint.getArgs()).thenReturn(new Object[]{message});
        when(jwtDecoder.extractTenantId(jwtToken)).thenReturn(tenantId);
        when(joinPoint.proceed()).thenReturn("success");

        // Act
        Object result = aspect.setTenantContext(joinPoint);

        // Assert
        assertEquals("success", result);
        verify(jwtDecoder).extractTenantId(jwtToken);
        verify(jwtTokenService).setRabbitJwtToken(jwtToken);
        verify(jwtTokenService).setRabbitTenantId(tenantId);
    }

    @Test
    @DisplayName("Debe proceder sin contexto cuando no hay objeto Message en argumentos")
    void testSetTenantContextWithoutMessageArgument() throws Throwable {
        // Arrange
        when(joinPoint.getArgs()).thenReturn(new Object[]{"string", 123});
        when(joinPoint.proceed()).thenReturn("success");

        // Act
        Object result = aspect.setTenantContext(joinPoint);

        // Assert
        assertEquals("success", result);
        verify(joinPoint).proceed();
        verify(jwtDecoder, never()).extractTenantId(anyString());
        verify(jwtTokenService, never()).setRabbitJwtToken(anyString());
    }

    @Test
    @DisplayName("Debe proceder sin contexto cuando no hay header x-jwt-token")
    void testSetTenantContextWithoutJwtHeader() throws Throwable {
        // Arrange
        Message message = createMessageWithoutToken();
        when(joinPoint.getArgs()).thenReturn(new Object[]{message});
        when(joinPoint.proceed()).thenReturn("success");

        // Act
        Object result = aspect.setTenantContext(joinPoint);

        // Assert
        assertEquals("success", result);
        verify(joinPoint).proceed();
        verify(jwtDecoder, never()).extractTenantId(anyString());
    }

    @Test
    @DisplayName("Debe proceder sin contexto cuando tenant ID no se puede extraer")
    void testSetTenantContextWhenTenantIdCannotBeExtracted() throws Throwable {
        // Arrange
        String jwtToken = "invalid.jwt.token";
        Message message = createMessageWithToken(jwtToken);

        when(joinPoint.getArgs()).thenReturn(new Object[]{message});
        when(jwtDecoder.extractTenantId(jwtToken)).thenReturn(null);
        when(joinPoint.proceed()).thenReturn("success");

        // Act
        Object result = aspect.setTenantContext(joinPoint);

        // Assert
        assertEquals("success", result);
        verify(joinPoint).proceed();
        verify(jwtDecoder).extractTenantId(jwtToken);
        verify(jwtTokenService, never()).setRabbitJwtToken(anyString());
    }

    @Test
    @DisplayName("Debe limpiar contexto en bloque finally incluso si joinPoint lanza excepción")
    void testClearContextInFinallyBlockWhenJoinPointThrowsException() throws Throwable {
        // Arrange
        String jwtToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.test";
        String tenantId = "TENANT123";
        Message message = createMessageWithToken(jwtToken);

        when(joinPoint.getArgs()).thenReturn(new Object[]{message});
        when(jwtDecoder.extractTenantId(jwtToken)).thenReturn(tenantId);
        when(joinPoint.proceed()).thenThrow(new RuntimeException("Processing error"));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> aspect.setTenantContext(joinPoint));

        verify(jwtTokenService).setRabbitJwtToken(jwtToken);
        verify(jwtTokenService).setRabbitTenantId(tenantId);
        verify(jwtTokenService).clearRabbitContext();
    }

    @Test
    @DisplayName("Debe manejar múltiples argumentos donde uno es Message")
    void testSetTenantContextWithMultipleArgumentsIncludingMessage() throws Throwable {
        // Arrange
        String jwtToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.test";
        String tenantId = "TENANT123";
        Message message = createMessageWithToken(jwtToken);

        when(joinPoint.getArgs()).thenReturn(new Object[]{"string", message, 123});
        when(jwtDecoder.extractTenantId(jwtToken)).thenReturn(tenantId);
        when(joinPoint.proceed()).thenReturn("success");

        // Act
        Object result = aspect.setTenantContext(joinPoint);

        // Assert
        assertEquals("success", result);
        verify(jwtDecoder).extractTenantId(jwtToken);
        verify(jwtTokenService).setRabbitJwtToken(jwtToken);
    }

    @Test
    @DisplayName("Debe retornar resultado del joinPoint correctamente")
    void testSetTenantContextReturnsJoinPointResult() throws Throwable {
        // Arrange
        String jwtToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.test";
        String tenantId = "TENANT123";
        String expectedResult = "custom result";
        Message message = createMessageWithToken(jwtToken);

        when(joinPoint.getArgs()).thenReturn(new Object[]{message});
        when(jwtDecoder.extractTenantId(jwtToken)).thenReturn(tenantId);
        when(joinPoint.proceed()).thenReturn(expectedResult);

        // Act
        Object result = aspect.setTenantContext(joinPoint);

        // Assert
        assertEquals(expectedResult, result);
    }

    @Test
    @DisplayName("Debe limpiar contexto de TenantContext correctamente")
    void testTenantContextIsClearedInFinally() throws Throwable {
        // Arrange
        String jwtToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.test";
        String tenantId = "TENANT123";
        Message message = createMessageWithToken(jwtToken);

        when(joinPoint.getArgs()).thenReturn(new Object[]{message});
        when(jwtDecoder.extractTenantId(jwtToken)).thenReturn(tenantId);
        when(joinPoint.proceed()).thenReturn("success");

        // Act
        aspect.setTenantContext(joinPoint);

        // Assert
        assertNull(TenantContext.getTenantId());
    }
}
