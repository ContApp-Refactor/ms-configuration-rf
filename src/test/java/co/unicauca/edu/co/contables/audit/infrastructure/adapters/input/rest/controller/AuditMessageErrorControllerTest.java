package co.unicauca.edu.co.contables.audit.infrastructure.adapters.input.rest.controller;

import java.time.Instant;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import co.unicauca.edu.co.contables.audit.application.dto.response.MessageErrorAuditResponse;
import co.unicauca.edu.co.contables.audit.application.port.input.commands.DeleteAllMessageErrorsCommand;
import co.unicauca.edu.co.contables.audit.application.port.input.queries.GetLatestMessageErrorQuery;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Contrato REST de los mensajes de error de auditoria, verificado contra la
 * coleccion newman AUDITORIA-INTEGRATION-TEST:
 *
 * <ul>
 * <li>GET /api/audit/message-errors/latest -> 200 con JSON plano (id numerico,
 * eventType, entityType, errorDescription, messageData, errorStage, errorAt
 * ISO-8601).</li>
 * <li>DELETE /api/audit/message-errors -> 204 sin cuerpo.</li>
 * <li>GET /latest sin registros -> 404.</li>
 * </ul>
 *
 * Setup standalone (sin contexto Spring) para que el test sea deterministico y
 * rapido: el controller no depende de seguridad ni de la base.
 */
class AuditMessageErrorControllerTest {

    private GetLatestMessageErrorQuery getLatestMessageErrorQuery;
    private DeleteAllMessageErrorsCommand deleteAllMessageErrorsCommand;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        getLatestMessageErrorQuery = mock(GetLatestMessageErrorQuery.class);
        deleteAllMessageErrorsCommand = mock(DeleteAllMessageErrorsCommand.class);

        AuditMessageErrorController controller = new AuditMessageErrorController(
                getLatestMessageErrorQuery, deleteAllMessageErrorsCommand);

        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void latestRespondeOkConJsonPlano() throws Exception {
        MessageErrorAuditResponse response = MessageErrorAuditResponse.builder()
                .id(7L)
                .eventType("LOGIN")
                .entityType("Session")
                .errorDescription("Validation failed: userName missing")
                .messageData("{\"user_name\":null}")
                .errorStage("VALIDATION")
                .errorAt(Instant.parse("2026-10-14T03:57:47Z").toString())
                .build();

        when(getLatestMessageErrorQuery.execute()).thenReturn(Optional.of(response));

        mockMvc.perform(get("/api/audit/message-errors/latest"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.eventType").value("LOGIN"))
                .andExpect(jsonPath("$.entityType").value("Session"))
                .andExpect(jsonPath("$.errorDescription").isString())
                .andExpect(jsonPath("$.messageData").isString())
                .andExpect(jsonPath("$.errorStage").value("VALIDATION"))
                .andExpect(jsonPath("$.errorAt").isString());
    }

    @Test
    void latestSinRegistrosRespondeNotFound() throws Exception {
        when(getLatestMessageErrorQuery.execute()).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/audit/message-errors/latest"))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteRespondeNoContent() throws Exception {
        mockMvc.perform(delete("/api/audit/message-errors"))
                .andExpect(status().isNoContent());

        verify(deleteAllMessageErrorsCommand).execute();
    }
}
