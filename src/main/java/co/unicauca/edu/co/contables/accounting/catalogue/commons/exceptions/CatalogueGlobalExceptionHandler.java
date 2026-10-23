package co.unicauca.edu.co.contables.accounting.catalogue.commons.exceptions;

import co.unicauca.edu.co.contables.commons.exceptions.ErrorCode;
import co.unicauca.edu.co.contables.commons.exceptions.ErrorResponse;
import java.time.LocalDateTime;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

/**
 * @brief Manejador de excepciones del módulo catálogo (origen: account-catalogue-rf).
 *
 * El microservicio origen traducía los mensajes de error de paginación de Spring
 * Data al español, y sus colecciones Postman los asertan en español
 * ("Metodos de pago": "índice de página no puede ser menor a cero"). En el monolito
 * los demás módulos (origen ms-configuration-rf, products-management-rf) asertan el
 * mensaje inglés crudo, así que la traducción NO puede vivir en el handler global:
 * se acota acá, al mismo paquete que tenía el microservicio origen.
 *
 * @Order(HIGHEST_PRECEDENCE) lo hace ganarle al {@code commons.GlobalExceptionHandler}
 * solo para los controllers de {@code accounting.catalogue}; para cualquier otra
 * excepción no declara método y Spring sigue buscando en los demás advice.
 */
@Order(Ordered.HIGHEST_PRECEDENCE)
@ControllerAdvice(basePackages = "co.unicauca.edu.co.contables.accounting.catalogue")
public class CatalogueGlobalExceptionHandler {

    /**
     * @brief Maneja errores de argumentos ilegales, en particular los de paginación.
     *
     * Spring Data lanza {@code IllegalArgumentException} con el mensaje en inglés
     * ("Page index must not be less than zero"). Este módulo lo presenta en español,
     * igual que el microservicio origen.
     *
     * @param ex excepción de argumento ilegal
     * @param request solicitud web que causó la excepción
     * @return respuesta HTTP 400 con el mensaje traducido
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(
            IllegalArgumentException ex, WebRequest request) {

        String message = ex.getMessage();

        // Mensajes específicos para errores de paginación
        if (message != null && message.contains("Page index must not be less than zero")) {
            message = "El índice de página no puede ser menor a cero";
        } else if (message != null && message.contains("Page size must not be less than one")) {
            message = "El tamaño de página debe ser mayor a cero";
        }

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .message(message)
                .code(ErrorCode.GENERIC_ERROR.getCode())
                .path(request.getDescription(false).replace("uri=", ""))
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }
}
