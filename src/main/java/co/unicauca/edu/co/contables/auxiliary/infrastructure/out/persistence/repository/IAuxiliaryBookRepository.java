package co.unicauca.edu.co.contables.auxiliary.infrastructure.out.persistence.repository;

import co.unicauca.edu.co.contables.auxiliary.infrastructure.out.persistence.entity.AuxiliaryBookEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * @brief Repositorio JPA para la entidad de libro auxiliar.
 *
 * Provee las operaciones CRUD de Spring Data y una búsqueda
 * especializada por el identificador público (UUID) del libro.
 */
public interface IAuxiliaryBookRepository extends JpaRepository<AuxiliaryBookEntity, Long> {
    /**
     * Busca una entidad por su ID público (UUID).
     * @param publicId El ID externo (UUID)
     * @return Optional con la entidad si se encuentra.
     */
    Optional<AuxiliaryBookEntity> findByPublicId(String publicId);
}
