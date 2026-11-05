package co.unicauca.edu.co.contables.accounting.catalogue.accounting.manualnotes.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;

public record AccountingNoteRequest(
        @NotBlank(message = "El código del tipo de documento es obligatorio")
        String documentTypeCode,
        @NotBlank(message = "El concepto general de la nota es obligatorio")
        @Size(max = 255)
        String concept,
        @NotNull(message = "La fecha del asiento es obligatoria")
        LocalDate date,
        @NotBlank(message = "La empresa es obligatoria")
        String enterpriseId,
        @NotBlank(message = "El módulo de origen es obligatorio")
        String originModule,
        @NotBlank(message = "El identificador de origen es obligatorio")
        String originId,
        @NotEmpty(message = "La nota contable debe contener al menos un detalle")
        @Size(min = 2, max = 999, message = "La nota contable debe contener entre dos y 999 detalles")
        List<@Valid AccountingNoteDetailRequest> details) {
}
