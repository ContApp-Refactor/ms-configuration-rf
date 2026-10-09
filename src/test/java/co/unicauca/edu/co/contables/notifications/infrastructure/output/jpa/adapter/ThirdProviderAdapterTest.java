package co.unicauca.edu.co.contables.notifications.infrastructure.output.jpa.adapter;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import co.unicauca.edu.co.contables.notifications.domain.model.ThirdReplica;
import co.unicauca.edu.co.contables.notifications.infrastructure.output.jpa.entity.ThirdReplicaEntity;
import co.unicauca.edu.co.contables.notifications.infrastructure.output.jpa.repository.IThirdReplicaRepository;

@DisplayName("ThirdProviderAdapter - Unit Tests")
class ThirdProviderAdapterUnitTest {

    private ThirdProviderAdapter adapter;
    private IThirdReplicaRepository repositoryMock;

    @BeforeEach
    void setUp() {
        // Crear mock sin cargar contexto de Spring
        repositoryMock = mock(IThirdReplicaRepository.class);

        // Inyectar la dependencia manualmente
        adapter = new ThirdProviderAdapter(repositoryMock);
    }

    @Test
    @DisplayName("Should return ThirdReplica when third party found")
    void testFindByThirdPartyId_Success() {
        // Arrange
        Long thirdPartyId = 100L;
        ThirdReplicaEntity entity = createThirdReplicaEntity(1L, thirdPartyId, "Juan Pérez", "juan@example.com");

        when(repositoryMock.findByThirdId(thirdPartyId))
                .thenReturn(Optional.of(entity));

        // Act
        Optional<ThirdReplica> result = adapter.findByThirdPartyId(thirdPartyId);

        // Assert
        assertTrue(result.isPresent());
        ThirdReplica thirdReplica = result.get();
        assertEquals(1L, thirdReplica.getId());
        assertEquals(100L, thirdReplica.getThirdPartyId());
        assertEquals("Juan Pérez", thirdReplica.getFullName());
        assertEquals("juan@example.com", thirdReplica.getEmail());

        verify(repositoryMock, times(1)).findByThirdId(thirdPartyId);
    }

    @Test
    @DisplayName("Should return empty Optional when third party not found")
    void testFindByThirdPartyId_NotFound() {
        // Arrange
        Long thirdPartyId = 999L;

        when(repositoryMock.findByThirdId(thirdPartyId))
                .thenReturn(Optional.empty());

        // Act
        Optional<ThirdReplica> result = adapter.findByThirdPartyId(thirdPartyId);

        // Assert
        assertFalse(result.isPresent());
        assertTrue(result.isEmpty());

        verify(repositoryMock, times(1)).findByThirdId(thirdPartyId);
    }

    @Test
    @DisplayName("Should correctly map entity to domain model")
    void testFindByThirdPartyId_MappingValidation() {
        // Arrange
        Long thirdPartyId = 50L;
        ThirdReplicaEntity entity = new ThirdReplicaEntity();
        entity.setId(5L);
        entity.setThirdId(thirdPartyId);
        entity.setFullName("María García López");
        entity.setEmail("maria.garcia@empresa.com");

        when(repositoryMock.findByThirdId(thirdPartyId))
                .thenReturn(Optional.of(entity));

        // Act
        Optional<ThirdReplica> result = adapter.findByThirdPartyId(thirdPartyId);

        // Assert
        assertTrue(result.isPresent());
        ThirdReplica mapped = result.get();
        assertEquals(entity.getId(), mapped.getId());
        assertEquals(entity.getThirdId(), mapped.getThirdPartyId());
        assertEquals(entity.getFullName(), mapped.getFullName());
        assertEquals(entity.getEmail(), mapped.getEmail());
    }

    @Test
    @DisplayName("Should handle different third party IDs independently")
    void testFindByThirdPartyId_DifferentIds() {
        // Arrange
        Long thirdPartyId1 = 10L;
        Long thirdPartyId2 = 20L;

        ThirdReplicaEntity entity1 = createThirdReplicaEntity(1L, thirdPartyId1, "Persona 1", "p1@email.com");
        ThirdReplicaEntity entity2 = createThirdReplicaEntity(2L, thirdPartyId2, "Persona 2", "p2@email.com");

        when(repositoryMock.findByThirdId(thirdPartyId1))
                .thenReturn(Optional.of(entity1));
        when(repositoryMock.findByThirdId(thirdPartyId2))
                .thenReturn(Optional.of(entity2));

        // Act
        Optional<ThirdReplica> result1 = adapter.findByThirdPartyId(thirdPartyId1);
        Optional<ThirdReplica> result2 = adapter.findByThirdPartyId(thirdPartyId2);

        // Assert
        assertTrue(result1.isPresent());
        assertTrue(result2.isPresent());

        assertEquals("Persona 1", result1.get().getFullName());
        assertEquals("Persona 2", result2.get().getFullName());

        verify(repositoryMock, times(1)).findByThirdId(thirdPartyId1);
        verify(repositoryMock, times(1)).findByThirdId(thirdPartyId2);
    }

    @Test
    @DisplayName("Should preserve all entity fields when mapping")
    void testFindByThirdPartyId_PreservesAllFields() {
        // Arrange
        Long thirdPartyId = 75L;
        Long entityId = 42L;
        String fullName = "Carlos Roberto López Mendez";
        String email = "carlos.lopez@domain.co";

        ThirdReplicaEntity entity = createThirdReplicaEntity(entityId, thirdPartyId, fullName, email);

        when(repositoryMock.findByThirdId(thirdPartyId))
                .thenReturn(Optional.of(entity));

        // Act
        Optional<ThirdReplica> result = adapter.findByThirdPartyId(thirdPartyId);

        // Assert
        assertTrue(result.isPresent());
        ThirdReplica replica = result.get();

        assertAll(
                () -> assertEquals(entityId, replica.getId()),
                () -> assertEquals(thirdPartyId, replica.getThirdPartyId()),
                () -> assertEquals(fullName, replica.getFullName()),
                () -> assertEquals(email, replica.getEmail()));
    }

    @Test
    @DisplayName("Should call repository exactly once per request")
    void testFindByThirdPartyId_RepositoryCallCount() {
        // Arrange
        Long thirdPartyId = 100L;
        ThirdReplicaEntity entity = createThirdReplicaEntity(1L, thirdPartyId, "Test", "test@email.com");

        when(repositoryMock.findByThirdId(thirdPartyId))
                .thenReturn(Optional.of(entity));

        // Act
        adapter.findByThirdPartyId(thirdPartyId);

        // Assert
        verify(repositoryMock, times(1)).findByThirdId(thirdPartyId);
        verify(repositoryMock, times(1)).findByThirdId(anyLong());
    }

    @Test
    @DisplayName("Should handle null email gracefully")
    void testFindByThirdPartyId_NullEmail() {
        // Arrange
        Long thirdPartyId = 200L;
        ThirdReplicaEntity entity = createThirdReplicaEntity(1L, thirdPartyId, "No Email Person", null);

        when(repositoryMock.findByThirdId(thirdPartyId))
                .thenReturn(Optional.of(entity));

        // Act
        Optional<ThirdReplica> result = adapter.findByThirdPartyId(thirdPartyId);

        // Assert
        assertTrue(result.isPresent());
        assertNull(result.get().getEmail());
    }

    @Test
    @DisplayName("Should handle null full name gracefully")
    void testFindByThirdPartyId_NullFullName() {
        // Arrange
        Long thirdPartyId = 300L;
        ThirdReplicaEntity entity = createThirdReplicaEntity(1L, thirdPartyId, null, "email@example.com");

        when(repositoryMock.findByThirdId(thirdPartyId))
                .thenReturn(Optional.of(entity));

        // Act
        Optional<ThirdReplica> result = adapter.findByThirdPartyId(thirdPartyId);

        // Assert
        assertTrue(result.isPresent());
        assertNull(result.get().getFullName());
    }

    // Helper methods

    private ThirdReplicaEntity createThirdReplicaEntity(Long id, Long thirdId, String fullName, String email) {
        ThirdReplicaEntity entity = new ThirdReplicaEntity();
        entity.setId(id);
        entity.setThirdId(thirdId);
        entity.setFullName(fullName);
        entity.setEmail(email);
        return entity;
    }

}
