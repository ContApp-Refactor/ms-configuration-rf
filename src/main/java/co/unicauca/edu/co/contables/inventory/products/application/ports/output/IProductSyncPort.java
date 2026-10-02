package co.unicauca.edu.co.contables.inventory.products.application.ports.output;

import java.util.List;

import co.unicauca.edu.co.contables.inventory.products.application.dto.ProductSyncDto;

import java.time.Instant;

/**
 * @brief Puerto de salida para sincronización de productos entre sistemas
 *
 * Define contrato para operaciones de sincronización de productos basadas en timestamp,
 * utilizado para replicación de datos entre diferentes instancias del sistema.
 */
public interface IProductSyncPort {
    /**
     * @brief Busca productos para sincronización por empresa y fecha
     * @param enterpriseId el ID de la empresa
     * @param since timestamp desde cuándo buscar cambios
     * @return lista de productos modificados desde el timestamp especificado
     */
    List<ProductSyncDto> findByEnterpriseId(String enterpriseId, Instant since);
}
