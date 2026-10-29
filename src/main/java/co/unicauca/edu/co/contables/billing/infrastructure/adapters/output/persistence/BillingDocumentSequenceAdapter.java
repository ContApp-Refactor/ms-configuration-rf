package co.unicauca.edu.co.contables.billing.infrastructure.adapters.output.persistence;

import co.unicauca.edu.co.contables.billing.application.ports.output.BillingClockPort;
import co.unicauca.edu.co.contables.billing.application.ports.output.BillingTransactionRunnerPort;
import co.unicauca.edu.co.contables.billing.application.ports.output.DocumentNumberSequencePort;
import co.unicauca.edu.co.contables.billing.domain.exception.BillingErrorCode;
import co.unicauca.edu.co.contables.billing.domain.exception.BillingException;
import co.unicauca.edu.co.contables.billing.domain.model.DocumentType;
import co.unicauca.edu.co.contables.billing.infrastructure.adapters.output.persistence.entity.BillingDocumentSequenceEntity;
import co.unicauca.edu.co.contables.billing.infrastructure.adapters.output.persistence.repository.BillingDocumentSequenceJpaRepository;
import jakarta.persistence.LockTimeoutException;
import jakarta.persistence.PessimisticLockException;
import java.util.Optional;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.dao.QueryTimeoutException;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.stereotype.Component;

/**
 * Secuencia de numeracion con bloqueo pesimista (EMT-3, D11). Debe ejecutarse dentro de la
 * transaccion de emision del llamador: el {@code FOR UPDATE} se libera con su commit o rollback, por
 * lo que el numero es consecutivo, sin huecos y se reutiliza si la emision se revierte.
 *
 * <p>Si la fila (tenant, empresa, tipo) no existe se crea en una transaccion NUEVA (para que sea
 * visible a la competencia sin bloquear la emision); si dos emisiones la crean a la vez, la unicidad
 * hace fallar a la perdedora, que se captura FUERA de esa transaccion y relee la fila ganadora.
 * Un fallo de bloqueo o de espera se traduce a {@link BillingErrorCode#BILLING_SEQUENCE_BUSY}.
 */
@Component("billingDocumentSequenceAdapter")
public class BillingDocumentSequenceAdapter implements DocumentNumberSequencePort {

    private final BillingDocumentSequenceJpaRepository repository;
    private final BillingTransactionRunnerPort transactions;
    private final BillingClockPort clock;

    public BillingDocumentSequenceAdapter(BillingDocumentSequenceJpaRepository repository,
            BillingTransactionRunnerPort transactions, BillingClockPort clock) {
        this.repository = repository;
        this.transactions = transactions;
        this.clock = clock;
    }

    @Override
    public long nextValue(String enterpriseId, DocumentType type) {
        try {
            BillingDocumentSequenceEntity row = lockRow(enterpriseId, type);
            long next = row.getLastNumber() + 1;
            row.setLastNumber(next);
            row.setUpdatedAt(clock.now());
            repository.saveAndFlush(row);
            return next;
        } catch (RuntimeException ex) {
            if (isLockTimeout(ex)) {
                throw new BillingException(BillingErrorCode.BILLING_SEQUENCE_BUSY,
                        "La secuencia de numeracion esta ocupada, intente de nuevo");
            }
            throw ex;
        }
    }

    private boolean isLockTimeout(RuntimeException ex) {
        if (ex instanceof PessimisticLockingFailureException
                || ex instanceof QueryTimeoutException
                || ex instanceof LockTimeoutException
                || ex instanceof PessimisticLockException
                || ex instanceof jakarta.persistence.QueryTimeoutException) {
            return true;
        }
        if (ex instanceof JpaSystemException) {
            String msg = ex.getMessage() != null ? ex.getMessage().toLowerCase() : "";
            if (msg.contains("lock") || msg.contains("timeout") || msg.contains("rollback")) {
                return true;
            }
        }
        Throwable cause = ex.getCause();
        while (cause != null) {
            if (cause instanceof LockTimeoutException || cause instanceof PessimisticLockException) {
                return true;
            }
            cause = cause.getCause();
        }
        return false;
    }

    private BillingDocumentSequenceEntity lockRow(String enterpriseId, DocumentType type) {
        String typeName = type.name();
        Optional<BillingDocumentSequenceEntity> existing = repository.findForUpdate(enterpriseId, typeName);
        if (existing.isPresent()) {
            return existing.get();
        }
        createRow(enterpriseId, type);
        return repository.findForUpdate(enterpriseId, typeName).orElseThrow(() -> new BillingException(
                BillingErrorCode.BILLING_INTERNAL_ERROR, "No se pudo crear la secuencia de numeracion"));
    }

    /** Alta en transaccion nueva; si otra emision la creo primero, se ignora el choque de unicidad. */
    private void createRow(String enterpriseId, DocumentType type) {
        try {
            transactions.inNewTransaction(() -> {
                BillingDocumentSequenceEntity created = new BillingDocumentSequenceEntity();
                created.setEnterpriseId(enterpriseId);
                created.setDocumentType(type.name());
                created.setPrefix(type.prefix());
                created.setLastNumber(0L);
                created.setUpdatedAt(clock.now());
                repository.saveAndFlush(created);
            });
        } catch (DataIntegrityViolationException raced) {
            // La fila ya existe (creada por otra emision concurrente): se relee con bloqueo a continuacion.
        }
    }
}
