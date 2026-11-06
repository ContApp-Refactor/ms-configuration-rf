package co.unicauca.edu.co.contables.billing.domain.event;

import static co.unicauca.edu.co.contables.billing.support.BillingTestData.NOW;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.annulledSale;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.emittedSale;
import static co.unicauca.edu.co.contables.billing.support.BillingTestData.uuid;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import co.unicauca.edu.co.contables.billing.domain.model.BillingDocument;
import co.unicauca.edu.co.contables.billing.domain.model.ReturnStatus;
import org.junit.jupiter.api.Test;

/** EVT-1: eventos de dominio que acompanan emision y anulacion. */
class BillingDomainEventTest {

    @Test
    void EVT_1_emitted_event_carries_id_time_document_and_no_return_context_for_invoices() {
        BillingDocument doc = emittedSale();

        BillingDomainEvent event = new BillingDocumentEmitted(uuid(100), NOW, doc, null);

        assertThat(event.eventId()).isEqualTo(uuid(100));
        assertThat(event.occurredAt()).isEqualTo(NOW);
        assertThat(event.document()).isSameAs(doc);
        assertThat(event.returnContext()).isNull();
        assertThat(event).isInstanceOf(BillingDocumentEmitted.class);
    }

    @Test
    void EVT_1_annulled_event_carries_return_context_when_given() {
        BillingDocument doc = annulledSale();
        ReturnEventContext context = new ReturnEventContext(2, ReturnStatus.PARCIAL);

        BillingDomainEvent event = new BillingDocumentAnnulled(uuid(101), NOW, doc, context);

        assertThat(event.returnContext().ordinal()).isEqualTo(2);
        assertThat(event.returnContext().originalStatusAfter()).isEqualTo(ReturnStatus.PARCIAL);
        assertThat(event).isInstanceOf(BillingDocumentAnnulled.class);
    }

    @Test
    void EVT_1_events_require_id_time_and_document() {
        BillingDocument doc = emittedSale();

        assertThatThrownBy(() -> new BillingDocumentEmitted(null, NOW, doc, null))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> new BillingDocumentEmitted(uuid(1), null, doc, null))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> new BillingDocumentAnnulled(uuid(1), NOW, null, null))
                .isInstanceOf(NullPointerException.class);
    }
}
