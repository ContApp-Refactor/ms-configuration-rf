package co.unicauca.edu.co.contables.accounting.catalogue.banks.domain.mapper;

import co.unicauca.edu.co.contables.accounting.catalogue.banks.domain.model.Bank;
import co.unicauca.edu.co.contables.accounting.catalogue.banks.presentation.DTO.request.BankCreateReq;
import co.unicauca.edu.co.contables.accounting.catalogue.banks.presentation.DTO.request.BankUpdateReq;
import co.unicauca.edu.co.contables.accounting.catalogue.banks.presentation.DTO.response.BankRes;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * @brief Mapeador de dominio para conversiones de bancos
 *
 * Gestiona las transformaciones entre DTOs de presentación y modelos de dominio,
 * manejando campos específicos como ID en operaciones de creación.
 */
@Mapper(componentModel = "spring")
public interface BankDomainMapper {
    
    /**
     * @brief Convierte DTO de creación a modelo de dominio
     * @param request Datos de creación de banco
     * @return Modelo de dominio sin ID (se genera automáticamente)
     */
    @Mapping(target = "id", ignore = true)
    Bank toDomain(BankCreateReq request);

    /**
     * @brief Convierte DTO de actualización a modelo de dominio
     * @param request Datos de actualización de banco
     * @return Modelo de dominio con ID incluido
     */
    Bank toDomain(BankUpdateReq request);

    /**
     * @brief Convierte modelo de dominio a DTO de respuesta
     * @param domain Modelo de dominio de banco
     * @return DTO para respuesta de la API
     */
    BankRes toRes(Bank domain);
}
