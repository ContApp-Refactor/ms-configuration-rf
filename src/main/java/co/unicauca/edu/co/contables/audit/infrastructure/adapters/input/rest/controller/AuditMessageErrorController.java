package co.unicauca.edu.co.contables.audit.infrastructure.adapters.input.rest.controller;

import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import co.unicauca.edu.co.contables.audit.application.dto.response.MessageErrorAuditResponse;
import co.unicauca.edu.co.contables.audit.application.port.input.commands.DeleteAllMessageErrorsCommand;
import co.unicauca.edu.co.contables.audit.application.port.input.queries.GetLatestMessageErrorQuery;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Exposicion REST de los errores de procesamiento de mensajes de auditoria.
 *
 * Rutas y forma de la respuesta fijadas por la coleccion newman
 * AUDITORIA-INTEGRATION-TEST: {@code GET /latest} devuelve JSON plano y
 * {@code DELETE} responde 204 sin cuerpo.
 */
@RestController
@RequestMapping("/api/audit/message-errors")
@RequiredArgsConstructor
@Slf4j
public class AuditMessageErrorController {

    private final GetLatestMessageErrorQuery getLatestMessageErrorQuery;
    private final DeleteAllMessageErrorsCommand deleteAllMessageErrorsCommand;

    /**
     * @brief Devuelve el error de procesamiento de mensajes mas reciente.
     * @return 200 con el error en JSON plano, o 404 si no hay registros.
     */
    @GetMapping("/latest")
    @PreAuthorize("hasAnyRole('Administrador', 'Profesor')")
    public ResponseEntity<MessageErrorAuditResponse> findLatest() {
        Optional<MessageErrorAuditResponse> latest = getLatestMessageErrorQuery.execute();

        return latest.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * @brief Elimina todos los registros de errores de procesamiento.
     * @return 204 sin cuerpo.
     */
    @DeleteMapping
    @PreAuthorize("hasAnyRole('Administrador', 'Profesor')")
    public ResponseEntity<Void> deleteAll() {
        deleteAllMessageErrorsCommand.execute();
        log.info("Todos los errores de procesamiento de mensajes de auditoria fueron eliminados");
        return ResponseEntity.noContent().build();
    }
}
