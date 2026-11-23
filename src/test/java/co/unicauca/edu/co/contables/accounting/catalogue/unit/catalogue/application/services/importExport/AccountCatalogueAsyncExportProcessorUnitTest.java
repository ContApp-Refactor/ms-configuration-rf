package co.unicauca.edu.co.contables.accounting.catalogue.unit.catalogue.application.services.importExport;

import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.application.input.IAccountCatalogueSearchInputPort;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.application.services.importExport.AccountCatalogueAsyncExportProcessor;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.application.services.importExport.AccountCatalogueExportJobTracker;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.application.services.validation.AccountCatalogueExcelValidationService;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.domain.enums.ClassificationEnum;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.domain.enums.FinancialStatusEnum;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.domain.enums.ImportStatus;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.domain.enums.NatureEnum;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.domain.models.AccountCatalogue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AccountCatalogueAsyncExportProcessorUnitTest {

    @Mock
    private AccountCatalogueExportJobTracker jobTracker;

    @Mock
    private IAccountCatalogueSearchInputPort accountCatalogueSearchInputPort;

    @Mock
    private AccountCatalogueExcelValidationService excelValidationService;

    @InjectMocks
    private AccountCatalogueAsyncExportProcessor asyncExportProcessor;

    private String entId;
    private String jobId;
    private AccountCatalogue accountCatalogue;

    @BeforeEach
    void setUp() {
        entId = "ENT-001";
        jobId = "JOB-001";

        accountCatalogue = AccountCatalogue.builder()
                .id(1L)
                .idEnterprise(entId)
                .code("11050501")
                .description("Caja General")
                .nature(NatureEnum.DEBIT)
                .financialStatus(FinancialStatusEnum.STATEMENTFINANCIALPOSITION)
                .classification(ClassificationEnum.CURRENTASSETS)
                .crossing(false)
                .costCenter(false)
                .status(true)
                .build();
    }

    @Test
    @DisplayName("Debe procesar exportación exitosamente con datos")
    void testProcessExportAsyncSuccess() throws Exception {
        // Arrange
        Boolean status = null;
        List<AccountCatalogue> accounts = List.of(accountCatalogue);
        Page<AccountCatalogue> page = new PageImpl<>(accounts);
        when(accountCatalogueSearchInputPort.getAllAccountCataloguesForExport(eq(entId), eq(status), any(Pageable.class)))
            .thenReturn(page);
        doNothing().when(jobTracker).updateJobStatus(anyString(), any(ImportStatus.class));
        doNothing().when(jobTracker).updateProgress(anyString(), anyInt());
        doNothing().when(jobTracker).updateTotalRecords(anyString(), anyInt());
        doNothing().when(jobTracker).setFileData(anyString(), any(byte[].class));
        doNothing().when(excelValidationService).applyAccountCatalogueValidations(any(), anyInt(), anyInt());

        // Act
        asyncExportProcessor.processExportAsync(entId, status, jobId);

        // Assert
        verify(jobTracker).updateJobStatus(jobId, ImportStatus.PROCESSING);
        verify(jobTracker).updateTotalRecords(jobId, 1);
        verify(jobTracker).setFileData(eq(jobId), any(byte[].class));
        verify(jobTracker).updateJobStatus(jobId, ImportStatus.COMPLETED);
        verify(jobTracker).updateProgress(jobId, 100);
    }

    @Test
    @DisplayName("Debe manejar lista vacía y marcar como fallido")
    void testProcessExportAsyncHandlesEmptyData() {
        // Arrange
        Boolean status = true;
        Page<AccountCatalogue> emptyPage = new PageImpl<>(Collections.emptyList());
        when(accountCatalogueSearchInputPort.getAllAccountCataloguesForExport(eq(entId), eq(status), any(Pageable.class)))
            .thenReturn(emptyPage);
        doNothing().when(jobTracker).updateJobStatus(anyString(), any(ImportStatus.class));
        doNothing().when(jobTracker).updateProgress(anyString(), anyInt());
        doNothing().when(jobTracker).setErrorMessage(anyString(), anyString());

        // Act
        asyncExportProcessor.processExportAsync(entId, status, jobId);

        // Assert
        verify(jobTracker).setErrorMessage(eq(jobId), anyString());
        verify(jobTracker).updateJobStatus(jobId, ImportStatus.FAILED);
        verify(jobTracker, never()).setFileData(anyString(), any(byte[].class));
    }

    @Test
    @DisplayName("Debe manejar lista vacía con status null")
    void testProcessExportAsyncHandlesEmptyDataWithNullStatus() {
        // Arrange
        Boolean status = null;
        Page<AccountCatalogue> emptyPage = new PageImpl<>(Collections.emptyList());
        when(accountCatalogueSearchInputPort.getAllAccountCataloguesForExport(eq(entId), eq(status), any(Pageable.class)))
            .thenReturn(emptyPage);
        doNothing().when(jobTracker).updateJobStatus(anyString(), any(ImportStatus.class));
        doNothing().when(jobTracker).updateProgress(anyString(), anyInt());
        doNothing().when(jobTracker).setErrorMessage(anyString(), anyString());

        // Act
        asyncExportProcessor.processExportAsync(entId, status, jobId);

        // Assert
        verify(jobTracker).setErrorMessage(eq(jobId), anyString());
        verify(jobTracker).updateJobStatus(jobId, ImportStatus.FAILED);
    }

    @Test
    @DisplayName("Debe manejar lista vacía con status false")
    void testProcessExportAsyncHandlesEmptyDataWithFalseStatus() {
        // Arrange
        Boolean status = false;
        Page<AccountCatalogue> emptyPage = new PageImpl<>(Collections.emptyList());
        when(accountCatalogueSearchInputPort.getAllAccountCataloguesForExport(eq(entId), eq(status), any(Pageable.class)))
            .thenReturn(emptyPage);
        doNothing().when(jobTracker).updateJobStatus(anyString(), any(ImportStatus.class));
        doNothing().when(jobTracker).updateProgress(anyString(), anyInt());
        doNothing().when(jobTracker).setErrorMessage(anyString(), anyString());

        // Act
        asyncExportProcessor.processExportAsync(entId, status, jobId);

        // Assert
        verify(jobTracker).setErrorMessage(eq(jobId), anyString());
        verify(jobTracker).updateJobStatus(jobId, ImportStatus.FAILED);
    }

    @Test
    @DisplayName("Debe manejar excepción genérica y marcar como fallido")
    void testProcessExportAsyncHandlesGenericException() {
        // Arrange
        Boolean status = null;
        when(accountCatalogueSearchInputPort.getAllAccountCataloguesForExport(eq(entId), eq(status), any(Pageable.class)))
            .thenThrow(new RuntimeException("Error de conexión"));
        doNothing().when(jobTracker).updateJobStatus(anyString(), any(ImportStatus.class));
        doNothing().when(jobTracker).updateProgress(anyString(), anyInt());
        doNothing().when(jobTracker).setErrorMessage(anyString(), anyString());

        // Act
        asyncExportProcessor.processExportAsync(entId, status, jobId);

        // Assert
        verify(jobTracker).setErrorMessage(eq(jobId), contains("Error del sistema"));
        verify(jobTracker).updateJobStatus(jobId, ImportStatus.FAILED);
        verify(jobTracker).updateProgress(jobId, 100);
    }

    @Test
    @DisplayName("Debe procesar múltiples páginas de datos")
    void testProcessExportAsyncHandlesMultiplePages() throws Exception {
        // Arrange
        Boolean status = null;
        List<AccountCatalogue> accountsPage1 = List.of(accountCatalogue);
        AccountCatalogue accountCatalogue2 = AccountCatalogue.builder()
                .id(2L)
                .idEnterprise(entId)
                .code("11050502")
                .description("Caja Menor")
                .nature(NatureEnum.DEBIT)
                .financialStatus(FinancialStatusEnum.STATEMENTFINANCIALPOSITION)
                .classification(ClassificationEnum.CURRENTASSETS)
                .crossing(false)
                .costCenter(false)
                .status(true)
                .build();
        List<AccountCatalogue> accountsPage2 = List.of(accountCatalogue2);
        
        Page<AccountCatalogue> page1 = new PageImpl<>(accountsPage1, Pageable.ofSize(1000), 2000);
        Page<AccountCatalogue> page2 = new PageImpl<>(accountsPage2, Pageable.ofSize(1000).withPage(1), 2000);
        
        when(accountCatalogueSearchInputPort.getAllAccountCataloguesForExport(eq(entId), eq(status), any(Pageable.class)))
            .thenReturn(page1)
            .thenReturn(page2)
            .thenReturn(new PageImpl<>(Collections.emptyList()));
        doNothing().when(jobTracker).updateJobStatus(anyString(), any(ImportStatus.class));
        doNothing().when(jobTracker).updateProgress(anyString(), anyInt());
        doNothing().when(jobTracker).updateTotalRecords(anyString(), anyInt());
        doNothing().when(jobTracker).setFileData(anyString(), any(byte[].class));
        doNothing().when(excelValidationService).applyAccountCatalogueValidations(any(), anyInt(), anyInt());

        // Act
        asyncExportProcessor.processExportAsync(entId, status, jobId);

        // Assert
        verify(accountCatalogueSearchInputPort, atLeast(2)).getAllAccountCataloguesForExport(eq(entId), eq(status), any(Pageable.class));
        verify(jobTracker).updateJobStatus(jobId, ImportStatus.COMPLETED);
    }

    @Test
    @DisplayName("Debe actualizar progreso correctamente durante la exportación")
    void testProcessExportAsyncUpdatesProgress() throws Exception {
        // Arrange
        Boolean status = null;
        List<AccountCatalogue> accounts = List.of(accountCatalogue);
        Page<AccountCatalogue> page = new PageImpl<>(accounts);
        when(accountCatalogueSearchInputPort.getAllAccountCataloguesForExport(eq(entId), eq(status), any(Pageable.class)))
            .thenReturn(page);
        doNothing().when(jobTracker).updateJobStatus(anyString(), any(ImportStatus.class));
        doNothing().when(jobTracker).updateProgress(anyString(), anyInt());
        doNothing().when(jobTracker).updateTotalRecords(anyString(), anyInt());
        doNothing().when(jobTracker).setFileData(anyString(), any(byte[].class));
        doNothing().when(excelValidationService).applyAccountCatalogueValidations(any(), anyInt(), anyInt());

        // Act
        asyncExportProcessor.processExportAsync(entId, status, jobId);

        // Assert
        verify(jobTracker).updateProgress(jobId, 10);
        verify(jobTracker).updateProgress(jobId, 50);
        verify(jobTracker).updateProgress(jobId, 90);
        verify(jobTracker).updateProgress(jobId, 100);
    }

    @Test
    @DisplayName("Debe generar Excel con campos booleanos correctamente")
    void testProcessExportAsyncWithBooleanFields() throws Exception {
        // Arrange
        Boolean status = null;
        AccountCatalogue accountWithBooleans = AccountCatalogue.builder()
                .id(1L)
                .idEnterprise(entId)
                .code("11050501")
                .description("Caja General")
                .nature(NatureEnum.DEBIT)
                .financialStatus(FinancialStatusEnum.STATEMENTFINANCIALPOSITION)
                .classification(ClassificationEnum.CURRENTASSETS)
                .crossing(true)
                .costCenter(true)
                .status(true)
                .build();
        List<AccountCatalogue> accounts = List.of(accountWithBooleans);
        Page<AccountCatalogue> page = new PageImpl<>(accounts);
        when(accountCatalogueSearchInputPort.getAllAccountCataloguesForExport(eq(entId), eq(status), any(Pageable.class)))
            .thenReturn(page);
        doNothing().when(jobTracker).updateJobStatus(anyString(), any(ImportStatus.class));
        doNothing().when(jobTracker).updateProgress(anyString(), anyInt());
        doNothing().when(jobTracker).updateTotalRecords(anyString(), anyInt());
        doNothing().when(jobTracker).setFileData(anyString(), any(byte[].class));
        doNothing().when(excelValidationService).applyAccountCatalogueValidations(any(), anyInt(), anyInt());

        // Act
        asyncExportProcessor.processExportAsync(entId, status, jobId);

        // Assert
        verify(jobTracker).setFileData(eq(jobId), any(byte[].class));
        verify(jobTracker).updateJobStatus(jobId, ImportStatus.COMPLETED);
    }

    @Test
    @DisplayName("Debe generar Excel con campos booleanos null")
    void testProcessExportAsyncWithNullBooleanFields() throws Exception {
        // Arrange
        Boolean status = null;
        AccountCatalogue accountWithNullBooleans = AccountCatalogue.builder()
                .id(1L)
                .idEnterprise(entId)
                .code("11050501")
                .description("Caja General")
                .nature(NatureEnum.DEBIT)
                .financialStatus(FinancialStatusEnum.STATEMENTFINANCIALPOSITION)
                .classification(ClassificationEnum.CURRENTASSETS)
                .crossing(null)
                .costCenter(null)
                .status(true)
                .build();
        List<AccountCatalogue> accounts = List.of(accountWithNullBooleans);
        Page<AccountCatalogue> page = new PageImpl<>(accounts);
        when(accountCatalogueSearchInputPort.getAllAccountCataloguesForExport(eq(entId), eq(status), any(Pageable.class)))
            .thenReturn(page);
        doNothing().when(jobTracker).updateJobStatus(anyString(), any(ImportStatus.class));
        doNothing().when(jobTracker).updateProgress(anyString(), anyInt());
        doNothing().when(jobTracker).updateTotalRecords(anyString(), anyInt());
        doNothing().when(jobTracker).setFileData(anyString(), any(byte[].class));
        doNothing().when(excelValidationService).applyAccountCatalogueValidations(any(), anyInt(), anyInt());

        // Act
        asyncExportProcessor.processExportAsync(entId, status, jobId);

        // Assert
        verify(jobTracker).setFileData(eq(jobId), any(byte[].class));
        verify(jobTracker).updateJobStatus(jobId, ImportStatus.COMPLETED);
    }

    @Test
    @DisplayName("Debe omitir validaciones para exportaciones grandes mayores a 3000 registros")
    void testProcessExportAsyncSkipsValidationsForLargeExports() throws Exception {
        // Arrange
        Boolean status = null;
        List<AccountCatalogue> largeAccountList = new ArrayList<>();
        for (int i = 0; i < 3500; i++) {
            largeAccountList.add(AccountCatalogue.builder()
                    .id((long) i)
                    .idEnterprise(entId)
                    .code(String.format("%08d", i))
                    .description("Cuenta " + i)
                    .nature(NatureEnum.DEBIT)
                    .financialStatus(FinancialStatusEnum.STATEMENTFINANCIALPOSITION)
                    .classification(ClassificationEnum.CURRENTASSETS)
                    .crossing(false)
                    .costCenter(false)
                    .status(true)
                    .build());
        }
        Page<AccountCatalogue> page = new PageImpl<>(largeAccountList);
        when(accountCatalogueSearchInputPort.getAllAccountCataloguesForExport(eq(entId), eq(status), any(Pageable.class)))
            .thenReturn(page);
        doNothing().when(jobTracker).updateJobStatus(anyString(), any(ImportStatus.class));
        doNothing().when(jobTracker).updateProgress(anyString(), anyInt());
        doNothing().when(jobTracker).updateTotalRecords(anyString(), anyInt());
        doNothing().when(jobTracker).setFileData(anyString(), any(byte[].class));

        // Act
        asyncExportProcessor.processExportAsync(entId, status, jobId);

        // Assert
        verify(excelValidationService, never()).applyAccountCatalogueValidations(any(), anyInt(), anyInt());
        verify(jobTracker).updateJobStatus(jobId, ImportStatus.COMPLETED);
    }

    @Test
    @DisplayName("Debe aplicar validaciones para exportaciones medianas entre 1000 y 3000 registros")
    void testProcessExportAsyncAppliesValidationsForMediumExports() throws Exception {
        // Arrange
        Boolean status = null;
        List<AccountCatalogue> mediumAccountList = new ArrayList<>();
        for (int i = 0; i < 1500; i++) {
            mediumAccountList.add(AccountCatalogue.builder()
                    .id((long) i)
                    .idEnterprise(entId)
                    .code(String.format("%08d", i))
                    .description("Cuenta " + i)
                    .nature(NatureEnum.DEBIT)
                    .financialStatus(FinancialStatusEnum.STATEMENTFINANCIALPOSITION)
                    .classification(ClassificationEnum.CURRENTASSETS)
                    .crossing(false)
                    .costCenter(false)
                    .status(true)
                    .build());
        }
        Page<AccountCatalogue> page = new PageImpl<>(mediumAccountList);
        when(accountCatalogueSearchInputPort.getAllAccountCataloguesForExport(eq(entId), eq(status), any(Pageable.class)))
            .thenReturn(page);
        doNothing().when(jobTracker).updateJobStatus(anyString(), any(ImportStatus.class));
        doNothing().when(jobTracker).updateProgress(anyString(), anyInt());
        doNothing().when(jobTracker).updateTotalRecords(anyString(), anyInt());
        doNothing().when(jobTracker).setFileData(anyString(), any(byte[].class));
        doNothing().when(excelValidationService).applyAccountCatalogueValidations(any(), anyInt(), anyInt());

        // Act
        asyncExportProcessor.processExportAsync(entId, status, jobId);

        // Assert
        verify(excelValidationService).applyAccountCatalogueValidations(any(), eq(1), eq(1550));
        verify(jobTracker).updateJobStatus(jobId, ImportStatus.COMPLETED);
    }

    @Test
    @DisplayName("Debe aplicar validaciones para exportaciones pequeñas menores a 1000 registros")
    void testProcessExportAsyncAppliesValidationsForSmallExports() throws Exception {
        // Arrange
        Boolean status = null;
        List<AccountCatalogue> smallAccountList = new ArrayList<>();
        for (int i = 0; i < 500; i++) {
            smallAccountList.add(AccountCatalogue.builder()
                    .id((long) i)
                    .idEnterprise(entId)
                    .code(String.format("%08d", i))
                    .description("Cuenta " + i)
                    .nature(NatureEnum.DEBIT)
                    .financialStatus(FinancialStatusEnum.STATEMENTFINANCIALPOSITION)
                    .classification(ClassificationEnum.CURRENTASSETS)
                    .crossing(false)
                    .costCenter(false)
                    .status(true)
                    .build());
        }
        Page<AccountCatalogue> page = new PageImpl<>(smallAccountList);
        when(accountCatalogueSearchInputPort.getAllAccountCataloguesForExport(eq(entId), eq(status), any(Pageable.class)))
            .thenReturn(page);
        doNothing().when(jobTracker).updateJobStatus(anyString(), any(ImportStatus.class));
        doNothing().when(jobTracker).updateProgress(anyString(), anyInt());
        doNothing().when(jobTracker).updateTotalRecords(anyString(), anyInt());
        doNothing().when(jobTracker).setFileData(anyString(), any(byte[].class));
        doNothing().when(excelValidationService).applyAccountCatalogueValidations(any(), anyInt(), anyInt());

        // Act
        asyncExportProcessor.processExportAsync(entId, status, jobId);

        // Assert
        verify(excelValidationService).applyAccountCatalogueValidations(any(), eq(1), eq(600));
        verify(jobTracker).updateJobStatus(jobId, ImportStatus.COMPLETED);
    }

    @Test
    @DisplayName("Debe filtrar solo cuentas activas cuando status es true")
    void testProcessExportAsyncFiltersActiveAccounts() throws Exception {
        // Arrange
        Boolean status = true;
        List<AccountCatalogue> accounts = List.of(accountCatalogue);
        Page<AccountCatalogue> page = new PageImpl<>(accounts);
        when(accountCatalogueSearchInputPort.getAllAccountCataloguesForExport(eq(entId), eq(true), any(Pageable.class)))
            .thenReturn(page);
        doNothing().when(jobTracker).updateJobStatus(anyString(), any(ImportStatus.class));
        doNothing().when(jobTracker).updateProgress(anyString(), anyInt());
        doNothing().when(jobTracker).updateTotalRecords(anyString(), anyInt());
        doNothing().when(jobTracker).setFileData(anyString(), any(byte[].class));
        doNothing().when(excelValidationService).applyAccountCatalogueValidations(any(), anyInt(), anyInt());

        // Act
        asyncExportProcessor.processExportAsync(entId, status, jobId);

        // Assert
        verify(accountCatalogueSearchInputPort).getAllAccountCataloguesForExport(eq(entId), eq(true), any(Pageable.class));
        verify(jobTracker).updateJobStatus(jobId, ImportStatus.COMPLETED);
    }

    @Test
    @DisplayName("Debe filtrar solo cuentas inactivas cuando status es false")
    void testProcessExportAsyncFiltersInactiveAccounts() throws Exception {
        // Arrange
        Boolean status = false;
        AccountCatalogue inactiveAccount = AccountCatalogue.builder()
                .id(1L)
                .idEnterprise(entId)
                .code("11050501")
                .description("Caja General")
                .nature(NatureEnum.DEBIT)
                .financialStatus(FinancialStatusEnum.STATEMENTFINANCIALPOSITION)
                .classification(ClassificationEnum.CURRENTASSETS)
                .status(false)
                .build();
        List<AccountCatalogue> accounts = List.of(inactiveAccount);
        Page<AccountCatalogue> page = new PageImpl<>(accounts);
        when(accountCatalogueSearchInputPort.getAllAccountCataloguesForExport(eq(entId), eq(false), any(Pageable.class)))
            .thenReturn(page);
        doNothing().when(jobTracker).updateJobStatus(anyString(), any(ImportStatus.class));
        doNothing().when(jobTracker).updateProgress(anyString(), anyInt());
        doNothing().when(jobTracker).updateTotalRecords(anyString(), anyInt());
        doNothing().when(jobTracker).setFileData(anyString(), any(byte[].class));
        doNothing().when(excelValidationService).applyAccountCatalogueValidations(any(), anyInt(), anyInt());

        // Act
        asyncExportProcessor.processExportAsync(entId, status, jobId);

        // Assert
        verify(accountCatalogueSearchInputPort).getAllAccountCataloguesForExport(eq(entId), eq(false), any(Pageable.class));
        verify(jobTracker).updateJobStatus(jobId, ImportStatus.COMPLETED);
    }

    @Test
    @DisplayName("Debe manejar página null en la paginación")
    void testProcessExportAsyncHandlesNullPage() {
        // Arrange
        Boolean status = null;
        when(accountCatalogueSearchInputPort.getAllAccountCataloguesForExport(eq(entId), eq(status), any(Pageable.class)))
            .thenReturn(null);
        doNothing().when(jobTracker).updateJobStatus(anyString(), any(ImportStatus.class));
        doNothing().when(jobTracker).updateProgress(anyString(), anyInt());
        doNothing().when(jobTracker).setErrorMessage(anyString(), anyString());

        // Act
        asyncExportProcessor.processExportAsync(entId, status, jobId);

        // Assert
        verify(jobTracker).setErrorMessage(eq(jobId), anyString());
        verify(jobTracker).updateJobStatus(jobId, ImportStatus.FAILED);
    }
}
