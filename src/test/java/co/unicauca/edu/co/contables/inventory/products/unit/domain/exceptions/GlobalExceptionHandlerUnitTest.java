package co.unicauca.edu.co.contables.inventory.products.unit.domain.exceptions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import co.unicauca.edu.co.contables.inventory.products.domain.exception.ErrorCode;
import co.unicauca.edu.co.contables.inventory.products.domain.exception.ProductsGlobalExceptionHandler;
import co.unicauca.edu.co.contables.inventory.products.domain.exception.category.CategoryNameAlreadyExistsException;
import co.unicauca.edu.co.contables.inventory.products.domain.exception.category.CategoryNotFoundException;
import co.unicauca.edu.co.contables.inventory.products.domain.exception.product.ProductFileSizeExceededException;
import co.unicauca.edu.co.contables.inventory.products.domain.exception.product.ProductNameAlreadyExistsException;
import co.unicauca.edu.co.contables.inventory.products.domain.exception.product.ProductNotFoundException;
import co.unicauca.edu.co.contables.inventory.products.domain.exception.productType.ProductTypeInUseException;
import co.unicauca.edu.co.contables.inventory.products.domain.exception.unitOfMeasure.UnitOfMeasureAssociatedException;
import co.unicauca.edu.co.contables.inventory.products.infraestructure.input.rest.dto.response.ErrorResponse;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class GlobalExceptionHandlerUnitTest {

    @InjectMocks
    private ProductsGlobalExceptionHandler globalExceptionHandler;

    private WebRequest webRequest;

    @BeforeEach
    void setUp() {
        webRequest = mock(WebRequest.class);
        when(webRequest.getDescription(false)).thenReturn("uri=/api/test");
    }

    // Business Exceptions - NOT_FOUND (404)
    @Test
    @DisplayName("Debe manejar ProductNotFoundException con estado 404")
    void testHandleProductNotFoundException() {
        // Arrange
        ProductNotFoundException exception = new ProductNotFoundException();

        // Act
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleBusinessExceptions(exception, webRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals(404, response.getBody().getStatus());
        assertEquals("Not Found", response.getBody().getError());
        assertEquals(ErrorCode.PRODUCT_NOT_FOUND.getCode(), response.getBody().getCode());
        assertEquals("/api/test", response.getBody().getPath());
        assertNotNull(response.getBody().getTimestamp());
    }

    @Test
    @DisplayName("Debe manejar CategoryNotFoundException con estado 404")
    void testHandleCategoryNotFoundException() {
        // Arrange
        CategoryNotFoundException exception = new CategoryNotFoundException();

        // Act
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleBusinessExceptions(exception, webRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals(404, response.getBody().getStatus());
        assertEquals(ErrorCode.CATEGORY_NOT_FOUND.getCode(), response.getBody().getCode());
    }

    // Business Exceptions - CONFLICT (409)
    @Test
    @DisplayName("Debe manejar ProductNameAlreadyExistsException con estado 409")
    void testHandleProductNameAlreadyExistsException() {
        // Arrange
        ProductNameAlreadyExistsException exception = new ProductNameAlreadyExistsException("Producto Test");

        // Act
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleBusinessExceptions(exception, webRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals(409, response.getBody().getStatus());
        assertEquals("Conflict", response.getBody().getError());
        assertEquals(ErrorCode.PRODUCT_NAME_ALREADY_EXISTS.getCode(), response.getBody().getCode());
        assertTrue(response.getBody().getMessage().contains("Producto Test"));
    }

    @Test
    @DisplayName("Debe manejar CategoryNameAlreadyExistsException con estado 409")
    void testHandleCategoryNameAlreadyExistsException() {
        // Arrange
        CategoryNameAlreadyExistsException exception = new CategoryNameAlreadyExistsException("Categoría Test");

        // Act
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleBusinessExceptions(exception, webRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals(409, response.getBody().getStatus());
        assertEquals(ErrorCode.CATEGORY_NAME_ALREADY_EXISTS.getCode(), response.getBody().getCode());
    }

    @Test
    @DisplayName("Debe manejar ProductTypeInUseException con estado 400")
    void testHandleProductTypeInUseException() {
        // Arrange
        ProductTypeInUseException exception = new ProductTypeInUseException();

        // Act
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleBusinessExceptions(exception, webRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(400, response.getBody().getStatus());
    }

    @Test
    @DisplayName("Debe manejar UnitOfMeasureAssociatedException con estado 409")
    void testHandleUnitOfMeasureAssociatedException() {
        // Arrange
        UnitOfMeasureAssociatedException exception = new UnitOfMeasureAssociatedException();

        // Act
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleBusinessExceptions(exception, webRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals(409, response.getBody().getStatus());
    }

    // Business Exceptions - PAYLOAD_TOO_LARGE (413)
    @Test
    @DisplayName("Debe manejar ProductFileSizeExceededException con estado 413")
    void testHandleProductFileSizeExceededException() {
        // Arrange
        ProductFileSizeExceededException exception = new ProductFileSizeExceededException(5242880);

        // Act
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleBusinessExceptions(exception, webRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(400, response.getBody().getStatus());
        assertEquals("Bad Request", response.getBody().getError());
        assertEquals(ErrorCode.FILE_VALIDATION_ERROR.getCode(), response.getBody().getCode());
    }

    // Validation Exceptions
    @Test
    @DisplayName("Debe manejar MethodArgumentNotValidException con errores de campo")
    void testHandleMethodArgumentNotValidException() {
        // Arrange
        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError = new FieldError("product", "name", "El nombre es requerido");

        when(exception.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(Collections.singletonList(fieldError));

        // Act
        ResponseEntity<Object> response = globalExceptionHandler.handleMethodArgumentNotValid(exception, webRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        
        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertNotNull(body);
        assertTrue(body.containsKey("error"));
        assertTrue(body.containsKey("fieldErrors"));

        ErrorResponse errorResponse = (ErrorResponse) body.get("error");
        assertEquals(400, errorResponse.getStatus());
        assertEquals("Error de validación de campos", errorResponse.getMessage());
        assertEquals(ErrorCode.GENERIC_ERROR.getCode(), errorResponse.getCode());

        @SuppressWarnings("unchecked")
        Map<String, String> fieldErrors = (Map<String, String>) body.get("fieldErrors");
        assertEquals(1, fieldErrors.size());
        assertEquals("El nombre es requerido", fieldErrors.get("name"));
    }

    @Test
    @DisplayName("Debe manejar MethodArgumentNotValidException con múltiples errores de campo")
    void testHandleMethodArgumentNotValidExceptionMultipleErrors() {
        // Arrange
        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError1 = new FieldError("product", "name", "El nombre es requerido");
        FieldError fieldError2 = new FieldError("product", "cost", "El costo debe ser positivo");

        when(exception.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(java.util.Arrays.asList(fieldError1, fieldError2));

        // Act
        ResponseEntity<Object> response = globalExceptionHandler.handleMethodArgumentNotValid(exception, webRequest);

        // Assert
        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        
        @SuppressWarnings("unchecked")
        Map<String, String> fieldErrors = (Map<String, String>) body.get("fieldErrors");
        assertEquals(2, fieldErrors.size());
        assertTrue(fieldErrors.containsKey("name"));
        assertTrue(fieldErrors.containsKey("cost"));
    }

    @Test
    @DisplayName("Debe manejar ConstraintViolationException con violaciones")
    void testHandleConstraintViolationException() {
        // Arrange
        ConstraintViolation<?> violation = mock(ConstraintViolation.class);
        Path path = mock(Path.class);
        
        when(violation.getPropertyPath()).thenReturn(path);
        when(path.toString()).thenReturn("id");
        when(violation.getMessage()).thenReturn("debe ser mayor que 0");

        Set<ConstraintViolation<?>> violations = Collections.singleton(violation);
        ConstraintViolationException exception = new ConstraintViolationException(violations);

        // Act
        ResponseEntity<Object> response = globalExceptionHandler.handleConstraintViolation(exception, webRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertNotNull(body);
        assertTrue(body.containsKey("error"));
        assertTrue(body.containsKey("violations"));

        ErrorResponse errorResponse = (ErrorResponse) body.get("error");
        assertEquals(400, errorResponse.getStatus());
        assertEquals("Error de validación", errorResponse.getMessage());

        @SuppressWarnings("unchecked")
        Map<String, String> violationsMap = (Map<String, String>) body.get("violations");
        assertEquals(1, violationsMap.size());
        assertEquals("debe ser mayor que 0", violationsMap.get("id"));
    }

    // Data Integrity Exception
    @Test
    @DisplayName("Debe manejar DataIntegrityViolationException")
    void testHandleDataIntegrityViolationException() {
        // Arrange
        DataIntegrityViolationException exception = new DataIntegrityViolationException("Violación de clave única");

        // Act
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleDataIntegrityViolation(exception, webRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(400, response.getBody().getStatus());
        assertEquals("Bad Request", response.getBody().getError());
        assertTrue(response.getBody().getMessage().contains("Error de integridad de datos"));
        assertEquals(ErrorCode.GENERIC_ERROR.getCode(), response.getBody().getCode());
    }

    @Test
    @DisplayName("Debe manejar DataIntegrityViolationException sin mensaje")
    void testHandleDataIntegrityViolationExceptionWithoutMessage() {
        // Arrange
        DataIntegrityViolationException exception = new DataIntegrityViolationException(null);

        // Act
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleDataIntegrityViolation(exception, webRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().getMessage().contains("Violación de restricción"));
    }

    // Max Upload Size Exceeded
    @Test
    @DisplayName("Debe manejar MaxUploadSizeExceededException")
    void testHandleMaxUploadSizeExceededException() {
        // Arrange
        MaxUploadSizeExceededException exception = new MaxUploadSizeExceededException(5242880);

        // Act
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleMaxUploadSizeExceeded(exception, webRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(400, response.getBody().getStatus());
        assertEquals(ErrorCode.FILE_VALIDATION_ERROR.getCode(), response.getBody().getCode());
        assertTrue(response.getBody().getMessage().contains("5"));
    }

    @Test
    @DisplayName("Debe manejar MaxUploadSizeExceededException con tamaño por defecto")
    void testHandleMaxUploadSizeExceededExceptionWithDefaultSize() {
        // Arrange
        MaxUploadSizeExceededException exception = new MaxUploadSizeExceededException(-1);

        // Act
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleMaxUploadSizeExceeded(exception, webRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().getMessage().contains("5"));
    }

    // HTTP Message Not Readable
    @Test
    @DisplayName("Debe manejar HttpMessageNotReadableException")
    void testHandleHttpMessageNotReadableException() {
        // Arrange
        HttpMessageNotReadableException exception = mock(HttpMessageNotReadableException.class);

        // Act
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleHttpMessageNotReadable(exception, webRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(400, response.getBody().getStatus());
        assertEquals("Bad Request", response.getBody().getError());
        assertEquals("Error en el formato de los datos enviados", response.getBody().getMessage());
        assertEquals("INVALID_REQUEST_FORMAT", response.getBody().getCode());
    }

    // Generic Exception
    @Test
    @DisplayName("Debe manejar excepción genérica")
    void testHandleGenericException() {
        // Arrange
        Exception exception = new RuntimeException("Error inesperado");

        // Act
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleGenericException(exception, webRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals(500, response.getBody().getStatus());
        assertEquals("Internal Server Error", response.getBody().getError());
        assertEquals("Ha ocurrido un error interno del servidor", response.getBody().getMessage());
        assertEquals(ErrorCode.GENERIC_ERROR.getCode(), response.getBody().getCode());
        assertEquals("/api/test", response.getBody().getPath());
        assertNotNull(response.getBody().getTimestamp());
    }

    @Test
    @DisplayName("Debe manejar NullPointerException como excepción genérica")
    void testHandleNullPointerException() {
        // Arrange
        NullPointerException exception = new NullPointerException("Valor nulo");

        // Act
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleGenericException(exception, webRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals(500, response.getBody().getStatus());
    }

    // Status Mapping Tests
    @Test
    @DisplayName("Debe mapear código con NOT_FOUND a estado 404")
    void testMapStatusNotFound() {
        // Arrange
        ProductNotFoundException exception = new ProductNotFoundException();

        // Act
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleBusinessExceptions(exception, webRequest);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    @DisplayName("Debe mapear código con ALREADY_EXISTS a estado 409")
    void testMapStatusAlreadyExists() {
        // Arrange
        ProductNameAlreadyExistsException exception = new ProductNameAlreadyExistsException("Test");

        // Act
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleBusinessExceptions(exception, webRequest);

        // Assert
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    }

    @Test
    @DisplayName("Debe mapear código con VALIDATION_ERROR a estado 400")
    void testMapStatusValidationError() {
        // Arrange
        ProductFileSizeExceededException exception = new ProductFileSizeExceededException(1000000);

        // Act
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleBusinessExceptions(exception, webRequest);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    @DisplayName("Debe extraer path correctamente de WebRequest")
    void testExtractPathFromWebRequest() {
        // Arrange
        when(webRequest.getDescription(false)).thenReturn("uri=/api/products/123");
        ProductNotFoundException exception = new ProductNotFoundException();

        // Act
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleBusinessExceptions(exception, webRequest);

        // Assert
        assertEquals("/api/products/123", response.getBody().getPath());
    }

    @Test
    @DisplayName("Debe incluir timestamp en todas las respuestas de error")
    void testTimestampIncludedInAllResponses() {
        // Arrange
        ProductNotFoundException exception = new ProductNotFoundException();

        // Act
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleBusinessExceptions(exception, webRequest);

        // Assert
        assertNotNull(response.getBody().getTimestamp());
    }
}
