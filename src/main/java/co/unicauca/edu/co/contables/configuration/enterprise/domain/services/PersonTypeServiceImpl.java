package co.unicauca.edu.co.contables.configuration.enterprise.domain.services;

import co.unicauca.edu.co.contables.configuration.enterprise.dataAccess.entity.PersonTypeEntity;
import co.unicauca.edu.co.contables.configuration.enterprise.dataAccess.mapper.PersonTypeDataMapper;
import co.unicauca.edu.co.contables.configuration.enterprise.dataAccess.repository.PersonTypeRepository;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.models.PersonType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * @brief Implementación del servicio de dominio para el tipo de persona.
 */
@Service
@RequiredArgsConstructor
public class PersonTypeServiceImpl implements IPersonTypeService {

    private final PersonTypeRepository repository;
    private final PersonTypeDataMapper dataMapper;

    @Override
    public PersonType create(PersonType personType) {
        PersonTypeEntity entity = dataMapper.toEntity(personType);
        if (entity.getId() != null) {
            return null;
        }
        return dataMapper.toDomain(repository.save(entity));
    }

    @Override
    public boolean delete(Long id) {
        if (repository.existsById(id)) {
            repository.deleteById(id);
            return true;
        }
        return false;
    }
}
