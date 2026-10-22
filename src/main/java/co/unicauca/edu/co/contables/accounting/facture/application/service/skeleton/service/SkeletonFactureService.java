package co.unicauca.edu.co.contables.accounting.facture.application.service.skeleton.service;

import co.unicauca.edu.co.contables.accounting.facture.application.service.skeleton.audit.annotation.FactureDocumentAuditable;
import co.unicauca.edu.co.contables.commons.audit.annotation.DocumentOperationType;
import co.unicauca.edu.co.contables.accounting.facture.application.service.skeleton.dto.*;
import co.unicauca.edu.co.contables.accounting.facture.application.service.skeleton.mapper.SkeletonFactureMapper;
import co.unicauca.edu.co.contables.accounting.facture.application.service.skeleton.model.SkeletonFacture;
import co.unicauca.edu.co.contables.accounting.facture.application.service.skeleton.model.SkeletonProduct;
import co.unicauca.edu.co.contables.accounting.facture.application.service.skeleton.model.SkeletonReturn;
import co.unicauca.edu.co.contables.accounting.facture.application.service.skeleton.model.SkeletonFactureType;
import co.unicauca.edu.co.contables.accounting.facture.application.service.skeleton.model.PurchaseInvoiceStatus;
import co.unicauca.edu.co.contables.accounting.facture.application.service.skeleton.repository.SkeletonFactureRepository;
import co.unicauca.edu.co.contables.accounting.facture.application.service.skeleton.repository.SkeletonReturnRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SkeletonFactureService {

    private static final int DEFAULT_PAYMENT_TERM_DAYS = 30;

    private final SkeletonFactureRepository factureRepository;
    private final SkeletonReturnRepository returnRepository;
    private final SkeletonFactureMapper mapper;
    private final PurchaseInvoiceOutboxService purchaseOutbox;
    private final PayableAccountDefaultResolver payableAccountDefaultResolver;

    @Transactional
    @FactureDocumentAuditable(operationType = DocumentOperationType.CREATE, moduleName = "INVOICES")
    public SkeletonFactureDetailDto createPurchase(SkeletonFactureRequestDto request) {
        if (request.getFactureType() != SkeletonFactureType.PURCHASE) throw new IllegalArgumentException("El flujo purchase solo admite facturas PURCHASE");
        preparePurchaseRequest(request);
        if (factureRepository.existsByFactCode(request.getFactCode())) throw new IllegalArgumentException("Ya existe una factura con el codigo: " + request.getFactCode());
        SkeletonFacture saved = factureRepository.save(mapper.toEntity(request));
        purchaseOutbox.enqueue(saved, "PURCHASE_INVOICE_CREATED");
        return mapper.toDetailDto(saved);
    }

    @Transactional
    public SkeletonFactureDetailDto updatePurchase(Long id, SkeletonFactureRequestDto request) {
        SkeletonFacture current = factureRepository.findByIdWithProducts(id).orElseThrow(() -> new IllegalArgumentException("Factura de compra no encontrada"));
        if (current.getFactureType() != SkeletonFactureType.PURCHASE || current.getPurchaseStatus() == PurchaseInvoiceStatus.VOIDED) throw new IllegalArgumentException("La factura de compra no se puede actualizar");
        if (request.getIssueDate() == null) {
            request.setIssueDate(current.getIssueDate() != null
                    ? current.getIssueDate()
                    : current.getCreatedAt() != null ? current.getCreatedAt().toLocalDate() : LocalDate.now());
        }
        preparePurchaseRequest(request);
        SkeletonFacture replacement = mapper.toEntity(request);
        current.setFactCode(replacement.getFactCode());current.setEntId(replacement.getEntId());current.setThId(replacement.getThId());current.setTotalValue(replacement.getTotalValue());current.setTotalPay(replacement.getTotalPay());current.setPendingValue(replacement.getPendingValue());current.setIssueDate(replacement.getIssueDate());current.setExpirationDate(replacement.getExpirationDate());current.setAccountingAccount(replacement.getAccountingAccount());current.getProducts().clear();replacement.getProducts().forEach(current::addProduct);
        SkeletonFacture saved = factureRepository.save(current);
        purchaseOutbox.enqueue(saved, "PURCHASE_INVOICE_UPDATED");
        return mapper.toDetailDto(saved);
    }

    @Transactional
    public SkeletonFactureDetailDto voidPurchase(Long id) {
        SkeletonFacture current = factureRepository.findByIdWithProducts(id).orElseThrow(() -> new IllegalArgumentException("Factura de compra no encontrada"));
        if (current.getFactureType() != SkeletonFactureType.PURCHASE) throw new IllegalArgumentException("El documento no es una compra");
        if (current.getPurchaseStatus() != PurchaseInvoiceStatus.VOIDED) {current.setPurchaseStatus(PurchaseInvoiceStatus.VOIDED);current.setVoidedAt(java.time.LocalDateTime.now());factureRepository.save(current);purchaseOutbox.enqueue(current,"PURCHASE_INVOICE_VOIDED");}
        return mapper.toDetailDto(current);
    }

    @Transactional
    public int replayPurchases(String enterpriseId) {
        List<SkeletonFacture> purchases=factureRepository.findByEntIdAndFactureType(enterpriseId,SkeletonFactureType.PURCHASE);
        purchases.forEach(f->purchaseOutbox.enqueue(f,f.getPurchaseStatus()==PurchaseInvoiceStatus.VOIDED?"PURCHASE_INVOICE_VOIDED":"PURCHASE_INVOICE_CREATED"));
        return purchases.size();
    }

    @Transactional
    @FactureDocumentAuditable(operationType = DocumentOperationType.CREATE, moduleName = "INVOICES")
    public SkeletonFactureDetailDto createFacture(SkeletonFactureRequestDto request) {
        log.info("Creando factura con código: {}", request.getFactCode());

        if (factureRepository.existsByFactCode(request.getFactCode())) {
            throw new IllegalArgumentException("Ya existe una factura con el código: " + request.getFactCode());
        }

        SkeletonFacture facture = mapper.toEntity(request);
        SkeletonFacture savedFacture = factureRepository.save(facture);

        log.info("Factura creada exitosamente con ID: {}", savedFacture.getId());
        return mapper.toDetailDto(savedFacture);
    }

    @Transactional(readOnly = true)
    public List<SkeletonFactureSummaryDto> getAllFactures() {
        return factureRepository.findAll().stream()
                .map(mapper::toSummaryDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public SkeletonFactureDetailDto getFactureById(Long id) {
        SkeletonFacture facture = factureRepository.findByIdWithProducts(id)
                .orElseThrow(() -> new IllegalArgumentException("Factura no encontrada con ID: " + id));
        return mapper.toDetailDto(facture);
    }

    @Transactional(readOnly = true)
    public SkeletonFactureDetailDto getFactureByFactCode(Long factCode) {
        SkeletonFacture facture = factureRepository.findByFactCodeWithProducts(factCode)
                .orElseThrow(() -> new IllegalArgumentException("Factura no encontrada con código: " + factCode));
        return mapper.toDetailDto(facture);
    }

    @Transactional
    @FactureDocumentAuditable(operationType = DocumentOperationType.CREATE, moduleName = "INVOICES")
    public void processReturn(ReturnRequestDto returnRequest, SkeletonReturn.ReturnType returnType) {
        log.info("Procesando devolución para factura: {}", returnRequest.getFactCode());

        // Validar que la factura existe
        SkeletonFacture facture = factureRepository.findByFactCodeWithProducts(returnRequest.getFactCode())
                .orElseThrow(() -> new IllegalArgumentException(
                        "No se encontró la factura con código: " + returnRequest.getFactCode()));

        // Procesar cada producto a devolver
        for (ReturnRequestDto.ProductReturnDto productReturn : returnRequest.getProducts()) {
            validateAndProcessProductReturn(facture, productReturn, returnType);
        }

        log.info("Devolución procesada exitosamente");
    }

    private void validateAndProcessProductReturn(
            SkeletonFacture facture,
            ReturnRequestDto.ProductReturnDto productReturn,
            SkeletonReturn.ReturnType returnType) {

        // Buscar el producto en la factura
        SkeletonProduct product = facture.getProducts().stream()
                .filter(p -> p.getProductId().equals(productReturn.getProductId()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "El producto " + productReturn.getProductId() + " no existe en la factura "
                                + facture.getFactCode()));

        // Obtener la cantidad total devuelta previamente
        Integer totalReturned = returnRepository.getTotalReturnedQuantity(
                facture.getFactCode(),
                productReturn.getProductId());

        if (totalReturned == null) {
            totalReturned = 0;
        }

        // Validar que no se exceda la cantidad original
        int newTotal = totalReturned + productReturn.getQuantity();
        if (newTotal > product.getAmount()) {
            throw new IllegalArgumentException(
                    String.format("No se puede devolver %d unidades del producto %d. " +
                            "Cantidad original: %d, Ya devueltos: %d, Disponible para devolver: %d",
                            productReturn.getQuantity(),
                            productReturn.getProductId(),
                            product.getAmount(),
                            totalReturned,
                            product.getAmount() - totalReturned));
        }

        // Guardar el registro de devolución
        SkeletonReturn returnRecord = SkeletonReturn.builder()
                .originalFactCode(facture.getFactCode())
                .productId(productReturn.getProductId())
                .returnedQuantity(productReturn.getQuantity())
                .returnType(returnType)
                .details(productReturn.getReason())
                .build();

        returnRepository.save(returnRecord);

        log.info("Registro de devolución guardado - Factura: {}, Producto: {}, Cantidad: {}",
                facture.getFactCode(), productReturn.getProductId(), productReturn.getQuantity());
    }

    @Transactional(readOnly = true)
    public List<SkeletonReturn> getReturnsByFactCode(Long factCode) {
        return returnRepository.findByOriginalFactCode(factCode);
    }

    @Transactional(readOnly = true)
    public Integer getTotalReturnedQuantity(Long factCode, Long productId) {
        Integer total = returnRepository.getTotalReturnedQuantity(factCode, productId);
        return total != null ? total : 0;
    }

    private void preparePurchaseRequest(SkeletonFactureRequestDto request) {
        if (request.getFactCode() == null || request.getFactCode() <= 0) {
            request.setFactCode(System.currentTimeMillis() % 1_000_000_000L);
        }
        if (request.getIssueDate() == null) {
            request.setIssueDate(LocalDate.now());
        }
        if (request.getExpirationDate() == null) {
            request.setExpirationDate(request.getIssueDate().plusDays(DEFAULT_PAYMENT_TERM_DAYS));
        }
        if (!request.getExpirationDate().isAfter(request.getIssueDate())) {
            throw new IllegalArgumentException("La fecha de vencimiento debe ser posterior a la fecha de emisión");
        }
        request.setAccountingAccount(
                payableAccountDefaultResolver.resolveForPurchase(request.getAccountingAccount(), request.getEntId()));
    }
}
