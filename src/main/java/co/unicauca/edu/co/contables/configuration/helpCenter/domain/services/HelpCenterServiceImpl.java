package co.unicauca.edu.co.contables.configuration.helpCenter.domain.services;

import co.unicauca.edu.co.contables.configuration.commons.exceptions.helpCenter.InvalidModuleException;
import co.unicauca.edu.co.contables.configuration.commons.exceptions.helpCenter.HelpCenterAlreadyExistsException;
import co.unicauca.edu.co.contables.configuration.commons.exceptions.helpCenter.HelpCenterNotFoundException;
import co.unicauca.edu.co.contables.configuration.commons.utils.StringStandardizationUtils;
import co.unicauca.edu.co.contables.configuration.helpCenter.dataAccess.entity.HelpCenterEntity;
import co.unicauca.edu.co.contables.configuration.helpCenter.dataAccess.mapper.HelpCenterDataMapper;
import co.unicauca.edu.co.contables.configuration.helpCenter.dataAccess.repository.HelpCenterRepository;
import co.unicauca.edu.co.contables.configuration.helpCenter.domain.enums.DocumentModule;
import co.unicauca.edu.co.contables.configuration.helpCenter.domain.mapper.HelpCenterDomainMapper;
import co.unicauca.edu.co.contables.configuration.helpCenter.domain.models.HelpCenter;
import co.unicauca.edu.co.contables.configuration.helpCenter.presentation.DTO.request.HelpCenterCreateReq;
import co.unicauca.edu.co.contables.configuration.helpCenter.presentation.DTO.request.HelpCenterUpdateReq;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @brief Implementación del servicio de centro de ayuda
 *
 * Implementación principal del servicio de centro de ayuda que maneja
 * operaciones CRUD, validaciones y lógica de negocio.
 */
@Service
@RequiredArgsConstructor
public class HelpCenterServiceImpl implements IHelpCenterService {

    // Constantes para campos de ordenamiento
    private static final String DEFAULT_SORT_FIELD = "name";
    private static final String SORT_FIELD_MODULE = "module";
    private static final String SORT_FIELD_MODULE_ID = "moduleId";
    private static final String SORT_FIELD_NAME = "name";

    private final HelpCenterRepository repository;
    private final HelpCenterDataMapper dataMapper;
    private final HelpCenterDomainMapper domainMapper;

    @Override
    @Transactional
    public HelpCenter create(HelpCenterCreateReq request) {

        // Validar que el ID del módulo sea válido
        if (!DocumentModule.isValidId(request.getModuleId())) {
            throw new InvalidModuleException(request.getModuleId());
        }

        // Estandarización de nombre
        String standardizedName = StringStandardizationUtils.standardizeName(request.getName());

        // Validar unicidad de nombre
        if (repository.existsByName(standardizedName)) {
            throw new HelpCenterAlreadyExistsException(standardizedName);
        }

        HelpCenter domain = domainMapper.toDomain(request);
        domain.setName(standardizedName);
        domain.setModuleId(request.getModuleId());

        HelpCenterEntity toSave = dataMapper.toEntity(domain);
        HelpCenterEntity saved = repository.save(toSave);

        return dataMapper.toDomain(saved);
    }

    @Override
    @Transactional
    public HelpCenter update(HelpCenterUpdateReq request) {
        // Validar que el ID del módulo sea válido
        if (!DocumentModule.isValidId(request.getModuleId())) {
            throw new InvalidModuleException(request.getModuleId());
        }

        HelpCenterEntity current = repository.findById(request.getId())
                .orElseThrow(HelpCenterNotFoundException::new);

        String standardizedName = StringStandardizationUtils.standardizeName(request.getName());

        // Validar unicidad del nombre si cambió
        if (!standardizedName.equals(current.getName())) {
            if (repository.existsByNameAndIdNot(standardizedName, current.getId())) {
                throw new HelpCenterAlreadyExistsException(standardizedName);
            }
        }

        // Obtener el módulo y actualizar
        DocumentModule documentModule = DocumentModule.fromId(request.getModuleId());

        current.setName(standardizedName);
        current.setDescription(request.getDescription());
        current.setModule(documentModule);
        current.setModuleId(request.getModuleId());

        HelpCenterEntity saved = repository.save(current);
        return dataMapper.toDomain(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public HelpCenter findById(Long id) {
        return dataMapper.toDomain(
                repository.findById(id)
                        .orElseThrow(HelpCenterNotFoundException::new));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<HelpCenter> findAll(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return repository.findAll(pageable).map(dataMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<HelpCenter> findAll(int page, int size, String sortField, String sortOrder) {

        sortField = validateSortField(sortField);
        Sort sort = "desc".equalsIgnoreCase(sortOrder) ? Sort.by(sortField).descending()
                : Sort.by(sortField).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return repository.findAll(pageable).map(dataMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<HelpCenter> findAllByModule(Integer moduleId) {
        // Validar que el ID del módulo sea válido
        if (!DocumentModule.isValidId(moduleId)) {
            throw new InvalidModuleException(moduleId);
        }

        // Obtener el módulo por ID
        DocumentModule documentModule = DocumentModule.fromId(moduleId);

        return repository.findAllByModuleAndStatus(documentModule, true)
                .stream()
                .map(dataMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public HelpCenter changeState(Long id, Boolean status) {
        HelpCenterEntity current = repository.findById(id)
                .orElseThrow(HelpCenterNotFoundException::new);

        current.setStatus(status);
        HelpCenterEntity saved = repository.save(current);
        return dataMapper.toDomain(saved);
    }

    @Override
    @Transactional
    public HelpCenter delete(Long id) {
        HelpCenterEntity current = repository.findById(id)
                .orElseThrow(HelpCenterNotFoundException::new);

        repository.delete(current);
        return dataMapper.toDomain(current);
    }

    @Override
    @Transactional(readOnly = true)
    public long countAll() {
        return repository.count();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<HelpCenter> findByNameContaining(String search, int page, int size, String sortField,
            String sortOrder) {
        // Validar y mapear sortField
        sortField = validateSortField(sortField);
        Sort sort = "desc".equalsIgnoreCase(sortOrder) ? Sort.by(sortField).descending()
                : Sort.by(sortField).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        // Busca en nombre módulo, nombre O descripción usando query personalizada
        return repository.searchByText(search, pageable)
                .map(dataMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public long countByNameContaining(String search) {
        // Cuenta en nombre módulo, nombre O descripción usando query personalizada
        return repository.countByText(search);
    }

    /**
     * @brief Valida que el campo de ordenamiento sea permitido.
     * Campos permitidos: id, module, moduleId, name
     * 
     * @param sortField Campo a validar
     * @return Campo válido o 'name' por defecto
     */
    private String validateSortField(String sortField) {
        if (sortField == null || sortField.trim().isEmpty()) {
            return DEFAULT_SORT_FIELD;
        }
        switch (sortField.toLowerCase()) {
        case SORT_FIELD_MODULE:
            return SORT_FIELD_MODULE;
        case SORT_FIELD_MODULE_ID:
            return SORT_FIELD_MODULE_ID;
        case SORT_FIELD_NAME:
            return SORT_FIELD_NAME;
        default:
            return DEFAULT_SORT_FIELD; // Default
    }
    }
}
