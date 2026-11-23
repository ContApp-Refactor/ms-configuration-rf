package co.unicauca.edu.co.contables.keycloak.controller;

import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Pruebas para {@link KeycloakTestController}.
 * <p>
 * Verifica los endpoints de prueba bajo {@code /api/keycloak/test}.
 * El endpoint {@code /ping} no requiere autenticación y sirve como prueba
 * básica de conectividad. Los endpoints {@code /testAdmin} y {@code /testUser}
 * están protegidos con {@code @PreAuthorize}; probarlos requeriría
 * configuración de Spring Security Test (@WithMockUser, etc.) que
 * escapa al alcance de una prueba unitaria enfocada y barata.
 * </p>
 */
class KeycloakTestControllerTest {

    private final MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new KeycloakTestController())
            .build();

    @Test
    void pingDevuelve200ConPong() throws Exception {
        mockMvc.perform(get("/api/keycloak/test/ping"))
                .andExpect(status().isOk())
                .andExpect(content().string("pong"));
    }
}