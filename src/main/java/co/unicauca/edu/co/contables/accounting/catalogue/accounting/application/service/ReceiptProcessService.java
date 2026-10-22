package co.unicauca.edu.co.contables.accounting.catalogue.accounting.application.service;

import java.util.UUID;

import org.springframework.stereotype.Service;

import co.unicauca.edu.co.contables.accounting.catalogue.accounting.application.input.IAccountBalanceUpdateInputPort;
import co.unicauca.edu.co.contables.accounting.catalogue.accounting.application.input.IReceiptProcessInputPort;
import co.unicauca.edu.co.contables.accounting.catalogue.accounting.application.output.IAccountingEntryPersistenceOutputPort;
import co.unicauca.edu.co.contables.accounting.catalogue.accounting.application.output.IAccountingSearchOutputPort;
import co.unicauca.edu.co.contables.accounting.catalogue.accounting.application.output.IInvoiceProviderPort;
import co.unicauca.edu.co.contables.accounting.catalogue.accounting.application.output.IReceiptPersistenceOutputPort;
import co.unicauca.edu.co.contables.accounting.catalogue.accounting.domain.enums.ProcessingStatus;
import co.unicauca.edu.co.contables.accounting.catalogue.accounting.domain.enums.SourceDocumentType;
import co.unicauca.edu.co.contables.accounting.catalogue.accounting.domain.exception.AccountingEntryNotFoundException;
import co.unicauca.edu.co.contables.accounting.debt_payments.domain.exception.InvoiceNotFoundException;
import co.unicauca.edu.co.contables.accounting.debt_payments.domain.exception.ReceiptNotFoundException;
import co.unicauca.edu.co.contables.accounting.catalogue.accounting.domain.models.AccountingEntry;
import co.unicauca.edu.co.contables.accounting.catalogue.accounting.domain.models.InvoiceReplica;
import co.unicauca.edu.co.contables.accounting.catalogue.accounting.domain.models.Receipt;
import co.unicauca.edu.co.contables.accounting.catalogue.accounting.domain.models.ReceiptDetail;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.application.output.IAccountCatalogueSearchOutputPort;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.domain.models.AccountCatalogue;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import java.util.function.Function;

@Service
@AllArgsConstructor
@Slf4j
public class ReceiptProcessService implements IReceiptProcessInputPort {
        private final IAccountingSearchOutputPort accountingSearchOutputPort;
        private final IReceiptPersistenceOutputPort receiptPersistenceOutputPort;
        private final IAccountingEntryPersistenceOutputPort accountingEntryPersistenceOutputPort;;
        private final IAccountBalanceUpdateInputPort accountBalanceUpdateInputPort;
        private final IInvoiceProviderPort invoiceProviderPort;
        private final IAccountCatalogueSearchOutputPort accountCatalogueSearchOutputPort;

        @Override
        @Transactional
        public void processReceiptCreation(Receipt receipt) {
                log.info("Iniciando procesamiento de creación para recibo de origen: {}", receipt.getReceiptCode());

                if (receiptPersistenceOutputPort.existsByReceiptCode(receipt.getReceiptCode())) {
                        log.warn("El recibo con código {} ya ha sido procesado. Ignorando mensaje duplicado.",
                                        receipt.getReceiptCode());
                        return;
                }

                receipt.setProcessingStatus(ProcessingStatus.PENDING);
                Receipt savedReceipt = receiptPersistenceOutputPort.save(receipt);
                log.info("Recibo {} guardado localmente con ID: {}", savedReceipt.getReceiptCode(),
                                savedReceipt.getId());

                try {
                        // 1. ORDENAR a las facturas que apliquen su pago (Principio "Tell, Don't Ask")
                        // (Asumiendo que 1L es 'Abono a Factura'. Idealmente, usar un Enum aquí)
                        if (savedReceipt.getReceiptTypeId() == 1L) {
                                log.info("Recibo {} es de tipo Abono. Actualizando saldos de facturas...",
                                                savedReceipt.getReceiptCode());
                                for (ReceiptDetail detail : savedReceipt.getDetails()) {
                                        InvoiceReplica invoice = invoiceProviderPort
                                                        .findInvoiceById(detail.getOriginalInvoiceId())
                                                        .orElseThrow(() -> new InvoiceNotFoundException(
                                                                        "No se encontró la factura con ID " + detail
                                                                                        .getOriginalInvoiceId()));

                                        // La lógica de negocio está ahora en el modelo de dominio InvoiceReplica
                                        invoice.applyPayment(detail.getAmountPaid());

                                        invoiceProviderPort.updateInvoice(invoice);
                                        log.info("Factura {} actualizada. Nuevo saldo pendiente: {}",
                                                        invoice.getFactCode(), invoice.getPendingValue());
                                }
                        }

                        // 2. PEDIR al modelo de dominio 'Receipt' que construya el asiento contable
                        // El servicio solo provee las dependencias externas (la función para buscar
                        // cuentas).
                        Function<String, AccountCatalogue> accountFinder = code -> accountCatalogueSearchOutputPort
                                        .getAccountCatalogueByCode(code, savedReceipt.getEnterpriseId());
                        String entryCode = "AE-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

                        // La lógica compleja de creación está encapsulada en el modelo de dominio.
                        AccountingEntry accountingEntry = savedReceipt.buildAccountingEntryFromReceipt(accountFinder,
                                        entryCode);

                        accountingEntryPersistenceOutputPort.save(accountingEntry);
                        log.info("Asiento contable {} generado para el recibo {}", accountingEntry.getCode(),
                                        savedReceipt.getId());

                        // 3. ORQUESTAR la actualización de saldos
                        accountBalanceUpdateInputPort.updateBalancesFromAccountingEntry(accountingEntry);

                        // 4. ACTUALIZAR el estado final del recibo
                        savedReceipt.setProcessingStatus(ProcessingStatus.PROCESSED);
                        receiptPersistenceOutputPort.save(savedReceipt);

                } catch (Exception e) {
                        log.error("Error crítico al procesar el recibo {}. Se hará rollback.", savedReceipt.getId(), e);
                        throw new IllegalStateException(
                                        "Fallo al procesar el asiento contable para el recibo " + savedReceipt.getId(),
                                        e);
                }
        }

        @Override
        @Transactional
        public void processReceiptVoid(Receipt receiptEventData) {
                log.info("Iniciando procesamiento de ANULACIÓN para recibo con código: {}",
                                receiptEventData.getReceiptCode());

                Receipt localReceipt = receiptPersistenceOutputPort.findByReceiptCode(receiptEventData.getReceiptCode())
                                .orElseThrow(() -> new ReceiptNotFoundException(
                                                "No se puede anular un recibo inexistente: "
                                                                + receiptEventData.getReceiptCode()));

                if (localReceipt.getProcessingStatus() == ProcessingStatus.VOIDED) {
                        log.warn("El recibo {} ya ha sido anulado previamente. Omitiendo mensaje duplicado.",
                                        localReceipt.getReceiptCode());
                        return;
                }

                AccountingEntry originalEntry = accountingSearchOutputPort
                                .findBySourceDocumentIdAndType(localReceipt.getOriginalReceiptId(),
                                                SourceDocumentType.RECEIPT.name()) // Asumiendo un método en
                                                                                   // Receipt que devuelve el
                                                                                   // SourceDocumentType
                                .orElseThrow(() -> new AccountingEntryNotFoundException(
                                                "No se encontró el asiento contable para el recibo "
                                                                + localReceipt.getReceiptCode()));

                // 1. ORDENAR la reversión de saldos en las facturas afectadas
                if (localReceipt.getReceiptTypeId() == 1L) {
                        log.info("Anulando recibo de abono {}. Reversando saldos de facturas...",
                                        localReceipt.getReceiptCode());
                        for (ReceiptDetail detail : localReceipt.getDetails()) {
                                InvoiceReplica invoice = invoiceProviderPort
                                                .findInvoiceById(detail.getOriginalInvoiceId())
                                                .orElseThrow(() -> new InvoiceNotFoundException(
                                                                "Inconsistencia: No se encontró la factura ID "
                                                                                + detail.getOriginalInvoiceId()));

                                // La lógica de negocio está en el modelo de dominio.
                                invoice.reversePayment(detail.getAmountPaid());

                                invoiceProviderPort.updateInvoice(invoice);
                                log.info("Factura {} reversada. Nuevo saldo pendiente: {}", invoice.getFactCode(),
                                                invoice.getPendingValue());
                        }
                }

                // 2. ORQUESTAR la reversión de saldos contables
                accountBalanceUpdateInputPort.reverseBalancesFromAccountingEntry(originalEntry);
                log.info("Los saldos de las cuentas afectadas por el asiento {} han sido revertidos.",
                                originalEntry.getCode());

                // 3. ORDENAR al asiento contable que se anule a sí mismo
                // Se respeta la encapsulación y las reglas de negocio internas de
                // AccountingEntry.
                originalEntry.voidEntry();
                accountingEntryPersistenceOutputPort.save(originalEntry);
                log.info("Asiento contable original {} marcado como VOIDED.", originalEntry.getCode());

                // 4. ACTUALIZAR el estado final del recibo
                localReceipt.setProcessingStatus(ProcessingStatus.VOIDED);
                localReceipt.setStatus("VOIDED");
                receiptPersistenceOutputPort.save(localReceipt);
                log.info("Recibo local {} actualizado a estado VOIDED.", localReceipt.getId());
        }
}
