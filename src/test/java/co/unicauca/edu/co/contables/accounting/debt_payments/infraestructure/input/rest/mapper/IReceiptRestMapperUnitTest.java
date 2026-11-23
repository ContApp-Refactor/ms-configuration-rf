package co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.input.rest.mapper;

import co.unicauca.edu.co.contables.accounting.debt_payments.domain.model.Receipt;
import co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.input.rest.dto.request.ReceiptCreateRequest;
import co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.input.rest.dto.request.ReceiptDetailRequest;
import co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.input.rest.dto.response.ReceiptResponse;
import co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.input.rest.mapper.IReceiptRestMapper;
import co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.input.rest.mapper.ReceiptTypeMapper;
import co.unicauca.edu.co.contables.accounting.debt_payments.test.fixtures.TestFixtures;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@Import({IReceiptRestMapperImpl.class, ReceiptTypeMapper.class})
@DisplayName("IReceiptRestMapper (MapStruct) tests")
public class IReceiptRestMapperUnitTest {

    @Autowired
    private IReceiptRestMapper mapper;

    @Test
    @DisplayName("toDomain should map ReceiptCreateRequest -> Receipt domain for invoice payment")
    void toDomain_mapsCreateRequestToDomain() {
        ReceiptDetailRequest d = new ReceiptDetailRequest();
        d.setInvoiceId(1L);
        d.setAmountPaid(100L);

        ReceiptCreateRequest req = new ReceiptCreateRequest();
        req.setEnterpriseId("ENT-1");
        req.setThirdPartyId(10L);
        req.setPaymentMethodId(2L);
        req.setReceiptTypeId(1L); // assuming 1L -> INVOICE_PAYMENT
        req.setDetails(List.of(d));

        Receipt domain = mapper.toDomain(req);

        assertThat(domain).isNotNull();
        assertThat(domain.getEnterpriseId()).isEqualTo("ENT-1");
        assertThat(domain.getDetails()).hasSize(1);
        assertThat(domain.getDetails().get(0).getInvoiceId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("toResponse should map Receipt domain -> ReceiptResponse")
    void toResponse_mapsDomainToResponse() {
        var detail = TestFixtures.receiptDetail(2L, 50L);
        Receipt receipt = TestFixtures.receiptForInvoicePayment("ENT-1", 10L, 2L, "obs", List.of(detail));
        receipt.setId(11L);

        ReceiptResponse resp = mapper.toResponse(receipt);

        assertThat(resp).isNotNull();
        assertThat(resp.getId()).isEqualTo(11L);
        assertThat(resp.getReceiptTypeId()).isNotNull();
        assertThat(resp.getDetails()).hasSize(1);
    }
}

