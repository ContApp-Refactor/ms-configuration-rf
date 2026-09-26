package co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.output.messageBroker.mapper;

import co.unicauca.edu.co.contables.accounting.debt_payments.domain.model.PortfolioWriteOff;
import co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.output.messageBroker.dto.PortfolioWriteOffEventDto;
import co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.output.messageBroker.mapper.IPortfolioWriteOffEventMapper;
import co.unicauca.edu.co.contables.accounting.debt_payments.test.fixtures.TestFixtures;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("IPortfolioWriteOffEventMapper (MapStruct) tests")
public class IPortfolioWriteOffEventMapperUnitTest {

    private final IPortfolioWriteOffEventMapper mapper = new IPortfolioWriteOffEventMapperImpl();

    @Test
    @DisplayName("toEventDto should map PortfolioWriteOff -> PortfolioWriteOffEventDto ignoring details")
    void toEventDto_mapsDomainToEventDto() {
        var detail = TestFixtures.writeOffDetail(3L, 120L);
        PortfolioWriteOff writeOff = TestFixtures.portfolioWriteOffWithDetails("ENT-1", 5L, "just", List.of(detail));
        writeOff.setId(77L);

        PortfolioWriteOffEventDto dto = mapper.toEventDto(writeOff);

        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isEqualTo(77L);
        assertThat(dto.getEnterpriseId()).isEqualTo("ENT-1");
        // details are ignored by mapping
        assertThat(dto.getDetails()).isNull();
    }
}

