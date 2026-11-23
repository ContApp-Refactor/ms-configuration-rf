package co.unicauca.edu.co.contables.inventory.peps.unit.infrastucture.output.jpa.adapter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import co.unicauca.edu.co.contables.inventory.peps.domain.model.MessageProcessingError;
import co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.output.jpa.adapter.MessageProcessingErrorQueryAdapter;
import co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.output.jpa.entity.MessageProcessingErrorEntity;
import co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.output.jpa.mapper.IMessageProcessingErrorEntityMapper;
import co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.output.jpa.repository.IMessageProcessingErrorRepository;

@ExtendWith(MockitoExtension.class)
public class MessageprocessingErrorQueryAdapterUnitTest {
    
    @Mock
    private IMessageProcessingErrorRepository messageProcessingErrorRepository;
    
    @Mock
    private IMessageProcessingErrorEntityMapper messageProcessingErrorMapper;
    
    @InjectMocks
    private MessageProcessingErrorQueryAdapter messageProcessingErrorQueryAdapter;
    
    private MessageProcessingErrorEntity errorEntity;
    private MessageProcessingError errorDomain;
    
    @BeforeEach
    void setUp() {
        // Create entity
        errorEntity = new MessageProcessingErrorEntity();
        errorEntity.setId(1L);
        errorEntity.setEventType("PROCESSING_ERROR");
        errorEntity.setErrorDescription("Test error message");
        errorEntity.setMessageData("Test message data");
        errorEntity.setErrorTimestamp(Instant.now());
        errorEntity.setEntityType("Kardex");
        
        // Create domain model
        errorDomain = new MessageProcessingError();
        errorDomain.setId(1L);
        errorDomain.setEventType("PROCESSING_ERROR");
        errorDomain.setErrorDescription("Test error message");
        errorDomain.setMessageData("Test message data");
        errorDomain.setErrorTimestamp(Instant.now());
        errorDomain.setEntityType("Kardex");
    }
    
    // ==================== findById() ====================
    @Test
    @DisplayName("Should find message processing error by ID successfully")
    void testFindById_ExistingId_ReturnsError() {
        // Arrange
        when(messageProcessingErrorRepository.findById(1L))
            .thenReturn(Optional.of(errorEntity));
        when(messageProcessingErrorMapper.toDomain(errorEntity))
            .thenReturn(errorDomain);
        
        // Act
        Optional<MessageProcessingError> result = messageProcessingErrorQueryAdapter.findById(1L);
        
        // Assert
        assertTrue(result.isPresent());
        assertEquals(1L, result.get().getId());
        assertEquals("PROCESSING_ERROR", result.get().getEventType());
        assertEquals("Test error message", result.get().getErrorDescription());
        verify(messageProcessingErrorRepository).findById(1L);
        verify(messageProcessingErrorMapper).toDomain(errorEntity);
    }
    
    @Test
    @DisplayName("Should return empty Optional when ID does not exist")
    void testFindById_NonExistingId_ReturnsEmpty() {
        // Arrange
        when(messageProcessingErrorRepository.findById(999L))
            .thenReturn(Optional.empty());
        
        // Act
        Optional<MessageProcessingError> result = messageProcessingErrorQueryAdapter.findById(999L);
        
        // Assert
        assertFalse(result.isPresent());
        verify(messageProcessingErrorRepository).findById(999L);
    }
    
    @Test
    @DisplayName("Should handle null ID gracefully")
    void testFindById_NullId_ReturnsEmpty() {
        // Arrange
        when(messageProcessingErrorRepository.findById(null))
            .thenReturn(Optional.empty());
        
        // Act
        Optional<MessageProcessingError> result = messageProcessingErrorQueryAdapter.findById(null);
        
        // Assert
        assertFalse(result.isPresent());
        verify(messageProcessingErrorRepository).findById(null);
    }
    
    // ==================== findLastRecord() ====================
    @Test
    @DisplayName("Should find the most recent message processing error")
    void testFindLastRecord_RecordsExist_ReturnsLatest() {
        // Arrange
        when(messageProcessingErrorRepository.findFirstByOrderByErrorTimestampDesc())
            .thenReturn(Optional.of(errorEntity));
        when(messageProcessingErrorMapper.toDomain(errorEntity))
            .thenReturn(errorDomain);
        
        // Act
        Optional<MessageProcessingError> result = messageProcessingErrorQueryAdapter.findLastRecord();
        
        // Assert
        assertTrue(result.isPresent());
        assertEquals(1L, result.get().getId());
        assertEquals("PROCESSING_ERROR", result.get().getEventType());
        verify(messageProcessingErrorRepository).findFirstByOrderByErrorTimestampDesc();
        verify(messageProcessingErrorMapper).toDomain(errorEntity);
    }
    
    @Test
    @DisplayName("Should return empty Optional when no records exist")
    void testFindLastRecord_NoRecords_ReturnsEmpty() {
        // Arrange
        when(messageProcessingErrorRepository.findFirstByOrderByErrorTimestampDesc())
            .thenReturn(Optional.empty());
        
        // Act
        Optional<MessageProcessingError> result = messageProcessingErrorQueryAdapter.findLastRecord();
        
        // Assert
        assertFalse(result.isPresent());
        verify(messageProcessingErrorRepository).findFirstByOrderByErrorTimestampDesc();
    }
    
    @Test
    @DisplayName("Should retrieve the error with the latest timestamp")
    void testFindLastRecord_MultipleRecords_ReturnsNewest() {
        // Arrange
        MessageProcessingErrorEntity latestEntity = new MessageProcessingErrorEntity();
        latestEntity.setId(5L);
        latestEntity.setEventType("VALIDATION_ERROR");
        latestEntity.setErrorDescription("Latest error");
        latestEntity.setMessageData("Latest message data");
        latestEntity.setErrorTimestamp(Instant.now());
        latestEntity.setEntityType("Product");
        
        MessageProcessingError latestDomain = new MessageProcessingError();
        latestDomain.setId(5L);
        latestDomain.setEventType("VALIDATION_ERROR");
        latestDomain.setErrorDescription("Latest error");
        latestDomain.setMessageData("Latest message data");
        latestDomain.setErrorTimestamp(Instant.now());
        latestDomain.setEntityType("Product");
        
        when(messageProcessingErrorRepository.findFirstByOrderByErrorTimestampDesc())
            .thenReturn(Optional.of(latestEntity));
        when(messageProcessingErrorMapper.toDomain(latestEntity))
            .thenReturn(latestDomain);
        
        // Act
        Optional<MessageProcessingError> result = messageProcessingErrorQueryAdapter.findLastRecord();
        
        // Assert
        assertTrue(result.isPresent());
        assertEquals(5L, result.get().getId());
        assertEquals("VALIDATION_ERROR", result.get().getEventType());
        verify(messageProcessingErrorRepository).findFirstByOrderByErrorTimestampDesc();
    }
    
    // ==================== findAll() ====================
    @Test
    @DisplayName("Should retrieve all errors with pagination successfully")
    void testFindAll_WithPagination_ReturnsPage() {
        // Arrange
        MessageProcessingErrorEntity error2 = new MessageProcessingErrorEntity();
        error2.setId(2L);
        error2.setEventType("CONNECTION_ERROR");
        error2.setErrorDescription("Error 2");
        error2.setMessageData("Message data 2");
        error2.setErrorTimestamp(Instant.now());
        error2.setEntityType("Stock");
        
        MessageProcessingError domain2 = new MessageProcessingError();
        domain2.setId(2L);
        domain2.setEventType("CONNECTION_ERROR");
        domain2.setErrorDescription("Error 2");
        domain2.setMessageData("Message data 2");
        domain2.setErrorTimestamp(Instant.now());
        domain2.setEntityType("Stock");
        
        List<MessageProcessingErrorEntity> entities = List.of(errorEntity, error2);
        Page<MessageProcessingErrorEntity> entityPage = new PageImpl<>(entities, PageRequest.of(0, 10), 2);
        
        when(messageProcessingErrorRepository.findAll(any(Pageable.class)))
            .thenReturn(entityPage);
        when(messageProcessingErrorMapper.toDomain(errorEntity))
            .thenReturn(errorDomain);
        when(messageProcessingErrorMapper.toDomain(error2))
            .thenReturn(domain2);
        
        // Act
        Pageable pageable = PageRequest.of(0, 10);
        Page<MessageProcessingError> result = messageProcessingErrorQueryAdapter.findAll(pageable);
        
        // Assert
        assertNotNull(result);
        assertEquals(2, result.getTotalElements());
        assertEquals(2, result.getContent().size());
        assertEquals(0, result.getNumber());
        assertEquals(10, result.getSize());
        verify(messageProcessingErrorRepository).findAll(pageable);
        verify(messageProcessingErrorMapper).toDomain(errorEntity);
        verify(messageProcessingErrorMapper).toDomain(error2);
    }
    
    @Test
    @DisplayName("Should return empty page when no errors exist")
    void testFindAll_NoRecords_ReturnsEmptyPage() {
        // Arrange
        Page<MessageProcessingErrorEntity> emptyPage = new PageImpl<>(List.of(), PageRequest.of(0, 10), 0);
        
        when(messageProcessingErrorRepository.findAll(any(Pageable.class)))
            .thenReturn(emptyPage);
        
        // Act
        Pageable pageable = PageRequest.of(0, 10);
        Page<MessageProcessingError> result = messageProcessingErrorQueryAdapter.findAll(pageable);
        
        // Assert
        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
        assertTrue(result.getContent().isEmpty());
        verify(messageProcessingErrorRepository).findAll(pageable);
    }
    
    @Test
    @DisplayName("Should handle different page sizes correctly")
    void testFindAll_DifferentPageSize_ReturnsCorrectPage() {
        // Arrange
        List<MessageProcessingErrorEntity> entities = List.of(errorEntity);
        Page<MessageProcessingErrorEntity> entityPage = new PageImpl<>(entities, PageRequest.of(0, 5), 1);
        
        when(messageProcessingErrorRepository.findAll(any(Pageable.class)))
            .thenReturn(entityPage);
        when(messageProcessingErrorMapper.toDomain(errorEntity))
            .thenReturn(errorDomain);
        
        // Act
        Pageable pageable = PageRequest.of(0, 5);
        Page<MessageProcessingError> result = messageProcessingErrorQueryAdapter.findAll(pageable);
        
        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getContent().size());
        assertEquals(5, result.getSize());
        verify(messageProcessingErrorRepository).findAll(pageable);
    }
    
    @Test
    @DisplayName("Should retrieve second page correctly")
    void testFindAll_SecondPage_ReturnsCorrectContent() {
        // Arrange
        MessageProcessingErrorEntity error3 = new MessageProcessingErrorEntity();
        error3.setId(3L);
        error3.setEventType("TIMEOUT_ERROR");
        error3.setErrorDescription("Error 3");
        error3.setMessageData("Message data 3");
        error3.setErrorTimestamp(Instant.now());
        error3.setEntityType("Product");
        
        List<MessageProcessingErrorEntity> entities = List.of(error3);
        Page<MessageProcessingErrorEntity> entityPage = new PageImpl<>(entities, PageRequest.of(1, 2), 3);
        
        MessageProcessingError domain3 = new MessageProcessingError();
        domain3.setId(3L);
        domain3.setEventType("TIMEOUT_ERROR");
        domain3.setErrorDescription("Error 3");
        domain3.setMessageData("Message data 3");
        domain3.setErrorTimestamp(Instant.now());
        domain3.setEntityType("Product");
        
        when(messageProcessingErrorRepository.findAll(any(Pageable.class)))
            .thenReturn(entityPage);
        when(messageProcessingErrorMapper.toDomain(error3))
            .thenReturn(domain3);
        
        // Act
        Pageable pageable = PageRequest.of(1, 2);
        Page<MessageProcessingError> result = messageProcessingErrorQueryAdapter.findAll(pageable);
        
        // Assert
        assertNotNull(result);
        assertEquals(3, result.getTotalElements());
        assertEquals(1, result.getContent().size());
        assertEquals(1, result.getNumber());
        assertEquals(3L, result.getContent().get(0).getId());
        verify(messageProcessingErrorRepository).findAll(pageable);
    }
    
    @Test
    @DisplayName("Should map all entities to domain correctly")
    void testFindAll_MultipleRecords_MapsAllCorrectly() {
        // Arrange
        MessageProcessingErrorEntity error2 = new MessageProcessingErrorEntity();
        error2.setId(2L);
        error2.setEventType("TYPE");
        error2.setErrorDescription("Error 2");
        error2.setMessageData("Data 2");
        error2.setEntityType("Kardex");
        
        MessageProcessingErrorEntity error3 = new MessageProcessingErrorEntity();
        error3.setId(3L);
        error3.setEventType("TYPE");
        error3.setErrorDescription("Error 3");
        error3.setMessageData("Data 3");
        error3.setEntityType("Stock");
        
        List<MessageProcessingErrorEntity> entities = List.of(errorEntity, error2, error3);
        Page<MessageProcessingErrorEntity> entityPage = new PageImpl<>(entities, PageRequest.of(0, 10), 3);
        
        MessageProcessingError domain2 = new MessageProcessingError();
        domain2.setId(2L);
        domain2.setEventType("TYPE");
        domain2.setErrorDescription("Error 2");
        
        MessageProcessingError domain3 = new MessageProcessingError();
        domain3.setId(3L);
        domain3.setEventType("TYPE");
        domain3.setErrorDescription("Error 3");
        
        when(messageProcessingErrorRepository.findAll(any(Pageable.class)))
            .thenReturn(entityPage);
        when(messageProcessingErrorMapper.toDomain(errorEntity))
            .thenReturn(errorDomain);
        when(messageProcessingErrorMapper.toDomain(error2))
            .thenReturn(domain2);
        when(messageProcessingErrorMapper.toDomain(error3))
            .thenReturn(domain3);
        
        // Act
        Pageable pageable = PageRequest.of(0, 10);
        Page<MessageProcessingError> result = messageProcessingErrorQueryAdapter.findAll(pageable);
        
        // Assert
        assertEquals(3, result.getContent().size());
        assertEquals(1L, result.getContent().get(0).getId());
        assertEquals(2L, result.getContent().get(1).getId());
        assertEquals(3L, result.getContent().get(2).getId());
    }
}
