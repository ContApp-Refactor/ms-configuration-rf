package co.unicauca.edu.co.contables.configuration.enterprise.infraestructure.adapters.output.jpaAdapter.mapper;

import java.util.List;

import org.mapstruct.Mapper;

import co.unicauca.edu.co.contables.configuration.enterprise.domain.models.Subject;
import co.unicauca.edu.co.contables.configuration.enterprise.infraestructure.adapters.output.jpaAdapter.entity.SubjectEntity;

/**
 * Interfaz de mapeo para convertir entre la entidad JPA de materias y su
 * representación en el dominio. Utiliza MapStruct para generar
 * automáticamente las implementaciones de mapeo.
 *
 * La instancia se registra como bean en {@code MapStructConfig} (igual que
 * el resto de los mappers del módulo).
 */
@Mapper
public interface ISubjectMapper {

    /**
     * Convierte una entidad SubjectEntity a su representación en el dominio.
     *
     * @param subjectEntity la entidad a convertir
     * @return el objeto de dominio Subject correspondiente
     */
    Subject toDomain(SubjectEntity subjectEntity);

    /**
     * Convierte una lista de entidades a su representación en el dominio.
     *
     * @param subjectEntities las entidades a convertir
     * @return la lista de objetos de dominio Subject
     */
    List<Subject> toDomainList(List<SubjectEntity> subjectEntities);

    /**
     * Convierte un objeto de dominio Subject a su entidad correspondiente.
     *
     * @param subject el objeto de dominio a convertir
     * @return la entidad SubjectEntity correspondiente
     */
    SubjectEntity toEntity(Subject subject);
}
