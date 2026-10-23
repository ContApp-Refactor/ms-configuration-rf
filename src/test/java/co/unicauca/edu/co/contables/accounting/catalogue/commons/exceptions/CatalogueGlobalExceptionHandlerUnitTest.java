package co.unicauca.edu.co.contables.accounting.catalogue.commons.exceptions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.WebRequest;

import co.unicauca.edu.co.contables.commons.exceptions.ErrorResponse;

class CatalogueGlobalExceptionHandlerUnitTest {

    private final CatalogueGlobalExceptionHandler handler = new CatalogueGlobalExceptionHandler();

    private WebRequest request() {
        WebRequest request = mock(WebRequest.class);
        when(request.getDescription(false)).thenReturn("uri=/api/catalogue/payment-methods?page=0");
        return request;
    }

    @Test
    @DisplayName("Page index en inglés se traduce al español")
    void traduceIndiceDePagina() {
        ResponseEntity<ErrorResponse> response = handler.handleIllegalArgumentException(
                new IllegalArgumentException("Page index must not be less than zero"), request());

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("El índice de página no puede ser menor a cero", response.getBody().getMessage());
    }

    @Test
    @DisplayName("Page size en inglés se traduce al español")
    void traduceTamanoDePagina() {
        ResponseEntity<ErrorResponse> response = handler.handleIllegalArgumentException(
                new IllegalArgumentException("Page size must not be less than one"), request());

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("El tamaño de página debe ser mayor a cero", response.getBody().getMessage());
    }

    @Test
    @DisplayName("Otros mensajes de IllegalArgumentException se devuelven sin tocar")
    void conservaOtrosMensajes() {
        ResponseEntity<ErrorResponse> response = handler.handleIllegalArgumentException(
                new IllegalArgumentException("El código ya existe"), request());

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("El código ya existe", response.getBody().getMessage());
    }

    @Test
    @DisplayName("Mensaje nulo no revienta el manejador")
    void toleraMensajeNulo() {
        ResponseEntity<ErrorResponse> response =
                handler.handleIllegalArgumentException(new IllegalArgumentException(), request());

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
    }
}
