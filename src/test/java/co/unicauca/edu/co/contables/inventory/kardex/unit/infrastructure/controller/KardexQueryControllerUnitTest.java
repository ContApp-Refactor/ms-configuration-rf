package co.unicauca.edu.co.contables.inventory.kardex.unit.infrastructure.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;

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

import co.unicauca.edu.co.contables.inventory.kardex.application.ports.input.kardex.IKardexQueryPort;
import co.unicauca.edu.co.contables.inventory.kardex.domain.model.Kardex;
import co.unicauca.edu.co.contables.inventory.kardex.domain.model.MovementType;
import co.unicauca.edu.co.contables.inventory.kardex.infrastructure.adapters.input.rest.controller.KardexQueryController;
import co.unicauca.edu.co.contables.inventory.kardex.infrastructure.adapters.input.rest.dto.ResponseDto;
import co.unicauca.edu.co.contables.inventory.kardex.infrastructure.adapters.input.rest.dto.request.KardexFilterDto;
import co.unicauca.edu.co.contables.inventory.kardex.infrastructure.adapters.input.rest.dto.response.KardexDtoResponse;
import co.unicauca.edu.co.contables.inventory.kardex.infrastructure.adapters.input.rest.mapper.IKardexResponseMapper;

@ExtendWith(MockitoExtension.class)
public class KardexQueryControllerUnitTest {
    
    @Mock
    private IKardexQueryPort kardexQueryPort;
    
    @Mock
    private IKardexResponseMapper kardexResponseMapper;
    
    @InjectMocks
    private KardexQueryController kardexQueryController;
    
    private Kardex kardex1;
    private Kardex kardex2;
    private KardexDtoResponse response1;
    private KardexDtoResponse response2;
    private Pageable pageable;
    private KardexFilterDto filterDto;
    
    @BeforeEach
    void setUp() {
        pageable = PageRequest.of(0, 10);
        filterDto = new KardexFilterDto();
        
        // Setup kardex 1
        kardex1 = new Kardex();
        kardex1.setId(1L);
        kardex1.setProductId(100L);
        kardex1.setQuantity(10);
        kardex1.setUnitPrice(new BigDecimal("25.50"));
        kardex1.setType(MovementType.PURCHASE);
        kardex1.setDate(ZonedDateTime.now());
        
        // Setup kardex 2
        kardex2 = new Kardex();
        kardex2.setId(2L);
        kardex2.setProductId(100L);
        kardex2.setQuantity(5);
        kardex2.setUnitPrice(new BigDecimal("25.50"));
        kardex2.setType(MovementType.SALE);
        kardex2.setDate(ZonedDateTime.now());
        
        // Setup responses
        response1 = new KardexDtoResponse();
        response1.setId(1L);
        response1.setQuantity(10L);
        response1.setType(MovementType.PURCHASE);
        
        response2 = new KardexDtoResponse();
        response2.setId(2L);
        response2.setQuantity(5L);
        response2.setType(MovementType.SALE);
    }
    
    @Test
    @DisplayName("Should get kardex by product ID successfully")
    void testGetKardexByProductId() {
        // Arrange
        Long productId = 100L;
        List<Kardex> kardexList = Arrays.asList(kardex1, kardex2);
        Page<Kardex> kardexPage = new PageImpl<>(kardexList, pageable, kardexList.size());
        
        when(kardexQueryPort.findProductId(productId, pageable, null, null)).thenReturn(kardexPage);
        when(kardexResponseMapper.toDtoResponse(kardex1)).thenReturn(response1);
        when(kardexResponseMapper.toDtoResponse(kardex2)).thenReturn(response2);
        
        // Act
        ResponseDto<Page<KardexDtoResponse>> response = kardexQueryController.getKardexByProductId(productId, filterDto, pageable);
        
        // Assert
        assertNotNull(response);
        assertEquals(200, response.getStatus());
        assertEquals("Kardex records retrieved successfully", response.getMessage());
        assertNotNull(response.getData());
        assertEquals(2, response.getData().getTotalElements());
        
        verify(kardexQueryPort).findProductId(productId, pageable, null, null);
        verify(kardexResponseMapper, times(2)).toDtoResponse(any(Kardex.class));
    }
    
    @Test
    @DisplayName("Should get kardex by product ID with date filters")
    void testGetKardexByProductIdWithDateFilters() {
        // Arrange
        Long productId = 100L;
        LocalDate startDate = LocalDate.now().minusDays(7);
        LocalDate endDate = LocalDate.now();
        filterDto.setStartDate(startDate);
        filterDto.setEndDate(endDate);
        
        List<Kardex> kardexList = Arrays.asList(kardex1);
        Page<Kardex> kardexPage = new PageImpl<>(kardexList, pageable, kardexList.size());
        
        when(kardexQueryPort.findProductId(productId, pageable, startDate, endDate)).thenReturn(kardexPage);
        when(kardexResponseMapper.toDtoResponse(kardex1)).thenReturn(response1);
        
        // Act
        ResponseDto<Page<KardexDtoResponse>> response = kardexQueryController.getKardexByProductId(productId, filterDto, pageable);
        
        // Assert
        assertNotNull(response);
        assertEquals(200, response.getStatus());
        assertEquals(1, response.getData().getTotalElements());
        
        verify(kardexQueryPort).findProductId(productId, pageable, startDate, endDate);
    }
    
    @Test
    @DisplayName("Should return empty page when no kardex records found")
    void testGetKardexByProductIdWithNoRecords() {
        // Arrange
        Long productId = 999L;
        Page<Kardex> emptyPage = new PageImpl<>(Arrays.asList(), pageable, 0);
        
        when(kardexQueryPort.findProductId(productId, pageable, null, null)).thenReturn(emptyPage);
        
        // Act
        ResponseDto<Page<KardexDtoResponse>> response = kardexQueryController.getKardexByProductId(productId, filterDto, pageable);
        
        // Assert
        assertNotNull(response);
        assertEquals(200, response.getStatus());
        assertEquals(0, response.getData().getTotalElements());
        assertTrue(response.getData().getContent().isEmpty());
        
        verify(kardexQueryPort).findProductId(productId, pageable, null, null);
        verify(kardexResponseMapper, never()).toDtoResponse(any());
    }
    
    @Test
    @DisplayName("Should handle pagination correctly")
    void testGetKardexByProductIdWithPagination() {
        // Arrange
        Long productId = 100L;
        Pageable customPageable = PageRequest.of(1, 5);
        List<Kardex> kardexList = Arrays.asList(kardex1);
        Page<Kardex> kardexPage = new PageImpl<>(kardexList, customPageable, 10);
        
        when(kardexQueryPort.findProductId(productId, customPageable, null, null)).thenReturn(kardexPage);
        when(kardexResponseMapper.toDtoResponse(kardex1)).thenReturn(response1);
        
        // Act
        ResponseDto<Page<KardexDtoResponse>> response = kardexQueryController.getKardexByProductId(productId, filterDto, customPageable);
        
        // Assert
        assertNotNull(response);
        assertEquals(10, response.getData().getTotalElements());
        assertEquals(1, response.getData().getContent().size());
        assertEquals(1, response.getData().getNumber());
        
        verify(kardexQueryPort).findProductId(productId, customPageable, null, null);
    }
    
    @Test
    @DisplayName("Should map kardex entities to response DTOs correctly")
    void testGetKardexByProductIdMapping() {
        // Arrange
        Long productId = 100L;
        List<Kardex> kardexList = Arrays.asList(kardex1, kardex2);
        Page<Kardex> kardexPage = new PageImpl<>(kardexList, pageable, kardexList.size());
        
        when(kardexQueryPort.findProductId(productId, pageable, null, null)).thenReturn(kardexPage);
        when(kardexResponseMapper.toDtoResponse(kardex1)).thenReturn(response1);
        when(kardexResponseMapper.toDtoResponse(kardex2)).thenReturn(response2);
        
        // Act
        ResponseDto<Page<KardexDtoResponse>> response = kardexQueryController.getKardexByProductId(productId, filterDto, pageable);
        
        // Assert
        List<KardexDtoResponse> content = response.getData().getContent();
        assertEquals(2, content.size());
        assertEquals(1L, content.get(0).getId());
        assertEquals(2L, content.get(1).getId());
        assertEquals(MovementType.PURCHASE, content.get(0).getType());
        assertEquals(MovementType.SALE, content.get(1).getType());
    }
    
    @Test
    @DisplayName("Should handle only start date in filter")
    void testGetKardexByProductIdWithOnlyStartDate() {
        // Arrange
        Long productId = 100L;
        LocalDate startDate = LocalDate.now().minusDays(7);
        filterDto.setStartDate(startDate);
        filterDto.setEndDate(null);
        
        List<Kardex> kardexList = Arrays.asList(kardex1);
        Page<Kardex> kardexPage = new PageImpl<>(kardexList, pageable, kardexList.size());
        
        when(kardexQueryPort.findProductId(productId, pageable, startDate, null)).thenReturn(kardexPage);
        when(kardexResponseMapper.toDtoResponse(kardex1)).thenReturn(response1);
        
        // Act
        ResponseDto<Page<KardexDtoResponse>> response = kardexQueryController.getKardexByProductId(productId, filterDto, pageable);
        
        // Assert
        assertNotNull(response);
        verify(kardexQueryPort).findProductId(productId, pageable, startDate, null);
    }
    
    @Test
    @DisplayName("Should verify response structure")
    void testGetKardexByProductIdResponseStructure() {
        // Arrange
        Long productId = 100L;
        Page<Kardex> kardexPage = new PageImpl<>(Arrays.asList(kardex1), pageable, 1);
        
        when(kardexQueryPort.findProductId(productId, pageable, null, null)).thenReturn(kardexPage);
        when(kardexResponseMapper.toDtoResponse(kardex1)).thenReturn(response1);
        
        // Act
        ResponseDto<Page<KardexDtoResponse>> response = kardexQueryController.getKardexByProductId(productId, filterDto, pageable);
        
        // Assert
        assertNotNull(response.getData());
        assertNotNull(response.getMessage());
        assertTrue(response.getStatus() > 0);
        assertTrue(response.getData() instanceof Page);
    }
}
