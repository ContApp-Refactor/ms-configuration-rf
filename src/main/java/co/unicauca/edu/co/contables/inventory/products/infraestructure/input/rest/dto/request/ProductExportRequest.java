package co.unicauca.edu.co.contables.inventory.products.infraestructure.input.rest.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @brief DTO para solicitudes de exportación de productos
 *
 * Contiene los parámetros de filtro para exportación asíncrona de productos:
 * empresa, estado de productos y nombre de empresa para generación del archivo.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductExportRequest {
    private String entId;
    private String companyName;
    private Boolean status; // true=activos, false=inactivos, null=todos
}

