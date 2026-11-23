package co.unicauca.edu.co.contables.configuration.enterprise;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.support.TransactionTemplate;

import co.unicauca.edu.co.contables.commons.multitenancy.utils.TenantContext;
import co.unicauca.edu.co.contables.configuration.enterprise.dataAccess.entity.CityEntity;
import co.unicauca.edu.co.contables.configuration.enterprise.dataAccess.entity.CountryEntity;
import co.unicauca.edu.co.contables.configuration.enterprise.dataAccess.entity.DepartmentEntity;
import co.unicauca.edu.co.contables.configuration.enterprise.dataAccess.entity.EnterpriseTypeEntity;
import co.unicauca.edu.co.contables.configuration.enterprise.dataAccess.entity.SubjectEntity;
import co.unicauca.edu.co.contables.configuration.enterprise.dataAccess.entity.TaxPayerTypeEntity;
import co.unicauca.edu.co.contables.configuration.enterprise.dataAccess.repository.DepartmentRepository;
import co.unicauca.edu.co.contables.configuration.enterprise.dataAccess.repository.SubjectRepository;
import co.unicauca.edu.co.contables.configuration.enterprise.dataAccess.repository.TaxPayerTypeRepository;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.enums.StateEnum;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.mapper.EnterpriseDomainMapper;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.models.City;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.models.Country;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.models.Department;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.models.Enterprise;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.models.EnterpriseType;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.models.Location;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.models.PersonType;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.models.Subject;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.models.TaxPayerType;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.services.IEnterpriseService;
import co.unicauca.edu.co.contables.configuration.enterprise.presentation.DTO.request.EnterpriseUpdateReq;
import co.unicauca.edu.co.contables.configuration.enterprise.presentation.DTO.response.EnterpriseRes;
import co.unicauca.edu.co.contables.configuration.thirds.infrastructure.config.GeographyDataInitializer;
import jakarta.persistence.EntityManager;

/**
 * Materias de la empresa: crear/actualizar/leer contra H2 con {@code @TenantId}. Las lecturas se
 * hacen FUERA de una transaccion de test (open-in-view=false) para detectar LazyInitializationException.
 */
@SpringBootTest(properties = {
        "spring.sql.init.mode=never",
        "spring.jpa.defer-datasource-initialization=false"
})
@ActiveProfiles("test")
@Import(TestMultiTenancyConfig.class)
@Sql(scripts = "/enterprise-test-schema.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class EnterpriseSubjectsTest {

    private static final String TENANT = "test-tenant";

    @MockitoBean
    private GeographyDataInitializer geographyDataInitializer;

    @Autowired
    private IEnterpriseService enterpriseService;
    @Autowired
    private SubjectRepository subjectRepository;
    @Autowired
    private TaxPayerTypeRepository taxPayerTypeRepository;
    @Autowired
    private DepartmentRepository departmentRepository;
    @Autowired
    private EntityManager entityManager;
    @Autowired
    private TransactionTemplate tx;
    @Autowired
    private JdbcTemplate jdbc;
    @Autowired
    private EnterpriseDomainMapper domainMapper;

    private Long taxPayerTypeId;
    private Long enterpriseTypeId;
    private Long cityId;
    private Long departmentId;
    private Long countryId;

    private void seedCatalogs() {
        tx.executeWithoutResult(s -> {
            TaxPayerTypeEntity tp = taxPayerTypeRepository.save(TaxPayerTypeEntity.builder().name("PJ").build());
            taxPayerTypeId = tp.getId();
            EnterpriseTypeEntity et = EnterpriseTypeEntity.builder().name("Matriz").build();
            entityManager.persist(et);
            enterpriseTypeId = et.getId();
            CountryEntity country = CountryEntity.builder().name("Colombia").build();
            entityManager.persist(country);
            countryId = country.getId();
            DepartmentEntity dep = departmentRepository
                    .save(DepartmentEntity.builder().name("Cauca").country(country).build());
            departmentId = dep.getId();
            CityEntity city = CityEntity.builder().name("Popayan").department(dep).build();
            entityManager.persist(city);
            cityId = city.getId();
            entityManager.flush();
        });
    }

    private SubjectEntity newSubject(String prefix) {
        SubjectEntity s = new SubjectEntity();
        s.setCode(prefix + "-" + UUID.randomUUID().toString().substring(0, 8));
        s.setName("Materia " + prefix);
        return subjectRepository.save(s);
    }

    private Enterprise baseEnterprise(List<Subject> subjects) {
        return Enterprise.builder()
                .idUser("u1").name("Emp").nit("900").DV("1").phone("300").email("a@b.co")
                .state(StateEnum.ACTIVE)
                .taxLiabilities(new ArrayList<>())
                .taxPayerType(TaxPayerType.builder().id(taxPayerTypeId).build())
                .enterpriseType(EnterpriseType.builder().id(enterpriseTypeId).build())
                .personType(PersonType.builder().type("J").name("n").surname("s").bussinessName("b").build())
                .location(Location.builder().address("Calle 1")
                        .city(City.builder().id(cityId).build())
                        .country(Country.builder().id(countryId).build())
                        .department(Department.builder().id(departmentId).build()).build())
                .subjects(subjects)
                .build();
    }

    private static List<String> codes(Enterprise e) {
        return e.getSubjects().stream().map(Subject::getCode).sorted().toList();
    }

    @Test
    void crearConMateriasIgnoraInexistentesYDeOtroTenantYGetLasDevuelve() {
        TenantContext.setTenantId(TENANT);
        try {
            seedCatalogs();
            SubjectEntity a = tx.execute(s -> newSubject("A"));
            SubjectEntity b = tx.execute(s -> newSubject("B"));
            // materia de OTRO tenant, insertada por SQL directo
            UUID foreign = UUID.randomUUID();
            jdbc.update("INSERT INTO subjects(id, code, name, tenant_id) VALUES (?,?,?,?)",
                    foreign, "FOREIGN-" + foreign, "Ajena", "otro-tenant");

            Enterprise created = enterpriseService.create(baseEnterprise(List.of(
                    Subject.builder().id(a.getId()).build(),
                    Subject.builder().id(b.getId()).build(),
                    Subject.builder().id(b.getId()).build(), // duplicado
                    Subject.builder().id(UUID.randomUUID()).build(), // inexistente
                    Subject.builder().id(foreign).build()))); // otro tenant

            // lectura fuera de transaccion: sin LazyInitializationException
            Enterprise read = enterpriseService.findById(created.getId());
            assertThat(read.getSubjects()).hasSize(2);
            assertThat(codes(read)).containsExactlyInAnyOrder(a.getCode(), b.getCode());

            EnterpriseRes res = domainMapper.toRes(read);
            assertThat(res.getSubjects()).extracting("id").containsExactlyInAnyOrder(a.getId(), b.getId());
            assertThat(res.getSubjects()).allSatisfy(x -> {
                assertThat(x.getCode()).isNotBlank();
                assertThat(x.getName()).isNotBlank();
            });
        } finally {
            TenantContext.clear();
        }
    }

    @Test
    void crearSinMateriasDevuelveListaVacia() {
        TenantContext.setTenantId(TENANT);
        try {
            seedCatalogs();
            Enterprise created = enterpriseService.create(baseEnterprise(null));
            Enterprise read = enterpriseService.findById(created.getId());
            assertThat(read.getSubjects()).isEmpty();
            assertThat(domainMapper.toRes(read).getSubjects()).isEmpty();
        } finally {
            TenantContext.clear();
        }
    }

    @Test
    void updateReemplazaVaciaYNullNoToca() {
        TenantContext.setTenantId(TENANT);
        try {
            seedCatalogs();
            SubjectEntity a = tx.execute(s -> newSubject("A"));
            SubjectEntity b = tx.execute(s -> newSubject("B"));
            SubjectEntity c = tx.execute(s -> newSubject("C"));
            Enterprise created = enterpriseService.create(baseEnterprise(List.of(
                    Subject.builder().id(a.getId()).build(), Subject.builder().id(b.getId()).build())));
            UUID id = created.getId();

            // null = no tocar
            enterpriseService.update(id, baseEnterprise(null));
            assertThat(codes(enterpriseService.findById(id))).containsExactlyInAnyOrder(a.getCode(), b.getCode());

            // reemplazar el conjunto (b se va, c entra) + id inexistente ignorado
            enterpriseService.update(id, baseEnterprise(List.of(
                    Subject.builder().id(a.getId()).build(), Subject.builder().id(c.getId()).build(),
                    Subject.builder().id(UUID.randomUUID()).build())));
            assertThat(codes(enterpriseService.findById(id))).containsExactlyInAnyOrder(a.getCode(), c.getCode());

            // lista vacia = sin materias
            enterpriseService.update(id, baseEnterprise(List.of()));
            assertThat(enterpriseService.findById(id).getSubjects()).isEmpty();
        } finally {
            TenantContext.clear();
        }
    }

    @Test
    void crearConCodigoEnlazaExistentesYOmiteFaltantes() {
        TenantContext.setTenantId(TENANT);
        try {
            seedCatalogs();
            SubjectEntity a = tx.execute(s -> newSubject("A"));
            Enterprise created = enterpriseService.create(baseEnterprise(List.of(
                    Subject.builder().code(a.getCode()).build(),
                    Subject.builder().code("NO-EXISTE").build())));
            assertThat(codes(enterpriseService.findById(created.getId()))).containsExactly(a.getCode());
        } finally {
            TenantContext.clear();
        }
    }

    @Test
    void requestUpdateDistingueNullDeListaVacia() {
        EnterpriseUpdateReq nullReq = new EnterpriseUpdateReq();
        EnterpriseUpdateReq emptyReq = new EnterpriseUpdateReq();
        emptyReq.setSubjects(List.of());
        UUID id = UUID.randomUUID();
        EnterpriseUpdateReq withIds = new EnterpriseUpdateReq();
        withIds.setSubjects(List.of(id, id));

        assertThat(domainMapper.toDomain(nullReq).getSubjects()).isNull();
        assertThat(domainMapper.toDomain(emptyReq).getSubjects()).isEmpty();
        assertThat(domainMapper.toDomain(withIds).getSubjects()).extracting(Subject::getId).containsExactly(id);
    }
}
