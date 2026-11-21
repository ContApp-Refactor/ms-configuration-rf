package co.unicauca.edu.co.contables.accounting.facture.application.service.skeleton.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import co.unicauca.edu.co.contables.accounting.facture.application.service.skeleton.model.PurchaseInvoiceStatus;
import co.unicauca.edu.co.contables.accounting.facture.application.service.skeleton.model.SkeletonFacture;
import co.unicauca.edu.co.contables.accounting.facture.application.service.skeleton.model.SkeletonFactureType;
import co.unicauca.edu.co.contables.accounting.facture.infraestructure.adapters.output.messageBroker.dto.PurchaseInvoiceEventDto;
import co.unicauca.edu.co.contables.commons.multitenancy.utils.TenantContext;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
@DisplayName("facture SpringEventsPurchaseInvoiceOutboxService Unit Tests")
class SpringEventsPurchaseInvoiceOutboxServiceTest {

    @Mock
    private PayableAccountCodeResolver payableAccountCodeResolver;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private SpringEventsPurchaseInvoiceOutboxService service;

    @BeforeEach
    void setUp() {
        service = new SpringEventsPurchaseInvoiceOutboxService(
                new com.fasterxml.jackson.databind.ObjectMapper(), payableAccountCodeResolver, eventPublisher);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    @DisplayName("publica el DTO de compra con los datos de la factura y el tenant")
    void publicaDtoConDatosDeLaFactura() {
        SkeletonFacture facture = facture(PurchaseInvoiceStatus.ACTIVE);
        when(payableAccountCodeResolver.resolveOrFail(220505L, "ENT-1")).thenReturn("22050501");
        TenantContext.setTenantId("tenant-a");

        service.enqueue(facture, "PURCHASE_INVOICE_CREATED");

        ArgumentCaptor<PurchaseInvoiceEventDto> captor =
                ArgumentCaptor.forClass(PurchaseInvoiceEventDto.class);
        verify(eventPublisher).publishEvent(captor.capture());
        PurchaseInvoiceEventDto event = captor.getValue();
        assertThat(event.getEventId()).isNotBlank();
        assertThat(event.getEventType()).isEqualTo("PURCHASE_INVOICE_CREATED");
        assertThat(event.getInvoiceId()).isEqualTo(9L);
        assertThat(event.getReference()).isEqualTo("1001");
        assertThat(event.getEnterpriseId()).isEqualTo("ENT-1");
        assertThat(event.getSupplierId()).isEqualTo(77L);
        assertThat(event.getOriginalAmount()).isEqualByComparingTo(new BigDecimal("100"));
        assertThat(event.getPaidAmount()).isEqualByComparingTo(new BigDecimal("0"));
        assertThat(event.getPendingAmount()).isEqualByComparingTo(new BigDecimal("100"));
        assertThat(event.getIssueDate()).isEqualTo(LocalDate.of(2026, 5, 10));
        assertThat(event.getDueDate()).isEqualTo(LocalDate.of(2026, 6, 10));
        assertThat(event.getPayableAccountId()).isEqualTo(220505L);
        assertThat(event.getPayableAccountCode()).isEqualTo("22050501");
        assertThat(event.isActive()).isTrue();
        assertThat(event.getTenantId()).isEqualTo("tenant-a");
    }

    @Test
    @DisplayName("una compra anulada viaja con active=false")
    void compraAnuladaViajaInactiva() {
        SkeletonFacture facture = facture(PurchaseInvoiceStatus.VOIDED);
        when(payableAccountCodeResolver.resolveOrFail(220505L, "ENT-1")).thenReturn("22050501");

        service.enqueue(facture, "PURCHASE_INVOICE_VOIDED");

        ArgumentCaptor<PurchaseInvoiceEventDto> captor =
                ArgumentCaptor.forClass(PurchaseInvoiceEventDto.class);
        verify(eventPublisher).publishEvent(captor.capture());
        assertThat(captor.getValue().isActive()).isFalse();
        assertThat(captor.getValue().getEventType()).isEqualTo("PURCHASE_INVOICE_VOIDED");
    }

    @Test
    @DisplayName("un fallo al publicar se propaga como IllegalStateException")
    void falloAlPublicarSePropaga() {
        SkeletonFacture facture = facture(PurchaseInvoiceStatus.ACTIVE);
        when(payableAccountCodeResolver.resolveOrFail(220505L, "ENT-1")).thenReturn("22050501");
        doThrow(new IllegalStateException("bus caido"))
                .when(eventPublisher).publishEvent(any(PurchaseInvoiceEventDto.class));

        assertThatThrownBy(() -> service.enqueue(facture, "PURCHASE_INVOICE_CREATED"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No fue posible publicar el evento de compra")
                .hasCauseInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("genera un eventId distinto por cada evento")
    void generaEventIdUnicoPorEvento() {
        SkeletonFacture facture = facture(PurchaseInvoiceStatus.ACTIVE);
        when(payableAccountCodeResolver.resolveOrFail(220505L, "ENT-1")).thenReturn("22050501");

        service.enqueue(facture, "PURCHASE_INVOICE_CREATED");
        service.enqueue(facture, "PURCHASE_INVOICE_CREATED");

        ArgumentCaptor<PurchaseInvoiceEventDto> captor =
                ArgumentCaptor.forClass(PurchaseInvoiceEventDto.class);
        verify(eventPublisher, times(2)).publishEvent(captor.capture());
        assertThat(captor.getAllValues().get(0).getEventId())
                .isNotEqualTo(captor.getAllValues().get(1).getEventId());
    }

    private SkeletonFacture facture(PurchaseInvoiceStatus status) {
        return SkeletonFacture.builder()
                .id(9L)
                .factCode(1001L)
                .entId("ENT-1")
                .thId(77L)
                .products(new HashSet<>())
                .totalValue("100")
                .totalPay("0")
                .pendingValue("100")
                .accountingAccount(220505L)
                .issueDate(LocalDate.of(2026, 5, 10))
                .expirationDate(LocalDate.of(2026, 6, 10))
                .factureType(SkeletonFactureType.PURCHASE)
                .purchaseStatus(status)
                .build();
    }
}
