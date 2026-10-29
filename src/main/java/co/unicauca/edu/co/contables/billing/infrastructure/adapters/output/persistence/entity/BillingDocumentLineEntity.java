package co.unicauca.edu.co.contables.billing.infrastructure.adapters.output.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.math.BigDecimal;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.TenantId;

/**
 * Linea de un documento. Guarda la referencia de catalogo, los overrides opcionales y los valores
 * efectivos/derivados (precio, IVA, subtotal, impuesto y total). La unicidad
 * {@code (document_id, line_no)} obliga a fusionar por numero de linea (D9) en lugar de reemplazar.
 */
@Entity(name = "BillingDocumentLineEntity")
@Table(name = "billing_document_lines",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_billing_line_no", columnNames = {"document_id", "line_no"})
        },
        indexes = {
                @Index(name = "ix_billing_line_product", columnList = "tenant_id, product_id")
        })
@Getter
@Setter
@NoArgsConstructor
public class BillingDocumentLineEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @TenantId
    @Column(name = "tenant_id", nullable = false, length = 80)
    @Setter(AccessLevel.NONE)
    private String tenantId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "document_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_billing_line_document"))
    private BillingDocumentEntity document;

    @Column(name = "line_no", nullable = false)
    private int lineNo;

    @Column(name = "source_line_no")
    private Integer sourceLineNo;

    @Column(name = "product_id", nullable = false)
    private long productId;

    @Column(name = "product_code", length = 100)
    private String productCode;

    @Column(name = "product_name", nullable = false, length = 255)
    private String productName;

    @Column(name = "unit_of_measure_id")
    private Long unitOfMeasureId;

    @Column(name = "unit_of_measure_name", length = 100)
    private String unitOfMeasureName;

    @Column(name = "quantity", nullable = false, precision = 19, scale = 3)
    private BigDecimal quantity;

    @Column(name = "catalog_unit_price", nullable = false, precision = 19, scale = 2)
    private BigDecimal catalogUnitPrice;

    @Column(name = "catalog_tax_rate", nullable = false, precision = 5, scale = 2)
    private BigDecimal catalogTaxRate;

    @Column(name = "unit_price_override", precision = 19, scale = 2)
    private BigDecimal unitPriceOverride;

    @Column(name = "tax_rate_override", precision = 5, scale = 2)
    private BigDecimal taxRateOverride;

    @Column(name = "unit_price", nullable = false, precision = 19, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "tax_rate", nullable = false, precision = 5, scale = 2)
    private BigDecimal taxRate;

    @Column(name = "subtotal", nullable = false, precision = 19, scale = 2)
    private BigDecimal subtotal;

    @Column(name = "tax_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal taxAmount;

    @Column(name = "total", nullable = false, precision = 19, scale = 2)
    private BigDecimal total;
}
