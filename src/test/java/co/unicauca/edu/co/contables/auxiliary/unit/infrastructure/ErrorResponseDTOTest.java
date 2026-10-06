package co.unicauca.edu.co.contables.auxiliary.unit.infrastructure;

import co.unicauca.edu.co.contables.auxiliary.application.dto.auxiliaryBook.AccountingMovementBookDTO;
import co.unicauca.edu.co.contables.auxiliary.application.dto.auxiliaryBook.DiaryBookDTO;
import co.unicauca.edu.co.contables.auxiliary.application.useCase.export.report.builders.ReportDataBuilder;
import co.unicauca.edu.co.contables.auxiliary.domain.models.core.AuxiliaryBook;
import co.unicauca.edu.co.contables.auxiliary.domain.models.core.export.ExportInfo;
import co.unicauca.edu.co.contables.auxiliary.domain.models.enums.EAuxiliaryBookType;
import co.unicauca.edu.co.contables.auxiliary.infrastructure.out.exception.dto.ErrorResponseDTO;
import net.sf.dynamicreports.jasper.builder.JasperReportBuilder;
import net.sf.dynamicreports.report.builder.DynamicReports;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.LinkedHashMap;
import java.util.List;

/**
 * @brief Pruebas unitarias para {@link ErrorResponseDTO} y {@link ReportDataBuilder}.
 */
class ErrorResponseDTOTest {

    @Test
    @DisplayName("of() debe construir un ResponseEntity con el status del DTO")
    void ofBuildsResponseEntityWithStatus() {
        ErrorResponseDTO dto = ErrorResponseDTO.builder()
                .status(HttpStatus.NOT_FOUND.value())
                .message("resource not found")
                .url("/api/v1/resource/1")
                .method("GET")
                .build();

        ResponseEntity<ErrorResponseDTO> response = dto.of();

        Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        Assertions.assertThat(response.getBody()).isSameAs(dto);
        Assertions.assertThat(response.getBody().getUrl()).isEqualTo("/api/v1/resource/1");
        Assertions.assertThat(response.getBody().getMethod()).isEqualTo("GET");
        Assertions.assertThat(response.getBody().getMessage()).isEqualTo("resource not found");
    }

    @Test
    @DisplayName("Setters y AllArgsConstructor deben funcionar correctamente")
    void settersAndAllArgsConstructor() {
        ErrorResponseDTO dto = new ErrorResponseDTO(500, "boom", "/x", "POST");

        Assertions.assertThat(dto.getStatus()).isEqualTo(500);
        Assertions.assertThat(dto.getMessage()).isEqualTo("boom");

        dto.setStatus(400);
        Assertions.assertThat(dto.getStatus()).isEqualTo(400);
    }

    @Test
    @DisplayName("ReportDataBuilder debe mapear datos crudos del libro diario a DiaryBookDTO")
    void reportDataBuilderMapsDiaryData() {
        ReportDataBuilder builder = new ReportDataBuilder();
        JasperReportBuilder report = DynamicReports.report();
        ExportInfo exportInfo = new ExportInfo(
                null,
                "ENT1",
                AuxiliaryBook.builder().type(EAuxiliaryBookType.DIARY).build(),
                List.of(new LinkedHashMap<>(java.util.Map.of(
                        "date", "05/01/2025",
                        "account", new LinkedHashMap<>(java.util.Map.of(
                                "nature", "debito",
                                "accountCode", "1105",
                                "accountDescription", "Caja"
                        )),
                        "voucherName", "RC",
                        "voucherNumber", "001",
                        "debit", "15.50",
                        "credit", 0
                ))),
                null
        );

        builder.setDataSource(report, exportInfo);

        JRBeanCollectionDataSource dataSource = (JRBeanCollectionDataSource) report.getDataSource();
        DiaryBookDTO row = (DiaryBookDTO) dataSource.getData().iterator().next();

        Assertions.assertThat(dataSource.getRecordCount()).isEqualTo(1);
        Assertions.assertThat(row.getDate()).isEqualTo("05/01/2025");
        Assertions.assertThat(row.getAccountCode()).isEqualTo(1105L);
        Assertions.assertThat(row.getAccountDescription()).isEqualTo("Caja");
        Assertions.assertThat(row.getDebit()).isEqualByComparingTo("15.50");
        Assertions.assertThat(row.getCredit()).isEqualByComparingTo("0");
    }

    @Test
    @DisplayName("ReportDataBuilder debe mapear datos de movimientos contables incluyendo el campo State")
    void reportDataBuilderMapsAccountingMovementData() {
        ReportDataBuilder builder = new ReportDataBuilder();
        JasperReportBuilder report = DynamicReports.report();
        ExportInfo exportInfo = new ExportInfo(
                null,
                "ENT1",
                AuxiliaryBook.builder().type(EAuxiliaryBookType.ACCOUNTING_MOVEMENT).build(),
                List.of(new LinkedHashMap<>(java.util.Map.of(
                        "voucherType", "RC",
                        "date", "05/01/2025",
                        "State", "RC",
                        "thirdPartyId", "900",
                        "thirdPartyName", "Proveedor Uno",
                        "account", new LinkedHashMap<>(java.util.Map.of(
                                "nature", "credito",
                                "accountCode", 2205,
                                "accountDescription", "Proveedores"
                        )),
                        "initialBalance", "100.00",
                        "debitMovement", "10.00",
                        "creditMovement", "30.00",
                        "netMovement", "120.00"
                ))),
                null
        );

        builder.setDataSource(report, exportInfo);

        JRBeanCollectionDataSource dataSource = (JRBeanCollectionDataSource) report.getDataSource();
        AccountingMovementBookDTO row = (AccountingMovementBookDTO) dataSource.getData().iterator().next();

        Assertions.assertThat(row.getVoucherType()).isEqualTo("RC");
        Assertions.assertThat(row.getState()).isEqualTo("RC");
        Assertions.assertThat(row.getThirdPartyName()).isEqualTo("Proveedor Uno");
        Assertions.assertThat(row.getAccountCode()).isEqualTo(2205L);
        Assertions.assertThat(row.getNetMovement()).isEqualByComparingTo("120.00");
    }

    @Test
    @DisplayName("ReportDataBuilder debe rechazar elementos de datos que no sean mapas")
    void reportDataBuilderRejectsUnexpectedRawItemType() {
        ReportDataBuilder builder = new ReportDataBuilder();
        JasperReportBuilder report = DynamicReports.report();
        ExportInfo exportInfo = new ExportInfo(
                null,
                "ENT1",
                AuxiliaryBook.builder().type(EAuxiliaryBookType.DIARY).build(),
                List.of("tipo inesperado"),
                null
        );

        Assertions.assertThatThrownBy(() -> builder.setDataSource(report, exportInfo))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unexpected data type");
    }
}
