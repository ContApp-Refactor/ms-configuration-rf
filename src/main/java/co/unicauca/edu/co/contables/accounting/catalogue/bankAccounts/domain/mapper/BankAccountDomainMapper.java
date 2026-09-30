package co.unicauca.edu.co.contables.accounting.catalogue.bankAccounts.domain.mapper;

import co.unicauca.edu.co.contables.accounting.catalogue.bankAccounts.domain.model.BankAccount;
import co.unicauca.edu.co.contables.accounting.catalogue.bankAccounts.presentation.DTO.request.BankAccountCreateReq;
import co.unicauca.edu.co.contables.accounting.catalogue.bankAccounts.presentation.DTO.request.BankAccountUpdateReq;
import co.unicauca.edu.co.contables.accounting.catalogue.bankAccounts.presentation.DTO.response.BankAccountRes;
import co.unicauca.edu.co.contables.accounting.catalogue.banks.domain.mapper.BankDomainMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * @brief Mapeador de dominio para conversiones de cuentas bancarias
 *
 * Gestiona las transformaciones entre DTOs de presentación y modelos de dominio,
 * delegando mapeos complejos de bancos al BankDomainMapper.
 */
@Mapper(componentModel = "spring", uses = BankDomainMapper.class)
public interface BankAccountDomainMapper {
    
    /**
     * @brief Convierte DTO de creación a modelo de dominio
     * @param request Datos de creación de cuenta bancaria
     * @return Modelo de dominio con campos de relaciones sin mapear
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "bank", ignore = true)
    @Mapping(target = "accountingAccountId", ignore = true)
    BankAccount toDomain(BankAccountCreateReq request);

    /**
     * @brief Convierte DTO de actualización a modelo de dominio
     * @param request Datos de actualización de cuenta bancaria
     * @return Modelo de dominio con campos de relaciones sin mapear
     */
    @Mapping(target = "bank", ignore = true)
    @Mapping(target = "accountingAccountId", ignore = true)
    BankAccount toDomain(BankAccountUpdateReq request);

    /**
     * @brief Convierte modelo de dominio a DTO de respuesta
     * @param domain Modelo de dominio de cuenta bancaria
     * @return DTO para respuesta de la API
     */
    BankAccountRes toRes(BankAccount domain);
}
