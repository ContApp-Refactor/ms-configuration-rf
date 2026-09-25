package co.unicauca.edu.co.contables.configuration.enterprise.infraestructure.adapters.output.jpaAdapter.repository;

import co.unicauca.edu.co.contables.configuration.enterprise.infraestructure.adapters.output.jpaAdapter.entity.SubjectEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * Repositorio JPA para la entidad {@link SubjectEntity}.
 * 
 * Proporciona operaciones CRUD básicas (crear, leer, actualizar, eliminar)
 * sin necesidad de definir consultas personalizadas.
 * 
 * @author CONTAPP
 * @version 1.0
 * @since 1.0.0
 */
@Repository
public interface ISubjectRepository extends JpaRepository<SubjectEntity, UUID> {

    /**
     * Verifica si ya existe una materia con un código específico.
     *
     * @param code el código único de la materia
     * @return true si existe una materia con el código, false en caso contrario
     */
    boolean existsByCode(String code);
}
