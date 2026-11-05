package co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.input.rest.mapper;

import co.unicauca.edu.co.contables.accounting.debt_payments.domain.model.PortfolioWriteOff;
import co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.input.rest.dto.request.CreateWriteOffRequest;
import co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.input.rest.dto.request.WriteOffDetailRequest;
import co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.input.rest.dto.response.PortfolioWriteOffResponse;
import co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.input.rest.mapper.IPortfolioWriteOffRestMapper;
import co.unicauca.edu.co.contables.accounting.debt_payments.test.fixtures.TestFixtures;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for MapStruct mapper IPortfolioWriteOffRestMapper.
 */
@DisplayName("IPortfolioWriteOffRestMapper (MapStruct) tests")
public class IPortfolioWriteOffRestMapperUnitTest {

    private final IPortfolioWriteOffRestMapper mapper = new IPortfolioWriteOffRestMapperImpl();

    @Test
    @DisplayName("toDomain should map CreateWriteOffRequest -> PortfolioWriteOff domain")
    void toDomain_mapsCreateRequestToDomain() {
        WriteOffDetailRequest d = new WriteOffDetailRequest();
        d.setInvoiceId(1L);

        CreateWriteOffRequest req = new CreateWriteOffRequest();
        req.setJustification("Justify");
        req.setWriteOffDate(LocalDate.now());
        req.setDebitAuxiliaryAccount(1000L);
        req.setDebitAuxiliaryAccountId(2000L);
        req.setThirdId(10L);
        req.setEnterpriseId("ENT-1");
        req.setDetails(List.of(d));

        PortfolioWriteOff domain = mapper.toDomain(req);

        assertThat(domain).isNotNull();
        assertThat(domain.getJustification()).isEqualTo("Justify");
        assertThat(domain.getEnterpriseId()).isEqualTo("ENT-1");
        assertThat(domain.getThirdId()).isEqualTo(10L);
        assertThat(domain.getDetails()).hasSize(1);
        assertThat(domain.getDetails().get(0).getInvoiceId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("toResponse should map PortfolioWriteOff domain -> PortfolioWriteOffResponse (details ignored)")
    void toResponse_mapsDomainToResponse() {
        var detail = TestFixtures.writeOffDetail(5L, 100L);
        PortfolioWriteOff writeOff = TestFixtures.portfolioWriteOffWithDetails("ENT-1", 10L, "just", List.of(detail));
        writeOff.setId(99L);

        PortfolioWriteOffResponse resp = mapper.toResponse(writeOff);

        assertThat(resp).isNotNull();
        assertThat(resp.getId()).isEqualTo(99L);
        assertThat(resp.getEnterpriseId()).isEqualTo("ENT-1");
        // Mapper ignores details when mapping to response
        assertThat(resp.getDetails()).isNull();
    }
}

