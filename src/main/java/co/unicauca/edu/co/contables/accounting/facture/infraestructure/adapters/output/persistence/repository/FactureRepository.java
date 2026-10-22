package co.unicauca.edu.co.contables.accounting.facture.infraestructure.adapters.output.persistence.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import co.unicauca.edu.co.contables.accounting.facture.infraestructure.adapters.output.persistence.entity.FactureEntity;

@Repository
public interface FactureRepository extends JpaRepository<FactureEntity, Long> {

    /**
     * Devuelve el código de factura máximo de la tabla FactureEntity.
     * 
     * @return el código de factura máximo
     */
    @Query("SELECT MAX(f.factCode) FROM FactureEntity f")
    Long findMaxFactCode(); // This method returns the maximum factCode

    /**
     * Recupera una lista paginada de todas las facturas asociadas a una empresa
     * específica,
     * incluyendo sus productos asociados.
     *
     * @param entId    el ID de la empresa para la cual se recuperarán las facturas
     * @param pageable la configuración de paginación
     * @return una lista paginada de facturas asociadas al ID de empresa dado
     */
    @EntityGraph(attributePaths = "factProducts")
    @Query("SELECT f FROM FactureEntity f WHERE f.entId LIKE :entId")
    Page<FactureEntity> findAllByEnterpriseId(String entId, Pageable pageable);

    /**
     * Recupera una lista paginada de todas las facturas de ventas asociadas a una
     * empresa específica, incluyendo sus productos asociados.
     *
     * @param entId    el ID de la empresa para la cual se recuperarán las facturas
     *                 de
     *                 ventas
     * @param pageable la configuración de paginación
     * @return una lista paginada de facturas de ventas asociadas al ID de empresa
     *         dado
     */
    @EntityGraph(attributePaths = "factProducts")
    @Query("SELECT f FROM FactureEntity f WHERE f.entId LIKE :entId AND f.factureType LIKE 'Venta'")
    Page<FactureEntity> findAllSalesFacturesByEnterpriseId(String entId, Pageable pageable);

    /**
     * Recupera una lista paginada de todas las facturas de compras asociadas a una
     * empresa específica, incluyendo sus productos asociados.
     *
     * @param entId    el ID de la empresa para la cual se recuperarán las facturas
     *                 de
     *                 compras
     * @param pageable la configuración de paginación
     * @return una lista paginada de facturas de compras asociadas al ID de empresa
     *         dado
     */
    @EntityGraph(attributePaths = "factProducts")
    @Query("SELECT f FROM FactureEntity f WHERE f.entId LIKE :entId AND f.factureType LIKE 'Compra'")
    Page<FactureEntity> findAllShoppingFacturesByEnterpriseId(String entId, Pageable pageable);
}
