package co.unicauca.edu.co.contables.inventory.peps.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * @brief Domain entity representing a Product
 * 
 * Manages product identity, state, and associated kardex records.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Product {
    private Long id;
    private Long productId;
    private String name;
    private String reference;
    private String presentation;
    private String enterpriseId;
    private boolean state;
    
    private List<Kardex>recordsKardex;

    /**
     * @brief Factory method to create a new Product
     * @param productId Unique product identifier
     * @param name Product name
     * @param reference Product reference code
     * @param presentation Product presentation/packaging
     * @param enterpriseId Enterprise identifier
     * @return New Product instance
     */
    public static Product create(Long productId, String name, String reference, 
                               String presentation, String enterpriseId) {
        Product product = Product.builder()
            .productId(validateProductId(productId))
            .name(validateName(name))
            .reference(reference)
            .presentation(presentation)
            .enterpriseId(validateEnterpriseId(enterpriseId))
            .state(true) 
            .recordsKardex(new ArrayList<>())
            .build();
        return product;
    }

    /**
     * @brief Activates the product
     * @throws IllegalStateException if already active
     */
    public void activate() {
        if (this.state) {
            throw new IllegalStateException("Product is already active");
        }
        this.state = true;
    }
    
    /**
     * @brief Deactivates the product
     * @throws IllegalStateException if already inactive or has available stock
     */
    public void deactivate() {
        if (!this.state) {
            throw new IllegalStateException("Product is already inactive");
        }
        
        if (hasAvailableStock()) {
            throw new IllegalStateException("Cannot deactivate product with available stock");
        }
        
        this.state = false;
    }
     
    /**
     * @brief Checks if product is active
     * @return true if active
     */
    public boolean isActive() {
        return this.state;
    }

    /**
     * @brief Checks if product has any available stock
     * @return true if any kardex record has available quantity > 0
     */
    public boolean hasAvailableStock() {
        if (recordsKardex == null) return false;
        return recordsKardex.stream()
                .anyMatch(kardex -> kardex.getAvailableQuantity() > 0);
    }

    /**
     * @brief Calculates total available stock across all records
     * @return Sum of available quantities
     */
     public int getTotalAvailableStock() {
        if (recordsKardex == null) return 0;
        return recordsKardex.stream()
                .mapToInt(Kardex::getAvailableQuantity)
                .sum();
    }

    /**
     * @brief Adds a kardex record to the product
     * @param kardex Record to add
     */
     public void addKardexRecord(Kardex kardex) {
        Objects.requireNonNull(kardex, "Kardex record cannot be null");
        if (!kardex.belongsToProduct(this.productId)) {
            throw new IllegalArgumentException("Kardex record does not belong to this product");
        }
        if (this.recordsKardex == null) {
            this.recordsKardex = new ArrayList<>();
        }
        this.recordsKardex.add(kardex);
    }
    
    /**
     * @brief Validates if a movement can be created for this product
     * @throws IllegalStateException if product is inactive
     */
    public void validateForMovementCreation() {
        if (!this.state) {
            throw new IllegalArgumentException("Cannot create movement for inactive product");
        }
    }
    
    /**
     * @brief Validates all required fields are present
     */
    public void validateRequiredFields() {
        validateProductId(this.productId);
        validateName(this.name);
        validateEnterpriseId(this.enterpriseId);
    }
    
    /**
     * @brief Gets a read-only view of kardex records
     * @return Unmodifiable list of records
     */
    public List<Kardex> getRecordsKardexReadOnly() {
        if (recordsKardex == null) return Collections.emptyList();
        return Collections.unmodifiableList(recordsKardex);
    }
    
    // Métodos de validación
    private static Long validateProductId(Long productId) {
        if (productId == null || productId <= 0) {
            throw new IllegalArgumentException("Product ID must be positive");
        }
        return productId;
    }
    
    private static String validateName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Product name cannot be empty");
        }
        if (name.length() > 255) {
            throw new IllegalArgumentException("Product name is too long (max 255 characters)");
        }
        return name.trim();
    }
    
    private static String validateEnterpriseId(String enterpriseId) {
        if (enterpriseId == null || enterpriseId.trim().isEmpty()) {
            throw new IllegalArgumentException("Enterprise ID cannot be empty");
        }
        return enterpriseId.trim();
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Product product = (Product) o;
        return Objects.equals(productId, product.productId);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(productId);
    }
    
    @Override
    public String toString() {
        return String.format("Product{productId=%d, name='%s', state=%s}", productId, name, state);
    }


    /**
     * @brief Normalizes product fields for consistent storage
     * 
     * Trims whitespace and converts to appropriate format.
     */
    public void normalize() {
        if (this.name != null) {
            this.name = this.name.trim();
        }
        
        if (this.reference != null) {
            this.reference = this.reference.trim().toUpperCase();
        }
        
        if (this.presentation != null) {
            this.presentation = this.presentation.trim();
        }
        
        if (this.enterpriseId != null) {
            this.enterpriseId = this.enterpriseId.trim();
        }
    }

}
