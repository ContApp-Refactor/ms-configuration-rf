package co.unicauca.edu.co.contables.accounting.catalogue.accounting.application.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;



import org.springframework.stereotype.Service;

import co.unicauca.edu.co.contables.accounting.catalogue.accounting.application.input.IInvoiceProcessInputPort;
import co.unicauca.edu.co.contables.accounting.catalogue.accounting.application.output.IInvoiceProviderPort;
import co.unicauca.edu.co.contables.accounting.catalogue.accounting.domain.enums.InvoiceStatus;
import co.unicauca.edu.co.contables.accounting.catalogue.accounting.domain.models.InvoiceReplica;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.application.output.IAccountCatalogueSearchOutputPort;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.domain.models.AccountCatalogue;
import co.unicauca.edu.co.contables.accounting.catalogue.accounting.domain.exception.AccountingEntryNotFoundException;
import co.unicauca.edu.co.contables.accounting.debt_payments.domain.exception.InvoiceNotFoundException;

import jakarta.transaction.Transactional;
import jakarta.validation.ValidationException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@AllArgsConstructor
@Slf4j
public class InvoiceProcessService implements IInvoiceProcessInputPort {

    // Inyectamos el servicio que ya sabe cómo actualizar saldos jerárquicamente.
    private final AccountBalanceUpdateService accountBalanceUpdateService;
    private final IAccountCatalogueSearchOutputPort accountCatalogueSearchOutputPort;
    private final IInvoiceProviderPort invoiceProviderPort;

    @Override
    @Transactional
    public void processInvoiceCreation(InvoiceReplica invoice) {
        log.info("Iniciando procesamiento de la nueva factura {}", invoice.getFactCode());

        // 1. Persistir la réplica de la factura. Esta lógica se movió desde el
        // listener.
        invoiceProviderPort.saveOrUpdate(invoice); // o save, dependiendo de la implementación
        log.info("Réplica de la factura {} guardada/actualizada.", invoice.getFactCode());

        // --- Lógica de actualización de saldos (ahora dentro de la misma transacción)
        // ---
        if (invoice.getAccountingAccount() == null || invoice.getPendingValue() == null) {
            log.error("La factura {} no tiene cuenta o valor pendiente.", invoice.getFactCode());
            throw new ValidationException(
                    "La factura " + invoice.getFactCode() + " no tiene cuenta contable o valor pendiente.");
        }

        // Buscar cuenta contable por codigo
        AccountCatalogue accountCatalogue = accountCatalogueSearchOutputPort
                .getAccountCatalogueByCode(invoice.getAccountingAccount().toString(), invoice.getEntId());
        if (accountCatalogue == null) {
            throw new AccountingEntryNotFoundException("No se encontró la cuenta contable: " + invoice.getAccountingAccount() + " para la empresa: " + invoice.getEntId());
        }

        Long accountId = accountCatalogue.getId();
        String enterpriseId = invoice.getEntId();

        // Convertimos el valor pendiente (Long) a BigDecimal.
        BigDecimal amount = invoice.getPendingValue();

        // Lógica Contable: Una nueva factura de venta (cuenta por cobrar) es un activo.
        // El aumento de un activo se registra como un DÉBITO.
        // Por lo tanto, pasamos el monto pendiente en el parámetro de débito.
        BigDecimal debitAmount = amount;
        BigDecimal creditAmount = BigDecimal.ZERO;

        accountBalanceUpdateService.updateSingleAccountHierarchy(accountId, debitAmount, creditAmount, invoice.getEntId());
        log.info("Saldos actualizados correctamente para la factura {}", invoice.getFactCode());
    }



    @Override
    public void updateDueDate(Long invoiceId, LocalDate newDueDate) {
        InvoiceReplica invoice = invoiceProviderPort.findInvoiceById(invoiceId)
                .orElseThrow(() -> new InvoiceNotFoundException("No se encontró la factura con ID: " + invoiceId));

        if (invoice.getStatus() == InvoiceStatus.PAID)
            throw new IllegalStateException("No se puede cambiar la fecha de vencimiento de una factura ya pagada.");

        invoice.setExpirationDate(newDueDate);

        invoice.validateDates();

        invoiceProviderPort.updateInvoice(invoice);
    }

    @Override
    public List<InvoiceReplica> findPendingInvoicesByClientId(Long clientId) {
        return invoiceProviderPort.findPendingInvoicesByClientId(clientId);
    }

    @Override
    public List<InvoiceReplica> findInvoicesByEnterpriseId(String enterpriseId) {
        return invoiceProviderPort.findInvoicesByEnterpriseId(enterpriseId);
    }

    @Override
    public InvoiceReplica findInvoiceById(Long invoiceId) {
        return invoiceProviderPort.findInvoiceById(invoiceId)
                .orElseThrow(() -> new InvoiceNotFoundException("No se encontró la factura con ID: " + invoiceId));
    }
}
