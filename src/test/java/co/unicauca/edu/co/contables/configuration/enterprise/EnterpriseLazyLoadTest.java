package co.unicauca.edu.co.contables.configuration.enterprise;

import co.unicauca.edu.co.contables.configuration.enterprise.application.ports.input.IEnterpriseSearchManagerPort;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.models.Enterprise;
import co.unicauca.edu.co.contables.configuration.enterprise.infraestructure.adapters.output.jpaAdapter.entity.*;
import co.unicauca.edu.co.contables.configuration.enterprise.infraestructure.adapters.output.jpaAdapter.repository.IEnterpriseRepository;
import co.unicauca.edu.co.contables.configuration.enterprise.infraestructure.adapters.output.jpaAdapter.repository.ITaxLiabilityRepository;
import co.unicauca.edu.co.contables.configuration.enterprise.infraestructure.adapters.output.jpaAdapter.repository.ITaxPayerTypeRepository;
import co.unicauca.edu.co.contables.configuration.enterprise.infraestructure.adapters.output.jpaAdapter.repository.IPersonTypeRepository;
import co.unicauca.edu.co.contables.configuration.enterprise.infraestructure.adapters.output.jpaAdapter.repository.ILocationRepository;
import co.unicauca.edu.co.contables.configuration.enterprise.infraestructure.adapters.output.jpaAdapter.repository.IDepartmentAddressRepository;
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
 * The bug: spring.jpa.open-in-view=false and no @Transactional on getEnterpriseById
 * causes the EntityManager to close before the MapStruct mapper accesses lazy collections.
 * 
 * This test must run the ACT (calling getEnterpriseById) OUTSIDE any test-managed transaction,
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
    private IEnterpriseSearchManagerPort enterpriseSearchManagerPort;

    @Autowired
    private IEnterpriseRepository enterpriseRepository;

    @Autowired
    private ITaxLiabilityRepository taxLiabilityRepository;

    @Autowired
    private ITaxPayerTypeRepository taxPayerTypeRepository;

    @Autowired
    private IPersonTypeRepository personTypeRepository;

    @Autowired
    private ILocationRepository locationRepository;

    @Autowired
    private IDepartmentAddressRepository departmentRepository;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Test
    void should_load_enterprise_with_lazy_tax_liabilities_without_lazy_initialization_exception() {
        // Set tenant context for this test
        TenantContext.setTenantId("test-tenant");
        
        try {
            // ARRANGE: Persist a complete Enterprise graph with lazy taxLiabilities
            // This setup runs inside a transaction to ensure data is committed
            UUID enterpriseId = transactionTemplate.execute(status -> {
                // Create required related entities using repositories where available
                TaxLiabilityEntity taxLiability = TaxLiabilityEntity.builder()
                        .name("IVA")
                        .build();
                taxLiability = taxLiabilityRepository.save(taxLiability);

                TaxPayerTypeEntity taxPayerType = TaxPayerTypeEntity.builder()
                        .name("Persona Jurídica")
                        .build();
                taxPayerType = taxPayerTypeRepository.save(taxPayerType);

                // EnterpriseTypeEntity and CityEntity don't have repositories - use EntityManager
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

            // ACT: Call the search port OUTSIDE any transaction
            // This is the critical part - if @Transactional is missing on the service,
            // this will throw LazyInitializationException when the mapper accesses taxLiabilities
            Enterprise enterprise = enterpriseSearchManagerPort.getEnterpriseById(enterpriseId);

            // ASSERT: Verify the enterprise and its lazy collections are loaded correctly
            assertNotNull(enterprise, "Enterprise should be found");
            assertEquals(enterpriseId, enterprise.getId());
            assertNotNull(enterprise.getTaxLiabilities(), "Tax liabilities should be loaded (not null)");
            assertTrue(enterprise.getTaxLiabilities().size() >= 1, "Should have at least one tax liability");
            assertEquals("IVA", enterprise.getTaxLiabilities().get(0).getName());
        } finally {
            TenantContext.clear();
        }
    }
}