package co.unicauca.edu.co.contables.billing.domain.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/** STA-1 (RN-01): solo BORRADOR->EMITIDA y EMITIDA->ANULADA. */
class DocumentStatusTest {

    @Test
    void STA_1_allowed_transitions() {
        assertThat(DocumentStatus.BORRADOR.canTransitionTo(DocumentStatus.EMITIDA)).isTrue();
        assertThat(DocumentStatus.EMITIDA.canTransitionTo(DocumentStatus.ANULADA)).isTrue();
    }

    @Test
    void STA_1_forbidden_transitions() {
        assertThat(DocumentStatus.BORRADOR.canTransitionTo(DocumentStatus.ANULADA)).isFalse();
        assertThat(DocumentStatus.BORRADOR.canTransitionTo(DocumentStatus.BORRADOR)).isFalse();
        assertThat(DocumentStatus.EMITIDA.canTransitionTo(DocumentStatus.BORRADOR)).isFalse();
        assertThat(DocumentStatus.EMITIDA.canTransitionTo(DocumentStatus.EMITIDA)).isFalse();
        assertThat(DocumentStatus.ANULADA.canTransitionTo(DocumentStatus.BORRADOR)).isFalse();
        assertThat(DocumentStatus.ANULADA.canTransitionTo(DocumentStatus.EMITIDA)).isFalse();
        assertThat(DocumentStatus.ANULADA.canTransitionTo(DocumentStatus.ANULADA)).isFalse();
    }
}
