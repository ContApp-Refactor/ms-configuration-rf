package co.unicauca.edu.co.contables.billing.application.service;

import static co.unicauca.edu.co.contables.billing.support.BillingTestData.annulledReturnOf;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.emittedReturnOf;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.emittedSaleWithLines;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.lineOf;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.product;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.returnDraftOf;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.returnLineOf;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.uuid;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import co.unicauca.edu.co.contables.billing.domain.event.ReturnEventContext;
import co.unicauca.edu.co.contables.billing.domain.model.BillingDocument;
import co.unicauca.edu.co.contables.billing.domain.model.ReturnStatus;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * RET-9, RET-10 (RN-20, RN-21): contexto de devolucion que acompana a los eventos de emision y
 * anulacion de una devolucion: ordinal dentro de su factura y estado de devolucion de la original
 * DESPUES de la operacion. Sin Spring.
 */
class ReturnEventContextFactoryTest {

    private static final UUID ORIGINAL_ID = uuid(10);

    private BillingDocument original;
    private ReturnEventContextFactory factory;

    @BeforeEach
    void setUp() {
        original = emittedSaleWithLines(ORIGINAL_ID, 7L, lineOf(1, product(1L), "10"));
        factory = new ReturnEventContextFactory();
    }

    @Test
    void RET_10_the_first_emitted_return_gets_ordinal_1_and_leaves_the_original_partial() {
        BillingDocument emitted = emittedReturnOf(original, uuid(21), 1L, returnLineOf(original, 1, "4"));

        ReturnEventContext context = factory.forReturn(emitted, original, List.of());

        assertThat(context.ordinal()).isEqualTo(1);
        assertThat(context.originalStatusAfter()).isEqualTo(ReturnStatus.PARCIAL);
    }

    @Test
    void RET_10_an_earlier_annulled_return_keeps_its_ordinal_and_pushes_the_next_one_to_2() {
        BillingDocument annulled = annulledReturnOf(original, uuid(20), 1L, returnLineOf(original, 1, "3"));
        BillingDocument emitted = emittedReturnOf(original, uuid(21), 2L, returnLineOf(original, 1, "4"));

        ReturnEventContext context = factory.forReturn(emitted, original, List.of(annulled));

        assertThat(context.ordinal()).isEqualTo(2);
        // la anulada no cuenta: solo 4 de 10 devueltas
        assertThat(context.originalStatusAfter()).isEqualTo(ReturnStatus.PARCIAL);
    }

    @Test
    void RET_9_returning_everything_leaves_the_original_in_TOTAL() {
        BillingDocument emitted = emittedReturnOf(original, uuid(21), 1L, returnLineOf(original, 1, "10"));

        ReturnEventContext context = factory.forReturn(emitted, original, List.of());

        assertThat(context.originalStatusAfter()).isEqualTo(ReturnStatus.TOTAL);
    }

    @Test
    void RET_9_two_returns_that_add_up_to_the_sold_quantity_leave_the_original_in_TOTAL() {
        BillingDocument previous = emittedReturnOf(original, uuid(20), 1L, returnLineOf(original, 1, "4"));
        BillingDocument emitted = emittedReturnOf(original, uuid(21), 2L, returnLineOf(original, 1, "6"));

        ReturnEventContext context = factory.forReturn(emitted, original, List.of(previous));

        assertThat(context.ordinal()).isEqualTo(2);
        assertThat(context.originalStatusAfter()).isEqualTo(ReturnStatus.TOTAL);
    }

    @Test
    void RET_9_annulling_a_return_releases_its_quantity_so_the_status_moves_back_to_PARCIAL() {
        BillingDocument other = emittedReturnOf(original, uuid(20), 1L, returnLineOf(original, 1, "6"));
        BillingDocument annulled = annulledReturnOf(original, uuid(21), 2L, returnLineOf(original, 1, "4"));

        ReturnEventContext context = factory.forReturn(annulled, original, List.of(other));

        assertThat(context.ordinal()).isEqualTo(2);
        assertThat(context.originalStatusAfter()).isEqualTo(ReturnStatus.PARCIAL);
    }

    @Test
    void RET_9_annulling_the_only_emitted_return_moves_the_status_back_to_SIN_DEVOLUCION() {
        BillingDocument annulled = annulledReturnOf(original, uuid(21), 1L, returnLineOf(original, 1, "4"));

        ReturnEventContext context = factory.forReturn(annulled, original, List.of());

        assertThat(context.ordinal()).isEqualTo(1);
        assertThat(context.originalStatusAfter()).isEqualTo(ReturnStatus.SIN_DEVOLUCION);
    }

    @Test
    void RET_10_the_return_itself_may_already_be_part_of_the_persisted_list_without_being_counted_twice() {
        BillingDocument emitted = emittedReturnOf(original, uuid(21), 1L, returnLineOf(original, 1, "4"));

        ReturnEventContext context = factory.forReturn(emitted, original, List.of(emitted));

        assertThat(context.ordinal()).isEqualTo(1);
        assertThat(context.originalStatusAfter()).isEqualTo(ReturnStatus.PARCIAL);
    }

    @Test
    void RET_10_draft_returns_of_the_list_have_no_number_and_do_not_shift_the_ordinal() {
        BillingDocument draft = returnDraftOf(original, uuid(30), "motivo", returnLineOf(original, 1, "5"));
        BillingDocument emitted = emittedReturnOf(original, uuid(21), 1L, returnLineOf(original, 1, "4"));

        ReturnEventContext context = factory.forReturn(emitted, original, List.of(draft));

        assertThat(context.ordinal()).isEqualTo(1);
        assertThat(context.originalStatusAfter()).isEqualTo(ReturnStatus.PARCIAL);
    }

    @Test
    void RET_1_a_document_that_is_not_a_return_is_rejected() {
        assertThatThrownBy(() -> factory.forReturn(original, original, List.of()))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
