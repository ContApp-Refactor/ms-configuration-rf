package co.unicauca.edu.co.contables.inventory.peps.unit.infrastucture.output.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import co.unicauca.edu.co.contables.inventory.stock.infrastructure.adapters.output.exception.FormatterResultOutputPort;
import co.unicauca.edu.co.contables.inventory.stock.infrastructure.adapters.output.exception.customized.BusinessRuleException;
import co.unicauca.edu.co.contables.inventory.stock.infrastructure.adapters.output.exception.customized.EntityAlreadyExists;
import co.unicauca.edu.co.contables.inventory.stock.infrastructure.adapters.output.exception.customized.EntityDoesNotExistException;
import co.unicauca.edu.co.contables.inventory.stock.infrastructure.adapters.output.exception.customized.GenericErrorException;


public class FormatterResultOutputPortUnitTest {
    
     private final FormatterResultOutputPort formatter = new FormatterResultOutputPort();

    // ==================== Tests para returnBusinessRuleErrorResponse ====================

    @Test
    @DisplayName("returnBusinessRuleErrorResponse - Lanza BusinessRuleException con status y mensaje")
    void returnBusinessRuleErrorResponse_ThrowsBusinessRuleExceptionWithStatusAndMessage() {
        // Arrange
        int status = HttpStatus.CONFLICT.value();
        String message = "Invalid operation";

        // Act & Assert
        BusinessRuleException exception = assertThrows(
            BusinessRuleException.class,
            () -> formatter.returnBusinessRuleErrorResponse(status, message)
        );

        assertEquals(status, exception.getStatus());
        assertTrue(exception.getMessage().contains("GC-004: Business rule violation ->"));
        assertTrue(exception.getMessage().contains(message));
    }

    @Test
    @DisplayName("returnBusinessRuleErrorResponse - Con mensaje vacío incluye código de error")
    void returnBusinessRuleErrorResponse_WithEmptyMessage_IncludesErrorCode() {
        // Arrange
        int status = HttpStatus.BAD_REQUEST.value();
        String message = "";

        // Act & Assert
        BusinessRuleException exception = assertThrows(
            BusinessRuleException.class,
            () -> formatter.returnBusinessRuleErrorResponse(status, message)
        );

        assertTrue(exception.getMessage().contains("GC-004"));
    }

    // ==================== Tests para returnEntityAlreadyExistsErrorResponse ====================

    @Test
    @DisplayName("returnEntityAlreadyExistsErrorResponse - Lanza EntityAlreadyExists con status y mensaje")
    void returnEntityAlreadyExistsErrorResponse_ThrowsEntityAlreadyExistsWithStatusAndMessage() {
        // Arrange
        int status = HttpStatus.CONFLICT.value();
        String message = "Product already exists";

        // Act & Assert
        EntityAlreadyExists exception = assertThrows(
            EntityAlreadyExists.class,
            () -> formatter.returnEntityAlreadyExistsErrorResponse(status, message)
        );

        assertEquals(status, exception.getStatus());
        assertTrue(exception.getMessage().contains("GC-002: Entity already exists ->"));
        assertTrue(exception.getMessage().contains(message));
    }

    @Test
    @DisplayName("returnEntityAlreadyExistsErrorResponse - Con mensaje complejo concatena correctamente")
    void returnEntityAlreadyExistsErrorResponse_WithComplexMessage_ConcatenatesCorrectly() {
        // Arrange
        int status = HttpStatus.CONFLICT.value();
        String message = "Entity with id=123 and name='test'";

        // Act & Assert
        EntityAlreadyExists exception = assertThrows(
            EntityAlreadyExists.class,
            () -> formatter.returnEntityAlreadyExistsErrorResponse(status, message)
        );

        assertTrue(exception.getMessage().contains("GC-002"));
        assertTrue(exception.getMessage().contains("id=123"));
    }

    // ==================== Tests para returnEntityDoesNotExistErrorResponse ====================

    @Test
    @DisplayName("returnEntityDoesNotExistErrorResponse - Lanza EntityDoesNotExistException con status y mensaje")
    void returnEntityDoesNotExistErrorResponse_ThrowsEntityDoesNotExistExceptionWithStatusAndMessage() {
        // Arrange
        int status = HttpStatus.NOT_FOUND.value();
        String message = "Product not found";

        // Act & Assert
        EntityDoesNotExistException exception = assertThrows(
            EntityDoesNotExistException.class,
            () -> formatter.returnEntityDoesNotExistErrorResponse(status, message)
        );

        assertEquals(status, exception.getStatus());
        assertTrue(exception.getMessage().contains("GC-003: Entity not found ->"));
        assertTrue(exception.getMessage().contains(message));
    }

    @Test
    @DisplayName("returnEntityDoesNotExistErrorResponse - Con diferentes status mantiene el código")
    void returnEntityDoesNotExistErrorResponse_WithDifferentStatus_MaintainsCode() {
        // Arrange
        int status = HttpStatus.GONE.value();
        String message = "Resource deleted";

        // Act & Assert
        EntityDoesNotExistException exception = assertThrows(
            EntityDoesNotExistException.class,
            () -> formatter.returnEntityDoesNotExistErrorResponse(status, message)
        );

        assertEquals(status, exception.getStatus());
        assertTrue(exception.getMessage().contains("GC-003"));
    }

    // ==================== Tests para returnErrorGenericResponse ====================

    @Test
    @DisplayName("returnErrorGenericResponse - Lanza GenericErrorException con status y mensaje")
    void returnErrorGenericResponse_ThrowsGenericErrorExceptionWithStatusAndMessage() {
        // Arrange
        int status = HttpStatus.INTERNAL_SERVER_ERROR.value();
        String message = "Unexpected error occurred";

        // Act & Assert
        GenericErrorException exception = assertThrows(
            GenericErrorException.class,
            () -> formatter.returnErrorGenericResponse(status, message)
        );

        assertEquals(status, exception.getStatus());
        assertTrue(exception.getMessage().contains("GC-001: Generic error ->"));
        assertTrue(exception.getMessage().contains(message));
    }

    @Test
    @DisplayName("returnErrorGenericResponse - Con mensaje null concatena correctamente")
    void returnErrorGenericResponse_WithNullMessage_ConcatenatesCorrectly() {
        // Arrange
        int status = HttpStatus.INTERNAL_SERVER_ERROR.value();
        String message = null;

        // Act & Assert
        GenericErrorException exception = assertThrows(
            GenericErrorException.class,
            () -> formatter.returnErrorGenericResponse(status, message)
        );

        assertTrue(exception.getMessage().contains("GC-001"));
    }

    // ==================== Tests de diferentes status codes ====================

    @Test
    @DisplayName("Todos los métodos - Preservan códigos de status personalizados")
    void allMethods_PreserveCustomStatusCodes() {
        // Arrange
        int customStatus = 418; // I'm a teapot

        // Act & Assert
        BusinessRuleException ex1 = assertThrows(
            BusinessRuleException.class,
            () -> formatter.returnBusinessRuleErrorResponse(customStatus, "test")
        );
        assertEquals(customStatus, ex1.getStatus());

        EntityAlreadyExists ex2 = assertThrows(
            EntityAlreadyExists.class,
            () -> formatter.returnEntityAlreadyExistsErrorResponse(customStatus, "test")
        );
        assertEquals(customStatus, ex2.getStatus());

        EntityDoesNotExistException ex3 = assertThrows(
            EntityDoesNotExistException.class,
            () -> formatter.returnEntityDoesNotExistErrorResponse(customStatus, "test")
        );
        assertEquals(customStatus, ex3.getStatus());

        GenericErrorException ex4 = assertThrows(
            GenericErrorException.class,
            () -> formatter.returnErrorGenericResponse(customStatus, "test")
        );
        assertEquals(customStatus, ex4.getStatus());
    }

    // ==================== Tests de formato de mensaje ====================

    @Test
    @DisplayName("Todos los métodos - Concatenan código de error con mensaje personalizado")
    void allMethods_ConcatenateErrorCodeWithCustomMessage() {
        // Arrange
        String customMessage = "Custom error message";
        int status = 400;

        // Act & Assert
        BusinessRuleException ex1 = assertThrows(
            BusinessRuleException.class,
            () -> formatter.returnBusinessRuleErrorResponse(status, customMessage)
        );
        assertTrue(ex1.getMessage().endsWith(customMessage));

        EntityAlreadyExists ex2 = assertThrows(
            EntityAlreadyExists.class,
            () -> formatter.returnEntityAlreadyExistsErrorResponse(status, customMessage)
        );
        assertTrue(ex2.getMessage().endsWith(customMessage));

        EntityDoesNotExistException ex3 = assertThrows(
            EntityDoesNotExistException.class,
            () -> formatter.returnEntityDoesNotExistErrorResponse(status, customMessage)
        );
        assertTrue(ex3.getMessage().endsWith(customMessage));

        GenericErrorException ex4 = assertThrows(
            GenericErrorException.class,
            () -> formatter.returnErrorGenericResponse(status, customMessage)
        );
        assertTrue(ex4.getMessage().endsWith(customMessage));
    }

}
