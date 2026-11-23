package co.unicauca.edu.co.contables.accounting.facture.infraestructure.adapters.output.persistence.repository;

import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import co.unicauca.edu.co.contables.accounting.facture.infraestructure.adapters.output.persistence.entity.ProductEntity;

public interface ProductRepository extends JpaRepository<ProductEntity, Long>{
    /**
     * Obtiene una lista de productos asociados a una factura por su Id.
     *
     * @param factId el Id de la factura.
     * @return una lista de productos asociados a la factura.
     */
    @Query("SELECT p FROM ProductEntity p INNER JOIN p.factures f WHERE f.factId = :factId")
    Set<ProductEntity> getProductsByFactureId(Long factId);
    
}
