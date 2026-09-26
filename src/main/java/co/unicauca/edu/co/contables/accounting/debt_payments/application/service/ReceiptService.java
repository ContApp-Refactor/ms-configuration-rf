package co.unicauca.edu.co.contables.accounting.debt_payments.application.service;

import co.unicauca.edu.co.contables.accounting.debt_payments.application.input.IAccountingEventPublisher;
import co.unicauca.edu.co.contables.accounting.debt_payments.application.input.IReceiptCommandUseCase;
import co.unicauca.edu.co.contables.accounting.debt_payments.application.input.IReceiptQueryUseCase;
import co.unicauca.edu.co.contables.accounting.debt_payments.application.output.IInvoiceProviderPort;
import co.unicauca.edu.co.contables.accounting.debt_payments.application.output.IReceiptCommandPersistencePort;
import co.unicauca.edu.co.contables.accounting.debt_payments.application.output.IReceiptQueryPersistencePort;
import co.unicauca.edu.co.contables.accounting.debt_payments.application.output.IResourceUsageNotifierPort;
import co.unicauca.edu.co.contables.accounting.debt_payments.domain.exception.ReceiptNotFoundException;
import co.unicauca.edu.co.contables.accounting.debt_payments.domain.model.Receipt;
import co.unicauca.edu.co.contables.accounting.debt_payments.domain.model.ReceiptStatus;
import co.unicauca.edu.co.contables.accounting.debt_payments.domain.model.Replica.InvoiceReplica;
import co.unicauca.edu.co.contables.commons.audit.annotation.DocumentAuditable;
import co.unicauca.edu.co.contables.commons.audit.annotation.DocumentOperationType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * @brief Service class for managing receipts.
 *        This class implements both command and query use cases for receipts,
 *        handling operations such as creating, voiding, and retrieving
 *        receipts.
 */

@Service
@RequiredArgsConstructor
public class ReceiptService implements IReceiptCommandUseCase, IReceiptQueryUseCase {

    private final IReceiptCommandPersistencePort receiptCommandPersistencePort;
    private final IReceiptQueryPersistencePort receiptQueryPersistencePort;
    private final IInvoiceProviderPort invoiceProviderPort;
    private final IAccountingEventPublisher accountingEventPublisher;
    private final IResourceUsageNotifierPort resourceUsageNotifier;

    /**
     * Creates a new receipt after validating external dependencies and generating a
     * unique receipt code.
     * 
     * @param receipt The receipt to be created.
     * @return The created receipt with updated fields.
     */
    @Override
    @Transactional
    @DocumentAuditable(operationType = DocumentOperationType.CREATE, moduleName = "WALLET")
    public Receipt createReceipt(Receipt receipt) {

        // Generar un código único para el recibo
        String uniqueCode = generateUniqueReceiptCode();
        receipt.setReceiptCode(uniqueCode);

        // Si es pago a facturas, delegar la lógica al objeto de dominio para aplicar
        // pagos
        try {
            if (receipt.isInvoicePayment()) {
                List<InvoiceReplica> modifiedInvoices = receipt
                        .processInvoicePayments(invoiceProviderPort::findInvoiceById);
                // Persistir las facturas modificadas
                for (InvoiceReplica inv : modifiedInvoices) {
                    invoiceProviderPort.updateInvoice(inv);
                }
            }
        } catch (Exception e) {
            throw new IllegalStateException("Error processing invoice payments: " + e.getMessage(), e);
        }

        if (receipt.getLedgerAccountId() == null && !receipt.isInvoicePayment())
            throw new IllegalArgumentException("Ledger account ID is null.");

        receipt.setStatus(ReceiptStatus.FINALIZED);
        receipt.setIssueDate(LocalDate.now());
        Receipt savedReceipt = receiptCommandPersistencePort.save(receipt);

        // Lineas para publicar el evento de creación
        accountingEventPublisher.publishReceiptCreatedEvent(savedReceipt);
        resourceUsageNotifier.notifyAll(savedReceipt.getUsageNotifications());

        return savedReceipt;
    }

    @Override
    @Transactional
    @DocumentAuditable(operationType = DocumentOperationType.VOID, moduleName = "WALLET")
    public Receipt voidReceipt(Long receiptId, String reasonDescription) {
        Receipt receiptToVoid = receiptQueryPersistencePort.findById(receiptId)
                .orElseThrow(() -> new ReceiptNotFoundException("Receipt with id " + receiptId + " does not exist."));

        if (receiptToVoid.getStatus() == ReceiptStatus.VOIDED) {
            throw new IllegalStateException("Receipt with id " + receiptId + " is already voided.");
        }

        // Delegar la anulación al objeto de dominio que devuelve las facturas
        // modificadas
        try {
            List<InvoiceReplica> modifiedInvoices = receiptToVoid.voidReceipt(reasonDescription,
                    invoiceProviderPort::findInvoiceById);
            // Persistir las facturas modificadas
            for (InvoiceReplica inv : modifiedInvoices) {
                invoiceProviderPort.updateInvoice(inv);
            }
        } catch (Exception e) {
            throw new IllegalStateException("Error reversing invoice payments when voiding receipt: " + e.getMessage(),
                    e);
        }

        // Persistir el recibo anulado
        Receipt voidedReceipt = receiptCommandPersistencePort.save(receiptToVoid);

        // Lineas para publicar el evento de anulación
        accountingEventPublisher.publishVoidReceiptEvent(voidedReceipt);

        return voidedReceipt;
    }

    @Override
    @Transactional(readOnly = true)
    public Receipt findById(Long id) {
        return receiptQueryPersistencePort.findById(id)
                .orElseThrow(() -> new ReceiptNotFoundException("El recibo con ID " + id + " no fue encontrado."));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Receipt> findByInvoiceId(String invoiceId) {
        return receiptQueryPersistencePort.findByInvoiceId(invoiceId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Receipt> findByThirdPartyId(String thirdPartyId, String enterpriseId) {
        return receiptQueryPersistencePort.findByThirdPartyId(thirdPartyId, enterpriseId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Receipt> findByEnterpriseId(String enterpriseId) {
        return receiptQueryPersistencePort.findByEnterpriseId(enterpriseId);
    }

    private String generateUniqueReceiptCode() {
        // Esto consultará un secuenciador de la base de datos.
        // Por ahora, un timestamp es suficiente para la demostración.
        return "RC-" + System.currentTimeMillis();
    }
}
