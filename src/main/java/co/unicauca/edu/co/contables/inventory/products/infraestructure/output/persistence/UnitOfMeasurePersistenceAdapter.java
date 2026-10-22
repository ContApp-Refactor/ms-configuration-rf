package co.unicauca.edu.co.contables.inventory.products.infraestructure.output.persistence;

import co.unicauca.edu.co.contables.inventory.products.application.ports.output.IUnitOfMeasurePersistencePort;
import co.unicauca.edu.co.contables.inventory.products.domain.model.UnitOfMeasure;
import co.unicauca.edu.co.contables.inventory.products.infraestructure.output.persistence.entity.UnitOfMeasureEntity;
import co.unicauca.edu.co.contables.inventory.products.infraestructure.output.persistence.mapper.interfaces.IUnitOfMeasurePersistenceMapper;
import co.unicauca.edu.co.contables.inventory.products.infraestructure.output.persistence.repository.IUnitOfMeasureRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import java.util.Optional;

/**
 * @brief Adaptador de persistencia para operaciones CRUD de unidades de medida
 *
 * Implementa IUnitOfMeasurePersistencePort para gestionar persistencia de unidades de medida
 * con soporte para multitenancy por empresa y operaciones paginadas.
 */
@Component
@RequiredArgsConstructor
public class UnitOfMeasurePersistenceAdapter implements IUnitOfMeasurePersistencePort {

    private final IUnitOfMeasureRepository unitOfMeasureRepository;
    private final IUnitOfMeasurePersistenceMapper unitOfMeasurePersistenceMapper;

    @Override
    public Optional<UnitOfMeasure> findByIdAndEnterpriseId(Long id, String enterpriseId) {
        return unitOfMeasureRepository.findByIdAndEnterpriseId(id, enterpriseId)
                .map(unitOfMeasurePersistenceMapper::toUnitOfMeasure);
    }

    @Override
    public UnitOfMeasure create(UnitOfMeasure unitOfMeasure) {
        return unitOfMeasurePersistenceMapper.toUnitOfMeasure(unitOfMeasureRepository.save(unitOfMeasurePersistenceMapper.toUnitOfMeasureEntity(unitOfMeasure)));
    }

    @Override
    public void deleteByIdAndEnterpriseId(Long id, String enterpriseId) {
        unitOfMeasureRepository.findByIdAndEnterpriseId(id, enterpriseId)
                .ifPresent(unitOfMeasureRepository::delete);
    }

    @Override
    public boolean existsByNameAndEnterpriseId(String name, String enterpriseId) {
        return unitOfMeasureRepository.existsByNameAndEnterpriseId(name, enterpriseId);
    }

    @Override
    public boolean existsByAbbreviationAndEnterpriseId(String abbreviation, String enterpriseId) {
        return unitOfMeasureRepository.existsByAbbreviationAndEnterpriseId(abbreviation, enterpriseId);
    }

    @Override
    public boolean existsByNameAndEnterpriseIdAndIdNot(String name, String enterpriseId, Long id) {
        return unitOfMeasureRepository.existsByNameAndEnterpriseIdAndIdNot(name, enterpriseId, id);
    }

    @Override
    public boolean existsByAbbreviationAndEnterpriseIdAndIdNot(String abbreviation, String enterpriseId, Long id) {
        return unitOfMeasureRepository.existsByAbbreviationAndEnterpriseIdAndIdNot(abbreviation, enterpriseId, id);
    }

    @Override
    public Page<UnitOfMeasure> getAllUnitOfMeasuresByState(String enterpriseId, Boolean state, Pageable pageable) {
        Page<UnitOfMeasureEntity> pageEntities = unitOfMeasureRepository.getUnitOfMeasuresByEnterpriseIdAndState(enterpriseId, state, pageable);
        Page<UnitOfMeasure> pageUnitOfMeasures = pageEntities.map(this::convertToUnitOfMeasure);

        return pageUnitOfMeasures;
    }

    @Override
    public Page<UnitOfMeasure> findByEnterpriseIdAndSearch(String enterpriseId, String search, int page, int size, String sortField, String sortOrder) {
        String entitySortField = mapUnitOfMeasureSortField(sortField);
        Sort sort = "desc".equalsIgnoreCase(sortOrder)
            ? Sort.by(entitySortField).descending()
            : Sort.by(entitySortField).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<UnitOfMeasureEntity> pageEntities = unitOfMeasureRepository.findByEnterpriseIdAndSearch(enterpriseId, search, pageable);
        Page<UnitOfMeasure> pageUnitOfMeasures = pageEntities.map(this::convertToUnitOfMeasure);

        return pageUnitOfMeasures;
    }

    @Override
    public long countByEnterpriseIdAndSearch(String enterpriseId, String search) {
        return unitOfMeasureRepository.countByEnterpriseIdAndSearch(enterpriseId, search);
    }

    @Override
    public Page<UnitOfMeasure> getAllUnitOfMeasuresByWithSort(String enterpriseId, int page, int size, String sortField, String sortOrder) {
        String entitySortField = mapUnitOfMeasureSortField(sortField);
        Sort sort = "desc".equalsIgnoreCase(sortOrder)
            ? Sort.by(entitySortField).descending()
            : Sort.by(entitySortField).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<UnitOfMeasureEntity> pageEntities = unitOfMeasureRepository.getUnitOfMeasuresBy(enterpriseId, pageable);
        Page<UnitOfMeasure> pageUnitOfMeasures = pageEntities.map(this::convertToUnitOfMeasure);

        return pageUnitOfMeasures;
    }

    @Override
    public long countByEnterpriseId(String enterpriseId) {
        return unitOfMeasureRepository.countByEnterpriseId(enterpriseId);
    }

    @Override
    public Page<UnitOfMeasure> getActiveUnitOfMeasuresBy(String enterpriseId, int page, int size, String sortField, String sortOrder) {
        String entitySortField = mapUnitOfMeasureSortField(sortField);
        Sort sort = "desc".equalsIgnoreCase(sortOrder)
            ? Sort.by(entitySortField).descending()
            : Sort.by(entitySortField).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<UnitOfMeasureEntity> pageEntities = unitOfMeasureRepository.getActiveUnitOfMeasuresBy(enterpriseId, pageable);
        Page<UnitOfMeasure> pageUnitOfMeasures = pageEntities.map(this::convertToUnitOfMeasure);

        return pageUnitOfMeasures;
    }

    @Override
    public long countActiveByEnterpriseId(String enterpriseId) {
        return unitOfMeasureRepository.countActiveByEnterpriseId(enterpriseId);
    }

    /**
     * @brief Mapea campo de ordenamiento del dominio a campo de entidad
     * @param sortField campo de ordenamiento del dominio
     * @return campo de ordenamiento de la entidad
     */
    private String mapUnitOfMeasureSortField(String sortField) {
        if (sortField == null || sortField.trim().isEmpty()) {
            return "name"; // Default
        }
        switch (sortField.toLowerCase()) {
            case "name":
                return "name";
            case "abbreviation":
                return "abbreviation";
            default:
                return "name"; // Default para cualquier otro campo
        }
    }

    /**
     * @brief Convierte entidad JPA a objeto de dominio UnitOfMeasure
     * @param unitOfMeasureEntity entidad a convertir
     * @return objeto UnitOfMeasure del dominio
     */
    private UnitOfMeasure convertToUnitOfMeasure(UnitOfMeasureEntity unitOfMeasureEntity) {
        return unitOfMeasurePersistenceMapper.toUnitOfMeasure(unitOfMeasureEntity);
    }
}
