package co.unicauca.edu.co.contables.accounting.facture.infraestructure.adapters.input.rest;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import co.unicauca.edu.co.contables.configuration.thirds.application.ports.output.ThirdOutputPort;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import org.springframework.beans.factory.annotation.Autowired;

@RestController
@RequestMapping("/api/factures/test")
public class FactureTestRestController {

    /**
     * Se inyecta el puerto de salida del módulo de terceros: dentro del
     * monolito la consulta es una llamada a un bean local, no una petición HTTP.
     */
    @Autowired
    private ThirdOutputPort thirdOutputPort;

    @GetMapping("/ping")
    @Operation(summary = "Ping", description = "Prueba básica accesible para cualquier usuario para verificar la disponibilidad del servidor.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Servidor responde correctamente", content = @Content(mediaType = "text/plain"))
    })
    public String testAll() {
        return "pong";
    }

    @GetMapping("/testExternal")
    @CircuitBreaker(name = "external", fallbackMethod = "fallback")
    @Operation(summary = "Prueba de consulta de terceros", description = "Consulta el tercero de demostración dentro del monolito. Usa un Circuit Breaker para manejar fallas.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Consulta exitosa", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "500", description = "Error interno en la consulta", content = @Content)
    })
    public JsonNode getEnterprises() {
        var tercero = thirdOutputPort.findById(1L);
        return new ObjectMapper().valueToTree(tercero);
    }

    /**
     * Método de fallback para manejar fallas de la consulta.
     *
     * @param t Excepción capturada.
     * @return Respuesta de fallback con un mensaje de error.
     */
    public String fallback(Throwable t) {
        return "Fallback: " + t.getMessage();
    }

}
