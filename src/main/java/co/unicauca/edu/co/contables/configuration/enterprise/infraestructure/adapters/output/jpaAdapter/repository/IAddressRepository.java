package co.unicauca.edu.co.contables.configuration.enterprise.infraestructure.adapters.output.jpaAdapter.repository;

import co.unicauca.edu.co.contables.configuration.enterprise.infraestructure.adapters.output.jpaAdapter.entity.CityEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositorio JPA para la entidad CityEntity.
 * Proporciona métodos para realizar operaciones CRUD en la base de datos.
 */
public interface IAddressRepository extends JpaRepository<CityEntity, Long> {

    /**
     * Encuentra una entidad CityEntity por su identificador.
     *
     * @param id el identificador de la ciudad
     * @return la entidad CityEntity correspondiente
     */
    CityEntity findAllById(Long id);
}
