package co.unicauca.edu.co.contables.auxiliary.infrastructure.out.persistence.mapper;

import java.net.MalformedURLException;
import java.net.URL;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import co.unicauca.edu.co.contables.auxiliary.domain.models.core.AuxiliaryBook;
import co.unicauca.edu.co.contables.auxiliary.domain.models.core.criteria.AuxiliaryBookCriteria;
import co.unicauca.edu.co.contables.auxiliary.domain.models.core.criteria.CriteriaRange;
import co.unicauca.edu.co.contables.auxiliary.domain.models.core.export.AuxiliaryBookTemplate;
import co.unicauca.edu.co.contables.auxiliary.infrastructure.out.persistence.entity.AuxiliaryBookCriteriaEntity;
import co.unicauca.edu.co.contables.auxiliary.infrastructure.out.persistence.entity.AuxiliaryBookEntity;
import co.unicauca.edu.co.contables.auxiliary.infrastructure.out.persistence.entity.AuxiliaryBookTemplateEntity;

/**
 * @brief Mapper MapStruct entre el dominio y las entidades JPA del libro auxiliar.
 *
 * Convierte entre {@link AuxiliaryBook} (dominio) y
 * {@link AuxiliaryBookEntity}, incluyendo la conversión anidada de
 * criterios, plantilla y rango, así como las utilidades de
 * conversión URL ↔ String usadas para la ruta del logotipo.
 */
@Mapper(componentModel = "spring")
public interface IAuxiliaryBookCommandEntityMapper {

    @Mapping(target = "log", ignore = true)
    @Mapping(target = "history", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(source = "template", target = "template")
    @Mapping(source = "criteria", target = "criteria", qualifiedByName = "criteriaToEntity")
    AuxiliaryBookEntity toEntity(AuxiliaryBook auxiliaryBook);

    @Mapping(source = "criteria", target = "criteria", qualifiedByName = "criteriaEntityToDomain")
    @Mapping(source = "template", target = "template")
    AuxiliaryBook toDomain(AuxiliaryBookEntity auxiliaryBookEntity);

    @Named("criteriaToEntity")
    @Mapping(target = "auxiliaryBook", ignore = true)
    @Mapping(source = "criteriaRange.fromRange", target = "fromRange")
    @Mapping(source = "criteriaRange.toRange", target = "toRange")
    AuxiliaryBookCriteriaEntity toCriteriaEntity(AuxiliaryBookCriteria criteria);

    @Named("criteriaEntityToDomain")
    @Mapping(target = "criteriaRange", expression = "java(mapCriteriaRange(criteriaEntity))")
    @Mapping(target = "voucherType", ignore = true)
    AuxiliaryBookCriteria toCriteria(AuxiliaryBookCriteriaEntity criteriaEntity);

    @Mapping(target = "auxiliaryBook", ignore = true)
    @Mapping(source = "pathLogotype", target = "pathLogotype", qualifiedByName = "urlToString")
    @Mapping(source = "alienation", target = "aligment")
    AuxiliaryBookTemplateEntity toTemplateEntity(AuxiliaryBookTemplate template);

    @Mapping(source = "pathLogotype", target = "pathLogotype", qualifiedByName = "stringToUrl")
    @Mapping(source = "aligment", target = "alienation")
    @Mapping(target = "fontSize", ignore = true)
    AuxiliaryBookTemplate toTemplate(AuxiliaryBookTemplateEntity templateEntity);

    default CriteriaRange mapCriteriaRange(AuxiliaryBookCriteriaEntity criteriaEntity) {
        if (criteriaEntity == null) {
            return null;
        }
        if (criteriaEntity.getFromRange() == null && criteriaEntity.getToRange() == null) {
            return null;
        }
        return new CriteriaRange(criteriaEntity.getFromRange(), criteriaEntity.getToRange());
    }

    @Named("stringToUrl")
    default URL stringToUrl(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return new URL(value);
        } catch (MalformedURLException e) {
            return null;
        }
    }

    @Named("urlToString")
    default String urlToString(URL url) {
        return url != null ? url.toString() : null;
    }
}
