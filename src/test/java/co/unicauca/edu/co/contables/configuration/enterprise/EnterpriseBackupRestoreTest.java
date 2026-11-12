package co.unicauca.edu.co.contables.configuration.enterprise;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.PlatformTransactionManager;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import co.unicauca.edu.co.contables.commons.backup.RestoreActor;
import co.unicauca.edu.co.contables.commons.backup.zip.ZipBackupFormat;
import co.unicauca.edu.co.contables.commons.multitenancy.utils.TenantContext;
import co.unicauca.edu.co.contables.configuration.enterprise.dataAccess.entity.EnterpriseEntity;
import co.unicauca.edu.co.contables.configuration.enterprise.dataAccess.entity.SubjectEntity;
import co.unicauca.edu.co.contables.configuration.enterprise.dataAccess.mapper.EnterpriseTypeDataMapper;
import co.unicauca.edu.co.contables.configuration.enterprise.dataAccess.mapper.LocationDataMapper;
import co.unicauca.edu.co.contables.configuration.enterprise.dataAccess.mapper.PersonTypeDataMapper;
import co.unicauca.edu.co.contables.configuration.enterprise.dataAccess.mapper.TaxLiabilityDataMapper;
import co.unicauca.edu.co.contables.configuration.enterprise.dataAccess.mapper.TaxPayerTypeDataMapper;
import co.unicauca.edu.co.contables.configuration.enterprise.dataAccess.repository.EnterpriseRepository;
import co.unicauca.edu.co.contables.configuration.enterprise.dataAccess.repository.SubjectRepository;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.dto.BackupReferenceDto;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.models.BackupManifest;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.models.Enterprise;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.models.Subject;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.services.EnterpriseBackupServiceImpl;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.services.EnterpriseRestoreServiceImpl;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.services.IEnterpriseService;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.services.ILocationService;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.services.ILogoStorageService;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.services.IPersonTypeService;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.services.backup.EnterpriseBackupParticipant;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.services.backup.EnterpriseSnapshotSupport;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.services.coordination.BackupProgressListener;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.services.coordination.EnterpriseBackupCoordinatorImpl;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.services.coordination.RestoreOptions;
import co.unicauca.edu.co.contables.configuration.enterprise.presentation.exception.InvalidRestoreRequestException;
import co.unicauca.edu.co.contables.configuration.enterprise.presentation.exception.RestoreFailedException;
import jakarta.persistence.EntityManager;

/**
 * Backup sin materias, restore tolerante a ZIP viejo, idUser actual y mensajes de error.
 *
 * Convertido al diseño ZIP v2: el backup/restore pasa por el coordinador real
 * con el participante {@code enterprise} real y repositorios/servicios
 * mockeados (la transacción es un {@code PlatformTransactionManager} mock).
 * Los ZIP v1 ({@code enterprise.json} suelto) se siguen leyendo.
 */
class EnterpriseBackupRestoreTest {

    private static final RestoreActor USER_B = new RestoreActor("tenant-B", "USER-B");

    @TempDir
    Path tmp;

    private EnterpriseRepository repo;
    private ObjectMapper mapper;
    private EnterpriseBackupServiceImpl backup;
    private SubjectRepository subjectRepo;
    private IEnterpriseService enterpriseService;
    private ILogoStorageService logos;
    private PlatformTransactionManager tm;
    private ZipBackupFormat format;
    private EnterpriseSnapshotSupport support;
    private EnterpriseBackupCoordinatorImpl coordinator;

    @BeforeEach
    void setUp() {
        TenantContext.setTenantId("tenant-A");
        repo = mock(EnterpriseRepository.class);
        subjectRepo = mock(SubjectRepository.class);
        enterpriseService = mock(IEnterpriseService.class);
        logos = mock(ILogoStorageService.class);
        tm = mock(PlatformTransactionManager.class);
        when(repo.existsById(any())).thenReturn(true);
        mapper = new ObjectMapper().registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        format = new ZipBackupFormat(mapper, 500_000_000L, 1000, 5);
        support = new EnterpriseSnapshotSupport(subjectRepo);
        EnterpriseBackupParticipant participant = new EnterpriseBackupParticipant(repo, enterpriseService, logos,
                support, mock(co.unicauca.edu.co.contables.configuration.enterprise.domain.services.IPersonTypeService.class));
        coordinator = new EnterpriseBackupCoordinatorImpl(List.of(participant), format, repo,
                mock(EntityManager.class), tm, "test", tmp.toString());
        backup = new EnterpriseBackupServiceImpl(coordinator, format);
        ReflectionTestUtils.setField(backup, "backupDir", tmp.toString());
        ReflectionTestUtils.setField(backup, "maxSizeBytes", 500_000_000L);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    private static byte[] zipOf(String json) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream z = new ZipOutputStream(baos)) {
            z.putNextEntry(new ZipEntry("enterprise.json"));
            z.write(json.getBytes(StandardCharsets.UTF_8));
            z.closeEntry();
        }
        return baos.toByteArray();
    }

    /** ZIP v1 (solo enterprise.json) en disco. */
    private Path zipFile(String json) throws Exception {
        Path p = Files.createTempFile(tmp, "v1-", ".zip");
        Files.write(p, zipOf(json));
        return p;
    }

    private String enterpriseJsonOf(BackupReferenceDto ref) throws Exception {
        try (ZipFile zip = new ZipFile(backup.resolveBackupFile(ref.getBackupId()).toFile())) {
            ZipEntry entry = zip.getEntry(EnterpriseSnapshotSupport.ENTRY_V2);
            assertThat(entry).as("el ZIP v2 trae enterprise/enterprise.json").isNotNull();
            try (InputStream in = zip.getInputStream(entry)) {
                return new String(in.readAllBytes(), StandardCharsets.UTF_8);
            }
        }
    }

    @Test
    void elBackupNoExportaMaterias() throws Exception {
        UUID id = UUID.randomUUID();
        EnterpriseEntity e = EnterpriseEntity.builder().id(id).idUser("A").name("Emp").nit("1").build();
        when(repo.findById(id)).thenReturn(Optional.of(e));

        BackupReferenceDto ref = backup.backup(id);

        String json = enterpriseJsonOf(ref);
        assertThat(json).doesNotContain("subjects");
        BackupManifest m = backup.readManifest(ref.getBackupId());
        assertThat(m.getSubjects()).isNull();
        assertThat(m.getEnterprise().getName()).isEqualTo("Emp");
        // ZIP v2: ruta por tenant, nunca directo en app.backup.dir
        assertThat(backup.resolveBackupFile(ref.getBackupId()).getParent()).isNotEqualTo(tmp.toAbsolutePath());
    }

    @Test
    void leerUnZipViejoConSubjectsNoFalla() throws Exception {
        String json = "{\"version\":1,\"enterprise\":{\"name\":\"X\"},"
                + "\"subjects\":[{\"code\":\"M1\",\"name\":\"Mat\"}],\"campoNuevo\":1}";
        BackupManifest m = backup.readManifest(zipFile(json));
        assertThat(m.getEnterprise().getName()).isEqualTo("X");
        assertThat(m.getSubjects()).hasSize(1);
    }

    private EnterpriseRestoreServiceImpl restoreService() {
        return new EnterpriseRestoreServiceImpl(coordinator, format, repo, null, null, null, null, null, null,
                null, support);
    }

    @Test
    void restoreNuevoIgnoraMateriasYFijaIdUserDelUsuarioActual() throws Exception {
        UUID newId = UUID.randomUUID();
        when(enterpriseService.create(any(Enterprise.class))).thenReturn(Enterprise.builder().id(newId).build());

        String json = "{\"version\":1,\"enterprise\":{\"idUser\":\"USER-A\",\"name\":\"X\"},"
                + "\"subjects\":[{\"code\":\"M1\",\"name\":\"Mat\"}]}";

        UUID result = restoreService().restore(zipFile(json), RestoreOptions.newEnterprise(null, null, false),
                USER_B);

        assertThat(result).isEqualTo(newId);
        ArgumentCaptor<Enterprise> cap = ArgumentCaptor.forClass(Enterprise.class);
        verify(enterpriseService).create(cap.capture());
        assertThat(cap.getValue().getIdUser()).isEqualTo("USER-B");
        assertThat(cap.getValue().getName()).isEqualTo("X");
    }

    @Test
    void errorDeUniqueMuestraLaCausaRealSinTextoDeCiudad() throws Exception {
        when(enterpriseService.create(any(Enterprise.class))).thenThrow(new DataIntegrityViolationException("wrap",
                new SQLException("duplicate key value violates unique constraint \"uk_nit\"", "23505")));

        Path file = zipFile("{\"version\":1,\"enterprise\":{\"name\":\"X\"}}");

        assertThatThrownBy(() -> restoreService().restore(file, RestoreOptions.newEnterprise(null, null, false),
                USER_B))
                .isInstanceOf(RestoreFailedException.class)
                .hasMessageContaining("duplicate key value violates unique constraint")
                .hasMessageNotContaining("ciudad")
                // al usuario no le llega el texto técnico de la BD
                .satisfies(ex -> assertThat(((RestoreFailedException) ex).getUserMessage())
                        .contains("enterprise").doesNotContain("duplicate key"));
    }

    @Test
    void errorDeLlaveForaneaConservaElTextoDeUbicacionYLaCausa() {
        String msg = EnterpriseRestoreServiceImpl.buildIntegrityMessage(new DataIntegrityViolationException("wrap",
                new SQLException("violates foreign key constraint fk_city", "23503")));
        assertThat(msg).contains("ciudad/departamento/país").contains("fk_city");
    }

    private static SubjectEntity subjectEntity(String code) {
        SubjectEntity e = new SubjectEntity();
        e.setId(UUID.randomUUID());
        e.setCode(code);
        e.setName("Mat " + code);
        return e;
    }

    @Test
    void elBackupExportaSoloLosCodigosDeLasMateriasDeLaEmpresa() throws Exception {
        UUID id = UUID.randomUUID();
        EnterpriseEntity e = EnterpriseEntity.builder().id(id).name("Emp").nit("1")
                .subjects(List.of(subjectEntity("M2"), subjectEntity("M1"))).build();
        when(repo.findById(id)).thenReturn(Optional.of(e));

        BackupReferenceDto ref = backup.backup(id);

        BackupManifest m = backup.readManifest(ref.getBackupId());
        assertThat(m.getEnterprise().getSubjectCodes()).containsExactly("M1", "M2");
        assertThat(m.getSubjects()).isNull();
        String json = enterpriseJsonOf(ref);
        assertThat(json).contains("subjectCodes").doesNotContain("\"subjects\"").doesNotContain("Mat M1");
    }

    @Test
    void empresaSinMateriasExportaSubjectCodesVacio() throws Exception {
        UUID id = UUID.randomUUID();
        when(repo.findById(id)).thenReturn(Optional.of(EnterpriseEntity.builder().id(id).name("Emp").build()));
        BackupManifest m = backup.readManifest(backup.backup(id).getBackupId());
        assertThat(m.getEnterprise().getSubjectCodes()).isNotNull().isEmpty();
    }

    @Test
    void restoreNuevoPasaLosCodigosAlServicioYZipViejoSinCampoDejaSubjectsNull() throws Exception {
        when(enterpriseService.create(any(Enterprise.class)))
                .thenAnswer(inv -> Enterprise.builder().id(UUID.randomUUID()).build());

        restoreService().restore(
                zipFile("{\"version\":1,\"enterprise\":{\"name\":\"X\",\"subjectCodes\":[\"M1\",\"\",\"M2\"]}}"),
                RestoreOptions.newEnterprise(null, null, false), USER_B);
        // ZIP viejo: sin subjectCodes
        restoreService().restore(zipFile("{\"version\":1,\"enterprise\":{\"name\":\"Y\"}}"),
                RestoreOptions.newEnterprise(null, null, false), USER_B);

        ArgumentCaptor<Enterprise> cap = ArgumentCaptor.forClass(Enterprise.class);
        verify(enterpriseService, org.mockito.Mockito.times(2)).create(cap.capture());
        assertThat(cap.getAllValues().get(0).getSubjects()).extracting(Subject::getCode).containsExactly("M1", "M2");
        assertThat(cap.getAllValues().get(1).getSubjects()).isNull();
    }

    private EnterpriseRestoreServiceImpl inPlaceService() {
        return new EnterpriseRestoreServiceImpl(coordinator, format, repo, mock(ILocationService.class),
                mock(IPersonTypeService.class), mock(LocationDataMapper.class), mock(PersonTypeDataMapper.class),
                mock(TaxLiabilityDataMapper.class), mock(TaxPayerTypeDataMapper.class),
                mock(EnterpriseTypeDataMapper.class), support);
    }

    @Test
    @SuppressWarnings("unchecked")
    void restoreInPlaceEnlazaLasExistentesYOmiteLasFaltantes() throws Exception {
        UUID target = UUID.randomUUID();
        EnterpriseEntity current = EnterpriseEntity.builder().id(target).name("Old")
                .subjects(new java.util.ArrayList<>(List.of(subjectEntity("VIEJA")))).build();
        when(repo.findById(target)).thenReturn(Optional.of(current));
        SubjectEntity m1 = subjectEntity("M1");
        // En el tenant destino solo existe M1; M2 no
        when(subjectRepo.findByCodeIn(any(Collection.class))).thenReturn(List.of(m1));

        inPlaceService().restoreInPlace(
                zipFile("{\"version\":1,\"enterprise\":{\"name\":\"New\",\"subjectCodes\":[\"M1\",\"M2\"]}}"),
                target);

        assertThat(current.getName()).isEqualTo("New");
        assertThat(current.getSubjects()).containsExactly(m1);
        ArgumentCaptor<Collection<String>> codes = ArgumentCaptor.forClass(Collection.class);
        verify(subjectRepo).findByCodeIn(codes.capture());
        assertThat(codes.getValue()).containsExactly("M1", "M2");
    }

    @Test
    void restoreInPlaceZipViejoConservaMateriasYListaVaciaLasQuita() throws Exception {
        UUID target = UUID.randomUUID();
        SubjectEntity keep = subjectEntity("KEEP");
        EnterpriseEntity current = EnterpriseEntity.builder().id(target).name("Old")
                .subjects(new java.util.ArrayList<>(List.of(keep))).build();
        when(repo.findById(target)).thenReturn(Optional.of(current));

        // ZIP viejo: sin subjectCodes -> no se tocan
        inPlaceService().restoreInPlace(
                zipFile("{\"version\":1,\"enterprise\":{\"name\":\"A\"},\"subjects\":[]}"), target);
        assertThat(current.getSubjects()).containsExactly(keep);

        // subjectCodes vacio -> sin materias
        inPlaceService().restoreInPlace(
                zipFile("{\"version\":1,\"enterprise\":{\"name\":\"B\",\"subjectCodes\":[]}}"), target);
        assertThat(current.getSubjects()).isEmpty();
    }

    /* ---------------- Casos nuevos del diseño v2 ---------------- */

    @Test
    void restoreInPlaceConZipV2SeRechaza() throws Exception {
        UUID id = UUID.randomUUID();
        when(repo.findById(id)).thenReturn(Optional.of(EnterpriseEntity.builder().id(id).name("Emp").build()));
        Path v2 = backup.resolveBackupFile(backup.backup(id).getBackupId());

        assertThatThrownBy(() -> inPlaceService().restoreInPlace(v2, id))
                .isInstanceOf(InvalidRestoreRequestException.class)
                .hasMessageContaining("empresa nueva");
        assertThatThrownBy(() -> inPlaceService().validateInPlace(v2, id))
                .isInstanceOf(InvalidRestoreRequestException.class);
    }

    @Test
    void restoreNuevoConNitRepetidoYaNoDa409YCreaLaEmpresa() throws Exception {
        // El NIT no es único por regla de negocio: ya no se valida al importar.
        when(repo.existsByNit("900")).thenReturn(true);
        when(enterpriseService.create(any(Enterprise.class)))
                .thenReturn(Enterprise.builder().id(UUID.randomUUID()).build());

        restoreService().restore(zipFile("{\"version\":1,\"enterprise\":{\"name\":\"X\",\"nit\":\"900\"}}"),
                RestoreOptions.newEnterprise(null, null, false), USER_B);

        ArgumentCaptor<Enterprise> cap = ArgumentCaptor.forClass(Enterprise.class);
        verify(enterpriseService).create(cap.capture());
        assertThat(cap.getValue().getNit()).isEqualTo("900");
    }

    @Test
    void restoreNuevoConNitYNombreDestinoEvitaEl409() throws Exception {
        when(repo.existsByNit("900")).thenReturn(true);
        when(enterpriseService.create(any(Enterprise.class)))
                .thenReturn(Enterprise.builder().id(UUID.randomUUID()).build());

        restoreService().restore(zipFile("{\"version\":1,\"enterprise\":{\"name\":\"X\",\"nit\":\"900\"}}"),
                RestoreOptions.newEnterprise("Nueva", "901", false), USER_B);

        ArgumentCaptor<Enterprise> cap = ArgumentCaptor.forClass(Enterprise.class);
        verify(enterpriseService).create(cap.capture());
        assertThat(cap.getValue().getNit()).isEqualTo("901");
        assertThat(cap.getValue().getName()).isEqualTo("Nueva");
    }

    @Test
    void duplicarPermiteNitRepetidoNombraCopiaYBorraElTemporal() throws Exception {
        UUID id = UUID.randomUUID();
        when(repo.findById(id)).thenReturn(Optional.of(EnterpriseEntity.builder().id(id).name("Emp").nit("900")
                .inventoryConfigurationType(
                        co.unicauca.edu.co.contables.configuration.enterprise.domain.enums.InventoryConfigurationTypeEnum.WEIGHTED_AVERAGE)
                .build()));
        when(repo.existsByNit("900")).thenReturn(true);
        UUID newId = UUID.randomUUID();
        when(enterpriseService.create(any(Enterprise.class))).thenReturn(Enterprise.builder().id(newId).build());

        UUID result = coordinator.duplicate(id, null, USER_B, "user-b", BackupProgressListener.NONE)
                .enterpriseId();

        assertThat(result).isEqualTo(newId);
        ArgumentCaptor<Enterprise> cap = ArgumentCaptor.forClass(Enterprise.class);
        verify(enterpriseService).create(cap.capture());
        assertThat(cap.getValue().getName()).isEqualTo("Emp (copia)");
        assertThat(cap.getValue().getNit()).isEqualTo("900");
        assertThat(cap.getValue().getIdUser()).isEqualTo("USER-B");
        assertThat(cap.getValue().getInventoryConfigurationType()).isNotNull();
        try (Stream<Path> left = Files.list(tmp.resolve("tmp"))) {
            assertThat(left).as("el ZIP temporal de duplicar se borra").isEmpty();
        }
    }

    @Test
    void elLogoViajaComoAssetYSeRestauraConClaveNueva() throws Exception {
        byte[] png = { (byte) 0x89, 'P', 'N', 'G', 13, 10, 26, 10, 1, 2, 3 };
        UUID id = UUID.randomUUID();
        when(repo.findById(id)).thenReturn(Optional.of(EnterpriseEntity.builder().id(id).name("Emp")
                .logo("/api/enterprises/logos/old-key").build()));
        when(logos.open("old-key")).thenAnswer(inv -> Optional.of(
                new ILogoStorageService.LogoContent(new ByteArrayInputStream(png), "image/png", png.length)));
        AtomicReference<byte[]> restored = new AtomicReference<>();
        when(logos.saveAfterCommit(any(InputStream.class), eq("image/png"))).thenAnswer(inv -> {
            restored.set(((InputStream) inv.getArgument(0)).readAllBytes());
            return "new-key";
        });
        when(enterpriseService.create(any(Enterprise.class)))
                .thenReturn(Enterprise.builder().id(UUID.randomUUID()).build());

        Path zip = backup.resolveBackupFile(backup.backup(id).getBackupId());
        try (ZipFile z = new ZipFile(zip.toFile())) {
            assertThat(z.getEntry(EnterpriseSnapshotSupport.LOGO_ASSET)).isNotNull();
        }
        restoreService().restore(zip, RestoreOptions.newEnterprise(null, null, false), USER_B);

        ArgumentCaptor<Enterprise> cap = ArgumentCaptor.forClass(Enterprise.class);
        verify(enterpriseService).create(cap.capture());
        assertThat(cap.getValue().getLogo()).isEqualTo("/api/enterprises/logos/new-key");
        assertThat(restored.get()).isEqualTo(png);
    }

    @Test
    void zipV1ConLogoQueNoExisteEnEsteEntornoSeRestauraSinLogo() throws Exception {
        when(enterpriseService.create(any(Enterprise.class)))
                .thenReturn(Enterprise.builder().id(UUID.randomUUID()).build());

        restoreService().restore(
                zipFile("{\"version\":1,\"enterprise\":{\"name\":\"X\",\"logo\":\"/api/enterprises/logos/nope\"}}"),
                RestoreOptions.newEnterprise(null, null, false), USER_B);

        ArgumentCaptor<Enterprise> cap = ArgumentCaptor.forClass(Enterprise.class);
        verify(enterpriseService).create(cap.capture());
        assertThat(cap.getValue().getLogo()).isNull();
        verify(logos, never()).saveAfterCommit(any(), any());
    }
}
