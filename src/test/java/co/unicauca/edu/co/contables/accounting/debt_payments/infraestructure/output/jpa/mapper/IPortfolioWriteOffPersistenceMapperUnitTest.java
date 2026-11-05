package co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.output.jpa.mapper;

import co.unicauca.edu.co.contables.accounting.debt_payments.domain.model.PortfolioWriteOff;
import co.unicauca.edu.co.contables.accounting.debt_payments.domain.model.WriteOffDetail;
import co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.output.jpa.entity.PortfolioWriteOffEntity;
import co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.output.jpa.entity.WriteOffDetailEntity;
import co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.output.jpa.mapper.IPortfolioWriteOffPersistenceMapper;
import co.unicauca.edu.co.contables.accounting.debt_payments.test.fixtures.TestFixtures;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("IPortfolioWriteOffPersistenceMapper (MapStruct) tests")
public class IPortfolioWriteOffPersistenceMapperUnitTest {

    private final IPortfolioWriteOffPersistenceMapper mapper = new IPortfolioWriteOffPersistenceMapperImpl();

    @Test
    @DisplayName("toEntity should map PortfolioWriteOff -> PortfolioWriteOffEntity with details")
    void toEntity_mapsDomainToEntity() {
        var detail = TestFixtures.writeOffDetail(7L, 250L);
        PortfolioWriteOff writeOff = TestFixtures.portfolioWriteOffWithDetails("ENT-1", 11L, "just", List.of(detail));
        writeOff.setId(33L);

        PortfolioWriteOffEntity entity = mapper.toEntity(writeOff);

        assertThat(entity).isNotNull();
        assertThat(entity.getEnterpriseId()).isEqualTo(writeOff.getEnterpriseId());
        assertThat(entity.getThirdId()).isEqualTo(writeOff.getThirdId());
        assertThat(entity.getJustification()).isEqualTo(writeOff.getJustification());
        assertThat(entity.getDetails()).hasSize(1);

        WriteOffDetailEntity det = entity.getDetails().get(0);
        assertThat(det.getInvoiceId()).isEqualTo(detail.getInvoiceId());
        assertThat(det.getAmountWrittenOff()).isEqualTo(detail.getAmountWrittenOff());
        assertThat(det.getAccountingAccount()).isEqualTo(detail.getAccountingAccount());
    }

    @Test
    @DisplayName("toDomain should map PortfolioWriteOffEntity -> PortfolioWriteOff with details")
    void toDomain_mapsEntityToDomain() {
        PortfolioWriteOffEntity entity = new PortfolioWriteOffEntity();
        entity.setId(55L);
        entity.setEnterpriseId("ENT-2");
        entity.setJustification("j");
        entity.setThirdId(22L);

        WriteOffDetailEntity d = new WriteOffDetailEntity();
        d.setInvoiceId(9L);
        d.setAmountWrittenOff(300L);
        d.setAccountingAccount(400L);
        d.setPortfolioWriteOff(entity);
        entity.setDetails(List.of(d));

        PortfolioWriteOff domain = mapper.toDomain(entity);

        assertThat(domain).isNotNull();
        assertThat(domain.getId()).isEqualTo(55L);
        assertThat(domain.getEnterpriseId()).isEqualTo("ENT-2");
        assertThat(domain.getThirdId()).isEqualTo(22L);
        assertThat(domain.getDetails()).hasSize(1);

        WriteOffDetail detail = domain.getDetails().get(0);
        assertThat(detail.getInvoiceId()).isEqualTo(9L);
        assertThat(detail.getAmountWrittenOff()).isEqualTo(300L);
        assertThat(detail.getAccountingAccount()).isEqualTo(400L);
    }
}

