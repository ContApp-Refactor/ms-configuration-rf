package co.unicauca.edu.co.contables.accounting.catalogue.accounting.application.output;

import java.util.List;
import java.util.Optional;

import co.unicauca.edu.co.contables.accounting.catalogue.accounting.domain.models.AccountingEntry;
import co.unicauca.edu.co.contables.accounting.catalogue.accounting.domain.models.AccountingMovement;
import co.unicauca.edu.co.contables.accounting.catalogue.accounting.domain.models.InvoiceReplica;
import co.unicauca.edu.co.contables.accounting.catalogue.accounting.domain.models.ReceiptDetail;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.domain.models.AccountCatalogue;

public interface IAccountingSearchOutputPort {
    Optional<AccountingEntry> findById(Long id);

    Optional<AccountingEntry> findByReceiptId(Long receiptId);

    List<AccountingMovement> findMovementsByAccountId(Long accountId);
    
    List<AccountingMovement> findMovementsByThirdPartyId(Long thirdPartyId);

    /**
     * Busca un asiento contable por el ID y TIPO del documento de origen.
     */
    Optional<AccountingEntry> findBySourceDocumentIdAndType(Long sourceDocumentId, String type);

    /**
     * Verifica si existe un asiento contable asociado a un ID y TIPO de documento de origen.
     */
    boolean existsBySourceDocumentIdAndType(Long sourceDocumentId, String type);

     /**
     * Busca todas las facturas con saldo pendiente para una lista de IDs de clientes.
     */
    List<InvoiceReplica> findPendingInvoicesByClientIds(List<Long> clientIds);

    /**
     * Busca todas las facturas con saldo pendiente para un cliente específico.
     */
    List<InvoiceReplica> findPendingInvoicesByClientId(Long clientId);

    /**
     * Busca todos los detalles de pago (abonos) aplicados a una factura específica.
     */
    List<ReceiptDetail> findReceiptDetailsByInvoiceId(Long invoiceId);

    List<AccountCatalogue> findAllAccountsByEnterprise(String enterpriseId);
    
    List<AccountingMovement> findMovementsByThirdAndAccountCodes(Long thirdId, List<String> accountCodes);

    /**
     * Busca y devuelve la cabecera del asiento contable (AccountingEntry)
     * a la que pertenece un movimiento específico.
     *
     * @param movementId El ID del movimiento contable.
     * @return Un Optional que contiene el AccountingEntry si se encuentra, o vacío si no.
     */
    Optional<AccountingEntry> findEntryByMovementId(Long movementId);
}
