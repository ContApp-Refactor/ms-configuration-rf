package co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.output.jpa.mapper.replicas;

import co.unicauca.edu.co.contables.accounting.debt_payments.domain.model.Replica.InvoiceReplica;
import co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.output.jpa.entity.replicas.InvoiceReplicaEntity;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;


@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface IInvoicePersistenceMapper {
    /**
     * Convierte una entidad JPA (InvoiceReplicaEntity) a un objeto de dominio (InvoiceReplica).
     * La lógica de negocio solo trabajará con objetos de dominio.
     */
    InvoiceReplica toDomain(InvoiceReplicaEntity entity);

    /**
     * Convierte un objeto de dominio (InvoiceReplica) a una entidad JPA (InvoiceReplicaEntity).
     * Esto es necesario antes de poder guardar los cambios en la base de datos.
     */
    
    InvoiceReplicaEntity toEntity(InvoiceReplica domain);

    /**
     * Convierte una lista de entidades JPA a una lista de objetos de dominio.
     * @param invoiceEntityList La lista de entidades JPA a convertir.
     * @return La lista de objetos de dominio resultante.
     */
    List<InvoiceReplica> toInvoiceReplicaList(List<InvoiceReplicaEntity> invoiceEntityList);
}
