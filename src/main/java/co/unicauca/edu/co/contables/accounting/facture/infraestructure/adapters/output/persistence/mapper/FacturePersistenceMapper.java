package co.unicauca.edu.co.contables.accounting.facture.infraestructure.adapters.output.persistence.mapper;

import org.mapstruct.Mapper;

import co.unicauca.edu.co.contables.accounting.facture.domain.model.Facture;
import co.unicauca.edu.co.contables.accounting.facture.domain.model.Product;
import co.unicauca.edu.co.contables.accounting.facture.infraestructure.adapters.output.persistence.entity.FactureEntity;
import co.unicauca.edu.co.contables.accounting.facture.infraestructure.adapters.output.persistence.entity.ProductEntity;

@Mapper(componentModel = "spring")
public interface FacturePersistenceMapper {

    FactureEntity toFactureEntity(Facture facture);

    Facture toFacture(FactureEntity factureEntity);

    ProductEntity toProductEntity(Product product);
}
