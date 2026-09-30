package co.unicauca.edu.co.contables.accounting.catalogue.accounting.application.service;


import java.util.List;

import org.springframework.stereotype.Service;

import co.unicauca.edu.co.contables.accounting.catalogue.accounting.application.input.IAccountBalanceUpdateInputPort;
import co.unicauca.edu.co.contables.accounting.catalogue.accounting.application.input.IWriteOffProcessInputPort;
import co.unicauca.edu.co.contables.accounting.catalogue.accounting.application.output.IAccountingEntryPersistenceOutputPort;
import co.unicauca.edu.co.contables.accounting.catalogue.accounting.application.output.IAccountingSearchOutputPort;
import co.unicauca.edu.co.contables.accounting.catalogue.accounting.application.output.IInvoiceProviderPort;
import co.unicauca.edu.co.contables.accounting.catalogue.accounting.domain.enums.AccountingEntryStatus;
import co.unicauca.edu.co.contables.accounting.catalogue.accounting.domain.enums.SourceDocumentType;
import co.unicauca.edu.co.contables.accounting.catalogue.accounting.domain.exception.AccountingEntryNotFoundException;
import co.unicauca.edu.co.contables.accounting.debt_payments.domain.exception.InvoiceNotFoundException;
import co.unicauca.edu.co.contables.accounting.catalogue.accounting.domain.models.AccountingEntry;
import co.unicauca.edu.co.contables.accounting.catalogue.accounting.domain.models.InvoiceReplica;
import co.unicauca.edu.co.contables.accounting.catalogue.accounting.domain.models.PortfolioWriteOff;
import co.unicauca.edu.co.contables.accounting.catalogue.accounting.domain.models.WriteOffDetail;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.application.output.IAccountCatalogueSearchOutputPort;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.domain.models.AccountCatalogue;
import java.util.function.Function;
import java.util.UUID;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@AllArgsConstructor
@Slf4j
public class WriteOffProcessService implements IWriteOffProcessInputPort {
    private final IAccountingSearchOutputPort accountingSearchOutputPort;
    private final IAccountingEntryPersistenceOutputPort accountingEntryPersistencePort;
    private final IAccountBalanceUpdateInputPort accountBalanceUpdatePort;
    private final IAccountCatalogueSearchOutputPort accountCatalogueSearchPort;
    private final IInvoiceProviderPort invoiceProviderPort;

    @Override
    @Transactional
    public void processWriteOffConfirmation(PortfolioWriteOff writeOff) {
        log.info("Iniciando procesamiento de confirmación para castigo de cartera: {}", writeOff.getCode());

        if (accountingSearchOutputPort.existsBySourceDocumentIdAndType(
                writeOff.getOriginalWriteOffId(), SourceDocumentType.PORTFOLIO_WRITEOFF.name())) {
            log.warn("El castigo con ID Origen: {} ya tiene un asiento contable. Ignorando mensaje duplicado.", writeOff.getOriginalWriteOffId());
            return;
        }

        try {
            // 1. ORDENAR a las facturas que se castiguen a sí mismas (Principio "Tell, Don't Ask")
            log.info("Castigo de cartera {} confirmado. Actualizando estado de facturas...", writeOff.getCode());
            for (WriteOffDetail detail : writeOff.getDetails()) {
                InvoiceReplica invoice = invoiceProviderPort.findInvoiceById(detail.getInvoiceId())
                        .orElseThrow(() -> new InvoiceNotFoundException("No se encontró la factura ID " + detail.getInvoiceId()));

                // La lógica de negocio está ahora en el modelo de dominio InvoiceReplica
                invoice.writeOff();

                invoiceProviderPort.updateInvoice(invoice);
                log.info("Factura {} actualizada a estado WRITTEN_OFF.", invoice.getFactCode());
            }

            // 2. PEDIR al modelo de dominio 'PortfolioWriteOff' que construya el asiento contable
            Function<Long, AccountCatalogue> accountFinder = accountCode -> accountCatalogueSearchPort
                    .getAccountCatalogueByCode(accountCode.toString(), writeOff.getEnterpriseId()); // Asumiendo un método por ID
            String entryCode = "AC-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

            // La lógica compleja de creación está encapsulada en el modelo de dominio.
            AccountingEntry accountingEntry = writeOff.generateAccountingEntry(accountFinder, entryCode);

            accountingEntryPersistencePort.save(accountingEntry);
            log.info("Asiento contable {} generado para el castigo {}", accountingEntry.getCode(), writeOff.getCode());
            
            // 3. ORQUESTAR la actualización de saldos
            accountBalanceUpdatePort.updateBalancesFromAccountingEntry(accountingEntry);

        } catch (Exception e) {
            log.error("Error crítico al procesar el asiento para el castigo {}. Se hará rollback.", writeOff.getCode(), e);
            throw new IllegalStateException("Fallo al procesar el asiento para el castigo " + writeOff.getCode(), e);
        }
    }

    @Override
    @Transactional
    public void processWriteOffAnnulment(PortfolioWriteOff writeOffEventData) {
        log.info("Iniciando procesamiento de ANULACIÓN para castigo: {}", writeOffEventData.getCode());

        AccountingEntry originalEntry = accountingSearchOutputPort
                .findBySourceDocumentIdAndType(writeOffEventData.getOriginalWriteOffId(), SourceDocumentType.PORTFOLIO_WRITEOFF.name())
                .orElseThrow(() -> new AccountingEntryNotFoundException("No existe asiento contable para el castigo: " + writeOffEventData.getCode()));

        if (originalEntry.getStatus() == AccountingEntryStatus.VOIDED) {
            log.warn("El asiento contable {} para el castigo {} ya ha sido anulado. Omitiendo mensaje duplicado.", originalEntry.getCode(), writeOffEventData.getCode());
            return;
        }

        // 1. ORDENAR a las facturas que reviertan su estado de castigo
        log.info("Anulando castigo {}. Reversando estado de facturas...", writeOffEventData.getCode());
        for(WriteOffDetail detail : writeOffEventData.getDetails()) {
            InvoiceReplica invoice = invoiceProviderPort.findInvoiceById(detail.getInvoiceId())
                    .orElseThrow(() -> new InvoiceNotFoundException("Inconsistencia: No se encontró la factura con ID " + detail.getInvoiceId()));

            // La lógica para revertir el castigo debe estar en el modelo de dominio.
            invoice.reverseWriteOff(detail.getAmountWrittenOff());
            
            invoiceProviderPort.updateInvoice(invoice);
            log.info("Factura {} reversada a estado PENDING. Saldo restaurado.", invoice.getFactCode());
        }

        // 2. ORQUESTAR la reversión de saldos contables
        accountBalanceUpdatePort.reverseBalancesFromAccountingEntry(originalEntry);
        log.info("Saldos del asiento {} revertidos.", originalEntry.getCode());

        // 3. ORDENAR al asiento que se anule a sí mismo
        originalEntry.voidEntry();
        accountingEntryPersistencePort.save(originalEntry);
        log.info("Asiento contable {} del castigo {} marcado como anulado.", originalEntry.getCode(), writeOffEventData.getCode());
    }

    @Override
    public void writeOffInvoices(List<Long> invoiceIds) {
        // La implementación de este método también debería seguir el patrón:
        // 1. Iterar sobre los IDs.
        // 2. Para cada ID, buscar la InvoiceReplica.
        // 3. Llamar a invoice.writeOff().
        // 4. Guardar la factura actualizada.
        // 5. (Opcional) Crear un objeto PortfolioWriteOff y llamar a processWriteOffConfirmation.
        throw new UnsupportedOperationException("Unimplemented method 'writeOffInvoices'");
    }
}