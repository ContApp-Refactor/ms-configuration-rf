package co.unicauca.edu.co.contables.accounting.debt_payments.application.service;

import co.unicauca.edu.co.contables.accounting.debt_payments.application.input.IAccountingEventPublisher;
import co.unicauca.edu.co.contables.accounting.debt_payments.application.input.IPortfolioWriteOffCommandUseCase;
import co.unicauca.edu.co.contables.accounting.debt_payments.application.input.IPortfolioWriteOffQueryUseCase;
import co.unicauca.edu.co.contables.accounting.debt_payments.application.output.IInvoiceProviderPort;
import co.unicauca.edu.co.contables.accounting.debt_payments.application.output.IPortfolioWriteOffPersistencePort;
import co.unicauca.edu.co.contables.accounting.debt_payments.application.output.IResourceUsageNotifierPort;
import co.unicauca.edu.co.contables.accounting.debt_payments.domain.exception.PortfolioWriteOffNotFoundException;
import co.unicauca.edu.co.contables.accounting.debt_payments.domain.model.PortfolioWriteOff;
import co.unicauca.edu.co.contables.accounting.debt_payments.domain.model.Replica.InvoiceReplica;
import co.unicauca.edu.co.contables.commons.audit.annotation.DocumentAuditable;
import co.unicauca.edu.co.contables.commons.audit.annotation.DocumentOperationType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @brief Service class for managing portfolio write-offs.
 *        This class implements both command and query use cases for portfolio
 *        write-offs,
 *        handling creation, confirmation, voidance, and retrieval operations.
 *        It orchestrates interactions between the domain model and various
 *        persistence
 *        and notification ports.
 */

@Service
@RequiredArgsConstructor
public class PortfolioWriteOffService implements IPortfolioWriteOffCommandUseCase, IPortfolioWriteOffQueryUseCase {

    private final IPortfolioWriteOffPersistencePort writeOffPersistencePort;
    private final IInvoiceProviderPort invoiceProviderPort;
    private final IAccountingEventPublisher accountingEventPublisher;
    private final IResourceUsageNotifierPort resourceUsageNotifier;

    @Override
    @Transactional
    @DocumentAuditable(operationType = DocumentOperationType.CREATE, moduleName = "WALLET")
    public PortfolioWriteOff createWriteOff(PortfolioWriteOff writeOff) {
        // 1. El objeto de dominio ya viene creado y validado desde la capa de
        // REST/Mapper.
        // Le pedimos al dominio que prepare las facturas.
        List<InvoiceReplica> modifiedInvoices = writeOff
                .prepareInvoicesForWriteOff(invoiceProviderPort::findInvoiceById);

        writeOff.setCode(generateUniqueWriteOffCode());

        // 3. Orquestar la persistencia
        for (InvoiceReplica invoice : modifiedInvoices) {
            invoiceProviderPort.updateInvoice(invoice);
        }

        // 4. Publicar tercero y centro de costo usados
        resourceUsageNotifier.notifyAll(writeOff.getUsageNotifications());

        return writeOffPersistencePort.save(writeOff);
    }

    @Override
    @DocumentAuditable(operationType = DocumentOperationType.APPROVE, moduleName = "WALLET")
    public PortfolioWriteOff confirmWriteOff(Long writeOffId) {
        // 1. Cargar el Agregado Raíz
        PortfolioWriteOff writeOff = findWriteOffOrThrow(writeOffId);

        // 2. Ejecutar la lógica de negocio en el dominio
        writeOff.confirm(); // Cambia el estado interno
        List<InvoiceReplica> modifiedInvoices = writeOff.processConfirmation(invoiceProviderPort::findInvoiceById);

        // 3. Orquestar la persistencia
        for (InvoiceReplica invoice : modifiedInvoices) {
            invoiceProviderPort.updateInvoice(invoice);
        }
        PortfolioWriteOff updatedWriteOff = writeOffPersistencePort.update(writeOff);

        // 4. Publicar evento de dominio
        accountingEventPublisher.publishWriteOffConfirmedEvent(updatedWriteOff);

        return updatedWriteOff;
    }

    @Override
    @DocumentAuditable(operationType = DocumentOperationType.VOID, moduleName = "WALLET")
    public PortfolioWriteOff voidWriteOffConfirmation(Long writeOffId) {
        // 1. Cargar el Agregado Raíz
        PortfolioWriteOff writeOff = findWriteOffOrThrow(writeOffId);

        // 2. Ejecutar la lógica de negocio en el dominio
        writeOff.voidConfirmation();
        List<InvoiceReplica> modifiedInvoices = writeOff.processVoidance(invoiceProviderPort::findInvoiceById);

        // 3. Orquestar la persistencia
        for (InvoiceReplica invoice : modifiedInvoices) {
            invoiceProviderPort.updateInvoice(invoice);
        }
        PortfolioWriteOff updatedWriteOff = writeOffPersistencePort.update(writeOff);

        // 4. Publicar evento de dominio
        accountingEventPublisher.publishWriteOffVoidedEvent(updatedWriteOff);

        return updatedWriteOff;
    }

    @Override
    @Transactional(readOnly = true)
    public PortfolioWriteOff findById(Long writeOffId) {
        return findWriteOffOrThrow(writeOffId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PortfolioWriteOff> findByEnterpriseId(String enterpriseId) {
        return writeOffPersistencePort.findByEnterpriseId(enterpriseId);
    }

    /**
     * Busca un castigo por su ID o lanza una excepción si no se encuentra.
     * 
     * @param writeOffId ID del castigo a buscar
     * @return El castigo encontrado
     * @throws RuntimeException si no se encuentra el castigo
     */
    private PortfolioWriteOff findWriteOffOrThrow(Long writeOffId) {
        return writeOffPersistencePort.findById(writeOffId)
                .orElseThrow(() -> new PortfolioWriteOffNotFoundException(
                        "Castigo de cartera con ID: " + writeOffId + " no fue encontrado."));
    }

    private String generateUniqueWriteOffCode() {
        return "CC-" + System.currentTimeMillis();
    }

    @Override
    public List<PortfolioWriteOff> findConfirmedOrVoidedByEnterpriseId(String enterpriseId) {
        return writeOffPersistencePort.findConfirmedOrVoidedByEnterpriseId(enterpriseId);
    }
}
