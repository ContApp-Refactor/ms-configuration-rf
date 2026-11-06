package co.unicauca.edu.co.contables.accounting.facture.application.service.skeleton.service;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
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
import co.unicauca.edu.co.contables.inventory.stock.domain.port.IMessageServicePort;

/**
 * Regla de negocio HU5: solo se puede vender si el kardex tiene stock suficiente.
 *
 * La venta se rechaza ANTES de crear la factura, no en el kardex diferido.
 */
class SaleStockValidatorUnitTest {

    private IKardexQueryPort kardexQueryPort;
    private IMessageServicePort messageService;
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
        validator = new SaleStockValidator(kardexQueryPort, messageService);
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

    private Kardex lot(int availableQuantity) {
        return Kardex.builder().availableQuantity(availableQuantity).build();
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
