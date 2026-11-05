package co.unicauca.edu.co.contables.audit.infrastructure.adapters.output.export.generator;

import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;
import org.apache.poi.ss.util.CellRangeAddress;

import co.unicauca.edu.co.contables.audit.application.internal.CombinedSession;
import co.unicauca.edu.co.contables.audit.infrastructure.adapters.output.export.helper.ExcelStyleHelper;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SessionExcelGenerator {

    private final ExcelStyleHelper styleHelper;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")
            .withZone(ZoneId.of("America/Bogota"));

    public byte[] generate(List<CombinedSession> sessions, String requestedBy, String reportDate,
            String appliedFilters) {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Sesiones");

            int rowIdx = 0;

            // Encabezado de metadata
            Row titleRow = sheet.createRow(rowIdx++);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("Auditoría de Sesiones");
            titleCell.setCellStyle(styleHelper.getTitleStyle(workbook));
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 3));

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

            rowIdx++; // espacio antes de la tabla

            // Encabezado de tabla
            Row headerRow = sheet.createRow(rowIdx++);
            String[] headers = { "Usuario", "Rol", "Inicio de sesión", "Cierre de sesión" };
            CellStyle headerStyle = styleHelper.getHeaderStyle(workbook);

            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }
            CellStyle dataStyle = styleHelper.getDataCellStyle(workbook);

            // Datos
            for (CombinedSession session : sessions) {
                Row row = sheet.createRow(rowIdx++);
                Cell c0 = row.createCell(0);
                c0.setCellValue(Objects.toString(session.getUserName(), ""));
                c0.setCellStyle(dataStyle);

                Cell c1 = row.createCell(1);
                c1.setCellValue(session.getUserRole() != null ? String.join(", ", session.getUserRole()) : "");
                c1.setCellStyle(dataStyle);

                Cell c2 = row.createCell(2);
                c2.setCellValue(formatDateTime(session.getLoginTime()));
                c2.setCellStyle(dataStyle);

                Cell c3 = row.createCell(3);
                c3.setCellValue(formatDateTime(session.getLogoutTime()));
                c3.setCellStyle(dataStyle);
            }

            // Auto-ajustar columnas
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            // Retornar como byte[]
            try (java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream()) {
                workbook.write(out);
                return out.toByteArray();
            }
        } catch (IOException e) {
            throw new RuntimeException("Error generando Excel de sesiones", e);
        }
    }

    private String formatDateTime(Instant instant) {
        if (instant == null)
            return "";
        return DATE_FORMATTER.format(instant);
    }
}
