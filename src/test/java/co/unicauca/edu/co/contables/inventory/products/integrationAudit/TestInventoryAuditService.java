package co.unicauca.edu.co.contables.inventory.products.integrationAudit;

import org.springframework.stereotype.Service;

import co.unicauca.edu.co.contables.inventory.products.domain.model.Category;
import co.unicauca.edu.co.contables.inventory.products.domain.model.Product;
import co.unicauca.edu.co.contables.inventory.products.infraestructure.audit.annotation.Auditable;
import co.unicauca.edu.co.contables.inventory.products.infraestructure.audit.annotation.OperationType;

@Service
public class TestInventoryAuditService {

    @Auditable(operationType = OperationType.CREATE, affectedTable = "CATEGORY")
    public Category createCategory() {
        return Category.builder()
                .id(1L)
                .enterpriseId("ENT-1")
                .name("CATEGORY")
                .state(true)
                .build();
    }

    @Auditable(operationType = OperationType.UPDATE, affectedTable = "PRODUCT", idArgIndex = 0, enterpriseIdArgIndex = 2)
    public Product updateProduct(Long id, Product product, String enterpriseId) {

        return Product.builder()
                .id(id)
                .enterpriseId(enterpriseId)
                .name(product.getName())
                .state(true)
                .build();
    }

    @Auditable(operationType = OperationType.DELETE, affectedTable = "PRODUCT_TYPE")
    public void deleteProductType(Long id, String enterpriseId) {
    }

    @Auditable(operationType = OperationType.INACTIVATE, affectedTable = "UNIT_OF_MEASURE")
    public void changeUnitState(Long id, String enterpriseId) {
    }
}
