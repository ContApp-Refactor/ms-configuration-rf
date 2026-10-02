package co.unicauca.edu.co.contables.accounting.catalogue.taxes.infraestructure.adapters.output.jpaAdapters;

import org.springframework.stereotype.Component;

import co.unicauca.edu.co.contables.accounting.catalogue.taxes.application.output.ITaxChangeStateOutputPort;
import co.unicauca.edu.co.contables.accounting.catalogue.taxes.domain.models.Tax;
import co.unicauca.edu.co.contables.accounting.catalogue.taxes.infraestructure.adapters.output.jpaAdapters.entity.TaxEntity;
import co.unicauca.edu.co.contables.accounting.catalogue.taxes.infraestructure.adapters.output.jpaAdapters.mapper.ITaxUpdateMapper;
import co.unicauca.edu.co.contables.accounting.catalogue.taxes.infraestructure.adapters.output.jpaAdapters.repository.ITaxRepository;

import lombok.AllArgsConstructor;

/**
 * @brief Adaptador JPA para operaciones de cambio de estado de impuestos
 *
 * Implementa la persistencia de cambios de estado de impuestos
 * mediante operaciones JPA con validación de existencia.
 */
@Component
@AllArgsConstructor
public class TaxChangeStateJpaAdapter implements ITaxChangeStateOutputPort {

    private final ITaxRepository taxRepository;
    private final ITaxUpdateMapper taxUpdateMapper;

    /**
     * @brief Cambia estado de impuesto con validación de existencia
     * @param id ID del impuesto
     * @param idEnterprise ID de la empresa
     * @param status nuevo estado a aplicar
     * @return impuesto con estado actualizado
     */
    @Override
    public Tax changeState(Long id, String idEnterprise, Boolean status) {
        TaxEntity taxEntity = taxRepository.findByIdAndIdEnterprise(id, idEnterprise);
        
        if (taxEntity == null) {
            return null;
        }

        taxEntity.setStatus(status);
        taxEntity = taxRepository.save(taxEntity);
        
        return taxUpdateMapper.toModel(taxEntity);
    }
}
