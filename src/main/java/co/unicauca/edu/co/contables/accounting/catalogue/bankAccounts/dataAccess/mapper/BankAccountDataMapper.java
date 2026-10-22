package co.unicauca.edu.co.contables.accounting.catalogue.bankAccounts.dataAccess.mapper;

import co.unicauca.edu.co.contables.accounting.catalogue.bankAccounts.dataAccess.entity.BankAccountEntity;
import co.unicauca.edu.co.contables.accounting.catalogue.bankAccounts.domain.model.BankAccount;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * @brief Mapeador de datos para entidades de cuentas bancarias
 *
 * Define el contrato para la conversión entre entidades de base de datos
 * y modelos de dominio, gestionando campos específicos de relaciones.
 */
@Mapper(componentModel = "spring")
public interface BankAccountDataMapper {

    /**
     * @brief Convierte entidad de base de datos a modelo de dominio
     * @param entity Entidad de cuenta bancaria con relaciones cargadas
     * @return Modelo de dominio con ID de cuenta contable extraído
     */
    @Mapping(target = "accountingAccountId", source = "accountingAccount.id")
    BankAccount toDomain(BankAccountEntity entity);

    /**
     * @brief Convierte modelo de dominio a entidad de base de datos
     * @param domain Modelo de dominio de cuenta bancaria
     * @return Entidad preparada para persistencia (tenantId se establece en runtime)
     */
    @Mapping(target = "tenantId", ignore = true)
    @Mapping(target = "bank.tenantId", ignore = true)
    @Mapping(target = "accountingAccount", ignore = true)
    BankAccountEntity toEntity(BankAccount domain);
}
