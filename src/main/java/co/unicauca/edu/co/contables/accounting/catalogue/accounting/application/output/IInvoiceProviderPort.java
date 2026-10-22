package co.unicauca.edu.co.contables.accounting.catalogue.accounting.application.output;


import java.util.List;
import java.util.Optional;

import co.unicauca.edu.co.contables.accounting.catalogue.accounting.domain.models.InvoiceReplica;

public interface IInvoiceProviderPort {

    void saveOrUpdate(InvoiceReplica invoice);

    // Obtener el saldo pendiente de una factura por su ID
    Optional<Long> getInvoiceBalance(Long invoiceId);

    /**
     * Busca una factura por su ID.
     * @param invoiceId El ID de la factura a buscar.
     * @return Un Optional que contiene el objeto de dominio InvoiceReplica si se encuentra, o un Optional vacío si no.
     */
    Optional<InvoiceReplica> findInvoiceById(Long invoiceId);

    /**
     * Busca factura por codigo de factura.
     * @param factCode El código de la factura a buscar.
     */
    Optional<InvoiceReplica> findInvoiceByFactCode(Long factCode);

    /**
     * Actualiza una factura en la base de datos.
     * @param invoice El objeto de dominio InvoiceReplica con los datos actualizados.
     */
    void updateInvoice(InvoiceReplica invoice);

    /**
     * Obtiene una lista de facturas con saldo pendiente para un cliente específico.
     *
     * @param clientId El ID del cliente.
     * @return Lista de modelos de dominio de InvoiceReplica.
     */
    List<InvoiceReplica> findPendingInvoicesByClientId(Long clientId);

    /**
     * Busca una lista de facturas por sus IDs.
     * Este método es crucial para poder procesar el castigo de varias facturas de forma eficiente.
     *
     * @param invoiceIds Lista de IDs de las facturas a buscar.
     * @return Una lista de objetos de dominio InvoiceReplica que fueron encontrados.
     *         Si un ID no corresponde a ninguna factura, simplemente no será incluido en la lista de resultados.
     */
    List<InvoiceReplica> findInvoicesByIds(List<Long> invoiceIds);

    /**
     * Busca una lista de facturas por el ID de la empresa.
     * @param enterpriseId El ID de la empresa.
     * @return Una lista de objetos de dominio InvoiceReplica que pertenecen a la empresa especificada.
     */
    List<InvoiceReplica> findInvoicesByEnterpriseId(String enterpriseId);

    /**
     * Obtiene todas las facturas con saldo pendiente de una empresa (para reporte consolidado).
     */
    List<InvoiceReplica> findPendingInvoicesByEnterpriseId(String enterpriseId);

}
