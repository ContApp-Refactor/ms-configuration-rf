package co.unicauca.edu.co.contables.audit.application.dto.response;

import co.unicauca.edu.co.contables.audit.domain.enums.ExportFormat;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ExportFileResult {
    private final byte[] data;
    private final String fileName;
    private final ExportFormat format;
}
