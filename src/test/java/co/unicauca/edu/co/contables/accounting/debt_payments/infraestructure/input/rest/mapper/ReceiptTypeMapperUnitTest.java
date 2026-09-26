package co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.input.rest.mapper;

import co.unicauca.edu.co.contables.accounting.debt_payments.domain.enums.ReceiptType;
import co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.input.rest.mapper.ReceiptTypeMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class ReceiptTypeMapperUnitTest {

    private final ReceiptTypeMapper mapper = new ReceiptTypeMapper();

    @Test
    void toReceiptType_null_returnsNull() {
        assertThat(mapper.toReceiptType(null)).isNull();
    }

    @Test
    void toReceiptType_validId_returnsEnum() {
        Long id = ReceiptType.INVOICE_PAYMENT.getId();
        ReceiptType t = mapper.toReceiptType(id);
        assertThat(t).isEqualTo(ReceiptType.INVOICE_PAYMENT);
    }

    @Test
    void toReceiptType_invalidId_throws() {
        assertThatThrownBy(() -> mapper.toReceiptType(999L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid ReceiptType ID");
    }

    @Test
    void toLong_nullAndValid() {
        assertThat(mapper.toLong(null)).isNull();
        assertThat(mapper.toLong(ReceiptType.DIRECT_INCOME)).isEqualTo(ReceiptType.DIRECT_INCOME.getId());
    }
}

