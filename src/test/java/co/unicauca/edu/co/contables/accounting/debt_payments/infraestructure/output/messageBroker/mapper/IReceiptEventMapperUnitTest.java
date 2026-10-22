package co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.output.messageBroker.mapper;

import co.unicauca.edu.co.contables.accounting.debt_payments.domain.model.Receipt;
import co.unicauca.edu.co.contables.accounting.debt_payments.domain.model.ReceiptDetail;
import co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.output.messageBroker.dto.ReceiptDetailEventDto;
import co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.output.messageBroker.dto.ReceiptEventDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static co.unicauca.edu.co.contables.accounting.debt_payments.test.fixtures.TestFixtures.receiptDetail;
import static co.unicauca.edu.co.contables.accounting.debt_payments.test.fixtures.TestFixtures.receiptForInvoicePayment;
import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("IReceiptEventMapper happy-paths")
public class IReceiptEventMapperUnitTest {

    private final IReceiptEventMapperImpl impl = new IReceiptEventMapperImpl();

    @Test
    @DisplayName("toEventDto maps receipt fields including totalAmount and details")
    void toEventDto_mapsFieldsAndDetails() {
        var d = receiptDetail(7L, 250L);
        Receipt r = receiptForInvoicePayment("ENT-1", 11L, 2L, "obs", List.of(d));
        r.setId(33L);
        r.setReceiptCode("RC-33");
        r.setTotalAmount(250L);

        ReceiptEventDto dto = impl.toEventDto(r);

        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isEqualTo(33L);
        assertThat(dto.getReceiptCode()).isEqualTo("RC-33");
        assertThat(dto.getTotalAmount()).isEqualByComparingTo(BigDecimal.valueOf(250L));
        assertThat(dto.getDetails()).hasSize(1);
        assertThat(dto.getDetails().get(0).getInvoiceId()).isEqualTo(7L);
        assertThat(dto.getReceiptTypeId()).isEqualTo(r.getReceiptType().getId());
    }

    @Test
    @DisplayName("toEventDtoList maps multiple receipts")
    void toEventDtoList_mapsMultiple() {
        Receipt r1 = new Receipt();
        r1.setId(1L);
        Receipt r2 = new Receipt();
        r2.setId(2L);

        var list = impl.toEventDtoList(List.of(r1, r2));
        assertThat(list).hasSize(2);
        assertThat(list.get(0).getId()).isEqualTo(1L);
        assertThat(list.get(1).getId()).isEqualTo(2L);
    }

    @Test
    @DisplayName("toEventDtoDetail converts BigDecimal amount to Long and copies fields")
    void toEventDtoDetail_convertsAmount() {
        ReceiptDetailEventDto dto = new ReceiptDetailEventDto();
        dto.setInvoiceId(5L);
        dto.setAmountPaid(BigDecimal.valueOf(123L));
        dto.setInvoiceCode("F-5");
        dto.setAccountingAccount(900L);

        ReceiptDetail detail = impl.toEventDtoDetail(dto);

        assertThat(detail).isNotNull();
        assertThat(detail.getInvoiceId()).isEqualTo(5L);
        assertThat(detail.getAmountPaid()).isEqualTo(123L);
        assertThat(detail.getInvoiceCode()).isEqualTo("F-5");
        assertThat(detail.getAccountingAccount()).isEqualTo(900L);
    }
}

