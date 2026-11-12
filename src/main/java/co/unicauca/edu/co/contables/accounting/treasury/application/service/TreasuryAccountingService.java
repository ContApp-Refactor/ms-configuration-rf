package co.unicauca.edu.co.contables.accounting.treasury.application.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.unicauca.edu.co.contables.accounting.catalogue.accounting.application.input.IAccountBalanceUpdateInputPort;
import co.unicauca.edu.co.contables.accounting.catalogue.accounting.application.output.IAccountingEntryPersistenceOutputPort;
import co.unicauca.edu.co.contables.accounting.catalogue.accounting.application.output.IAccountingSearchOutputPort;
import co.unicauca.edu.co.contables.accounting.catalogue.accounting.domain.enums.AccountingEntryStatus;
import co.unicauca.edu.co.contables.accounting.catalogue.accounting.domain.models.AccountingEntry;
import co.unicauca.edu.co.contables.accounting.catalogue.accounting.domain.models.AccountingMovement;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.application.output.IAccountCatalogueSearchOutputPort;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.domain.models.AccountCatalogue;
import co.unicauca.edu.co.contables.accounting.treasury.application.input.ITreasuryAccountingInputPort;
import co.unicauca.edu.co.contables.accounting.treasury.application.output.IPaymentMethodProviderPort;
import co.unicauca.edu.co.contables.accounting.treasury.application.output.ITreasuryAccountingResultPublisher;
import co.unicauca.edu.co.contables.accounting.treasury.application.output.ITreasuryAccountingResultPublisher.AccountingResultReply;
import co.unicauca.edu.co.contables.accounting.treasury.domain.model.PayableWriteOff;
import co.unicauca.edu.co.contables.accounting.treasury.domain.model.PayableWriteOffDetail;
import co.unicauca.edu.co.contables.accounting.treasury.domain.model.PaymentMethodData;
import co.unicauca.edu.co.contables.accounting.treasury.domain.model.PaymentVoucher;
import co.unicauca.edu.co.contables.accounting.treasury.domain.model.PaymentVoucherDetail;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Consumidor contable de los eventos de tesoreria.
 *
 * <p>Tesoreria mueve los comprobantes a {@code POSTING} y las bajas a
 * {@code VOIDING}, publica el evento y espera la respuesta por
 * {@code accounting.result.exchange}. Este servicio construye el asiento de
 * partida doble, lo guarda, actualiza los saldos de las cuentas y responde.</p>
 *
 * <p>Regla de la partida doble:</p>
 * <ul>
 *   <li><b>Comprobante de pago</b>: se <em>debitan</em> las cuentas por pagar de cada
 *       detalle (el pasivo se extingue) y se <em>acredita</em> la cuenta contable del
 *       metodo de pago por el total (por ahi salio el dinero).</li>
 *   <li><b>Baja de obligaciones</b>: se <em>debitan</em> las cuentas por pagar de cada
 *       detalle (la obligacion deja de existir) y se <em>acredita</em> la cuenta
 *       contrapartida por el total.</li>
 * </ul>
 *
 * <p>Si la partida no cierra, o si falta algun dato contable, el evento se
 * <b>rechaza</b> con {@code accepted=false} y un motivo. Rechazar es una respuesta
 * valida: tesoreria pasa el documento a {@code FAILED} (o a {@code VOID_FAILED}) y
 * libera las reservas de las facturas.</p>
 */
@Service
@AllArgsConstructor
@Slf4j
public class TreasuryAccountingService implements ITreasuryAccountingInputPort {

    /** Tipos de documento que usa tesoreria en {@code sourceDocument.type}. */
    public static final String DOCUMENT_TYPE_PAYMENT_VOUCHER = "PAYMENT_VOUCHER";
    public static final String DOCUMENT_TYPE_PAYABLE_WRITEOFF = "PAYABLE_WRITEOFF";

    /** Operaciones del resultado contable; {@code TreasuryCommands.AccountingResult} las lee. */
    public static final String OPERATION_CREATE = "CREATE";
    public static final String OPERATION_VOID = "VOID";

    private final IAccountingSearchOutputPort accountingSearchOutputPort;
    private final IAccountingEntryPersistenceOutputPort accountingEntryPersistencePort;
    private final IAccountBalanceUpdateInputPort accountBalanceUpdatePort;
    private final IAccountCatalogueSearchOutputPort accountCatalogueSearchPort;
    private final IPaymentMethodProviderPort paymentMethodProviderPort;
    private final ITreasuryAccountingResultPublisher resultPublisher;

    @Override
    @Transactional
    public void postPaymentVoucher(PaymentVoucher voucher, AccountingEventContext context) {
        String type = DOCUMENT_TYPE_PAYMENT_VOUCHER;
        Long documentId = voucher == null ? null : voucher.getId();
        if (documentId == null) {
            reject(context, OPERATION_CREATE, type, null, "El comprobante no trae identificador");
            return;
        }
        if (alreadyAccounted(documentId, type)) {
            log.warn("El comprobante {} ya tiene asiento contable; se reenvia el resultado", documentId);
            accountingSearchOutputPort.findBySourceDocumentIdAndType(documentId, type)
                    .ifPresent(entry -> accept(context, OPERATION_CREATE, type, documentId, entry.getId()));
            return;
        }

        String enterpriseId = voucher.getEnterpriseId();
        PaymentMethodData method = paymentMethodProviderPort
                .findActive(voucher.getPaymentMethodId(), enterpriseId)
                .orElse(null);
        if (method == null) {
            reject(context, OPERATION_CREATE, type, documentId,
                    "Metodo de pago inexistente o inactivo");
            return;
        }
        AccountCatalogue paymentAccount = account(method.accountingAccountId(), enterpriseId);
        if (paymentAccount == null) {
            reject(context, OPERATION_CREATE, type, documentId,
                    "El metodo de pago no tiene una cuenta contable valida");
            return;
        }

        // La cuenta por pagar es un pasivo: al extinguirse se DEBE. La plata que sale
        // de la cuenta del metodo de pago es un activo que disminuye: se ACREDITA.
        Long thirdPartyId = primarySupplierId(voucher.getDetails(), PaymentVoucherDetail::getSupplierId);
        if (thirdPartyId == null) {
            reject(context, OPERATION_CREATE, type, documentId,
                    "El comprobante no tiene ninguna factura con tercero asociado");
            return;
        }
        List<AccountingMovement> movements = new ArrayList<>();
        for (PaymentVoucherDetail detail : voucher.getDetails()) {
            AccountCatalogue payableAccount = account(detail.getPayableAccountId(), enterpriseId);
            if (payableAccount == null) {
                reject(context, OPERATION_CREATE, type, documentId,
                        "La factura " + detail.getInvoiceId() + " no tiene cuenta por pagar valida");
                return;
            }
            movements.add(AccountingMovement.builder()
                    .account(payableAccount.getId())
                    .thirdPartyId(detail.getSupplierId())
                    .description("Pago factura " + detail.getInvoiceReference())
                    .debit(detail.getAmountPaid())
                    .credit(BigDecimal.ZERO)
                    .build());
        }
        movements.add(AccountingMovement.builder()
                .account(paymentAccount.getId())
                .thirdPartyId(thirdPartyId)
                .description("Pago comprobante " + voucher.getVoucherNumber())
                .debit(BigDecimal.ZERO)
                .credit(voucher.getTotal() == null ? BigDecimal.ZERO : voucher.getTotal())
                .build());

        BigDecimal totalDebit = movements.stream()
                .map(AccountingMovement::getDebit)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (voucher.getTotal() == null || voucher.getTotal().signum() <= 0) {
            reject(context, OPERATION_CREATE, type, documentId, "El total del comprobante debe ser mayor que cero");
            return;
        }
        if (voucher.getTotal().compareTo(totalDebit) != 0) {
            reject(context, OPERATION_CREATE, type, documentId,
                    "La partida no cierra: el total (" + voucher.getTotal()
                            + ") no coincide con la suma de los detalles (" + totalDebit + ")");
            return;
        }

        AccountingEntry entry = buildEntry(voucher.getTotal(), voucher.getIssueDate(),
                "Contabilizacion de comprobante de pago " + voucher.getVoucherNumber(),
                documentId, type, enterpriseId, movements);
        // save() devuelve el dominio con el id asignado: si se descarta, la respuesta
        // sale con accountingEntryId null y el comprobante queda POSTED sin asiento.
        entry = accountingEntryPersistencePort.save(entry);
        accountBalanceUpdatePort.updateBalancesFromAccountingEntry(entry);
        log.info("Asiento contable {} generado para el comprobante {}", entry.getCode(), documentId);
        accept(context, OPERATION_CREATE, type, documentId, entry.getId());
    }

    @Override
    @Transactional
    public void voidPaymentVoucher(Long voucherId, AccountingEventContext context) {
        voidDocument(voucherId, DOCUMENT_TYPE_PAYMENT_VOUCHER, context);
    }

    @Override
    @Transactional
    public void postPayableWriteOff(PayableWriteOff writeOff, AccountingEventContext context) {
        String type = DOCUMENT_TYPE_PAYABLE_WRITEOFF;
        Long documentId = writeOff == null ? null : writeOff.getId();
        if (documentId == null) {
            reject(context, OPERATION_CREATE, type, null, "La baja no trae identificador");
            return;
        }
        if (alreadyAccounted(documentId, type)) {
            log.warn("La baja {} ya tiene asiento contable; se reenvia el resultado", documentId);
            accountingSearchOutputPort.findBySourceDocumentIdAndType(documentId, type)
                    .ifPresent(entry -> accept(context, OPERATION_CREATE, type, documentId, entry.getId()));
            return;
        }

        String enterpriseId = writeOff.getEnterpriseId();
        AccountCatalogue creditAccount = account(writeOff.getCounterpartAccountId(), enterpriseId);
        if (creditAccount == null) {
            reject(context, OPERATION_CREATE, type, documentId,
                    "La baja no tiene una cuenta contrapartida valida");
            return;
        }

        Long thirdPartyId = primarySupplierId(writeOff.getDetails(), PayableWriteOffDetail::getSupplierId);
        if (thirdPartyId == null) {
            reject(context, OPERATION_CREATE, type, documentId,
                    "La baja no tiene ninguna factura con tercero asociado");
            return;
        }

        List<AccountingMovement> movements = new ArrayList<>();
        BigDecimal totalDebit = BigDecimal.ZERO;
        for (PayableWriteOffDetail detail : writeOff.getDetails()) {
            AccountCatalogue debitAccount = account(detail.getPayableAccountId(), enterpriseId);
            if (debitAccount == null) {
                reject(context, OPERATION_CREATE, type, documentId,
                        "La factura " + detail.getInvoiceId() + " no tiene cuenta por pagar valida");
                return;
            }
            movements.add(AccountingMovement.builder()
                    .account(debitAccount.getId())
                    .thirdPartyId(detail.getSupplierId())
                    .description("Baja factura " + detail.getInvoiceId())
                    .debit(detail.getAmount())
                    .credit(BigDecimal.ZERO)
                    .build());
            totalDebit = totalDebit.add(detail.getAmount() == null ? BigDecimal.ZERO : detail.getAmount());
        }

        if (writeOff.getTotal() == null || writeOff.getTotal().signum() <= 0) {
            reject(context, OPERATION_CREATE, type, documentId, "El total de la baja debe ser mayor que cero");
            return;
        }
        if (writeOff.getTotal().compareTo(totalDebit) != 0) {
            reject(context, OPERATION_CREATE, type, documentId,
                    "La partida no cierra: el total (" + writeOff.getTotal()
                            + ") no coincide con la suma de los detalles (" + totalDebit + ")");
            return;
        }
        movements.add(AccountingMovement.builder()
                .account(creditAccount.getId())
                .thirdPartyId(thirdPartyId)
                .description("Baja de obligaciones " + documentId)
                .debit(BigDecimal.ZERO)
                .credit(writeOff.getTotal())
                .build());

        AccountingEntry entry = buildEntry(writeOff.getTotal(), LocalDate.now(),
                "Contabilizacion de baja de obligaciones " + documentId,
                documentId, type, enterpriseId, movements);
        entry = accountingEntryPersistencePort.save(entry);
        accountBalanceUpdatePort.updateBalancesFromAccountingEntry(entry);
        log.info("Asiento contable {} generado para la baja {}", entry.getCode(), documentId);
        accept(context, OPERATION_CREATE, type, documentId, entry.getId());
    }

    @Override
    @Transactional
    public void voidPayableWriteOff(Long writeOffId, AccountingEventContext context) {
        voidDocument(writeOffId, DOCUMENT_TYPE_PAYABLE_WRITEOFF, context);
    }

    /** Anula el asiento de un documento y responde. Compartido por comprobante y baja. */
    private void voidDocument(Long documentId, String type, AccountingEventContext context) {
        if (documentId == null) {
            reject(context, OPERATION_VOID, type, null, "El documento no trae identificador");
            return;
        }
        AccountingEntry entry = accountingSearchOutputPort
                .findBySourceDocumentIdAndType(documentId, type)
                .orElse(null);
        if (entry == null) {
            reject(context, OPERATION_VOID, type, documentId,
                    "No existe asiento contable para anular");
            return;
        }
        if (entry.getStatus() == AccountingEntryStatus.VOIDED) {
            log.warn("El asiento {} ya estaba anulado; se reenvia el resultado", entry.getCode());
            accept(context, OPERATION_VOID, type, documentId, entry.getId());
            return;
        }
        accountBalanceUpdatePort.reverseBalancesFromAccountingEntry(entry);
        entry.voidEntry();
        accountingEntryPersistencePort.save(entry);
        log.info("Asiento contable {} anulado para el documento {} {}", entry.getCode(), type, documentId);
        accept(context, OPERATION_VOID, type, documentId, entry.getId());
    }

    private AccountingEntry buildEntry(BigDecimal total, LocalDate date, String description,
            Long documentId, String type, String enterpriseId, List<AccountingMovement> movements) {
        return AccountingEntry.builder()
                .code("AC-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .date(date == null ? LocalDate.now() : date)
                .description(description)
                .status(AccountingEntryStatus.ACTIVE)
                .sourceDocumentId(documentId)
                .type(type)
                .idEnterprise(enterpriseId)
                .movements(movements)
                .build();
    }

    /**
     * Resuelve una cuenta contable por id. Devuelve {@code null} cuando no existe
     * para la empresa, de modo que el evento se rechace en vez de romper.
     */
    private AccountCatalogue account(Long accountId, String enterpriseId) {
        if (accountId == null || enterpriseId == null) return null;
        try {
            return accountCatalogueSearchPort.getAccountCatalogueById(accountId, enterpriseId);
        } catch (Exception ex) {
            log.debug("No se pudo resolver la cuenta contable {} para la empresa {}",
                    accountId, enterpriseId, ex);
            return null;
        }
    }

    /**
     * Devuelve el tercero que identifica al documento. Ningun comprobante ni baja
     * trae el tercero a nivel de documento, pero {@code accounting_movements.third_party_id}
     * es NOT NULL: igual que hacen {@code Receipt} y {@code PortfolioWriteOff}, la patada
     * acreedora repite el tercero de la primera factura. Devuelve {@code null} si ninguna
     * factura trae tercero, para que el evento se rechace en vez de fallar el insert.
     */
    private <T> Long primarySupplierId(List<T> details, Function<T, Long> supplierId) {
        if (details == null) return null;
        return details.stream()
                .map(supplierId)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
    }

    private boolean alreadyAccounted(Long documentId, String type) {
        return accountingSearchOutputPort.existsBySourceDocumentIdAndType(documentId, type);
    }

    private void accept(AccountingEventContext context, String operation, String type,
            Long documentId, Long accountingEntryId) {
        resultPublisher.publish(new AccountingResultReply(
                context.eventId(), context.eventType(), context.tenantId(), context.enterpriseId(),
                context.correlationId(), operation, type, documentId, true, accountingEntryId, null));
    }

    private void reject(AccountingEventContext context, String operation, String type,
            Long documentId, String reason) {
        log.warn("Evento contable {} {} de tesoreria rechazado: {}", type, operation, reason);
        resultPublisher.publish(new AccountingResultReply(
                context.eventId(), context.eventType(), context.tenantId(), context.enterpriseId(),
                context.correlationId(), operation, type, documentId, false, null, reason));
    }
}
