package co.unicauca.edu.co.contables.configuration.enterprise.domain.dto;

import co.unicauca.edu.co.contables.configuration.enterprise.domain.enums.InventoryConfigurationTypeEnum;
import lombok.*;

import java.util.UUID;

/**
 * DTO (Data Transfer Object) que representa la información básica de una empresa.
 * Se utiliza para transferir datos simplificados de empresas entre diferentes
 * capas de la aplicación.
 *
 * @author CONTAPP
 * @version 1.0
 * @since 1.0.0
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EnterpriseInfoDto {
    
    /**
     * Identificador único de la empresa.
     */
    private UUID id;

    /**
     * Nombre o razón social de la empresa.
     */
    private String name;

    /**
     * Número de identificación tributaria de la empresa.
     */
    private String nit;

    /**
     * URL o ruta del logo de la empresa.
     */
    private String logo;
    
    /**
     * configuración de inventario(PEPS, Promedio Ponderado) empresa.
    */
    private InventoryConfigurationTypeEnum inventoryConfigurationType;
}
