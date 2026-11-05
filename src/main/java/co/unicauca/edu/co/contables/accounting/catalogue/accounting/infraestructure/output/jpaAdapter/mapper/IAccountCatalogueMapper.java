package co.unicauca.edu.co.contables.accounting.catalogue.accounting.infraestructure.output.jpaAdapter.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.domain.models.AccountCatalogue;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.infraestructure.adapters.output.jpaAdapter.entity.AccountCatalogueEntity;

@Mapper(componentModel = "spring")
public interface IAccountCatalogueMapper {
      // Ignoramos los hijos para evitar recursividad infinita en el mapeo inicial.
    // La lógica del servicio se encargará de ensamblar la jerarquía.
    @Mapping(target = "children", ignore = true)
    AccountCatalogue toDomain(AccountCatalogueEntity entity);

    List<AccountCatalogue> toDomainList(List<AccountCatalogueEntity> entities);
    
    // Si también necesitas mapear de Dominio a Entidad en algún momento
    AccountCatalogueEntity toEntity(AccountCatalogue domain);
}
