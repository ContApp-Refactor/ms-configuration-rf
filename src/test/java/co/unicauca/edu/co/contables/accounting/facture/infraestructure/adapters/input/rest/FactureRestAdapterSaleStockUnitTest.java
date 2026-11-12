package co.unicauca.edu.co.contables.accounting.facture.infraestructure.adapters.input.rest;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import co.unicauca.edu.co.contables.accounting.facture.application.ports.input.CreateFactureUseCase;
import co.unicauca.edu.co.contables.accounting.facture.application.ports.input.GenerateFacturePDFUseCase;
import co.unicauca.edu.co.contables.accounting.facture.application.ports.input.GetFactureUseCase;
import co.unicauca.edu.co.contables.accounting.facture.application.ports.input.ListFactureUseCase;
import co.unicauca.edu.co.contables.accounting.facture.application.service.skeleton.service.SaleStockValidator;
import co.unicauca.edu.co.contables.accounting.facture.domain.model.Facture;
import co.unicauca.edu.co.contables.accounting.facture.domain.model.Product;
import co.unicauca.edu.co.contables.accounting.facture.domain.model.eFactureType;
import co.unicauca.edu.co.contables.accounting.facture.infraestructure.adapters.input.rest.data.request.FactureCreateRequest;
import co.unicauca.edu.co.contables.accounting.facture.infraestructure.adapters.input.rest.mapper.FactureRestMapper;

/**
 * HU5: POST /api/factures/ es el camino que usa el front de Factura de Venta.
 *
 * Verifico que una VENTA valide stock antes de persistir y antes de generar el
 * PDF, y que una COMPRA no quede bloqueada por esa validación.
 */
class FactureRestAdapterSaleStockUnitTest {

    private static final String ENTERPRISE = "enterprise-a";

    private CreateFactureUseCase createFactureUseCase;
    private GenerateFacturePDFUseCase generateFacturePDFUseCase;
    private FactureRestMapper factureRestMapper;
    private SaleStockValidator saleStockValidator;
    private FactureRestAdapter adapter;

    @BeforeEach
    void setUp() {
        createFactureUseCase = mock(CreateFactureUseCase.class);
        generateFacturePDFUseCase = mock(GenerateFacturePDFUseCase.class);
        factureRestMapper = mock(FactureRestMapper.class);
        saleStockValidator = mock(SaleStockValidator.class);
        adapter = new FactureRestAdapter(
                createFactureUseCase,
                mock(GetFactureUseCase.class),
                mock(ListFactureUseCase.class),
                generateFacturePDFUseCase,
                factureRestMapper,
                saleStockValidator);
    }

    @Test
    void saleValidatesStockBeforeGeneratingThePdfOrPersisting() {
        FactureCreateRequest request = request(eFactureType.Venta, product(12L, 40.0));
        when(factureRestMapper.toFacture(request))
                .thenReturn(Facture.builder().factureType(eFactureType.Venta).build());
        doThrow(new IllegalArgumentException("Stock insuficiente. Solicitado: 40, Disponible: 30."))
                .when(saleStockValidator).validateLine(any(), anyLong(), any());

        assertThatThrownBy(() -> adapter.createFacture(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Stock insuficiente");

        verify(saleStockValidator).validateLine(ENTERPRISE, 12L, 40.0);
        verify(generateFacturePDFUseCase, never()).generetePDFFacture(any());
        verify(createFactureUseCase, never()).createFacture(any());
    }

    @Test
    void saleValidatesEveryLineOfTheInvoice() {
        FactureCreateRequest request = request(eFactureType.Venta,
                product(12L, 40.0), product(13L, 5.0));
        when(factureRestMapper.toFacture(request))
                .thenReturn(Facture.builder().factureType(eFactureType.Venta).build());
        when(generateFacturePDFUseCase.generetePDFFacture(any())).thenReturn(new byte[] {1, 2, 3});
        when(createFactureUseCase.createFacture(any()))
                .thenReturn(Facture.builder().factId(7L).build());

        adapter.createFacture(request);

        verify(saleStockValidator).validateLine(ENTERPRISE, 12L, 40.0);
        verify(saleStockValidator).validateLine(ENTERPRISE, 13L, 5.0);
        verify(saleStockValidator, times(2)).validateLine(any(), anyLong(), any());
    }

    @Test
    void purchaseIsNotBlockedByTheSaleStockCheck() {
        FactureCreateRequest request = request(eFactureType.Compra, product(12L, 9999.0));
        when(factureRestMapper.toFacture(request))
                .thenReturn(Facture.builder().factureType(eFactureType.Compra).build());
        when(generateFacturePDFUseCase.generetePDFFacture(any())).thenReturn(new byte[] {1, 2, 3});
        when(createFactureUseCase.createFacture(any()))
                .thenReturn(Facture.builder().factId(8L).build());

        adapter.createFacture(request);

        verify(saleStockValidator, never()).validateLine(any(), anyLong(), any());
    }

    private FactureCreateRequest request(eFactureType type, Product... products) {
        return FactureCreateRequest.builder()
                .entId(ENTERPRISE)
                .thId(1L)
                .factCode(1001L)
                .factureType(type)
                .factProducts(List.of(products))
                .factSubtotals(0.0)
                .facSalesTax(0.0)
                .facWithholdingSource(0.0)
                .factObservations("test")
                .descounts(0.0)
                .build();
    }

    private Product product(long productId, double amount) {
        return Product.builder().productId(productId).amount(amount).unitPrice(1000.0).subtotal(1000.0 * amount).build();
    }
}
