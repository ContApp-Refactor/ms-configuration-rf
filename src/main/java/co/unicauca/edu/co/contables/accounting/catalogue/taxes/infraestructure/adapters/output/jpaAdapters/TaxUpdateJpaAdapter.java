package co.unicauca.edu.co.contables.accounting.catalogue.taxes.infraestructure.adapters.output.jpaAdapters;

import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.infraestructure.adapters.output.jpaAdapter.entity.AccountCatalogueEntity;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.infraestructure.adapters.output.jpaAdapter.repository.IAccountCatalogueRepository;
import co.unicauca.edu.co.contables.accounting.catalogue.taxes.application.output.ITaxUpdateOutputPort;
import co.unicauca.edu.co.contables.accounting.catalogue.taxes.application.services.TaxValidationService;
import co.unicauca.edu.co.contables.accounting.catalogue.taxes.domain.DTO.TaxDTO;
import co.unicauca.edu.co.contables.accounting.catalogue.taxes.domain.models.Tax;
import co.unicauca.edu.co.contables.accounting.catalogue.taxes.infraestructure.adapters.output.jpaAdapters.entity.TaxEntity;
import co.unicauca.edu.co.contables.accounting.catalogue.taxes.infraestructure.adapters.output.jpaAdapters.mapper.ITaxUpdateMapper;
import co.unicauca.edu.co.contables.accounting.catalogue.taxes.infraestructure.adapters.output.jpaAdapters.repository.ITaxRepository;

import lombok.Data;
import org.springframework.stereotype.Component;

/**
 * @brief Adaptador JPA para operaciones de actualización de impuestos
 *
 * Implementa la actualización de impuestos con manejo condicional de relaciones
 * bidireccionales y validaciones selectivas de cambios.
 */
@Component
@Data
public class TaxUpdateJpaAdapter implements ITaxUpdateOutputPort {

    private final ITaxRepository taxRepository;

    private final ITaxUpdateMapper taxUpdateMapper;

    private final IAccountCatalogueRepository accountCatalogueRepository;

    private final TaxValidationService taxValidationService;

    /**
     * @brief Actualiza impuesto con manejo condicional de relaciones
     * @param taxDTO datos actualizados del impuesto
     * @param id identificador del impuesto a actualizar
     * @return impuesto actualizado con relaciones modificadas
     */
    @Override
    public Tax update(TaxDTO taxDTO, long id) {
        AccountCatalogueEntity salesTax;
        AccountCatalogueEntity purchaseTax;
        TaxEntity taxEntity = taxRepository.findByIdAndIdEnterprise(id, taxDTO.getIdEnterprise());

        if (taxEntity == null) {
            return null;
        }


        taxValidationService.validateAccountDigits(taxDTO.getSalesTaxId(), taxDTO.getPurchaseTaxId(), taxDTO.getIdEnterprise());
        taxValidationService.validateDifferentTaxAccounts(taxDTO.getSalesTaxId(), taxDTO.getPurchaseTaxId());

        // Manejar salesTax
        Long currentSalesTaxId = taxEntity.getSalesTax() != null ? taxEntity.getSalesTax().getId() : null;
        if (!java.util.Objects.equals(currentSalesTaxId, taxDTO.getSalesTaxId())) {
            salesTax = null;
            if (taxDTO.getSalesTaxId() != null) {
                salesTax = accountCatalogueRepository.findByIdAndIdEnterprise(taxDTO.getSalesTaxId(),
                        taxDTO.getIdEnterprise());
            }
            taxEntity.setSalesTax(salesTax);
        }

        // Manejar purchaseTax
        Long currentPurchaseTaxId = taxEntity.getPurchaseTax() != null ? taxEntity.getPurchaseTax().getId() : null;
        if (!java.util.Objects.equals(currentPurchaseTaxId, taxDTO.getPurchaseTaxId())) {
            purchaseTax = null;
            if (taxDTO.getPurchaseTaxId() != null) {
                purchaseTax = accountCatalogueRepository.findByIdAndIdEnterprise(taxDTO.getPurchaseTaxId(),
                        taxDTO.getIdEnterprise());
            }
            taxEntity.setPurchaseTax(purchaseTax);
        }

        if (!taxEntity.getCode().equals(taxDTO.getCode())) {
            taxValidationService.validateTaxCodeNotExists(taxDTO.getCode(), taxDTO.getIdEnterprise());
            taxEntity.setCode(taxDTO.getCode());
        }

        taxEntity.setDescription(taxDTO.getDescription());
        taxEntity.setInterest(taxDTO.getInterest());
        taxEntity = taxRepository.save(taxEntity);

        return taxUpdateMapper.toModel(taxEntity);
    }
}
