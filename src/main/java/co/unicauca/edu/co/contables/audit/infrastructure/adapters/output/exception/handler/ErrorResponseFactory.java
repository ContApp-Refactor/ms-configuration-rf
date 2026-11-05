package co.unicauca.edu.co.contables.audit.infrastructure.adapters.output.exception.handler;

import java.time.ZonedDateTime;
import java.util.Map;

import org.springframework.http.HttpStatus;

import co.unicauca.edu.co.contables.audit.infrastructure.adapters.output.exception.dto.ErrorResponseDto;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ErrorResponseFactory {

    public static ErrorResponseDto build(
            HttpStatus status,
            String error,
            String message,
            String path) {

        return ErrorResponseDto.builder()
                .timestamp(ZonedDateTime.now())
                .status(status.value())
                .error(error)
                .message(message)
                .path(path)
                .build();
    }

    public static ErrorResponseDto buildValidation(
            String message,
            String path,
            Map<String, String> validationErrors) {

        return ErrorResponseDto.builder()
                .timestamp(ZonedDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Validation Failed")
                .message(message)
                .path(path)
                .validationErrors(validationErrors)
                .build();
    }
}
