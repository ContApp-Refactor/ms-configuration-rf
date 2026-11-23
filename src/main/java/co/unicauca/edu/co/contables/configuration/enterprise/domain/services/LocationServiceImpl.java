package co.unicauca.edu.co.contables.configuration.enterprise.domain.services;

import co.unicauca.edu.co.contables.configuration.enterprise.dataAccess.entity.LocationEntity;
import co.unicauca.edu.co.contables.configuration.enterprise.dataAccess.mapper.LocationDataMapper;
import co.unicauca.edu.co.contables.configuration.enterprise.dataAccess.repository.LocationRepository;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.models.Location;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * @brief Implementación del servicio de dominio para ubicaciones geográficas.
 */
@Service
@RequiredArgsConstructor
public class LocationServiceImpl implements ILocationService {

    private final LocationRepository repository;
    private final LocationDataMapper dataMapper;

    @Override
    public Location create(Location location) {
        LocationEntity entity = dataMapper.toEntity(location);
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
