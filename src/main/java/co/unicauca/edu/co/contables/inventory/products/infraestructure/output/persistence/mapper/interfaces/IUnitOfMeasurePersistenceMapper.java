package co.unicauca.edu.co.contables.inventory.products.infraestructure.output.persistence.mapper.interfaces;

import co.unicauca.edu.co.contables.inventory.products.domain.model.UnitOfMeasure;
import co.unicauca.edu.co.contables.inventory.products.infraestructure.output.persistence.entity.UnitOfMeasureEntity;

import java.util.List;

import org.mapstruct.Mapper;

/**
 * @brief Mapper para transformación entre dominio y persistencia de unidades de medida
 *
 * Define contratos de mapeo bidireccional entre entidades JPA UnitOfMeasureEntity
 * y objetos de dominio UnitOfMeasure para operaciones de persistencia.
 */
@Mapper(componentModel = "spring")
public interface IUnitOfMeasurePersistenceMapper {

    /**
     * @brief Convierte objeto de dominio a entidad JPA
     * @param unitOfMeasure objeto UnitOfMeasure del dominio
     * @return UnitOfMeasureEntity correspondiente para persistencia
     */
    UnitOfMeasureEntity toUnitOfMeasureEntity(UnitOfMeasure unitOfMeasure);

    /**
     * @brief Convierte entidad JPA a objeto de dominio
     * @param unitOfMeasureEntity UnitOfMeasureEntity de persistencia
     * @return objeto UnitOfMeasure del dominio
     */
    UnitOfMeasure toUnitOfMeasure(UnitOfMeasureEntity unitOfMeasureEntity);

    /**
     * @brief Convierte lista de entidades JPA a lista de objetos de dominio
     * @param unitOfMeasureEntityList lista de UnitOfMeasureEntity de persistencia
     * @return lista de objetos UnitOfMeasure del dominio
     */
    List<UnitOfMeasure> toUnitOfMeasureList(List<UnitOfMeasureEntity> unitOfMeasureEntityList);
}
