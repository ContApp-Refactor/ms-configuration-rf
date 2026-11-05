package co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.output.messageBroker.mapper;

import co.unicauca.edu.co.contables.accounting.debt_payments.domain.model.Replica.InvoiceReplica;
import co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.output.jpa.entity.replicas.InvoiceReplicaEntity;

import java.math.BigDecimal;
import java.util.List;


/**
 * @brief Mapper interface for converting between domain models and persistence entities related to invoices.
 */

public interface IInvoicePersistenceMapper {

    BigDecimal toDomain(BigDecimal balance);

    List<InvoiceReplica> toInvoiceReplicaList(List<InvoiceReplicaEntity> invoiceEntityList);
}
