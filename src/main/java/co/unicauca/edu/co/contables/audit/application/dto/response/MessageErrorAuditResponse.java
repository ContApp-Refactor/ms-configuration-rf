package co.unicauca.edu.co.contables.audit.application.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Representacion plana de un error de procesamiento de mensajes de auditoria.
 *
 * El contrato lo fija la coleccion newman AUDITORIA-INTEGRATION-TEST: JSON
 * plano (sin envoltorio), con {@code id} numerico y {@code errorAt} como texto
 * ISO-8601. No se usa el {@code ApiResponse} envolvente del resto del monolito
 * porque cambiaria la forma que la coleccion ya valida.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MessageErrorAuditResponse {

    private Long id;
    private String eventType;
    private String entityType;
    private String errorDescription;
    private String messageData;
    private String errorStage;
    private String errorAt;
}
