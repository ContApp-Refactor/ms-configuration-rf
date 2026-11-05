package co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.output.jpa.mapper;

import co.unicauca.edu.co.contables.accounting.debt_payments.domain.model.Receipt;
import co.unicauca.edu.co.contables.accounting.debt_payments.domain.model.ReceiptDetail;
import co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.output.jpa.entity.ReceiptEntity;
import co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.output.jpa.mapper.IReceiptPersistenceMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("IReceiptPersistenceMapper sad-paths (no Spring)")
public class IReceiptPersistenceMapperSadPathsUnitTest {

    private final IReceiptPersistenceMapper mapper = new IReceiptPersistenceMapperImpl();

    @Test
    @DisplayName("toEntity should return null when input is null")
    void toEntity_nullInput_returnsNull() {
        ReceiptEntity entity = mapper.toEntity((Receipt) null);
        assertThat(entity).isNull();
    }

    @Test
    @DisplayName("toEntity should handle null details without throwing and keep details null")
    void toEntity_nullDetails_keepsNull() {
        Receipt r = new Receipt();
        r.setEnterpriseId("ENT");
        r.setDetails(null);

        ReceiptEntity entity = mapper.toEntity(r);

        assertThat(entity).isNotNull();
        assertThat(entity.getDetails()).isNull();
    }

    @Test
    @DisplayName("toEntity should handle empty details and produce empty list")
    void toEntity_emptyDetails_producesEmptyList() {
        Receipt r = new Receipt();
        r.setEnterpriseId("ENT");
        r.setDetails(new ArrayList<>());

        ReceiptEntity entity = mapper.toEntity(r);

        assertThat(entity).isNotNull();
        assertThat(entity.getDetails()).isEmpty();
    }

    @Test
    @DisplayName("toEntity should link child details to parent when details present")
    void toEntity_linksChildrenToParent() {
        var d = new ReceiptDetail(null, null, null, 50L, null);
        Receipt r = new Receipt();
        r.setEnterpriseId("ENT");
        r.setDetails(java.util.List.of(d));

        ReceiptEntity entity = mapper.toEntity(r);

        assertThat(entity).isNotNull();
        assertThat(entity.getDetails()).hasSize(1);
        assertThat(entity.getDetails().get(0).getReceipt()).isEqualTo(entity);
    }

    @Test
    @DisplayName("toEntity should handle detail with null fields without throwing")
    void toEntity_detailWithNullFields_noException() {
        var d = new ReceiptDetail(null, null, null, null, null);
        Receipt r = new Receipt();
        r.setEnterpriseId("ENT");
        r.setDetails(java.util.List.of(d));

        ReceiptEntity entity = mapper.toEntity(r);

        assertThat(entity).isNotNull();
        assertThat(entity.getDetails()).hasSize(1);
        // fields may be null but mapping shouldn't throw
        assertThat(entity.getDetails().get(0).getInvoiceId()).isNull();
        assertThat(entity.getDetails().get(0).getAmountPaid()).isNull();
    }

    @Test
    @DisplayName("toDomain should return null when input is null")
    void toDomain_nullInput_returnsNull() {
        assertThat(mapper.toDomain((ReceiptEntity) null)).isNull();
    }
}

