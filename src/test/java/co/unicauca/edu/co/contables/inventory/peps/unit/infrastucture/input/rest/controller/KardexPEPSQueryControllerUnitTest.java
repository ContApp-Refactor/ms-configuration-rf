package co.unicauca.edu.co.contables.inventory.peps.unit.infrastucture.input.rest.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.ArrayList;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import co.unicauca.edu.co.contables.inventory.peps.application.ports.input.IKardexQueryPort;
import co.unicauca.edu.co.contables.inventory.peps.domain.model.Kardex;
import co.unicauca.edu.co.contables.inventory.peps.domain.model.KardexMigration;
import co.unicauca.edu.co.contables.inventory.peps.domain.model.KardexReport;
import co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.input.rest.controller.KardexPEPSQueryController;
import co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.input.rest.dto.ResponseDTO;
import co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.input.rest.dto.request.KardexByDateDTORequest;
import co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.input.rest.dto.response.KardexAvailableQuantityDTOResponse;
import co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.input.rest.dto.response.KardexRecordsDTOResponse;
import co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.input.rest.dto.response.ListLastProductKardexDtoResponse;
import co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.input.rest.dto.response.PageResponseDTO;
import co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.input.rest.mapper.IKardexRestMapper;

/**
 * @brief Unit tests for KardexPEPSQueryController
 * 
 * Tests the REST controller for kardex PEPS query operations.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("KardexPEPSQueryController Tests")
public class KardexPEPSQueryControllerUnitTest {
    
    @Mock
    private IKardexQueryPort kardexQueryPort;
    
    @Mock
    private IKardexRestMapper kardexRestMapper;
    
    @InjectMocks
    private KardexPEPSQueryController kardexPEPSQueryController;
    
    private Long productId;
    private String enterpriseId;
    private KardexByDateDTORequest dateRequest;
    private Pageable pageable;
    private Page<KardexReport> kardexReportPage;
    private Page<KardexRecordsDTOResponse> kardexRecordsResponsePage;
    private List<Kardex> kardexList;
    private List<KardexAvailableQuantityDTOResponse> availableQuantityResponseList;
    private List<KardexMigration> kardexMigrationList;
    private List<ListLastProductKardexDtoResponse> lastProductKardexResponseList;
    
    @BeforeEach
    void setUp() {
        productId = 1L;
        enterpriseId = "ENT-001";
        
        // Setup date request
        dateRequest = new KardexByDateDTORequest();
        dateRequest.setStartDate(LocalDate.of(2024, 1, 1));
        dateRequest.setEndDate(LocalDate.of(2024, 12, 31));
        
        // Setup pageable
        pageable = PageRequest.of(0, 10);
        
        // Setup kardex report page
        List<KardexReport> reports = new ArrayList<>();
        KardexReport report = new KardexReport();
        reports.add(report);
        kardexReportPage = new PageImpl<>(reports, pageable, reports.size());
        
        // Setup kardex records response page
        List<KardexRecordsDTOResponse> records = new ArrayList<>();
        KardexRecordsDTOResponse record = new KardexRecordsDTOResponse();
        records.add(record);
        kardexRecordsResponsePage = new PageImpl<>(records, pageable, records.size());
        
        // Setup kardex list
        kardexList = new ArrayList<>();
        Kardex kardex = Kardex.builder().idKardex(1L).build();
        kardexList.add(kardex);
        
        // Setup available quantity response list
        availableQuantityResponseList = new ArrayList<>();
        KardexAvailableQuantityDTOResponse availableQuantity = new KardexAvailableQuantityDTOResponse();
        availableQuantityResponseList.add(availableQuantity);
        
        // Setup kardex migration list
        kardexMigrationList = new ArrayList<>();
        KardexMigration migration = new KardexMigration();
        kardexMigrationList.add(migration);
        
        // Setup last product kardex response list
        lastProductKardexResponseList = new ArrayList<>();
        ListLastProductKardexDtoResponse lastProduct = new ListLastProductKardexDtoResponse();
        lastProductKardexResponseList.add(lastProduct);
    }
    
    @Test
    @DisplayName("Should retrieve kardex list by date successfully")
    void testGetKardexListPEPSByDate_Success() {
        // Arrange
        when(kardexQueryPort.getRecordsKardexByProduct(
            eq(productId), 
            eq(dateRequest.getStartDate()), 
            eq(dateRequest.getEndDate()), 
            eq(pageable)
        )).thenReturn(kardexReportPage);
        
        when(kardexRestMapper.toDTORecord(any(KardexReport.class)))
            .thenAnswer(invocation -> {
                KardexRecordsDTOResponse response = new KardexRecordsDTOResponse();
                return response;
            });
        
        // Act
        ResponseDTO<PageResponseDTO<KardexRecordsDTOResponse>> response = 
            kardexPEPSQueryController.getKardexListPEPSByDate(productId, dateRequest, pageable);
        
        // Assert
        assertNotNull(response);
        assertEquals(200, response.getStatus());
        assertEquals("Kardex records retrieved successfully", response.getMessage());
        assertNotNull(response.getData());
        assertEquals(1, response.getData().getContent().size());
        
        verify(kardexQueryPort, times(1)).getRecordsKardexByProduct(
            productId, 
            dateRequest.getStartDate(), 
            dateRequest.getEndDate(), 
            pageable
        );
    }
    
    @Test
    @DisplayName("Should retrieve available quantity successfully")
    void testGetAvailable_Success() {
        // Arrange
        when(kardexQueryPort.getKardexAvailableQuantityByProduct(productId)).thenReturn(kardexList);
        when(kardexRestMapper.toKardexDTO(kardexList)).thenReturn(availableQuantityResponseList);
        
        // Act
        ResponseEntity<ResponseDTO<List<KardexAvailableQuantityDTOResponse>>> response = 
            kardexPEPSQueryController.getAvailable(productId);
        
        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        
        ResponseDTO<List<KardexAvailableQuantityDTOResponse>> body = response.getBody();
        assertNotNull(body);
        assertEquals(200, body.getStatus());
        assertEquals("kardex purchase registered sucesfully", body.getMessage());
        assertNotNull(body.getData());
        assertEquals(1, body.getData().size());
        
        verify(kardexQueryPort, times(1)).getKardexAvailableQuantityByProduct(productId);
        verify(kardexRestMapper, times(1)).toKardexDTO(kardexList);
    }
    
    @Test
    @DisplayName("Should retrieve last kardex for all products successfully")
    void testGetLastKardexForAllProducts_Success() {
        // Arrange
        when(kardexQueryPort.findLastKardexForAllProducts(enterpriseId)).thenReturn(kardexMigrationList);
        when(kardexRestMapper.toListLastProductKardexDtoResponseList(kardexMigrationList))
            .thenReturn(lastProductKardexResponseList);
        
        // Act
        ResponseDTO<List<ListLastProductKardexDtoResponse>> response = 
            kardexPEPSQueryController.getLastKardexForAllProducts(enterpriseId);
        
        // Assert
        assertNotNull(response);
        assertEquals(200, response.getStatus());
        assertEquals("Last kardex records for all products retrieved successfully", response.getMessage());
        assertNotNull(response.getData());
        assertEquals(1, response.getData().size());
        
        verify(kardexQueryPort, times(1)).findLastKardexForAllProducts(enterpriseId);
        verify(kardexRestMapper, times(1)).toListLastProductKardexDtoResponseList(kardexMigrationList);
    }
    
    @Test
    @DisplayName("Should handle empty kardex list by date")
    void testGetKardexListPEPSByDate_EmptyList() {
        // Arrange
        Page<KardexReport> emptyPage = new PageImpl<>(new ArrayList<>(), pageable, 0);
        when(kardexQueryPort.getRecordsKardexByProduct(
            eq(productId), 
            any(LocalDate.class), 
            any(LocalDate.class), 
            eq(pageable)
        )).thenReturn(emptyPage);
        
        // Act
        ResponseDTO<PageResponseDTO<KardexRecordsDTOResponse>> response = 
            kardexPEPSQueryController.getKardexListPEPSByDate(productId, dateRequest, pageable);
        
        // Assert
        assertNotNull(response);
        assertEquals(200, response.getStatus());
        assertNotNull(response.getData());
        assertEquals(0, response.getData().getContent().size());
        assertEquals(0, response.getData().getTotalElements());
    }
    
    @Test
    @DisplayName("Should handle empty available quantity list")
    void testGetAvailable_EmptyList() {
        // Arrange
        when(kardexQueryPort.getKardexAvailableQuantityByProduct(productId)).thenReturn(new ArrayList<>());
        when(kardexRestMapper.toKardexDTO(any())).thenReturn(new ArrayList<>());
        
        // Act
        ResponseEntity<ResponseDTO<List<KardexAvailableQuantityDTOResponse>>> response = 
            kardexPEPSQueryController.getAvailable(productId);
        
        // Assert
        assertNotNull(response);
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getData());
        assertEquals(0, response.getBody().getData().size());
    }
    
    @Test
    @DisplayName("Should handle empty last kardex list")
    void testGetLastKardexForAllProducts_EmptyList() {
        // Arrange
        when(kardexQueryPort.findLastKardexForAllProducts(enterpriseId)).thenReturn(new ArrayList<>());
        when(kardexRestMapper.toListLastProductKardexDtoResponseList(any())).thenReturn(new ArrayList<>());
        
        // Act
        ResponseDTO<List<ListLastProductKardexDtoResponse>> response = 
            kardexPEPSQueryController.getLastKardexForAllProducts(enterpriseId);
        
        // Assert
        assertNotNull(response);
        assertNotNull(response.getData());
        assertEquals(0, response.getData().size());
    }
    
    @Test
    @DisplayName("Should handle multiple kardex reports in page")
    void testGetKardexListPEPSByDate_MultipleRecords() {
        // Arrange
        List<KardexReport> reports = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            reports.add(new KardexReport());
        }
        Page<KardexReport> multiPage = new PageImpl<>(reports, pageable, reports.size());
        
        when(kardexQueryPort.getRecordsKardexByProduct(
            eq(productId), 
            any(LocalDate.class), 
            any(LocalDate.class), 
            eq(pageable)
        )).thenReturn(multiPage);
        
        when(kardexRestMapper.toDTORecord(any(KardexReport.class)))
            .thenAnswer(invocation -> new KardexRecordsDTOResponse());
        
        // Act
        ResponseDTO<PageResponseDTO<KardexRecordsDTOResponse>> response = 
            kardexPEPSQueryController.getKardexListPEPSByDate(productId, dateRequest, pageable);
        
        // Assert
        assertNotNull(response);
        assertEquals(5, response.getData().getContent().size());
        assertEquals(5, response.getData().getTotalElements());
    }
    
    @Test
    @DisplayName("Should handle different page sizes")
    void testGetKardexListPEPSByDate_DifferentPageSize() {
        // Arrange
        Pageable largePageable = PageRequest.of(0, 50);
        Page<KardexReport> largePage = new PageImpl<>(new ArrayList<>(), largePageable, 0);
        
        when(kardexQueryPort.getRecordsKardexByProduct(
            eq(productId), 
            any(LocalDate.class), 
            any(LocalDate.class), 
            eq(largePageable)
        )).thenReturn(largePage);
        
        // Act
        ResponseDTO<PageResponseDTO<KardexRecordsDTOResponse>> response = 
            kardexPEPSQueryController.getKardexListPEPSByDate(productId, dateRequest, largePageable);
        
        // Assert
        assertNotNull(response);
        assertEquals(50, response.getData().getPageSize());
    }
    
    @Test
    @DisplayName("Should handle different product IDs for kardex list")
    void testGetKardexListPEPSByDate_DifferentProductIds() {
        // Arrange
        Long differentProductId = 999L;
        when(kardexQueryPort.getRecordsKardexByProduct(
            eq(differentProductId), 
            any(LocalDate.class), 
            any(LocalDate.class), 
            eq(pageable)
        )).thenReturn(kardexReportPage);
        
        when(kardexRestMapper.toDTORecord(any(KardexReport.class)))
            .thenReturn(new KardexRecordsDTOResponse());
        
        // Act
        ResponseDTO<PageResponseDTO<KardexRecordsDTOResponse>> response = 
            kardexPEPSQueryController.getKardexListPEPSByDate(differentProductId, dateRequest, pageable);
        
        // Assert
        assertNotNull(response);
        verify(kardexQueryPort, times(1)).getRecordsKardexByProduct(
            differentProductId, 
            dateRequest.getStartDate(), 
            dateRequest.getEndDate(), 
            pageable
        );
    }
    
    @Test
    @DisplayName("Should handle different product IDs for available quantity")
    void testGetAvailable_DifferentProductIds() {
        // Arrange
        Long differentProductId = 999L;
        when(kardexQueryPort.getKardexAvailableQuantityByProduct(differentProductId)).thenReturn(kardexList);
        when(kardexRestMapper.toKardexDTO(any())).thenReturn(availableQuantityResponseList);
        
        // Act
        ResponseEntity<ResponseDTO<List<KardexAvailableQuantityDTOResponse>>> response = 
            kardexPEPSQueryController.getAvailable(differentProductId);
        
        // Assert
        assertNotNull(response);
        verify(kardexQueryPort, times(1)).getKardexAvailableQuantityByProduct(differentProductId);
    }
    
    @Test
    @DisplayName("Should handle different enterprise IDs")
    void testGetLastKardexForAllProducts_DifferentEnterpriseIds() {
        // Arrange
        String differentEnterpriseId = "ENT-999";
        when(kardexQueryPort.findLastKardexForAllProducts(differentEnterpriseId)).thenReturn(kardexMigrationList);
        when(kardexRestMapper.toListLastProductKardexDtoResponseList(any())).thenReturn(lastProductKardexResponseList);
        
        // Act
        ResponseDTO<List<ListLastProductKardexDtoResponse>> response = 
            kardexPEPSQueryController.getLastKardexForAllProducts(differentEnterpriseId);
        
        // Assert
        assertNotNull(response);
        verify(kardexQueryPort, times(1)).findLastKardexForAllProducts(differentEnterpriseId);
    }
    
    @Test
    @DisplayName("Should handle different date ranges")
    void testGetKardexListPEPSByDate_DifferentDateRanges() {
        // Arrange
        KardexByDateDTORequest customDateRequest = new KardexByDateDTORequest();
        customDateRequest.setStartDate(LocalDate.of(2023, 6, 1));
        customDateRequest.setEndDate(LocalDate.of(2023, 6, 30));
        
        when(kardexQueryPort.getRecordsKardexByProduct(
            eq(productId), 
            eq(customDateRequest.getStartDate()), 
            eq(customDateRequest.getEndDate()), 
            eq(pageable)
        )).thenReturn(kardexReportPage);
        
        when(kardexRestMapper.toDTORecord(any(KardexReport.class)))
            .thenReturn(new KardexRecordsDTOResponse());
        
        // Act
        ResponseDTO<PageResponseDTO<KardexRecordsDTOResponse>> response = 
            kardexPEPSQueryController.getKardexListPEPSByDate(productId, customDateRequest, pageable);
        
        // Assert
        assertNotNull(response);
        verify(kardexQueryPort, times(1)).getRecordsKardexByProduct(
            productId, 
            LocalDate.of(2023, 6, 1), 
            LocalDate.of(2023, 6, 30), 
            pageable
        );
    }
    
    @Test
    @DisplayName("Should handle pagination with different page numbers")
    void testGetKardexListPEPSByDate_DifferentPageNumbers() {
        // Arrange
        Pageable secondPage = PageRequest.of(1, 10);
        Page<KardexReport> page2 = new PageImpl<>(new ArrayList<>(), secondPage, 0);
        
        when(kardexQueryPort.getRecordsKardexByProduct(
            eq(productId), 
            any(LocalDate.class), 
            any(LocalDate.class), 
            eq(secondPage)
        )).thenReturn(page2);
        
        // Act
        ResponseDTO<PageResponseDTO<KardexRecordsDTOResponse>> response = 
            kardexPEPSQueryController.getKardexListPEPSByDate(productId, dateRequest, secondPage);
        
        // Assert
        assertNotNull(response);
        assertEquals(1, response.getData().getPageNumber());
    }
    
    @Test
    @DisplayName("Should verify mapper is called for each kardex report")
    void testGetKardexListPEPSByDate_MapperVerification() {
        // Arrange
        List<KardexReport> reports = new ArrayList<>();
        reports.add(new KardexReport());
        reports.add(new KardexReport());
        reports.add(new KardexReport());
        Page<KardexReport> threePage = new PageImpl<>(reports, pageable, reports.size());
        
        when(kardexQueryPort.getRecordsKardexByProduct(
            eq(productId), 
            any(LocalDate.class), 
            any(LocalDate.class), 
            eq(pageable)
        )).thenReturn(threePage);
        
        when(kardexRestMapper.toDTORecord(any(KardexReport.class)))
            .thenReturn(new KardexRecordsDTOResponse());
        
        // Act
        kardexPEPSQueryController.getKardexListPEPSByDate(productId, dateRequest, pageable);
        
        // Assert
        verify(kardexRestMapper, times(3)).toDTORecord(any(KardexReport.class));
    }
    
    @Test
    @DisplayName("Should handle multiple available quantities")
    void testGetAvailable_MultipleQuantities() {
        // Arrange
        List<Kardex> multipleKardex = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            multipleKardex.add(Kardex.builder().idKardex((long) i).build());
        }
        
        List<KardexAvailableQuantityDTOResponse> multipleResponses = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            multipleResponses.add(new KardexAvailableQuantityDTOResponse());
        }
        
        when(kardexQueryPort.getKardexAvailableQuantityByProduct(productId)).thenReturn(multipleKardex);
        when(kardexRestMapper.toKardexDTO(multipleKardex)).thenReturn(multipleResponses);
        
        // Act
        ResponseEntity<ResponseDTO<List<KardexAvailableQuantityDTOResponse>>> response = 
            kardexPEPSQueryController.getAvailable(productId);
        
        // Assert
        assertNotNull(response);
        assertEquals(3, response.getBody().getData().size());
    }
    
    @Test
    @DisplayName("Should handle multiple last kardex records")
    void testGetLastKardexForAllProducts_MultipleProducts() {
        // Arrange
        List<KardexMigration> multipleMigrations = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            multipleMigrations.add(new KardexMigration());
        }
        
        List<ListLastProductKardexDtoResponse> multipleResponses = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            multipleResponses.add(new ListLastProductKardexDtoResponse());
        }
        
        when(kardexQueryPort.findLastKardexForAllProducts(enterpriseId)).thenReturn(multipleMigrations);
        when(kardexRestMapper.toListLastProductKardexDtoResponseList(multipleMigrations))
            .thenReturn(multipleResponses);
        
        // Act
        ResponseDTO<List<ListLastProductKardexDtoResponse>> response = 
            kardexPEPSQueryController.getLastKardexForAllProducts(enterpriseId);
        
        // Assert
        assertNotNull(response);
        assertEquals(5, response.getData().size());
    }
    
    @Test
    @DisplayName("Should return correct HTTP status for all query operations")
    void testAllOperations_HttpStatusVerification() {
        // Arrange
        when(kardexQueryPort.getRecordsKardexByProduct(any(), any(), any(), any())).thenReturn(kardexReportPage);
        when(kardexQueryPort.getKardexAvailableQuantityByProduct(any())).thenReturn(kardexList);
        when(kardexQueryPort.findLastKardexForAllProducts(any())).thenReturn(kardexMigrationList);
        when(kardexRestMapper.toDTORecord(any())).thenReturn(new KardexRecordsDTOResponse());
        when(kardexRestMapper.toKardexDTO(any())).thenReturn(availableQuantityResponseList);
        when(kardexRestMapper.toListLastProductKardexDtoResponseList(any())).thenReturn(lastProductKardexResponseList);
        
        // Act
        ResponseDTO<PageResponseDTO<KardexRecordsDTOResponse>> listResponse = 
            kardexPEPSQueryController.getKardexListPEPSByDate(productId, dateRequest, pageable);
        ResponseEntity<ResponseDTO<List<KardexAvailableQuantityDTOResponse>>> availableResponse = 
            kardexPEPSQueryController.getAvailable(productId);
        ResponseDTO<List<ListLastProductKardexDtoResponse>> lastResponse = 
            kardexPEPSQueryController.getLastKardexForAllProducts(enterpriseId);
        
        // Assert
        assertEquals(200, listResponse.getStatus());
        assertEquals(HttpStatus.OK, availableResponse.getStatusCode());
        assertEquals(200, availableResponse.getBody().getStatus());
        assertEquals(200, lastResponse.getStatus());
    }
}

