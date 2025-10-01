package co.unicauca.edu.co.contables.configuration.typesOfDocuments.domain.services;

import co.unicauca.edu.co.contables.configuration.classesOfDocuments.dataAccess.entity.DocumentClassEntity;
import co.unicauca.edu.co.contables.configuration.classesOfDocuments.dataAccess.repository.DocumentClassRepository;
import co.unicauca.edu.co.contables.configuration.commons.exceptions.documentClasses.DocumentClassesNotFoundException;
import co.unicauca.edu.co.contables.configuration.commons.exceptions.documentClasses.DocumentClassInactiveException;
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
        
        if (!DocumentModule.isValidName(request.getModule())) {
            throw new InvalidModuleException(request.getModule());
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
        domain.setModule(StringStandardizationUtils.standardizeName(request.getModule()));
        DocumentTypeEntity toSave = dataMapper.toEntity(domain);
        toSave.setDocumentClass(docClass);

        DocumentTypeEntity saved = repository.save(toSave);
        return dataMapper.toDomain(saved);
    }

    @Override
    @Transactional
    public DocumentType update(DocumentTypeUpdateReq request) {
        // Validación de módulo usando el ENUM
        if (!DocumentModule.isValidName(request.getModule())) {
            throw new InvalidModuleException(request.getModule());
        }

        DocumentTypeEntity current = repository.findByIdAndIdEnterprise(request.getId(), request.getIdEnterprise())
                .orElseThrow(DocumentTypesNotFoundException::new);

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
        current.setModule(StringStandardizationUtils.standardizeName(request.getModule()));

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
    public Page<DocumentType> findAllByModuleAndEnterprise(Integer moduleId, String idEnterprise, int page, int size) {
        // Validar que el ID del módulo sea válido
        if (!DocumentModule.isValidId(moduleId)) {
            throw new InvalidModuleException(moduleId);
        }
        
        // Obtener el módulo por ID y estandarizar su nombre
        DocumentModule documentModule = DocumentModule.fromId(moduleId);
        String standardizedModule = StringStandardizationUtils.standardizeName(documentModule.getName());
        
        Pageable pageable = PageRequest.of(page, size);
        return repository.findAllByModuleAndIdEnterprise(standardizedModule, idEnterprise, pageable)
                .map(dataMapper::toDomain);
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
    public long countAllByModuleAndEnterprise(Integer moduleId, String idEnterprise) {
        // Validar que el ID del módulo sea válido
        if (!DocumentModule.isValidId(moduleId)) {
            throw new InvalidModuleException(moduleId);
        }
        
        // Obtener el módulo por ID y estandarizar su nombre
        DocumentModule documentModule = DocumentModule.fromId(moduleId);
        String standardizedModule = StringStandardizationUtils.standardizeName(documentModule.getName());
        
        return repository.countByModuleAndIdEnterprise(standardizedModule, idEnterprise);
    }

}
