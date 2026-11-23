package co.unicauca.edu.co.contables.auxiliary.unit.infrastructure.exception;

import co.unicauca.edu.co.contables.auxiliary.infrastructure.out.exception.customized.BusinessRuleException;
import co.unicauca.edu.co.contables.auxiliary.infrastructure.out.exception.customized.EntityAlreadyExists;
import co.unicauca.edu.co.contables.auxiliary.infrastructure.out.exception.customized.EntityDoesNotExistException;
import co.unicauca.edu.co.contables.auxiliary.infrastructure.out.exception.dto.ErrorResponseDTO;
import co.unicauca.edu.co.contables.auxiliary.infrastructure.out.exception.handler.AuxiliaryGlobalExceptionHandler;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.springframework.http.HttpMethod;

import java.util.Map;

/**
 * @brief Pruebas unitarias para {@link AuxiliaryGlobalExceptionHandler}.
 */
class GlobalExceptionHandlerTest {

    private AuxiliaryGlobalExceptionHandler handler;
    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        handler = new AuxiliaryGlobalExceptionHandler();
        request = Mockito.mock(HttpServletRequest.class);
        Mockito.when(request.getRequestURI()).thenReturn("/api/v1/test");
        Mockito.when(request.getMethod()).thenReturn("GET");
    }

    @Test
    @DisplayName("handleValidationExceptions debe retornar 400 con los errores de los campos")
    void handleValidationExceptions() throws NoSuchMethodException {
        BeanPropertyBindingResult binding = new BeanPropertyBindingResult(new Object(), "target");
        binding.addError(new FieldError("target", "name", "must not be empty"));

        MethodParameter methodParameter = new MethodParameter(
                GlobalExceptionHandlerTest.class.getDeclaredMethod("dummyForMethodParameter"), -1);
        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(methodParameter, binding);

        ResponseEntity<Map<String, String>> response = handler.handleValidationExceptions(ex);

        Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        Assertions.assertThat(response.getBody()).containsEntry("name", "must not be empty");
    }

    @Test
    @DisplayName("handleCustomExceptions debe usar status y message de la BaseException (BusinessRule)")
    void handleCustomBusinessRule() {
        BusinessRuleException ex = new BusinessRuleException(422, "regla violada");

        ResponseEntity<ErrorResponseDTO> response = handler.handleCustomExceptions(request, ex);

        Assertions.assertThat(response.getStatusCode().value()).isEqualTo(422);
        Assertions.assertThat(response.getBody().getMessage()).isEqualTo("regla violada");
        Assertions.assertThat(response.getBody().getUrl()).isEqualTo("/api/v1/test");
        Assertions.assertThat(response.getBody().getMethod()).isEqualTo("GET");
    }

    @Test
    @DisplayName("handleCustomExceptions debe propagar status de EntityAlreadyExists")
    void handleCustomEntityAlreadyExists() {
        EntityAlreadyExists ex = new EntityAlreadyExists(409, "duplicado");

        ResponseEntity<ErrorResponseDTO> response = handler.handleCustomExceptions(request, ex);

        Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        Assertions.assertThat(response.getBody().getMessage()).isEqualTo("duplicado");
    }

    @Test
    @DisplayName("handleCustomExceptions debe propagar status de EntityDoesNotExistException")
    void handleCustomEntityDoesNotExist() {
        EntityDoesNotExistException ex = new EntityDoesNotExistException(404, "no existe");

        ResponseEntity<ErrorResponseDTO> response = handler.handleCustomExceptions(request, ex);

        Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        Assertions.assertThat(response.getBody().getMessage()).isEqualTo("no existe");
    }

    @Test
    @DisplayName("handleBadRequestExceptions debe retornar 404 para NoResourceFoundException")
    void handleNoResourceFound() {
        NoResourceFoundException ex = new NoResourceFoundException(HttpMethod.GET, "/missing");

        ResponseEntity<ErrorResponseDTO> response = handler.handleBadRequestExceptions(request, ex);

        Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        Assertions.assertThat(response.getBody().getMessage()).isEqualTo("Resource not found");
    }

    @Test
    @DisplayName("handleBadRequestExceptions debe retornar 400 para MissingServletRequestParameterException")
    void handleMissingParameter() {
        MissingServletRequestParameterException ex = new MissingServletRequestParameterException("entId", "String");

        ResponseEntity<ErrorResponseDTO> response = handler.handleBadRequestExceptions(request, ex);

        Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        Assertions.assertThat(response.getBody().getMessage()).isEqualTo("Missing parameter: entId");
    }

    @Test
    @DisplayName("handleBadRequestExceptions debe retornar 400 para MethodArgumentTypeMismatchException")
    void handleTypeMismatch() {
        MethodArgumentTypeMismatchException ex = new MethodArgumentTypeMismatchException(
                "abc", Integer.class, "age", null, new IllegalArgumentException());

        ResponseEntity<ErrorResponseDTO> response = handler.handleBadRequestExceptions(request, ex);

        Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        Assertions.assertThat(response.getBody().getMessage()).isEqualTo("Method argument type mismatch for 'age'");
    }

    @Test
    @DisplayName("handleBadRequestExceptions debe retornar 400 para IllegalArgumentException con su mensaje")
    void handleIllegalArgument() {
        IllegalArgumentException ex = new IllegalArgumentException("argumento inválido");

        ResponseEntity<ErrorResponseDTO> response = handler.handleBadRequestExceptions(request, ex);

        Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        Assertions.assertThat(response.getBody().getMessage()).isEqualTo("argumento inválido");
    }

    @Test
    @DisplayName("handleBadRequestExceptions debe retornar 400 para EntityNotFoundException con su mensaje")
    void handleEntityNotFound() {
        EntityNotFoundException ex = new EntityNotFoundException("not found");

        ResponseEntity<ErrorResponseDTO> response = handler.handleBadRequestExceptions(request, ex);

        Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        Assertions.assertThat(response.getBody().getMessage()).isEqualTo("not found");
    }

    @Test
    @DisplayName("handleRuntimeException debe retornar 500 con mensaje Internal server error")
    void handleRuntimeException() {
        RuntimeException ex = new RuntimeException("cualquier cosa");

        ResponseEntity<ErrorResponseDTO> response = handler.handleRuntimeException(request, ex);

        Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        Assertions.assertThat(response.getBody().getMessage()).isEqualTo("Internal server error");
    }

    @SuppressWarnings("unused")
    private void dummyForMethodParameter() {
        // no-op: usado sólo para construir MethodParameter en pruebas de validación
    }
}
