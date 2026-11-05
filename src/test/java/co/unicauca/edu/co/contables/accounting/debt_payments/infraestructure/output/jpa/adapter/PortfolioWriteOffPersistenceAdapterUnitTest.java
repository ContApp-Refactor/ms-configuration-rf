package co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.output.jpa.adapter;

import co.unicauca.edu.co.contables.accounting.debt_payments.domain.model.PortfolioWriteOff;
import co.unicauca.edu.co.contables.accounting.debt_payments.domain.model.WriteOffDetail;
import co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.output.jpa.adapter.PortfolioWriteOffPersistenceAdapter;
import co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.output.jpa.entity.PortfolioWriteOffEntity;
import co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.output.jpa.entity.WriteOffDetailEntity;
import co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.output.jpa.mapper.IPortfolioWriteOffPersistenceMapper;
import co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.output.jpa.repository.IPortfolioWriteOffRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class) // Habilita Mockito, sin contexto de Spring
@DisplayName("Pruebas Unitarias para PortfolioWriteOffPersistenceAdapter")
public class PortfolioWriteOffPersistenceAdapterUnitTest {
    @Mock 
    private IPortfolioWriteOffRepository portfolioWriteOffRepository;

    @Mock 
    private IPortfolioWriteOffPersistenceMapper portfolioWriteOffMapper;

    @InjectMocks 
    private PortfolioWriteOffPersistenceAdapter portfolioWriteOffPersistenceAdapter;

    @Test
    @DisplayName("save - Debe mapear, establecer la relación bidireccional, guardar y devolver el dominio mapeado")
    void save_shouldMapAndPersistSuccessfully() {
        // ARRANGE
        // 1. Creamos los objetos de prueba (Dominio y Entidad)
        // Tengo builder en WriteOffDetail (usamos un mock para evitar usar un constructor no visible)
        WriteOffDetail domainDetail = mock(WriteOffDetail.class);
        PortfolioWriteOff domainToSave = new PortfolioWriteOff();
        domainToSave.setDetails(List.of(domainDetail));
        
        WriteOffDetailEntity entityDetail = new WriteOffDetailEntity();
        PortfolioWriteOffEntity entityToSave = new PortfolioWriteOffEntity();
        entityToSave.setDetails(List.of(entityDetail));

        // 2. Programamos el comportamiento de nuestros mocks
        // Cuando el mapper reciba el objeto de dominio, devolverá nuestra entidad de prueba
        when(portfolioWriteOffMapper.toEntity(domainToSave)).thenReturn(entityToSave);
        
        // Cuando el repositorio guarde CUALQUIER PortfolioWriteOffEntity, devolverá la misma entidad
        when(portfolioWriteOffRepository.save(any(PortfolioWriteOffEntity.class))).thenReturn(entityToSave);
        
        // Cuando el mapper reciba la entidad guardada, devolverá nuestro objeto de dominio original
        when(portfolioWriteOffMapper.toDomain(entityToSave)).thenReturn(domainToSave);

        // ACT
        PortfolioWriteOff result = portfolioWriteOffPersistenceAdapter.save(domainToSave);

        // ASSERT
        // Verificamos que el resultado es el que esperamos
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(domainToSave);

        // Verificación de interacciones (la parte más importante)
        verify(portfolioWriteOffMapper).toEntity(domainToSave); // Se llamó al mapper para convertir a entidad
        verify(portfolioWriteOffRepository).save(entityToSave);  // Se llamó al repositorio para guardar
        verify(portfolioWriteOffMapper).toDomain(entityToSave);  // Se llamó al mapper para convertir de vuelta a dominio

        // Verificación clave: que la relación bidireccional fue establecida
        assertThat(entityDetail.getPortfolioWriteOff()).isNotNull();
        assertThat(entityDetail.getPortfolioWriteOff()).isEqualTo(entityToSave);
    }
    
    @Test
    @DisplayName("findById - Debe llamar al repositorio y mapear el resultado si existe")
    void findById_shouldReturnMappedDomainWhenFound() {
        // ARRANGE
        Long idToFind = 1L;
        PortfolioWriteOffEntity foundEntity = new PortfolioWriteOffEntity();
        foundEntity.setId(idToFind);
        
        PortfolioWriteOff expectedDomain = new PortfolioWriteOff();
        expectedDomain.setId(idToFind);

        // Programamos los mocks
        when(portfolioWriteOffRepository.findById(idToFind)).thenReturn(Optional.of(foundEntity));
        when(portfolioWriteOffMapper.toDomain(foundEntity)).thenReturn(expectedDomain);

        // ACT
        Optional<PortfolioWriteOff> result = portfolioWriteOffPersistenceAdapter.findById(idToFind);

        // ASSERT
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(expectedDomain);
        
        verify(portfolioWriteOffRepository).findById(idToFind);
        verify(portfolioWriteOffMapper).toDomain(foundEntity);
    }
    
    @Test
    @DisplayName("findById - Debe devolver un Optional vacío si no se encuentra")
    void findById_shouldReturnEmptyOptionalWhenNotFound() {
        // ARRANGE
        Long idToFind = 99L;
        when(portfolioWriteOffRepository.findById(idToFind)).thenReturn(Optional.empty());

        // ACT
        Optional<PortfolioWriteOff> result = portfolioWriteOffPersistenceAdapter.findById(idToFind);

        // ASSERT
        assertThat(result).isNotPresent();
        
        // Verificamos que el mapper NUNCA fue llamado
        verify(portfolioWriteOffMapper, never()).toDomain(any(PortfolioWriteOffEntity.class));
    }

    @Nested
    @DisplayName("Pruebas para el método update")
    class UpdateTests {
        @Test
        @DisplayName("Debe actualizar exitosamente si el ID existe")
        void shouldUpdateSuccessfullyWhenIdExists() {
            // ARRANGE
            Long existingId = 1L;
            PortfolioWriteOff domainToUpdate = new PortfolioWriteOff();
            domainToUpdate.setId(existingId);
            domainToUpdate.setDetails(List.of(mock(WriteOffDetail.class)));

            WriteOffDetailEntity entityDetail = new WriteOffDetailEntity();
            PortfolioWriteOffEntity entityToUpdate = new PortfolioWriteOffEntity();
            entityToUpdate.setId(existingId);
            entityToUpdate.setDetails(new java.util.ArrayList<>(List.of(entityDetail)));

            // Programamos los mocks
            when(portfolioWriteOffRepository.existsById(existingId)).thenReturn(true);
            when(portfolioWriteOffMapper.toEntity(domainToUpdate)).thenReturn(entityToUpdate);
            when(portfolioWriteOffRepository.save(entityToUpdate)).thenReturn(entityToUpdate);
            when(portfolioWriteOffMapper.toDomain(entityToUpdate)).thenReturn(domainToUpdate);

            // ACT
            PortfolioWriteOff result = portfolioWriteOffPersistenceAdapter.update(domainToUpdate);

            // ASSERT
            assertThat(result).isNotNull().isEqualTo(domainToUpdate);

            // Verificamos la secuencia de llamadas
            var inOrder = inOrder(portfolioWriteOffRepository, portfolioWriteOffMapper);
            inOrder.verify(portfolioWriteOffRepository).existsById(existingId);
            inOrder.verify(portfolioWriteOffMapper).toEntity(domainToUpdate);
            inOrder.verify(portfolioWriteOffRepository).save(entityToUpdate);
            inOrder.verify(portfolioWriteOffMapper).toDomain(entityToUpdate);

            // Verificamos la lógica de la relación bidireccional
            assertThat(entityDetail.getPortfolioWriteOff()).isEqualTo(entityToUpdate);
        }
        
        @Test
        @DisplayName("Debe lanzar IllegalArgumentException si el ID es nulo")
        void shouldThrowExceptionWhenUpdatingWithNullId() {
            // ARRANGE
            PortfolioWriteOff domainWithNullId = new PortfolioWriteOff();
            domainWithNullId.setId(null); // ID es nulo
            
            // ACT & ASSERT
            assertThatThrownBy(() -> portfolioWriteOffPersistenceAdapter.update(domainWithNullId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("El castigo con ID null no existe.");
            
            // Verificamos que no se intentó guardar nada
            verify(portfolioWriteOffRepository, never()).save(any());
        }

        @Test
        @DisplayName("Debe lanzar IllegalArgumentException si el ID no existe")
        void shouldThrowExceptionWhenUpdatingNonExistentId() {
            // ARRANGE
            Long nonExistentId = 99L;
            PortfolioWriteOff domainToUpdate = new PortfolioWriteOff();
            domainToUpdate.setId(nonExistentId);
            
            // Simulamos que el repositorio nos dice que no existe
            when(portfolioWriteOffRepository.existsById(nonExistentId)).thenReturn(false);

            // ACT & ASSERT
            assertThatThrownBy(() -> portfolioWriteOffPersistenceAdapter.update(domainToUpdate))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("El castigo con ID " + nonExistentId + " no existe.");
            
            verify(portfolioWriteOffRepository, never()).save(any());
        }
    }

    // --- Pruebas para el método findByEnterpriseId ---
    @Nested
    @DisplayName("Pruebas para el método findByEnterpriseId")
    class FindByEnterpriseIdTests {
        @Test
        @DisplayName("Debe devolver una lista de dominios mapeados cuando se encuentran entidades")
        void shouldReturnMappedDomainListWhenEntitiesAreFound() {
            // ARRANGE
            String enterpriseId = "ENT-01";
            List<PortfolioWriteOffEntity> foundEntities = List.of(new PortfolioWriteOffEntity(), new PortfolioWriteOffEntity());
            List<PortfolioWriteOff> expectedDomains = List.of(new PortfolioWriteOff(), new PortfolioWriteOff());
            
            when(portfolioWriteOffRepository.findByEnterpriseId(enterpriseId)).thenReturn(foundEntities);
            when(portfolioWriteOffMapper.toDomainList(foundEntities)).thenReturn(expectedDomains);

            // ACT
            List<PortfolioWriteOff> result = portfolioWriteOffPersistenceAdapter.findByEnterpriseId(enterpriseId);

            // ASSERT
            assertThat(result).isNotNull().isEqualTo(expectedDomains);
            
            verify(portfolioWriteOffRepository).findByEnterpriseId(enterpriseId);
            verify(portfolioWriteOffMapper).toDomainList(foundEntities);
        }
        
        @Test
        @DisplayName("Debe devolver una lista vacía si el repositorio no encuentra entidades")
        void shouldReturnEmptyListWhenNoEntitiesAreFound() {
            // ARRANGE
            String enterpriseId = "ENT-02";
            // Simulamos que el repositorio devuelve una lista vacía
            when(portfolioWriteOffRepository.findByEnterpriseId(enterpriseId)).thenReturn(Collections.emptyList());
            when(portfolioWriteOffMapper.toDomainList(Collections.emptyList())).thenReturn(Collections.emptyList());

            // ACT
            List<PortfolioWriteOff> result = portfolioWriteOffPersistenceAdapter.findByEnterpriseId(enterpriseId);

            // ASSERT
            assertThat(result).isNotNull().isEmpty();
            
            verify(portfolioWriteOffRepository).findByEnterpriseId(enterpriseId);
            verify(portfolioWriteOffMapper).toDomainList(Collections.emptyList());
        }
    }

}

