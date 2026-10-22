package co.unicauca.edu.co.contables.configuration.enterprise.domain.services;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import co.unicauca.edu.co.contables.configuration.enterprise.dataAccess.entity.EnterpriseEntity;
import co.unicauca.edu.co.contables.configuration.enterprise.dataAccess.mapper.EnterpriseTypeDataMapper;
import co.unicauca.edu.co.contables.configuration.enterprise.dataAccess.mapper.LocationDataMapper;
import co.unicauca.edu.co.contables.configuration.enterprise.dataAccess.mapper.PersonTypeDataMapper;
import co.unicauca.edu.co.contables.configuration.enterprise.dataAccess.mapper.TaxLiabilityDataMapper;
import co.unicauca.edu.co.contables.configuration.enterprise.dataAccess.mapper.TaxPayerTypeDataMapper;
import co.unicauca.edu.co.contables.configuration.enterprise.dataAccess.repository.EnterpriseRepository;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.models.BackupManifest;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.models.BackupManifest.EnterpriseSnapshot;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.models.BackupManifest.SubjectSnapshot;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.models.City;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.models.Country;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.models.Department;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.models.Enterprise;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.models.EnterpriseType;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.models.Location;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.models.PersonType;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.models.RestoreEquivalenceMap;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.models.Subject;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.models.TaxLiability;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.models.TaxPayerType;
import co.unicauca.edu.co.contables.configuration.enterprise.presentation.exception.BackupTooLargeException;
import co.unicauca.edu.co.contables.configuration.enterprise.presentation.exception.EnterpriseNotFoundException;
import co.unicauca.edu.co.contables.configuration.enterprise.presentation.exception.InvalidBackupFileException;
import co.unicauca.edu.co.contables.configuration.enterprise.presentation.exception.InvalidRestoreRequestException;
import co.unicauca.edu.co.contables.configuration.enterprise.presentation.exception.RestoreFailedException;
import lombok.RequiredArgsConstructor;

/**
 * @brief Implementación del restore (import) de una empresa.
 */
@Service
@RequiredArgsConstructor
public class EnterpriseRestoreServiceImpl implements IEnterpriseRestoreService {

    private final IEnterpriseBackupService backupService;
    private final IEnterpriseService enterpriseService;
    private final EnterpriseRepository enterpriseRepository;
    private final ILocationService locationService;
    private final IPersonTypeService personTypeService;
    private final ISubjectService subjectService;

    private final LocationDataMapper locationDataMapper;
    private final PersonTypeDataMapper personTypeDataMapper;
    private final TaxLiabilityDataMapper taxLiabilityDataMapper;
    private final TaxPayerTypeDataMapper taxPayerTypeDataMapper;
    private final EnterpriseTypeDataMapper enterpriseTypeDataMapper;

    @Value("${app.backup.max-size-bytes:524288000}")
    private long maxUploadSizeBytes;

    // @Transactional va en estos dos métodos públicos (los del puerto), no en
    // el "restore" interno: este último se invoca siempre por auto-invocación
    // (this.restore(...)) desde dentro de la misma instancia, y el proxy de
    // Spring AOP que hace efectivo @Transactional no intercepta llamadas que
    // no pasan por él. Poniéndolo aquí, el restore interno completo (borrar
    // fila vieja, crear fila nueva, upsert de materias) queda dentro de la
    // misma transacción abierta por el proxy al entrar por este método.
    @Override
    @Transactional
    public UUID restoreFromBackupId(String backupId, boolean inplace, UUID targetEnterpriseId) {
        return restore(backupService.readManifest(backupId), inplace, targetEnterpriseId);
    }

    @Override
    @Transactional
    public UUID restoreFromFile(MultipartFile file, boolean inplace, UUID targetEnterpriseId) {
        if (file == null || file.isEmpty()) {
            throw new InvalidBackupFileException("El archivo de backup subido está vacío");
        }
        if (file.getSize() > maxUploadSizeBytes) {
            throw new BackupTooLargeException(file.getSize(), maxUploadSizeBytes);
        }
        byte[] zipBytes;
        try {
            zipBytes = file.getBytes();
        } catch (IOException e) {
            throw new InvalidBackupFileException("No se pudo leer el archivo de backup subido", e);
        }
        return restore(backupService.readManifest(zipBytes), inplace, targetEnterpriseId);
    }

    private UUID restore(BackupManifest manifest, boolean inplace, UUID targetEnterpriseId) {
        if (inplace && targetEnterpriseId == null) {
            throw new InvalidRestoreRequestException(
                    "inplace=true requiere indicar targetEnterpriseId (la empresa a sobrescribir)");
        }

        RestoreEquivalenceMap equivalenceMap = new RestoreEquivalenceMap();
        try {
            UUID resultId = inplace
                    ? restoreInPlace(manifest.getEnterprise(), targetEnterpriseId, equivalenceMap)
                    : restoreAsNew(manifest.getEnterprise());

            restoreSubjects(manifest.getSubjects());
            return resultId;
        } catch (DataIntegrityViolationException e) {
            throw new RestoreFailedException(
                    "El restore violó una restricción de integridad referencial; revise los ids de "
                            + "ciudad/departamento/país y catálogos (responsabilidad tributaria, tipo de "
                            + "contribuyente, tipo de empresa) contenidos en el backup",
                    e);
        }
    }

    private UUID restoreAsNew(EnterpriseSnapshot snapshot) {
        Enterprise toCreate = toDomainEnterprise(snapshot);
        Enterprise created = enterpriseService.create(toCreate);
        return created.getId();
    }

    private UUID restoreInPlace(EnterpriseSnapshot snapshot, UUID targetEnterpriseId,
            RestoreEquivalenceMap equivalenceMap) {
        EnterpriseEntity current = enterpriseRepository.findById(targetEnterpriseId)
                .orElseThrow(() -> new EnterpriseNotFoundException(targetEnterpriseId));

        Long oldLocationId = current.getLocation() != null ? current.getLocation().getId() : null;
        Long oldPersonTypeId = current.getPersonType() != null ? current.getPersonType().getId() : null;

        Enterprise fromBackup = toDomainEnterprise(snapshot);

        Location newLocation = locationService.create(fromBackup.getLocation());
        PersonType newPersonType = personTypeService.create(fromBackup.getPersonType());

        if (oldLocationId != null && newLocation != null) {
            equivalenceMap.put("location", oldLocationId, newLocation.getId());
        }
        if (oldPersonTypeId != null && newPersonType != null) {
            equivalenceMap.put("person_type", oldPersonTypeId, newPersonType.getId());
        }

        current.setName(fromBackup.getName());
        current.setNit(fromBackup.getNit());
        current.setDV(fromBackup.getDV());
        current.setPhone(fromBackup.getPhone());
        current.setBranch(fromBackup.getBranch());
        current.setEmail(fromBackup.getEmail());
        current.setLogo(fromBackup.getLogo());
        current.setMainActivity(fromBackup.getMainActivity());
        current.setSecondaryActivity(fromBackup.getSecondaryActivity());
        current.setState(fromBackup.getState());
        current.setInventoryConfigurationType(fromBackup.getInventoryConfigurationType());
        current.setTaxLiabilities(taxLiabilityDataMapper.toEntityList(fromBackup.getTaxLiabilities()));
        current.setTaxPayerType(taxPayerTypeDataMapper.toEntity(fromBackup.getTaxPayerType()));
        current.setEnterpriseType(enterpriseTypeDataMapper.toEntity(fromBackup.getEnterpriseType()));
        current.setPersonType(personTypeDataMapper.toEntity(newPersonType));
        current.setLocation(locationDataMapper.toEntity(newLocation));

        enterpriseRepository.save(current);

        if (oldLocationId != null) {
            locationService.delete(oldLocationId);
        }
        if (oldPersonTypeId != null) {
            personTypeService.delete(oldPersonTypeId);
        }

        return targetEnterpriseId;
    }

    /**
     * Las materias (subjects) son datos del tenant, no de una empresa
     * puntual (ver {@link BackupManifest}), así que tanto en restore
     * {@code inplace} como en uno que crea empresa nueva se tratan igual:
     * upsert por código. Nunca se borran materias existentes del tenant
     * porque, al no estar atadas a una sola empresa, podrían seguir en uso
     * por otra empresa del mismo tenant.
     */
    private void restoreSubjects(List<SubjectSnapshot> subjects) {
        if (subjects == null) {
            return;
        }
        for (SubjectSnapshot s : subjects) {
            try {
                subjectService.update(s.getCode(), Subject.builder().code(s.getCode()).name(s.getName()).build());
            } catch (RuntimeException notFound) {
                subjectService.create(Subject.builder().code(s.getCode()).name(s.getName()).build());
            }
        }
    }

    private Enterprise toDomainEnterprise(EnterpriseSnapshot snapshot) {
        return Enterprise.builder()
                .idUser(snapshot.getIdUser())
                .name(snapshot.getName())
                .nit(snapshot.getNit())
                .DV(snapshot.getDV())
                .phone(snapshot.getPhone())
                .branch(snapshot.getBranch())
                .email(snapshot.getEmail())
                .logo(snapshot.getLogo())
                .mainActivity(snapshot.getMainActivity())
                .secondaryActivity(snapshot.getSecondaryActivity())
                .state(snapshot.getState())
                .inventoryConfigurationType(snapshot.getInventoryConfigurationType())
                .taxLiabilities(toTaxLiabilities(snapshot.getTaxLiabilityIds()))
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

    private List<TaxLiability> toTaxLiabilities(List<Long> ids) {
        if (ids == null) {
            return null;
        }
        return ids.stream().map(id -> TaxLiability.builder().id(id).build()).toList();
    }
}
