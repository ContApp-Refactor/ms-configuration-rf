package co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.output.messageBroker.mapper;

import co.unicauca.edu.co.contables.accounting.debt_payments.domain.model.Receipt;
import co.unicauca.edu.co.contables.accounting.debt_payments.domain.model.ReceiptDetail;
import co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.output.messageBroker.dto.ReceiptDetailEventDto;
import co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.output.messageBroker.dto.ReceiptEventDto;
import co.unicauca.edu.co.contables.accounting.debt_payments.test.fixtures.TestFixtures;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

public class IReceiptEventMapperSadPathsUnitTest {

    @Test
    void toEventDto_nullReceipt_returnsNull() {
        IReceiptEventMapperImpl impl = new IReceiptEventMapperImpl();
        ReceiptEventDto dto = impl.toEventDto(null);
        assertThat(dto).isNull();
    }

    @Test
    void toEventDto_minimalDirectIncome_mapsTotalAndType() {
        IReceiptEventMapperImpl impl = new IReceiptEventMapperImpl();

        Receipt r = TestFixtures.receiptForDirectIncome("ENT-1", 1L, 2L, "obs", 1500L, 5000L);
        r.setId(10L);
        r.setReceiptCode("RC-1");

        ReceiptEventDto dto = impl.toEventDto(r);

        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isEqualTo(10L);
        assertThat(dto.getReceiptCode()).isEqualTo("RC-1");
        assertThat(dto.getTotalAmount()).isEqualByComparingTo(BigDecimal.valueOf(1500L));
        assertThat(dto.getReceiptTypeId()).isEqualTo(r.getReceiptType().getId());
    }

    @Test
    void toEventDtoList_null_returnsNull() {
        IReceiptEventMapperImpl impl = new IReceiptEventMapperImpl();
        assertThat(impl.toEventDtoList(null)).isNull();
    }

    @Test
    void toEventDtoDetail_handlesNullAmountAndFields() {
        IReceiptEventMapperImpl impl = new IReceiptEventMapperImpl();

        ReceiptDetailEventDto e = new ReceiptDetailEventDto();
        e.setInvoiceId(5L);
        e.setAmountPaid(null);
        e.setInvoiceCode("F-5");

        ReceiptDetail detail = impl.toEventDtoDetail(e);

        assertThat(detail).isNotNull();
        assertThat(detail.getInvoiceId()).isEqualTo(5L);
        assertThat(detail.getAmountPaid()).isNull();
        assertThat(detail.getInvoiceCode()).isEqualTo("F-5");
    }

    @Test
    void toEventDtoDetail_null_returnsNull() {
        IReceiptEventMapperImpl impl = new IReceiptEventMapperImpl();
        assertThat(impl.toEventDtoDetail(null)).isNull();
    }
}

