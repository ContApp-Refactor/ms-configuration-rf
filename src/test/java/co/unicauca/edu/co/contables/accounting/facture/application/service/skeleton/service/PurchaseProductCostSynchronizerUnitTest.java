package co.unicauca.edu.co.contables.accounting.facture.application.service.skeleton.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.Pageable;

import co.unicauca.edu.co.contables.accounting.facture.application.service.skeleton.model.PurchaseInvoiceStatus;
import co.unicauca.edu.co.contables.accounting.facture.application.service.skeleton.model.SkeletonFacture;
import co.unicauca.edu.co.contables.accounting.facture.application.service.skeleton.model.SkeletonFactureType;
import co.unicauca.edu.co.contables.accounting.facture.application.service.skeleton.model.SkeletonProduct;
import co.unicauca.edu.co.contables.accounting.facture.application.service.skeleton.repository.SkeletonFactureRepository;
import co.unicauca.edu.co.contables.inventory.products.application.ports.input.IProductCostPort;

class PurchaseProductCostSynchronizerUnitTest {

    private SkeletonFactureRepository factureRepository;
    private IProductCostPort productCostPort;
    private PurchaseProductCostSynchronizer synchronizer;

    @BeforeEach
    void setUp() {
        factureRepository = org.mockito.Mockito.mock(SkeletonFactureRepository.class);
        productCostPort = org.mockito.Mockito.mock(IProductCostPort.class);
        synchronizer = new PurchaseProductCostSynchronizer(factureRepository, productCostPort);
    }

    @Test
    void syncProductWritesTheLatestNonVoidedPurchasePrice() {
        when(factureRepository.findLastPurchaseUnitPrice(
                eq(10L),
                eq(SkeletonFactureType.PURCHASE),
                eq(PurchaseInvoiceStatus.VOIDED),
                any(Pageable.class)))
                .thenReturn(List.of(1500.0));

        synchronizer.syncProduct(10L);

        verify(productCostPort).updateLastPurchaseCost(10L, 1500.0);
    }

    @Test
    void syncProductKeepsTheCurrentCostWhenNoPurchaseRemains() {
        when(factureRepository.findLastPurchaseUnitPrice(
                eq(11L),
                eq(SkeletonFactureType.PURCHASE),
                eq(PurchaseInvoiceStatus.VOIDED),
                any(Pageable.class)))
                .thenReturn(List.of());

        synchronizer.syncProduct(11L);

        verify(productCostPort, never()).updateLastPurchaseCost(eq(11L), org.mockito.ArgumentMatchers.anyDouble());
    }

    @Test
    void syncProductAsksForASingleRecordExcludingVoidedPurchases() {
        when(factureRepository.findLastPurchaseUnitPrice(
                eq(12L),
                eq(SkeletonFactureType.PURCHASE),
                eq(PurchaseInvoiceStatus.VOIDED),
                any(Pageable.class)))
                .thenReturn(List.of(900.0));

        synchronizer.syncProduct(12L);

        ArgumentCaptor<Pageable> pageable = ArgumentCaptor.forClass(Pageable.class);
        verify(factureRepository).findLastPurchaseUnitPrice(
                eq(12L),
                eq(SkeletonFactureType.PURCHASE),
                eq(PurchaseInvoiceStatus.VOIDED),
                pageable.capture());
        assertThat(pageable.getValue().getPageSize()).isEqualTo(1);
    }

    @Test
    void syncProductsFromInvoiceTouchesEachDistinctProductOnce() {
        SkeletonFacture invoice = SkeletonFacture.builder()
                .id(1L)
                .factCode(9001L)
                .entId("enterprise-a")
                .factureType(SkeletonFactureType.PURCHASE)
                .purchaseStatus(PurchaseInvoiceStatus.ACTIVE)
                .products(new HashSet<>(Set.of(
                        line(10L), line(10L), line(20L))))
                .build();
        when(factureRepository.findLastPurchaseUnitPrice(
                eq(10L), any(), any(), any(Pageable.class))).thenReturn(List.of(100.0));
        when(factureRepository.findLastPurchaseUnitPrice(
                eq(20L), any(), any(), any(Pageable.class))).thenReturn(List.of(200.0));

        synchronizer.syncProducts(invoice);

        verify(productCostPort).updateLastPurchaseCost(10L, 100.0);
        verify(productCostPort).updateLastPurchaseCost(20L, 200.0);
    }

    @Test
    void syncProductsIgnoresNullInvoiceAndNullProducts() {
        synchronizer.syncProducts((SkeletonFacture) null);
        synchronizer.syncProducts((Set<Long>) null);

        verify(productCostPort, never()).updateLastPurchaseCost(
                org.mockito.ArgumentMatchers.anyLong(),
                org.mockito.ArgumentMatchers.anyDouble());
    }

    private SkeletonProduct line(Long productId) {
        return SkeletonProduct.builder().productId(productId).build();
    }
}
