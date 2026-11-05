package co.unicauca.edu.co.contables.configuration.enterprise.domain.mapper;

import org.mapstruct.Mapper;

import co.unicauca.edu.co.contables.configuration.enterprise.domain.models.Subject;
import co.unicauca.edu.co.contables.configuration.enterprise.presentation.DTO.request.SubjectReq;
import co.unicauca.edu.co.contables.configuration.enterprise.presentation.DTO.response.SubjectRes;

import java.util.List;

/**
 * @brief Mapeador de dominio para materias (subjects).
 */
@Mapper(componentModel = "spring")
public interface SubjectDomainMapper {

    Subject toDomain(SubjectReq req);

    SubjectRes toRes(Subject domain);

    List<SubjectRes> toResList(List<Subject> domain);
}
