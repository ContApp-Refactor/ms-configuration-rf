package co.unicauca.edu.co.contables.accounting.treasury.infrastructure.adapters.output.jpa.mapper;

import co.unicauca.edu.co.contables.accounting.treasury.domain.model.SupplierInvoiceReplica;
import co.unicauca.edu.co.contables.accounting.treasury.infrastructure.adapters.output.jpa.entity.SupplierInvoiceReplicaEntity;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ISupplierInvoicePersistenceMapper {
    SupplierInvoiceReplica toDomain(SupplierInvoiceReplicaEntity entity);
    List<SupplierInvoiceReplica> toDomainList(List<SupplierInvoiceReplicaEntity> entities);
    SupplierInvoiceReplicaEntity toEntity(SupplierInvoiceReplica domain);
}
