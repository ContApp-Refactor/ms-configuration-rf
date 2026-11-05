package co.unicauca.edu.co.contables.inventory.products.unit.application.service.product;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import co.unicauca.edu.co.contables.inventory.products.application.ports.output.IProductPersistencePort;
import co.unicauca.edu.co.contables.inventory.products.application.service.product.ProductCostService;
import co.unicauca.edu.co.contables.inventory.products.domain.model.Product;

import static org.assertj.core.api.Assertions.assertThat;

class ProductCostServiceUnitTest {

    private IProductPersistencePort productPersistencePort;
    private ProductCostService service;

    @BeforeEach
    void setUp() {
        productPersistencePort = org.mockito.Mockito.mock(IProductPersistencePort.class);
        service = new ProductCostService(productPersistencePort);
    }

    @Test
    void updateLastPurchaseCostPersistsTheNewCostOnTheExistingProduct() {
        Product existing = Product.builder().id(10L).name("Teclado").cost(1000.0).build();
        when(productPersistencePort.findById(10L)).thenReturn(Optional.of(existing));

        service.updateLastPurchaseCost(10L, 7500.0);

        ArgumentCaptor<Product> saved = ArgumentCaptor.forClass(Product.class);
        verify(productPersistencePort).create(saved.capture());
        assertThat(saved.getValue().getCost()).isEqualTo(7500.0);
        assertThat(saved.getValue().getName()).isEqualTo("Teclado");
    }

    @Test
    void updateLastPurchaseCostDoesNothingWhenTheProductDoesNotExist() {
        when(productPersistencePort.findById(99L)).thenReturn(Optional.empty());

        service.updateLastPurchaseCost(99L, 500.0);

        verify(productPersistencePort, never()).create(any(Product.class));
    }

    @Test
    void updateLastPurchaseCostIgnoresANullProductId() {
        service.updateLastPurchaseCost(null, 500.0);

        verify(productPersistencePort, never()).findById(any());
        verify(productPersistencePort, never()).create(any(Product.class));
    }
}
