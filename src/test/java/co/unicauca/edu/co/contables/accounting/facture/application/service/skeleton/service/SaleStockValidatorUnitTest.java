package co.unicauca.edu.co.contables.accounting.facture.application.service.skeleton.service;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import co.unicauca.edu.co.contables.accounting.facture.application.service.skeleton.dto.SkeletonFactureRequestDto;
import co.unicauca.edu.co.contables.accounting.facture.application.service.skeleton.dto.SkeletonProductRequestDto;
import co.unicauca.edu.co.contables.inventory.peps.application.ports.input.IKardexQueryPort;
import co.unicauca.edu.co.contables.inventory.peps.domain.model.Kardex;
import co.unicauca.edu.co.contables.inventory.products.application.ports.input.IProductServicePort;
import co.unicauca.edu.co.contables.inventory.products.application.ports.input.IProductTypeServicePort;
import co.unicauca.edu.co.contables.inventory.products.domain.model.Product;
import co.unicauca.edu.co.contables.inventory.products.domain.model.ProductType;
import co.unicauca.edu.co.contables.inventory.stock.domain.port.IMessageServicePort;

/**
 * Regla de negocio HU5: solo se puede vender si el kardex tiene stock suficiente.
 *
 * La venta se rechaza ANTES de crear la factura, no en el kardex diferido.
 */
class SaleStockValidatorUnitTest {

    private IKardexQueryPort kardexQueryPort;
    private IMessageServicePort messageService;
    private IProductServicePort productServicePort;
    private IProductTypeServicePort productTypeServicePort;
    private SaleStockValidator validator;

    @BeforeEach
    void setUp() {
        kardexQueryPort = mock(IKardexQueryPort.class);
        messageService = new IMessageServicePort() {
            @Override
            public String getMessage(String key, Object... args) {
                return String.format("Stock insuficiente. Solicitado: %s, Disponible: %s.", args[0], args[1]);
            }

            @Override
            public String getMessage(String key, String defaultMessage, Object... args) {
                return defaultMessage;
            }
        };
        productServicePort = mock(IProductServicePort.class);
        productTypeServicePort = mock(IProductTypeServicePort.class);
        // Por defecto los productos del test son inventariables: se valida stock.
        when(productServicePort.findById(anyLong(), any())).thenReturn(productOfType(5L));
        when(productTypeServicePort.getProductTypeByIdAndEnterpriseId(anyLong(), any()))
                .thenReturn(typeInventoryable(true));
        validator = new SaleStockValidator(
                kardexQueryPort, messageService, productServicePort, productTypeServicePort);
    }

    @Test
    void skipsValidationWhenTheProductTypeIsNotInventoryable() {
        when(productTypeServicePort.getProductTypeByIdAndEnterpriseId(anyLong(), any()))
                .thenReturn(typeInventoryable(false));

        assertThatCode(() -> validator.validate(request(line(10L, 9999))))
                .doesNotThrowAnyException();

        verify(kardexQueryPort, never()).getKardexAvailableQuantityByProduct(anyLong());
    }

    @Test
    void stillValidatesWhenTheProductTypeCannotBeDetermined() {
        when(productServicePort.findById(anyLong(), any())).thenThrow(new RuntimeException("producto inexistente"));

        assertThatThrownBy(() -> validator.validate(request(line(10L, 8))))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsWhenRequestedExceedsAvailableStock() {
        when(kardexQueryPort.getKardexAvailableQuantityByProduct(10L)).thenReturn(List.of(lot(7)));

        assertThatThrownBy(() -> validator.validate(request(line(10L, 8))))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Stock insuficiente")
                .hasMessageContaining("Solicitado: 8")
                .hasMessageContaining("Disponible: 7");
    }

    @Test
    void allowsWhenRequestedEqualsAvailableStock() {
        when(kardexQueryPort.getKardexAvailableQuantityByProduct(10L)).thenReturn(List.of(lot(7)));

        assertThatCode(() -> validator.validate(request(line(10L, 7))))
                .doesNotThrowAnyException();
    }

    @Test
    void allowsWhenRequestedIsBelowAvailableStock() {
        when(kardexQueryPort.getKardexAvailableQuantityByProduct(10L)).thenReturn(List.of(lot(7)));

        assertThatCode(() -> validator.validate(request(line(10L, 3))))
                .doesNotThrowAnyException();
    }

    @Test
    void treatsProductWithoutKardexLotsAsZeroStock() {
        when(kardexQueryPort.getKardexAvailableQuantityByProduct(99L)).thenReturn(List.of());

        assertThatThrownBy(() -> validator.validate(request(line(99L, 1))))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void sumsEveryAvailableLotOfTheProduct() {
        when(kardexQueryPort.getKardexAvailableQuantityByProduct(10L))
                .thenReturn(List.of(lot(7), lot(3)));

        assertThatCode(() -> validator.validate(request(line(10L, 10))))
                .doesNotThrowAnyException();

        assertThatThrownBy(() -> validator.validate(request(line(10L, 11))))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void validatesEveryLineOfTheSale() {
        when(kardexQueryPort.getKardexAvailableQuantityByProduct(10L)).thenReturn(List.of(lot(7)));
        when(kardexQueryPort.getKardexAvailableQuantityByProduct(20L)).thenReturn(List.of(lot(5)));

        assertThatThrownBy(() -> validator.validate(request(line(10L, 1), line(20L, 6))))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void ignoresSaleWithoutLines() {
        SkeletonFactureRequestDto empty = SkeletonFactureRequestDto.builder().products(Set.of()).build();

        assertThatCode(() -> validator.validate(empty)).doesNotThrowAnyException();
        verify(kardexQueryPort, never()).getKardexAvailableQuantityByProduct(any());
    }

    @Test
    void validateLineRejectsASingleLineWithoutEnoughStock() {
        when(kardexQueryPort.getKardexAvailableQuantityByProduct(12L)).thenReturn(List.of(lot(30)));

        assertThatThrownBy(() -> validator.validateLine("enterprise-a", 12L, 40.0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Solicitado: 40")
                .hasMessageContaining("Disponible: 30");
    }

    @Test
    void validateLineAcceptsALineWithinTheAvailableStock() {
        when(kardexQueryPort.getKardexAvailableQuantityByProduct(12L)).thenReturn(List.of(lot(30)));

        assertThatCode(() -> validator.validateLine("enterprise-a", 12L, 25))
                .doesNotThrowAnyException();

        verify(kardexQueryPort).getKardexAvailableQuantityByProduct(12L);
    }

    private Kardex lot(int availableQuantity) {
        return Kardex.builder().availableQuantity(availableQuantity).build();
    }

    private Product productOfType(Long productTypeId) {
        return Product.builder().id(10L).productTypeId(productTypeId).enterpriseId("enterprise-a").build();
    }

    private ProductType typeInventoryable(boolean inventoryable) {
        return ProductType.builder().id(5L).inventoryable(inventoryable).build();
    }

    private SkeletonProductRequestDto line(Long productId, int amount) {
        return SkeletonProductRequestDto.builder()
                .productId(productId)
                .amount(amount)
                .unitPrice(1000.0)
                .subtotal(1000.0 * amount)
                .build();
    }

    private SkeletonFactureRequestDto request(SkeletonProductRequestDto... lines) {
        return SkeletonFactureRequestDto.builder().products(Set.of(lines)).build();
    }
}
