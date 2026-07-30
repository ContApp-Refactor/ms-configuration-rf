package co.unicauca.edu.co.contables.configuration.accountingCalendar.dataAccess.mapper;

import co.unicauca.edu.co.contables.configuration.accountingCalendar.dataAccess.entity.AccountingCalendarEntity;
import co.unicauca.edu.co.contables.configuration.accountingCalendar.domain.models.AccountingCalendar;
import org.mapstruct.Mapper;

/**
 * @brief Mapeador de datos para el calendario contable
 *
 * Interfaz que utiliza MapStruct para convertir entre entidades de base de datos
 * (AccountingCalendarEntity) y modelos de dominio (AccountingCalendar).
 * Gestiona la transformación automática de datos entre capas.
 */
@Mapper(componentModel = "spring")
public interface AccountingCalendarDataMapper {
    AccountingCalendarEntity toEntity(AccountingCalendar domain);
    AccountingCalendar toDomain(AccountingCalendarEntity entity);
}


