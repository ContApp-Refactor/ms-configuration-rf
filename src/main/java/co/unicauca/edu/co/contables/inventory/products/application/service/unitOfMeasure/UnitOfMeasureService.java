package co.unicauca.edu.co.contables.inventory.products.application.service.unitOfMeasure;

import co.unicauca.edu.co.contables.inventory.products.application.ports.input.IUnitOfMeasureServicePort;
import co.unicauca.edu.co.contables.inventory.products.application.ports.output.IUnitOfMeasurePersistencePort;
import co.unicauca.edu.co.contables.inventory.products.application.service.product.ProductService;
import co.unicauca.edu.co.contables.inventory.products.domain.exception.unitOfMeasure.UnitOfMeasureAbbreviationAlreadyExistsException;
import co.unicauca.edu.co.contables.inventory.products.domain.exception.unitOfMeasure.UnitOfMeasureAssociatedException;
import co.unicauca.edu.co.contables.inventory.products.domain.exception.unitOfMeasure.UnitOfMeasureInUseException;
import co.unicauca.edu.co.contables.inventory.products.domain.exception.unitOfMeasure.UnitOfMeasureNameAlreadyExistsException;
import co.unicauca.edu.co.contables.inventory.products.domain.exception.unitOfMeasure.UnitOfMeasureNotFoundException;
import co.unicauca.edu.co.contables.inventory.products.domain.model.Product;
import co.unicauca.edu.co.contables.inventory.products.domain.model.UnitOfMeasure;
import co.unicauca.edu.co.contables.inventory.products.domain.utils.StringNormalizer;
import co.unicauca.edu.co.contables.inventory.products.infraestructure.audit.annotation.Auditable;
import co.unicauca.edu.co.contables.inventory.products.infraestructure.audit.annotation.OperationType;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.List;



/**
 * Servicio que implementa la lógica de negocio para las unidades de medida.
 * Esta clase interactúa con los puertos de persistencia y realiza las operaciones
 * necesarias para gestionar las unidades de medida.
 */
@Service
@RequiredArgsConstructor
public class UnitOfMeasureService implements IUnitOfMeasureServicePort {

    private final IUnitOfMeasurePersistencePort unitMeasurePersistencePort;
    private final ProductService productServicePort;

    @Override
    public UnitOfMeasure findByIdAndEnterpriseId(Long id, String enterpriseId) {
        return unitMeasurePersistencePort.findByIdAndEnterpriseId(id, enterpriseId)
                .orElseThrow(UnitOfMeasureNotFoundException::new);
    }

    @Auditable(operationType = OperationType.CREATE, affectedTable = "UNIT_OF_MEASURE")
    @Override
    public UnitOfMeasure create(UnitOfMeasure unitOfMeasure) {
        // Normalizar nombre y abreviación de manera consistente (para validación y almacenamiento)
        unitOfMeasure.setName(StringNormalizer.normalize(unitOfMeasure.getName()));
        unitOfMeasure.setAbbreviation(StringNormalizer.normalize(unitOfMeasure.getAbbreviation()));
        validateUnitOfMeasureUniqueness(unitOfMeasure);
        return unitMeasurePersistencePort.create(unitOfMeasure);
    }

    @Auditable(operationType = OperationType.UPDATE, affectedTable = "UNIT_OF_MEASURE")
    @Override
    public UnitOfMeasure update(Long id, String enterpriseId, UnitOfMeasure unitOfMeasure) {
        return unitMeasurePersistencePort.findByIdAndEnterpriseId(id, enterpriseId)
                .map(existingUnit -> {
                    // Verificar que la unidad de medida no contenga productos en uso antes de permitir la edición
                    validateUnitOfMeasureNotInUse(id);
                    // Normalizar nombre y abreviación de manera consistente (para validación y almacenamiento)
                    unitOfMeasure.setName(StringNormalizer.normalize(unitOfMeasure.getName()));
                    unitOfMeasure.setAbbreviation(StringNormalizer.normalize(unitOfMeasure.getAbbreviation()));
                    validateUnitOfMeasureUniquenessForUpdate(id, unitOfMeasure);
                    existingUnit.setName(unitOfMeasure.getName());
                    existingUnit.setDescription(unitOfMeasure.getDescription());
                    existingUnit.setAbbreviation(unitOfMeasure.getAbbreviation());
                    return unitMeasurePersistencePort.create(existingUnit);
                })
                .orElseThrow(UnitOfMeasureNotFoundException::new);
    }

    @Auditable(operationType = OperationType.INACTIVATE, affectedTable = "UNIT_OF_MEASURE")
    @Override
    public void changeState(Long id, String enterpriseId) {
        UnitOfMeasure unitOfMeasure = unitMeasurePersistencePort.findByIdAndEnterpriseId(id, enterpriseId)
                .orElseThrow(UnitOfMeasureNotFoundException::new);
        unitOfMeasure.setState(!unitOfMeasure.isState());
        unitMeasurePersistencePort.create(unitOfMeasure);
    }

    @Auditable(operationType = OperationType.DELETE, affectedTable = "UNIT_OF_MEASURE")
    @Override
    public void deleteById(Long id, String enterpriseId) {
        if (unitMeasurePersistencePort.findByIdAndEnterpriseId(id, enterpriseId).isEmpty()) {
            throw new UnitOfMeasureNotFoundException();
        }
        List<Product> products = productServicePort.findAllByUnitOfMeasure(id);
        if (!products.isEmpty()) {
            throw new UnitOfMeasureAssociatedException();
        }
        unitMeasurePersistencePort.deleteByIdAndEnterpriseId(id, enterpriseId);
    }

    /**
     * @brief Valida unicidad del nombre y abreviación en la empresa
     * @param unitOfMeasure unidad de medida a validar
     * @throws UnitOfMeasureNameAlreadyExistsException si nombre ya existe
     * @throws UnitOfMeasureAbbreviationAlreadyExistsException si abreviación ya existe
     */
    private void validateUnitOfMeasureUniqueness(UnitOfMeasure unitOfMeasure) {
        // El nombre y abreviación ya están normalizados, se usan directamente para validación
        if (unitMeasurePersistencePort.existsByNameAndEnterpriseId(
                unitOfMeasure.getName(), unitOfMeasure.getEnterpriseId())) {
            throw new UnitOfMeasureNameAlreadyExistsException(unitOfMeasure.getName());
        }

        if (unitMeasurePersistencePort.existsByAbbreviationAndEnterpriseId(
                unitOfMeasure.getAbbreviation(), unitOfMeasure.getEnterpriseId())) {
            throw new UnitOfMeasureAbbreviationAlreadyExistsException(unitOfMeasure.getAbbreviation());
        }
    }

    /**
     * @brief Valida unicidad durante actualización excluyendo registro actual
     * @param id ID de la unidad que se está actualizando
     * @param unitOfMeasure unidad de medida a validar
     * @throws UnitOfMeasureNameAlreadyExistsException si nombre ya existe en otro registro
     * @throws UnitOfMeasureAbbreviationAlreadyExistsException si abreviación ya existe en otro registro
     */
    private void validateUnitOfMeasureUniquenessForUpdate(Long id, UnitOfMeasure unitOfMeasure) {
        // El nombre y abreviación ya están normalizados, se usan directamente para validación
        if (unitMeasurePersistencePort.existsByNameAndEnterpriseIdAndIdNot(
                unitOfMeasure.getName(), unitOfMeasure.getEnterpriseId(), id)) {
            throw new UnitOfMeasureNameAlreadyExistsException(unitOfMeasure.getName());
        }

        if (unitMeasurePersistencePort.existsByAbbreviationAndEnterpriseIdAndIdNot(
                unitOfMeasure.getAbbreviation(), unitOfMeasure.getEnterpriseId(), id)) {
            throw new UnitOfMeasureAbbreviationAlreadyExistsException(unitOfMeasure.getAbbreviation());
        }
    }

    /**
     * @brief Valida que la unidad de medida no contenga productos que ya han sido usados
     * @param unitOfMeasureId ID de la unidad de medida a validar
     * @throws UnitOfMeasureInUseException si la unidad de medida contiene productos en uso
     */
    private void validateUnitOfMeasureNotInUse(Long unitOfMeasureId) {
        List<Product> products = productServicePort.findAllByUnitOfMeasure(unitOfMeasureId);
        boolean hasProductsInUse = products.stream().anyMatch(Product::isInUse);

        if (hasProductsInUse) {
            throw new UnitOfMeasureInUseException();
        }
    }

    @Override
    public long countAllUnitOfMeasuresByEntId(String enterpriseId) {
        return unitMeasurePersistencePort.countByEnterpriseId(enterpriseId);
    }

 
    @Override
    public Page<UnitOfMeasure> findByEntIdAndSearch(String enterpriseId, String search, int page, int size, String sortField, String sortOrder) {
        return unitMeasurePersistencePort.findByEnterpriseIdAndSearch(enterpriseId, search, page, size, sortField, sortOrder);
    }

  
    @Override
    public long countByEntIdAndSearch(String enterpriseId, String search) {
        return unitMeasurePersistencePort.countByEnterpriseIdAndSearch(enterpriseId, search);
    }

    @Override
    public Page<UnitOfMeasure> getAllUnitOfMeasuresByWithSort(String enterpriseId, int page, int size, String sortField, String sortOrder) {
        return unitMeasurePersistencePort.getAllUnitOfMeasuresByWithSort(enterpriseId, page, size, sortField, sortOrder);
    }

    @Override
    public Page<UnitOfMeasure> getAllActiveUnitOfMeasuresBy(String enterpriseId, int page, int size) {
        return unitMeasurePersistencePort.getActiveUnitOfMeasuresBy(enterpriseId, page, size, "name", "asc");
    }

    @Override
    public long countActiveUnitOfMeasuresByEntId(String enterpriseId) {
        return unitMeasurePersistencePort.countActiveByEnterpriseId(enterpriseId);
    }
}
