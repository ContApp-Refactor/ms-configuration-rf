package co.unicauca.edu.co.contables.configuration.enterprise.domain.services.backup;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import co.unicauca.edu.co.contables.commons.backup.BackupCorruptedException;
import co.unicauca.edu.co.contables.commons.backup.BackupReader;
import co.unicauca.edu.co.contables.configuration.enterprise.dataAccess.entity.EnterpriseEntity;
import co.unicauca.edu.co.contables.configuration.enterprise.dataAccess.entity.SubjectEntity;
import co.unicauca.edu.co.contables.configuration.enterprise.dataAccess.entity.TaxLiabilityEntity;
import co.unicauca.edu.co.contables.configuration.enterprise.dataAccess.repository.SubjectRepository;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.models.BackupManifest;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.models.BackupManifest.EnterpriseSnapshot;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.models.BackupManifest.LocationSnapshot;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.models.BackupManifest.PersonTypeSnapshot;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.models.City;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.models.Country;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.models.Department;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.models.Enterprise;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.models.EnterpriseType;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.models.Location;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.models.PersonType;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.models.Subject;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.models.TaxLiability;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.models.TaxPayerType;
import lombok.RequiredArgsConstructor;

/**
 * @brief Conversión empresa ↔ {@link BackupManifest} ({@code enterprise.json}),
 * compartida por el participante {@code enterprise} (ZIP v2) y por el restore
 * in-place del ZIP v1. Es una clase aparte del participante porque, por la
 * regla ArchUnit R2, nadie fuera de este paquete puede depender del
 * participante concreto.
 *
 * El contenido de {@code enterprise/enterprise.json} (v2) es el MISMO
 * {@link BackupManifest} del ZIP v1: empresa + tipo de persona + ubicación +
 * ids de catálogos seed + códigos de materias (nunca el catálogo de materias).
 */
@Component
@RequiredArgsConstructor
public class EnterpriseSnapshotSupport {

    /** Entrada del módulo enterprise en el ZIP v2. */
    public static final String ENTRY_V2 = "enterprise/enterprise.json";
    /** Única entrada del ZIP v1. */
    public static final String ENTRY_V1 = "enterprise.json";
    /** Logo de la empresa (binario) en el ZIP v2. */
    public static final String LOGO_ASSET = "assets/logo";

    private final SubjectRepository subjectRepository;

    /** Lee el {@code enterprise.json} del módulo (v2 o v1). */
    public static BackupManifest readPayload(BackupReader reader) {
        String path = reader.has(ENTRY_V2) ? ENTRY_V2 : ENTRY_V1;
        if (!reader.has(path)) {
            throw new BackupCorruptedException("El archivo no es una copia válida: falta '" + ENTRY_V2 + "'");
        }
        BackupManifest payload = reader.readJson(path, BackupManifest.class);
        if (payload == null || payload.getEnterprise() == null) {
            throw new BackupCorruptedException("El archivo no es una copia válida: '" + path
                    + "' no contiene los datos de la empresa");
        }
        return payload;
    }

    public BackupManifest toManifest(EnterpriseEntity entity) {
        List<Long> taxLiabilityIds = entity.getTaxLiabilities() == null ? List.of()
                : entity.getTaxLiabilities().stream().map(TaxLiabilityEntity::getId).toList();

        List<String> subjectCodes = entity.getSubjects() == null ? List.of()
                : entity.getSubjects().stream().map(SubjectEntity::getCode).filter(Objects::nonNull)
                        .sorted().toList();

        PersonTypeSnapshot personTypeSnapshot = entity.getPersonType() == null ? null
                : PersonTypeSnapshot.builder()
                        .type(entity.getPersonType().getType())
                        .name(entity.getPersonType().getName())
                        .surname(entity.getPersonType().getSurname())
                        .bussinessName(entity.getPersonType().getBussinessName())
                        .build();

        LocationSnapshot locationSnapshot = entity.getLocation() == null ? null
                : LocationSnapshot.builder()
                        .address(entity.getLocation().getAddress())
                        .cityId(entity.getLocation().getCity() == null ? null
                                : entity.getLocation().getCity().getId())
                        .departmentId(entity.getLocation().getDepartment() == null ? null
                                : entity.getLocation().getDepartment().getId())
                        .countryId(entity.getLocation().getCountry() == null ? null
                                : entity.getLocation().getCountry().getId())
                        .build();

        EnterpriseSnapshot enterpriseSnapshot = EnterpriseSnapshot.builder()
                .idUser(entity.getIdUser())
                .name(entity.getName())
                .nit(entity.getNit())
                .DV(entity.getDV())
                .phone(entity.getPhone())
                .branch(entity.getBranch())
                .email(entity.getEmail())
                .logo(entity.getLogo())
                .mainActivity(entity.getMainActivity())
                .secondaryActivity(entity.getSecondaryActivity())
                .state(entity.getState())
                .inventoryConfigurationType(entity.getInventoryConfigurationType())
                .taxLiabilityIds(taxLiabilityIds)
                .subjectCodes(subjectCodes)
                .taxPayerTypeId(entity.getTaxPayerType() == null ? null : entity.getTaxPayerType().getId())
                .enterpriseTypeId(entity.getEnterpriseType() == null ? null : entity.getEnterpriseType().getId())
                .personType(personTypeSnapshot)
                .location(locationSnapshot)
                .build();

        return BackupManifest.builder()
                .version(1)
                .exportedAt(LocalDateTime.now())
                .sourceEnterpriseId(entity.getId())
                .enterprise(enterpriseSnapshot)
                .build();
    }

    /**
     * Empresa de dominio a crear/sobrescribir desde la copia. {@code idUser}
     * es SIEMPRE el usuario que restaura; {@code name}/{@code nit} ya vienen
     * resueltos (override del usuario o los de la copia). El logo NO se copia
     * (lo resuelve quien llama, ver §8 del diseño).
     */
    public Enterprise toDomainEnterprise(EnterpriseSnapshot snapshot, String userId, String name, String nit) {
        return Enterprise.builder()
                .idUser(userId)
                .name(name)
                .nit(nit)
                .DV(snapshot.getDV())
                .phone(snapshot.getPhone())
                .branch(snapshot.getBranch())
                .email(snapshot.getEmail())
                .mainActivity(snapshot.getMainActivity())
                .secondaryActivity(snapshot.getSecondaryActivity())
                .state(snapshot.getState())
                .inventoryConfigurationType(snapshot.getInventoryConfigurationType())
                .taxLiabilities(toTaxLiabilities(snapshot.getTaxLiabilityIds()))
                .subjects(snapshot.getSubjectCodes() == null ? null
                        : snapshot.getSubjectCodes().stream().filter(c -> c != null && !c.isBlank())
                                .map(c -> Subject.builder().code(c).build()).toList())
                .taxPayerType(TaxPayerType.builder().id(snapshot.getTaxPayerTypeId()).build())
                .enterpriseType(EnterpriseType.builder().id(snapshot.getEnterpriseTypeId()).build())
                .personType(snapshot.getPersonType() == null ? null
                        : PersonType.builder()
                                .type(snapshot.getPersonType().getType())
                                .name(snapshot.getPersonType().getName())
                                .surname(snapshot.getPersonType().getSurname())
                                .bussinessName(snapshot.getPersonType().getBussinessName())
                                .build())
                .location(snapshot.getLocation() == null ? null
                        : Location.builder()
                                .address(snapshot.getLocation().getAddress())
                                .city(City.builder().id(snapshot.getLocation().getCityId()).build())
                                .country(Country.builder().id(snapshot.getLocation().getCountryId()).build())
                                .department(Department.builder().id(snapshot.getLocation().getDepartmentId())
                                        .build())
                                .build())
                .build();
    }

    /** Materias del tenant actual con esos códigos; los inexistentes se omiten. */
    public List<SubjectEntity> findSubjectsByCodes(List<String> codes) {
        List<String> clean = cleanCodes(codes);
        return clean.isEmpty() ? List.of() : subjectRepository.findByCodeIn(clean);
    }

    /** Códigos de la copia que NO existen en el catálogo de materias del tenant actual. */
    public List<String> missingSubjectCodes(List<String> codes) {
        List<String> clean = cleanCodes(codes);
        if (clean.isEmpty()) {
            return List.of();
        }
        List<SubjectEntity> found = subjectRepository.findByCodeIn(clean);
        Set<String> existing = found == null ? Set.of()
                : found.stream().map(SubjectEntity::getCode).collect(Collectors.toSet());
        return clean.stream().filter(c -> !existing.contains(c)).toList();
    }

    private static List<String> cleanCodes(List<String> codes) {
        return codes == null ? List.of()
                : codes.stream().filter(c -> c != null && !c.isBlank()).distinct().toList();
    }

    private static List<TaxLiability> toTaxLiabilities(List<Long> ids) {
        if (ids == null) {
            return null;
        }
        return ids.stream().map(id -> TaxLiability.builder().id(id).build()).toList();
    }
}
