package co.unicauca.edu.co.contables.accounting.catalogue.accounting.infraestructure.output.jpaAdapter.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import co.unicauca.edu.co.contables.accounting.catalogue.accounting.domain.models.ReceiptDetail;
import co.unicauca.edu.co.contables.accounting.catalogue.accounting.infraestructure.output.jpaAdapter.entity.ReceiptDetailEntity;

@Mapper(componentModel = "spring")
public interface IReceiptDetailMapper {
    @Mapping(target = "id", ignore = true) 
    @Mapping(target = "receipt", ignore = true)
    ReceiptDetailEntity toEntity(ReceiptDetail domain);

    ReceiptDetail toDomain(ReceiptDetailEntity entity);

    List<ReceiptDetail> toReceiptDetailList(List<ReceiptDetailEntity> entities);
}
