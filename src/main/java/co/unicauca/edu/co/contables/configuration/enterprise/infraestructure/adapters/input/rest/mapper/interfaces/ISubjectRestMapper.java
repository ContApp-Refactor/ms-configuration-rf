package co.unicauca.edu.co.contables.configuration.enterprise.infraestructure.adapters.input.rest.mapper.interfaces;

import java.util.List;

import org.mapstruct.Mapper;

import co.unicauca.edu.co.contables.configuration.enterprise.domain.models.Subject;
import co.unicauca.edu.co.contables.configuration.enterprise.infraestructure.adapters.input.rest.data.request.SubjectRequest;
import co.unicauca.edu.co.contables.configuration.enterprise.infraestructure.adapters.input.rest.data.response.SubjectResponse;

/**
 * Interfaz de mapeo REST para materias (subjects). Convierte entre los DTOs
 * de la capa web y el modelo de dominio. Utiliza MapStruct; la
 * implementación se registra como bean en {@code MapStructConfig}.
 */
@Mapper
public interface ISubjectRestMapper {

    /**
     * Convierte el modelo de dominio a su respuesta REST.
     *
     * @param subject el objeto de dominio
     * @return el DTO de respuesta con code y name
     */
    SubjectResponse toResponse(Subject subject);

    /**
     * Convierte una lista de modelos de dominio a respuestas REST.
     *
     * @param subjects la lista de modelos de dominio
     * @return la lista de DTOs de respuesta
     */
    List<SubjectResponse> toResponseList(List<Subject> subjects);

    /**
     * Convierte la solicitud REST al modelo de dominio.
     *
     * @param request el DTO de solicitud con code y name
     * @return el objeto de dominio Subject
     */
    Subject toDomain(SubjectRequest request);
}
