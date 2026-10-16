package co.unicauca.edu.co.contables.auxiliary.application.useCase.export.report.builders;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;

import co.unicauca.edu.co.contables.auxiliary.domain.models.enums.EAuxiliaryBookType;

import lombok.NoArgsConstructor;
import net.sf.dynamicreports.jasper.builder.JasperReportBuilder;
import net.sf.dynamicreports.report.builder.DynamicReports;
import net.sf.dynamicreports.report.builder.column.TextColumnBuilder;
import net.sf.dynamicreports.report.builder.grid.ColumnTitleGroupBuilder;

/**
 * @brief Constructor de columnas para los reportes de libros auxiliares.
 *
 * Define las columnas comunes y específicas (cuenta, tercero, comprobante,
 * movimientos, saldos) y configura el {@link JasperReportBuilder} de
 * DynamicReports según el {@link EAuxiliaryBookType} solicitado.
 */
@Service
@NoArgsConstructor
public class ReportColumnsBuilder {

    // === Columnas comunes ===
    private final TextColumnBuilder<String> dateCol =
            DynamicReports.col.column("Fecha", "date", DynamicReports.type.stringType());

    // === Columnas de cuenta (ya no anidadas) ===

    private final TextColumnBuilder<Long> accountCodeCol =
            DynamicReports.col.column("Código", "account.accountCode", DynamicReports.type.longType());

    private final TextColumnBuilder<String> accountDescriptionCol =
            DynamicReports.col.column("Descripción", "account.accountDescription", DynamicReports.type.stringType());

    private final ColumnTitleGroupBuilder titleAccountGroup = DynamicReports.grid
            .titleGroup("Cuenta", accountCodeCol, accountDescriptionCol);

    // === Columnas de tercero ===
    private final TextColumnBuilder<String> thirdPartyIdCol =
            DynamicReports.col.column("Número de Identificación", "thirdPartyId", DynamicReports.type.stringType());

    private final TextColumnBuilder<String> thirdPartyNameCol =
            DynamicReports.col.column("Nombre", "thirdPartyName", DynamicReports.type.stringType());

    private final ColumnTitleGroupBuilder titleThirdPartyGroup = DynamicReports.grid
            .titleGroup("Tercero", thirdPartyIdCol, thirdPartyNameCol);

    // === Método principal ===
    public void setColumnsReport(JasperReportBuilder report, EAuxiliaryBookType type) {
        switch (type) {
            case INVENTORY_AND_BALANCES -> setColumnsInventoryAndBalancesReport(report);
            case DIARY -> setColumnsDiaryReport(report);
            case MAJOR_AND_BALANCES -> setColumnsMajorAndBalancesReport(report);
            case ACCOUNT, THIRD_PARTY -> setColumnsAccountAndThirdPartyReport(report);
            case ACCOUNTING_MOVEMENT -> setColumnsAccountingMovementReport(report);
            default -> throw new IllegalArgumentException("Unsupported auxiliary book type: " + type);
        }
    }

    private void setColumnsInventoryAndBalancesReport(JasperReportBuilder report) {
        TextColumnBuilder<String> description =
                DynamicReports.col.column("Descripción", "description", DynamicReports.type.stringType());
        TextColumnBuilder<BigDecimal> value =
                DynamicReports.col.column("Valor", "value", DynamicReports.type.bigDecimalType());

        report.columnGrid(titleAccountGroup, description, value);
        report.columns(accountCodeCol, accountDescriptionCol, description, value);
    }

    private void setColumnsDiaryReport(JasperReportBuilder report) {
        TextColumnBuilder<String> voucherNameCol =
                DynamicReports.col.column("Comprobante", "voucherName", DynamicReports.type.stringType());
        TextColumnBuilder<String> voucherNumberCol =
                DynamicReports.col.column("Número", "voucherNumber", DynamicReports.type.stringType());
        TextColumnBuilder<BigDecimal> debitCol =
                DynamicReports.col.column("Débito", "debit", DynamicReports.type.bigDecimalType());
        TextColumnBuilder<BigDecimal> creditCol =
                DynamicReports.col.column("Crédito", "credit", DynamicReports.type.bigDecimalType());

        ColumnTitleGroupBuilder titleVoucherGroup =
                DynamicReports.grid.titleGroup("Comprobante", voucherNameCol, voucherNumberCol);

        report.columnGrid(dateCol, titleAccountGroup, titleVoucherGroup, debitCol, creditCol);
        report.columns(dateCol, accountCodeCol, accountDescriptionCol,
                voucherNameCol, voucherNumberCol, debitCol, creditCol);
    }

    private void setColumnsMajorAndBalancesReport(JasperReportBuilder report) {
        TextColumnBuilder<BigDecimal> initialBalanceCol =
                DynamicReports.col.column("Saldo Inicial", "initialBalance", DynamicReports.type.bigDecimalType());
        TextColumnBuilder<BigDecimal> debitCol =
                DynamicReports.col.column("Débito", "debitMovement", DynamicReports.type.bigDecimalType());
        TextColumnBuilder<BigDecimal> creditCol =
                DynamicReports.col.column("Crédito", "creditMovement", DynamicReports.type.bigDecimalType());
        TextColumnBuilder<BigDecimal> finalBalanceCol =
                DynamicReports.col.column("Saldo Final", "finalBalance", DynamicReports.type.bigDecimalType());

        ColumnTitleGroupBuilder titleMovementGroup =
                DynamicReports.grid.titleGroup("Movimiento", debitCol, creditCol);

        report.columnGrid(titleAccountGroup, initialBalanceCol, titleMovementGroup, finalBalanceCol);
        report.columns(accountCodeCol, accountDescriptionCol,
                initialBalanceCol, debitCol, creditCol, finalBalanceCol);
    }

    private void setColumnsAccountAndThirdPartyReport(JasperReportBuilder report) {
        TextColumnBuilder<BigDecimal> debitCol =
                DynamicReports.col.column("Débito", "debitMovement", DynamicReports.type.bigDecimalType());
        TextColumnBuilder<BigDecimal> creditCol =
                DynamicReports.col.column("Crédito", "creditMovement", DynamicReports.type.bigDecimalType());
        TextColumnBuilder<BigDecimal> balanceCol =
                DynamicReports.col.column("Saldo", "balanceMovement", DynamicReports.type.bigDecimalType());
        TextColumnBuilder<String> voucherCostCenterCol =
                DynamicReports.col.column("Centro de Costo", "voucherCostCenter", DynamicReports.type.stringType());
        TextColumnBuilder<String> voucherNumberCol =
                DynamicReports.col.column("Número", "voucherNumber", DynamicReports.type.stringType());

        ColumnTitleGroupBuilder titleMovementGroup =
                DynamicReports.grid.titleGroup("Movimiento", debitCol, creditCol, balanceCol);
        ColumnTitleGroupBuilder titleVoucherGroup =
                DynamicReports.grid.titleGroup("Documento", voucherCostCenterCol, voucherNumberCol);

        report.columnGrid(dateCol, titleAccountGroup, titleMovementGroup, titleThirdPartyGroup, titleVoucherGroup);
        report.columns(dateCol, accountCodeCol, accountDescriptionCol,
                thirdPartyIdCol, thirdPartyNameCol, voucherCostCenterCol, voucherNumberCol,
                debitCol, creditCol, balanceCol);
    }

    private void setColumnsAccountingMovementReport(JasperReportBuilder report) {
        TextColumnBuilder<String> voucherTypeCol =
                DynamicReports.col.column("Tipo Documento", "voucherType", DynamicReports.type.stringType());
        TextColumnBuilder<String> voucherStateCol =
                DynamicReports.col.column("Estado", "state", DynamicReports.type.stringType());
        TextColumnBuilder<BigDecimal> initialBalanceCol =
                DynamicReports.col.column("Saldo Inicial", "initialBalance", DynamicReports.type.bigDecimalType());
        TextColumnBuilder<BigDecimal> debitCol =
                DynamicReports.col.column("Débito", "debitMovement", DynamicReports.type.bigDecimalType());
        TextColumnBuilder<BigDecimal> creditCol =
                DynamicReports.col.column("Crédito", "creditMovement", DynamicReports.type.bigDecimalType());
        TextColumnBuilder<BigDecimal> netMovementCol =
                DynamicReports.col.column("Movimiento Neto", "netMovement", DynamicReports.type.bigDecimalType());

        ColumnTitleGroupBuilder titleMovementGroup =
                DynamicReports.grid.titleGroup("Movimiento", initialBalanceCol, debitCol, creditCol);

        report.columnGrid(voucherTypeCol, dateCol, voucherStateCol, titleThirdPartyGroup,
                titleAccountGroup, titleMovementGroup, netMovementCol);

        report.columns(voucherTypeCol, dateCol, voucherStateCol, thirdPartyIdCol, thirdPartyNameCol
                , accountCodeCol, accountDescriptionCol,
                initialBalanceCol, debitCol, creditCol, netMovementCol);
    }
}
