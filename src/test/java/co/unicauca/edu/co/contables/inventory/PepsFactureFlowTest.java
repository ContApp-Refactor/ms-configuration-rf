package co.unicauca.edu.co.contables.inventory;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import co.unicauca.edu.co.contables.accounting.debt_payments.domain.ports.IMessageErrorHandlingPort;
import co.unicauca.edu.co.contables.accounting.facture.infraestructure.adapters.config.rabbitConfig.RabbitPEPSConfig;
import co.unicauca.edu.co.contables.accounting.facture.infraestructure.adapters.output.messageBroker.PepsEventPublisher;
import co.unicauca.edu.co.contables.accounting.facture.infraestructure.adapters.output.messageBroker.SpringEventsPepsEventPublisher;
import co.unicauca.edu.co.contables.accounting.facture.infraestructure.adapters.output.messageBroker.dto.KardexPurchaseDtoRequest;
import co.unicauca.edu.co.contables.accounting.facture.infraestructure.adapters.output.messageBroker.dto.KardexSalesDtoRequest;
import co.unicauca.edu.co.contables.accounting.facture.infraestructure.adapters.output.messageBroker.dto.PepsPurchaseEvent;
import co.unicauca.edu.co.contables.accounting.facture.infraestructure.adapters.output.messageBroker.dto.PepsSalesEvent;
import co.unicauca.edu.co.contables.accounting.facture.infraestructure.adapters.output.messageBroker.dto.WeightedAveragePurchaseEvent;
import co.unicauca.edu.co.contables.accounting.facture.infraestructure.adapters.output.messageBroker.enums.EventFactureType;
import co.unicauca.edu.co.contables.configuration.thirds.infrastructure.config.GeographyDataInitializer;
import co.unicauca.edu.co.contables.inventory.peps.domain.model.Kardex;
import co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.output.messageBroker.SpringEventsFactureListener;

@SpringBootTest(properties = {
        "spring.sql.init.mode=never",
        "spring.jpa.defer-datasource-initialization=false"
})
@ActiveProfiles("test")
@DisplayName("facture → peps — flujo en proceso (reemplazo de peps.exchange)")
class PepsFactureFlowTest {

    private static final long PRODUCT_ID = 9001L;
    private static final long FACT_CODE = 12345L;
    private static final BigDecimal UNIT_PRICE = new BigDecimal("15.75");

    @MockitoBean
    private GeographyDataInitializer geographyDataInitializer;

    @MockitoBean
    private co.unicauca.edu.co.contables.inventory.peps.application.ports.input.IKardexCommandPort pepsKardexCommandPort;

    @MockitoBean
    private co.unicauca.edu.co.contables.inventory.kardex.application.ports.input.kardex.IKardexCommandPort kardexCommandPort;

    @MockitoBean
    private IMessageErrorHandlingPort messageErrorHandlingPort;

    @Autowired
    private ApplicationEventPublisher events;

    @Autowired
    private ApplicationContext applicationContext;

    @ParameterizedTest(name = "{0}")
    @MethodSource("happyEvents")
    @DisplayName("evento válido - el listener traduce el contrato y registra el movimiento PEPS")
    void happyPath_registraMovimiento(EventFactureType type, boolean purchase) {
        events.publishEvent(event(type, purchase));

        ArgumentCaptor<Kardex> captor = ArgumentCaptor.forClass(Kardex.class);
        await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> verifyCommand(type, captor));

        Kardex kardex = captor.getValue();
        assertEquals(PRODUCT_ID, kardex.getProduct().getProductId());
        assertEquals(String.valueOf(FACT_CODE), kardex.getFactCode());
        assertEquals(quantityFor(type), kardex.getQuantity());
        assertEquals(detailsFor(type), kardex.getDetails());
        if (purchase) {
            assertEquals(UNIT_PRICE, kardex.getUnitPrice());
        } else {
            assertNull(kardex.getUnitPrice());
        }

        verifyNoInteractions(messageErrorHandlingPort);
        verifyNoInteractions(kardexCommandPort);
    }

    @Test
    @DisplayName("evento de kardex - no llega al listener de peps")
    void weightedAverageEvent_noLlegaAlListenerPeps() {
        events.publishEvent(new WeightedAveragePurchaseEvent(
                new KardexPurchaseDtoRequest(50, FACT_CODE, UNIT_PRICE, PRODUCT_ID, "Compra de prueba"),
                EventFactureType.PURCHASE));

        await().atMost(Duration.ofSeconds(5)).untilAsserted(() ->
                verify(kardexCommandPort).registerPurchase(any()));

        verifyNoInteractions(pepsKardexCommandPort);
    }

    @Test
    @DisplayName("Perfil por defecto - sólo el nuevo publicador y el nuevo listener están activos")
    void perfilPorDefecto_soloSpringEvents() {
        assertNotNull(applicationContext.getBean("pepsEventPublisher",
                SpringEventsPepsEventPublisher.class));
        assertNotNull(applicationContext.getBean("pepsSpringEventsFactureListener",
                SpringEventsFactureListener.class));

        assertThrows(NoSuchBeanDefinitionException.class,
                () -> applicationContext.getBean(PepsEventPublisher.class));
        assertThrows(NoSuchBeanDefinitionException.class,
                () -> applicationContext.getBean(
                        co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.output.messageBroker.FactureListener.class));
        assertThrows(NoSuchBeanDefinitionException.class,
                () -> applicationContext.getBean(RabbitPEPSConfig.class));
    }

    private static Stream<Arguments> happyEvents() {
        return Stream.of(
                Arguments.of(EventFactureType.PURCHASE, true),
                Arguments.of(EventFactureType.SALE, false),
                Arguments.of(EventFactureType.RETURNONSALE, false),
                Arguments.of(EventFactureType.RETURNONPURCHASE, false),
                Arguments.of(EventFactureType.NONCOMMERCIALENTRY, true),
                Arguments.of(EventFactureType.NONCOMMERCIALEXIT, false));
    }

    private Object event(EventFactureType type, boolean purchase) {
        if (purchase) {
            return new PepsPurchaseEvent(new KardexPurchaseDtoRequest(
                    quantityFor(type), FACT_CODE, UNIT_PRICE, PRODUCT_ID, detailsFor(type)), type);
        }

        return new PepsSalesEvent(new KardexSalesDtoRequest(
                quantityFor(type), FACT_CODE, PRODUCT_ID, detailsFor(type)), type);
    }

    private void verifyCommand(EventFactureType type, ArgumentCaptor<Kardex> captor) {
        switch (type) {
            case PURCHASE -> verify(pepsKardexCommandPort).registerPurchase(captor.capture());
            case SALE -> verify(pepsKardexCommandPort).registerSale(captor.capture());
            case RETURNONSALE -> verify(pepsKardexCommandPort).registerSaleReturn(captor.capture());
            case RETURNONPURCHASE -> verify(pepsKardexCommandPort).registerPurchaseReturn(captor.capture());
            case NONCOMMERCIALENTRY -> verify(pepsKardexCommandPort).registerNonCommercialEntry(captor.capture());
            case NONCOMMERCIALEXIT -> verify(pepsKardexCommandPort).registerNonCommercialExit(captor.capture());
        }
    }

    private static int quantityFor(EventFactureType type) {
        return switch (type) {
            case PURCHASE, NONCOMMERCIALENTRY -> 50;
            case SALE, RETURNONSALE, RETURNONPURCHASE, NONCOMMERCIALEXIT -> 10;
        };
    }

    private static String detailsFor(EventFactureType type) {
        return switch (type) {
            case PURCHASE -> "Compra de prueba";
            case SALE -> "Venta de prueba";
            case RETURNONSALE -> "Devolución de Venta prueba";
            case RETURNONPURCHASE -> "Devolución de Compra prueba";
            case NONCOMMERCIALENTRY -> "Compra no comercial de prueba";
            case NONCOMMERCIALEXIT -> "Venta no comercial de prueba";
        };
    }
}
