package co.unicauca.edu.co.contables.accounting.catalogue.paymentMethods.dataAccess.mapper;

import co.unicauca.edu.co.contables.accounting.catalogue.paymentMethods.dataAccess.entity.PaymentMethodEntity;
import co.unicauca.edu.co.contables.accounting.catalogue.paymentMethods.domain.model.PaymentMethod;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.infraestructure.adapters.output.jpaAdapter.entity.AccountCatalogueEntity;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.domain.models.AccountCatalogue;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * @brief Mapeador de datos para entidades de métodos de pago
 *
 * Gestiona la conversión entre entidades de base de datos y modelos de dominio,
 * manejando campos específicos de relaciones contables con expresiones personalizadas.
 */
@Mapper(componentModel = "spring")
public interface PaymentMethodDataMapper {

    /**
     * @brief Convierte entidad de base de datos a modelo de dominio
     * @param entity Entidad de método de pago con relaciones cargadas
     * @return Modelo de dominio con información contable formateada
     */
    @Mapping(target = "accountingAccount", expression = "java(entity.getAccountingAccount() != null ? entity.getAccountingAccount().getCode() + \" - \" + entity.getAccountingAccount().getDescription() : \"\")")
    @Mapping(target = "accountingAccountEntity", expression = "java(mapToAccountCatalogue(entity.getAccountingAccount()))")
    PaymentMethod toDomain(PaymentMethodEntity entity);

    /**
     * @brief Convierte modelo de dominio a entidad de base de datos
     * @param domain Modelo de dominio de método de pago
     * @return Entidad preparada para persistencia (relaciones se establecen posteriormente)
     */
    @Mapping(target = "tenantId", ignore = true)
    @Mapping(target = "accountingAccount", ignore = true)
    PaymentMethodEntity toEntity(PaymentMethod domain);


    /**
     * @brief Convierte entidad contable a modelo de dominio
     * @param entity Entidad de cuenta contable JPA
     * @return Modelo de dominio de cuenta contable o null si entity es null
     */
    default AccountCatalogue mapToAccountCatalogue(AccountCatalogueEntity entity) {
        if (entity == null) {
            return null;
        }
        return AccountCatalogue.builder()
                .id(entity.getId())
                .code(entity.getCode())
                .description(entity.getDescription())
                .build();
    }
}
