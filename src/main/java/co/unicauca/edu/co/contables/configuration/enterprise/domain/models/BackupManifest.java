package co.unicauca.edu.co.contables.configuration.enterprise.domain.models;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import co.unicauca.edu.co.contables.configuration.enterprise.domain.enums.InventoryConfigurationTypeEnum;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.enums.StateEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @brief Contrato (contenido de {@code enterprise.json}) de una copia de
 * seguridad de empresa.
 *
 * Es el formato de intercambio que serializa {@code IEnterpriseBackupService}
 * y que consume {@code IEnterpriseRestoreService}. A diferencia del
 * microservicio de referencia, aquí no se exportan filas crudas de catálogo
 * (país/departamento/ciudad, responsabilidad tributaria, tipo de
 * contribuyente, tipo de empresa): esos datos ya existen en la base del
 * monolito y el restore solo necesita sus ids para volver a enlazarlos.
 * Lo que sí se exporta por valor es lo que el restore recrea como datos
 * nuevos: la propia empresa, su tipo de persona, su ubicación (dirección +
 * ids de ciudad/departamento/país) y las materias (subjects) visibles en el
 * tenant de la empresa de origen.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BackupManifest {

    /** Versión del formato del manifiesto, para evolucionarlo sin romper backups viejos. */
    private int version;

    private LocalDateTime exportedAt;

    private UUID sourceEnterpriseId;

    private EnterpriseSnapshot enterprise;

    private List<SubjectSnapshot> subjects;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class EnterpriseSnapshot {
        private String idUser;
        private String name;
        private String nit;
        private String DV;
        private String phone;
        private String branch;
        private String email;
        private String logo;
        private Long mainActivity;
        private Long secondaryActivity;
        private StateEnum state;
        private InventoryConfigurationTypeEnum inventoryConfigurationType;
        private List<Long> taxLiabilityIds;
        private Long taxPayerTypeId;
        private Long enterpriseTypeId;
        private PersonTypeSnapshot personType;
        private LocationSnapshot location;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PersonTypeSnapshot {
        private String type;
        private String name;
        private String surname;
        private String bussinessName;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class LocationSnapshot {
        private String address;
        private Long cityId;
        private Long departmentId;
        private Long countryId;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SubjectSnapshot {
        private String code;
        private String name;
    }
}
