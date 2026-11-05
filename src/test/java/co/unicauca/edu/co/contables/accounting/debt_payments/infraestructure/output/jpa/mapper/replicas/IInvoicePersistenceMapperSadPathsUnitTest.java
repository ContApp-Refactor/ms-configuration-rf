package co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.output.jpa.mapper.replicas;

import co.unicauca.edu.co.contables.accounting.debt_payments.domain.model.Replica.InvoiceReplica;
import co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.output.jpa.entity.replicas.InvoiceReplicaEntity;
import co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.output.jpa.mapper.replicas.IInvoicePersistenceMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("IInvoicePersistenceMapper sad-paths (no Spring)")
public class IInvoicePersistenceMapperSadPathsUnitTest {

    private final IInvoicePersistenceMapper mapper = new IInvoicePersistenceMapperImpl();

    @Test
    @DisplayName("toDomain should return null when input is null")
    void toDomain_nullInput_returnsNull() {
        InvoiceReplica domain = mapper.toDomain((InvoiceReplicaEntity) null);
        assertThat(domain).isNull();
    }

    @Test
    @DisplayName("toEntity should return null when input is null")
    void toEntity_nullInput_returnsNull() {
        InvoiceReplicaEntity entity = mapper.toEntity((InvoiceReplica) null);
        assertThat(entity).isNull();
    }
}

