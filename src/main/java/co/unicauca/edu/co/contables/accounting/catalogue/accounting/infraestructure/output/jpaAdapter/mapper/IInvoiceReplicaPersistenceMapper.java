package co.unicauca.edu.co.contables.accounting.catalogue.accounting.infraestructure.output.jpaAdapter.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import co.unicauca.edu.co.contables.accounting.catalogue.accounting.domain.models.InvoiceReplica;
import co.unicauca.edu.co.contables.accounting.catalogue.accounting.infraestructure.output.jpaAdapter.entity.InvoiceReplicaEntity;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface IInvoiceReplicaPersistenceMapper {
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
