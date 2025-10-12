package co.unicauca.edu.co.contables.configuration.helpCenter.domain.services;

import co.unicauca.edu.co.contables.configuration.commons.exceptions.helpCenter.InvalidModuleException;
import co.unicauca.edu.co.contables.configuration.commons.exceptions.helpCenter.HelpCenterAlreadyExistsException;
import co.unicauca.edu.co.contables.configuration.commons.exceptions.helpCenter.HelpCenterNotFoundException;
import co.unicauca.edu.co.contables.configuration.commons.utils.StringStandardizationUtils;
import co.unicauca.edu.co.contables.configuration.helpCenter.dataAccess.entity.HelpCenterEntity;
import co.unicauca.edu.co.contables.configuration.helpCenter.dataAccess.mapper.HelpCenterDataMapper;
import co.unicauca.edu.co.contables.configuration.helpCenter.dataAccess.repository.HelpCenterRepository;
import co.unicauca.edu.co.contables.configuration.helpCenter.domain.mapper.HelpCenterDomainMapper;
import co.unicauca.edu.co.contables.configuration.helpCenter.domain.models.HelpCenter;
import co.unicauca.edu.co.contables.configuration.helpCenter.presentation.DTO.request.HelpCenterCreateReq;
import co.unicauca.edu.co.contables.configuration.helpCenter.presentation.DTO.request.HelpCenterUpdateReq;
import co.unicauca.edu.co.contables.configuration.helpCenter.domain.models.DocumentModule;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HelpCenterServiceImpl implements IHelpCenterService {

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

        // Unicidad por empresa: nombre
        if (repository.existsByNameAndIdEnterprise(standardizedName, request.getIdEnterprise())) {
            throw new HelpCenterAlreadyExistsException(standardizedName, request.getIdEnterprise());
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

        HelpCenterEntity current = repository.findByIdAndIdEnterprise(request.getId(), request.getIdEnterprise())
                .orElseThrow(HelpCenterNotFoundException::new);

        String targetEnterprise = request.getIdEnterprise() != null ? request.getIdEnterprise() : current.getIdEnterprise();
        String standardizedName = StringStandardizationUtils.standardizeName(request.getName());

        boolean nameChanged = standardizedName != null && !standardizedName.equals(current.getName());
        boolean enterpriseChanged = targetEnterprise != null && !targetEnterprise.equals(current.getIdEnterprise());

        // Validar unicidad del nombre si cambió
        if (nameChanged || enterpriseChanged) {
            if (repository.existsByNameAndIdEnterpriseAndIdNot(standardizedName, targetEnterprise, current.getId())) {
                throw new HelpCenterAlreadyExistsException(standardizedName, targetEnterprise);
            }
        }

        // Obtener el módulo y actualizar
        DocumentModule documentModule = DocumentModule.fromId(request.getModuleId());

        current.setIdEnterprise(targetEnterprise);
        current.setName(standardizedName);
        current.setDescription(request.getDescription());
        current.setModule(documentModule);

        HelpCenterEntity saved = repository.save(current);
        return dataMapper.toDomain(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public HelpCenter findById(Long id, String idEnterprise) {
        return dataMapper.toDomain(
            repository.findByIdAndIdEnterprise(id, idEnterprise)
                .orElseThrow(HelpCenterNotFoundException::new)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public Page<HelpCenter> findAllByEnterprise(String idEnterprise, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return repository.findAllByIdEnterprise(idEnterprise, pageable).map(dataMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<HelpCenter> findAllByEnterprise(String idEnterprise, int page, int size, String sortField, String sortOrder) {
        Sort sort = "desc".equalsIgnoreCase(sortOrder) ? 
            Sort.by(sortField).descending() : 
            Sort.by(sortField).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return repository.findAllByIdEnterprise(idEnterprise, pageable).map(dataMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<HelpCenter> findAllByModuleAndEnterprise(Integer moduleId, String idEnterprise) {
        // Validar que el ID del módulo sea válido
        if (!DocumentModule.isValidId(moduleId)) {
            throw new InvalidModuleException(moduleId);
        }
        
        // Obtener el módulo por ID
        DocumentModule documentModule = DocumentModule.fromId(moduleId);
        
        return repository.findAllByModuleAndIdEnterprise(documentModule, idEnterprise)
                .stream()
                .map(dataMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public HelpCenter changeState(Long id, String idEnterprise, Boolean status) {
        HelpCenterEntity current = repository.findByIdAndIdEnterprise(id, idEnterprise)
                .orElseThrow(HelpCenterNotFoundException::new);

        current.setStatus(status);
        HelpCenterEntity saved = repository.save(current);
        return dataMapper.toDomain(saved);
    }

    @Override
    @Transactional
    public HelpCenter delete(Long id, String idEnterprise) {
        HelpCenterEntity current = repository.findByIdAndIdEnterprise(id, idEnterprise)
                .orElseThrow(HelpCenterNotFoundException::new);

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
    public Page<HelpCenter> findByEnterpriseAndNameContaining(String idEnterprise, String search, int page, int size, String sortField, String sortOrder) {
        Sort sort = "desc".equalsIgnoreCase(sortOrder) ? 
            Sort.by(sortField).descending() : 
            Sort.by(sortField).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        // Busca en nombre O descripción usando query personalizada
        return repository.searchByEnterpriseAndText(idEnterprise, search, pageable)
                .map(dataMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public long countByEnterpriseAndNameContaining(String idEnterprise, String search) {
        // Cuenta en nombre O descripción usando query personalizada
        return repository.countByEnterpriseAndText(idEnterprise, search);
    }
}
