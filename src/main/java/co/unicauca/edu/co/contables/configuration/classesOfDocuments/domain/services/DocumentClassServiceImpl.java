package co.unicauca.edu.co.contables.configuration.classesOfDocuments.domain.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import co.unicauca.edu.co.contables.configuration.classesOfDocuments.dataAccess.entity.DocumentClassEntity;
import co.unicauca.edu.co.contables.configuration.classesOfDocuments.dataAccess.mapper.DocumentClassDataMapper;
import co.unicauca.edu.co.contables.configuration.classesOfDocuments.dataAccess.repository.DocumentClassRepository;
import co.unicauca.edu.co.contables.configuration.classesOfDocuments.domain.mapper.DocumentClassDomainMapper;
import co.unicauca.edu.co.contables.configuration.classesOfDocuments.domain.models.DocumentClass;
import co.unicauca.edu.co.contables.configuration.classesOfDocuments.presentation.DTO.request.DocumentClassCreateReq;
import co.unicauca.edu.co.contables.configuration.classesOfDocuments.presentation.DTO.request.DocumentClassUpdateReq;
import co.unicauca.edu.co.contables.configuration.commons.exceptions.documentClasses.DocumentClassesAlreadyExistsException;
import co.unicauca.edu.co.contables.configuration.commons.exceptions.documentClasses.DocumentClassesNotFoundException;
import co.unicauca.edu.co.contables.configuration.commons.audit.annotation.Auditable;
import co.unicauca.edu.co.contables.configuration.commons.audit.annotation.OperationType;
import co.unicauca.edu.co.contables.configuration.commons.exceptions.documentClasses.DocumentClassInUseException;
import co.unicauca.edu.co.contables.configuration.commons.utils.StringStandardizationUtils;
import co.unicauca.edu.co.contables.configuration.typesOfDocuments.dataAccess.repository.DocumentTypeRepository;
import lombok.RequiredArgsConstructor;

/**
 * @brief Implementación del servicio de dominio para clases de documento
 *
 *        Clase que implementa la lógica de negocio para gestionar clases de
 *        documento,
 *        incluyendo operaciones CRUD, validaciones de unicidad, cambios de
 *        estado
 *        y consultas paginadas con filtros.
 */
@Service
@RequiredArgsConstructor
public class DocumentClassServiceImpl implements IDocumentClassService {

    private final DocumentClassRepository repository;
    private final DocumentClassDataMapper dataMapper;
    private final DocumentClassDomainMapper domainMapper;
    private final DocumentTypeRepository documentTypeRepository;

    @Override
    @Transactional
    @Auditable(operationType = OperationType.CREATE, affectedTable = "DOCUMENT_CLASS", moduleName = "CLASSES_OF_DOCUMENTS")
    public DocumentClass create(DocumentClassCreateReq request) {
        String standardizedName = StringStandardizationUtils.standardizeName(request.getName());

        // Validar unicidad del nombre por empresa
        if (repository.existsByNameAndIdEnterprise(standardizedName, request.getIdEnterprise())) {
            throw new DocumentClassesAlreadyExistsException(standardizedName, request.getIdEnterprise());
        }
        DocumentClass domain = domainMapper.toDomain(request);
        domain.setName(standardizedName);
        DocumentClassEntity saved = repository.save(dataMapper.toEntity(domain));
        return dataMapper.toDomain(saved);
    }

    @Override
    @Transactional
    @Auditable(operationType = OperationType.UPDATE, affectedTable = "DOCUMENT_CLASS", moduleName = "CLASSES_OF_DOCUMENTS")
    public DocumentClass update(DocumentClassUpdateReq request) {
        DocumentClassEntity current = repository.findByIdAndIdEnterprise(request.getId(), request.getIdEnterprise())
                .orElseThrow(DocumentClassesNotFoundException::new);

        // Validar que no se pueda editar si tiene tipos de documento con registros
        // contables
        boolean hasDocumentTypesWithMovements = documentTypeRepository
                .existsByDocumentClassIdAndIdEnterpriseAndUsageCountGreaterThan(
                        current.getId(), current.getIdEnterprise(), 0);
        if (hasDocumentTypesWithMovements) {
            throw new DocumentClassInUseException(current.getName(), true); // true = edición
        }

        String targetEnterprise = request.getIdEnterprise() != null ? request.getIdEnterprise()
                : current.getIdEnterprise();

        String standardizedName = StringStandardizationUtils.standardizeName(request.getName());

        boolean nameChanged = standardizedName != null && !standardizedName.equals(current.getName());
        boolean enterpriseChanged = targetEnterprise != null && !targetEnterprise.equals(current.getIdEnterprise());

        // Validar unicidad del nombre si cambió
        if (nameChanged || enterpriseChanged) {
            if (repository.existsByNameAndIdEnterpriseAndIdNot(standardizedName, targetEnterprise, current.getId())) {
                throw new DocumentClassesAlreadyExistsException(standardizedName, targetEnterprise);
            }
        }

        current.setName(standardizedName);
        current.setIdEnterprise(targetEnterprise);

        DocumentClassEntity saved = repository.save(current);
        return dataMapper.toDomain(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public DocumentClass findById(Long id, String idEnterprise) {
        return dataMapper.toDomain(repository.findByIdAndIdEnterprise(id, idEnterprise)
                .orElseThrow(DocumentClassesNotFoundException::new));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DocumentClass> findAllByEnterprise(String idEnterprise, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return repository.findAllByIdEnterprise(idEnterprise, pageable).map(dataMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DocumentClass> findAllByEnterprise(String idEnterprise, int page, int size, String sortField,
            String sortOrder) {
        Sort sort = "desc".equalsIgnoreCase(sortOrder) ? Sort.by(sortField).descending()
                : Sort.by(sortField).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return repository.findAllByIdEnterprise(idEnterprise, pageable).map(dataMapper::toDomain);
    }

    @Override
    public Page<DocumentClass> findAllByEnterpriseAndStatus(String idEnterprise, Boolean status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return repository.findAllByIdEnterpriseAndStatus(idEnterprise, status, pageable).map(dataMapper::toDomain);
    }

    @Override
    @Transactional
    @Auditable(operationType = OperationType.INACTIVATE, affectedTable = "DOCUMENT_CLASS", moduleName = "CLASSES_OF_DOCUMENTS", idArgIndex = 0, enterpriseIdArgIndex = 1)
    public DocumentClass changeState(Long id, String idEnterprise, Boolean status) {
        DocumentClassEntity current = repository.findByIdAndIdEnterprise(id, idEnterprise)
                .orElseThrow(DocumentClassesNotFoundException::new);

        current.setStatus(status);
        DocumentClassEntity saved = repository.save(current);
        return dataMapper.toDomain(saved);
    }

    @Override
    @Transactional
    @Auditable(operationType = OperationType.DELETE, affectedTable = "DOCUMENT_CLASS", moduleName = "CLASSES_OF_DOCUMENTS", idArgIndex = 0, enterpriseIdArgIndex = 1)
    public DocumentClass Delete(Long id, String idEnterprise) {
        DocumentClassEntity current = repository.findByIdAndIdEnterprise(id, idEnterprise)
                .orElseThrow(DocumentClassesNotFoundException::new);

        // Validar que la clase de documento no esté siendo utilizada por tipos de
        // documentos
        if (documentTypeRepository.existsByDocumentClassId(id)) {
            throw new DocumentClassInUseException(current.getName());
        }

        repository.delete(current);
        return dataMapper.toDomain(current);
    }

    @Override
    @Transactional(readOnly = true)
    public long countAllByEnterprise(String idEnterprise) {
        return repository.countByIdEnterprise(idEnterprise);
    }

    @Override
    @Transactional(readOnly = true)
    public long countAllByEnterpriseAndStatus(String idEnterprise, Boolean status) {
        return repository.countByIdEnterpriseAndStatus(idEnterprise, status);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DocumentClass> findByEnterpriseAndNameContaining(String idEnterprise, String search, int page, int size,
            String sortField, String sortOrder) {
        Sort sort = "desc".equalsIgnoreCase(sortOrder) ? Sort.by(sortField).descending()
                : Sort.by(sortField).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return repository.findByIdEnterpriseAndNameContainingIgnoreCase(idEnterprise, search, pageable)
                .map(dataMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public long countByEnterpriseAndNameContaining(String idEnterprise, String search) {
        return repository.countByIdEnterpriseAndNameContainingIgnoreCase(idEnterprise, search);
    }

}
