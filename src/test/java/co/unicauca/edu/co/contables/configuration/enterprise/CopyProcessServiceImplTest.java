package co.unicauca.edu.co.contables.configuration.enterprise;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.transaction.PlatformTransactionManager;

import co.unicauca.edu.co.contables.commons.backup.BackupCorruptedException;
import co.unicauca.edu.co.contables.commons.backup.RestoreActor;
import co.unicauca.edu.co.contables.configuration.enterprise.dataAccess.entity.CopyProcessEntity;
import co.unicauca.edu.co.contables.configuration.enterprise.dataAccess.mapper.CopyProcessDataMapper;
import co.unicauca.edu.co.contables.configuration.enterprise.dataAccess.repository.CopyProcessRepository;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.dto.BackupReferenceDto;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.enums.CopyProcessEstadoEnum;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.enums.CopyProcessTipoEnum;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.models.CopyProcess;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.services.CopyProcessServiceImpl;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.services.ICopyProcessService.RestoreRequest;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.services.IEnterpriseBackupService;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.services.IEnterpriseRestoreService;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.services.coordination.IEnterpriseBackupCoordinator;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.services.coordination.RestoreResult;
import co.unicauca.edu.co.contables.configuration.enterprise.presentation.exception.BackupNotFoundException;

/** T5: backupRef ligado al tenant, ZIP compartido, upload rechazado sin registro y DUPLICATE en un solo proceso. */
class CopyProcessServiceImplTest {

    private static final RestoreActor ACTOR = new RestoreActor("tenant-A", "user-A");

    private CopyProcessRepository repository;
    private IEnterpriseBackupService backupService;
    private IEnterpriseBackupCoordinator coordinator;
    private CopyProcessServiceImpl service;

    @BeforeEach
    void setUp() {
        repository = mock(CopyProcessRepository.class);
        backupService = mock(IEnterpriseBackupService.class);
        coordinator = mock(IEnterpriseBackupCoordinator.class);
        CopyProcessDataMapper mapper = mock(CopyProcessDataMapper.class);
        when(mapper.toDomain(any())).thenAnswer(inv -> {
            CopyProcessEntity e = inv.getArgument(0);
            return CopyProcess.builder().idProceso(e.getIdProceso()).estado(e.getEstado())
                    .empresaDestino(e.getEmpresaDestino()).errorResumen(e.getErrorResumen())
                    .progreso(e.getProgreso()).faseActual(e.getFaseActual()).build();
        });
        when(repository.save(any(CopyProcessEntity.class))).thenAnswer(inv -> {
            CopyProcessEntity e = inv.getArgument(0);
            if (e.getIdProceso() == null) {
                e.setIdProceso(UUID.randomUUID());
            }
            return e;
        });
        service = new CopyProcessServiceImpl(repository, mapper, backupService,
                mock(IEnterpriseRestoreService.class), coordinator, mock(PlatformTransactionManager.class));
    }

    private static RestoreRequest nueva(String nombre) {
        return new RestoreRequest(nombre, false, null, null, false, ACTOR, "user-a");
    }

    @Test
    void unBackupRefQueNoEsDelTenantDa404SinLeerElArchivo() {
        when(repository.existsByBackupRef("ajeno")).thenReturn(false);

        assertThatThrownBy(() -> service.restoreFromBackupRef("ajeno", nueva("Nueva")))
                .isInstanceOf(BackupNotFoundException.class);
        verify(backupService, never()).resolveBackupFile(anyString());
        verify(coordinator, never()).prepare(any(), any(), any());
    }

    @Test
    void borrarUnRestoreNoBorraElZipQueComparteConSuBackup() {
        UUID id = UUID.randomUUID();
        CopyProcessEntity restore = CopyProcessEntity.builder().idProceso(id).tipo(CopyProcessTipoEnum.RESTORE)
                .estado(CopyProcessEstadoEnum.COMPLETADO).backupRef("ref").build();
        when(repository.findById(id)).thenReturn(Optional.of(restore));
        when(repository.countByBackupRef("ref")).thenReturn(2L);

        service.delete(id);

        verify(backupService, never()).deleteBackup(anyString());
        verify(repository).delete(restore);
    }

    @Test
    void uploadInvalidoNoCreaProcesoYBorraElArchivoGuardado() {
        MockMultipartFile file = new MockMultipartFile("file", "b.zip", "application/zip", new byte[] { 1, 2 });
        when(backupService.storeUpload(file)).thenReturn(BackupReferenceDto.builder().backupId("up-1").build());
        when(backupService.resolveBackupFile("up-1")).thenReturn(Path.of("up-1.zip"));
        when(coordinator.prepare(any(), any(), any())).thenThrow(new BackupCorruptedException("no es un ZIP"));

        assertThatThrownBy(() -> service.restoreFromUpload(file, nueva(UUID.randomUUID().toString())))
                .isInstanceOf(BackupCorruptedException.class);
        verify(backupService).deleteBackup("up-1");
        verify(repository, never()).save(any());
    }

    @Test
    void duplicarEsUnSoloProcesoQueTerminaConLaEmpresaNueva() {
        UUID origen = UUID.randomUUID();
        UUID nueva = UUID.randomUUID();
        when(coordinator.duplicate(any(), any(), any(), any(), any()))
                .thenReturn(new RestoreResult(nueva, 2, List.of("enterprise"), List.of(), java.util.Map.of()));

        CopyProcess p = service.create(CopyProcessTipoEnum.DUPLICATE, origen, "Emp (copia)", ACTOR, "user-a");

        assertThat(p.getEstado()).isEqualTo(CopyProcessEstadoEnum.COMPLETADO);
        assertThat(p.getEmpresaDestino()).isEqualTo(nueva.toString());
        assertThat(p.getProgreso()).isEqualTo(100);
        verify(backupService, never()).backup(any());
    }
}
