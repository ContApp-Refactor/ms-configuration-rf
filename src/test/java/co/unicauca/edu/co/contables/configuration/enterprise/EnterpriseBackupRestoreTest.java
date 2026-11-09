package co.unicauca.edu.co.contables.configuration.enterprise;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.Optional;
import java.util.Collection;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.util.ReflectionTestUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import co.unicauca.edu.co.contables.commons.security.IJwtUtils;
import co.unicauca.edu.co.contables.configuration.enterprise.dataAccess.entity.EnterpriseEntity;
import co.unicauca.edu.co.contables.configuration.enterprise.dataAccess.entity.SubjectEntity;
import co.unicauca.edu.co.contables.configuration.enterprise.dataAccess.repository.EnterpriseRepository;
import co.unicauca.edu.co.contables.configuration.enterprise.dataAccess.repository.SubjectRepository;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.models.Subject;
import co.unicauca.edu.co.contables.configuration.enterprise.dataAccess.mapper.EnterpriseTypeDataMapper;
import co.unicauca.edu.co.contables.configuration.enterprise.dataAccess.mapper.LocationDataMapper;
import co.unicauca.edu.co.contables.configuration.enterprise.dataAccess.mapper.PersonTypeDataMapper;
import co.unicauca.edu.co.contables.configuration.enterprise.dataAccess.mapper.TaxLiabilityDataMapper;
import co.unicauca.edu.co.contables.configuration.enterprise.dataAccess.mapper.TaxPayerTypeDataMapper;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.services.ILocationService;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.services.IPersonTypeService;
import java.util.List;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.dto.BackupReferenceDto;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.models.BackupManifest;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.models.Enterprise;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.services.EnterpriseBackupServiceImpl;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.services.EnterpriseRestoreServiceImpl;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.services.IEnterpriseService;
import co.unicauca.edu.co.contables.configuration.enterprise.presentation.exception.RestoreFailedException;

/** Backup sin materias, restore tolerante a ZIP viejo, idUser actual y mensajes de error. */
class EnterpriseBackupRestoreTest {

    @TempDir
    Path tmp;

    private EnterpriseRepository repo;
    private ObjectMapper mapper;
    private EnterpriseBackupServiceImpl backup;
    private SubjectRepository subjectRepo;

    @BeforeEach
    void setUp() {
        repo = mock(EnterpriseRepository.class);
        subjectRepo = mock(SubjectRepository.class);
        mapper = new ObjectMapper().registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        backup = new EnterpriseBackupServiceImpl(repo, mapper);
        ReflectionTestUtils.setField(backup, "backupDir", tmp.toString());
        ReflectionTestUtils.setField(backup, "maxSizeBytes", 500_000_000L);
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

    @Test
    void elBackupNoExportaMaterias() throws Exception {
        UUID id = UUID.randomUUID();
        EnterpriseEntity e = EnterpriseEntity.builder().id(id).idUser("A").name("Emp").nit("1").build();
        when(repo.findById(id)).thenReturn(Optional.of(e));

        BackupReferenceDto ref = backup.backup(id);

        Path zip = tmp.resolve(ref.getBackupId() + ".zip");
        String json;
        try (ZipInputStream in = new ZipInputStream(Files.newInputStream(zip))) {
            in.getNextEntry();
            json = new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
        assertThat(json).doesNotContain("subjects");
        BackupManifest m = backup.readManifest(ref.getBackupId());
        assertThat(m.getSubjects()).isNull();
        assertThat(m.getEnterprise().getName()).isEqualTo("Emp");
    }

    @Test
    void leerUnZipViejoConSubjectsNoFalla() throws Exception {
        String json = "{\"version\":1,\"enterprise\":{\"name\":\"X\"},"
                + "\"subjects\":[{\"code\":\"M1\",\"name\":\"Mat\"}],\"campoNuevo\":1}";
        BackupManifest m = backup.readManifest(zipOf(json));
        assertThat(m.getEnterprise().getName()).isEqualTo("X");
        assertThat(m.getSubjects()).hasSize(1);
    }

    private EnterpriseRestoreServiceImpl restoreService(IEnterpriseService enterpriseService, IJwtUtils jwt) {
        EnterpriseRestoreServiceImpl svc = new EnterpriseRestoreServiceImpl(backup, enterpriseService, repo,
                null, null, jwt, null, null, null, null, null, subjectRepo);
        ReflectionTestUtils.setField(svc, "maxUploadSizeBytes", 500_000_000L);
        return svc;
    }

    @Test
    void restoreNuevoIgnoraMateriasYFijaIdUserDelUsuarioActual() throws Exception {
        IEnterpriseService es = mock(IEnterpriseService.class);
        IJwtUtils jwt = mock(IJwtUtils.class);
        when(jwt.getId()).thenReturn("USER-B");
        UUID newId = UUID.randomUUID();
        when(es.create(any(Enterprise.class))).thenReturn(Enterprise.builder().id(newId).build());

        String json = "{\"version\":1,\"enterprise\":{\"idUser\":\"USER-A\",\"name\":\"X\"},"
                + "\"subjects\":[{\"code\":\"M1\",\"name\":\"Mat\"}]}";
        org.springframework.mock.web.MockMultipartFile file = new org.springframework.mock.web.MockMultipartFile(
                "file", "b.zip", "application/zip", zipOf(json));

        UUID result = restoreService(es, jwt).restoreFromFile(file, false, null);

        assertThat(result).isEqualTo(newId);
        ArgumentCaptor<Enterprise> cap = ArgumentCaptor.forClass(Enterprise.class);
        verify(es).create(cap.capture());
        assertThat(cap.getValue().getIdUser()).isEqualTo("USER-B");
        assertThat(cap.getValue().getName()).isEqualTo("X");
    }

    @Test
    void errorDeUniqueMuestraLaCausaRealSinTextoDeCiudad() throws Exception {
        IEnterpriseService es = mock(IEnterpriseService.class);
        IJwtUtils jwt = mock(IJwtUtils.class);
        when(es.create(any(Enterprise.class))).thenThrow(new DataIntegrityViolationException("wrap",
                new SQLException("duplicate key value violates unique constraint \"uk_nit\"", "23505")));

        org.springframework.mock.web.MockMultipartFile file = new org.springframework.mock.web.MockMultipartFile(
                "file", "b.zip", "application/zip", zipOf("{\"version\":1,\"enterprise\":{\"name\":\"X\"}}"));

        org.assertj.core.api.Assertions.assertThatThrownBy(
                () -> restoreService(es, jwt).restoreFromFile(file, false, null))
                .isInstanceOf(RestoreFailedException.class)
                .hasMessageContaining("duplicate key value violates unique constraint")
                .hasMessageNotContaining("ciudad");
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

    private static org.springframework.mock.web.MockMultipartFile zipFile(String json) throws Exception {
        return new org.springframework.mock.web.MockMultipartFile("file", "b.zip", "application/zip", zipOf(json));
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
        String json;
        try (ZipInputStream in = new ZipInputStream(Files.newInputStream(tmp.resolve(ref.getBackupId() + ".zip")))) {
            in.getNextEntry();
            json = new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
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
        IEnterpriseService es = mock(IEnterpriseService.class);
        IJwtUtils jwt = mock(IJwtUtils.class);
        when(es.create(any(Enterprise.class))).thenReturn(Enterprise.builder().id(UUID.randomUUID()).build());

        restoreService(es, jwt).restoreFromFile(
                zipFile("{\"version\":1,\"enterprise\":{\"name\":\"X\",\"subjectCodes\":[\"M1\",\"\",\"M2\"]}}"),
                false, null);
        // ZIP viejo: sin subjectCodes
        restoreService(es, jwt).restoreFromFile(zipFile("{\"version\":1,\"enterprise\":{\"name\":\"Y\"}}"), false,
                null);

        ArgumentCaptor<Enterprise> cap = ArgumentCaptor.forClass(Enterprise.class);
        verify(es, org.mockito.Mockito.times(2)).create(cap.capture());
        assertThat(cap.getAllValues().get(0).getSubjects()).extracting(Subject::getCode).containsExactly("M1", "M2");
        assertThat(cap.getAllValues().get(1).getSubjects()).isNull();
    }

    private EnterpriseRestoreServiceImpl inPlaceService() {
        EnterpriseRestoreServiceImpl svc = new EnterpriseRestoreServiceImpl(backup, mock(IEnterpriseService.class),
                repo, mock(ILocationService.class), mock(IPersonTypeService.class), mock(IJwtUtils.class),
                mock(LocationDataMapper.class), mock(PersonTypeDataMapper.class),
                mock(TaxLiabilityDataMapper.class), mock(TaxPayerTypeDataMapper.class),
                mock(EnterpriseTypeDataMapper.class), subjectRepo);
        ReflectionTestUtils.setField(svc, "maxUploadSizeBytes", 500_000_000L);
        return svc;
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

        inPlaceService().restoreFromFile(
                zipFile("{\"version\":1,\"enterprise\":{\"name\":\"New\",\"subjectCodes\":[\"M1\",\"M2\"]}}"),
                true, target);

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
        inPlaceService().restoreFromFile(
                zipFile("{\"version\":1,\"enterprise\":{\"name\":\"A\"},\"subjects\":[]}"), true, target);
        assertThat(current.getSubjects()).containsExactly(keep);

        // subjectCodes vacio -> sin materias
        inPlaceService().restoreFromFile(
                zipFile("{\"version\":1,\"enterprise\":{\"name\":\"B\",\"subjectCodes\":[]}}"), true, target);
        assertThat(current.getSubjects()).isEmpty();
    }
}
