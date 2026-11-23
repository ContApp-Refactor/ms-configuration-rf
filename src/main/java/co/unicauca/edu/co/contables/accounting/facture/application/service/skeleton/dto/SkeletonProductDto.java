package co.unicauca.edu.co.contables.accounting.facture.application.service.skeleton.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SkeletonProductDto {
    private Long id;
    private Long productId;
    private Integer amount;
    private String description;
    private Double discount;
    private Double unitPrice;
    private Double subtotal;
    private List<Integer> taxPercentage;
}
