package co.unicauca.edu.co.contables.billing.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

/** EMT-3: formato prefijo + 6 digitos. */
class DocumentNumberTest {

    @Test
    void EMT_3_display_pads_to_six_digits() {
        assertThat(new DocumentNumber("FV", 123L).display()).isEqualTo("FV-000123");
        assertThat(new DocumentNumber("FC", 1L).display()).isEqualTo("FC-000001");
        assertThat(new DocumentNumber("NC", 999_999L).display()).isEqualTo("NC-999999");
    }

    @Test
    void EMT_3_display_grows_beyond_six_digits_without_truncating() {
        assertThat(new DocumentNumber("DC", 1_234_567L).display()).isEqualTo("DC-1234567");
    }

    @Test
    void EMT_3_of_uses_document_type_prefix() {
        assertThat(DocumentNumber.of(DocumentType.SALE_INVOICE, 8L).display()).isEqualTo("FV-000008");
        assertThat(DocumentNumber.of(DocumentType.PURCHASE_RETURN, 9L).display()).isEqualTo("DC-000009");
    }

    @Test
    void EMT_3_value_must_be_positive_and_prefix_present() {
        assertThatThrownBy(() -> new DocumentNumber("FV", 0L)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new DocumentNumber("FV", -5L)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new DocumentNumber(" ", 1L)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new DocumentNumber(null, 1L)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void EMT_3_toString_is_display() {
        assertThat(new DocumentNumber("FV", 7L)).hasToString("FV-000007");
    }
}
