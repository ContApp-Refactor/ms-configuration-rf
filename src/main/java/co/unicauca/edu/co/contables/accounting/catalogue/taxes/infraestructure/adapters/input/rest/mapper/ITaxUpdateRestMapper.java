package co.unicauca.edu.co.contables.accounting.catalogue.taxes.infraestructure.adapters.input.rest.mapper;

import co.unicauca.edu.co.contables.accounting.catalogue.taxes.domain.DTO.TaxDTO;
import co.unicauca.edu.co.contables.accounting.catalogue.taxes.domain.models.Tax;
import co.unicauca.edu.co.contables.accounting.catalogue.taxes.infraestructure.adapters.input.rest.dto.request.TaxUpdateReq;
import co.unicauca.edu.co.contables.accounting.catalogue.taxes.infraestructure.adapters.input.rest.dto.response.TaxUpdateRes;

import org.mapstruct.Mapper;

/**
 * @brief Mapeador REST para operaciones de actualización de impuestos
 *
 * Gestiona la conversión entre DTOs de presentación y modelos de dominio
 * para operaciones de actualización de impuestos.
 */
@Mapper
public interface ITaxUpdateRestMapper {
    /**
     * @brief Convierte request de actualización a DTO de dominio
     * @param taxUpdateReq request de actualización de impuesto
     * @return DTO de dominio con datos mapeados
     */
    default TaxDTO toDomain(TaxUpdateReq taxUpdateReq){
        if(taxUpdateReq==null){
            return null;
        }
        return TaxDTO.builder()
                .idEnterprise(taxUpdateReq.getIdEnterprise())
                .code(taxUpdateReq.getCode())
                .description(taxUpdateReq.getDescription())
                .interest(taxUpdateReq.getInterest())
                .salesTaxId(taxUpdateReq.getSalesTaxId())
                .purchaseTaxId(taxUpdateReq.getPurchaseTaxId())
                .build();

    }
    /**
     * @brief Convierte modelo de dominio a response de actualización
     * @param tax modelo de dominio con entidades relacionadas cargadas
     * @return response con IDs de cuentas contables extraídos
     */
    default TaxUpdateRes toCreateResponse(Tax tax){
        if(tax==null){
            return null;
        }
        return TaxUpdateRes.builder()
                .id(tax.getId())
                .idEnterprise(tax.getIdEnterprise())
                .code(tax.getCode())
                .description(tax.getDescription())
                .interest(tax.getInterest())
                .purchaseTax(tax.getPurchaseTax() != null ? tax.getPurchaseTax().getId() : null)
                .salesTax(tax.getSalesTax() != null ? tax.getSalesTax().getId() : null)
                .status(tax.getStatus())
                .usageCount(tax.getUsageCount())
                .build();
    }
}
