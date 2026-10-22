package co.unicauca.edu.co.contables.accounting.catalogue.taxes.infraestructure.adapters.output.jpaAdapters;

import co.unicauca.edu.co.contables.accounting.catalogue.taxes.application.output.ITaxSearchOutputPort;
import co.unicauca.edu.co.contables.accounting.catalogue.taxes.domain.models.Tax;
import co.unicauca.edu.co.contables.accounting.catalogue.taxes.infraestructure.adapters.output.jpaAdapters.entity.TaxEntity;
import co.unicauca.edu.co.contables.accounting.catalogue.taxes.infraestructure.adapters.output.jpaAdapters.mapper.ITaxSearchMapper;
import co.unicauca.edu.co.contables.accounting.catalogue.taxes.infraestructure.adapters.output.jpaAdapters.repository.ITaxRepository;

import lombok.Data;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @brief Adaptador JPA para operaciones de consulta de impuestos
 *
 * Implementa consultas paginadas y filtradas de impuestos
 * con conversión automática a modelos de dominio.
 */
@Component
@Data
public class TaxSearchJpaAdapter implements ITaxSearchOutputPort {
    private final ITaxRepository taxRepository;
    private final ITaxSearchMapper taxSearchMapper;

    /**
     * @brief Obtiene la información del impuesto basada en el c digo y el ID de empresa
     * @param code código del impuesto
     * @param idEnterprise ID de la empresa
     * @return impuesto correspondiente al código y ID de empresa proporcionados
     */
    @Override
    public Tax getTax(String code, String idEnterprise) {
        TaxEntity taxEntity=taxRepository.findByCode(code, idEnterprise);
        return taxSearchMapper.toDomain(taxEntity);
    }

    /**
     * @brief Obtiene una lista de impuestos activos asociados al ID de empresa proporcionado.
     * @param idEnterprise ID de la empresa
     * @return lista de impuestos activos asociados al ID de empresa proporcionado
     */
    @Override
    public List<Tax> getActiveTaxes(String idEnterprise) {
        List<TaxEntity> taxEntities = taxRepository.findActiveByIdEnterprise(idEnterprise);
        return taxSearchMapper.toDomainList(taxEntities);
    }

    /**
     * @brief Obtiene una lista de todos los impuestos asociados al ID de empresa proporcionado.
     * @param idEnterprise ID de la empresa
     * @return lista de todos los impuestos asociados al ID de empresa proporcionado
     */
    @Override
    public List<Tax> getTaxesByEnterprise(String idEnterprise) {
        List<TaxEntity> taxEntities = taxRepository.findAllByIdEnterprise(idEnterprise);
        return taxSearchMapper.toDomainList(taxEntities);
    }

    /**
     * @brief Obtiene un impuesto por ID y empresa.
     * Método optimizado que valida que el impuesto pertenece a la empresa especificada.
     * @param id ID del impuesto
     * @param idEnterprise ID de la empresa
     * @return impuesto encontrado o null si no existe
     */
    @Override
    public Tax getTaxByIdAndEnterprise(Long id, String idEnterprise) {
        TaxEntity taxEntity = taxRepository.findByIdAndIdEnterprise(id, idEnterprise);
        return taxSearchMapper.toDomain(taxEntity);
    }

    /**
     * @brief Obtiene página paginada de impuestos por empresa
     * @param idEnterprise ID de la empresa
     * @param page número de página (0-based)
     * @param size tamaño de página
     * @param sortField campo para ordenamiento
     * @param sortOrder dirección del ordenamiento
     * @return página de impuestos con conversión automática
     */
    @Override
    public Page<Tax> getTaxesPaginated(String idEnterprise, int page, int size, String sortField, String sortOrder) {
        Sort sort = Sort.by(sortOrder.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC, sortField);
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<TaxEntity> taxEntitiesPage = taxRepository.findAllByIdEnterprise(idEnterprise, pageable);
        return taxEntitiesPage.map(taxSearchMapper::toDomain);
    }

    /**
     * @brief Obtiene página filtrada de impuestos por búsqueda
     * @param idEnterprise ID de la empresa
     * @param search término de búsqueda parcial (código/descripción)
     * @param page número de página (0-based)
     * @param size tamaño de página
     * @param sortField campo para ordenamiento
     * @param sortOrder dirección del ordenamiento
     * @return página de impuestos filtrados con conversión automática
     */
    @Override
    public Page<Tax> getTaxesByCodeOrDescriptionPaginated(String idEnterprise, String search, int page, int size, String sortField, String sortOrder) {
        Sort sort = Sort.by(sortOrder.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC, sortField);
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<TaxEntity> taxEntitiesPage = taxRepository.findByIdEnterpriseAndDescriptionContainingIgnoreCase(idEnterprise, search, pageable);
        return taxEntitiesPage.map(taxSearchMapper::toDomain);
    }

    /**
     * @brief Cuenta el total de impuestos por empresa.
     * @param idEnterprise ID de la empresa
     * @return cantidad total de impuestos
     */
    @Override
    public long countTaxesByEnterprise(String idEnterprise) {
        return taxRepository.countByIdEnterprise(idEnterprise);
    }

    /**
     * @brief Cuenta impuestos por empresa y código o descripción.
     * @param idEnterprise ID de la empresa
     * @param search término de búsqueda en el código o descripción
     * @return cantidad de impuestos que coinciden con la búsqueda
     */
    @Override
    public long countTaxesByEnterpriseAndCodeOrDescription(String idEnterprise, String search) {
        return taxRepository.countByIdEnterpriseAndDescriptionContainingIgnoreCase(idEnterprise, search);
    }
}
