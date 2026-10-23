package co.unicauca.edu.co.contables.audit.application.usecases.queries;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;

import co.unicauca.edu.co.contables.audit.application.dto.request.GetSessionsRequest;
import co.unicauca.edu.co.contables.audit.application.dto.response.PageResponse;
import co.unicauca.edu.co.contables.audit.application.dto.response.SessionAuditResponse;
import co.unicauca.edu.co.contables.audit.application.internal.CombinedSession;
import co.unicauca.edu.co.contables.audit.application.internal.query.AuditSessionCriteria;
import co.unicauca.edu.co.contables.audit.application.internal.query.PageResult;
import co.unicauca.edu.co.contables.audit.application.internal.query.QueryOptions;
import co.unicauca.edu.co.contables.audit.application.port.output.AuditSessionQueryPort;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Regresion de migracion: la coleccion newman AUDITORIA-INTEGRATION-TEST
 * localiza la sesion recien publicada con
 * {@code json.data.find(s => s.sessionId === testSessionId)}. El modelo de
 * dominio {@code CombinedSession} y la proyeccion JPA
 * {@code SessionProjection} sí traen {@code sessionId}, pero la respuesta REST
 * lo descartaba al mapear, asi que la sesion nunca podia encontrarse.
 */
class GetAuditSessionsQueryImplTest {

        @Test
        void laRespuestaDeSesionesExponeElSessionId() {
                AuditSessionQueryPort queryPort = mock(AuditSessionQueryPort.class);
                GetAuditSessionsQueryImpl useCase = new GetAuditSessionsQueryImpl(queryPort);

                CombinedSession session = CombinedSession.of(
                                "session-it-123",
                                "test.user@unicauca.edu.co",
                                List.of("Administrador"),
                                Instant.parse("2026-05-15T00:00:00Z"),
                                null);

                when(queryPort.findCombinedSessions(any(AuditSessionCriteria.class), any(QueryOptions.class)))
                                .thenReturn(new PageResult<>(List.of(session), 1L));

                GetSessionsRequest request = GetSessionsRequest.builder()
                                .dateFrom(Instant.parse("2026-05-15T00:00:00Z"))
                                .dateTo(Instant.parse("2026-05-15T23:59:59Z"))
                                .userName("test.user@unicauca.edu.co")
                                .sortField("loginTime")
                                .build();

                PageResponse<SessionAuditResponse> response = useCase.execute(request);

                assertThat(response.getData()).hasSize(1);
                SessionAuditResponse first = response.getData().get(0);
                assertThat(first.getSessionId()).isEqualTo("session-it-123");
                assertThat(first.getUserName()).isEqualTo("test.user@unicauca.edu.co");
        }
}
