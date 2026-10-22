package co.unicauca.edu.co.contables.configuration.enterprise.domain.services;

import co.unicauca.edu.co.contables.configuration.enterprise.domain.models.Location;

public interface ILocationService {
    Location create(Location location);

    boolean delete(Long id);
}
