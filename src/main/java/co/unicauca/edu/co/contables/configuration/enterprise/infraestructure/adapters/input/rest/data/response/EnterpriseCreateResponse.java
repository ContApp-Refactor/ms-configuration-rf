package co.unicauca.edu.co.contables.configuration.enterprise.infraestructure.adapters.input.rest.data.response;

import co.unicauca.edu.co.contables.configuration.enterprise.domain.enums.InventoryConfigurationTypeEnum;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.models.EnterpriseType;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.models.PersonType;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.models.TaxLiability;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.models.TaxPayerType;
import co.unicauca.edu.co.contables.configuration.enterprise.infraestructure.adapters.input.rest.data.dto.LocationResponseDto;
import lombok.*;

import java.util.List;
import java.util.UUID;

/**
 * Clase de respuesta que representa la información de una empresa recién creada.
 * Se utiliza específicamente para respuestas REST después de crear una nueva empresa,
 * incluyendo todos sus datos asociados.
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
public class EnterpriseCreateResponse {
    
    /**
     * Identificador único de la empresa.
     */
    private UUID id;

    /**
     * Nombre o razón social de la empresa.
     */
    private String name;

    /**
     * Número de identificación tributaria.
     */
    private String nit;

    /**
     * Dígito de verificación del NIT.
     */
    private String DV;

    /**
     * Número de teléfono de contacto.
     */
    private String phone;

    /**
     * Sector o rama de actividad de la empresa.
     */
    private String branch;

    /**
     * Correo electrónico de contacto.
     */
    private String email;

    /**
     * URL o ruta del logo de la empresa.
     */
    private String logo;

    /**
     * Código de la actividad principal.
     */
    private Long mainActivity;

    /**
     * tipo de configuración de inventario (PEPS,WEIGHT_AVERAGE).
     */
    private InventoryConfigurationTypeEnum inventoryConfigurationType;


    /**
     * Código de la actividad secundaria.
     */
    private Long secondaryActivity;

    /**
     * Lista de responsabilidades fiscales de la empresa.
     */
    List<TaxLiability> taxLiabilities;

    /**
     * Tipo de contribuyente de la empresa.
     */
    TaxPayerType taxPayerType;

    /**
     * Tipo de empresa.
     */
    EnterpriseType enterpriseType;

    /**
     * Tipo de persona (natural o jurídica).
     */
    PersonType personType;

    /**
     * Ubicación de la empresa.
     */
    LocationResponseDto location;
}
