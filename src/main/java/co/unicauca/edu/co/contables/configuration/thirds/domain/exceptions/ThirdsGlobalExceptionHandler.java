package co.unicauca.edu.co.contables.configuration.thirds.domain.exceptions;

import co.unicauca.edu.co.contables.configuration.thirds.domain.exceptions.third.FileSizeExceededException;
import co.unicauca.edu.co.contables.configuration.thirds.domain.exceptions.thirdType.ThirdTypeForeignKeyViolationException;
import co.unicauca.edu.co.contables.configuration.thirds.domain.exceptions.typeId.TypeIdForeignKeyViolationException;
import co.unicauca.edu.co.contables.configuration.thirds.infrastructure.adapters.input.rest.dto.response.ErrorResponse;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @brief Manejador global de excepciones para toda la aplicación
 *
 * Proporciona respuestas consistentes y descriptivas para diferentes tipos de errores,
 * convirtiendo excepciones del dominio en respuestas HTTP apropiadas.
 */
@ControllerAdvice(basePackages = "co.unicauca.edu.co.contables.configuration.thirds")
public class ThirdsGlobalExceptionHandler {

    /**
     * @brief Maneja excepciones de negocio y resuelve el estado HTTP según el código de error
     *
     * Convierte excepciones de negocio en respuestas HTTP apropiadas,
     * mapeando códigos de error específicos a estados HTTP correspondientes.
     * @param ex excepción de negocio del dominio
     * @param request solicitud web que causó la excepción
     * @return respuesta HTTP con información estructurada del error
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
     * @brief Maneja errores de deserialización JSON, incluyendo enums inválidos
     *
     * Detecta y maneja errores específicos como valores de enum inválidos,
     * especialmente para clasificaciones de persona, proporcionando mensajes claros.
     * @param ex excepción de mensaje no legible
     * @param request solicitud web que causó el error
     * @return respuesta HTTP con error de formato de solicitud
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex, WebRequest request) {

        String message = "Error en el formato de los datos enviados";
        String code = "INVALID_REQUEST_FORMAT";

        // Detectar si es un error de enum inválido
        if (ex.getCause() instanceof InvalidFormatException) {
            InvalidFormatException formatEx = (InvalidFormatException) ex.getCause();

            // Verificar si es un error de PersonClassification
            if (formatEx.getTargetType() != null &&
                formatEx.getTargetType().getSimpleName().equals("PersonClassification")) {

                message = "La clasificación de persona '" + formatEx.getValue() +
                         "' no es válida. Valores válidos: NATURAL_PERSON, LEGAL_ENTITY";
                code = "TYPE_ID_INVALID_CLASSIFICATION";
            }
        }

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Bad Request")
                .message(message)
                .code(code)
                .path(request.getDescription(false).replace("uri=", ""))
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * @brief Maneja errores cuando un parámetro requerido no está presente en la solicitud
     *
     * Procesa excepciones de parámetros faltantes en endpoints REST,
     * proporcionando mensajes claros sobre qué parámetro falta.
     * @param ex excepción de parámetro de solicitud faltante
     * @param request solicitud web que causó el error
     * @return respuesta HTTP con detalle del parámetro faltante
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingServletRequestParameter(
            MissingServletRequestParameterException ex, WebRequest request) {

        String parameterName = ex.getParameterName();
        String parameterType = ex.getParameterType();

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Bad Request")
                .message("El parámetro requerido '" + parameterName + "' de tipo " + parameterType + " no está presente")
                .code("MISSING_REQUIRED_PARAMETER")
                .path(request.getDescription(false).replace("uri=", ""))
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * @brief Maneja errores cuando una parte requerida del multipart no está presente
     *
     * Procesa excepciones cuando falta un archivo requerido en solicitudes multipart,
     * proporcionando mensajes claros sobre qué parte del request falta.
     * @param ex excepción de parte de solicitud faltante
     * @param request solicitud web que causó el error
     * @return respuesta HTTP con detalle de la parte faltante
     */
    @ExceptionHandler(MissingServletRequestPartException.class)
    public ResponseEntity<ErrorResponse> handleMissingServletRequestPart(
            MissingServletRequestPartException ex, WebRequest request) {

        String partName = ex.getRequestPartName();

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Bad Request")
                .message("El archivo requerido '" + partName + "' no está presente en la solicitud")
                .code("MISSING_REQUIRED_FILE")
                .path(request.getDescription(false).replace("uri=", ""))
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * @brief Maneja errores cuando la solicitud no es multipart
     *
     * Procesa errores cuando se espera una solicitud multipart pero no se recibe,
     * o cuando hay problemas con el procesamiento de archivos multipart.
     * @param ex excepción de multipart
     * @param request solicitud web que causó el error
     * @return respuesta HTTP indicando que se requiere un archivo
     */
    @ExceptionHandler(MultipartException.class)
    public ResponseEntity<ErrorResponse> handleMultipartException(
            MultipartException ex, WebRequest request) {

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Bad Request")
                .message("La solicitud debe incluir un archivo. Asegúrese de enviar el archivo en formato multipart/form-data")
                .code("MISSING_REQUIRED_FILE")
                .path(request.getDescription(false).replace("uri=", ""))
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * @brief Maneja errores de validación de parámetros de método (Spring 6+)
     *
     * Procesa errores de validación de parámetros en métodos de controlador,
     * como @NotBlank, @NotNull en @RequestParam o @PathVariable.
     * @param ex excepción de validación de método
     * @param request solicitud web que causó el error
     * @return respuesta HTTP con información de los errores de validación
     */
    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ErrorResponse> handleHandlerMethodValidation(
            HandlerMethodValidationException ex, WebRequest request) {

        StringBuilder messageBuilder = new StringBuilder("Error de validación: ");
        ex.getAllErrors().forEach(error -> {
            messageBuilder.append(error.getDefaultMessage()).append(". ");
        });

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Bad Request")
                .message(messageBuilder.toString().trim())
                .code("VALIDATION_ERROR")
                .path(request.getDescription(false).replace("uri=", ""))
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * @brief Maneja errores de conversión de tipos en parámetros de solicitud
     *
     * Procesa errores cuando un parámetro de solicitud no puede ser convertido
     * al tipo esperado (e.g., String a Boolean, String a Integer).
     * @param ex excepción de desajuste de tipo de argumento
     * @param request solicitud web que causó el error
     * @return respuesta HTTP con información del error de conversión
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatch(
            MethodArgumentTypeMismatchException ex, WebRequest request) {

        String paramName = ex.getName();
        String requiredType = ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "desconocido";
        Object value = ex.getValue();

        String message = String.format(
            "El parámetro '%s' tiene un valor inválido '%s'. Se esperaba un valor de tipo %s",
            paramName, value, requiredType
        );

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Bad Request")
                .message(message)
                .code("INVALID_PARAMETER_TYPE")
                .path(request.getDescription(false).replace("uri=", ""))
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }
    
    /**
     * @brief Maneja errores de validación de payload (Bean Validation en @RequestBody con @Valid)
     *
     * Procesa errores de validación de campos en objetos DTO, incluyendo
     * manejo especial para clasificaciones de persona requeridas.
     * @param ex excepción de argumento de método no válido
     * @param request solicitud web que causó el error
     * @return respuesta HTTP con errores de campo detallados
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

        // Detectar si es un error específico de clasificación
        String message = "Error de validación de campos";
        String code = ErrorCode.GENERIC_ERROR.getCode();

        if (fieldErrors.containsKey("classification")) {
            message = "La clasificación de persona no puede ser nula. Debe especificar: NATURAL_PERSON o LEGAL_ENTITY";
            code = "TYPE_ID_INVALID_CLASSIFICATION";
        }

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(message)
                .code(code)
                .path(request.getDescription(false).replace("uri=", ""))
                .build();

        return new ResponseEntity<>(Map.of(
                "error", errorResponse,
                "fieldErrors", fieldErrors
        ), status);
    }

    /**
     * @brief Maneja errores de validación a nivel de parámetros (e.g., @RequestParam, @PathVariable)
     *
     * Procesa violaciones de restricciones Bean Validation en parámetros
     * de métodos REST, proporcionando detalles específicos de cada violación.
     * @param ex excepción de violación de restricción
     * @param request solicitud web que causó el error
     * @return respuesta HTTP con violaciones de restricción detalladas
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
     * @brief Maneja excepciones de integridad de datos, especialmente violaciones de clave foránea
     *
     * Detecta y convierte violaciones de integridad de base de datos en excepciones
     * de negocio específicas, especialmente para claves foráneas relacionadas
     * con tipos de tercero e identificación.
     * @param ex excepción de violación de integridad de datos
     * @param request solicitud web que causó el error
     * @return respuesta HTTP con error de integridad de datos
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(
            DataIntegrityViolationException ex, WebRequest request) {

        String message = ex.getMessage();
        String errorCode = ErrorCode.GENERIC_ERROR.getCode();
        HttpStatus status = HttpStatus.BAD_REQUEST;

        // Detectar violaciones de clave foránea específicas
        if (message != null) {
            if (message.contains("fk_thirds_third_types") || message.contains("tt_id")) {
                // Extraer el ID del tipo de tercero del mensaje de error si es posible
                String thirdTypeId = extractIdFromConstraintMessage(message, "tt_id");
                if (thirdTypeId == null) {
                    thirdTypeId = "desconocido";
                }

                ThirdTypeForeignKeyViolationException customEx =
                    new ThirdTypeForeignKeyViolationException(thirdTypeId, ex);
                return handleBusinessExceptions(customEx, request);
            }

            if (message.contains("fk_thirds_type_id") || message.contains("ti_id")) {
                // Extraer el ID del tipo de identificación del mensaje de error si es posible
                String typeId = extractIdFromConstraintMessage(message, "ti_id");
                if (typeId == null) {
                    typeId = "desconocido";
                }

                TypeIdForeignKeyViolationException customEx =
                    new TypeIdForeignKeyViolationException(typeId, ex);
                return handleBusinessExceptions(customEx, request);
            }
        }

        // Para otras violaciones de integridad de datos
        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(status.value())
                .error(status.getReasonPhrase())
                .message("Error de integridad de datos: " + (message != null ? message : "Violación de restricción"))
                .code(errorCode)
                .path(request.getDescription(false).replace("uri=", ""))
                .build();

        return new ResponseEntity<>(errorResponse, status);
    }

    /**
     * @brief Extrae el ID de una restricción de clave foránea del mensaje de error
     *
     * Analiza mensajes de error de base de datos para extraer IDs específicos
     * de claves foráneas violadas, utilizando expresiones regulares para
     * diferentes formatos de mensajes de PostgreSQL y MySQL.
     * @param message mensaje de error de la base de datos
     * @param columnName nombre de la columna de clave foránea
     * @return ID extraído o null si no se puede extraer
     */
    private String extractIdFromConstraintMessage(String message, String columnName) {
        try {
            // Buscar patrones comunes en mensajes de error de PostgreSQL/MySQL
            String[] patterns = {
                "\\(" + columnName + "\\)=\\(([^)]+)\\)",
                "'" + columnName + "'='([^']+)'",
                columnName + "=([^\\s,)]+)"
            };

            for (String pattern : patterns) {
                java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern);
                java.util.regex.Matcher m = p.matcher(message);
                if (m.find()) {
                    return m.group(1);
                }
            }
        } catch (Exception e) {
            // Si no se puede extraer, devolver null
        }
        return null;
    }

    /**
     * @brief Maneja excepciones cuando un archivo excede el tamaño máximo permitido
     *
     * Convierte excepciones de Spring sobre límites de carga de archivos
     * en excepciones de negocio personalizadas con mensajes más descriptivos.
     * @param ex excepción de tamaño máximo de carga excedido
     * @param request solicitud web que causó el error
     * @return respuesta HTTP delegada al manejador de excepciones de negocio
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponse> handleMaxUploadSizeExceeded(
            MaxUploadSizeExceededException ex, WebRequest request) {

        // Extraer el tamaño máximo permitido
        long maxSize = ex.getMaxUploadSize();

        // Crear excepción personalizada
        FileSizeExceededException customEx = new FileSizeExceededException(
            maxSize > 0 ? maxSize : 5242880
        );

        return handleBusinessExceptions(customEx, request);
    }

    /**
     * @brief Mapea códigos de error a estados HTTP apropiados
     *
     * Convierte códigos de error específicos del dominio en códigos de estado HTTP
     * estándar para proporcionar respuestas REST apropiadas.
     * @param code código de error del dominio
     * @return estado HTTP correspondiente al tipo de error
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
        if (upper.contains("PDF_RUT_INVALID_FORMAT") || upper.contains("INVALID_FORMAT")) {
            return HttpStatus.BAD_REQUEST;
        }
        if (upper.contains("FILE_SIZE_EXCEEDED")) {
            return HttpStatus.PAYLOAD_TOO_LARGE;
        }
        return HttpStatus.BAD_REQUEST;
    }

    /**
     * @brief Maneja excepciones de argumentos inválidos
     *
     * Procesa excepciones cuando se reciben parámetros con valores no válidos,
     * como números de página negativos o tamaños de página inválidos.
     * @param ex excepción de argumento ilegal
     * @param request solicitud web que causó el error
     * @return respuesta HTTP con error de validación
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(
            IllegalArgumentException ex, WebRequest request) {

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Bad Request")
                .message(ex.getMessage())
                .code("INVALID_PARAMETER_VALUE")
                .path(request.getDescription(false).replace("uri=", ""))
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * @brief Maneja excepciones generales no específicas
     *
     * Captura cualquier excepción no manejada específicamente por otros
     * métodos del manejador, proporcionando una respuesta genérica de error interno.
     * @param ex excepción general no específica
     * @param request solicitud web que causó el error
     * @return respuesta HTTP con error interno del servidor
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