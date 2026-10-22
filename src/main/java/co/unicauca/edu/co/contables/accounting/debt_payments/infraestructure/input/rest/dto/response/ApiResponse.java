package co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.input.rest.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.ToString;
import org.springframework.http.HttpStatus;

/**
 * Generic class for standardized API responses.
 * Provides a consistent structure for success and error cases.
 * @param <T> The type of the response body (payload).
 */
@Getter
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL) // Key annotation: will not include null fields in the response JSON.
public class ApiResponse<T> {
    private final boolean success;
    private final String message;
    private final String code; // An internal code for the frontend to identify the case.
    private final T data;

    private final Integer status; // Código de estado HTTP (ej. 404, 500)
    private final String path;

    private ApiResponse(boolean success, String message, String code, T data) {
        this.success = success;
        this.message = message;
        this.code = code;
        this.data = data;
        this.status = null; // No se necesita en éxito
        this.path = null;   // No se necesita en éxito
    }

    // Constructor para respuestas de ERROR (ahora incluye status y path)
    private ApiResponse(boolean success, String message, String code, Integer status, String path) {
        this.success = success;
        this.message = message;
        this.code = code;
        this.data = null; // En este caso de error, no hay 'data'
        this.status = status;
        this.path = path;
    }


    // --- METHODS FOR SUCCESS RESPONSES ---

    /**
     * Creates a success response with data and a default message.
     */
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, "Operación exitosa.", "OK", data);
    }

    /**
     * Creates a success response with data and a custom message.
     */
    public static <T> ApiResponse<T> success(T data, String message) {
        return new ApiResponse<>(true, message, "OK", data);
    }

    /**
     * Creates a success response for situations where there is no content to return (e.g., an empty list).
     * This is much better than returning an empty array [].
     */
    public static <T> ApiResponse<T> successEmpty(String message, String code) {
        return new ApiResponse<>(true, message, code, null);
    }

    // --- METHODS FOR ERROR RESPONSES ---

    /**
     * Creates an error response with a message and a code.
     */
    public static <T> ApiResponse<T> error(String message, String code, HttpStatus status, String path) {
        return new ApiResponse<>(false, message, code, status.value(), path);
    }
}
