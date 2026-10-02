package co.unicauca.edu.co.contables.accounting.catalogue.unit.catalogue.infraestructure.adapters.input.rest.controller;

import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.application.input.*;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.domain.enums.ImportStatus;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.domain.models.AccountCatalogue;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.domain.models.ExportJobStatus;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.domain.models.ImportJobStatus;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.infraestructure.adapters.input.rest.controller.AccountCatalogueController;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.infraestructure.adapters.input.rest.dto.request.AccountCatalogueCreateReq;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.infraestructure.adapters.input.rest.dto.request.AccountCatalogueExportRequest;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.infraestructure.adapters.input.rest.dto.request.AccountCatalogueUpdateReq;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.infraestructure.adapters.input.rest.dto.response.*;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.infraestructure.adapters.input.rest.mapper.*;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.infraestructure.utils.AccountCatalogueExcelFileNameGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AccountCatalogueControllerUnitTest {

    @Mock
    private IAccountCatalogueCreateInputPort accountCatalogueCreateInputPort;

    @Mock
    private IAccountCreateRestMapper accountCreateRestMapper;

    @Mock
    private IAccountSearchRestMapper accountSearchRestMapper;

    @Mock
    private IAccountCatalogueSearchInputPort accountCatalogueSearchInputPort;

    @Mock
    private IItemAccountSearchRestMapper itemAccountSearchRestMapper;

    @Mock
    private IAccountUpdateRestMapper accountUpdateRestMapper;

    @Mock
    private IAccountCatalogueDeleteInputPort accountCatalogueDeleteInputPort;

    @Mock
    private IAccountCatalogueUpdateInputPort accountCatalogueUpdateInputPort;

    @Mock
    private IAccountCatalogueChangeStateInputPort accountCatalogueChangeStateInputPort;

    @Mock
    private IAccountChangeStateRestMapper accountChangeStateRestMapper;

    @Mock
    private IAuxiliaryAccountRestMapper auxiliaryAccountRestMapper;

    @Mock
    private IAccountCatalogueExportInputPort accountCatalogueExportInputPort;

    @Mock
    private IAccountCatalogueImportInputPort accountCatalogueImportInputPort;

    @Mock
    private AccountCatalogueExcelFileNameGenerator fileNameGenerator;

    @InjectMocks
    private AccountCatalogueController controller;

    private static final String ENTERPRISE_ID = "ENT001";
    private static final Long ACCOUNT_ID = 1L;
    private static final String CODE = "11050101";

    private AccountCatalogue accountCatalogue;
    private AccountCatalogueCreateReq createReq;
    private AccountCatalogueCreateRes createRes;
    private AccountCatalogueUpdateReq updateReq;
    private AccountCatalogueUpdateRes updateRes;
    private ItemAccountCatalogueSearchRes itemSearchRes;
    private AccountCatalogueListRes listRes;

    @BeforeEach
    void setUp() {
        accountCatalogue = new AccountCatalogue();
        accountCatalogue.setId(ACCOUNT_ID);
        accountCatalogue.setCode(CODE);
        accountCatalogue.setDescription("Caja General");
        accountCatalogue.setIdEnterprise(ENTERPRISE_ID);
        accountCatalogue.setStatus(true);

        createReq = AccountCatalogueCreateReq.builder()
                .code(CODE)
                .description("Caja General")
                .nature("DEBITO")
                .financialStatus("BALANCE")
                .classification("CURRENTASSETS")
                .idEnterprise(ENTERPRISE_ID)
                .build();

        createRes = AccountCatalogueCreateRes.builder()
                .id(ACCOUNT_ID)
                .code(CODE)
                .description("Caja General")
                .build();

        updateReq = AccountCatalogueUpdateReq.builder()
                .code(CODE)
                .description("Caja Actualizada")
                .idEnterprise(ENTERPRISE_ID)
                .build();

        updateRes = AccountCatalogueUpdateRes.builder()
                .id(ACCOUNT_ID)
                .code(CODE)
                .description("Caja Actualizada")
                .build();

        itemSearchRes = ItemAccountCatalogueSearchRes.builder()
                .id(ACCOUNT_ID)
                .code(CODE)
                .description("Caja General")
                .build();

        listRes = AccountCatalogueListRes.builder()
                .id(ACCOUNT_ID)
                .code(CODE)
                .description("Caja General")
                .children(Collections.emptyList())
                .build();
    }

    // ========== Tests de createAccountCatalogue ==========

    @Test
    @DisplayName("Debe crear cuenta contable exitosamente sin padre")
    void testCreateAccountCatalogueWithoutParent() {
        // Arrange
        createReq.setParent(null);
        when(accountCreateRestMapper.toDomain(createReq, null)).thenReturn(accountCatalogue);
        when(accountCatalogueCreateInputPort.createAccountCatalogue(accountCatalogue)).thenReturn(accountCatalogue);
        when(accountCreateRestMapper.toCreateResponse(accountCatalogue)).thenReturn(createRes);

        // Act
        ResponseEntity<AccountCatalogueCreateRes> response = controller.createAccountCatalogue(createReq);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(ACCOUNT_ID, response.getBody().getId());
    }

    @Test
    @DisplayName("Debe crear cuenta contable exitosamente con padre")
    void testCreateAccountCatalogueWithParent() {
        // Arrange
        Long parentId = 10L;
        createReq.setParent(parentId);
        AccountCatalogue parentAccount = new AccountCatalogue();
        parentAccount.setId(parentId);
        parentAccount.setCode("1105");

        when(accountCatalogueSearchInputPort.getAccountCatalogueById(parentId, ENTERPRISE_ID))
                .thenReturn(parentAccount);
        when(accountCreateRestMapper.toDomain(createReq, parentAccount)).thenReturn(accountCatalogue);
        when(accountCatalogueCreateInputPort.createAccountCatalogue(accountCatalogue)).thenReturn(accountCatalogue);
        when(accountCreateRestMapper.toCreateResponse(accountCatalogue)).thenReturn(createRes);

        // Act
        ResponseEntity<AccountCatalogueCreateRes> response = controller.createAccountCatalogue(createReq);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(accountCatalogueSearchInputPort).getAccountCatalogueById(parentId, ENTERPRISE_ID);
    }

    @Test
    @DisplayName("Debe invocar el servicio de creación correctamente")
    void testCreateAccountCatalogueCallsService() {
        // Arrange
        createReq.setParent(null);
        when(accountCreateRestMapper.toDomain(createReq, null)).thenReturn(accountCatalogue);
        when(accountCatalogueCreateInputPort.createAccountCatalogue(accountCatalogue)).thenReturn(accountCatalogue);
        when(accountCreateRestMapper.toCreateResponse(accountCatalogue)).thenReturn(createRes);

        // Act
        controller.createAccountCatalogue(createReq);

        // Assert
        verify(accountCatalogueCreateInputPort, times(1)).createAccountCatalogue(accountCatalogue);
    }

    @Test
    @DisplayName("Debe usar el mapper para convertir request a dominio")
    void testCreateAccountCatalogueUsesMapper() {
        // Arrange
        createReq.setParent(null);
        when(accountCreateRestMapper.toDomain(createReq, null)).thenReturn(accountCatalogue);
        when(accountCatalogueCreateInputPort.createAccountCatalogue(accountCatalogue)).thenReturn(accountCatalogue);
        when(accountCreateRestMapper.toCreateResponse(accountCatalogue)).thenReturn(createRes);

        // Act
        controller.createAccountCatalogue(createReq);

        // Assert
        verify(accountCreateRestMapper).toDomain(createReq, null);
        verify(accountCreateRestMapper).toCreateResponse(accountCatalogue);
    }

    // ========== Tests de updateAccountCatalogue ==========

    @Test
    @DisplayName("Debe actualizar cuenta contable exitosamente")
    void testUpdateAccountCatalogueSuccessfully() {
        // Arrange
        when(accountUpdateRestMapper.toDomain(updateReq)).thenReturn(accountCatalogue);
        when(accountCatalogueUpdateInputPort.updateAccountCatalogue(eq(ACCOUNT_ID), any()))
                .thenReturn(accountCatalogue);
        when(accountUpdateRestMapper.toUpdateResponse(accountCatalogue)).thenReturn(updateRes);

        // Act
        ResponseEntity<AccountCatalogueUpdateRes> response = controller.updateAccountCatalogue(ACCOUNT_ID.intValue(), updateReq);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(ACCOUNT_ID, response.getBody().getId());
    }

    @Test
    @DisplayName("Debe invocar el servicio de actualización con el ID correcto")
    void testUpdateAccountCataloguePassesCorrectId() {
        // Arrange
        long idToUpdate = 5L;
        when(accountUpdateRestMapper.toDomain(updateReq)).thenReturn(accountCatalogue);
        when(accountCatalogueUpdateInputPort.updateAccountCatalogue(eq(idToUpdate), any())).thenReturn(accountCatalogue);
        when(accountUpdateRestMapper.toUpdateResponse(accountCatalogue)).thenReturn(updateRes);

        // Act
        controller.updateAccountCatalogue((int) idToUpdate, updateReq);

        // Assert
        verify(accountCatalogueUpdateInputPort).updateAccountCatalogue(eq(idToUpdate), any());
    }

    @Test
    @DisplayName("Debe usar el mapper para convertir request a dominio en actualización")
    void testUpdateAccountCatalogueUsesMapper() {
        // Arrange
        when(accountUpdateRestMapper.toDomain(updateReq)).thenReturn(accountCatalogue);
        when(accountCatalogueUpdateInputPort.updateAccountCatalogue(anyLong(), any())).thenReturn(accountCatalogue);
        when(accountUpdateRestMapper.toUpdateResponse(any())).thenReturn(updateRes);

        // Act
        controller.updateAccountCatalogue(ACCOUNT_ID.intValue(), updateReq);

        // Assert
        verify(accountUpdateRestMapper).toDomain(updateReq);
        verify(accountUpdateRestMapper).toUpdateResponse(any());
    }

    // ========== Tests de getAccountCatalogue ==========

    @Test
    @DisplayName("Debe obtener cuenta por código exitosamente")
    void testGetAccountCatalogueByCodeSuccessfully() {
        // Arrange
        when(accountCatalogueSearchInputPort.getAccountCatalogueByCode(CODE, ENTERPRISE_ID))
                .thenReturn(accountCatalogue);
        when(itemAccountSearchRestMapper.toItemAccountCatalogueSearch(accountCatalogue)).thenReturn(itemSearchRes);

        // Act
        ResponseEntity<ItemAccountCatalogueSearchRes> response = controller.getAccountCatalogue(CODE, ENTERPRISE_ID);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(CODE, response.getBody().getCode());
    }

    @Test
    @DisplayName("Debe usar el código y empresa correctos para buscar")
    void testGetAccountCatalogueUsesCorrectParams() {
        // Arrange
        String testCode = "22010101";
        String testEnterprise = "ENT999";
        when(accountCatalogueSearchInputPort.getAccountCatalogueByCode(testCode, testEnterprise))
                .thenReturn(accountCatalogue);
        when(itemAccountSearchRestMapper.toItemAccountCatalogueSearch(accountCatalogue)).thenReturn(itemSearchRes);

        // Act
        controller.getAccountCatalogue(testCode, testEnterprise);

        // Assert
        verify(accountCatalogueSearchInputPort).getAccountCatalogueByCode(testCode, testEnterprise);
    }

    // ========== Tests de deleteByCode ==========

    @Test
    @DisplayName("Debe eliminar cuenta y retornar NO_CONTENT")
    void testDeleteByCodeSuccessfully() {
        // Arrange
        doNothing().when(accountCatalogueDeleteInputPort).deleteById(ACCOUNT_ID, ENTERPRISE_ID);

        // Act
        ResponseEntity<Void> response = controller.deleteByCode(ACCOUNT_ID, ENTERPRISE_ID);

        // Assert
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    @DisplayName("Debe invocar el servicio de eliminación con parámetros correctos")
    void testDeleteByCodeCallsService() {
        // Arrange
        Long idToDelete = 100L;
        String enterpriseId = "ENT555";
        doNothing().when(accountCatalogueDeleteInputPort).deleteById(idToDelete, enterpriseId);

        // Act
        controller.deleteByCode(idToDelete, enterpriseId);

        // Assert
        verify(accountCatalogueDeleteInputPort, times(1)).deleteById(idToDelete, enterpriseId);
    }

    // ========== Tests de getAccountCatalogueTree ==========

    @Test
    @DisplayName("Debe obtener árbol de cuentas exitosamente")
    void testGetAccountCatalogueTreeSuccessfully() {
        // Arrange
        when(accountCatalogueSearchInputPort.getAccountCatalogueTree(CODE, ENTERPRISE_ID))
                .thenReturn(accountCatalogue);
        when(accountSearchRestMapper.toAccountCatalogueListRes(accountCatalogue)).thenReturn(listRes);

        // Act
        ResponseEntity<AccountCatalogueListRes> response = controller.getAccountCatalogueTree(CODE, ENTERPRISE_ID);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(CODE, response.getBody().getCode());
    }

    @Test
    @DisplayName("Debe usar mapper para convertir árbol a respuesta")
    void testGetAccountCatalogueTreeUsesMapper() {
        // Arrange
        when(accountCatalogueSearchInputPort.getAccountCatalogueTree(CODE, ENTERPRISE_ID))
                .thenReturn(accountCatalogue);
        when(accountSearchRestMapper.toAccountCatalogueListRes(accountCatalogue)).thenReturn(listRes);

        // Act
        controller.getAccountCatalogueTree(CODE, ENTERPRISE_ID);

        // Assert
        verify(accountSearchRestMapper).toAccountCatalogueListRes(accountCatalogue);
    }

    // ========== Tests de searchAccountCatalogues ==========

    @Test
    @DisplayName("Debe buscar cuentas por término de búsqueda")
    void testSearchAccountCataloguesWithSearchTerm() {
        // Arrange
        String searchTerm = "Caja";
        List<AccountCatalogue> accounts = Arrays.asList(accountCatalogue);
        when(accountCatalogueSearchInputPort.getAccountsByCodeOrDescription(ENTERPRISE_ID, searchTerm))
                .thenReturn(accounts);
        when(itemAccountSearchRestMapper.toItemAccountCatalogueSearch(accountCatalogue)).thenReturn(itemSearchRes);

        // Act
        ResponseEntity<List<ItemAccountCatalogueSearchRes>> response = 
                controller.searchAccountCatalogues(ENTERPRISE_ID, searchTerm);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
    }

    @Test
    @DisplayName("Debe retornar todas las cuentas cuando search es null")
    void testSearchAccountCataloguesWithNullSearch() {
        // Arrange
        List<AccountCatalogue> accounts = Arrays.asList(accountCatalogue);
        when(accountCatalogueSearchInputPort.getAllAccountsByEnterprise(ENTERPRISE_ID)).thenReturn(accounts);
        when(itemAccountSearchRestMapper.toItemAccountCatalogueSearch(accountCatalogue)).thenReturn(itemSearchRes);

        // Act
        ResponseEntity<List<ItemAccountCatalogueSearchRes>> response = 
                controller.searchAccountCatalogues(ENTERPRISE_ID, null);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(accountCatalogueSearchInputPort).getAllAccountsByEnterprise(ENTERPRISE_ID);
        verify(accountCatalogueSearchInputPort, never()).getAccountsByCodeOrDescription(anyString(), anyString());
    }

    @Test
    @DisplayName("Debe retornar todas las cuentas cuando search es vacío")
    void testSearchAccountCataloguesWithEmptySearch() {
        // Arrange
        List<AccountCatalogue> accounts = Arrays.asList(accountCatalogue);
        when(accountCatalogueSearchInputPort.getAllAccountsByEnterprise(ENTERPRISE_ID)).thenReturn(accounts);
        when(itemAccountSearchRestMapper.toItemAccountCatalogueSearch(accountCatalogue)).thenReturn(itemSearchRes);

        // Act
        controller.searchAccountCatalogues(ENTERPRISE_ID, "");

        // Assert
        verify(accountCatalogueSearchInputPort).getAllAccountsByEnterprise(ENTERPRISE_ID);
    }

    @Test
    @DisplayName("Debe retornar todas las cuentas cuando search es solo espacios")
    void testSearchAccountCataloguesWithBlankSearch() {
        // Arrange
        List<AccountCatalogue> accounts = Arrays.asList(accountCatalogue);
        when(accountCatalogueSearchInputPort.getAllAccountsByEnterprise(ENTERPRISE_ID)).thenReturn(accounts);
        when(itemAccountSearchRestMapper.toItemAccountCatalogueSearch(accountCatalogue)).thenReturn(itemSearchRes);

        // Act
        controller.searchAccountCatalogues(ENTERPRISE_ID, "   ");

        // Assert
        verify(accountCatalogueSearchInputPort).getAllAccountsByEnterprise(ENTERPRISE_ID);
    }

    @Test
    @DisplayName("Debe retornar lista vacía cuando no hay resultados")
    void testSearchAccountCataloguesReturnsEmptyList() {
        // Arrange
        when(accountCatalogueSearchInputPort.getAccountsByCodeOrDescription(ENTERPRISE_ID, "inexistente"))
                .thenReturn(Collections.emptyList());

        // Act
        ResponseEntity<List<ItemAccountCatalogueSearchRes>> response = 
                controller.searchAccountCatalogues(ENTERPRISE_ID, "inexistente");

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isEmpty());
    }

    // ========== Tests de getAccountCatalogueTrees ==========

    @Test
    @DisplayName("Debe obtener lista de árboles de cuentas")
    void testGetAccountCatalogueTreesSuccessfully() {
        // Arrange
        List<AccountCatalogue> trees = Arrays.asList(accountCatalogue);
        when(accountCatalogueSearchInputPort.getAccountCatalogueTrees(ENTERPRISE_ID)).thenReturn(trees);
        when(accountSearchRestMapper.toAccountCatalogueListRes(accountCatalogue)).thenReturn(listRes);

        // Act
        ResponseEntity<List<AccountCatalogueListRes>> response = controller.getAccountCatalogueTrees(ENTERPRISE_ID);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
    }

    @Test
    @DisplayName("Debe mapear cada árbol a respuesta")
    void testGetAccountCatalogueTreesMapsEachTree() {
        // Arrange
        AccountCatalogue tree2 = new AccountCatalogue();
        tree2.setId(2L);
        tree2.setCode("2");
        List<AccountCatalogue> trees = Arrays.asList(accountCatalogue, tree2);
        when(accountCatalogueSearchInputPort.getAccountCatalogueTrees(ENTERPRISE_ID)).thenReturn(trees);
        when(accountSearchRestMapper.toAccountCatalogueListRes(any())).thenReturn(listRes);

        // Act
        controller.getAccountCatalogueTrees(ENTERPRISE_ID);

        // Assert
        verify(accountSearchRestMapper, times(2)).toAccountCatalogueListRes(any());
    }

    // ========== Tests de changeState ==========

    @Test
    @DisplayName("Debe cambiar estado de cuenta exitosamente")
    void testChangeStateSuccessfully() {
        // Arrange
        Boolean newStatus = false;
        AccountCatalogueChangeStateRes changeStateRes = AccountCatalogueChangeStateRes.builder()
                .id(ACCOUNT_ID)
                .status(newStatus)
                .build();
        when(accountCatalogueChangeStateInputPort.changeState(ACCOUNT_ID, ENTERPRISE_ID, newStatus))
                .thenReturn(accountCatalogue);
        when(accountChangeStateRestMapper.toChangeStateResponse(accountCatalogue)).thenReturn(changeStateRes);

        // Act
        ResponseEntity<AccountCatalogueChangeStateRes> response = 
                controller.changeState(ACCOUNT_ID, ENTERPRISE_ID, newStatus);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    @DisplayName("Debe pasar parámetros correctos al servicio de cambio de estado")
    void testChangeStatePassesCorrectParams() {
        // Arrange
        Long id = 50L;
        String entId = "ENT777";
        Boolean status = true;
        AccountCatalogueChangeStateRes changeStateRes = new AccountCatalogueChangeStateRes();
        when(accountCatalogueChangeStateInputPort.changeState(id, entId, status)).thenReturn(accountCatalogue);
        when(accountChangeStateRestMapper.toChangeStateResponse(accountCatalogue)).thenReturn(changeStateRes);

        // Act
        controller.changeState(id, entId, status);

        // Assert
        verify(accountCatalogueChangeStateInputPort).changeState(id, entId, status);
    }

    // ========== Tests de getAuxiliaryAccounts ==========

    @Test
    @DisplayName("Debe obtener cuentas auxiliares exitosamente")
    void testGetAuxiliaryAccountsSuccessfully() {
        // Arrange
        List<AccountCatalogue> auxiliaryAccounts = Arrays.asList(accountCatalogue);
        AuxiliaryAccountListRes auxiliaryRes = AuxiliaryAccountListRes.builder()
                .auxiliaryAccounts(Arrays.asList(itemSearchRes))
                .totalCount(1)
                .idEnterprise(ENTERPRISE_ID)
                .build();
        when(accountCatalogueSearchInputPort.getAuxiliaryAccounts(ENTERPRISE_ID)).thenReturn(auxiliaryAccounts);
        when(auxiliaryAccountRestMapper.toAuxiliaryAccountListRes(auxiliaryAccounts, ENTERPRISE_ID))
                .thenReturn(auxiliaryRes);

        // Act
        ResponseEntity<AuxiliaryAccountListRes> response = controller.getAuxiliaryAccounts(ENTERPRISE_ID);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getTotalCount());
    }

    // ========== Tests de getAuxiliaryAccountsWithCrossing ==========

    @Test
    @DisplayName("Debe obtener cuentas auxiliares con cruce exitosamente")
    void testGetAuxiliaryAccountsWithCrossingSuccessfully() {
        // Arrange
        List<AccountCatalogue> crossingAccounts = Arrays.asList(accountCatalogue);
        AuxiliaryAccountListRes auxiliaryRes = AuxiliaryAccountListRes.builder()
                .auxiliaryAccounts(Arrays.asList(itemSearchRes))
                .totalCount(1)
                .idEnterprise(ENTERPRISE_ID)
                .build();
        when(accountCatalogueSearchInputPort.getAuxiliaryAccountsWithCrossing(ENTERPRISE_ID))
                .thenReturn(crossingAccounts);
        when(auxiliaryAccountRestMapper.toAuxiliaryAccountListRes(crossingAccounts, ENTERPRISE_ID))
                .thenReturn(auxiliaryRes);

        // Act
        ResponseEntity<AuxiliaryAccountListRes> response = controller.getAuxiliaryAccountsWithCrossing(ENTERPRISE_ID);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    // ========== Tests de exportAccountCatalogueTemplate ==========

    @Test
    @DisplayName("Debe exportar plantilla Excel exitosamente")
    void testExportAccountCatalogueTemplateSuccessfully() {
        // Arrange
        Resource templateResource = new ByteArrayResource("test content".getBytes());
        String fileName = "Plantilla_Catalogo_Cuentas_20231127.xlsx";
        when(accountCatalogueExportInputPort.exportAccountCatalogueTemplate(ENTERPRISE_ID))
                .thenReturn(templateResource);
        when(fileNameGenerator.generateTemplateFileName()).thenReturn(fileName);

        // Act
        ResponseEntity<Resource> response = controller.exportAccountCatalogueTemplate(ENTERPRISE_ID);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getHeaders().getContentDisposition().toString().contains(fileName));
    }

    @Test
    @DisplayName("Debe retornar content type correcto para Excel")
    void testExportAccountCatalogueTemplateContentType() {
        // Arrange
        Resource templateResource = new ByteArrayResource("test content".getBytes());
        when(accountCatalogueExportInputPort.exportAccountCatalogueTemplate(ENTERPRISE_ID))
                .thenReturn(templateResource);
        when(fileNameGenerator.generateTemplateFileName()).thenReturn("template.xlsx");

        // Act
        ResponseEntity<Resource> response = controller.exportAccountCatalogueTemplate(ENTERPRISE_ID);

        // Assert
        assertTrue(response.getHeaders().getContentType().toString()
                .contains("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
    }

    // ========== Tests de exportAccountCatalogueAsync ==========

    @Test
    @DisplayName("Debe iniciar exportación asíncrona exitosamente")
    void testExportAccountCatalogueAsyncSuccessfully() {
        // Arrange
        String jobId = "job-12345";
        when(accountCatalogueExportInputPort.exportAccountCatalogueAsync(any(AccountCatalogueExportRequest.class)))
                .thenReturn(jobId);

        // Act
        ResponseEntity<Map<String, String>> response = 
                controller.exportAccountCatalogueAsync(ENTERPRISE_ID, "CompanyName", true);

        // Assert
        assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(jobId, response.getBody().get("jobId"));
    }

    @Test
    @DisplayName("Debe incluir endpoint de estado en respuesta de exportación")
    void testExportAccountCatalogueAsyncIncludesStatusEndpoint() {
        // Arrange
        String jobId = "job-67890";
        when(accountCatalogueExportInputPort.exportAccountCatalogueAsync(any())).thenReturn(jobId);

        // Act
        ResponseEntity<Map<String, String>> response = 
                controller.exportAccountCatalogueAsync(ENTERPRISE_ID, null, null);

        // Assert
        assertTrue(response.getBody().get("statusEndpoint").contains(jobId));
    }

    @Test
    @DisplayName("Debe construir request de exportación correctamente")
    void testExportAccountCatalogueAsyncBuildsRequest() {
        // Arrange
        String companyName = "Mi Empresa";
        Boolean status = false;
        when(accountCatalogueExportInputPort.exportAccountCatalogueAsync(any())).thenReturn("job-id");

        // Act
        controller.exportAccountCatalogueAsync(ENTERPRISE_ID, companyName, status);

        // Assert
        verify(accountCatalogueExportInputPort).exportAccountCatalogueAsync(argThat(request -> 
                request.getEntId().equals(ENTERPRISE_ID) &&
                request.getCompanyName().equals(companyName) &&
                request.getStatus().equals(status)
        ));
    }

    // ========== Tests de getExportStatus ==========

    @Test
    @DisplayName("Debe retornar estado de exportación cuando existe")
    void testGetExportStatusWhenExists() {
        // Arrange
        String jobId = "job-status";
        ExportJobStatus status = ExportJobStatus.builder()
                .jobId(jobId)
                .status(ImportStatus.PROCESSING)
                .build();
        when(accountCatalogueExportInputPort.getExportStatus(jobId)).thenReturn(Optional.of(status));

        // Act
        ResponseEntity<?> response = controller.getExportStatus(jobId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    @DisplayName("Debe retornar NOT_FOUND cuando no existe estado de exportación")
    void testGetExportStatusWhenNotExists() {
        // Arrange
        String jobId = "nonexistent-job";
        when(accountCatalogueExportInputPort.getExportStatus(jobId)).thenReturn(Optional.empty());

        // Act
        ResponseEntity<?> response = controller.getExportStatus(jobId);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    // ========== Tests de downloadExportedFile ==========

    @Test
    @DisplayName("Debe descargar archivo exportado exitosamente")
    void testDownloadExportedFileSuccessfully() {
        // Arrange
        String jobId = "job-download";
        byte[] fileData = "Excel content".getBytes();
        ExportJobStatus status = ExportJobStatus.builder()
                .jobId(jobId)
                .status(ImportStatus.COMPLETED)
                .fileData(fileData)
                .fileName("export.xlsx")
                .build();
        when(accountCatalogueExportInputPort.getExportStatus(jobId)).thenReturn(Optional.of(status));

        // Act
        ResponseEntity<Resource> response = controller.downloadExportedFile(jobId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    @DisplayName("Debe retornar NOT_FOUND cuando job no existe para descarga")
    void testDownloadExportedFileNotFound() {
        // Arrange
        when(accountCatalogueExportInputPort.getExportStatus("nonexistent")).thenReturn(Optional.empty());

        // Act
        ResponseEntity<Resource> response = controller.downloadExportedFile("nonexistent");

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    @DisplayName("Debe retornar BAD_REQUEST cuando exportación no está completada")
    void testDownloadExportedFileNotCompleted() {
        // Arrange
        String jobId = "job-processing";
        ExportJobStatus status = ExportJobStatus.builder()
                .jobId(jobId)
                .status(ImportStatus.PROCESSING)
                .build();
        when(accountCatalogueExportInputPort.getExportStatus(jobId)).thenReturn(Optional.of(status));

        // Act
        ResponseEntity<Resource> response = controller.downloadExportedFile(jobId);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    @DisplayName("Debe retornar INTERNAL_SERVER_ERROR cuando fileData es null")
    void testDownloadExportedFileWithNullFileData() {
        // Arrange
        String jobId = "job-no-data";
        ExportJobStatus status = ExportJobStatus.builder()
                .jobId(jobId)
                .status(ImportStatus.COMPLETED)
                .fileData(null)
                .build();
        when(accountCatalogueExportInputPort.getExportStatus(jobId)).thenReturn(Optional.of(status));

        // Act
        ResponseEntity<Resource> response = controller.downloadExportedFile(jobId);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    @DisplayName("Debe incluir nombre de archivo en header de descarga")
    void testDownloadExportedFileIncludesFileName() {
        // Arrange
        String jobId = "job-filename";
        String fileName = "Catalogo_Export.xlsx";
        ExportJobStatus status = ExportJobStatus.builder()
                .jobId(jobId)
                .status(ImportStatus.COMPLETED)
                .fileData("data".getBytes())
                .fileName(fileName)
                .build();
        when(accountCatalogueExportInputPort.getExportStatus(jobId)).thenReturn(Optional.of(status));

        // Act
        ResponseEntity<Resource> response = controller.downloadExportedFile(jobId);

        // Assert
        assertTrue(response.getHeaders().getContentDisposition().toString().contains(fileName));
    }

    // ========== Tests de importFromExcel ==========

    @Test
    @DisplayName("Debe iniciar importación desde Excel exitosamente")
    void testImportFromExcelSuccessfully() {
        // Arrange
        String jobId = "import-job-123";
        MultipartFile file = new MockMultipartFile("file", "test.xlsx", 
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", 
                "test content".getBytes());
        when(accountCatalogueImportInputPort.importAccountCatalogueAsync(any())).thenReturn(jobId);

        // Act
        ResponseEntity<Map<String, String>> response = controller.importFromExcel(ENTERPRISE_ID, file);

        // Assert
        assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(jobId, response.getBody().get("jobId"));
    }

    @Test
    @DisplayName("Debe incluir mensaje de confirmación en respuesta de importación")
    void testImportFromExcelIncludesMessage() {
        // Arrange
        MultipartFile file = new MockMultipartFile("file", "test.xlsx", 
                "application/octet-stream", "content".getBytes());
        when(accountCatalogueImportInputPort.importAccountCatalogueAsync(any())).thenReturn("job-id");

        // Act
        ResponseEntity<Map<String, String>> response = controller.importFromExcel(ENTERPRISE_ID, file);

        // Assert
        assertNotNull(response.getBody().get("message"));
        assertTrue(response.getBody().get("message").contains("Importación iniciada"));
    }

    @Test
    @DisplayName("Debe incluir endpoint de estado en respuesta de importación")
    void testImportFromExcelIncludesStatusEndpoint() {
        // Arrange
        String jobId = "import-status-job";
        MultipartFile file = new MockMultipartFile("file", "test.xlsx", 
                "application/octet-stream", "content".getBytes());
        when(accountCatalogueImportInputPort.importAccountCatalogueAsync(any())).thenReturn(jobId);

        // Act
        ResponseEntity<Map<String, String>> response = controller.importFromExcel(ENTERPRISE_ID, file);

        // Assert
        assertTrue(response.getBody().get("statusEndpoint").contains(jobId));
    }

    // ========== Tests de getImportStatus ==========

    @Test
    @DisplayName("Debe retornar estado de importación cuando existe")
    void testGetImportStatusWhenExists() {
        // Arrange
        String jobId = "import-status";
        ImportJobStatus status = ImportJobStatus.builder()
                .jobId(jobId)
                .status(ImportStatus.COMPLETED)
                .build();
        when(accountCatalogueImportInputPort.getImportStatus(jobId)).thenReturn(Optional.of(status));

        // Act
        ResponseEntity<?> response = controller.getImportStatus(jobId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    @DisplayName("Debe retornar NOT_FOUND cuando no existe estado de importación")
    void testGetImportStatusWhenNotExists() {
        // Arrange
        String jobId = "nonexistent-import";
        when(accountCatalogueImportInputPort.getImportStatus(jobId)).thenReturn(Optional.empty());

        // Act
        ResponseEntity<?> response = controller.getImportStatus(jobId);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    // ========== Tests de múltiples cuentas ==========

    @Test
    @DisplayName("Debe mapear múltiples cuentas en búsqueda")
    void testSearchMapsMultipleAccounts() {
        // Arrange
        AccountCatalogue account2 = new AccountCatalogue();
        account2.setId(2L);
        account2.setCode("22010101");
        List<AccountCatalogue> accounts = Arrays.asList(accountCatalogue, account2);
        
        ItemAccountCatalogueSearchRes itemRes2 = ItemAccountCatalogueSearchRes.builder()
                .id(2L)
                .code("22010101")
                .build();
        
        when(accountCatalogueSearchInputPort.getAccountsByCodeOrDescription(ENTERPRISE_ID, "test"))
                .thenReturn(accounts);
        when(itemAccountSearchRestMapper.toItemAccountCatalogueSearch(accountCatalogue)).thenReturn(itemSearchRes);
        when(itemAccountSearchRestMapper.toItemAccountCatalogueSearch(account2)).thenReturn(itemRes2);

        // Act
        ResponseEntity<List<ItemAccountCatalogueSearchRes>> response = 
                controller.searchAccountCatalogues(ENTERPRISE_ID, "test");

        // Assert
        assertEquals(2, response.getBody().size());
        verify(itemAccountSearchRestMapper, times(2)).toItemAccountCatalogueSearch(any());
    }

    // ========== Tests de validación de estados de exportación ==========

    @Test
    @DisplayName("Debe retornar BAD_REQUEST cuando estado es PENDING")
    void testDownloadExportedFileWithPendingStatus() {
        // Arrange
        ExportJobStatus status = ExportJobStatus.builder()
                .status(ImportStatus.PENDING)
                .build();
        when(accountCatalogueExportInputPort.getExportStatus("job")).thenReturn(Optional.of(status));

        // Act
        ResponseEntity<Resource> response = controller.downloadExportedFile("job");

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    @DisplayName("Debe retornar BAD_REQUEST cuando estado es FAILED")
    void testDownloadExportedFileWithFailedStatus() {
        // Arrange
        ExportJobStatus status = ExportJobStatus.builder()
                .status(ImportStatus.FAILED)
                .build();
        when(accountCatalogueExportInputPort.getExportStatus("job")).thenReturn(Optional.of(status));

        // Act
        ResponseEntity<Resource> response = controller.downloadExportedFile("job");

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }
}
