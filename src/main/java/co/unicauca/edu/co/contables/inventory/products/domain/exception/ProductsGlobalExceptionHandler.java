package co.unicauca.edu.co.contables.inventory.products.domain.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import co.unicauca.edu.co.contables.inventory.products.domain.exception.product.ProductFileSizeExceededException;
import co.unicauca.edu.co.contables.inventory.products.infraestructure.input.rest.dto.response.ErrorResponse;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @brief Manejador de excepciones específico para el módulo de productos
 *
 * Centraliza el manejo de excepciones en los controladores de productos, proporcionando
 * respuestas HTTP consistentes y mapeo automático de códigos de error a estados HTTP.
 */
@ControllerAdvice(basePackages = "co.unicauca.edu.co.contables.inventory.products")
public class ProductsGlobalExceptionHandler {

    /**
     * @brief Maneja excepciones de negocio del dominio
     *
     * Procesa excepciones BaseBusinessException, mapea códigos de error a estados HTTP
     * y construye respuestas de error estructuradas con información completa.
     *
     * @param ex Excepción de negocio lanzada
     * @param request Información de la solicitud HTTP
     * @return Respuesta HTTP con error formateado
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
     * @brief Maneja errores de validación Bean Validation
     *
     * Procesa errores de validación en objetos @RequestBody marcados con @Valid,
     * recopilando errores de campo individuales y retornando respuesta estructurada.
     *
     * @param ex Excepción de validación de argumentos de método
     * @param request Información de la solicitud HTTP
     * @return Respuesta HTTP con errores de campo detallados
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
     * Procesa violaciones de restricciones en parámetros de método como @RequestParam
     * y @PathVariable, recopilando todas las violaciones encontradas.
     *
     * @param ex Excepción de violación de restricciones
     * @param request Información de la solicitud HTTP
     * @return Respuesta HTTP con violaciones de validación detalladas
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
     * @brief Maneja errores de integridad de datos
     *
     * Procesa excepciones de violación de integridad de datos de la base de datos,
     * generalmente causadas por restricciones de unicidad o claves foráneas.
     *
     * @param ex Excepción de violación de integridad de datos
     * @param request Información de la solicitud HTTP
     * @return Respuesta HTTP con error de integridad de datos
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(
            DataIntegrityViolationException ex, WebRequest request) {

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Bad Request")
                .message("Error de integridad de datos: " + (ex.getMessage() != null ? ex.getMessage() : "Violación de restricción"))
                .code(ErrorCode.GENERIC_ERROR.getCode())
                .path(request.getDescription(false).replace("uri=", ""))
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * @brief Maneja archivos que exceden tamaño máximo de subida
     *
     * Convierte la excepción de Spring en una excepción personalizada de negocio
     * y delega el manejo al método de excepciones de negocio estándar.
     *
     * @param ex Excepción de tamaño de archivo excedido
     * @param request Información de la solicitud HTTP
     * @return Respuesta HTTP delegada al manejo de excepciones de negocio
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponse> handleMaxUploadSizeExceeded(
            MaxUploadSizeExceededException ex, WebRequest request) {

        // Extraer el tamaño máximo permitido
        long maxSize = ex.getMaxUploadSize();
        
        // Crear excepción personalizada
        ProductFileSizeExceededException customEx = new ProductFileSizeExceededException(
            maxSize > 0 ? maxSize : 5242880
        );

        return handleBusinessExceptions(customEx, request);
    }

    /**
     * @brief Maneja errores de parámetro de solicitud faltante
     *
     * Procesa errores cuando un parámetro requerido de la solicitud
     * (query param) no está presente.
     *
     * @param ex Excepción de parámetro de solicitud faltante
     * @param request Información de la solicitud HTTP
     * @return Respuesta HTTP con error de parámetro faltante
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingServletRequestParameter(
            MissingServletRequestParameterException ex, WebRequest request) {

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Bad Request")
                .message("El parámetro requerido '" + ex.getParameterName() + "' no está presente")
                .code("MISSING_REQUEST_PARAMETER")
                .path(request.getDescription(false).replace("uri=", ""))
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * @brief Maneja errores de parte faltante en solicitud multipart
     *
     * Procesa errores cuando una parte requerida del formulario multipart
     * (como un archivo) no está presente en la solicitud.
     *
     * @param ex Excepción de parte de solicitud faltante
     * @param request Información de la solicitud HTTP
     * @return Respuesta HTTP con error de parte faltante
     */
    @ExceptionHandler(MissingServletRequestPartException.class)
    public ResponseEntity<ErrorResponse> handleMissingServletRequestPart(
            MissingServletRequestPartException ex, WebRequest request) {

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Bad Request")
                .message("La parte requerida '" + ex.getRequestPartName() + "' no está presente en la solicitud")
                .code("MISSING_REQUEST_PART")
                .path(request.getDescription(false).replace("uri=", ""))
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * @brief Maneja errores de deserialización JSON
     *
     * Procesa errores cuando el cuerpo de la solicitud HTTP no puede ser
     * deserializado correctamente (JSON malformado, tipos incorrectos, etc.).
     *
     * @param ex Excepción de mensaje HTTP no legible
     * @param request Información de la solicitud HTTP
     * @return Respuesta HTTP con error de formato de datos
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex, WebRequest request) {

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Bad Request")
                .message("Error en el formato de los datos enviados")
                .code("INVALID_REQUEST_FORMAT")
                .path(request.getDescription(false).replace("uri=", ""))
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * @brief Mapea códigos de error a estados HTTP apropiados
     *
     * Convierte códigos de error de negocio en códigos de estado HTTP
     * semánticamente correctos para respuestas REST.
     *
     * @param code Código de error a mapear
     * @return Estado HTTP correspondiente al tipo de error
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
        if (upper.contains("INVALID_FORMAT") || upper.contains("VALIDATION_ERROR")) {
            return HttpStatus.BAD_REQUEST;
        }
        if (upper.contains("FILE_SIZE_EXCEEDED")) {
            return HttpStatus.PAYLOAD_TOO_LARGE;
        }
        return HttpStatus.BAD_REQUEST;
    }

    /**
     * @brief Maneja excepciones de argumentos ilegales
     *
     * Procesa excepciones IllegalArgumentException, típicamente lanzadas
     * por validaciones de parámetros como paginación negativa.
     *
     * @param ex Excepción de argumento ilegal
     * @param request Información de la solicitud HTTP
     * @return Respuesta HTTP con error de validación de parámetros
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(
            IllegalArgumentException ex, WebRequest request) {

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Bad Request")
                .message(ex.getMessage())
                .code("INVALID_PARAMETER")
                .path(request.getDescription(false).replace("uri=", ""))
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * @brief Maneja excepciones de recurso no encontrado
     *
     * Procesa NoResourceFoundException que ocurre cuando una URL tiene
     * segmentos vacíos o el recurso no existe en el sistema.
     *
     * @param ex Excepción de recurso no encontrado
     * @param request Información de la solicitud HTTP
     * @return Respuesta HTTP con error de recurso no encontrado
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoResourceFoundException(
            NoResourceFoundException ex, WebRequest request) {

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.NOT_FOUND.value())
                .error("Not Found")
                .message("El recurso solicitado no fue encontrado. Verifique que los parámetros de la URL no estén vacíos.")
                .code("RESOURCE_NOT_FOUND")
                .path(request.getDescription(false).replace("uri=", ""))
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    /**
     * @brief Maneja excepciones genéricas no manejadas específicamente
     *
     * Captura cualquier excepción no manejada por los otros métodos,
     * retornando un error interno del servidor genérico por seguridad.
     *
     * @param ex Excepción genérica no manejada específicamente
     * @param request Información de la solicitud HTTP
     * @return Respuesta HTTP con error interno del servidor
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex, WebRequest request) {

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
