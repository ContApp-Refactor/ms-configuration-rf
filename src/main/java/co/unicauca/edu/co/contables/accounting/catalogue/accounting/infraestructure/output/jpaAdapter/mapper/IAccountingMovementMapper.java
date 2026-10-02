package co.unicauca.edu.co.contables.accounting.catalogue.accounting.infraestructure.output.jpaAdapter.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import co.unicauca.edu.co.contables.accounting.catalogue.accounting.domain.models.AccountingMovement;
import co.unicauca.edu.co.contables.accounting.catalogue.accounting.infraestructure.output.jpaAdapter.entity.AccountingMovementEntity;

@Mapper(componentModel = "spring")
public interface IAccountingMovementMapper {
    // Al convertir de Dominio a Entidad, ignoramos la referencia al padre 'accountingEntry'.
    @Mapping(target = "accountingEntry", ignore = true)
    AccountingMovementEntity toEntity(AccountingMovement domain);

    AccountingMovement toDomain(AccountingMovementEntity entity);

     List<AccountingMovement> toDomainList(List<AccountingMovementEntity> entities);
}
