package co.unicauca.edu.co.contables.configuration.accountingCalendar.domain.mapper;

import co.unicauca.edu.co.contables.configuration.accountingCalendar.domain.models.AccountingCalendar;
import co.unicauca.edu.co.contables.configuration.accountingCalendar.presentation.DTO.request.*;
import co.unicauca.edu.co.contables.configuration.accountingCalendar.presentation.DTO.response.AccountingCalendarRes;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * @brief Mapeador de dominio para el calendario contable
 *
 * Interfaz que utiliza MapStruct para convertir entre modelos de dominio
 * (AccountingCalendar) y DTOs de presentación (request/response).
 * Gestiona la transformación de datos entre la capa de dominio y la presentación.
 */
@Mapper(componentModel = "spring")
public interface AccountingCalendarDomainMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "tenantId", ignore = true)
    @Mapping(target = "date", ignore = true) // La fecha se asigna manualmente después de parsear
    AccountingCalendar toDomain(AccountingCalendarCreateReq req);

    AccountingCalendarRes toRes(AccountingCalendar domain);
}


