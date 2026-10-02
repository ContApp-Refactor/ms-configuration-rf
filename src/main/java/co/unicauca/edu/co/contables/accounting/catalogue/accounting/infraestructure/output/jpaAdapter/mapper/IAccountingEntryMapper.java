package co.unicauca.edu.co.contables.accounting.catalogue.accounting.infraestructure.output.jpaAdapter.mapper;

import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

import co.unicauca.edu.co.contables.accounting.catalogue.accounting.domain.models.AccountingEntry;
import co.unicauca.edu.co.contables.accounting.catalogue.accounting.infraestructure.output.jpaAdapter.entity.AccountingEntryEntity;

@Mapper(componentModel = "spring", uses = {IAccountingMovementMapper.class})
public interface IAccountingEntryMapper {
     // El 'sourceDocumentId' del dominio se mapea a la entidad 'sourceDocument'.
    // Necesitaremos un paso extra para cargar la entidad ReceiptEntity.
    // Por simplicidad, lo manejaremos en el adaptador.

    AccountingEntryEntity toEntity(AccountingEntry domain);

    // Al convertir de entidad a dominio, mapeamos el ID de la entidad anidada al campo Long.

    AccountingEntry toDomain(AccountingEntryEntity entity);

    @AfterMapping
    default void setEntryInMovements(AccountingEntry domain, @MappingTarget AccountingEntryEntity entity) {
        if (entity.getMovements() != null) {
            entity.getMovements().forEach(movement -> movement.setAccountingEntry(entity));
        }
    }
}
