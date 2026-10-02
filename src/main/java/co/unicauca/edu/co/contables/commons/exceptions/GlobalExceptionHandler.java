package co.unicauca.edu.co.contables.commons.exceptions;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.mapping.PropertyReferenceException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.domain.utils.ImportConstants;
import co.unicauca.edu.co.contables.accounting.catalogue.commons.exceptions.catalogue.AccountCatalogueErrorCode;
import co.unicauca.edu.co.contables.accounting.catalogue.commons.exceptions.catalogue.AccountCatalogueHierarchyException;
import co.unicauca.edu.co.contables.accounting.catalogue.commons.exceptions.catalogue.AccountCatalogueImportException;
import co.unicauca.edu.co.contables.accounting.catalogue.commons.exceptions.catalogue.FileSizeExceededException;
import co.unicauca.edu.co.contables.accounting.catalogue.commons.exceptions.catalogue.FileValidationException;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @brief Manejador global de excepciones para toda la aplicación.
 * Proporciona respuestas consistentes y descriptivas para diferentes tipos de errores.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * @brief Maneja excepciones de negocio
     *
     * Maneja excepciones de negocio y resuelve el estado HTTP según el código de error.
     * @param ex la excepción de negocio lanzada
     * @param request la solicitud web que causó la excepción
     * @return ResponseEntity con la respuesta de error estructurada
     */
    @ExceptionHandler(BaseBusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessExceptions(
            BaseBusinessException ex, WebRequest request) {

        String errorCode = ex.getErrorCode().getCode();
        HttpStatus status = mapStatusFromErrorCode(errorCode);

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(ex.getMessage())
                .code(errorCode)
                .path(request.getDescription(false).replace("uri=", ""))
                .build();

        return new ResponseEntity<>(errorResponse, status);
    }

    /**
     * @brief Maneja errores de validación de payload
     *
     * Maneja errores de validación de payload (Bean Validation en @RequestBody con @Valid).
     * @param ex la excepción de validación de argumentos de método
     * @param request la solicitud web que causó la excepción
     * @return ResponseEntity con la respuesta de error y errores de campo
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, WebRequest request) {

        HttpStatus status = HttpStatus.BAD_REQUEST;
        Map<String, String> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(
                        fe -> fe.getField(),
                        fe -> fe.getDefaultMessage(),
                        (msg1, msg2) -> msg1
                ));

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(status.value())
                .error(status.getReasonPhrase())
                .message("Error de validación de campos")
                .code(ErrorCode.GENERIC_ERROR.getCode())
                .path(request.getDescription(false).replace("uri=", ""))
                .build();

        return new ResponseEntity<>(Map.of(
                "error", errorResponse,
                "fieldErrors", fieldErrors
        ), status);
    }

    /**
     * @brief Maneja errores de validación de parámetros
     *
     * Maneja errores de validación a nivel de parámetros (e.g., @RequestParam, @PathVariable).
     * @param ex la excepción de violación de restricciones
     * @param request la solicitud web que causó la excepción
     * @return ResponseEntity con la respuesta de error y violaciones
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Object> handleConstraintViolation(
            ConstraintViolationException ex, WebRequest request) {

        HttpStatus status = HttpStatus.BAD_REQUEST;
        Map<String, String> violations = ex.getConstraintViolations().stream()
                .collect(Collectors.toMap(
                        v -> v.getPropertyPath().toString(),
                        ConstraintViolation::getMessage,
                        (msg1, msg2) -> msg1
                ));

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(status.value())
                .error(status.getReasonPhrase())
                .message("Error de validación")
                .code(ErrorCode.GENERIC_ERROR.getCode())
                .path(request.getDescription(false).replace("uri=", ""))
                .build();

        return new ResponseEntity<>(Map.of(
                "error", errorResponse,
                "violations", violations
        ), status);
    }

    /**
     * @brief Maneja errores de deserialización JSON
     *
     * Maneja errores de deserialización JSON (tipos de datos incorrectos).
     * @param ex la excepción de mensaje HTTP no legible
     * @param request la solicitud web que causó la excepción
     * @return ResponseEntity con la respuesta de error estructurada
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex, WebRequest request) {

        HttpStatus status = HttpStatus.BAD_REQUEST;
        String message = "Error en el formato de los datos enviados";

        // Intentar extraer información más específica si es un error de formato
        Throwable cause = ex.getCause();
        if (cause instanceof InvalidFormatException) {
            InvalidFormatException ife = (InvalidFormatException) cause;
            String fieldName = ife.getPath().isEmpty() ? "campo" : ife.getPath().get(0).getFieldName();
            String targetType = ife.getTargetType().getSimpleName();
            Object value = ife.getValue();
            
            message = String.format("El campo '%s' tiene un formato inválido. Se esperaba un %s pero se recibió: '%s'", 
                    fieldName, targetType, value);
        }

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(message)
                .code(ErrorCode.GENERIC_ERROR.getCode())
                .path(request.getDescription(false).replace("uri=", ""))
                .build();

        return new ResponseEntity<>(errorResponse, status);
    }

    /**
     * @brief Maneja errores de parámetros de solicitud faltantes
     *
     * Maneja errores cuando un parámetro requerido (@RequestParam) no está presente.
     * @param ex la excepción de parámetro de solicitud faltante
     * @param request la solicitud web que causó la excepción
     * @return ResponseEntity con la respuesta de error estructurada
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingServletRequestParameter(
            MissingServletRequestParameterException ex, WebRequest request) {

        HttpStatus status = HttpStatus.BAD_REQUEST;
        String message = String.format("El parámetro '%s' de tipo '%s' es requerido", 
                ex.getParameterName(), ex.getParameterType());

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(message)
                .code(ErrorCode.GENERIC_ERROR.getCode())
                .path(request.getDescription(false).replace("uri=", ""))
                .build();

        return new ResponseEntity<>(errorResponse, status);
    }

    /**
     * @brief Maneja errores de conversión de tipo de argumento
     *
     * Maneja errores cuando un parámetro no puede convertirse al tipo esperado.
     * @param ex la excepción de tipo de argumento no coincidente
     * @param request la solicitud web que causó la excepción
     * @return ResponseEntity con la respuesta de error estructurada
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatch(
            MethodArgumentTypeMismatchException ex, WebRequest request) {

        HttpStatus status = HttpStatus.BAD_REQUEST;
        String paramName = ex.getName();
        Object value = ex.getValue();
        String requiredType = ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "desconocido";
        
        String message = String.format("El parámetro '%s' con valor '%s' no puede convertirse al tipo requerido '%s'", 
                paramName, value, requiredType);

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(message)
                .code(ErrorCode.GENERIC_ERROR.getCode())
                .path(request.getDescription(false).replace("uri=", ""))
                .build();

        return new ResponseEntity<>(errorResponse, status);
    }

    /**
     * @brief Maneja excepciones de argumento ilegal
     *
     * Maneja errores cuando se pasa un argumento inválido, como índices de paginación negativos.
     * @param ex la excepción de argumento ilegal
     * @param request la solicitud web que causó la excepción
     * @return ResponseEntity con la respuesta de error estructurada
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(
            IllegalArgumentException ex, WebRequest request) {

        HttpStatus status = HttpStatus.BAD_REQUEST;

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(ex.getMessage())
                .code(ErrorCode.GENERIC_ERROR.getCode())
                .path(request.getDescription(false).replace("uri=", ""))
                .build();

        return new ResponseEntity<>(errorResponse, status);
    }

    /**
     * @brief Maneja excepciones de tamaño de archivo excedido por Spring Boot.
     *
     * Esta excepción es lanzada por Spring antes de que llegue al controlador.
     * Delega a FileSizeExceededException para reutilizar la lógica de formateo.
     * @param ex la excepción de tamaño máximo de upload excedido
     * @param request la solicitud web que causó la excepción
     * @return ResponseEntity con la respuesta de error estructurada
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponse> handleMaxUploadSizeExceededException(
            MaxUploadSizeExceededException ex, WebRequest request) {

        long maxSize = ImportConstants.MAX_FILE_SIZE;
        FileSizeExceededException fileSizeException = new FileSizeExceededException(maxSize);

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.PAYLOAD_TOO_LARGE.value())
                .error("Payload Too Large")
                .message(fileSizeException.getMessage())
                .code(fileSizeException.getErrorCode().getCode())
                .path(request.getDescription(false).replace("uri=", ""))
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.PAYLOAD_TOO_LARGE);
    }

    /**
     * @brief Maneja excepciones de tamaño de archivo excedido (custom).
     *
     * @param ex la excepción de tamaño de archivo excedido
     * @param request la solicitud web que causó la excepción
     * @return ResponseEntity con la respuesta de error estructurada
     */
    @ExceptionHandler(FileSizeExceededException.class)
    public ResponseEntity<ErrorResponse> handleFileSizeExceededException(
            FileSizeExceededException ex, WebRequest request) {

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.PAYLOAD_TOO_LARGE.value())
                .error("File Too Large")
                .message(ex.getMessage())
                .code(ex.getErrorCode().getCode())
                .path(request.getDescription(false).replace("uri=", ""))
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.PAYLOAD_TOO_LARGE);
    }

    /**
     * @brief Maneja excepciones específicas de importación del catálogo de cuentas.
     *
     * @param ex la excepción de importación
     * @param request la solicitud web que causó la excepción
     * @return ResponseEntity con la respuesta de error estructurada
     */
    @ExceptionHandler(AccountCatalogueImportException.class)
    public ResponseEntity<ErrorResponse> handleAccountCatalogueImportException(
            AccountCatalogueImportException ex, WebRequest request) {

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Import Error")
                .message(ex.getMessage())
                .code(ex.getErrorCode().getCode())
                .path(request.getDescription(false).replace("uri=", ""))
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * @brief Maneja excepciones de validación de archivos.
     *
     * @param ex la excepción de validación de archivo
     * @param request la solicitud web que causó la excepción
     * @return ResponseEntity con la respuesta de error estructurada
     */
    @ExceptionHandler(FileValidationException.class)
    public ResponseEntity<ErrorResponse> handleFileValidationException(
            FileValidationException ex, WebRequest request) {

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("File Validation Error")
                .message(ex.getMessage())
                .code(ex.getErrorCode().getCode())
                .path(request.getDescription(false).replace("uri=", ""))
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * @brief Maneja excepciones de jerarquía de cuentas.
     *
     * @param ex la excepción de jerarquía de cuentas
     * @param request la solicitud web que causó la excepción
     * @return ResponseEntity con la respuesta de error estructurada
     */
    @ExceptionHandler(AccountCatalogueHierarchyException.class)
    public ResponseEntity<ErrorResponse> handleAccountCatalogueHierarchyException(
            AccountCatalogueHierarchyException ex, WebRequest request) {

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Hierarchy Error")
                .message(ex.getMessage())
                .code(ex.getErrorCode().getCode())
                .path(request.getDescription(false).replace("uri=", ""))
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * @brief Maneja excepciones de violación de integridad de datos (claves foráneas).
     *
     * @param ex la excepción de violación de integridad de datos
     * @param request la solicitud web que causó la excepción
     * @return ResponseEntity con la respuesta de error estructurada
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolationException(
            DataIntegrityViolationException ex, WebRequest request) {

        String errorMessage = ex.getMessage();

        // Verificar si es una violación de clave foránea relacionada con Tax
        if (errorMessage != null && errorMessage.contains("fkkndntrea9snpaq594re8whhmk")
                && errorMessage.contains("table \"tax\"")) {

            ErrorResponse errorResponse = ErrorResponse.builder()
                    .timestamp(LocalDateTime.now())
                    .status(HttpStatus.CONFLICT.value())
                    .error("Data Integrity Violation")
                    .message(AccountCatalogueErrorCode.ACCOUNT_ASSOCIATED_WITH_TAX.getMessage())
                    .code(AccountCatalogueErrorCode.ACCOUNT_ASSOCIATED_WITH_TAX.getCode())
                    .path(request.getDescription(false).replace("uri=", ""))
                    .build();

            return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
        }

        // Para otras violaciones de integridad, devolver error genérico
        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.CONFLICT.value())
                .error("Data Integrity Violation")
                .message("No se puede completar la operación debido a restricciones de integridad de datos")
                .code(ErrorCode.GENERIC_ERROR.getCode())
                .path(request.getDescription(false).replace("uri=", ""))
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
    }

    /**
     * @brief Maneja excepciones de referencia de propiedad inválida (campos de
     * ordenamiento inexistentes).
     *
     * @param ex la excepción de referencia de propiedad
     * @param request la solicitud web que causó la excepción
     * @return ResponseEntity con la respuesta de error estructurada
     */
    @ExceptionHandler(PropertyReferenceException.class)
    public ResponseEntity<ErrorResponse> handlePropertyReferenceException(
            PropertyReferenceException ex, WebRequest request) {

        String propertyName = ex.getPropertyName();
        String message = String.format("El campo de ordenamiento '%s' no es válido", propertyName);

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Bad Request")
                .message(message)
                .code(ErrorCode.GENERIC_ERROR.getCode())
                .path(request.getDescription(false).replace("uri=", ""))
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * @brief Mapea estado HTTP desde código de error
     *
     * Mapea un código de error a un estado HTTP apropiado basado en patrones del código.
     * @param code el código de error a mapear
     * @return HttpStatus correspondiente al código de error
     */
    private HttpStatus mapStatusFromErrorCode(String code) {
        if (code == null) {
            return HttpStatus.BAD_REQUEST;
        }
        String upper = code.toUpperCase();
        if (upper.endsWith("_NOT_FOUND") || upper.contains("NOT_FOUND")) {
            return HttpStatus.NOT_FOUND;
        }
        if (upper.endsWith("_ALREADY_EXISTS") || upper.contains("DUPLICATE") || upper.contains("ASSOCIATED")) {
            return HttpStatus.CONFLICT;
        }
        if (upper.contains("IMPORT") || upper.contains("HIERARCHY") || upper.contains("FILE")) {
            return HttpStatus.BAD_REQUEST;
        }
        return HttpStatus.BAD_REQUEST;
    }

    /**
     * @brief Maneja excepciones generales
     *
     * Maneja excepciones generales no específicas.
     * @param ex la excepción general
     * @param request la solicitud web que causó la excepción
     * @return ResponseEntity con respuesta de error interno del servidor
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex, WebRequest request) {
        ex.printStackTrace();
        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error("Internal Server Error")
                .message("Ha ocurrido un error interno del servidor")
                .code(ErrorCode.GENERIC_ERROR.getCode())
                .path(request.getDescription(false).replace("uri=", ""))
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}