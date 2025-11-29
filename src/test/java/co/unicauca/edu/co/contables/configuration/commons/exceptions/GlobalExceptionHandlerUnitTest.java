package co.unicauca.edu.co.contables.configuration.commons.exceptions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.mockito.Mockito;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.WebRequest;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class GlobalExceptionHandlerUnitTest {

    @InjectMocks
    private GlobalExceptionHandler globalExceptionHandler;

    private WebRequest webRequest;

    private static final String REQUEST_PATH = "/api/test";

    @BeforeEach
    void setUp() {
        webRequest = mock(WebRequest.class);
        when(webRequest.getDescription(false)).thenReturn("uri=" + REQUEST_PATH);
    }

    @Nested
    @DisplayName("Tests para handleBusinessExceptions")
    class HandleBusinessExceptionsTests {

        @Test
        @DisplayName("Debe manejar excepción de negocio con código NOT_FOUND")
        void testHandleBusinessExceptions_NotFoundError() {
            TestErrorCode errorCode = new TestErrorCode("RESOURCE_NOT_FOUND", "Recurso no encontrado");
            TestBusinessException exception = new TestBusinessException(errorCode);

            ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleBusinessExceptions(exception, webRequest);

            assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(404, response.getBody().getStatus());
            assertEquals("RESOURCE_NOT_FOUND", response.getBody().getCode());
            assertEquals("Recurso no encontrado", response.getBody().getMessage());
            assertEquals(REQUEST_PATH, response.getBody().getPath());
        }

        @Test
        @DisplayName("Debe manejar excepción de negocio con código ALREADY_EXISTS")
        void testHandleBusinessExceptions_ConflictError() {
            TestErrorCode errorCode = new TestErrorCode("ENTITY_ALREADY_EXISTS", "La entidad ya existe");
            TestBusinessException exception = new TestBusinessException(errorCode);

            ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleBusinessExceptions(exception, webRequest);

            assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(409, response.getBody().getStatus());
            assertEquals("ENTITY_ALREADY_EXISTS", response.getBody().getCode());
        }

        @Test
        @DisplayName("Debe manejar excepción de negocio con código DUPLICATE")
        void testHandleBusinessExceptions_DuplicateError() {
            TestErrorCode errorCode = new TestErrorCode("DUPLICATE_ENTRY", "Entrada duplicada");
            TestBusinessException exception = new TestBusinessException(errorCode);

            ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleBusinessExceptions(exception, webRequest);

            assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
            assertEquals(409, response.getBody().getStatus());
        }

        @Test
        @DisplayName("Debe manejar excepción de negocio con código ASSOCIATED")
        void testHandleBusinessExceptions_AssociatedError() {
            TestErrorCode errorCode = new TestErrorCode("RESOURCE_ASSOCIATED", "Recurso asociado");
            TestBusinessException exception = new TestBusinessException(errorCode);

            ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleBusinessExceptions(exception, webRequest);

            assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        }

        @Test
        @DisplayName("Debe manejar excepción de negocio con código genérico como BAD_REQUEST")
        void testHandleBusinessExceptions_GenericError() {
            TestErrorCode errorCode = new TestErrorCode("INVALID_DATA", "Datos inválidos");
            TestBusinessException exception = new TestBusinessException(errorCode);

            ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleBusinessExceptions(exception, webRequest);

            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
            assertEquals(400, response.getBody().getStatus());
        }

        @Test
        @DisplayName("Debe manejar excepción con mensaje personalizado")
        void testHandleBusinessExceptions_CustomMessage() {
            TestErrorCode errorCode = new TestErrorCode("VALIDATION_ERROR", "Error de validación");
            TestBusinessException exception = new TestBusinessException(errorCode, "Mensaje personalizado");

            ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleBusinessExceptions(exception, webRequest);

            assertEquals("Mensaje personalizado", response.getBody().getMessage());
        }

        @Test
        @DisplayName("Debe incluir timestamp en la respuesta")
        void testHandleBusinessExceptions_IncludesTimestamp() {
            TestErrorCode errorCode = new TestErrorCode("TEST_ERROR", "Error de prueba");
            TestBusinessException exception = new TestBusinessException(errorCode);

            ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleBusinessExceptions(exception, webRequest);

            assertNotNull(response.getBody().getTimestamp());
        }

        @Test
        @DisplayName("Debe manejar código de error null como BAD_REQUEST")
        void testHandleBusinessExceptions_NullCodeError() {
            TestErrorCode errorCode = new TestErrorCode(null, "Error sin código");
            TestBusinessException exception = new TestBusinessException(errorCode);

            ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleBusinessExceptions(exception, webRequest);

            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        }
    }

    @Nested
    @DisplayName("Tests para handleMethodArgumentNotValid")
    class HandleMethodArgumentNotValidTests {

        @Test
        @DisplayName("Debe manejar errores de validación de campos")
        void testHandleMethodArgumentNotValid_WithFieldErrors() {
            MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
            BindingResult bindingResult = mock(BindingResult.class);
            
            FieldError fieldError1 = new FieldError("object", "email", "El email es inválido");
            FieldError fieldError2 = new FieldError("object", "nombre", "El nombre es requerido");
            
            when(exception.getBindingResult()).thenReturn(bindingResult);
            when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError1, fieldError2));

            ResponseEntity<Object> response = globalExceptionHandler.handleMethodArgumentNotValid(exception, webRequest);

            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
            assertNotNull(response.getBody());
            assertTrue(response.getBody() instanceof Map);
            
            @SuppressWarnings("unchecked")
            Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
            assertTrue(responseBody.containsKey("error"));
            assertTrue(responseBody.containsKey("fieldErrors"));
            
            @SuppressWarnings("unchecked")
            Map<String, String> fieldErrors = (Map<String, String>) responseBody.get("fieldErrors");
            assertEquals("El email es inválido", fieldErrors.get("email"));
            assertEquals("El nombre es requerido", fieldErrors.get("nombre"));
        }

        @Test
        @DisplayName("Debe manejar lista vacía de errores de campo")
        void testHandleMethodArgumentNotValid_EmptyFieldErrors() {
            MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
            BindingResult bindingResult = mock(BindingResult.class);
            
            when(exception.getBindingResult()).thenReturn(bindingResult);
            when(bindingResult.getFieldErrors()).thenReturn(List.of());

            ResponseEntity<Object> response = globalExceptionHandler.handleMethodArgumentNotValid(exception, webRequest);

            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        }

        @Test
        @DisplayName("Debe incluir código de error genérico en la respuesta")
        void testHandleMethodArgumentNotValid_IncludesGenericErrorCode() {
            MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
            BindingResult bindingResult = mock(BindingResult.class);
            
            when(exception.getBindingResult()).thenReturn(bindingResult);
            when(bindingResult.getFieldErrors()).thenReturn(List.of());

            ResponseEntity<Object> response = globalExceptionHandler.handleMethodArgumentNotValid(exception, webRequest);

            @SuppressWarnings("unchecked")
            Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
            ErrorResponse errorResponse = (ErrorResponse) responseBody.get("error");
            assertEquals(ErrorCode.GENERIC_ERROR.getCode(), errorResponse.getCode());
        }
    }

    @Nested
    @DisplayName("Tests para handleConstraintViolation")
    class HandleConstraintViolationTests {

        @Test
        @DisplayName("Debe manejar violaciones de restricciones")
        void testHandleConstraintViolation_WithViolations() {
            ConstraintViolation<?> violation1 = createMockViolation("campo1", "El campo1 es inválido");
            ConstraintViolation<?> violation2 = createMockViolation("campo2", "El campo2 es requerido");
            
            Set<ConstraintViolation<?>> violations = new HashSet<>();
            violations.add(violation1);
            violations.add(violation2);
            
            ConstraintViolationException exception = new ConstraintViolationException(violations);

            ResponseEntity<Object> response = globalExceptionHandler.handleConstraintViolation(exception, webRequest);

            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
            assertNotNull(response.getBody());
            assertTrue(response.getBody() instanceof Map);
            
            @SuppressWarnings("unchecked")
            Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
            assertTrue(responseBody.containsKey("error"));
            assertTrue(responseBody.containsKey("violations"));
        }

        @Test
        @DisplayName("Debe manejar conjunto vacío de violaciones")
        void testHandleConstraintViolation_EmptyViolations() {
            Set<ConstraintViolation<?>> violations = new HashSet<>();
            ConstraintViolationException exception = new ConstraintViolationException(violations);

            ResponseEntity<Object> response = globalExceptionHandler.handleConstraintViolation(exception, webRequest);

            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        }

        @Test
        @DisplayName("Debe incluir path en la respuesta de error")
        void testHandleConstraintViolation_IncludesPath() {
            Set<ConstraintViolation<?>> violations = new HashSet<>();
            ConstraintViolationException exception = new ConstraintViolationException(violations);

            ResponseEntity<Object> response = globalExceptionHandler.handleConstraintViolation(exception, webRequest);

            @SuppressWarnings("unchecked")
            Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
            ErrorResponse errorResponse = (ErrorResponse) responseBody.get("error");
            assertEquals(REQUEST_PATH, errorResponse.getPath());
        }
    }

    @Nested
    @DisplayName("Tests para handleHttpMessageNotReadable")
    class HandleHttpMessageNotReadableTests {

        @Test
        @DisplayName("Debe manejar error de mensaje HTTP no legible genérico")
        void testHandleHttpMessageNotReadable_GenericError() {
            HttpMessageNotReadableException exception = new HttpMessageNotReadableException("Error de parsing", (Throwable) null, null);

            ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleHttpMessageNotReadable(exception, webRequest);

            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals("Error en el formato de los datos enviados", response.getBody().getMessage());
        }

        @Test
        @DisplayName("Debe manejar error de formato inválido con detalles específicos")
        void testHandleHttpMessageNotReadable_InvalidFormatException() {
            InvalidFormatException invalidFormatException = mock(InvalidFormatException.class);
            JsonMappingException.Reference reference = mock(JsonMappingException.Reference.class);
            
            when(reference.getFieldName()).thenReturn("edad");
            when(invalidFormatException.getPath()).thenReturn(List.of(reference));
            Mockito.doReturn(Integer.class).when(invalidFormatException).getTargetType();
            when(invalidFormatException.getValue()).thenReturn("abc");
            
            HttpMessageNotReadableException exception = new HttpMessageNotReadableException("Error", invalidFormatException, null);

            ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleHttpMessageNotReadable(exception, webRequest);

            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
            assertTrue(response.getBody().getMessage().contains("edad"));
            assertTrue(response.getBody().getMessage().contains("Integer"));
            assertTrue(response.getBody().getMessage().contains("abc"));
        }

        @Test
        @DisplayName("Debe manejar error de formato inválido con path vacío")
        void testHandleHttpMessageNotReadable_EmptyPath() {
            InvalidFormatException invalidFormatException = mock(InvalidFormatException.class);
            
            when(invalidFormatException.getPath()).thenReturn(List.of());
            Mockito.doReturn(String.class).when(invalidFormatException).getTargetType();
            when(invalidFormatException.getValue()).thenReturn("valor");
            
            HttpMessageNotReadableException exception = new HttpMessageNotReadableException("Error", invalidFormatException, null);

            ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleHttpMessageNotReadable(exception, webRequest);

            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
            assertTrue(response.getBody().getMessage().contains("campo"));
        }

        @Test
        @DisplayName("Debe incluir código de error genérico")
        void testHandleHttpMessageNotReadable_IncludesGenericErrorCode() {
            HttpMessageNotReadableException exception = new HttpMessageNotReadableException("Error", (Throwable) null, null);

            ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleHttpMessageNotReadable(exception, webRequest);

            assertEquals(ErrorCode.GENERIC_ERROR.getCode(), response.getBody().getCode());
        }
    }

    @Nested
    @DisplayName("Tests para handleGenericException")
    class HandleGenericExceptionTests {

        @Test
        @DisplayName("Debe manejar excepción genérica como error interno del servidor")
        void testHandleGenericException_ReturnsInternalServerError() {
            Exception exception = new RuntimeException("Error inesperado");

            ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleGenericException(exception, webRequest);

            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
            assertEquals(500, response.getBody().getStatus());
            assertEquals("Internal Server Error", response.getBody().getError());
        }

        @Test
        @DisplayName("Debe ocultar mensaje de excepción por seguridad")
        void testHandleGenericException_HidesExceptionMessage() {
            Exception exception = new RuntimeException("Detalles sensibles del error");

            ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleGenericException(exception, webRequest);

            assertEquals("Ha ocurrido un error interno del servidor", response.getBody().getMessage());
        }

        @Test
        @DisplayName("Debe incluir código de error genérico")
        void testHandleGenericException_IncludesGenericErrorCode() {
            Exception exception = new Exception("Error");

            ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleGenericException(exception, webRequest);

            assertEquals(ErrorCode.GENERIC_ERROR.getCode(), response.getBody().getCode());
        }

        @Test
        @DisplayName("Debe incluir path en la respuesta")
        void testHandleGenericException_IncludesPath() {
            Exception exception = new Exception("Error");

            ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleGenericException(exception, webRequest);

            assertEquals(REQUEST_PATH, response.getBody().getPath());
        }

        @Test
        @DisplayName("Debe incluir timestamp en la respuesta")
        void testHandleGenericException_IncludesTimestamp() {
            Exception exception = new Exception("Error");

            ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleGenericException(exception, webRequest);

            assertNotNull(response.getBody().getTimestamp());
        }
    }

    @Nested
    @DisplayName("Tests para mapStatusFromErrorCode")
    class MapStatusFromErrorCodeTests {

        @Test
        @DisplayName("Debe mapear código terminado en _NOT_FOUND a NOT_FOUND")
        void testMapStatus_EndsWithNotFound() {
            TestErrorCode errorCode = new TestErrorCode("USER_NOT_FOUND", "Usuario no encontrado");
            TestBusinessException exception = new TestBusinessException(errorCode);

            ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleBusinessExceptions(exception, webRequest);

            assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        }

        @Test
        @DisplayName("Debe mapear código que contiene NOT_FOUND a NOT_FOUND")
        void testMapStatus_ContainsNotFound() {
            TestErrorCode errorCode = new TestErrorCode("NOT_FOUND_ERROR", "No encontrado");
            TestBusinessException exception = new TestBusinessException(errorCode);

            ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleBusinessExceptions(exception, webRequest);

            assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        }

        @Test
        @DisplayName("Debe mapear código terminado en _ALREADY_EXISTS a CONFLICT")
        void testMapStatus_EndsWithAlreadyExists() {
            TestErrorCode errorCode = new TestErrorCode("EMAIL_ALREADY_EXISTS", "Email ya existe");
            TestBusinessException exception = new TestBusinessException(errorCode);

            ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleBusinessExceptions(exception, webRequest);

            assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        }

        @Test
        @DisplayName("Debe mapear código que contiene DUPLICATE a CONFLICT")
        void testMapStatus_ContainsDuplicate() {
            TestErrorCode errorCode = new TestErrorCode("DUPLICATE_KEY_ERROR", "Clave duplicada");
            TestBusinessException exception = new TestBusinessException(errorCode);

            ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleBusinessExceptions(exception, webRequest);

            assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        }

        @Test
        @DisplayName("Debe mapear código que contiene ASSOCIATED a CONFLICT")
        void testMapStatus_ContainsAssociated() {
            TestErrorCode errorCode = new TestErrorCode("ENTITY_ASSOCIATED_ERROR", "Entidad asociada");
            TestBusinessException exception = new TestBusinessException(errorCode);

            ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleBusinessExceptions(exception, webRequest);

            assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        }

        @Test
        @DisplayName("Debe mapear código sin patrón conocido a BAD_REQUEST")
        void testMapStatus_UnknownPattern() {
            TestErrorCode errorCode = new TestErrorCode("SOME_RANDOM_ERROR", "Error aleatorio");
            TestBusinessException exception = new TestBusinessException(errorCode);

            ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleBusinessExceptions(exception, webRequest);

            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        }

        @Test
        @DisplayName("Debe ser case-insensitive al mapear códigos")
        void testMapStatus_CaseInsensitive() {
            TestErrorCode errorCode = new TestErrorCode("resource_not_found", "No encontrado");
            TestBusinessException exception = new TestBusinessException(errorCode);

            ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleBusinessExceptions(exception, webRequest);

            assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        }
    }

    private ConstraintViolation<?> createMockViolation(String propertyPath, String message) {
        ConstraintViolation<?> violation = mock(ConstraintViolation.class);
        Path path = mock(Path.class);
        when(path.toString()).thenReturn(propertyPath);
        when(violation.getPropertyPath()).thenReturn(path);
        when(violation.getMessage()).thenReturn(message);
        return violation;
    }

    private static class TestErrorCode implements ErrorCodeDefinition {
        private final String code;
        private final String message;

        TestErrorCode(String code, String message) {
            this.code = code;
            this.message = message;
        }

        @Override
        public String getCode() {
            return code;
        }

        @Override
        public String getMessage() {
            return message;
        }
    }

    private static class TestBusinessException extends BaseBusinessException {
        TestBusinessException(ErrorCodeDefinition errorCode) {
            super(errorCode);
        }

        TestBusinessException(ErrorCodeDefinition errorCode, String customMessage) {
            super(errorCode, customMessage);
        }
    }
}
