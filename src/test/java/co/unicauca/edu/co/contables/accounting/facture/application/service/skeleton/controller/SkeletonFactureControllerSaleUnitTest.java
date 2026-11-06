package co.unicauca.edu.co.contables.accounting.facture.application.service.skeleton.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import co.unicauca.edu.co.contables.accounting.facture.application.service.skeleton.dto.SkeletonFactureDetailDto;
import co.unicauca.edu.co.contables.accounting.facture.application.service.skeleton.dto.SkeletonFactureRequestDto;
import co.unicauca.edu.co.contables.accounting.facture.application.service.skeleton.model.SkeletonFacture;
import co.unicauca.edu.co.contables.accounting.facture.application.service.skeleton.model.SkeletonFactureType;
import co.unicauca.edu.co.contables.accounting.facture.application.service.skeleton.repository.SkeletonFactureRepository;
import co.unicauca.edu.co.contables.accounting.facture.application.service.skeleton.service.SaleStockValidator;
import co.unicauca.edu.co.contables.accounting.facture.application.service.skeleton.service.SkeletonEventService;
import co.unicauca.edu.co.contables.accounting.facture.application.service.skeleton.service.SkeletonFactureService;

/**
 * HU5: la validación de stock debe correr ANTES de crear la factura de venta.
 *
 * Si alguien quita la llamada a SaleStockValidator, el kardex rechazaría el
 * movimiento en diferido pero el documento ya quedaría creado.
 */
class SkeletonFactureControllerSaleUnitTest {

    private SkeletonFactureService factureService;
    private SaleStockValidator saleStockValidator;
    private SkeletonFactureController controller;

    @BeforeEach
    void setUp() {
        factureService = mock(SkeletonFactureService.class);
        saleStockValidator = mock(SaleStockValidator.class);
        controller = new SkeletonFactureController(
                factureService,
                mock(SkeletonEventService.class),
                mock(SkeletonFactureRepository.class),
                saleStockValidator);
    }

    @Test
    void createSaleValidatesStockBeforeCreatingTheInvoice() {
        doThrow(new IllegalArgumentException("Stock insuficiente. Solicitado: 9999, Disponible: 10."))
                .when(saleStockValidator).validate(any(SkeletonFactureRequestDto.class));

        assertThatThrownBy(() -> controller.createSale(saleRequest()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Stock insuficiente");

        verify(factureService, never()).createFacture(any(SkeletonFactureRequestDto.class));
    }

    @Test
    void createSalePersistsTheInvoiceWhenStockIsAvailable() {
        SkeletonFactureDetailDto saved = SkeletonFactureDetailDto.builder().id(42L).build();
        SkeletonFacture facture = SkeletonFacture.builder()
                .id(42L)
                .factCode(1001L)
                .entId("enterprise-a")
                .factureType(SkeletonFactureType.SALE)
                .products(new java.util.HashSet<>())
                .build();
        SkeletonFactureRepository repository = mock(SkeletonFactureRepository.class);
        when(repository.findByIdWithProducts(42L)).thenReturn(Optional.of(facture));
        when(factureService.createFacture(any(SkeletonFactureRequestDto.class))).thenReturn(saved);

        controller = new SkeletonFactureController(
                factureService,
                mock(SkeletonEventService.class),
                repository,
                saleStockValidator);

        var response = controller.createSale(saleRequest());

        verify(saleStockValidator).validate(any(SkeletonFactureRequestDto.class));
        verify(factureService).createFacture(any(SkeletonFactureRequestDto.class));
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }

    private SkeletonFactureRequestDto saleRequest() {
        return SkeletonFactureRequestDto.builder()
                .factCode(1001L)
                .entId("enterprise-a")
                .thId(1L)
                .factureType(SkeletonFactureType.SALE)
                .products(java.util.Set.of())
                .build();
    }
}
