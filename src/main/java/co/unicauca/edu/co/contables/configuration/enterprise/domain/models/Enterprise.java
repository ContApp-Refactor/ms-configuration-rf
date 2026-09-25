package co.unicauca.edu.co.contables.configuration.enterprise.domain.models;

import co.unicauca.edu.co.contables.configuration.enterprise.domain.enums.InventoryConfigurationTypeEnum;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.enums.StateEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

/**
 * Modelo de dominio que representa una empresa.
 * Contiene toda la información relacionada con una empresa, incluyendo
 * sus datos de identificación, contacto, ubicación y características fiscales.
 *
 * @author CONTAPP
 * @version 1.0
 * @since 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Enterprise {

    /**
     * Identificador único de la empresa.
     */
    private UUID id;

    /**
     * Identificador del usuario asociado a la empresa.
     */
    private String idUser;

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
     * Código de la actividad principal de la empresa.
     */
    private Long mainActivity;

    /**
     * Código de la actividad secundaria de la empresa.
     */
    private Long secondaryActivity;

    /**
     * Estado actual de la empresa.
     */
    private StateEnum state;

    /**
     * configuración de inventario(PEPS, Promedio Ponderado) empresa.
    */
    private InventoryConfigurationTypeEnum inventoryConfigurationType;

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
     * Ubicación física de la empresa.
     */
    Location location;
}
