package co.unicauca.edu.co.contables.inventory.kardex.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @brief Domain model representing a product in the system with business logic
 * 
 * Encapsulates product behavior including validation, state management,
 * and synchronization operations.
 */
@Getter @Setter
@Builder
@NoArgsConstructor @AllArgsConstructor 
public class Product {
    private Long id;

    private Long productId;

    private String reference;

    private String name;

    private String presentation;

    private String enterpriseId;

    private boolean state;

    /**
     * @brief Validates that all required fields are present and valid
     * @throws IllegalArgumentException if validation fails
     */
    public void validateRequiredFields() {
        if (productId == null) {
            throw new IllegalArgumentException("Product ID is required");
        }
        
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Product name is required and cannot be empty");
        }
        
        if (reference == null || reference.trim().isEmpty()) {
            throw new IllegalArgumentException("Product reference is required and cannot be empty");
        }
        
        if (enterpriseId == null || enterpriseId.trim().isEmpty()) {
            throw new IllegalArgumentException("Enterprise ID is required and cannot be empty");
        }
    }

    /**
     * @brief Checks if the product has all required fields populated
     * @return true if all required fields are valid, false otherwise
     */
    public boolean isValid() {
        return productId != null 
            && name != null && !name.trim().isEmpty()
            && reference != null && !reference.trim().isEmpty()
            && enterpriseId != null && !enterpriseId.trim().isEmpty();
    }

    /**
     * @brief Activates the product by setting state to true
     */
    public void activate() {
        this.state = true;
    }

    /**
     * @brief Deactivates the product by setting state to false
     */
    public void deactivate() {
        this.state = false;
    }

    /**
     * @brief Normalizes product fields for consistent storage
     * Trims whitespace and converts to appropriate format
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

    /**
     * @brief Checks if the product belongs to a specific enterprise
     * @param targetEnterpriseId Enterprise ID to check against
     * @return true if product belongs to the enterprise, false otherwise
     */
    public boolean belongsToEnterprise(String targetEnterpriseId) {
        return this.enterpriseId != null && this.enterpriseId.equals(targetEnterpriseId);
    }

    /**
     * @brief Prepares the product for persistence
     * Normalizes data and validates required fields
     */
    public void prepareForPersistence() {
        normalize();
        validateRequiredFields();
    }

    /**
     * @brief Checks if the product is active
     * @return true if product state is active, false otherwise
     */
    public boolean isActive() {
        return this.state;
    }

}
