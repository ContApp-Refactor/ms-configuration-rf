package co.unicauca.edu.co.contables.inventory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import co.unicauca.edu.co.contables.configuration.thirds.infrastructure.config.GeographyDataInitializer;
import co.unicauca.edu.co.contables.inventory.products.application.dto.ProductSyncDto;
import co.unicauca.edu.co.contables.inventory.products.infraestructure.config.rabbitConfig.RabbitProductConfig;
import co.unicauca.edu.co.contables.inventory.products.infraestructure.output.messageBroker.ProductEventPublisher;
import co.unicauca.edu.co.contables.inventory.products.infraestructure.output.messageBroker.SpringEventsProductEventPublisher;
import co.unicauca.edu.co.contables.inventory.stock.domain.port.IStockCommandRepositoryPort;
import co.unicauca.edu.co.contables.inventory.stock.infrastructure.adapters.output.jpa.adapter.StockCommandAdapter;
import co.unicauca.edu.co.contables.inventory.stock.infrastructure.adapters.output.messageBroker.SpringEventsProductListener;
import co.unicauca.edu.co.contables.inventory.stock.infrastructure.adapters.output.messageBroker.StockListener;

@SpringBootTest(properties = {
        "spring.sql.init.mode=never",
        "spring.jpa.defer-datasource-initialization=false"
})
@ActiveProfiles("test")
class ProductEventFlowTest {

    @MockitoBean
    private GeographyDataInitializer geographyDataInitializer;

    @MockitoBean
    private co.unicauca.edu.co.contables.inventory.kardex.domain.port.product.IProductCommandRepositoryPort kardexProductCommandPort;

    @MockitoBean
    private co.unicauca.edu.co.contables.inventory.peps.domain.port.output.command.IProductCommandOutPutPort pepsProductCommandPort;

    @MockitoBean
    private StockCommandAdapter stockCommandAdapter;

    @Autowired
    private IStockCommandRepositoryPort stockCommandPort;

    @Autowired
    private SpringEventsProductEventPublisher publisher;

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    @DisplayName("CREATED - kardex, peps y stock reaccionan al mismo evento en proceso")
    void created_llegaAKardexPepsYStock() {
        publisher.publishCreatedStockEvent(postmanProduct());

        ArgumentCaptor<co.unicauca.edu.co.contables.inventory.kardex.domain.model.Product> kardexCaptor =
                ArgumentCaptor.forClass(co.unicauca.edu.co.contables.inventory.kardex.domain.model.Product.class);
        verify(kardexProductCommandPort).save(kardexCaptor.capture());

        ArgumentCaptor<co.unicauca.edu.co.contables.inventory.peps.domain.model.Product> pepsCaptor =
                ArgumentCaptor.forClass(co.unicauca.edu.co.contables.inventory.peps.domain.model.Product.class);
        verify(pepsProductCommandPort).save(pepsCaptor.capture());

        ArgumentCaptor<co.unicauca.edu.co.contables.inventory.stock.domain.model.Stock> stockCaptor =
                ArgumentCaptor.forClass(co.unicauca.edu.co.contables.inventory.stock.domain.model.Stock.class);
        verify(stockCommandPort).save(stockCaptor.capture());

        assertKardex(kardexCaptor.getValue());
        assertPeps(pepsCaptor.getValue());
        assertStock(stockCaptor.getValue());
    }

    @Test
    @DisplayName("UPDATED - kardex actualiza, peps guarda y stock actualiza por productId/name")
    void updated_llegaALosTresConsumidores() {
        publisher.publishUpdatedStockEvent(postmanProduct());

        ArgumentCaptor<co.unicauca.edu.co.contables.inventory.kardex.domain.model.Product> kardexCaptor =
                ArgumentCaptor.forClass(co.unicauca.edu.co.contables.inventory.kardex.domain.model.Product.class);
        verify(kardexProductCommandPort).update(kardexCaptor.capture());
        assertEquals(9001L, kardexCaptor.getValue().getProductId());

        verify(pepsProductCommandPort).save(any(co.unicauca.edu.co.contables.inventory.peps.domain.model.Product.class));
        verify(stockCommandPort).update(9001L, "Producto Test");
        verify(stockCommandPort, never()).save(any());
    }

    @Test
    @DisplayName("DELETED - kardex, peps y stock eliminan por productId")
    void deleted_llegaALosTresConsumidores() {
        publisher.publishDeletedStockEvent(postmanProduct());

        verify(kardexProductCommandPort).delete(9001L);
        verify(pepsProductCommandPort).delete(9001L);
        verify(stockCommandPort).deleteByProductId(9001L);
    }

    @Test
    @DisplayName("Perfil por defecto - publicador de eventos activo y topología Rabbit de productos ausente")
    void perfilPorDefecto_soloSpringEvents() {
        assertNotNull(applicationContext.getBean(SpringEventsProductEventPublisher.class));
        assertNotNull(applicationContext.getBean("stockSpringEventsProductListener", SpringEventsProductListener.class));

        assertThrows(NoSuchBeanDefinitionException.class,
                () -> applicationContext.getBean(ProductEventPublisher.class));
        assertThrows(NoSuchBeanDefinitionException.class,
                () -> applicationContext.getBean(RabbitProductConfig.class));
        assertThrows(NoSuchBeanDefinitionException.class,
                () -> applicationContext.getBean(StockListener.class));
        assertThrows(NoSuchBeanDefinitionException.class,
                () -> applicationContext.getBean("productStockQueue"));
        assertThrows(NoSuchBeanDefinitionException.class,
                () -> applicationContext.getBean("productFanoutExchange"));
    }

    private ProductSyncDto postmanProduct() {

        return new ProductSyncDto(9001L, "Producto Test", "REF-TEST-002", "ENT-1",
                "Caja x 12 unidades", null, 0.0, true);
    }

    private void assertKardex(co.unicauca.edu.co.contables.inventory.kardex.domain.model.Product product) {
        assertEquals(9001L, product.getProductId());
        assertEquals("Producto Test", product.getName());
        assertEquals("REF-TEST-002", product.getReference());
        assertEquals("ENT-1", product.getEnterpriseId());
        assertEquals("Caja x 12 unidades", product.getPresentation());
        assertTrue(product.isState());
    }

    private void assertPeps(co.unicauca.edu.co.contables.inventory.peps.domain.model.Product product) {
        assertEquals(9001L, product.getProductId());
        assertEquals("Producto Test", product.getName());
        assertEquals("REF-TEST-002", product.getReference());
        assertEquals("ENT-1", product.getEnterpriseId());
        assertEquals("Caja x 12 unidades", product.getPresentation());
        assertTrue(product.isState());
    }

    private void assertStock(co.unicauca.edu.co.contables.inventory.stock.domain.model.Stock stock) {
        assertEquals(9001L, stock.getProductId());
        assertEquals("Producto Test", stock.getName());
        assertEquals("ENT-1", stock.getEnterpriseId());
        assertEquals(0, stock.getQuantity());
        assertEquals(true, stock.isState());
    }
}
