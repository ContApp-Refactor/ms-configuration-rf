package co.unicauca.edu.co.contables.accounting.catalogue.catalogue.infraestructure.adapters.input.rest.controller;

//import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @brief Controlador de pruebas para testing de endpoints
 *
 * Proporciona endpoints simples para verificar funcionamiento básico
 * y validación de autorizaciones en el módulo de catálogo de cuentas.
 */
@RestController
@RequestMapping("/api/accountCatalogue/test")
public class AccountCatalogueTestController {

    @GetMapping("/testAdmin")
    //@PreAuthorize("hasRole('admin_client')")
    public String test() {
        return "Test for Admin";
    }

    @GetMapping("/testUser")
    //@PreAuthorize("hasRole('user_client') or hasRole('admin_client')")
    public String testUser() {
        return "Test for User";
    }

    @GetMapping("/ping")
    public String ping() {
        return "pong";
    }

}
