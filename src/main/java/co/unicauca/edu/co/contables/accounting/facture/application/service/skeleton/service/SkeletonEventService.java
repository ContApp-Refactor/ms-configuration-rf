package co.unicauca.edu.co.contables.accounting.facture.application.service.skeleton.service;

import co.unicauca.edu.co.contables.accounting.facture.application.ports.input.IReceiptEventPort;
import co.unicauca.edu.co.contables.accounting.facture.application.ports.input.IWeightedAverageEventPort;
import co.unicauca.edu.co.contables.accounting.facture.application.ports.input.IpepsEventPort;
import co.unicauca.edu.co.contables.accounting.facture.application.service.skeleton.dto.ReturnRequestDto;
import co.unicauca.edu.co.contables.accounting.facture.application.service.skeleton.model.InventoryConfigurationType;
import co.unicauca.edu.co.contables.accounting.facture.application.service.skeleton.model.SkeletonFacture;
import co.unicauca.edu.co.contables.accounting.facture.application.service.skeleton.model.SkeletonProduct;
import co.unicauca.edu.co.contables.accounting.facture.infraestructure.adapters.output.messageBroker.dto.KardexPurchaseDtoRequest;
import co.unicauca.edu.co.contables.accounting.facture.infraestructure.adapters.output.messageBroker.dto.KardexSalesDtoRequest;
import co.unicauca.edu.co.contables.accounting.facture.infraestructure.adapters.output.messageBroker.dto.ReceiptSalesDtoRequest;
import co.unicauca.edu.co.contables.accounting.facture.infraestructure.adapters.output.messageBroker.dto.PurchaseInvoiceEventDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import co.unicauca.edu.co.contables.commons.security.IJwtUtils;

@Service
@RequiredArgsConstructor
@Slf4j
public class SkeletonEventService {
    
    private final IWeightedAverageEventPort weightedAverageEventPort;
    private final IReceiptEventPort receiptEventPort;
    private final IpepsEventPort pepsEventPort;
    private final IJwtUtils jwtUtils;
    private final PayableAccountCodeResolver payableAccountCodeResolver;
    
    /**
     * Publica eventos de compra uno por uno de forma asíncrona según la configuración
     */
    public void publishPurchaseEvents(SkeletonFacture facture, InventoryConfigurationType configType) {
        log.info("Publicando eventos de compra para factura: {} con configuración: {}", 
                facture.getFactCode(), configType);
        
        for (SkeletonProduct product : facture.getProducts()) {
            publishSinglePurchaseEvent(facture.getFactCode(), product, configType);
        }
    }

    public void publishPurchaseInvoiceEvent(SkeletonFacture facture) {
        BigDecimal original = new BigDecimal(facture.getTotalValue());
        BigDecimal paid = new BigDecimal(facture.getTotalPay());
        BigDecimal pending = new BigDecimal(facture.getPendingValue());
        String payableCode = payableAccountCodeResolver.resolveOrFail(
                facture.getAccountingAccount(), facture.getEntId());
        PurchaseInvoiceEventDto event = PurchaseInvoiceEventDto.builder()
                .eventId(UUID.randomUUID().toString()).eventType("PURCHASE_INVOICE_CREATED")
                .invoiceId(facture.getFactCode()).reference(String.valueOf(facture.getFactCode()))
                .enterpriseId(facture.getEntId()).supplierId(facture.getThId())
                .originalAmount(original).paidAmount(paid).pendingAmount(pending)
                .issueDate(facture.getIssueDate() != null
                        ? facture.getIssueDate()
                        : facture.getCreatedAt() != null ? facture.getCreatedAt().toLocalDate() : LocalDate.now())
                .dueDate(facture.getExpirationDate()).payableAccountId(facture.getAccountingAccount())
                .payableAccountCode(payableCode).active(true)
                .tenantId(jwtUtils.getId()).build();
        receiptEventPort.publishPurchaseInvoiceEvent(event);
    }
    
    private void publishSinglePurchaseEvent(Long factCode, SkeletonProduct product, InventoryConfigurationType configType) {
        KardexPurchaseDtoRequest kardexDto = buildPurchaseKardexDto(factCode, product);
        
        if (configType == InventoryConfigurationType.PEPS) {
            try {
                kardexDto.setDetails("Compra-Factura:" + factCode);
                pepsEventPort.publishPurchasePEPSEvent(kardexDto);
                log.info("Evento PEPS de compra publicado - Producto: {}", product.getProductId());
            } catch (Exception e) {
                log.error("Error publicando evento PEPS para producto {}: {}", 
                        product.getProductId(), e.getMessage());
            }
        } else {
            try {
                weightedAverageEventPort.publishPurchaseWeightedAverageEvent(kardexDto);
                log.info("Evento WeightedAverage de compra publicado - Producto: {}", product.getProductId());
            } catch (Exception e) {
                log.error("Error publicando evento WeightedAverage para producto {}: {}", 
                        product.getProductId(), e.getMessage());
            }
        }
    }
    
    /**
     * Publica eventos de venta uno por uno de forma asíncrona según la configuración
     */
    public void publishSaleEvents(SkeletonFacture facture, InventoryConfigurationType configType) {
        log.info("Publicando eventos de venta para factura: {} con configuración: {}", 
                facture.getFactCode(), configType);
        
        for (SkeletonProduct product : facture.getProducts()) {
            publishSingleSaleEvent(facture.getFactCode(), product, configType);
        }
    }
    
    private void publishSingleSaleEvent(Long factCode, SkeletonProduct product, InventoryConfigurationType configType) {
        KardexSalesDtoRequest kardexDto = buildSalesKardexDto(factCode, product);
        
        if (configType == InventoryConfigurationType.PEPS) {
            try {
                kardexDto.setDetails("Venta-Factura:" + factCode);
                pepsEventPort.publishSalePEPSEvent(kardexDto);
                log.info("Evento PEPS de venta publicado - Producto: {}", product.getProductId());
            } catch (Exception e) {
                log.error("Error publicando evento PEPS para producto {}: {}", 
                        product.getProductId(), e.getMessage());
            }
        } else {
            try {
                weightedAverageEventPort.publishSaleWeightedAverageEvent(kardexDto);
                log.info("Evento WeightedAverage de venta publicado - Producto: {}", product.getProductId());
            } catch (Exception e) {
                log.error("Error publicando evento WeightedAverage para producto {}: {}", 
                        product.getProductId(), e.getMessage());
            }
        }
    }
    
    /**
     * Publica eventos de devolución en venta uno por uno según la configuración
     */
    public void publishReturnOnSaleEvents(Long factCode, ReturnRequestDto.ProductReturnDto productReturn, 
                                          InventoryConfigurationType configType) {
        log.info("Publicando eventos de devolución en venta - Factura: {}, Producto: {} con configuración: {}", 
                factCode, productReturn.getProductId(), configType);
        
        KardexSalesDtoRequest kardexDto = buildReturnKardexDto(factCode, productReturn);
        
        if (configType == InventoryConfigurationType.PEPS) {
            try {
                kardexDto.setDetails("Devolución de venta-Factura:" + factCode);
                pepsEventPort.publishReturnOnSalePEPSEvent(kardexDto);
                log.info("Evento PEPS de devolución en venta publicado");
            } catch (Exception e) {
                log.error("Error publicando evento PEPS de devolución: {}", e.getMessage());
            }
        } else {
            try {
                weightedAverageEventPort.publishReturnOnSaleWeightedAverageEvent(kardexDto);
                log.info("Evento WeightedAverage de devolución en venta publicado");
            } catch (Exception e) {
                log.error("Error publicando evento WeightedAverage de devolución: {}", e.getMessage());
            }
        }
    }
    
    /**
     * Publica eventos de devolución en compra uno por uno según la configuración
     */
    public void publishReturnOnPurchaseEvents(Long factCode, ReturnRequestDto.ProductReturnDto productReturn,
                                              InventoryConfigurationType configType) {
        log.info("Publicando eventos de devolución en compra - Factura: {}, Producto: {} con configuración: {}", 
                factCode, productReturn.getProductId(), configType);
        
        KardexSalesDtoRequest kardexDto = buildReturnKardexDto(factCode, productReturn);
        
        if (configType == InventoryConfigurationType.PEPS) {
            try {
                kardexDto.setDetails("Devolución de compra-Factura:" + factCode);
                pepsEventPort.publishReturnOnPurchasePEPSEvent(kardexDto);
                log.info("Evento PEPS de devolución en compra publicado");
            } catch (Exception e) {
                log.error("Error publicando evento PEPS de devolución: {}", e.getMessage());
            }
        } else {
            try {
                weightedAverageEventPort.publishReturnOnPurchaseWeightedAverageEvent(kardexDto);
                log.info("Evento WeightedAverage de devolución en compra publicado");
            } catch (Exception e) {
                log.error("Error publicando evento WeightedAverage de devolución: {}", e.getMessage());
            }
        }
    }
    
    /**
     * Publica evento de recibo de venta
     */
    public void publishSaleReceiptEvent(SkeletonFacture facture) {
        log.info("Publicando evento de recibo de venta para factura: {}", facture.getFactCode());
        
        try {
            ReceiptSalesDtoRequest receiptDto = ReceiptSalesDtoRequest.builder()
                    .factCode(facture.getFactCode())
                    .entId(facture.getEntId())
                    .thirdId(facture.getThId())
                    .totalPay(Long.valueOf(facture.getTotalPay()))
                    .totalValue(Long.valueOf(facture.getTotalValue()))
                    .pendingValue(Long.valueOf(facture.getPendingValue()))
                    .creationDate(LocalDate.now())
                    .expirationDate(facture.getExpirationDate())
                    .active(true)
                    .accountingAccount(facture.getAccountingAccount())
                    .build();

            receiptEventPort.publishSaleReceiptEvent(receiptDto);
            log.info("Evento de recibo de venta publicado exitosamente");
        } catch (Exception e) {
            log.error("Error publicando evento de recibo de venta: {}", e.getMessage());
        }
    }
    
    /**
     * Publica eventos de entrada no comercial según la configuración
     */
    public void publishNonCommercialEntryEvents(SkeletonFacture facture, InventoryConfigurationType configType,String tagTitle) {
        log.info("Publicando eventos de entrada no comercial para factura: {} con configuración: {}", 
                facture.getFactCode(), configType);
        
        for (SkeletonProduct product : facture.getProducts()) {
            publishSingleNonCommercialEntryEvent(facture.getFactCode(), product, configType, tagTitle);
        }
    }
    
    private void publishSingleNonCommercialEntryEvent(Long factCode, SkeletonProduct product, 
                                                      InventoryConfigurationType configType,String tagTitle) {
        KardexPurchaseDtoRequest kardexDto = buildPurchaseKardexDto(factCode, product);
        
        if (configType == InventoryConfigurationType.PEPS) {
            try {
                kardexDto.setDetails("Entrada no comercial-Factura:" + factCode+" "+tagTitle);
                pepsEventPort.publishNonCommercialEntryPEPSEvent(kardexDto);
                log.info("Evento PEPS de entrada no comercial publicado - Producto: {}", product.getProductId());
            } catch (Exception e) {
                log.error("Error publicando evento PEPS: {}", e.getMessage());
            }
        } else {
            try {
                weightedAverageEventPort.publishNonCommercialEntryWeightedAverageEvent(kardexDto);
                log.info("Evento WeightedAverage de entrada no comercial publicado - Producto: {}", product.getProductId());
            } catch (Exception e) {
                log.error("Error publicando evento WeightedAverage: {}", e.getMessage());
            }
        }
    }
    
    /**
     * Publica eventos de salida no comercial según la configuración
     */
    public void publishNonCommercialExitEvents(SkeletonFacture facture, InventoryConfigurationType configType,String tagTitle) {
        log.info("Publicando eventos de salida no comercial para factura: {} con configuración: {}", 
                facture.getFactCode(), configType);
        
        for (SkeletonProduct product : facture.getProducts()) {
            publishSingleNonCommercialExitEvent(facture.getFactCode(), product, configType,tagTitle);
        }
    }
    
    private void publishSingleNonCommercialExitEvent(Long factCode, SkeletonProduct product,
                                                     InventoryConfigurationType configType,String tagTitle) {
        KardexSalesDtoRequest kardexDto = buildSalesKardexDto(factCode, product);
        
        if (configType == InventoryConfigurationType.PEPS) {
            try {
                kardexDto.setDetails("Salida no comercial-Factura:" + factCode+" "+tagTitle);
                pepsEventPort.publishNonCommercialExitPEPSEvent(kardexDto);
                log.info("Evento PEPS de salida no comercial publicado - Producto: {}", product.getProductId());
            } catch (Exception e) {
                log.error("Error publicando evento PEPS: {}", e.getMessage());
            }
        } else {
            try {
                weightedAverageEventPort.publishNonCommercialExitWeightedAverageEvent(kardexDto);
                log.info("Evento WeightedAverage de salida no comercial publicado - Producto: {}", product.getProductId());
            } catch (Exception e) {
                log.error("Error publicando evento WeightedAverage: {}", e.getMessage());
            }
        }
    }
    
    // Métodos helper para construir DTOs
    private KardexPurchaseDtoRequest buildPurchaseKardexDto(Long factCode, SkeletonProduct product) {
        KardexPurchaseDtoRequest dto = new KardexPurchaseDtoRequest();
        dto.setQuantity(product.getAmount());
        dto.setFactCode(factCode);
        dto.setUnitPrice(BigDecimal.valueOf(product.getBasePrice()));
        dto.setProductId(product.getProductId());
        return dto;
    }
    
    private KardexSalesDtoRequest buildSalesKardexDto(Long factCode, SkeletonProduct product) {
        KardexSalesDtoRequest dto = new KardexSalesDtoRequest();
        dto.setQuantity(product.getAmount());
        dto.setFactCode(factCode);
        dto.setProductId(product.getProductId());
        return dto;
    }
    
    private KardexSalesDtoRequest buildReturnKardexDto(Long factCode, ReturnRequestDto.ProductReturnDto productReturn) {
        KardexSalesDtoRequest dto = new KardexSalesDtoRequest();
        dto.setQuantity(productReturn.getQuantity());
        dto.setFactCode(factCode);
        dto.setProductId(productReturn.getProductId());
        return dto;
    }
}
