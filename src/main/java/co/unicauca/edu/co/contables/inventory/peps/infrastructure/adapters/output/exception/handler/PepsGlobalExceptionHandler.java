package co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.output.exception.handler;

import java.util.HashMap;
import java.util.Map;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.output.exception.customized.BaseException;
import co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.output.exception.customized.BusinessRuleException;
import co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.output.exception.customized.EntityAlreadyExists;
import co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.output.exception.customized.EntityDoesNotExistException;
import co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.output.exception.customized.GenericErrorException;
import co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.output.exception.dto.ErrorResponseDTO;

import org.springframework.http.HttpStatus;


/**
 * @brief Global exception handler for the application
 * 
 * Intercepts exceptions thrown by controllers and converts them into
 * standardized JSON error responses.
 */
@RestControllerAdvice(basePackages = "co.unicauca.edu.co.contables.inventory.peps")
@Order(Ordered.HIGHEST_PRECEDENCE)
public class PepsGlobalExceptionHandler {
    

  /**
   * @brief Handles validation exceptions (e.g., @Valid failures)
   * 
   * Extracts field-specific error messages and returns them in a map.
   *
   * @param ex The MethodArgumentNotValidException instance.
   * @return Response entity containing a map of field names to error messages.
   */
  @ExceptionHandler(MethodArgumentNotValidException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
      Map<String, String> errors = new HashMap<>();
      ex.getBindingResult().getAllErrors().forEach((error) -> {
          String fieldName;
          if(error instanceof FieldError){
              fieldName=((FieldError)error).getField();
          }else{
            fieldName=error.getObjectName();
          }
          String errorMessage=error.getDefaultMessage();
          errors.put(fieldName, errorMessage);
      });
      return ResponseEntity.badRequest().body(errors);
  }

   /**
   * @brief Handles custom application exceptions
   * 
   * Processes exceptions that extend BaseException, using their specific
   * status codes and messages.
   * 
   * @param req The HTTP request that triggered the exception
   * @param e The custom exception instance
   * @return Response entity containing standardized error details
   */
   @ExceptionHandler({
      BusinessRuleException.class,
      EntityAlreadyExists.class,
      EntityDoesNotExistException.class,
      GenericErrorException.class
   })
   public ResponseEntity<ErrorResponseDTO> handleCustomExceptions(final HttpServletRequest req, BaseException e) {
     return buildErrorResponse(e.getStatus(),e.getMessage(),req);
   }

    /**
     * @brief Handles various bad request exceptions
     * 
     * Maps common Spring and Java exceptions to appropriate HTTP 400/404 responses.
     * 
     * @param req The HTTP request
     * @param ex The exception instance
     * @return Response entity containing error details
     */
    @ExceptionHandler({
      NoResourceFoundException.class,
      MissingServletRequestParameterException.class,
      MethodArgumentTypeMismatchException.class,
      IllegalArgumentException.class,
      EntityNotFoundException.class
    })
    public ResponseEntity<ErrorResponseDTO> handleBadRequestExceptions(final HttpServletRequest req,Exception ex) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        String message = ex.getMessage();

        if (ex instanceof NoResourceFoundException) {
            status = HttpStatus.NOT_FOUND;
            message = "Resource not found";
        } else if (ex instanceof MissingServletRequestParameterException) {
            message = "Missing parameter: " + ((MissingServletRequestParameterException) ex).getParameterName();
        } else if (ex instanceof MethodArgumentTypeMismatchException) {
            message = "Method argument type mismatch for '" + ((MethodArgumentTypeMismatchException) ex).getName() + "'";
        }
        
        return buildErrorResponse(status.value(), message, req);
    }

    /**
     * @brief Handles unexpected runtime exceptions
     * 
     * Acts as a catch-all for unhandled errors, returning a 500 Internal Server Error.
     * 
     * @param req The HTTP request
     * @param ex The runtime exception
     * @return Response entity with generic error message
     */
    @ExceptionHandler(RuntimeException.class)
    @ResponseBody
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<ErrorResponseDTO> handleRuntimeException(final HttpServletRequest req, RuntimeException ex) {
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Internal server error", req);
    }



    /**
     * @brief Helper method to build a consistent error response
     * @param status HTTP status code
     * @param message Error message
     * @param req HTTP request to extract metadata
     * @return Constructed ResponseEntity
     */
    private ResponseEntity<ErrorResponseDTO> buildErrorResponse(int status, String message, HttpServletRequest req) {
      return ErrorResponseDTO.builder()
          .status(status)
          .message(message)
          .url(req.getRequestURI())
          .method(req.getMethod())
          .build()
          .of();
    }




}
