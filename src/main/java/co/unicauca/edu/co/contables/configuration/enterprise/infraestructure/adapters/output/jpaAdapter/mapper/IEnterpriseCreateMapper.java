package co.unicauca.edu.co.contables.configuration.enterprise.infraestructure.adapters.output.jpaAdapter.mapper;

import co.unicauca.edu.co.contables.configuration.enterprise.domain.models.Enterprise;
import co.unicauca.edu.co.contables.configuration.enterprise.infraestructure.adapters.output.jpaAdapter.entity.EnterpriseEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

/**
 * Interfaz de mapeo para convertir entre entidades de empresa y su representación en el dominio.
 * Utiliza MapStruct para generar automáticamente las implementaciones de mapeo.
 */
@Mapper
public interface IEnterpriseCreateMapper {

    
    /**
     * Convierte un objeto de dominio Enterprise a su entidad correspondiente.
     * Ignora el campo tenantId durante el mapeo.
     *
     * @param enterprise el objeto de dominio a convertir
     * @return la entidad EnterpriseEntity correspondiente
     */
    @Mappings({@Mapping(target = "tenantId", ignore = true)})
    EnterpriseEntity toEntity(Enterprise enterprise);



    /**
     * Convierte una entidad EnterpriseEntity a su representación en el dominio.
     *
     * @param enterpriseEntity la entidad a convertir
     * @return el objeto de dominio Enterprise correspondiente
     */
    Enterprise toModel(EnterpriseEntity enterpriseEntity);
}
