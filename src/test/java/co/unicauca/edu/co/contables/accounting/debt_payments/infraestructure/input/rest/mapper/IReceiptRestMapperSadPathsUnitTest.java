package co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.input.rest.mapper;

import co.unicauca.edu.co.contables.accounting.debt_payments.domain.model.Receipt;
import co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.input.rest.dto.request.ReceiptCreateRequest;
import co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.input.rest.dto.response.ReceiptResponse;
import co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.input.rest.mapper.IReceiptRestMapper;
import co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.input.rest.mapper.ReceiptTypeMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("IReceiptRestMapper sad-paths (no Spring)")
public class IReceiptRestMapperSadPathsUnitTest {

    private IReceiptRestMapper createMapperWithHelper() throws Exception {
        IReceiptRestMapperImpl impl = new IReceiptRestMapperImpl();
        // inject ReceiptTypeMapper instance into generated impl to avoid NPE
        try {
            Field f = impl.getClass().getDeclaredField("receiptTypeMapper");
            f.setAccessible(true);
            f.set(impl, new ReceiptTypeMapper());
        } catch (NoSuchFieldException nsf) {
            // If field name differs, try to find any field of type ReceiptTypeMapper
            for (Field ff : impl.getClass().getDeclaredFields()) {
                if (ff.getType().equals(ReceiptTypeMapper.class)) {
                    ff.setAccessible(true);
                    ff.set(impl, new ReceiptTypeMapper());
                    break;
                }
            }
        }
        return impl;
    }

    @Test
    @DisplayName("toDomain should return null when input is null")
    void toDomain_nullInput_returnsNull() throws Exception {
        var mapper = createMapperWithHelper();
        assertThat(mapper.toDomain((ReceiptCreateRequest) null)).isNull();
    }

    @Test
    @DisplayName("toDomain should handle null details without throwing and keep details null")
    void toDomain_nullDetails_keepsNull() throws Exception {
        var mapper = createMapperWithHelper();
        ReceiptCreateRequest req = new ReceiptCreateRequest();
        req.setEnterpriseId("ENT-1");
        req.setThirdPartyId(10L);
        req.setPaymentMethodId(2L);
        req.setReceiptTypeId(1L);
        req.setDetails(null);

        Receipt domain = mapper.toDomain(req);

        assertThat(domain).isNotNull();
        assertThat(domain.getDetails()).isNull();
    }

    @Test
    @DisplayName("toDomain should handle null receiptTypeId without throwing and set receiptType null")
    void toDomain_nullReceiptTypeId_setsNullReceiptType() throws Exception {
        var mapper = createMapperWithHelper();
        ReceiptCreateRequest req = new ReceiptCreateRequest();
        req.setEnterpriseId("ENT-1");
        req.setThirdPartyId(10L);
        req.setPaymentMethodId(2L);
        req.setReceiptTypeId(null);
        req.setDetails(new ArrayList<>());

        Receipt domain = mapper.toDomain(req);

        assertThat(domain).isNotNull();
        assertThat(domain.getReceiptType()).isNull();
    }

    @Test
    @DisplayName("toResponse should return null when input is null")
    void toResponse_nullInput_returnsNull() throws Exception {
        var mapper = createMapperWithHelper();
        assertThat(mapper.toResponse((Receipt) null)).isNull();
    }

    @Test
    @DisplayName("toResponse should map null receiptType to null receiptTypeId and handle null details")
    void toResponse_nullReceiptType_and_nullDetails() throws Exception {
        var mapper = createMapperWithHelper();
        Receipt r = new Receipt();
        r.setId(5L);
        r.setEnterpriseId("ENT-1");
        r.setDetails(null);
        r.setReceiptType(null);

        ReceiptResponse resp = mapper.toResponse(r);

        assertThat(resp).isNotNull();
        assertThat(resp.getId()).isEqualTo(5L);
        assertThat(resp.getReceiptTypeId()).isNull();
        assertThat(resp.getDetails()).isNull();
    }
}

