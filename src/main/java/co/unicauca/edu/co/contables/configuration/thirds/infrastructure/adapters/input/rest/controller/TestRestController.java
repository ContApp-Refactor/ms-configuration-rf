package co.unicauca.edu.co.contables.configuration.thirds.infrastructure.adapters.input.rest.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;




/**
 * @brief Controlador REST para pruebas de disponibilidad del servicio
 *
 * Adaptador de entrada que expone un endpoint simple para verificar
 * que el servicio de terceros está operativo y responde correctamente.
 */
@RestController
@RequestMapping("/api/thirds/test")
public class TestRestController {

    /**
     * @brief Endpoint de verificación de disponibilidad (health check)
     *
     * Método que responde con "pong" para confirmar que el servicio
     * está funcionando correctamente.
     * @return cadena "pong" indicando que el servicio está disponible
     */
    @GetMapping("/ping")
    public String ping() {
        return "pong";
    }
}
