package co.unicauca.edu.co.contables.accounting.facture.application.service.skeleton.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import co.unicauca.edu.co.contables.accounting.facture.application.service.skeleton.dto.SkeletonFactureDetailDto;
import co.unicauca.edu.co.contables.accounting.facture.application.service.skeleton.dto.SkeletonFactureRequestDto;
import co.unicauca.edu.co.contables.accounting.facture.application.service.skeleton.mapper.SkeletonFactureMapper;
import co.unicauca.edu.co.contables.accounting.facture.application.service.skeleton.model.PurchaseInvoiceStatus;
import co.unicauca.edu.co.contables.accounting.facture.application.service.skeleton.model.SkeletonFacture;
import co.unicauca.edu.co.contables.accounting.facture.application.service.skeleton.model.SkeletonFactureType;
import co.unicauca.edu.co.contables.accounting.facture.application.service.skeleton.model.SkeletonProduct;
import co.unicauca.edu.co.contables.accounting.facture.application.service.skeleton.repository.SkeletonFactureRepository;
import co.unicauca.edu.co.contables.accounting.facture.application.service.skeleton.repository.SkeletonReturnRepository;
import java.util.List;
import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SkeletonFactureServicePurchaseUnitTest {

    private SkeletonFactureRepository factureRepository;
    private SkeletonFactureMapper mapper;
    private PurchaseInvoiceOutboxService outbox;
    private PayableAccountDefaultResolver payableAccountDefaultResolver;
    private PurchaseProductCostSynchronizer productCostSynchronizer;
    private SkeletonFactureService service;

    @BeforeEach
    void setUp() {
        factureRepository = mock(SkeletonFactureRepository.class);
        mapper = mock(SkeletonFactureMapper.class);
        outbox = mock(PurchaseInvoiceOutboxService.class);
        payableAccountDefaultResolver = mock(PayableAccountDefaultResolver.class);
        productCostSynchronizer = mock(PurchaseProductCostSynchronizer.class);
        service = new SkeletonFactureService(
                factureRepository,
                mock(SkeletonReturnRepository.class),
                mapper,
                outbox,
                payableAccountDefaultResolver,
                productCostSynchronizer);
    }

    @Test
    void createPurchasePersistsAndEnqueuesCreatedEvent() {
        SkeletonFactureRequestDto request = request(1001L);
        SkeletonFacture invoice = invoice(1L, PurchaseInvoiceStatus.ACTIVE);
        SkeletonFactureDetailDto response = SkeletonFactureDetailDto.builder().id(1L).build();
        when(payableAccountDefaultResolver.resolveForPurchase(null, "enterprise-a")).thenReturn(2205L);
        when(mapper.toEntity(request)).thenReturn(invoice);
        when(factureRepository.save(invoice)).thenReturn(invoice);
        when(mapper.toDetailDto(invoice)).thenReturn(response);

        service.createPurchase(request);

        verify(factureRepository).save(invoice);
        verify(outbox).enqueue(invoice, "PURCHASE_INVOICE_CREATED");
    }

    @Test
    void createPurchasePersistsSelectedIssueDate() {
        SkeletonFactureRequestDto request = request(1004L);
        request.setIssueDate(LocalDate.of(2026, 7, 15));
        request.setExpirationDate(LocalDate.of(2026, 8, 14));
        SkeletonFactureMapper realMapper = new SkeletonFactureMapper();
        SkeletonFactureService realService = new SkeletonFactureService(
                factureRepository,
                mock(SkeletonReturnRepository.class),
                realMapper,
                outbox,
                payableAccountDefaultResolver,
                productCostSynchronizer);
        when(payableAccountDefaultResolver.resolveForPurchase(null, "enterprise-a")).thenReturn(2205L);
        when(factureRepository.save(org.mockito.ArgumentMatchers.any(SkeletonFacture.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        SkeletonFactureDetailDto response = realService.createPurchase(request);

        assertThat(response.getIssueDate()).isEqualTo(LocalDate.of(2026, 7, 15));
        verify(outbox).enqueue(org.mockito.ArgumentMatchers.argThat(invoice ->
                LocalDate.of(2026, 7, 15).equals(invoice.getIssueDate())),
                org.mockito.ArgumentMatchers.eq("PURCHASE_INVOICE_CREATED"));
    }

    @Test
    void updatePurchaseEnqueuesUpdatedEvent() {
        SkeletonFacture current = invoice(1L, PurchaseInvoiceStatus.ACTIVE);
        SkeletonFacture replacement = invoice(null, PurchaseInvoiceStatus.ACTIVE);
        SkeletonFactureRequestDto request = request(1002L);
        when(payableAccountDefaultResolver.resolveForPurchase(null, "enterprise-a")).thenReturn(2205L);
        when(factureRepository.findByIdWithProducts(1L)).thenReturn(Optional.of(current));
        when(mapper.toEntity(request)).thenReturn(replacement);
        when(factureRepository.save(current)).thenReturn(current);

        service.updatePurchase(1L, request);

        verify(outbox).enqueue(current, "PURCHASE_INVOICE_UPDATED");
    }

    @Test
    void voidPurchaseIsIdempotentAndEnqueuesOnlyFirstTransition() {
        SkeletonFacture invoice = invoice(1L, PurchaseInvoiceStatus.ACTIVE);
        when(factureRepository.findByIdWithProducts(1L)).thenReturn(Optional.of(invoice));

        service.voidPurchase(1L);
        service.voidPurchase(1L);

        verify(factureRepository).save(invoice);
        verify(outbox).enqueue(invoice, "PURCHASE_INVOICE_VOIDED");
    }

    @Test
    void replayEmitsCurrentStateForEveryPurchase() {
        SkeletonFacture active = invoice(1L, PurchaseInvoiceStatus.ACTIVE);
        SkeletonFacture voided = invoice(2L, PurchaseInvoiceStatus.VOIDED);
        when(factureRepository.findByEntIdAndFactureType("enterprise-a", SkeletonFactureType.PURCHASE))
                .thenReturn(List.of(active, voided));

        service.replayPurchases("enterprise-a");

        verify(outbox).enqueue(active, "PURCHASE_INVOICE_CREATED");
        verify(outbox).enqueue(voided, "PURCHASE_INVOICE_VOIDED");
    }

    @Test
    void outboxFailureIsPropagatedSoTheTransactionCanRollBack() {
        SkeletonFactureRequestDto request = request(1003L);
        SkeletonFacture invoice = invoice(3L, PurchaseInvoiceStatus.ACTIVE);
        when(payableAccountDefaultResolver.resolveForPurchase(null, "enterprise-a")).thenReturn(2205L);
        when(mapper.toEntity(request)).thenReturn(invoice);
        when(factureRepository.save(invoice)).thenReturn(invoice);
        doThrow(new IllegalStateException("outbox unavailable"))
                .when(outbox).enqueue(invoice, "PURCHASE_INVOICE_CREATED");

        assertThatThrownBy(() -> service.createPurchase(request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("outbox unavailable");

        verify(mapper, never()).toDetailDto(invoice);
    }

    @Test
    void createPurchaseSynchronizesProductCosts() {
        SkeletonFactureRequestDto request = request(1005L);
        SkeletonFacture invoice = invoice(5L, PurchaseInvoiceStatus.ACTIVE);
        when(payableAccountDefaultResolver.resolveForPurchase(null, "enterprise-a")).thenReturn(2205L);
        when(mapper.toEntity(request)).thenReturn(invoice);
        when(factureRepository.save(invoice)).thenReturn(invoice);
        when(mapper.toDetailDto(invoice)).thenReturn(SkeletonFactureDetailDto.builder().id(5L).build());

        service.createPurchase(request);

        verify(productCostSynchronizer).syncProducts(invoice);
    }

    @Test
    void updatePurchaseSynchronizesProductCosts() {
        SkeletonFacture current = invoice(6L, PurchaseInvoiceStatus.ACTIVE);
        current.setProducts(new java.util.HashSet<>(Set.of(line(30L))));
        SkeletonFacture replacement = invoice(null, PurchaseInvoiceStatus.ACTIVE);
        replacement.setProducts(new java.util.HashSet<>(Set.of(line(30L))));
        SkeletonFactureRequestDto request = request(1006L);
        when(payableAccountDefaultResolver.resolveForPurchase(null, "enterprise-a")).thenReturn(2205L);
        when(factureRepository.findByIdWithProducts(6L)).thenReturn(Optional.of(current));
        when(mapper.toEntity(request)).thenReturn(replacement);
        when(factureRepository.save(current)).thenReturn(current);

        service.updatePurchase(6L, request);

        verify(productCostSynchronizer).syncProducts(
                argThat((java.util.Collection<Long> ids) -> ids.contains(30L)));
    }

    @Test
    void voidPurchaseRecalculatesProductCostsAfterMarkingTheInvoice() {
        SkeletonFacture invoice = invoice(7L, PurchaseInvoiceStatus.ACTIVE);
        when(factureRepository.findByIdWithProducts(7L)).thenReturn(Optional.of(invoice));

        service.voidPurchase(7L);

        verify(productCostSynchronizer).syncProducts(invoice);
        assertThat(invoice.getPurchaseStatus()).isEqualTo(PurchaseInvoiceStatus.VOIDED);
    }

    @Test
    void voidPurchaseSynchronizesOnlyOnTheFirstTransition() {
        SkeletonFacture invoice = invoice(8L, PurchaseInvoiceStatus.ACTIVE);
        when(factureRepository.findByIdWithProducts(8L)).thenReturn(Optional.of(invoice));

        service.voidPurchase(8L);
        service.voidPurchase(8L);

        verify(productCostSynchronizer).syncProducts(invoice);
    }

    @Test
    void updatePurchaseResynchronizesProductsRemovedFromTheInvoice() {
        SkeletonFacture current = invoice(9L, PurchaseInvoiceStatus.ACTIVE);
        current.setProducts(new java.util.HashSet<>(Set.of(line(10L), line(20L))));
        SkeletonFacture replacement = invoice(null, PurchaseInvoiceStatus.ACTIVE);
        replacement.setProducts(new java.util.HashSet<>(Set.of(line(20L))));
        SkeletonFactureRequestDto request = request(1009L);
        when(payableAccountDefaultResolver.resolveForPurchase(null, "enterprise-a")).thenReturn(2205L);
        when(factureRepository.findByIdWithProducts(9L)).thenReturn(Optional.of(current));
        when(mapper.toEntity(request)).thenReturn(replacement);
        when(factureRepository.save(org.mockito.ArgumentMatchers.any(SkeletonFacture.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        service.updatePurchase(9L, request);

        verify(productCostSynchronizer).syncProducts(
                argThat((java.util.Collection<Long> ids) -> ids.contains(10L) && ids.contains(20L)));
    }

    private SkeletonFactureRequestDto request(long code) {
        return SkeletonFactureRequestDto.builder()
                .factCode(code)
                .entId("enterprise-a")
                .factureType(SkeletonFactureType.PURCHASE)
                .products(Set.of())
                .build();
    }

    private SkeletonProduct line(Long productId) {
        return SkeletonProduct.builder().productId(productId).unitPrice(1000.0).build();
    }

    private SkeletonFacture invoice(Long id, PurchaseInvoiceStatus status) {
        return SkeletonFacture.builder()
                .id(id)
                .factCode(1001L)
                .entId("enterprise-a")
                .thId(77L)
                .products(new java.util.HashSet<>())
                .totalValue("100")
                .totalPay("0")
                .pendingValue("100")
                .factureType(SkeletonFactureType.PURCHASE)
                .purchaseStatus(status)
                .build();
    }
}
