package co.unicauca.edu.co.contables.accounting.catalogue.accounting.manualnotes.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record AccountingNoteDetailRequest(
        @NotBlank(message = "El código de la cuenta es obligatorio")
        String accountCode,
        String thirdPartyDocument,
        String costCenterCode,
        @Size(max = 255) String description,
        @NotNull @PositiveOrZero(message = "El valor del débito debe ser mayor o igual a cero")
        BigDecimal debit,
        @NotNull @PositiveOrZero(message = "El valor del crédito debe ser mayor o igual a cero")
        BigDecimal credit) {
}
