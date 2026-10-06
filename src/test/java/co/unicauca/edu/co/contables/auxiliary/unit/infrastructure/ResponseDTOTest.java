package co.unicauca.edu.co.contables.auxiliary.unit.infrastructure;

import co.unicauca.edu.co.contables.auxiliary.application.useCase.export.ExportAuxiliaryBookUC;
import co.unicauca.edu.co.contables.auxiliary.application.useCase.export.report.ReportGenerator;
import co.unicauca.edu.co.contables.auxiliary.application.useCase.export.report.builders.ReportColumnsBuilder;
import co.unicauca.edu.co.contables.auxiliary.domain.models.core.AuxiliaryBook;
import co.unicauca.edu.co.contables.auxiliary.domain.models.core.export.ExportInfo;
import co.unicauca.edu.co.contables.auxiliary.domain.models.enums.EAuxiliaryBookFormat;
import co.unicauca.edu.co.contables.auxiliary.domain.models.enums.EAuxiliaryBookType;
import co.unicauca.edu.co.contables.auxiliary.domain.models.enums.EState;
import co.unicauca.edu.co.contables.auxiliary.domain.models.enums.ETypeEvent;
import co.unicauca.edu.co.contables.auxiliary.domain.models.history.AuxiliaryBookHistory;
import co.unicauca.edu.co.contables.auxiliary.domain.models.log.AuxiliaryBookLog;
import co.unicauca.edu.co.contables.auxiliary.domain.ports.AuxiliaryBookHistory.IAuxiliaryBookHistoryCommandRepositoryPort;
import co.unicauca.edu.co.contables.auxiliary.domain.ports.AuxiliaryBookHistory.IAuxiliaryBookHistoryQueryRepositoryPort;
import co.unicauca.edu.co.contables.auxiliary.domain.ports.AuxiliaryBookLog.IAuxiliaryBookLogCommandRepositoryPort;
import co.unicauca.edu.co.contables.auxiliary.domain.ports.IFormatterResultOutputPort;
import co.unicauca.edu.co.contables.auxiliary.domain.ports.IMessageServicePort;
import co.unicauca.edu.co.contables.auxiliary.infrastructure.in.rest.dto.response.ResponseDTO;
import net.sf.dynamicreports.jasper.builder.JasperReportBuilder;
import net.sf.dynamicreports.report.base.column.DRColumn;
import net.sf.dynamicreports.report.builder.DynamicReports;
import net.sf.dynamicreports.report.exception.DRException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.io.OutputStream;
import java.util.List;

/**
 * @brief Pruebas unitarias para {@link ResponseDTO}, {@link ReportColumnsBuilder}
 * y {@link ExportAuxiliaryBookUC}.
 */
@ExtendWith(MockitoExtension.class)
class ResponseDTOTest {

    @Mock
    private ReportGenerator reportGenerator;

    @Mock
    private IAuxiliaryBookLogCommandRepositoryPort auxiliaryBookLogCommandRepositoryPort;

    @Mock
    private IAuxiliaryBookHistoryQueryRepositoryPort auxiliaryBookHistoryQueryRepositoryPort;

    @Mock
    private IAuxiliaryBookHistoryCommandRepositoryPort auxiliaryBookHistoryCommandRepositoryPort;

    @Mock
    private IFormatterResultOutputPort formatterResultOutputPort;

    @Mock
    private IMessageServicePort messageServicePort;

    @InjectMocks
    private ExportAuxiliaryBookUC exportAuxiliaryBookUC;

    @Test
    @DisplayName("of() debe construir un ResponseEntity con el statusCode y payload")
    void ofBuildsResponseEntity() {
        ResponseDTO<String> dto = ResponseDTO.<String>builder()
                .data("payload")
                .statusCode(HttpStatus.OK.value())
                .message("ok")
                .build();

        ResponseEntity<ResponseDTO<String>> response = dto.of();

        Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        Assertions.assertThat(response.getBody()).isSameAs(dto);
        Assertions.assertThat(response.getBody().getData()).isEqualTo("payload");
        Assertions.assertThat(response.getBody().getMessage()).isEqualTo("ok");
    }

    @Test
    @DisplayName("of() debe respetar códigos 4xx")
    void ofSupportsClientErrorCodes() {
        ResponseDTO<Object> dto = ResponseDTO.builder()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .message("bad request")
                .build();

        ResponseEntity<ResponseDTO<Object>> response = dto.of();

        Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        Assertions.assertThat(response.getBody().getData()).isNull();
    }

    @Test
    @DisplayName("AllArgsConstructor debe asignar todos los campos")
    void allArgsConstructorAssignsAllFields() {
        ResponseDTO<Integer> dto = new ResponseDTO<>(42, 201, "created");

        Assertions.assertThat(dto.getData()).isEqualTo(42);
        Assertions.assertThat(dto.getStatusCode()).isEqualTo(201);
        Assertions.assertThat(dto.getMessage()).isEqualTo("created");
    }

    @Test
    @DisplayName("ReportColumnsBuilder debe configurar las columnas esperadas para libro diario")
    void reportColumnsBuilderConfiguresDiaryColumns() {
        ReportColumnsBuilder builder = new ReportColumnsBuilder();
        JasperReportBuilder report = DynamicReports.report();

        builder.setColumnsReport(report, EAuxiliaryBookType.DIARY);

        Assertions.assertThat(columnNames(report)).containsExactly(
                "date",
                "account.accountCode",
                "account.accountDescription",
                "voucherName",
                "voucherNumber",
                "debit",
                "credit"
        );
    }

    @Test
    @DisplayName("ReportColumnsBuilder debe configurar las columnas esperadas para inventarios y balances")
    void reportColumnsBuilderConfiguresInventoryAndBalancesColumns() {
        ReportColumnsBuilder builder = new ReportColumnsBuilder();
        JasperReportBuilder report = DynamicReports.report();

        builder.setColumnsReport(report, EAuxiliaryBookType.INVENTORY_AND_BALANCES);

        Assertions.assertThat(columnNames(report)).containsExactly(
                "account.accountCode",
                "account.accountDescription",
                "description",
                "value"
        );
    }

    @Test
    @DisplayName("ReportColumnsBuilder debe configurar las columnas esperadas para mayor y balances")
    void reportColumnsBuilderConfiguresMajorAndBalancesColumns() {
        ReportColumnsBuilder builder = new ReportColumnsBuilder();
        JasperReportBuilder report = DynamicReports.report();

        builder.setColumnsReport(report, EAuxiliaryBookType.MAJOR_AND_BALANCES);

        Assertions.assertThat(columnNames(report)).containsExactly(
                "account.accountCode",
                "account.accountDescription",
                "initialBalance",
                "debit",
                "credit",
                "finalBalance"
        );
    }

    @Test
    @DisplayName("ReportColumnsBuilder debe usar la misma configuración para ACCOUNT y THIRD_PARTY")
    void reportColumnsBuilderUsesSharedLayoutForAccountAndThirdParty() {
        ReportColumnsBuilder builder = new ReportColumnsBuilder();
        JasperReportBuilder accountReport = DynamicReports.report();
        JasperReportBuilder thirdPartyReport = DynamicReports.report();

        builder.setColumnsReport(accountReport, EAuxiliaryBookType.ACCOUNT);
        builder.setColumnsReport(thirdPartyReport, EAuxiliaryBookType.THIRD_PARTY);

        Assertions.assertThat(columnNames(accountReport)).containsExactly(
                "date",
                "account.accountCode",
                "account.accountDescription",
                "thirdPartyId",
                "thirdPartyName",
                "voucherCostCenter",
                "voucherNumber",
                "debitMovement",
                "creditMovement",
                "balanceMovement"
        );
        Assertions.assertThat(columnNames(thirdPartyReport)).isEqualTo(columnNames(accountReport));
    }

    @Test
    @DisplayName("ReportColumnsBuilder debe configurar las columnas esperadas para movimientos contables")
    void reportColumnsBuilderConfiguresAccountingMovementColumns() {
        ReportColumnsBuilder builder = new ReportColumnsBuilder();
        JasperReportBuilder report = DynamicReports.report();

        builder.setColumnsReport(report, EAuxiliaryBookType.ACCOUNTING_MOVEMENT);

        Assertions.assertThat(columnNames(report)).containsExactly(
                "voucherType",
                "date",
                "voucherState",
                "thirdPartyId",
                "thirdPartyName",
                "account.accountCode",
                "account.accountDescription",
                "initialBalance",
                "debitMovement",
                "creditMovement",
                "netMovement"
        );
    }

    @Test
    @DisplayName("exportReport debe exportar PDF, registrar logs y crear historial cuando no existe")
    void exportReportExportsPdfAndCreatesHistory() throws Exception {
        AuxiliaryBook book = book(1L, "book-public-id", EAuxiliaryBookType.DIARY);
        ExportInfo exportInfo = new ExportInfo(EAuxiliaryBookFormat.PDF, "Entidad", book, List.of(), null);
        JasperReportBuilder report = Mockito.mock(JasperReportBuilder.class);
        AuxiliaryBookHistory persistedHistory = AuxiliaryBookHistory.builder()
                .id(90L)
                .auxiliaryBook(book)
                .state(EState.GENERATED)
                .build();

        Mockito.when(reportGenerator.generate(exportInfo)).thenReturn(report);
        Mockito.when(auxiliaryBookHistoryQueryRepositoryPort.findByBookId(1L)).thenReturn(null, persistedHistory);
        Mockito.doAnswer(invocation -> {
            OutputStream outputStream = invocation.getArgument(0);
            outputStream.write(new byte[]{1, 2, 3});
            return report;
        }).when(report).toPdf(Mockito.any(OutputStream.class));

        byte[] result = exportAuxiliaryBookUC.exportReport(exportInfo);

        ArgumentCaptor<AuxiliaryBookHistory> historyCaptor = ArgumentCaptor.forClass(AuxiliaryBookHistory.class);
        ArgumentCaptor<AuxiliaryBookLog> logCaptor = ArgumentCaptor.forClass(AuxiliaryBookLog.class);

        Assertions.assertThat(result).containsExactly(1, 2, 3);
        Mockito.verify(auxiliaryBookHistoryCommandRepositoryPort).registerAuxiliaryBookHistory(historyCaptor.capture());
        Mockito.verify(auxiliaryBookHistoryCommandRepositoryPort).updateAuxiliaryBookHistory(persistedHistory);
        Mockito.verify(auxiliaryBookLogCommandRepositoryPort, Mockito.times(2)).registerAuxiliaryBookLog(logCaptor.capture());

        Assertions.assertThat(historyCaptor.getValue().getState()).isEqualTo(EState.SCHEDULED);
        Assertions.assertThat(persistedHistory.getState()).isEqualTo(EState.EXPORT);
        Assertions.assertThat(logCaptor.getAllValues()).extracting(AuxiliaryBookLog::getETypeEvent)
                .containsExactly(ETypeEvent.GENERATING, ETypeEvent.SUCCESS_GENERATION);
    }

    @Test
    @DisplayName("exportReport debe exportar Excel y actualizar historial existente en ambos cambios de estado")
    void exportReportExportsExcelAndUpdatesExistingHistory() throws Exception {
        AuxiliaryBook book = book(2L, "book-excel-id", EAuxiliaryBookType.ACCOUNT);
        ExportInfo exportInfo = new ExportInfo(EAuxiliaryBookFormat.EXCEL, "Entidad", book, List.of(), null);
        JasperReportBuilder report = Mockito.mock(JasperReportBuilder.class);
        AuxiliaryBookHistory scheduledHistory = AuxiliaryBookHistory.builder()
                .id(91L)
                .auxiliaryBook(book)
                .state(EState.GENERATED)
                .build();
        AuxiliaryBookHistory exportedHistory = AuxiliaryBookHistory.builder()
                .id(92L)
                .auxiliaryBook(book)
                .state(EState.GENERATED)
                .build();

        Mockito.when(reportGenerator.generate(exportInfo)).thenReturn(report);
        Mockito.when(auxiliaryBookHistoryQueryRepositoryPort.findByBookId(2L))
                .thenReturn(scheduledHistory, exportedHistory);
        Mockito.doAnswer(invocation -> {
            OutputStream outputStream = invocation.getArgument(0);
            outputStream.write(new byte[]{9, 8});
            return report;
        }).when(report).toXlsx(Mockito.any(OutputStream.class));

        byte[] result = exportAuxiliaryBookUC.exportReport(exportInfo);

        ArgumentCaptor<AuxiliaryBookHistory> historyCaptor = ArgumentCaptor.forClass(AuxiliaryBookHistory.class);

        Assertions.assertThat(result).containsExactly(9, 8);
        Mockito.verify(auxiliaryBookHistoryCommandRepositoryPort, Mockito.times(2))
                .updateAuxiliaryBookHistory(historyCaptor.capture());
        Mockito.verify(auxiliaryBookHistoryCommandRepositoryPort, Mockito.never())
                .registerAuxiliaryBookHistory(Mockito.any(AuxiliaryBookHistory.class));
        Assertions.assertThat(historyCaptor.getAllValues()).extracting(AuxiliaryBookHistory::getState)
                .containsExactly(EState.SCHEDULED, EState.EXPORT);
    }

    @Test
    @DisplayName("exportReport debe registrar error, marcar historial en ERROR y delegar al formatter cuando falla la exportación")
    void exportReportMarksErrorWhenGenerationFails() throws Exception {
        AuxiliaryBook book = book(3L, "book-error-id", EAuxiliaryBookType.THIRD_PARTY);
        ExportInfo exportInfo = new ExportInfo(EAuxiliaryBookFormat.PDF, "Entidad", book, List.of(), null);
        JasperReportBuilder report = Mockito.mock(JasperReportBuilder.class);
        AuxiliaryBookHistory scheduledHistory = AuxiliaryBookHistory.builder()
                .id(93L)
                .auxiliaryBook(book)
                .state(EState.GENERATED)
                .build();
        AuxiliaryBookHistory failedHistory = AuxiliaryBookHistory.builder()
                .id(94L)
                .auxiliaryBook(book)
                .state(EState.GENERATED)
                .build();

        Mockito.when(reportGenerator.generate(exportInfo)).thenReturn(report);
        Mockito.when(auxiliaryBookHistoryQueryRepositoryPort.findByBookId(3L))
                .thenReturn(scheduledHistory, failedHistory);
        Mockito.when(messageServicePort.getMessage(Mockito.anyString(), Mockito.anyString())).thenReturn("formatted error");
        Mockito.doThrow(new DRException("boom")).when(report).toPdf(Mockito.any(OutputStream.class));

        byte[] result = exportAuxiliaryBookUC.exportReport(exportInfo);

        ArgumentCaptor<AuxiliaryBookHistory> historyCaptor = ArgumentCaptor.forClass(AuxiliaryBookHistory.class);
        ArgumentCaptor<AuxiliaryBookLog> logCaptor = ArgumentCaptor.forClass(AuxiliaryBookLog.class);

        Assertions.assertThat(result).isNull();
        Mockito.verify(auxiliaryBookHistoryCommandRepositoryPort, Mockito.times(2))
                .updateAuxiliaryBookHistory(historyCaptor.capture());
        Mockito.verify(formatterResultOutputPort).returnErrorGenericResponse(500, "formatted error");
        Mockito.verify(auxiliaryBookLogCommandRepositoryPort, Mockito.times(2)).registerAuxiliaryBookLog(logCaptor.capture());

        Assertions.assertThat(historyCaptor.getAllValues()).extracting(AuxiliaryBookHistory::getState)
                .containsExactly(EState.SCHEDULED, EState.ERROR);
        Assertions.assertThat(logCaptor.getAllValues()).extracting(AuxiliaryBookLog::getETypeEvent)
                .containsExactly(ETypeEvent.GENERATING, ETypeEvent.ERROR_GENERATION);
        Assertions.assertThat(logCaptor.getAllValues().get(1).getMessage()).contains("boom");
    }

    @Test
    @DisplayName("exportReport debe delegar al formatter cuando el libro es nulo")
    void exportReportDelegatesToFormatterWhenBookIsNull() {
        ExportInfo exportInfo = new ExportInfo(EAuxiliaryBookFormat.PDF, "Entidad", null, List.of(), null);

        byte[] result = exportAuxiliaryBookUC.exportReport(exportInfo);

        Assertions.assertThat(result).isNull();
        Mockito.verify(formatterResultOutputPort)
                .returnErrorGenericResponse(400, "AuxiliaryBook no puede ser nulo en ExportInfo");
        Mockito.verifyNoInteractions(reportGenerator, auxiliaryBookLogCommandRepositoryPort, auxiliaryBookHistoryCommandRepositoryPort);
    }

    @Test
    @DisplayName("getHttpHeaders debe devolver encabezados correctos para PDF y Excel")
    void getHttpHeadersReturnsHeadersPerFormat() {
        HttpHeaders pdfHeaders = exportAuxiliaryBookUC.getHttpHeaders(EAuxiliaryBookFormat.PDF, EAuxiliaryBookType.ACCOUNT);
        HttpHeaders excelHeaders = exportAuxiliaryBookUC.getHttpHeaders(EAuxiliaryBookFormat.EXCEL, EAuxiliaryBookType.ACCOUNTING_MOVEMENT);

        Assertions.assertThat(pdfHeaders.getContentType()).isEqualTo(MediaType.APPLICATION_PDF);
        Assertions.assertThat(pdfHeaders.getFirst(HttpHeaders.CONTENT_DISPOSITION))
                .contains("ACCOUNT_")
                .endsWith(".pdf");
        Assertions.assertThat(excelHeaders.getFirst(HttpHeaders.CONTENT_TYPE)).isEqualTo("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        Assertions.assertThat(excelHeaders.getFirst(HttpHeaders.CONTENT_DISPOSITION))
                .contains("ACCOUNTING_MOVEMENT_")
                .endsWith(".xlsx");
    }

    private List<String> columnNames(JasperReportBuilder report) {
        return report.getReport().getColumns().stream()
                .map(DRColumn::getName)
                .toList();
    }

    private AuxiliaryBook book(Long id, String publicId, EAuxiliaryBookType type) {
        return AuxiliaryBook.builder()
                .id(id)
                .publicId(publicId)
                .type(type)
                .entId("ENT1")
                .userId(40L)
                .format(EAuxiliaryBookFormat.PDF)
                .build();
    }
}
