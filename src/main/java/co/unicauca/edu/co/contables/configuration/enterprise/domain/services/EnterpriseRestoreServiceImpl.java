package co.unicauca.edu.co.contables.configuration.enterprise.domain.services;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.UUID;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.unicauca.edu.co.contables.commons.backup.BackupKeys;
import co.unicauca.edu.co.contables.commons.backup.RestoreActor;
import co.unicauca.edu.co.contables.commons.backup.zip.ZipBackupFormat;
import co.unicauca.edu.co.contables.commons.backup.zip.ZipBackupReader;
import co.unicauca.edu.co.contables.configuration.enterprise.dataAccess.entity.EnterpriseEntity;
import co.unicauca.edu.co.contables.configuration.enterprise.dataAccess.mapper.EnterpriseTypeDataMapper;
import co.unicauca.edu.co.contables.configuration.enterprise.dataAccess.mapper.LocationDataMapper;
import co.unicauca.edu.co.contables.configuration.enterprise.dataAccess.mapper.PersonTypeDataMapper;
import co.unicauca.edu.co.contables.configuration.enterprise.dataAccess.mapper.TaxLiabilityDataMapper;
import co.unicauca.edu.co.contables.configuration.enterprise.dataAccess.mapper.TaxPayerTypeDataMapper;
import co.unicauca.edu.co.contables.configuration.enterprise.dataAccess.repository.EnterpriseRepository;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.models.BackupManifest;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.models.BackupManifest.EnterpriseSnapshot;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.models.Enterprise;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.models.Location;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.models.PersonType;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.services.backup.EnterpriseSnapshotSupport;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.services.coordination.BackupErrorMessages;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.services.coordination.BackupProgressListener;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.services.coordination.IEnterpriseBackupCoordinator;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.services.coordination.RestoreOptions;
import co.unicauca.edu.co.contables.configuration.enterprise.presentation.exception.EnterpriseNotFoundException;
import co.unicauca.edu.co.contables.configuration.enterprise.presentation.exception.InvalidRestoreRequestException;
import co.unicauca.edu.co.contables.configuration.enterprise.presentation.exception.RestoreFailedException;
import lombok.RequiredArgsConstructor;

/**
 * @brief Restore (import) de una empresa: empresa nueva vía coordinador;
 * in-place solo para ZIP v1 (D4), con la lógica de siempre.
 */
@Service
@RequiredArgsConstructor
public class EnterpriseRestoreServiceImpl implements IEnterpriseRestoreService {

    private final IEnterpriseBackupCoordinator coordinator;
    private final ZipBackupFormat format;
    private final EnterpriseRepository enterpriseRepository;
    private final ILocationService locationService;
    private final IPersonTypeService personTypeService;

    private final LocationDataMapper locationDataMapper;
    private final PersonTypeDataMapper personTypeDataMapper;
    private final TaxLiabilityDataMapper taxLiabilityDataMapper;
    private final TaxPayerTypeDataMapper taxPayerTypeDataMapper;
    private final EnterpriseTypeDataMapper enterpriseTypeDataMapper;
    private final EnterpriseSnapshotSupport support;

    @Override
    public int formatVersion(Path zip) {
        try (ZipBackupReader reader = format.open(zip)) {
            return reader.formatVersion();
        }
    }

    @Override
    public UUID restore(Path zip, RestoreOptions options, RestoreActor actor) {
        return coordinator.restore(zip, options, actor, BackupProgressListener.NONE).enterpriseId();
    }

    @Override
    public BackupManifest validateInPlace(Path zip, UUID targetEnterpriseId) {
        if (targetEnterpriseId == null) {
            throw new InvalidRestoreRequestException(
                    "inplace=true requiere indicar targetEnterpriseId (la empresa a sobrescribir)");
        }
        BackupManifest payload = readV1(zip);
        if (!enterpriseRepository.existsById(targetEnterpriseId)) {
            throw new EnterpriseNotFoundException(targetEnterpriseId);
        }
        return payload;
    }

    @Override
    @Transactional
    public UUID restoreInPlace(Path zip, UUID targetEnterpriseId) {
        if (targetEnterpriseId == null) {
            throw new InvalidRestoreRequestException(
                    "inplace=true requiere indicar targetEnterpriseId (la empresa a sobrescribir)");
        }
        EnterpriseSnapshot snapshot = readV1(zip).getEnterprise();
        try {
            UUID resultId = overwrite(snapshot, targetEnterpriseId);
            // Con ids UUID los save() no hacen flush: sin esto la violación ocurriría en el commit,
            // fuera de este try, y se vería como un 409 genérico.
            enterpriseRepository.flush();
            return resultId;
        } catch (DataIntegrityViolationException e) {
            throw new RestoreFailedException(buildIntegrityMessage(e),
                    BackupErrorMessages.integrityUserMessage(BackupKeys.MODULE_ENTERPRISE, e), e);
        }
    }

    /** Mensaje técnico con la causa real de la violación (se conserva por compatibilidad). */
    public static String buildIntegrityMessage(DataIntegrityViolationException e) {
        return BackupErrorMessages.integrityMessage(e);
    }

    /** D4: sobrescribir solo con ZIP v1; el ZIP se valida completo (estructura y tamaños). */
    private BackupManifest readV1(Path zip) {
        try (ZipBackupReader reader = format.open(zip)) {
            if (reader.formatVersion() != 1) {
                throw new InvalidRestoreRequestException("Esta copia es completa (versión " + reader.formatVersion()
                        + "): no se puede restaurar sobrescribiendo una empresa. Restáurala como empresa nueva.");
            }
            reader.verifyAll();
            return EnterpriseSnapshotSupport.readPayload(reader.module(BackupKeys.MODULE_ENTERPRISE));
        }
    }

    private UUID overwrite(EnterpriseSnapshot snapshot, UUID targetEnterpriseId) {
        EnterpriseEntity current = enterpriseRepository.findById(targetEnterpriseId)
                .orElseThrow(() -> new EnterpriseNotFoundException(targetEnterpriseId));

        Long oldLocationId = current.getLocation() != null ? current.getLocation().getId() : null;
        Long oldPersonTypeId = current.getPersonType() != null ? current.getPersonType().getId() : null;

        // idUser no cambia en in-place: la empresa sigue siendo de su dueño actual.
        Enterprise fromBackup = support.toDomainEnterprise(snapshot, current.getIdUser(), snapshot.getName(),
                snapshot.getNit());

        Location newLocation = locationService.create(fromBackup.getLocation());
        PersonType newPersonType = personTypeService.create(fromBackup.getPersonType());

        current.setName(fromBackup.getName());
        current.setNit(fromBackup.getNit());
        current.setDV(fromBackup.getDV());
        current.setPhone(fromBackup.getPhone());
        current.setBranch(fromBackup.getBranch());
        current.setEmail(fromBackup.getEmail());
        // v1 in-place conserva el comportamiento histórico: la URL de logo de la copia tal cual.
        current.setLogo(snapshot.getLogo());
        current.setMainActivity(fromBackup.getMainActivity());
        current.setSecondaryActivity(fromBackup.getSecondaryActivity());
        current.setState(fromBackup.getState());
        current.setInventoryConfigurationType(fromBackup.getInventoryConfigurationType());
        current.setTaxLiabilities(taxLiabilityDataMapper.toEntityList(fromBackup.getTaxLiabilities()));
        current.setTaxPayerType(taxPayerTypeDataMapper.toEntity(fromBackup.getTaxPayerType()));
        current.setEnterpriseType(enterpriseTypeDataMapper.toEntity(fromBackup.getEnterpriseType()));
        current.setPersonType(personTypeDataMapper.toEntity(newPersonType));
        current.setLocation(locationDataMapper.toEntity(newLocation));
        // subjectCodes ausente (ZIP antiguo) = no hay información: se conservan las materias actuales.
        if (snapshot.getSubjectCodes() != null) {
            current.setSubjects(new ArrayList<>(support.findSubjectsByCodes(snapshot.getSubjectCodes())));
        }

        enterpriseRepository.save(current);

        if (oldLocationId != null) {
            locationService.delete(oldLocationId);
        }
        if (oldPersonTypeId != null) {
            personTypeService.delete(oldPersonTypeId);
        }

        return targetEnterpriseId;
    }
}
