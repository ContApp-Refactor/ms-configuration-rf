package co.unicauca.edu.co.contables.audit.infrastructure.adapters.output.export.generator;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

import co.unicauca.edu.co.contables.audit.application.internal.CombinedSession;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import org.springframework.stereotype.Component;

@Component
public class SessionPdfGenerator {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")
            .withZone(ZoneId.of("America/Bogota"));

    public byte[] generate(List<CombinedSession> sessions, String requestedBy, String reportDate,
            String appliedFilters) {
        try (java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4.rotate(),
                    36, 36, 36, 36);
            PdfWriter.getInstance(document, out);

            document.open();

            // Título
            Font titleFont = new Font(Font.FontFamily.HELVETICA, 16, Font.BOLD);
            Paragraph title = new Paragraph("Auditoría de Sesiones", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            document.add(Chunk.NEWLINE);

            // Metadata
            Font metaFont = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL);
            Paragraph meta = new Paragraph("Generado por: " + requestedBy, metaFont);
            document.add(meta);
            document.add(new Paragraph("Fecha de generación: " + reportDate, metaFont));
            if (appliedFilters != null && !appliedFilters.isBlank()) {
                document.add(new Paragraph("Filtros aplicados: " + appliedFilters, metaFont));
            }

            document.add(Chunk.NEWLINE);

            // Tabla
            PdfPTable table = new PdfPTable(4);
            table.setWidthPercentage(100);
            table.setWidths(new float[] { 3, 2, 3, 3 });

            // Encabezado
            Font headerFont = new Font(Font.FontFamily.HELVETICA, 11, Font.BOLD);
            BaseColor headerColor = new BaseColor(211, 211, 211); // gris claro

            addTableHeader(table, "Usuario", headerFont, headerColor);
            addTableHeader(table, "Rol", headerFont, headerColor);
            addTableHeader(table, "Inicio de sesión", headerFont, headerColor);
            addTableHeader(table, "Cierre de sesión", headerFont, headerColor);

            // Datos
            Font dataFont = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL);
            for (CombinedSession session : sessions) {
                addTableCell(table, Objects.toString(session.getUserName(), ""), dataFont);
                addTableCell(table, session.getUserRole() != null ? String.join(", ", session.getUserRole()) : "",
                        dataFont);
                addTableCell(table, formatDateTime(session.getLoginTime()), dataFont);
                addTableCell(table, formatDateTime(session.getLogoutTime()), dataFont);
            }

            document.add(table);
            document.close();

            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Error generando PDF de sesiones", e);
        }
    }

    private void addTableHeader(PdfPTable table, String text, Font font, BaseColor bgColor) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBackgroundColor(bgColor);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(5);
        table.addCell(cell);
    }

    private void addTableCell(PdfPTable table, String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setPadding(5);
        table.addCell(cell);
    }

    private String formatDateTime(Instant instant) {
        if (instant == null)
            return "";
        return DATE_FORMATTER.format(instant);
    }
}
