package co.unicauca.edu.co.contables.configuration.typesOfDocuments.domain.services;

import co.unicauca.edu.co.contables.configuration.classesOfDocuments.dataAccess.entity.DocumentClassEntity;
import co.unicauca.edu.co.contables.configuration.classesOfDocuments.dataAccess.repository.DocumentClassRepository;
import co.unicauca.edu.co.contables.configuration.commons.exceptions.documentClasses.DocumentClassesNotFoundException;
import co.unicauca.edu.co.contables.configuration.commons.exceptions.documentClasses.DocumentClassInactiveException;
import co.unicauca.edu.co.contables.configuration.commons.exceptions.documentTypes.DocumentTypeInUseException;
import co.unicauca.edu.co.contables.configuration.commons.exceptions.documentTypes.DocumentTypesAlreadyExistsException;
import co.unicauca.edu.co.contables.configuration.commons.exceptions.documentTypes.DocumentTypesNotFoundException;
import co.unicauca.edu.co.contables.configuration.commons.exceptions.documentTypes.InvalidModuleException;
import co.unicauca.edu.co.contables.configuration.commons.utils.StringStandardizationUtils;
import co.unicauca.edu.co.contables.configuration.typesOfDocuments.dataAccess.entity.DocumentTypeEntity;
import co.unicauca.edu.co.contables.configuration.typesOfDocuments.dataAccess.mapper.DocumentTypeDataMapper;
import co.unicauca.edu.co.contables.configuration.typesOfDocuments.dataAccess.repository.DocumentTypeRepository;
import co.unicauca.edu.co.contables.configuration.typesOfDocuments.domain.mapper.DocumentTypeDomainMapper;
import co.unicauca.edu.co.contables.configuration.typesOfDocuments.domain.models.DocumentModule;
import co.unicauca.edu.co.contables.configuration.typesOfDocuments.domain.models.DocumentType;
import co.unicauca.edu.co.contables.configuration.typesOfDocuments.presentation.DTO.request.DocumentTypeCreateReq;
import co.unicauca.edu.co.contables.configuration.typesOfDocuments.presentation.DTO.request.DocumentTypeUpdateReq;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/** @brief Implementación del servicio de tipos de documentos */
@Service
@RequiredArgsConstructor
public class DocumentTypeServiceImpl implements IDocumentTypeService {

    private final DocumentTypeRepository repository;
    private final DocumentTypeDataMapper dataMapper;
    private final DocumentTypeDomainMapper domainMapper;
    private final DocumentClassRepository documentClassRepository;

    @Override
    @Transactional
    public DocumentType create(DocumentTypeCreateReq request) {
        
        // Validar que el ID del módulo sea válido
        if (!DocumentModule.isValidId(request.getModuleId())) {
            throw new InvalidModuleException(request.getModuleId());
        }

        // Estandarización de nombre y prefijo
        String standardizedName = StringStandardizationUtils.standardizeName(request.getName());
        String standardizedPrefix = StringStandardizationUtils.standardizePrefix(request.getPrefix());

        // Unicidad por empresa: prefijo y nombre
        if (repository.existsByPrefixAndIdEnterprise(standardizedPrefix, request.getIdEnterprise())) {
            throw new DocumentTypesAlreadyExistsException("prefijo", standardizedPrefix, request.getIdEnterprise());
        }
        if (repository.existsByNameAndIdEnterprise(standardizedName, request.getIdEnterprise())) {
            throw new DocumentTypesAlreadyExistsException("nombre", standardizedName, request.getIdEnterprise());
        }

        // Validar que la clase exista y esté activa
        DocumentClassEntity docClass = documentClassRepository.findByIdAndIdEnterpriseAndStatus(request.getDocumentClassId(), request.getIdEnterprise(), true)
                .orElseGet(() -> {
                    // Verificar si existe pero está inactiva
                    DocumentClassEntity inactiveClass = documentClassRepository.findByIdAndIdEnterprise(request.getDocumentClassId(), request.getIdEnterprise())
                            .orElseThrow(DocumentClassesNotFoundException::new);
                    throw new DocumentClassInactiveException(inactiveClass.getName());
                });

        DocumentType domain = domainMapper.toDomain(request);
        domain.setName(standardizedName);
        domain.setPrefix(standardizedPrefix);
        domain.setModuleId(request.getModuleId());
        DocumentTypeEntity toSave = dataMapper.toEntity(domain);
        toSave.setDocumentClass(docClass);

        DocumentTypeEntity saved = repository.save(toSave);
        return dataMapper.toDomain(saved);
    }

    @Override
    @Transactional
    public DocumentType update(DocumentTypeUpdateReq request) {
        // Validar que el ID del módulo sea válido
        if (!DocumentModule.isValidId(request.getModuleId())) {
            throw new InvalidModuleException(request.getModuleId());
        }

        // Obtener el módulo por ID y estandarizar su nombre
        DocumentModule documentModule = DocumentModule.fromId(request.getModuleId());
        String standardizedModule = StringStandardizationUtils.standardizeName(documentModule.getName());

        DocumentTypeEntity current = repository.findByIdAndIdEnterprise(request.getId(), request.getIdEnterprise())
                .orElseThrow(DocumentTypesNotFoundException::new);

        // Validar que el tipo de documento no tenga movimientos contables registrados
        DocumentType existingDocumentType = dataMapper.toDomain(current);
        if (existingDocumentType.isInUse()) {
            throw new DocumentTypeInUseException(current.getPrefix(), true); // true indica operación de edición
        }

        String targetEnterprise = request.getIdEnterprise() != null ? request.getIdEnterprise() : current.getIdEnterprise();
        String standardizedName = StringStandardizationUtils.standardizeName(request.getName());
        String standardizedPrefix = StringStandardizationUtils.standardizePrefix(request.getPrefix());

        boolean prefixChanged = standardizedPrefix != null && !standardizedPrefix.equals(current.getPrefix());
        boolean nameChanged = standardizedName != null && !standardizedName.equals(current.getName());
        boolean enterpriseChanged = targetEnterprise != null && !targetEnterprise.equals(current.getIdEnterprise());

        // Validar unicidad del prefijo si cambió
        if (prefixChanged || enterpriseChanged) {
            if (repository.existsByPrefixAndIdEnterpriseAndIdNot(standardizedPrefix, targetEnterprise, current.getId())) {
                throw new DocumentTypesAlreadyExistsException("prefijo", standardizedPrefix, targetEnterprise);
            }
        }
        // Validar unicidad del nombre si cambió
        if (nameChanged || enterpriseChanged) {
            if (repository.existsByNameAndIdEnterpriseAndIdNot(standardizedName, targetEnterprise, current.getId())) {
                throw new DocumentTypesAlreadyExistsException("nombre", standardizedName, targetEnterprise);
            }
        }

        // Validar que la clase exista y esté activa
        DocumentClassEntity docClass = documentClassRepository.findByIdAndIdEnterpriseAndStatus(request.getDocumentClassId(), targetEnterprise, true)
                .orElseGet(() -> {
                    // Verificar si existe pero está inactiva
                    DocumentClassEntity inactiveClass = documentClassRepository.findByIdAndIdEnterprise(request.getDocumentClassId(), targetEnterprise)
                            .orElseThrow(DocumentClassesNotFoundException::new);
                    throw new DocumentClassInactiveException(inactiveClass.getName());
                });

        current.setIdEnterprise(targetEnterprise);
        current.setPrefix(standardizedPrefix);
        current.setName(standardizedName);
        current.setDocumentClass(docClass);
        current.setModule(standardizedModule);

        DocumentTypeEntity saved = repository.save(current);
        return dataMapper.toDomain(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public DocumentType findById(Long id, String idEnterprise) {
        return dataMapper.toDomain(repository.findByIdAndIdEnterprise(id, idEnterprise).orElseThrow(DocumentTypesNotFoundException::new));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DocumentType> findAllByEnterprise(String idEnterprise, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return repository.findAllByIdEnterprise(idEnterprise, pageable).map(dataMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DocumentType> findAllByEnterprise(String idEnterprise, int page, int size, String sortField, String sortOrder) {
        Sort sort = "desc".equalsIgnoreCase(sortOrder) ? 
            Sort.by(sortField).descending() : 
            Sort.by(sortField).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return repository.findAllByIdEnterprise(idEnterprise, pageable).map(dataMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DocumentType> findAllByModuleAndEnterprise(Integer moduleId, String idEnterprise) {
        // Validar que el ID del módulo sea válido
        if (!DocumentModule.isValidId(moduleId)) {
            throw new InvalidModuleException(moduleId);
        }
        
        // Obtener el módulo por ID y estandarizar su nombre
        DocumentModule documentModule = DocumentModule.fromId(moduleId);
        String standardizedModule = StringStandardizationUtils.standardizeName(documentModule.getName());
        
        return repository.findAllByModuleAndIdEnterprise(standardizedModule, idEnterprise)
                .stream()
                .map(dataMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public DocumentType changeState(Long id, String idEnterprise, Boolean status) {
        DocumentTypeEntity current = repository.findByIdAndIdEnterprise(id, idEnterprise)
                .orElseThrow(DocumentTypesNotFoundException::new);

        current.setStatus(status);
        DocumentTypeEntity saved = repository.save(current);
        return dataMapper.toDomain(saved);
    }

    @Override
    @Transactional
    public DocumentType Delete(Long id, String idEnterprise) {
        DocumentTypeEntity current = repository.findByIdAndIdEnterprise(id, idEnterprise)
                .orElseThrow(DocumentTypesNotFoundException::new);

        // Validar que el tipo de documento no tenga movimientos contables registrados
        DocumentType existingDocumentType = dataMapper.toDomain(current);
        if (existingDocumentType.isInUse()) {
            throw new DocumentTypeInUseException(current.getPrefix(), false); // false indica operación de eliminación
        }

        repository.delete(current);
        return dataMapper.toDomain(current);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DocumentModule> getAllModules() {
        return Arrays.asList(DocumentModule.values());
    }

    @Override
    @Transactional(readOnly = true)
    public long countAllByEnterprise(String idEnterprise) {
        return repository.countByIdEnterprise(idEnterprise);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DocumentType> findByEnterpriseAndNameContaining(String idEnterprise, String search, int page, int size, String sortField, String sortOrder) {
        Sort sort = "desc".equalsIgnoreCase(sortOrder) ? 
            Sort.by(sortField).descending() : 
            Sort.by(sortField).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return repository.findByIdEnterpriseAndNameContainingIgnoreCase(idEnterprise, search, pageable).map(dataMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public long countByEnterpriseAndNameContaining(String idEnterprise, String search) {
        return repository.countByIdEnterpriseAndNameContainingIgnoreCase(idEnterprise, search);
    }

    @Override
    public void updateUsageCount(Long id, String enterpriseId, Integer usageCount) {
        DocumentTypeEntity entity = repository.findByIdAndIdEnterprise(id, enterpriseId)
                .orElseThrow(() -> new RuntimeException("Tipo de documento no encontrado con ID: " + id));

        entity.setUsageCount(usageCount);
        repository.save(entity);
    }

}
