package co.unicauca.edu.co.contables.accounting.catalogue.accounting.infraestructure.output.jpaAdapter.mapper;

import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import co.unicauca.edu.co.contables.accounting.catalogue.accounting.domain.models.Receipt;
import co.unicauca.edu.co.contables.accounting.catalogue.accounting.infraestructure.output.jpaAdapter.entity.ReceiptEntity;

@Mapper(componentModel = "spring", uses = {IReceiptDetailMapper.class})
public interface IReceiptMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(source = "id", target = "originalReceiptId")
    ReceiptEntity toEntity(Receipt domain);

    //@Mapping(source = "originalReceiptId", target = "id")
    Receipt toDomain(ReceiptEntity entity);
    
    // Método para manejar la relación bidireccional después del mapeo
    @AfterMapping
    default void setReceiptInDetails(Receipt domain, @MappingTarget ReceiptEntity entity) {
        if (entity.getDetails() != null) {
            entity.getDetails().forEach(detail -> detail.setReceipt(entity));
        }
    }

    // El método de actualización también necesita manejar la relación
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "originalReceiptId", ignore = true)
    void updateEntityFromDomain(Receipt domain, @MappingTarget ReceiptEntity entity);

    @AfterMapping
    default void updateReceiptInDetails(Receipt domain, @MappingTarget ReceiptEntity entity) {
        if (entity.getDetails() != null) {
            entity.getDetails().forEach(detail -> detail.setReceipt(entity));
        }
    }
    
}