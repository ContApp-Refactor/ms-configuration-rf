package co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.output.jpa.mapper.replicas;

import co.unicauca.edu.co.contables.accounting.debt_payments.domain.enums.InvoiceStatus;
import co.unicauca.edu.co.contables.accounting.debt_payments.domain.model.Replica.InvoiceReplica;
import co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.output.jpa.entity.replicas.InvoiceReplicaEntity;
import co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.output.jpa.mapper.replicas.IInvoicePersistenceMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static co.unicauca.edu.co.contables.accounting.debt_payments.test.fixtures.TestFixtures.invoiceReplicaWith;
import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("IInvoicePersistenceMapper happy-paths (no Spring)")
public class IInvoicePersistenceMapperUnitTest {

    private final IInvoicePersistenceMapper mapper = new IInvoicePersistenceMapperImpl();

    @Test
    @DisplayName("toDomain maps fields correctly and converts factCode to String")
    void toDomain_mapsFields() {
        InvoiceReplicaEntity e = new InvoiceReplicaEntity();
        e.setId(7L);
        e.setFactCode(12345L);
        e.setEntId("ENT-1");
        e.setThirdId(11L);
        e.setTotalValue(1000L);
        e.setTotalPay(200L);
        e.setPendingValue(800L);
        e.setAccountingAccount(500L);
        e.setCreationDate(LocalDate.now().minusDays(10));
        e.setExpirationDate(LocalDate.now().plusDays(10));
        e.setStatus(InvoiceStatus.PENDING);
        e.setActive(true);

        InvoiceReplica domain = mapper.toDomain(e);

        assertThat(domain).isNotNull();
        assertThat(domain.getId()).isEqualTo(7L);
        assertThat(domain.getFactCode()).isEqualTo("12345");
        assertThat(domain.getEntId()).isEqualTo("ENT-1");
        assertThat(domain.getThirdId()).isEqualTo(11L);
        assertThat(domain.getTotalValue()).isEqualTo(1000L);
        assertThat(domain.getPendingValue()).isEqualTo(800L);
        assertThat(domain.getAccountingAccount()).isEqualTo(500L);
        assertThat(domain.getStatus()).isEqualTo(InvoiceStatus.PENDING);
    }

    @Test
    @DisplayName("toEntity maps fields correctly and parses factCode from String")
    void toEntity_mapsFields() {
        InvoiceReplica domain = invoiceReplicaWith(9L, 2000L, 1500L, InvoiceStatus.PENDING);
        domain.setFactCode("54321");

        var entity = mapper.toEntity(domain);

        assertThat(entity).isNotNull();
        assertThat(entity.getId()).isEqualTo(9L);
        assertThat(entity.getFactCode()).isEqualTo(54321L);
        assertThat(entity.getEntId()).isEqualTo(domain.getEntId());
        assertThat(entity.getTotalValue()).isEqualTo(domain.getTotalValue());
        assertThat(entity.getPendingValue()).isEqualTo(domain.getPendingValue());
        assertThat(entity.getStatus()).isEqualTo(domain.getStatus());
    }

    @Test
    @DisplayName("toInvoiceReplicaList maps lists correctly")
    void toInvoiceReplicaList_mapsList() {
        InvoiceReplicaEntity e = new InvoiceReplicaEntity();
        e.setId(1L);
        e.setFactCode(7L);
        e.setEntId("ENT-1");
        List<InvoiceReplica> list = mapper.toInvoiceReplicaList(List.of(e));

        assertThat(list).hasSize(1);
        assertThat(list.get(0).getFactCode()).isEqualTo("7");
    }
}

