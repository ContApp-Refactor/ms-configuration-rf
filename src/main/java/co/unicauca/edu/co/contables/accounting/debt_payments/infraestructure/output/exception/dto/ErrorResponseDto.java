package co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.output.exception.dto;

import java.time.LocalDateTime;

public record ErrorResponseDto(
    LocalDateTime timestamp,
    int status,
    String error,
    String message,
    String path
) {}
