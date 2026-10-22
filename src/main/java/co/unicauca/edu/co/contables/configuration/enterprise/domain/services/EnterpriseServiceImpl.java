package co.unicauca.edu.co.contables.configuration.enterprise.domain.services;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.unicauca.edu.co.contables.configuration.enterprise.dataAccess.entity.EnterpriseEntity;
import co.unicauca.edu.co.contables.configuration.enterprise.dataAccess.mapper.EnterpriseDataMapper;
import co.unicauca.edu.co.contables.configuration.enterprise.dataAccess.mapper.EnterpriseTypeDataMapper;
import co.unicauca.edu.co.contables.configuration.enterprise.dataAccess.mapper.LocationDataMapper;
import co.unicauca.edu.co.contables.configuration.enterprise.dataAccess.mapper.PersonTypeDataMapper;
import co.unicauca.edu.co.contables.configuration.enterprise.dataAccess.mapper.TaxLiabilityDataMapper;
import co.unicauca.edu.co.contables.configuration.enterprise.dataAccess.mapper.TaxPayerTypeDataMapper;
import co.unicauca.edu.co.contables.configuration.enterprise.dataAccess.repository.EnterpriseRepository;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.dto.EnterpriseInfoDto;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.enums.InventoryConfigurationTypeEnum;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.enums.StateEnum;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.models.Enterprise;
import lombok.RequiredArgsConstructor;

/**
 * @brief Implementación del servicio de dominio para empresas.
 *
 * Contiene la lógica de negocio de las operaciones CRUD de empresa,
 * incluyendo la creación en cascada de su ubicación y de su tipo de persona
 * (tal como lo orquestaba el controlador en la implementación original), el
 * cambio de estado y la configuración de inventario.
 */
@Service
@RequiredArgsConstructor
public class EnterpriseServiceImpl implements IEnterpriseService {

    private final EnterpriseRepository repository;
    private final EnterpriseDataMapper dataMapper;
    private final LocationDataMapper locationDataMapper;
    private final PersonTypeDataMapper personTypeDataMapper;
    private final TaxLiabilityDataMapper taxLiabilityDataMapper;
    private final TaxPayerTypeDataMapper taxPayerTypeDataMapper;
    private final EnterpriseTypeDataMapper enterpriseTypeDataMapper;
    private final ILocationService locationService;
    private final IPersonTypeService personTypeService;

    @Override
    @Transactional
    public Enterprise create(Enterprise enterprise) {
        enterprise.setLocation(locationService.create(enterprise.getLocation()));
        enterprise.setPersonType(personTypeService.create(enterprise.getPersonType()));

        EnterpriseEntity entity = dataMapper.toEntity(enterprise);
        if (entity == null) {
            return null;
        }
        entity = repository.save(entity);
        return dataMapper.toDomain(entity);
    }

    @Override
    @Transactional
    public void update(UUID id, Enterprise enterprise) {
        EnterpriseEntity current = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Enterprise not found"));

        Long previousLocationId = current.getLocation() != null ? current.getLocation().getId() : null;

        enterprise.setLocation(locationService.create(enterprise.getLocation()));
        enterprise.setPersonType(personTypeService.create(enterprise.getPersonType()));

        current.setName(enterprise.getName());
        current.setNit(enterprise.getNit());
        current.setDV(enterprise.getDV());
        current.setPhone(enterprise.getPhone());
        current.setBranch(enterprise.getBranch());
        current.setEmail(enterprise.getEmail());
        current.setLogo(enterprise.getLogo());
        current.setMainActivity(enterprise.getMainActivity());
        current.setSecondaryActivity(enterprise.getSecondaryActivity());
        current.setTaxLiabilities(taxLiabilityDataMapper.toEntityList(enterprise.getTaxLiabilities()));
        current.setTaxPayerType(taxPayerTypeDataMapper.toEntity(enterprise.getTaxPayerType()));
        current.setEnterpriseType(enterpriseTypeDataMapper.toEntity(enterprise.getEnterpriseType()));
        current.setPersonType(personTypeDataMapper.toEntity(enterprise.getPersonType()));
        current.setLocation(locationDataMapper.toEntity(enterprise.getLocation()));

        repository.save(current);

        if (previousLocationId != null) {
            locationService.delete(previousLocationId);
        }
    }

    @Override
    @Transactional
    public void updateStatus(UUID id, StateEnum state) {
        EnterpriseEntity entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Enterprise not found"));
        entity.setState(state);
        repository.save(entity);
    }

    @Override
    @Transactional
    public void updateInventoryConfiguration(UUID id, InventoryConfigurationTypeEnum inventoryConfigurationType) {
        EnterpriseEntity entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Enterprise not found"));
        entity.setInventoryConfigurationType(inventoryConfigurationType);
        repository.save(entity);
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        if (!repository.existsById(id)) {
            throw new RuntimeException("Enterprise not found");
        }
        repository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Enterprise findById(UUID id) {
        return dataMapper.toDomain(repository.findById(id).orElse(null));
    }

    @Override
    public List<EnterpriseInfoDto> findAllActive() {
        return repository.findEnterpriseInfo().stream()
                .map(p -> EnterpriseInfoDto.builder()
                        .id(p.getId())
                        .name(p.getName())
                        .nit(p.getNit())
                        .logo(p.getLogo())
                        .inventoryConfigurationType(p.getInventoryConfigurationType())
                        .build())
                .toList();
    }

    @Override
    public List<EnterpriseInfoDto> findAllInactive() {
        return repository.findEnterpriseInfoInactive().stream()
                .map(p -> EnterpriseInfoDto.builder()
                        .id(p.getId())
                        .name(p.getName())
                        .nit(p.getNit())
                        .logo(p.getLogo())
                        .inventoryConfigurationType(p.getInventoryConfigurationType())
                        .build())
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<EnterpriseInfoDto> search(String q) {
        return repository.searchByNameOrNit(q).stream()
                .map(e -> EnterpriseInfoDto.builder()
                        .id(e.getId())
                        .name(e.getName())
                        .nit(e.getNit())
                        .logo(e.getLogo())
                        .inventoryConfigurationType(e.getInventoryConfigurationType())
                        .build())
                .toList();
    }
}
