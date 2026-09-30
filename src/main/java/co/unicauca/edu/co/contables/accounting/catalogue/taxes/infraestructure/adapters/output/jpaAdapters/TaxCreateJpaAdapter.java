package co.unicauca.edu.co.contables.accounting.catalogue.taxes.infraestructure.adapters.output.jpaAdapters;

import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.infraestructure.adapters.output.jpaAdapter.entity.AccountCatalogueEntity;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.infraestructure.adapters.output.jpaAdapter.repository.IAccountCatalogueRepository;
import co.unicauca.edu.co.contables.accounting.catalogue.taxes.application.output.ITaxCreateOutputPort;
import co.unicauca.edu.co.contables.accounting.catalogue.taxes.application.services.TaxValidationService;
import co.unicauca.edu.co.contables.accounting.catalogue.taxes.domain.DTO.TaxDTO;
import co.unicauca.edu.co.contables.accounting.catalogue.taxes.domain.models.Tax;
import co.unicauca.edu.co.contables.accounting.catalogue.taxes.infraestructure.adapters.output.jpaAdapters.entity.TaxEntity;
import co.unicauca.edu.co.contables.accounting.catalogue.taxes.infraestructure.adapters.output.jpaAdapters.mapper.ITaxCreateMapper;
import co.unicauca.edu.co.contables.accounting.catalogue.taxes.infraestructure.adapters.output.jpaAdapters.repository.ITaxRepository;

import lombok.Data;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

/**
 * @brief Adaptador JPA para operaciones de creación de impuestos
 *
 * Implementa la persistencia de nuevos impuestos con manejo de relaciones
 * bidireccionales con cuentas contables y validaciones de negocio.
 */
@Component
@Data
public class TaxCreateJpaAdapter implements ITaxCreateOutputPort {

    private final ITaxCreateMapper taxCreateMapper;
    private final ITaxRepository taxRepository;
    private final IAccountCatalogueRepository accountCatalogueRepository;
    private final TaxValidationService taxValidationService;

    /**
     * @brief Crea nuevo impuesto con validaciones y manejo de relaciones
     * @param tax datos del impuesto a crear
     * @return impuesto creado con relaciones establecidas
     */
    @Override
    public Tax createTax(TaxDTO tax) {
        // Validaciones usando el servicio centralizado
        taxValidationService.validateTaxCodeNotExists(tax.getCode(), tax.getIdEnterprise());
        taxValidationService.validateAccountDigits(tax.getSalesTaxId(), tax.getPurchaseTaxId(), tax.getIdEnterprise());
        taxValidationService.validateDifferentTaxAccounts(tax.getSalesTaxId(), tax.getPurchaseTaxId());

        AccountCatalogueEntity salesTax = null;
        if (tax.getSalesTaxId() != null) {
            salesTax = accountCatalogueRepository.findByIdAndIdEnterprise(tax.getSalesTaxId(),
                    tax.getIdEnterprise());
        }

        AccountCatalogueEntity purchaseTax = null;
        if (tax.getPurchaseTaxId() != null) {
            purchaseTax = accountCatalogueRepository.findByIdAndIdEnterprise(tax.getPurchaseTaxId(),
                    tax.getIdEnterprise());
        }

        Tax taxAux = Tax.builder()
                .code(tax.getCode())
                .idEnterprise(tax.getIdEnterprise())
                .description(tax.getDescription())
                .interest(tax.getInterest())
                .purchaseTax(purchaseTax)
                .salesTax(salesTax)
                .build();

        TaxEntity taxEntity = taxCreateMapper.toEntity(taxAux);

        // Asignar las entidades relacionadas obtenidas del repositorio
        taxEntity.setSalesTax(salesTax);
        taxEntity.setPurchaseTax(purchaseTax);

        // Mantener la integridad de las relaciones bidireccionales
        // Inicializar las colecciones lazy si no están cargadas
        if (salesTax != null) {
            if (salesTax.getSalesTaxes() == null) {
                salesTax.setSalesTaxes(new ArrayList<>());
            }
            salesTax.getSalesTaxes().add(taxEntity);
        }
        if (purchaseTax != null) {
            if (purchaseTax.getPurchaseTaxes() == null) {
                purchaseTax.setPurchaseTaxes(new ArrayList<>());
            }
            purchaseTax.getPurchaseTaxes().add(taxEntity);
        }
        taxEntity = taxRepository.save(taxEntity);
        return taxCreateMapper.toModel(taxEntity);
    }
}
