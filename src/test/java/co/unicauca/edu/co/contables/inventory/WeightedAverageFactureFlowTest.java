package co.unicauca.edu.co.contables.inventory;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
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
import co.unicauca.edu.co.contables.accounting.facture.infraestructure.adapters.config.rabbitConfig.RabbitWeightedAverageConfig;
import co.unicauca.edu.co.contables.accounting.facture.infraestructure.adapters.output.messageBroker.SpringEventsWeightedAverageEventPublisher;
import co.unicauca.edu.co.contables.accounting.facture.infraestructure.adapters.output.messageBroker.WeightedAverageEventPublisher;
import co.unicauca.edu.co.contables.accounting.facture.infraestructure.adapters.output.messageBroker.dto.KardexPurchaseDtoRequest;
import co.unicauca.edu.co.contables.accounting.facture.infraestructure.adapters.output.messageBroker.dto.KardexSalesDtoRequest;
import co.unicauca.edu.co.contables.accounting.facture.infraestructure.adapters.output.messageBroker.dto.WeightedAveragePurchaseEvent;
import co.unicauca.edu.co.contables.accounting.facture.infraestructure.adapters.output.messageBroker.dto.WeightedAverageSalesEvent;
import co.unicauca.edu.co.contables.accounting.facture.infraestructure.adapters.output.messageBroker.enums.EventFactureType;
import co.unicauca.edu.co.contables.configuration.thirds.infrastructure.config.GeographyDataInitializer;
import co.unicauca.edu.co.contables.inventory.kardex.application.ports.input.kardex.IKardexCommandPort;
import co.unicauca.edu.co.contables.inventory.kardex.domain.model.Kardex;
import co.unicauca.edu.co.contables.inventory.kardex.domain.model.MovementType;
import co.unicauca.edu.co.contables.inventory.kardex.infrastructure.adapters.output.messageBroker.FactureListener;
import co.unicauca.edu.co.contables.inventory.kardex.infrastructure.adapters.output.messageBroker.SpringEventsFactureListener;

@SpringBootTest(properties = {
        "spring.sql.init.mode=never",
        "spring.jpa.defer-datasource-initialization=false"
})
@ActiveProfiles("test")
@DisplayName("inventory-weighted-average-rf §3–6, §9–10 — facture → kardex")
class WeightedAverageFactureFlowTest {

    private static final long PRODUCT_ID = 9001L;
    private static final long FACT_CODE = 12345L;
    private static final BigDecimal UNIT_PRICE = new BigDecimal("15.75");

    @MockitoBean
    private GeographyDataInitializer geographyDataInitializer;

    @MockitoBean
    private IKardexCommandPort kardexCommandPort;

    @MockitoBean
    private IMessageErrorHandlingPort messageErrorHandlingPort;

    @Autowired
    private ApplicationEventPublisher events;

    @Autowired
    private ApplicationContext applicationContext;

    @ParameterizedTest(name = "{0}")
    @MethodSource("happyEvents")
    @DisplayName("evento válido - el listener traduce el contrato y registra el movimiento")
    void happyPath_registraMovimiento(EventFactureType type, boolean purchase) {
        events.publishEvent(event(type, purchase));

        ArgumentCaptor<Kardex> captor = ArgumentCaptor.forClass(Kardex.class);
        await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> verifyCommand(type, captor));

        Kardex kardex = captor.getValue();
        assertEquals(PRODUCT_ID, kardex.getProductId());
        assertEquals(String.valueOf(factCodeFor(type)), kardex.getFactCode());
        assertEquals(quantityFor(type), kardex.getQuantity());
        assertEquals(detailsFor(type), kardex.getDetails());
        assertEquals(movementFor(type), kardex.getType());
        if (purchase) {
            assertEquals(UNIT_PRICE, kardex.getUnitPrice());
        } else {
            assertNull(kardex.getUnitPrice());
        }
        verifyNoInteractions(messageErrorHandlingPort);
    }

    @ParameterizedTest(name = "{0} - {2} missing")
    @MethodSource("invalidEvents")
    @DisplayName("evento inválido - persiste el error de validación sin lanzar")
    void invalidEvent_persisteError(EventFactureType type, boolean purchase, String field) {
        Object event = event(type, purchase);
        mutate(event, field);

        assertDoesNotThrow(() -> events.publishEvent(event));

        String expectedType = "type".equals(field) ? null : type.name();
        await().atMost(Duration.ofSeconds(5)).untilAsserted(() ->
                verifyValidationError(expectedType, validationFragment(field)));

        verifyNoInteractions(kardexCommandPort);
    }

    @Test
    @DisplayName("Perfil por defecto - sólo el nuevo publicador y el nuevo listener están activos")
    void perfilPorDefecto_soloSpringEvents() {
        assertNotNull(applicationContext.getBean("weightedAverageEventPublisher",
                SpringEventsWeightedAverageEventPublisher.class));
        assertNotNull(applicationContext.getBean("kardexSpringEventsFactureListener",
                SpringEventsFactureListener.class));

        assertThrows(NoSuchBeanDefinitionException.class,
                () -> applicationContext.getBean(WeightedAverageEventPublisher.class));
        assertThrows(NoSuchBeanDefinitionException.class,
                () -> applicationContext.getBean(FactureListener.class));
        assertThrows(NoSuchBeanDefinitionException.class,
                () -> applicationContext.getBean(RabbitWeightedAverageConfig.class));
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

    private static Stream<Arguments> invalidEvents() {
        return Stream.of(

                Arguments.of(EventFactureType.PURCHASE, true, "quantity"),
                Arguments.of(EventFactureType.PURCHASE, true, "factCode"),
                Arguments.of(EventFactureType.PURCHASE, true, "productId"),
                Arguments.of(EventFactureType.PURCHASE, true, "unitPrice"),
                Arguments.of(EventFactureType.PURCHASE, true, "type"),

                Arguments.of(EventFactureType.SALE, false, "quantity"),
                Arguments.of(EventFactureType.SALE, false, "factCode"),
                Arguments.of(EventFactureType.SALE, false, "productId"),

                Arguments.of(EventFactureType.RETURNONSALE, false, "quantity"),
                Arguments.of(EventFactureType.RETURNONSALE, false, "factCode"),
                Arguments.of(EventFactureType.RETURNONSALE, false, "productId"),
                Arguments.of(EventFactureType.RETURNONSALE, false, "type"),

                Arguments.of(EventFactureType.RETURNONPURCHASE, false, "quantity"),
                Arguments.of(EventFactureType.RETURNONPURCHASE, false, "factCode"),
                Arguments.of(EventFactureType.RETURNONPURCHASE, false, "productId"),
                Arguments.of(EventFactureType.RETURNONPURCHASE, false, "type"),

                Arguments.of(EventFactureType.NONCOMMERCIALENTRY, true, "quantity"),
                Arguments.of(EventFactureType.NONCOMMERCIALENTRY, true, "factCode"),
                Arguments.of(EventFactureType.NONCOMMERCIALENTRY, true, "productId"),
                Arguments.of(EventFactureType.NONCOMMERCIALENTRY, true, "unitPrice"),
                Arguments.of(EventFactureType.NONCOMMERCIALENTRY, true, "type"),

                Arguments.of(EventFactureType.NONCOMMERCIALEXIT, false, "quantity"),
                Arguments.of(EventFactureType.NONCOMMERCIALEXIT, false, "factCode"),
                Arguments.of(EventFactureType.NONCOMMERCIALEXIT, false, "productId"));
    }

    private Object event(EventFactureType type, boolean purchase) {
        if (purchase) {
            return new WeightedAveragePurchaseEvent(new KardexPurchaseDtoRequest(
                    quantityFor(type), factCodeFor(type), UNIT_PRICE, PRODUCT_ID, detailsFor(type)), type);
        }

        return new WeightedAverageSalesEvent(new KardexSalesDtoRequest(
                quantityFor(type), factCodeFor(type), PRODUCT_ID, detailsFor(type)), type);
    }

    private void mutate(Object event, String field) {
        if (event instanceof WeightedAveragePurchaseEvent purchaseEvent) {
            switch (field) {
                case "quantity" -> purchaseEvent.getData().setQuantity(0);
                case "factCode" -> purchaseEvent.getData().setFactCode(null);
                case "productId" -> purchaseEvent.getData().setProductId(null);
                case "unitPrice" -> purchaseEvent.getData().setUnitPrice(null);
                case "type" -> purchaseEvent.setType(null);
                default -> throw new IllegalArgumentException("Campo no soportado: " + field);
            }
            return;
        }

        WeightedAverageSalesEvent salesEvent = (WeightedAverageSalesEvent) event;
        switch (field) {
            case "quantity" -> salesEvent.getData().setQuantity(0);
            case "factCode" -> salesEvent.getData().setFactCode(null);
            case "productId" -> salesEvent.getData().setProductId(null);
            case "type" -> salesEvent.setType(null);
            default -> throw new IllegalArgumentException("Campo no soportado: " + field);
        }
    }

    private void verifyCommand(EventFactureType type, ArgumentCaptor<Kardex> captor) {
        switch (type) {
            case PURCHASE, NONCOMMERCIALENTRY -> verify(kardexCommandPort).registerPurchase(captor.capture());
            case SALE, NONCOMMERCIALEXIT -> verify(kardexCommandPort).registerSale(captor.capture());
            case RETURNONSALE -> verify(kardexCommandPort).registerReturnOnSale(captor.capture());
            case RETURNONPURCHASE -> verify(kardexCommandPort).registerReturnOnPurchase(captor.capture());
        }
    }

    private void verifyValidationError(String expectedType, String fragment) {
        if (expectedType == null) {
            verify(messageErrorHandlingPort).saveProcessingError(
                    isNull(),
                    contains(fragment),
                    anyString(),
                    eq("Kardex"));
        } else {
            verify(messageErrorHandlingPort).saveProcessingError(
                    eq(expectedType),
                    contains(fragment),
                    anyString(),
                    eq("Kardex"));
        }
    }

    private static String validationFragment(String field) {
        return switch (field) {
            case "quantity" -> "quantity";
            case "factCode" -> "factCode";
            case "productId" -> "productId";
            case "unitPrice" -> "unitPrice";
            case "type" -> "Event type is null";
            default -> throw new IllegalArgumentException("Campo no soportado: " + field);
        };
    }

    private static int quantityFor(EventFactureType type) {
        return switch (type) {
            case PURCHASE, NONCOMMERCIALENTRY -> 50;
            case SALE, RETURNONSALE, RETURNONPURCHASE, NONCOMMERCIALEXIT -> 10;
        };
    }

    private static long factCodeFor(EventFactureType type) {
        return type == EventFactureType.NONCOMMERCIALEXIT ? 123456L : FACT_CODE;
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

    private static MovementType movementFor(EventFactureType type) {
        return switch (type) {
            case PURCHASE -> MovementType.PURCHASE;
            case SALE -> MovementType.SALE;
            case RETURNONSALE -> MovementType.SALESRETURN;
            case RETURNONPURCHASE -> MovementType.PURCHASERETURN;
            case NONCOMMERCIALENTRY -> MovementType.NONCOMMERCIALENTRY;
            case NONCOMMERCIALEXIT -> MovementType.NONCOMMERCIALEXIT;
        };
    }
}
