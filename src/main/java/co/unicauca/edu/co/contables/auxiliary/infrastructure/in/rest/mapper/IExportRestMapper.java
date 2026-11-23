package co.unicauca.edu.co.contables.auxiliary.infrastructure.in.rest.mapper;

import co.unicauca.edu.co.contables.auxiliary.domain.models.core.export.ExportInfo;
import co.unicauca.edu.co.contables.auxiliary.infrastructure.in.rest.dto.request.ExportAuxiliaryBookRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * @brief Mapper REST entre la solicitud de exportación y la info del dominio.
 *
 * Convierte un {@link ExportAuxiliaryBookRequest} recibido por la
 * capa REST a {@link ExportInfo}, estructura de dominio utilizada por
 * el caso de uso de exportación. Implementado por MapStruct.
 */
@Mapper(componentModel = "spring")
public interface IExportRestMapper {
    @Mapping(source = "format", target = "format")
    @Mapping(source = "entName", target = "entName")
    @Mapping(source = "auxiliaryBook", target = "auxiliaryBook")
    @Mapping(source = "auxBookData", target = "auxBookData")
    @Mapping(source = "infoReportTemplate", target = "infoReportTemplate")
    ExportInfo toDomain(ExportAuxiliaryBookRequest exportAuxiliaryBookRequest);
}
