package co.unicauca.edu.co.contables.auxiliary.application.useCase.export;

import co.unicauca.edu.co.contables.auxiliary.application.ports.in.export.IExportReportPort;
import co.unicauca.edu.co.contables.auxiliary.application.useCase.export.report.ReportGenerator;
import co.unicauca.edu.co.contables.auxiliary.domain.models.core.AuxiliaryBook;
import co.unicauca.edu.co.contables.auxiliary.domain.models.core.export.ExportInfo;
import co.unicauca.edu.co.contables.auxiliary.domain.models.enums.EAuxiliaryBookFormat;
import co.unicauca.edu.co.contables.auxiliary.domain.models.enums.EAuxiliaryBookType;
import co.unicauca.edu.co.contables.auxiliary.domain.models.enums.EState;
import co.unicauca.edu.co.contables.auxiliary.domain.models.history.AuxiliaryBookHistory;
import co.unicauca.edu.co.contables.auxiliary.domain.models.log.AuxiliaryBookLog;
import co.unicauca.edu.co.contables.auxiliary.domain.models.enums.ETypeEvent;
import co.unicauca.edu.co.contables.auxiliary.domain.ports.AuxiliaryBookHistory.IAuxiliaryBookHistoryCommandRepositoryPort;
import co.unicauca.edu.co.contables.auxiliary.domain.ports.AuxiliaryBookHistory.IAuxiliaryBookHistoryQueryRepositoryPort;
import co.unicauca.edu.co.contables.auxiliary.domain.ports.AuxiliaryBookLog.IAuxiliaryBookLogCommandRepositoryPort;
import co.unicauca.edu.co.contables.auxiliary.domain.ports.IFormatterResultOutputPort;
import co.unicauca.edu.co.contables.auxiliary.domain.ports.IMessageServicePort;
import co.unicauca.edu.co.contables.auxiliary.infrastructure.config.i18n.MessageKeys;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;

import lombok.RequiredArgsConstructor;
import net.sf.dynamicreports.report.exception.DRException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

/**
 * @brief Caso de uso para la exportación de libros auxiliares.
 *
 * Implementa el puerto de entrada {@link IExportReportPort} y coordina la
 * generación de reportes en PDF o Excel mediante {@link ReportGenerator},
 * registrando logs y actualizando el estado del libro a lo largo del proceso.
 * También provee los encabezados HTTP apropiados para cada formato de descarga.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ExportAuxiliaryBookUC implements IExportReportPort {

    private final ReportGenerator reportGenerator;

    private final IAuxiliaryBookLogCommandRepositoryPort abLogCommandRepositoryPort;
    private final IAuxiliaryBookHistoryQueryRepositoryPort abHistoryQueryRepositoryPort;
    private final IAuxiliaryBookHistoryCommandRepositoryPort abHistoryCommandRepositoryPort;
    private final IFormatterResultOutputPort formatterResultOutputPort;
    private final IMessageServicePort messageServicePort;

    @Override
    public byte[] exportReport(ExportInfo exportInfo) {

        System.out.println(exportInfo.toString()); // Tu depurador

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        AuxiliaryBook book = exportInfo.getAuxiliaryBook();

        if (book == null) {
            this.formatterResultOutputPort.returnErrorGenericResponse(400, "AuxiliaryBook no puede ser nulo en ExportInfo");
            return null;
        }

        String publicIdForLog = Objects.requireNonNullElse(book.getPublicId(), "ID_DESCONOCIDO");

        log.info("Starting export (format: {}) for book: {}", exportInfo.getFormat(), publicIdForLog);

        // --- CORRECCIÓN 1: Usar enums de ETypeEvent que sí existen ---
        // (Usando GENERATING como ejemplo, podrías usar SENDING si prefieres)
        this.createLog(book, ETypeEvent.GENERATING, "Exporting AuxiliaryBook "+ publicIdForLog +" in (" + exportInfo.getFormat() + " format)");

        // --- CORRECCIÓN 2: Usar enum de EState que sí existe ---
        this.updateBookState(book, EState.SCHEDULED); // <-- Cambiado de PENDING

        try {
            switch (exportInfo.getFormat()) {
                case PDF:
                    reportGenerator.generate(exportInfo).toPdf(baos);
                    break;
                case EXCEL:
                    reportGenerator.generate(exportInfo).toXlsx(baos);
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported format: " + exportInfo.getFormat());
            }

            log.info("Successfully exported book: {}", publicIdForLog);
            this.createLog(book, ETypeEvent.SUCCESS_GENERATION, "Export generated successfully."); // <-- Cambiado

            this.updateBookState(book, EState.EXPORT);

        } catch (DRException | IllegalArgumentException e) {
            log.error("Error during export for book: {}", publicIdForLog, e);
            String errorMessage = "Error during export: " + e.getMessage();

            this.createLog(book, ETypeEvent.ERROR_GENERATION, errorMessage); // <-- Cambiado

            this.updateBookState(book, EState.ERROR);

            this.formatterResultOutputPort.returnErrorGenericResponse(500, this.messageServicePort.getMessage(
                    MessageKeys.ERROR_GENERIC,
                    errorMessage
            ));
            return null;
        }

        return baos.toByteArray();
    }

    /**
     * Actualiza el estado del libro en su historial.
     * Busca el historial existente o crea uno nuevo si no se encuentra.
     */
    private void updateBookState(AuxiliaryBook book, EState newState) {
        try {
            AuxiliaryBookHistory history = abHistoryQueryRepositoryPort.findByBookId(book.getId());

            if (history == null) {
                history = AuxiliaryBookHistory.builder()
                        .publicId(UUID.randomUUID().toString())
                        .auxiliaryBook(book)
                        .state(newState)
                        // .deliveryWay(..) // Podrías añadir esto
                        .build();
                abHistoryCommandRepositoryPort.registerAuxiliaryBookHistory(history);
            } else {
                history.setState(newState);

                // --- CORRECCIÓN 3: Nombre de método correcto ---
                abHistoryCommandRepositoryPort.updateAuxiliaryBookHistory(history); // <-- Cambiado de updateAuxiliaryBookHistory
            }
        } catch (Exception e) {
            String publicIdForLog = (book != null && book.getPublicId() != null) ? book.getPublicId() : "ID_DESCONOCIDO";
            log.error("Could not update book state for book: {}", publicIdForLog, e);

            this.createLog(book, ETypeEvent.ERROR_SCHEDULING, "Failed to update book state to " + newState);
        }
    }


    /**
     * Método helper privado para estandarizar la creación de logs.
     * Cada entrada de log tendrá su propio publicId único.
     */
    private void createLog(AuxiliaryBook book, ETypeEvent event, String message) {
        AuxiliaryBookLog newLog = AuxiliaryBookLog.builder()
                .publicId(UUID.randomUUID().toString())
                .auxiliaryBook(book)
                .ETypeEvent(event)
                .message(message)
                .build();

        abLogCommandRepositoryPort.registerAuxiliaryBookLog(newLog);
    }


    @Override
    public HttpHeaders getHttpHeaders(EAuxiliaryBookFormat format, EAuxiliaryBookType auxBookType) {
        HttpHeaders headers = new HttpHeaders();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String date = dateFormat.format(new Date());
        String fileName = auxBookType.name() + "_" + date;

        switch (format) {
            case PDF:
                headers.setContentType(MediaType.APPLICATION_PDF);
                headers.add(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=" + fileName + ".pdf");
                break;
            case EXCEL:
                headers.add(HttpHeaders.CONTENT_TYPE, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
                headers.add(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=" + fileName + ".xlsx");
                break;
            default:
                throw new IllegalArgumentException("Unsupported format: " + format);
        }
        return headers;
    }
}