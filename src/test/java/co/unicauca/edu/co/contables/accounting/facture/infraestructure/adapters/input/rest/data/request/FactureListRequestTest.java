package co.unicauca.edu.co.contables.accounting.facture.infraestructure.adapters.input.rest.data.request;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.util.Set;
import org.junit.jupiter.api.Test;

/**
 * Contrato de validación de {@link FactureListRequest}.
 *
 * `numPage` es primitiva: `@NotNull` sobre un `int` nunca puede dispararse
 * (el binding lo materializa siempre), por lo que la garantía real es que no
 * sea negativa. La página 0 es la primera página de Spring Data, no un valor
 * indefinido.
 */
class FactureListRequestTest {

    private static final Validator VALIDATOR = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void acceptsZeroAsFirstPage() {
        FactureListRequest request = FactureListRequest.builder()
                .entId("enterprise-a")
                .numPage(0)
                .build();

        assertThat(VALIDATOR.validate(request)).isEmpty();
    }

    @Test
    void rejectsNegativePageNumber() {
        FactureListRequest request = FactureListRequest.builder()
                .entId("enterprise-a")
                .numPage(-1)
                .build();

        Set<jakarta.validation.ConstraintViolation<FactureListRequest>> violations =
                VALIDATOR.validate(request);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getPropertyPath().toString()).isEqualTo("numPage");
        assertThat(violations.iterator().next().getMessage())
                .isEqualTo("Page number cannot be negative.");
    }

    @Test
    void defaultsOmittedPageToFirstPage() {
        FactureListRequest request = new FactureListRequest();
        request.setEntId("enterprise-a");

        assertThat(request.getNumPage()).isZero();
        assertThat(VALIDATOR.validate(request)).isEmpty();
    }

    @Test
    void stillRequiresEnterpriseId() {
        FactureListRequest request = FactureListRequest.builder().numPage(0).build();

        assertThat(VALIDATOR.validate(request))
                .extracting(v -> v.getPropertyPath().toString())
                .containsExactly("entId");
    }
}
