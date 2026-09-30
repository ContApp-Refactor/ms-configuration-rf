package co.unicauca.edu.co.contables.accounting.facture.infraestructure.adapters.output.PDFgeneration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.awt.image.BufferedImage;
import java.nio.file.Path;
import java.util.Set;
import java.util.UUID;

import javax.imageio.ImageIO;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.test.util.ReflectionTestUtils;

import co.unicauca.edu.co.contables.accounting.facture.domain.model.Facture;
import co.unicauca.edu.co.contables.accounting.facture.domain.model.Product;
import co.unicauca.edu.co.contables.accounting.facture.domain.model.eFactureType;
import co.unicauca.edu.co.contables.configuration.enterprise.application.ports.input.IEnterpriseSearchManagerPort;
import co.unicauca.edu.co.contables.configuration.thirds.application.ports.input.GetThirdUseCase;
import co.unicauca.edu.co.contables.configuration.thirds.domain.enums.ePersonType;
import co.unicauca.edu.co.contables.configuration.thirds.domain.model.City;
import co.unicauca.edu.co.contables.configuration.thirds.domain.model.Country;
import co.unicauca.edu.co.contables.configuration.thirds.domain.model.State;
import co.unicauca.edu.co.contables.configuration.thirds.domain.model.TypeId;

/**
 * El PDF resuelve empresa y tercero inyectando los puertos de entrada de esos
 * módulos: no hay llamadas HTTP internas contra el gateway.
 */
class FacturePDFAdapterUnitTest {

    private static final String ENTERPRISE_ID = "23420850-8547-44d4-9abf-04e9783ed400";
    private static final long THIRD_ID = 41L;

    @Test
    void shouldGenerateSalePdfThroughLocalPorts(@TempDir Path tempDir) throws Exception {
        IEnterpriseSearchManagerPort enterprisePort = mock(IEnterpriseSearchManagerPort.class);
        GetThirdUseCase thirdUseCase = mock(GetThirdUseCase.class);

        Path logoPath = tempDir.resolve("logo.png");
        ImageIO.write(new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB), "png", logoPath.toFile());

        when(enterprisePort.getEnterpriseById(UUID.fromString(ENTERPRISE_ID)))
                .thenReturn(co.unicauca.edu.co.contables.configuration.enterprise.domain.models.Enterprise
                        .builder()
                        .name("Empresa PP8")
                        .nit("900123456")
                        .email("empresa@example.com")
                        .phone("3000000000")
                        .logo(logoPath.toUri().toURL().toString())
                        .build());

        when(thirdUseCase.getThirdById(THIRD_ID, ENTERPRISE_ID))
                .thenReturn(co.unicauca.edu.co.contables.configuration.thirds.domain.model.Third
                        .builder()
                        .verificationNumber(7L)
                        .typeId(TypeId.builder().typeId("NIT").build())
                        .idNumber(901234567L)
                        .names("Cliente")
                        .lastNames("PP8")
                        .address("Calle 1")
                        .country(Country.builder().countryName("Colombia").build())
                        .province(State.builder().stateName("Cauca").build())
                        .city(City.builder().cityName("Popayan").build())
                        .phoneNumber("3100000000")
                        .email("cliente@example.com")
                        .personType(ePersonType.Juridica)
                        .build());

        FacturePDFAdapter adapter = new FacturePDFAdapter();
        ReflectionTestUtils.setField(adapter, "enterpriseSearchManagerPort", enterprisePort);
        ReflectionTestUtils.setField(adapter, "thirdUseCase", thirdUseCase);

        Product product = Product.builder()
                .productId(1L)
                .amount(1D)
                .description("Servicio")
                .descount(0D)
                .vat(0.19D)
                .unitPrice(1000D)
                .subtotal(1000D)
                .build();
        Facture facture = Facture.builder()
                .factId(10L)
                .entId(ENTERPRISE_ID)
                .thId(THIRD_ID)
                .factCode(100L)
                .factObservations("Prueba")
                .factureType(eFactureType.Venta)
                .factProducts(Set.of(product))
                .descounts(0D)
                .factSubtotals(1000D)
                .facSalesTax(190D)
                .facWithholdingSource(0D)
                .build();

        byte[] pdf = adapter.generatePDF(facture);

        assertThat(pdf).isNotNull().isNotEmpty();
        verify(enterprisePort, atLeastOnce()).getEnterpriseById(UUID.fromString(ENTERPRISE_ID));
        verify(thirdUseCase, atLeastOnce()).getThirdById(THIRD_ID, ENTERPRISE_ID);
    }
}
