package co.unicauca.edu.co.contables.accounting.facture.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Product {
    private long productId;
    private double amount;
    private String description;
    private double descount;
    private String code;
    private double vat;
    private double unitPrice; 
    private double subtotal;
}
