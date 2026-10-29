package co.unicauca.edu.co.contables.billing.application.service;

import co.unicauca.edu.co.contables.billing.application.ports.input.SaveReturnDraftUseCase;
import co.unicauca.edu.co.contables.billing.application.ports.input.command.ReturnDraftCommand;
import co.unicauca.edu.co.contables.billing.application.ports.input.command.ReturnLineCommand;
import co.unicauca.edu.co.contables.billing.application.ports.output.BillingClockPort;
import co.unicauca.edu.co.contables.billing.application.ports.output.BillingDocumentRepositoryPort;
import co.unicauca.edu.co.contables.billing.application.ports.output.BillingExecutionContextPort;
import co.unicauca.edu.co.contables.billing.application.ports.output.BillingIdGeneratorPort;
import co.unicauca.edu.co.contables.billing.domain.exception.BillingErrorCode;
import co.unicauca.edu.co.contables.billing.domain.exception.BillingException;
import co.unicauca.edu.co.contables.billing.domain.model.BillingDocument;
import co.unicauca.edu.co.contables.billing.domain.model.DocumentLine;
import co.unicauca.edu.co.contables.billing.domain.model.DocumentStatus;
import co.unicauca.edu.co.contables.billing.domain.model.DocumentType;
import co.unicauca.edu.co.contables.billing.domain.model.Quantity;
import co.unicauca.edu.co.contables.billing.domain.service.ReturnAvailabilityCalculator;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * Crea y actualiza (autosave) borradores de devolucion (RET-1..4, RET-6). Java plano: la
 * transaccion la aporta el adaptador de entrada (D3).
 *
 * <p>Orden de cada guardado: cargar la factura original (del tipo que corresponde a la devolucion;
 * una de otro tipo se comporta como inexistente) -> validar su estado -> convertir las lineas pedidas
 * rechazando una referencia desconocida o repetida ANTES de heredar de la original -> consultar las
 * demas devoluciones y validar la disponibilidad (solo cuentan las EMITIDA) -> reemplazar en el
 * dominio -> persistir. El control es de UX: la fuente de verdad se revalida al emitir con la
 * original bloqueada (D12).
 */
public class BillingReturnDraftService implements SaveReturnDraftUseCase {

    private final BillingDocumentRepositoryPort repository;
    private final BillingDocumentLoader loader;
    private final BillingExecutionContextPort context;
    private final BillingClockPort clock;
    private final BillingIdGeneratorPort ids;
    private final ReturnAvailabilityCalculator calculator = new ReturnAvailabilityCalculator();

    public BillingReturnDraftService(BillingDocumentRepositoryPort repository, BillingDocumentLoader loader,
            BillingExecutionContextPort context, BillingClockPort clock, BillingIdGeneratorPort ids) {
        this.repository = Objects.requireNonNull(repository, "repository");
        this.loader = Objects.requireNonNull(loader, "loader");
        this.context = Objects.requireNonNull(context, "context");
        this.clock = Objects.requireNonNull(clock, "clock");
        this.ids = Objects.requireNonNull(ids, "ids");
    }

    @Override
    public BillingDocument create(String enterpriseId, DocumentType type, ReturnDraftCommand command) {
        requireReturnType(type);
        Objects.requireNonNull(command, "command");
        if (command.originalDocumentId() == null) {
            throw new BillingException(BillingErrorCode.BILLING_VALIDATION_ERROR,
                    "La devolucion requiere la factura original");
        }
        BillingDocument original = loader.load(enterpriseId, type.originType(), command.originalDocumentId());

        Instant now = clock.now();
        String user = context.username();
        // newReturnDraft no admite fecha nula: sin fecha se usa hoy (RET-1)
        BillingDocument draft = BillingDocument.newReturnDraft(ids.newId(), type, original,
                command.issueDate() != null ? command.issueDate() : clock.today(), now, user);

        List<DocumentLine> lines = inheritLines(original, command.lines());
        calculator.assertWithinAvailability(original, repository.findReturnsOf(enterpriseId, original.id()), lines);
        draft.replaceReturnDraft(command.issueDate(), command.reason(), lines, now, user);
        return repository.save(draft);
    }

    @Override
    public BillingDocument update(String enterpriseId, DocumentType type, UUID id, ReturnDraftCommand command) {
        requireReturnType(type);
        Objects.requireNonNull(command, "command");
        BillingDocument draft = loader.load(enterpriseId, type, id, command.expectedVersion());
        if (draft.status() != DocumentStatus.BORRADOR) {
            throw new BillingException(BillingErrorCode.BILLING_INVALID_STATE,
                    "Solo se puede modificar un borrador (estado actual: " + draft.status() + ")");
        }
        UUID originalId = draft.original().id();
        if (command.originalDocumentId() != null && !command.originalDocumentId().equals(originalId)) {
            throw new BillingException(BillingErrorCode.BILLING_VALIDATION_ERROR,
                    "No se puede cambiar la factura original de una devolucion");
        }
        BillingDocument original = loader.load(enterpriseId, type.originType(), originalId);
        if (original.status() != DocumentStatus.EMITIDA) {
            throw new BillingException(BillingErrorCode.BILLING_ORIGINAL_NOT_EMITTED,
                    "Solo se puede devolver contra una factura emitida (estado actual: " + original.status() + ")");
        }

        List<DocumentLine> lines = inheritLines(original, command.lines());
        List<BillingDocument> others = new ArrayList<>();
        for (BillingDocument other : repository.findReturnsOf(enterpriseId, originalId)) {
            if (!other.id().equals(draft.id())) {
                others.add(other);
            }
        }
        calculator.assertWithinAvailability(original, others, lines);
        draft.replaceReturnDraft(command.issueDate(), command.reason(), lines, clock.now(), context.username());
        return repository.save(draft);
    }

    // ---------- internos ----------

    /**
     * Convierte las lineas pedidas en lineas de devolucion heredadas de la original (RET-2). Una
     * referencia repetida es INVALID_LINE y una desconocida RETURN_LINE_NOT_FOUND, ambas ANTES de
     * heredar; despues {@code forReturn} valida cantidad > 0 y que no supere lo vendido en la linea.
     */
    private static List<DocumentLine> inheritLines(BillingDocument original, List<ReturnLineCommand> requested) {
        if (requested == null || requested.isEmpty()) {
            return List.of();
        }
        Map<Integer, DocumentLine> sourceByLineNo = new HashMap<>();
        for (DocumentLine line : original.lines()) {
            sourceByLineNo.put(line.lineNo(), line);
        }
        Set<Integer> seen = new HashSet<>();
        List<DocumentLine> lines = new ArrayList<>(requested.size());
        int lineNo = 1;
        for (ReturnLineCommand command : requested) {
            Objects.requireNonNull(command, "line");
            if (!seen.add(command.sourceLineNo())) {
                throw new BillingException(BillingErrorCode.BILLING_INVALID_LINE,
                        "La linea " + command.sourceLineNo() + " de la factura original esta repetida");
            }
            DocumentLine source = sourceByLineNo.get(command.sourceLineNo());
            if (source == null) {
                throw new BillingException(BillingErrorCode.BILLING_RETURN_LINE_NOT_FOUND,
                        "La factura no tiene la linea " + command.sourceLineNo());
            }
            lines.add(DocumentLine.forReturn(lineNo++, source, Quantity.of(command.quantity())));
        }
        return lines;
    }

    private static void requireReturnType(DocumentType type) {
        if (type == null || !type.isReturn()) {
            throw new BillingException(BillingErrorCode.BILLING_VALIDATION_ERROR,
                    "El tipo " + type + " no es una devolucion");
        }
    }
}
