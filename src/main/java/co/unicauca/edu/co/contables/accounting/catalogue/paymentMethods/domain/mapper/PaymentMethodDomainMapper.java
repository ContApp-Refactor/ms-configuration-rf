package co.unicauca.edu.co.contables.accounting.catalogue.paymentMethods.domain.mapper;

import co.unicauca.edu.co.contables.accounting.catalogue.paymentMethods.domain.model.PaymentMethod;
import co.unicauca.edu.co.contables.accounting.catalogue.paymentMethods.presentation.DTO.request.PaymentMethodCreateReq;
import co.unicauca.edu.co.contables.accounting.catalogue.paymentMethods.presentation.DTO.request.PaymentMethodUpdateReq;
import co.unicauca.edu.co.contables.accounting.catalogue.paymentMethods.presentation.DTO.response.PaymentMethodRes;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * @brief Mapeador de dominio para conversiones de métodos de pago
 *
 * Gestiona las transformaciones entre DTOs de presentación y modelos de dominio,
 * manejando campos específicos como IDs de cuentas contables.
 */
@Mapper(componentModel = "spring")
public interface PaymentMethodDomainMapper {

    /**
     * @brief Convierte DTO de creación a modelo de dominio
     * @param request Datos de creación de método de pago
     * @return Modelo de dominio con ID ignorado y conversión de ID contable a String
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "accountingAccountEntity", ignore = true)
    @Mapping(target = "accountingAccount", expression = "java(\"\" + request.getAccountingAccountId())") // Convertir ID a String para compatibilidad
    PaymentMethod toDomain(PaymentMethodCreateReq request);

    /**
     * @brief Convierte DTO de actualización a modelo de dominio
     * @param request Datos de actualización de método de pago
     * @return Modelo de dominio con campos de estado ignorados y conversión de ID contable
     */
    @Mapping(target = "accountingAccountEntity", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "accountingAccount", expression = "java(\"\" + request.getAccountingAccountId())") // Convertir ID a String para compatibilidad
    PaymentMethod toDomain(PaymentMethodUpdateReq request);

    /**
     * @brief Convierte modelo de dominio a DTO de respuesta
     * @param domain Modelo de dominio de método de pago
     * @return DTO con información contable formateada y ID extraído
     */
    @Mapping(target = "accountingAccount", source = "accountingAccount")
    @Mapping(target = "accountingAccountId", expression = "java(domain.getAccountingAccountEntity() != null ? domain.getAccountingAccountEntity().getId() : null)")
    PaymentMethodRes toRes(PaymentMethod domain);


}
