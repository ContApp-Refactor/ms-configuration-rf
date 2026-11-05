package co.unicauca.edu.co.contables.audit.infrastructure.adapters.output.export.generator;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import co.unicauca.edu.co.contables.audit.domain.model.AuditOperation;
import co.unicauca.edu.co.contables.audit.domain.model.OperationData;
import co.unicauca.edu.co.contables.audit.infrastructure.adapters.output.export.helper.AuditOperationTranslationHelper;
import co.unicauca.edu.co.contables.audit.infrastructure.adapters.output.export.helper.ExcelStyleHelper;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class OperationExcelGenerator {

    private final ExcelStyleHelper styleHelper;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")
            .withZone(ZoneId.of("America/Bogota"));

    private static final int SNAPSHOT_COL_WIDTH = 18000;

    public byte[] generate(List<AuditOperation> operations, String enterpriseName, String requestedBy,
            String reportDate, String appliedFilters) {

        try (Workbook workbook = new XSSFWorkbook();
                ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Auditoría Operaciones");
            int rowIdx = 0;

            // Metadata
            Row titleRow = sheet.createRow(rowIdx++);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("Auditoría de Operaciones");
            titleCell.setCellStyle(styleHelper.getTitleStyle(workbook));
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 6));

            Row enterpriseRow = sheet.createRow(rowIdx++);
            enterpriseRow.createCell(0).setCellValue("Empresa:");
            enterpriseRow.createCell(1).setCellValue(enterpriseName);

            Row metaRow = sheet.createRow(rowIdx++);
            metaRow.createCell(0).setCellValue("Generado por:");
            metaRow.createCell(1).setCellValue(requestedBy);

            Row dateRow = sheet.createRow(rowIdx++);
            dateRow.createCell(0).setCellValue("Fecha de generación:");
            dateRow.createCell(1).setCellValue(reportDate);

            if (appliedFilters != null && !appliedFilters.isBlank()) {
                Row filterRow = sheet.createRow(rowIdx++);
                filterRow.createCell(0).setCellValue("Filtros aplicados:");
                filterRow.createCell(1).setCellValue(appliedFilters);
            }

            rowIdx++;

            // Encabezado de tabla
            String[] headers = {
                    "Usuario", "Rol", "Tipo Operación", "Fecha Operación",
                    "Módulo", "Entidad Afectada", "Detalles"
            };
            Row headerRow = sheet.createRow(rowIdx++);
            CellStyle headerStyle = styleHelper.getHeaderStyle(workbook);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Datos
            CellStyle dataStyle = styleHelper.getDataCellStyle(workbook);
            CellStyle wrapStyle = styleHelper.getWrapStyle(workbook);

            for (AuditOperation op : operations) {
                Row row = sheet.createRow(rowIdx++);

                createCell(row, 0, op.getUserName(), dataStyle);
                createCell(row, 1, op.getUserRole() != null ? String.join(", ", op.getUserRole()) : "", dataStyle);
                createCell(row, 2, AuditOperationTranslationHelper.translateOperation(op.getOperationType().name()),
                        dataStyle);
                createCell(row, 3, op.getOperationAt() != null ? DATE_FORMATTER.format(op.getOperationAt()) : "",
                        dataStyle);
                createCell(row, 4, AuditOperationTranslationHelper.translateModule(op.getModuleName()), dataStyle);
                createCell(row, 5, AuditOperationTranslationHelper.translateTable(op.getAffectedTable()), dataStyle);

                Cell snapshotCell = row.createCell(6);
                snapshotCell.setCellStyle(wrapStyle);
                if (op.getDataObject() != null) {
                    try {
                        snapshotCell.setCellValue(formatSnapshot(op));
                    } catch (Exception e) {
                        snapshotCell.setCellValue("[Error serializando snapshot]");
                    }
                }
                row.setHeightInPoints(80);
            }

            // Ajuste de columnas
            for (int i = 0; i < headers.length - 1; i++) {
                sheet.autoSizeColumn(i);
            }
            sheet.setColumnWidth(6, SNAPSHOT_COL_WIDTH);

            workbook.write(out);
            return out.toByteArray();

        } catch (IOException e) {
            throw new RuntimeException("Error generando Excel de operaciones", e);
        }
    }

    private void createCell(Row row, int col, String value, CellStyle style) {
        Cell cell = row.createCell(col);
        cell.setCellValue(value != null ? value : "");
        cell.setCellStyle(style);
    }

    private String formatSnapshot(AuditOperation op) {
        OperationData data = op.getDataObject();
        if (data == null)
            return "";

        StringBuilder sb = new StringBuilder();
        String type = op.getOperationType().name();

        if ("CREATE".equals(type) || "DELETE".equals(type)) {
            Map<String, Object> entity = data.getEntity();
            if (entity != null && !entity.isEmpty()) {
                sb.append("Entidad:\n");
                entity.entrySet().stream()
                        .filter(e -> AuditOperationTranslationHelper.isVisible(e.getKey()))
                        .forEach(e -> sb
                                .append("  ")
                                .append(AuditOperationTranslationHelper.translateFieldKey(e.getKey()))
                                .append(": ")
                                .append(AuditOperationTranslationHelper.translateValue(e.getValue()))
                                .append("\n"));
            }
            return sb.toString().trim();
        }

        Map<String, Object> context = data.getContext();
        if (context != null && !context.isEmpty()) {
            sb.append("Contexto:\n");
            context.entrySet().stream()
                    .filter(e -> AuditOperationTranslationHelper.isVisible(e.getKey()))
                    .forEach(e -> sb
                            .append("  ")
                            .append(AuditOperationTranslationHelper.translateFieldKey(e.getKey()))
                            .append(": ")
                            .append(AuditOperationTranslationHelper.translateValue(e.getValue()))
                            .append("\n"));
            sb.append("\n");
        }

        Map<String, OperationData.FieldChange> changes = data.getChanges();
        if (changes != null && !changes.isEmpty()) {
            sb.append("Cambios:\n");
            changes.forEach((field, change) -> sb
                    .append("  ")
                    .append(AuditOperationTranslationHelper.translateFieldKey(field))
                    .append(":\n")
                    .append("    Antes: ")
                    .append(AuditOperationTranslationHelper.translateValue(change.getBefore()))
                    .append("\n")
                    .append("    Después: ")
                    .append(AuditOperationTranslationHelper.translateValue(change.getAfter()))
                    .append("\n"));
        }

        return sb.toString().trim();
    }
}
