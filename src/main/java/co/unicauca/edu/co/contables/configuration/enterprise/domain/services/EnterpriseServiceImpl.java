package co.unicauca.edu.co.contables.configuration.enterprise.domain.services;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.unicauca.edu.co.contables.configuration.enterprise.dataAccess.entity.EnterpriseEntity;
import co.unicauca.edu.co.contables.configuration.enterprise.dataAccess.entity.SubjectEntity;
import co.unicauca.edu.co.contables.configuration.enterprise.dataAccess.mapper.EnterpriseDataMapper;
import co.unicauca.edu.co.contables.configuration.enterprise.dataAccess.mapper.EnterpriseTypeDataMapper;
import co.unicauca.edu.co.contables.configuration.enterprise.dataAccess.mapper.LocationDataMapper;
import co.unicauca.edu.co.contables.configuration.enterprise.dataAccess.mapper.PersonTypeDataMapper;
import co.unicauca.edu.co.contables.configuration.enterprise.dataAccess.mapper.TaxLiabilityDataMapper;
import co.unicauca.edu.co.contables.configuration.enterprise.dataAccess.mapper.TaxPayerTypeDataMapper;
import co.unicauca.edu.co.contables.configuration.enterprise.dataAccess.repository.EnterpriseRepository;
import co.unicauca.edu.co.contables.configuration.enterprise.dataAccess.repository.SubjectRepository;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.dto.EnterpriseInfoDto;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.enums.InventoryConfigurationTypeEnum;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.enums.StateEnum;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.models.Enterprise;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.models.Subject;
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
    private final SubjectRepository subjectRepository;

    @Override
    @Transactional
    public Enterprise create(Enterprise enterprise) {
        enterprise.setLocation(locationService.create(enterprise.getLocation()));
        enterprise.setPersonType(personTypeService.create(enterprise.getPersonType()));
        if (enterprise.getInventoryConfigurationType() == null) {
            enterprise.setInventoryConfigurationType(InventoryConfigurationTypeEnum.WEIGHTED_AVERAGE);
        }

        EnterpriseEntity entity = dataMapper.toEntity(enterprise);
        if (entity == null) {
            return null;
        }
        // Materias: solo las que existen en el catalogo del tenant actual (ids/codigos desconocidos se ignoran).
        entity.setSubjects(resolveSubjects(enterprise.getSubjects()));
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
        // Logo nuevo gana; logo null = no tocar; quitar solo con removeLogo=true explicito.
        // El archivo en disco NO se borra: la empresa duplicada/restaurada puede referenciar la misma clave.
        if (enterprise.getLogo() != null) {
            current.setLogo(enterprise.getLogo());
        } else if (Boolean.TRUE.equals(enterprise.getRemoveLogo())) {
            current.setLogo(null);
        }
        current.setMainActivity(enterprise.getMainActivity());
        current.setSecondaryActivity(enterprise.getSecondaryActivity());
        current.setTaxLiabilities(taxLiabilityDataMapper.toEntityList(enterprise.getTaxLiabilities()));
        current.setTaxPayerType(taxPayerTypeDataMapper.toEntity(enterprise.getTaxPayerType()));
        current.setEnterpriseType(enterpriseTypeDataMapper.toEntity(enterprise.getEnterpriseType()));
        current.setPersonType(personTypeDataMapper.toEntity(enterprise.getPersonType()));
        current.setLocation(locationDataMapper.toEntity(enterprise.getLocation()));

        // Este update copia los campos uno por uno y se saltaba el método de valoración,
        // así que la empresa nunca guardaba la selección hecha en el formulario
        // (el PUT devolvía 200 y el valor seguía vacío). Solo se pisa cuando viene
        // informado, para no vaciar lo que ya hubiera.
        if (enterprise.getInventoryConfigurationType() != null) {
            current.setInventoryConfigurationType(enterprise.getInventoryConfigurationType());
        }
        // null = no tocar; lista vacia = quitar todas; con elementos = reemplazar el conjunto.
        if (enterprise.getSubjects() != null) {
            current.setSubjects(resolveSubjects(enterprise.getSubjects()));
        }

        repository.save(current);

        if (previousLocationId != null) {
            locationService.delete(previousLocationId);
        }
    }

    /**
     * Resuelve las materias pedidas contra el catalogo del tenant actual (SubjectEntity lleva
     * {@code @TenantId}, asi que las consultas ya filtran por tenant). Cada materia se identifica
     * por id y/o por codigo; las que no existen se omiten en silencio. Sin duplicados.
     */
    private List<SubjectEntity> resolveSubjects(List<Subject> requested) {
        List<SubjectEntity> result = new ArrayList<>();
        if (requested == null || requested.isEmpty()) {
            return result;
        }
        List<java.util.UUID> ids = requested.stream().map(Subject::getId).filter(Objects::nonNull).distinct().toList();
        List<String> codes = requested.stream().map(Subject::getCode).filter(c -> c != null && !c.isBlank())
                .distinct().toList();

        Map<java.util.UUID, SubjectEntity> found = new LinkedHashMap<>();
        if (!ids.isEmpty()) {
            subjectRepository.findAllById(ids).forEach(s -> found.put(s.getId(), s));
        }
        if (!codes.isEmpty()) {
            subjectRepository.findByCodeIn(codes).forEach(s -> found.putIfAbsent(s.getId(), s));
        }
        result.addAll(found.values());
        return result;
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
