package co.unicauca.edu.co.contables.configuration.enterprise.domain.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import co.unicauca.edu.co.contables.configuration.enterprise.dataAccess.entity.EnterpriseEntity;
import co.unicauca.edu.co.contables.configuration.enterprise.dataAccess.mapper.EnterpriseDataMapper;
import co.unicauca.edu.co.contables.configuration.enterprise.dataAccess.mapper.LocationDataMapper;
import co.unicauca.edu.co.contables.configuration.enterprise.dataAccess.mapper.PersonTypeDataMapper;
import co.unicauca.edu.co.contables.configuration.enterprise.dataAccess.mapper.TaxLiabilityDataMapper;
import co.unicauca.edu.co.contables.configuration.enterprise.dataAccess.mapper.TaxPayerTypeDataMapper;
import co.unicauca.edu.co.contables.configuration.enterprise.dataAccess.mapper.EnterpriseTypeDataMapper;
import co.unicauca.edu.co.contables.configuration.enterprise.dataAccess.repository.EnterpriseRepository;
import co.unicauca.edu.co.contables.configuration.enterprise.dataAccess.repository.SubjectRepository;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.enums.InventoryConfigurationTypeEnum;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.models.Enterprise;

/**
 * El método de valoración del inventario debe sobrevivir al update de la empresa.
 *
 * Antes el update copiaba los campos uno por uno y se saltaba
 * inventoryConfigurationType, así que el formulario lo enviaba, el PUT
 * devolvía 200 y la empresa seguía sin método de valoración.
 */
class EnterpriseServiceImplTest {

    private static final UUID ID = UUID.fromString("d48409e5-eb00-4f61-92b9-323250d96be6");

    private EnterpriseRepository repository;
    private EnterpriseServiceImpl service;

    @BeforeEach
    void setUp() {
        repository = mock(EnterpriseRepository.class);
        service = new EnterpriseServiceImpl(
                repository,
                mock(EnterpriseDataMapper.class),
                mock(LocationDataMapper.class),
                mock(PersonTypeDataMapper.class),
                mock(TaxLiabilityDataMapper.class),
                mock(TaxPayerTypeDataMapper.class),
                mock(EnterpriseTypeDataMapper.class),
                mock(ILocationService.class),
                mock(IPersonTypeService.class),
                mock(SubjectRepository.class));
        when(repository.save(any(EnterpriseEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void updatePersistsTheInventoryConfigurationTypeSentByTheForm() {
        EnterpriseEntity current = EnterpriseEntity.builder().id(ID).name("Cocacola").build();
        when(repository.findById(ID)).thenReturn(Optional.of(current));

        Enterprise incoming = Enterprise.builder()
                .name("Cocacola")
                .inventoryConfigurationType(InventoryConfigurationTypeEnum.PEPS)
                .build();

        service.update(ID, incoming);

        assertThat(current.getInventoryConfigurationType())
                .isEqualTo(InventoryConfigurationTypeEnum.PEPS);
    }

    @Test
    void updateKeepsTheStoredInventoryConfigurationTypeWhenTheRequestOmitsIt() {
        EnterpriseEntity current = EnterpriseEntity.builder()
                .id(ID)
                .name("Cocacola")
                .inventoryConfigurationType(InventoryConfigurationTypeEnum.WEIGHTED_AVERAGE)
                .build();
        when(repository.findById(ID)).thenReturn(Optional.of(current));

        Enterprise incoming = Enterprise.builder().name("Cocacola").build();

        service.update(ID, incoming);

        assertThat(current.getInventoryConfigurationType())
                .isEqualTo(InventoryConfigurationTypeEnum.WEIGHTED_AVERAGE);
    }
}
