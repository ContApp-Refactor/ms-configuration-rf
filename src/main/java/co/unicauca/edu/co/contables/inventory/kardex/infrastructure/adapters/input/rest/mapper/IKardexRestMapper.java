package co.unicauca.edu.co.contables.inventory.kardex.infrastructure.adapters.input.rest.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import co.unicauca.edu.co.contables.inventory.kardex.domain.model.Kardex;
import co.unicauca.edu.co.contables.inventory.kardex.infrastructure.adapters.input.rest.dto.request.KardexAdjustmentEntryDtoRequest;
import co.unicauca.edu.co.contables.inventory.kardex.infrastructure.adapters.input.rest.dto.request.KardexAdjustmentExitDtoRequest;

@Mapper(componentModel = "spring", implementationName = "kardex<CLASS_NAME>Impl")
public interface IKardexRestMapper {

    @Mapping(target = "balanceQuantity", ignore = true)
    @Mapping(target = "balanceUnitPrice", ignore = true)
    @Mapping(target = "totalBalance", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "factCode", ignore = true)
    Kardex toDomain(KardexAdjustmentEntryDtoRequest kardexAdjustmentEntryDtoRequest);

    @Mapping(target = "balanceQuantity", ignore = true)
    @Mapping(target = "balanceUnitPrice", ignore = true)
    @Mapping(target = "totalBalance", ignore = true)
    @Mapping(target = "unitPrice", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "factCode", ignore = true)
    Kardex toDomain(KardexAdjustmentExitDtoRequest kardexAdjustmentExitDtoRequest);

}
