package co.unicauca.edu.co.contables.accounting.catalogue.unit.taxes.infraestructure.output.jpaAdapters;

import co.unicauca.edu.co.contables.accounting.catalogue.taxes.domain.models.Tax;
import co.unicauca.edu.co.contables.accounting.catalogue.taxes.infraestructure.adapters.output.jpaAdapters.TaxChangeStateJpaAdapter;
import co.unicauca.edu.co.contables.accounting.catalogue.taxes.infraestructure.adapters.output.jpaAdapters.entity.TaxEntity;
import co.unicauca.edu.co.contables.accounting.catalogue.taxes.infraestructure.adapters.output.jpaAdapters.mapper.ITaxUpdateMapper;
import co.unicauca.edu.co.contables.accounting.catalogue.taxes.infraestructure.adapters.output.jpaAdapters.repository.ITaxRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class TaxChangeStateJpaAdapterUnitTest {

    @Mock
    private ITaxRepository taxRepository;

    @Mock
    private ITaxUpdateMapper taxUpdateMapper;

    @InjectMocks
    private TaxChangeStateJpaAdapter taxChangeStateJpaAdapter;

    private Long taxId;
    private String idEnterprise;
    private TaxEntity taxEntity;
    private Tax tax;

    @BeforeEach
    void setUp() {
        taxId = 1L;
        idEnterprise = "ENT-001";

        taxEntity = TaxEntity.builder()
                .id(taxId)
                .idEnterprise(idEnterprise)
                .code("IVA19")
                .description("Impuesto al valor agregado 19%")
                .interest(19.0)
                .status(true)
                .usageCount(0)
                .build();

        tax = Tax.builder()
                .id(taxId)
                .idEnterprise(idEnterprise)
                .code("IVA19")
                .description("Impuesto al valor agregado 19%")
                .interest(19.0)
                .status(true)
                .usageCount(0)
                .build();
    }

    @Test
    @DisplayName("Debe cambiar estado a inactivo exitosamente")
    void testChangeStateToInactiveSuccess() {
        // Arrange
        Boolean newStatus = false;
        TaxEntity savedEntity = TaxEntity.builder()
                .id(taxId)
                .idEnterprise(idEnterprise)
                .code("IVA19")
                .description("Impuesto al valor agregado 19%")
                .interest(19.0)
                .status(false)
                .usageCount(0)
                .build();
        Tax expectedTax = Tax.builder()
                .id(taxId)
                .idEnterprise(idEnterprise)
                .code("IVA19")
                .description("Impuesto al valor agregado 19%")
                .interest(19.0)
                .status(false)
                .usageCount(0)
                .build();
        when(taxRepository.findByIdAndIdEnterprise(taxId, idEnterprise)).thenReturn(taxEntity);
        when(taxRepository.save(any(TaxEntity.class))).thenReturn(savedEntity);
        when(taxUpdateMapper.toModel(savedEntity)).thenReturn(expectedTax);

        // Act
        Tax result = taxChangeStateJpaAdapter.changeState(taxId, idEnterprise, newStatus);

        // Assert
        assertNotNull(result);
        assertFalse(result.getStatus());
        assertEquals(taxId, result.getId());
        verify(taxRepository).findByIdAndIdEnterprise(taxId, idEnterprise);
        verify(taxRepository).save(any(TaxEntity.class));
        verify(taxUpdateMapper).toModel(savedEntity);
    }

    @Test
    @DisplayName("Debe cambiar estado a activo exitosamente")
    void testChangeStateToActiveSuccess() {
        // Arrange
        Boolean newStatus = true;
        taxEntity.setStatus(false);
        TaxEntity savedEntity = TaxEntity.builder()
                .id(taxId)
                .idEnterprise(idEnterprise)
                .code("IVA19")
                .description("Impuesto al valor agregado 19%")
                .interest(19.0)
                .status(true)
                .usageCount(0)
                .build();
        Tax expectedTax = Tax.builder()
                .id(taxId)
                .idEnterprise(idEnterprise)
                .code("IVA19")
                .description("Impuesto al valor agregado 19%")
                .interest(19.0)
                .status(true)
                .usageCount(0)
                .build();
        when(taxRepository.findByIdAndIdEnterprise(taxId, idEnterprise)).thenReturn(taxEntity);
        when(taxRepository.save(any(TaxEntity.class))).thenReturn(savedEntity);
        when(taxUpdateMapper.toModel(savedEntity)).thenReturn(expectedTax);

        // Act
        Tax result = taxChangeStateJpaAdapter.changeState(taxId, idEnterprise, newStatus);

        // Assert
        assertNotNull(result);
        assertTrue(result.getStatus());
        assertEquals(taxId, result.getId());
        verify(taxRepository).findByIdAndIdEnterprise(taxId, idEnterprise);
        verify(taxRepository).save(any(TaxEntity.class));
    }

    @Test
    @DisplayName("Debe retornar null cuando el impuesto no existe")
    void testChangeStateReturnsNullWhenTaxNotFound() {
        // Arrange
        Boolean newStatus = false;
        when(taxRepository.findByIdAndIdEnterprise(taxId, idEnterprise)).thenReturn(null);

        // Act
        Tax result = taxChangeStateJpaAdapter.changeState(taxId, idEnterprise, newStatus);

        // Assert
        assertNull(result);
        verify(taxRepository).findByIdAndIdEnterprise(taxId, idEnterprise);
        verify(taxRepository, never()).save(any(TaxEntity.class));
        verify(taxUpdateMapper, never()).toModel(any(TaxEntity.class));
    }

    @Test
    @DisplayName("Debe retornar null cuando el impuesto no pertenece a la empresa")
    void testChangeStateReturnsNullWhenEnterpriseNotMatch() {
        // Arrange
        String differentEnterprise = "ENT-002";
        Boolean newStatus = false;
        when(taxRepository.findByIdAndIdEnterprise(taxId, differentEnterprise)).thenReturn(null);

        // Act
        Tax result = taxChangeStateJpaAdapter.changeState(taxId, differentEnterprise, newStatus);

        // Assert
        assertNull(result);
        verify(taxRepository).findByIdAndIdEnterprise(taxId, differentEnterprise);
        verify(taxRepository, never()).save(any(TaxEntity.class));
    }

    @Test
    @DisplayName("Debe actualizar el estado en la entidad antes de guardar")
    void testChangeStateUpdatesEntityBeforeSave() {
        // Arrange
        Boolean newStatus = false;
        when(taxRepository.findByIdAndIdEnterprise(taxId, idEnterprise)).thenReturn(taxEntity);
        when(taxRepository.save(any(TaxEntity.class))).thenAnswer(invocation -> {
            TaxEntity entity = invocation.getArgument(0);
            assertEquals(false, entity.getStatus());
            return entity;
        });
        when(taxUpdateMapper.toModel(any(TaxEntity.class))).thenReturn(tax);

        // Act
        taxChangeStateJpaAdapter.changeState(taxId, idEnterprise, newStatus);

        // Assert
        verify(taxRepository).save(argThat(entity -> !entity.getStatus()));
    }

    @Test
    @DisplayName("Debe invocar servicios en orden correcto")
    void testChangeStateInvokesServicesInCorrectOrder() {
        // Arrange
        Boolean newStatus = false;
        when(taxRepository.findByIdAndIdEnterprise(taxId, idEnterprise)).thenReturn(taxEntity);
        when(taxRepository.save(any(TaxEntity.class))).thenReturn(taxEntity);
        when(taxUpdateMapper.toModel(taxEntity)).thenReturn(tax);

        // Act
        taxChangeStateJpaAdapter.changeState(taxId, idEnterprise, newStatus);

        // Assert
        var inOrder = inOrder(taxRepository, taxUpdateMapper);
        inOrder.verify(taxRepository).findByIdAndIdEnterprise(taxId, idEnterprise);
        inOrder.verify(taxRepository).save(any(TaxEntity.class));
        inOrder.verify(taxUpdateMapper).toModel(any(TaxEntity.class));
    }

    @Test
    @DisplayName("Debe manejar cambio de estado con ID inexistente")
    void testChangeStateWithNonExistentId() {
        // Arrange
        Long nonExistentId = 999L;
        Boolean newStatus = true;
        when(taxRepository.findByIdAndIdEnterprise(nonExistentId, idEnterprise)).thenReturn(null);

        // Act
        Tax result = taxChangeStateJpaAdapter.changeState(nonExistentId, idEnterprise, newStatus);

        // Assert
        assertNull(result);
        verify(taxRepository).findByIdAndIdEnterprise(nonExistentId, idEnterprise);
    }

    @Test
    @DisplayName("Debe mantener otros atributos sin cambios al modificar estado")
    void testChangeStatePreservesOtherAttributes() {
        // Arrange
        Boolean newStatus = false;
        TaxEntity savedEntity = TaxEntity.builder()
                .id(taxId)
                .idEnterprise(idEnterprise)
                .code("IVA19")
                .description("Impuesto al valor agregado 19%")
                .interest(19.0)
                .status(false)
                .usageCount(5)
                .build();
        Tax expectedTax = Tax.builder()
                .id(taxId)
                .idEnterprise(idEnterprise)
                .code("IVA19")
                .description("Impuesto al valor agregado 19%")
                .interest(19.0)
                .status(false)
                .usageCount(5)
                .build();
        taxEntity.setUsageCount(5);
        when(taxRepository.findByIdAndIdEnterprise(taxId, idEnterprise)).thenReturn(taxEntity);
        when(taxRepository.save(any(TaxEntity.class))).thenReturn(savedEntity);
        when(taxUpdateMapper.toModel(savedEntity)).thenReturn(expectedTax);

        // Act
        Tax result = taxChangeStateJpaAdapter.changeState(taxId, idEnterprise, newStatus);

        // Assert
        assertNotNull(result);
        assertEquals("IVA19", result.getCode());
        assertEquals("Impuesto al valor agregado 19%", result.getDescription());
        assertEquals(19.0, result.getInterest());
        assertEquals(5, result.getUsageCount());
    }

    @Test
    @DisplayName("Debe manejar empresa con ID vacío")
    void testChangeStateWithEmptyEnterpriseId() {
        // Arrange
        String emptyEnterpriseId = "";
        Boolean newStatus = false;
        when(taxRepository.findByIdAndIdEnterprise(taxId, emptyEnterpriseId)).thenReturn(null);

        // Act
        Tax result = taxChangeStateJpaAdapter.changeState(taxId, emptyEnterpriseId, newStatus);

        // Assert
        assertNull(result);
        verify(taxRepository).findByIdAndIdEnterprise(taxId, emptyEnterpriseId);
    }
}
