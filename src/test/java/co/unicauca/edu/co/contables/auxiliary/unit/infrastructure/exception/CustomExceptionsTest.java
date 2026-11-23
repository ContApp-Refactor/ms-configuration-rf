package co.unicauca.edu.co.contables.auxiliary.unit.infrastructure.exception;

import co.unicauca.edu.co.contables.auxiliary.infrastructure.out.exception.customized.BusinessRuleException;
import co.unicauca.edu.co.contables.auxiliary.infrastructure.out.exception.customized.EntityAlreadyExists;
import co.unicauca.edu.co.contables.auxiliary.infrastructure.out.exception.customized.EntityDoesNotExistException;
import co.unicauca.edu.co.contables.auxiliary.infrastructure.out.exception.customized.ErrorCode;
import co.unicauca.edu.co.contables.auxiliary.infrastructure.out.exception.customized.GenericErrorException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * @brief Pruebas unitarias para las excepciones personalizadas y {@link ErrorCode}.
 */
class CustomExceptionsTest {

    @Test
    @DisplayName("BusinessRuleException conserva status y message")
    void businessRuleException() {
        BusinessRuleException ex = new BusinessRuleException(400, "regla violada");

        Assertions.assertThat(ex.getStatus()).isEqualTo(400);
        Assertions.assertThat(ex.getMessage()).isEqualTo("regla violada");
        Assertions.assertThat(ex).isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("EntityAlreadyExists conserva status y message")
    void entityAlreadyExists() {
        EntityAlreadyExists ex = new EntityAlreadyExists(409, "duplicado");

        Assertions.assertThat(ex.getStatus()).isEqualTo(409);
        Assertions.assertThat(ex.getMessage()).isEqualTo("duplicado");
    }

    @Test
    @DisplayName("EntityDoesNotExistException conserva status y message")
    void entityDoesNotExistException() {
        EntityDoesNotExistException ex = new EntityDoesNotExistException(404, "no existe");

        Assertions.assertThat(ex.getStatus()).isEqualTo(404);
        Assertions.assertThat(ex.getMessage()).isEqualTo("no existe");
    }

    @Test
    @DisplayName("GenericErrorException conserva status y message")
    void genericErrorException() {
        GenericErrorException ex = new GenericErrorException(500, "error genérico");

        Assertions.assertThat(ex.getStatus()).isEqualTo(500);
        Assertions.assertThat(ex.getMessage()).isEqualTo("error genérico");
    }

    @Test
    @DisplayName("BaseException setters actualizan status y message")
    void baseExceptionSetters() {
        GenericErrorException ex = new GenericErrorException(500, "inicial");
        ex.setStatus(418);
        ex.setMessage("actualizado");

        Assertions.assertThat(ex.getStatus()).isEqualTo(418);
        Assertions.assertThat(ex.getMessage()).isEqualTo("actualizado");
    }

    @Test
    @DisplayName("ErrorCode expone descripciones esperadas")
    void errorCodeDescriptions() {
        Assertions.assertThat(ErrorCode.GENERIC_ERROR.getDescription()).startsWith("GC-001");
        Assertions.assertThat(ErrorCode.ENTITY_ALREADY_EXISTS.getDescription()).startsWith("GC-002");
        Assertions.assertThat(ErrorCode.ENTITY_NOT_FOUND.getDescription()).startsWith("GC-003");
        Assertions.assertThat(ErrorCode.BUSINESS_RULE_VIOLATION.getDescription()).startsWith("GC-004");
        Assertions.assertThat(ErrorCode.values()).hasSize(4);
    }
}
