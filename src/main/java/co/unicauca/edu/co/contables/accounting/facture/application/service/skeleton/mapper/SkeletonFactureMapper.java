package co.unicauca.edu.co.contables.accounting.facture.application.service.skeleton.mapper;

import co.unicauca.edu.co.contables.accounting.facture.application.service.skeleton.dto.*;
import co.unicauca.edu.co.contables.accounting.facture.application.service.skeleton.model.SkeletonFacture;
import co.unicauca.edu.co.contables.accounting.facture.application.service.skeleton.model.SkeletonProduct;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

@Component
public class SkeletonFactureMapper {
    
    public SkeletonFacture toEntity(SkeletonFactureRequestDto dto) {
        SkeletonFacture facture = SkeletonFacture.builder()
                .factCode(dto.getFactCode())
                .entId(dto.getEntId())
                .thId(dto.getThId())
                .totalValue(dto.getTotalValue())
                .totalPay(dto.getTotalPay())
                .pendingValue(dto.getPendingValue())
                .issueDate(dto.getIssueDate())
                .expirationDate(dto.getExpirationDate())
                .accountingAccount(dto.getAccountingAccount())
                .factureType(dto.getFactureType())
                .build();
        
        Set<SkeletonProduct> products = dto.getProducts().stream()
                .map(productDto -> toProductEntity(productDto, facture))
                .collect(Collectors.toSet());
        
        products.forEach(facture::addProduct);
        
        return facture;
    }
    
    private SkeletonProduct toProductEntity(SkeletonProductRequestDto dto, SkeletonFacture facture) {
        return SkeletonProduct.builder()
                .productId(dto.getProductId())
                .amount(dto.getAmount())
                .description(dto.getDescription())
                .discount(dto.getDiscount() != null ? dto.getDiscount() : 0.0)
                .unitPrice(dto.getUnitPrice())
                .subtotal(dto.getSubtotal())
                .taxPercentage(dto.getTaxPercentage())
                .facture(facture)
                .build();
    }
    
    public SkeletonFactureSummaryDto toSummaryDto(SkeletonFacture entity) {
        return SkeletonFactureSummaryDto.builder()
                .id(entity.getId())
                .factCode(entity.getFactCode())
                .entId(entity.getEntId())
                .thId(entity.getThId())
                .totalValue(entity.getTotalValue())
                .factureType(entity.getFactureType())
                .issueDate(entity.getIssueDate())
                .createdAt(entity.getCreatedAt())
                .purchaseStatus(entity.getPurchaseStatus())
                .version(entity.getVersion())
                .build();
    }
    
    public SkeletonFactureDetailDto toDetailDto(SkeletonFacture entity) {
        Set<SkeletonProductDto> productDtos = entity.getProducts().stream()
                .map(this::toProductDto)
                .collect(Collectors.toSet());
        
        return SkeletonFactureDetailDto.builder()
                .id(entity.getId())
                .factCode(entity.getFactCode())
                .entId(entity.getEntId())
                .thId(entity.getThId())
                .products(productDtos)
                .totalValue(entity.getTotalValue())
                .totalPay(entity.getTotalPay())
                .pendingValue(entity.getPendingValue())
                .issueDate(entity.getIssueDate())
                .expirationDate(entity.getExpirationDate())
                .accountingAccount(entity.getAccountingAccount())
                .factureType(entity.getFactureType())
                .createdAt(entity.getCreatedAt())
                .purchaseStatus(entity.getPurchaseStatus())
                .version(entity.getVersion())
                .build();
    }
    
    private SkeletonProductDto toProductDto(SkeletonProduct entity) {
        return SkeletonProductDto.builder()
                .id(entity.getId())
                .productId(entity.getProductId())
                .amount(entity.getAmount())
                .description(entity.getDescription())
                .discount(entity.getDiscount())
                .unitPrice(entity.getUnitPrice())
                .subtotal(entity.getSubtotal())
                .taxPercentage(entity.getTaxPercentage())
                .build();
    }
}
