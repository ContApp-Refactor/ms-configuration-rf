package co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.output.jpa.mapper;

import co.unicauca.edu.co.contables.accounting.debt_payments.domain.enums.ReceiptType;
import co.unicauca.edu.co.contables.accounting.debt_payments.domain.model.Receipt;
import co.unicauca.edu.co.contables.accounting.debt_payments.domain.model.ReceiptDetail;
import co.unicauca.edu.co.contables.accounting.debt_payments.domain.model.ReceiptStatus;
import co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.output.jpa.entity.ReceiptDetailEntity;
import co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.output.jpa.entity.ReceiptEntity;
import co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.output.jpa.mapper.IReceiptPersistenceMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static co.unicauca.edu.co.contables.accounting.debt_payments.test.fixtures.TestFixtures.receiptDetail;
import static co.unicauca.edu.co.contables.accounting.debt_payments.test.fixtures.TestFixtures.receiptForInvoicePayment;
import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("IReceiptPersistenceMapper happy-paths (no Spring)")
public class IReceiptPersistenceMapperUnitTest {

    private final IReceiptPersistenceMapper mapper = new IReceiptPersistenceMapperImpl();

    @Test
    @DisplayName("toDomain maps fields and converts BigDecimal/LocalDateTime correctly")
    void toDomain_mapsFields() {
        ReceiptEntity e = new ReceiptEntity();
        e.setId(5L);
        e.setReceiptCode("RC-1");
        e.setEnterpriseId("ENT-1");
        e.setThirdPartyId(11L);
        e.setPaymentMethodId(2L);
        e.setPaymentMethodAccount(3L);
        e.setReceiptType(ReceiptType.INVOICE_PAYMENT);
        e.setStatus(ReceiptStatus.FINALIZED);
        e.setIssueDate(LocalDate.now());
        e.setTotalAmount(BigDecimal.valueOf(1234L));
        e.setObservations("obs");
        e.setVoidReasonDescription("vr");
        e.setVoidDate(LocalDateTime.of(2025,1,2,0,0));
        e.setLedgerAccountId(88L);
        e.setCenterCostId(99L);

        ReceiptDetailEntity d = new ReceiptDetailEntity();
        d.setId(11L);
        d.setInvoiceId(7L);
        d.setInvoiceCode("F-7");
        d.setAccountingAccount(1000L);
        d.setAmountPaid(500L);
        d.setReceipt(e);

        e.setDetails(List.of(d));

        Receipt r = mapper.toDomain(e);

        assertThat(r).isNotNull();
        assertThat(r.getId()).isEqualTo(5L);
        assertThat(r.getReceiptCode()).isEqualTo("RC-1");
        assertThat(r.getTotalAmount()).isEqualTo(1234L);
        assertThat(r.getVoidDate()).isEqualTo(LocalDate.of(2025,1,2));
        assertThat(r.getDetails()).hasSize(1);
        ReceiptDetail rd = r.getDetails().get(0);
        assertThat(rd.getInvoiceId()).isEqualTo(7L);
        assertThat(rd.getAmountPaid()).isEqualTo(500L);
    }

    @Test
    @DisplayName("toEntity maps fields and links child details to parent")
    void toEntity_mapsAndLinksChildren() {
        var detail = receiptDetail(7L, 200L);
        Receipt r = receiptForInvoicePayment("ENT-1", 11L, 2L, "obs", List.of(detail));
        r.setId(20L);
        r.setReceiptCode("RC-20");
        r.setTotalAmount(200L);

        ReceiptEntity e = mapper.toEntity(r);

        assertThat(e).isNotNull();
        assertThat(e.getId()).isEqualTo(20L);
        assertThat(e.getReceiptCode()).isEqualTo("RC-20");
        assertThat(e.getTotalAmount()).isEqualTo(BigDecimal.valueOf(200L));
        assertThat(e.getDetails()).hasSize(1);
        ReceiptDetailEntity d = e.getDetails().get(0);
        assertThat(d.getInvoiceId()).isEqualTo(7L);
        assertThat(d.getReceipt()).isEqualTo(e);
    }

    @Test
    @DisplayName("toDomainList and toEntityList handle lists")
    void listMappings() {
        ReceiptEntity e = new ReceiptEntity();
        e.setId(1L);
        e.setReceiptCode("RC-L");
        e.setTotalAmount(BigDecimal.valueOf(10L));

        var domainList = mapper.toDomainList(List.of(e));
        assertThat(domainList).hasSize(1);

        Receipt r = new Receipt();
        r.setId(2L);
        r.setReceiptCode("RC-2");
        var entityList = mapper.toEntityList(List.of(r));
        assertThat(entityList).hasSize(1);
    }

}


