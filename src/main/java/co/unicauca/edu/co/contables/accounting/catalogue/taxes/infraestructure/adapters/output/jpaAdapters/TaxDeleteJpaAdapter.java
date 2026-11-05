package co.unicauca.edu.co.contables.accounting.catalogue.taxes.infraestructure.adapters.output.jpaAdapters;

import co.unicauca.edu.co.contables.accounting.catalogue.taxes.application.output.ITaxDeleteOutputPort;
import co.unicauca.edu.co.contables.accounting.catalogue.taxes.infraestructure.adapters.output.jpaAdapters.entity.TaxEntity;
import co.unicauca.edu.co.contables.accounting.catalogue.taxes.infraestructure.adapters.output.jpaAdapters.repository.ITaxRepository;

import jakarta.transaction.Transactional;
import lombok.Data;
import org.springframework.stereotype.Component;

/**
 * @brief Adaptador JPA para operaciones de eliminación de impuestos
 *
 * Implementa la eliminación de impuestos
 * y validación de existencia previa.
 */
@Component
@Data
public class TaxDeleteJpaAdapter implements ITaxDeleteOutputPort {

    private final ITaxRepository taxRepository;

    /**
     * @brief Realiza eliminación de un impuesto por su ID y empresa.
     * @param id ID del impuesto a eliminar
     * @param idEnterprise ID de la empresa
     * @return true si se eliminó con éxito, false de lo contrario
     */
    @Override
    @Transactional
    public boolean deleteByCode(long id, String idEnterprise) {
        TaxEntity taxEntity = taxRepository.findByIdAndIdEnterprise(Long.valueOf(id), idEnterprise);
        if (taxEntity != null) {
            taxRepository.delete(taxEntity);
            return true;
        }
        return false;
    }
}
