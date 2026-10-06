package co.unicauca.edu.co.contables.auxiliary.infrastructure.out.persistence.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import co.unicauca.edu.co.contables.auxiliary.infrastructure.out.persistence.entity.AuxiliaryBookHistoryEntity;

/**
 * @brief Repositorio JPA para la entidad de historial del libro auxiliar.
 *
 * Además de las operaciones CRUD estándar, expone búsquedas por ID
 * público del historial, por {@code entId} del libro asociado
 * (paginada) y por ID interno del libro auxiliar.
 */
public interface IAuxiliaryBookHistoryRepository extends JpaRepository<AuxiliaryBookHistoryEntity, Long> {
    /**
     * Busca una entrada de historial por su ID público (UUID).
     */
    Optional<AuxiliaryBookHistoryEntity> findByPublicId(String publicId);

    /**
     * Busca todas las entradas de historial que pertenecen a un AuxiliaryBook
     * basándose en el 'entId' de dicho libro.
     *
     * @param entId El entId (String) del AuxiliaryBookEntity asociado.
     * @param pageable La información de paginación.
     * @return Una página de AuxiliaryBookHistoryEntity.
     */
    Page<AuxiliaryBookHistoryEntity> findByAuxiliaryBookEntId(String entId, Pageable pageable);

    /**
     * Busca la entrada de historial asociada a un AuxiliaryBook por su ID interno.
     * Asume una relación OneToOne (un libro solo tiene un historial "actual").
     *
     * @param auxiliaryBookId El ID (Long) interno del AuxiliaryBookEntity.
     * @return La entidad de historial, o null si no se encuentra.
     */
    AuxiliaryBookHistoryEntity findByAuxiliaryBookId(Long auxiliaryBookId);
}
