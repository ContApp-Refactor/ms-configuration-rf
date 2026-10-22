package co.unicauca.edu.co.contables.configuration.enterprise;

import co.unicauca.edu.co.contables.configuration.enterprise.dataAccess.entity.*;
import co.unicauca.edu.co.contables.configuration.enterprise.dataAccess.repository.DepartmentRepository;
import co.unicauca.edu.co.contables.configuration.enterprise.dataAccess.repository.EnterpriseRepository;
import co.unicauca.edu.co.contables.configuration.enterprise.dataAccess.repository.LocationRepository;
import co.unicauca.edu.co.contables.configuration.enterprise.dataAccess.repository.PersonTypeRepository;
import co.unicauca.edu.co.contables.configuration.enterprise.dataAccess.repository.TaxLiabilityRepository;
import co.unicauca.edu.co.contables.configuration.enterprise.dataAccess.repository.TaxPayerTypeRepository;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.models.Department;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.models.Enterprise;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.services.IAddressService;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.services.IEnterpriseService;
import co.unicauca.edu.co.contables.configuration.thirds.infrastructure.config.GeographyDataInitializer;
import co.unicauca.edu.co.contables.commons.multitenancy.utils.TenantContext;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Regression test for LazyInitializationException when loading Enterprise with taxLiabilities.
 *
 * The bug: spring.jpa.open-in-view=false and no @Transactional on findById
 * causes the EntityManager to close before the mapper accesses lazy collections.
 *
 * This test must run the ACT (calling findById) OUTSIDE any test-managed transaction,
 * otherwise the test would pass for the wrong reason (the transaction keeps the session alive).
 *
 * GeographyDataInitializer is mocked out and spring.sql.init is disabled because the seed
 * scripts are PostgreSQL-only (they use "ON CONFLICT ... DO NOTHING", which H2 rejects).
 *
 * TestMultiTenancyConfig provides a fixed tenant ID for consistent schema generation with @TenantId.
 * enterprise-test-schema.sql creates the required tables manually to work around H2 schema generation issues with @TenantId.
 */
@SpringBootTest(properties = {
        "spring.sql.init.mode=never",
        "spring.jpa.defer-datasource-initialization=false"
})
@ActiveProfiles("test")
@Import(TestMultiTenancyConfig.class)
@Sql(scripts = "/enterprise-test-schema.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class EnterpriseLazyLoadTest {

    @MockitoBean
    private GeographyDataInitializer geographyDataInitializer;

    @Autowired
    private IEnterpriseService enterpriseService;

    @Autowired
    private IAddressService addressService;

    @Autowired
    private EnterpriseRepository enterpriseRepository;

    @Autowired
    private TaxLiabilityRepository taxLiabilityRepository;

    @Autowired
    private TaxPayerTypeRepository taxPayerTypeRepository;

    @Autowired
    private PersonTypeRepository personTypeRepository;

    @Autowired
    private LocationRepository locationRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Test
    void should_load_enterprise_with_lazy_tax_liabilities_without_lazy_initialization_exception() {
        TenantContext.setTenantId("test-tenant");

        try {
            UUID enterpriseId = transactionTemplate.execute(status -> {
                TaxLiabilityEntity taxLiability = TaxLiabilityEntity.builder()
                        .name("IVA")
                        .build();
                taxLiability = taxLiabilityRepository.save(taxLiability);

                TaxPayerTypeEntity taxPayerType = TaxPayerTypeEntity.builder()
                        .name("Persona Jurídica")
                        .build();
                taxPayerType = taxPayerTypeRepository.save(taxPayerType);

                EnterpriseTypeEntity enterpriseType = EnterpriseTypeEntity.builder()
                        .name("Matriz")
                        .build();
                entityManager.persist(enterpriseType);
                entityManager.flush();

                PersonTypeEntity personType = PersonTypeEntity.builder()
                        .type("JURIDICA")
                        .name("Empresa")
                        .surname("Test")
                        .bussinessName("Empresa Test SAS")
                        .build();
                personType = personTypeRepository.save(personType);

                CountryEntity country = CountryEntity.builder()
                        .name("Colombia")
                        .build();
                entityManager.persist(country);
                entityManager.flush();

                DepartmentEntity department = DepartmentEntity.builder()
                        .name("Cauca")
                        .country(country)
                        .build();
                department = departmentRepository.save(department);

                CityEntity city = CityEntity.builder()
                        .name("Popayán")
                        .department(department)
                        .build();
                entityManager.persist(city);
                entityManager.flush();

                LocationEntity location = LocationEntity.builder()
                        .address("Calle 1 #2-3")
                        .city(city)
                        .country(country)
                        .department(department)
                        .build();
                location = locationRepository.save(location);

                EnterpriseEntity enterprise = EnterpriseEntity.builder()
                        .name("Empresa Test")
                        .nit("900123456")
                        .DV("7")
                        .phone("3001234567")
                        .branch("Tecnología")
                        .email("test@empresa.com")
                        .logo("logo.png")
                        .state(co.unicauca.edu.co.contables.configuration.enterprise.domain.enums.StateEnum.ACTIVE)
                        .inventoryConfigurationType(co.unicauca.edu.co.contables.configuration.enterprise.domain.enums.InventoryConfigurationTypeEnum.PEPS)
                        .mainActivity(6201L)
                        .secondaryActivity(6202L)
                        .taxLiabilities(List.of(taxLiability))
                        .taxPayerType(taxPayerType)
                        .enterpriseType(enterpriseType)
                        .personType(personType)
                        .location(location)
                        .tenantId("test-tenant")
                        .build();
                enterprise = enterpriseRepository.save(enterprise);

                return enterprise.getId();
            });

            Enterprise enterprise = enterpriseService.findById(enterpriseId);

            assertNotNull(enterprise, "Enterprise should be found");
            assertEquals(enterpriseId, enterprise.getId());
            assertNotNull(enterprise.getTaxLiabilities(), "Tax liabilities should be loaded (not null)");
            assertTrue(enterprise.getTaxLiabilities().size() >= 1, "Should have at least one tax liability");
            assertEquals("IVA", enterprise.getTaxLiabilities().get(0).getName());
        } finally {
            TenantContext.clear();
        }
    }

    /**
     * Regression test for LazyInitializationException when loading the cities of a department.
     */
    @Test
    void should_load_department_cities_without_lazy_initialization_exception() {
        TenantContext.setTenantId("test-tenant");

        try {
            Long departmentId = transactionTemplate.execute(status -> {
                CountryEntity country = CountryEntity.builder()
                        .name("Colombia")
                        .build();
                entityManager.persist(country);
                entityManager.flush();

                DepartmentEntity department = DepartmentEntity.builder()
                        .name("Cauca")
                        .country(country)
                        .build();
                department = departmentRepository.save(department);

                entityManager.persist(CityEntity.builder()
                        .name("Popayán")
                        .department(department)
                        .build());
                entityManager.persist(CityEntity.builder()
                        .name("Santander de Quilichao")
                        .department(department)
                        .build());
                entityManager.flush();

                return department.getId();
            });

            Department department = addressService.getAllCities(departmentId);

            assertNotNull(department, "Department should be found");
            assertNotNull(department.getCities(), "Cities should be loaded (not null)");
            assertEquals(2, department.getCities().size(), "Both cities should be loaded");
        } finally {
            TenantContext.clear();
        }
    }
}
