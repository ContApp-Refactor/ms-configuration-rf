package co.unicauca.edu.co.contables.keycloak.config;

import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Pruebas para {@link KeycloakInputSanitizerAdvice}.
 * <p>
 * Verifica que el {@code @InitBinder} con {@code StringTrimmerEditor(true)}
 * recorta espacios en blanco y convierte cadenas vacías o solo espacios en
 * {@code null}. Se usa un controlador de prueba mínimo y {@link MockMvc}
 * para ejercer el binding real de Spring, igual que el patrón de
 * {@code SubjectControllerAdviceTest} pero enfocado en binding en lugar de
 * orden de excepciones.
 * </p>
 */
class KeycloakInputSanitizerAdviceTest {

    /**
     * Controlador de prueba para ejercer el binding de String.
     */
    @RestController
    static class TestBindingController {

        @GetMapping("/test/bind")
        public String bind(@RequestParam(required = false) String value) {
            return value == null ? "NULL" : "'" + value + "'";
        }
    }

    private final MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new TestBindingController())
            .setControllerAdvice(new KeycloakInputSanitizerAdvice())
            .build();

    @Test
    void stringSoloEspaciosSeConvierteEnNull() throws Exception {
        mockMvc.perform(get("/test/bind").param("value", "   "))
                .andExpect(status().isOk())
                .andExpect(content().string("NULL"));
    }

    @Test
    void stringConEspaciosAlrededorSeRecorta() throws Exception {
        mockMvc.perform(get("/test/bind").param("value", "  hola  "))
                .andExpect(status().isOk())
                .andExpect(content().string("'hola'"));
    }

    @Test
    void stringVacioSeConvierteEnNull() throws Exception {
        mockMvc.perform(get("/test/bind").param("value", ""))
                .andExpect(status().isOk())
                .andExpect(content().string("NULL"));
    }

    @Test
    void stringNormalNoSeModifica() throws Exception {
        mockMvc.perform(get("/test/bind").param("value", "sin espacios"))
                .andExpect(status().isOk())
                .andExpect(content().string("'sin espacios'"));
    }

    @Test
    void parametroAusenteSigueSiendoNull() throws Exception {
        mockMvc.perform(get("/test/bind"))
                .andExpect(status().isOk())
                .andExpect(content().string("NULL"));
    }
}