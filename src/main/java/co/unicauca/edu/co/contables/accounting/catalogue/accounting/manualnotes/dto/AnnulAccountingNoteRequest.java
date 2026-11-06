package co.unicauca.edu.co.contables.accounting.catalogue.accounting.manualnotes.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AnnulAccountingNoteRequest(
        @NotBlank String enterpriseId,
        @NotBlank String originModule,
        @NotBlank @Size(max = 500) String reason) {
}
