package co.unicauca.edu.co.contables.auxiliary.application.useCase.scheduledReport.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import co.unicauca.edu.co.contables.auxiliary.application.ports.in.auxiliaryBook.IAuxiliaryBookCommandPort;
import co.unicauca.edu.co.contables.auxiliary.application.ports.in.export.IExportReportPort;
import co.unicauca.edu.co.contables.auxiliary.domain.models.core.AuxiliaryBook;
import co.unicauca.edu.co.contables.auxiliary.domain.models.core.export.AuxiliaryBookTemplate;
import co.unicauca.edu.co.contables.auxiliary.domain.models.core.export.ExportInfo;
import co.unicauca.edu.co.contables.auxiliary.domain.models.enums.EAlignment;
import co.unicauca.edu.co.contables.auxiliary.domain.models.enums.EAuxiliaryBookFormat;
import co.unicauca.edu.co.contables.auxiliary.domain.models.scheduling.ScheduledAuxiliaryBookJob;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;

/**
 * @brief Servicio reutilizable para generar el reporte (bytes) a partir de un job programado.
 *
 * Centraliza la creacion del AuxiliaryBook, la obtencion de datos contables
 * y la exportacion al formato solicitado, de modo que pueda ser invocado
 * tanto desde el scheduler (cuando el canal incluye email) como desde el
 * endpoint de descarga on-demand.
 *
 * Los DTOs tipados que devuelve {@code genereteAuxiliaryBookInfo} son
 * convertidos a {@link LinkedHashMap} via Jackson antes de ser pasados al
 * exportador, porque el {@code ReportDataBuilder} de exportacion espera
 * el mismo formato que el flujo HTTP inmediato (donde la data round-trips
 * por JSON FE-BE y llega como mapas genericos).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuxiliaryBookReportGenerator {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final IAuxiliaryBookCommandPort auxiliaryBookCommandPort;
    private final IExportReportPort exportReportPort;

    public GenerationResult generate(ScheduledAuxiliaryBookJob job, EAuxiliaryBookFormat format) {
        EAuxiliaryBookFormat resolvedFormat = format != null ? format : EAuxiliaryBookFormat.PDF;

        log.info("[ReportGenerator] start publicId={} bookType={} entId={} format={} criteria={}",
                job.getPublicId(), job.getBookType(), job.getEntId(), resolvedFormat, job.getCriteria());

        AuxiliaryBook book = AuxiliaryBook.builder()
                .type(job.getBookType())
                .entId(job.getEntId())
                .userId(job.getUserId())
                .format(resolvedFormat)
                .criteria(job.getCriteria())
                .build();

        AuxiliaryBook registeredBook = auxiliaryBookCommandPort.registerAuxiliaryBook(book);
        List<?> reportData = auxiliaryBookCommandPort.genereteAuxiliaryBookInfo(registeredBook);

        int rows = reportData != null ? reportData.size() : -1;
        log.info("[ReportGenerator] data rows={} for publicId={}", rows, job.getPublicId());

        List<?> serializableData = toLinkedHashMaps(reportData);

        ExportInfo exportInfo = new ExportInfo(
                resolvedFormat,
                resolveEntName(job),
                registeredBook,
                serializableData,
                resolveTemplate(job)
        );

        byte[] bytes = exportReportPort.exportReport(exportInfo);
        if (bytes == null || bytes.length == 0) {
            throw new IllegalStateException("Exported report content is empty.");
        }
        log.info("[ReportGenerator] exported bytes={} for publicId={}", bytes.length, job.getPublicId());
        return new GenerationResult(registeredBook, reportData, bytes, resolvedFormat);
    }

    /**
     * Convierte cada DTO en un {@link LinkedHashMap} via Jackson, replicando
     * el formato que el {@code ReportDataBuilder} espera (idéntico al que
     * llega al endpoint inmediato cuando el FE reenvia la data en JSON).
     */
    private List<LinkedHashMap<String, Object>> toLinkedHashMaps(List<?> data) {
        if (data == null || data.isEmpty()) {
            return List.of();
        }
        return data.stream()
                .map(item -> {
                    @SuppressWarnings("unchecked")
                    LinkedHashMap<String, Object> map = OBJECT_MAPPER.convertValue(item, LinkedHashMap.class);
                    return map;
                })
                .toList();
    }

    private String resolveEntName(ScheduledAuxiliaryBookJob job) {
        if (job != null && job.getBookType() != null) {
            return job.getBookType().name();
        }
        return job != null ? job.getEntId() : null;
    }

    /**
     * Devuelve el template del job si fue enviado desde el frontend al crear/actualizar.
     * Si está incompleto, completa con defaults para evitar que el renderer falle.
     */
    private AuxiliaryBookTemplate resolveTemplate(ScheduledAuxiliaryBookJob job) {
        AuxiliaryBookTemplate jobTemplate = job != null ? job.getTemplate() : null;
        AuxiliaryBookTemplate defaults = buildDefaultTemplate(job);

        if (jobTemplate == null) {
            log.info("[ReportGenerator] No template stored on job, using defaults.");
            return defaults;
        }

        return AuxiliaryBookTemplate.builder()
                .id(jobTemplate.getId() != null ? jobTemplate.getId() : defaults.getId())
                .name(jobTemplate.getName() != null && !jobTemplate.getName().isBlank()
                        ? jobTemplate.getName() : defaults.getName())
                .pathLogotype(jobTemplate.getPathLogotype())
                .alienation(jobTemplate.getAlienation() != null
                        ? jobTemplate.getAlienation() : defaults.getAlienation())
                .font(jobTemplate.getFont() != null && !jobTemplate.getFont().isBlank()
                        ? jobTemplate.getFont() : defaults.getFont())
                .fontSize(jobTemplate.getFontSize() != null && jobTemplate.getFontSize() > 0
                        ? jobTemplate.getFontSize() : defaults.getFontSize())
                .mainColor(jobTemplate.getMainColor() != null && !jobTemplate.getMainColor().isBlank()
                        ? jobTemplate.getMainColor() : defaults.getMainColor())
                .build();
    }

    private AuxiliaryBookTemplate buildDefaultTemplate(ScheduledAuxiliaryBookJob job) {
        String templateName = job != null && job.getBookType() != null
                ? job.getBookType().name()
                : "Auxiliary Book";

        return AuxiliaryBookTemplate.builder()
                .id(0L)
                .name(templateName)
                .pathLogotype(null)
                .alienation(EAlignment.CENTER)
                .font("Arial")
                .fontSize(12)
                .mainColor("#0B3C61")
                .build();
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class GenerationResult {
        private AuxiliaryBook registeredBook;
        private List<?> reportData;
        private byte[] reportBytes;
        private EAuxiliaryBookFormat format;
    }
}
