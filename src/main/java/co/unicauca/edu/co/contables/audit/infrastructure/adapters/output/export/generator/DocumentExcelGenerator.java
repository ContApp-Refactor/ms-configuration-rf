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

import co.unicauca.edu.co.contables.audit.domain.model.AuditDocumentEvent;
import co.unicauca.edu.co.contables.audit.domain.model.DocumentData;
import co.unicauca.edu.co.contables.audit.infrastructure.adapters.output.export.helper.DocumentAuditTranslationHelper;
import co.unicauca.edu.co.contables.audit.infrastructure.adapters.output.export.helper.ExcelStyleHelper;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DocumentExcelGenerator {

    private final ExcelStyleHelper styleHelper;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")
            .withZone(ZoneId.of("America/Bogota"));

    private static final String[] HEADERS = {
            "Código Documento", "Tipo Documento", "Tercero", "Fecha Contable",
            "Usuario", "Roles", "Tipo Operación", "Fecha Operación", "Módulo",
            "Encabezado", "Detalle", "Totales"
    };

    public byte[] generate(List<AuditDocumentEvent> events, String enterpriseName, String requestedBy,
            String reportDate, String appliedFilters) {

        try (Workbook workbook = new XSSFWorkbook();
                ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Auditoría Documentos");

            // Estilos creados una sola vez, se reusan en todas las filas
            CellStyle titleStyle = styleHelper.getTitleStyle(workbook);
            CellStyle headerStyle = styleHelper.getHeaderStyle(workbook);
            CellStyle dataStyle = styleHelper.getDataCellStyle(workbook);
            CellStyle wrapStyle = styleHelper.getWrapStyle(workbook);

            int rowIdx = 0;

            // Metadata
            Row titleRow = sheet.createRow(rowIdx++);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("Auditoría de Eventos de Documentos");
            titleCell.setCellStyle(titleStyle);
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, HEADERS.length - 1));

            createMetaRow(sheet, rowIdx++, "Empresa:", enterpriseName);
            createMetaRow(sheet, rowIdx++, "Generado por:", requestedBy);
            createMetaRow(sheet, rowIdx++, "Fecha de generación:", reportDate);
            if (appliedFilters != null && !appliedFilters.isBlank()) {
                createMetaRow(sheet, rowIdx++, "Filtros aplicados:", appliedFilters);
            }

            rowIdx++; // fila vacía de separación

            // Encabezado de tabla
            Row headerRow = sheet.createRow(rowIdx++);
            for (int i = 0; i < HEADERS.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(HEADERS[i]);
                cell.setCellStyle(headerStyle);
            }

            // Datos
            for (AuditDocumentEvent event : events) {
                Row row = sheet.createRow(rowIdx++);
                fillRow(row, event, dataStyle, wrapStyle);
            }

            // Anchos de columna
            // Columnas de datos básicos: autoSize
            for (int i = 0; i <= 8; i++) {
                sheet.autoSizeColumn(i);
            }
            // Columnas de snapshot: ancho fijo
            sheet.setColumnWidth(9, 14000); // Encabezado
            sheet.setColumnWidth(10, 14000); // Detalle
            sheet.setColumnWidth(11, 8000); // Totales

            workbook.write(out);
            return out.toByteArray();

        } catch (IOException e) {
            throw new RuntimeException("Error generando Excel de documentos", e);
        }
    }

    private void fillRow(Row row, AuditDocumentEvent event,
            CellStyle dataStyle, CellStyle wrapStyle) {

        createCell(row, 0, event.getDocumentCode(), dataStyle);
        createCell(row, 1, translate(event.getDocumentType()), dataStyle);
        createCell(row, 2, event.getThirdPartyName(), dataStyle);
        createCell(row, 3, event.getDocumentDate() != null
                ? event.getDocumentDate().toString()
                : "", dataStyle);
        createCell(row, 4, event.getUserName(), dataStyle);
        createCell(row, 5, event.getUserRoles() != null
                ? String.join(", ", event.getUserRoles())
                : "", dataStyle);
        createCell(row, 6, event.getOperationType() != null
                ? translate(event.getOperationType().name())
                : "", dataStyle);
        createCell(row, 7, event.getOperationAt() != null
                ? DATE_FORMATTER.format(event.getOperationAt())
                : "", dataStyle);
        createCell(row, 8, translate(event.getModuleName()), dataStyle);

        if (event.getDocumentData() != null) {
            DocumentData data = event.getDocumentData();
            setTextCell(row, 9, formatHeader(data.getHeader()), wrapStyle);
            setTextCell(row, 10, formatDetails(data.getDetails()), wrapStyle);
            setTextCell(row, 11, formatTotals(data.getTotals()), wrapStyle);
        } else {
            for (int i = 9; i <= 11; i++)
                createCell(row, i, "", dataStyle);
        }

        row.setHeightInPoints(80);
    }

    @SuppressWarnings("unchecked")
    private String formatHeader(Map<String, Object> header) {
        if (isEmpty(header))
            return "";
        StringBuilder sb = new StringBuilder();
        header.forEach((key, value) -> {
            if ("changes".equals(key))
                return;
            if (!isVisible(key))
                return;
            if (isBlankValue(value))
                return;
            sb.append(fieldLabel(key))
                    .append(": ")
                    .append(valueLabel(value))
                    .append("\n");
        });
        Object changesObj = header.get("changes");
        if (changesObj instanceof Map) {
            Map<String, Object> changes = (Map<String, Object>) changesObj;
            if (!changes.isEmpty()) {
                if (!sb.isEmpty())
                    sb.append("\n");
                sb.append("Cambios:\n");
                appendChanges(sb, changes);
            }
        }

        return sb.toString().trim();
    }

    @SuppressWarnings("unchecked")
    private String formatDetails(List<Map<String, Object>> details) {
        if (details == null || details.isEmpty())
            return "";
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < details.size(); i++) {
            Map<String, Object> item = details.get(i);
            if (item == null || item.isEmpty())
                continue;

            String lineLabel = resolveLineLabel(item, i);
            sb.append(lineLabel).append("\n");
            boolean hasFlat = false;
            for (Map.Entry<String, Object> e : item.entrySet()) {
                if ("changes".equals(e.getKey()))
                    continue;
                if (!isVisible(e.getKey()))
                    continue;
                if (isBlankValue(e.getValue()))
                    continue;
                sb.append("  ").append(fieldLabel(e.getKey()))
                        .append(": ").append(valueLabel(e.getValue()))
                        .append("\n");
                hasFlat = true;
            }
            Object changesObj = item.get("changes");
            if (changesObj instanceof Map) {
                Map<String, Object> changes = (Map<String, Object>) changesObj;
                if (!changes.isEmpty()) {
                    if (hasFlat)
                        sb.append("\n");
                    sb.append("  Cambios:\n");
                    appendChanges(sb, changes, "    ");
                }
            }

            if (i < details.size() - 1)
                sb.append("\n");
        }

        return sb.toString().trim();
    }

    @SuppressWarnings("unchecked")
    private String formatTotals(Map<String, Object> totals) {
        if (isEmpty(totals))
            return "";
        StringBuilder sb = new StringBuilder();

        totals.forEach((key, value) -> {
            if ("changes".equals(key))
                return;
            if (!isVisible(key))
                return;
            if (isBlankValue(value))
                return;
            sb.append(fieldLabel(key))
                    .append(": ")
                    .append(valueLabel(value))
                    .append("\n");
        });
        Object changesObj = totals.get("changes");
        if (changesObj instanceof Map) {
            Map<String, Object> changes = (Map<String, Object>) changesObj;
            if (!changes.isEmpty()) {
                if (!sb.isEmpty())
                    sb.append("\n");
                sb.append("Cambios:\n");
                appendChanges(sb, changes);
            }
        }

        return sb.toString().trim();
    }

    private void appendChanges(StringBuilder sb, Map<String, Object> changes) {
        appendChanges(sb, changes, "  ");
    }

    @SuppressWarnings("unchecked")
    private void appendChanges(StringBuilder sb, Map<String, Object> changes, String indent) {
        changes.forEach((field, diffObj) -> {
            if (!isVisible(field))
                return;
            if (!(diffObj instanceof Map))
                return;
            Map<String, Object> diff = (Map<String, Object>) diffObj;
            Object before = diff.get("before");
            Object after = diff.get("after");
            sb.append(indent)
                    .append(fieldLabel(field))
                    .append(": ")
                    .append(valueLabel(before))
                    .append(" → ")
                    .append(valueLabel(after))
                    .append("\n");
        });
    }

    private String resolveLineLabel(Map<String, Object> item, int idx) {
        for (String key : new String[] { "invoiceCode", "invoiceId", "productId" }) {
            Object val = item.get(key);
            if (val != null && !val.toString().isBlank())
                return fieldLabel(key) + " " + val;
        }
        return "Línea " + (idx + 1);
    }

    private boolean isVisible(String key) {
        return DocumentAuditTranslationHelper.isVisibleField(key);
    }

    private String fieldLabel(String key) {
        return DocumentAuditTranslationHelper.translateField(key);
    }

    private String valueLabel(Object value) {
        if (value == null)
            return "";
        return DocumentAuditTranslationHelper.translateValue(value);
    }

    private String translate(Object value) {
        if (value == null)
            return "";
        return DocumentAuditTranslationHelper.translateValue(value);
    }

    private boolean isBlankValue(Object value) {
        return value == null || value.toString().isBlank();
    }

    private boolean isEmpty(Map<?, ?> map) {
        return map == null || map.isEmpty();
    }

    private void createMetaRow(Sheet sheet, int rowIdx, String label, String value) {
        Row row = sheet.createRow(rowIdx);
        row.createCell(0).setCellValue(label);
        row.createCell(1).setCellValue(value != null ? value : "");
    }

    private void createCell(Row row, int col, String value, CellStyle style) {
        Cell cell = row.createCell(col);
        cell.setCellValue(value != null ? value : "");
        cell.setCellStyle(style);
    }

    private void setTextCell(Row row, int col, String text, CellStyle style) {
        Cell cell = row.createCell(col);
        cell.setCellStyle(style);
        cell.setCellValue(text != null ? text : "");
    }

}
