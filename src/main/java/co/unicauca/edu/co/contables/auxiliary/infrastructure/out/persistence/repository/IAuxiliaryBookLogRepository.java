package co.unicauca.edu.co.contables.auxiliary.infrastructure.out.persistence.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import co.unicauca.edu.co.contables.auxiliary.infrastructure.out.persistence.entity.AuxiliaryBookLogEntity;

/**
 * @brief Repositorio JPA para la entidad de log del libro auxiliar.
 *
 * Expone las operaciones CRUD y una búsqueda por el ID público del
 * libro auxiliar, navegando la relación log → auxiliaryBook → publicId.
 */
public interface IAuxiliaryBookLogRepository extends JpaRepository<AuxiliaryBookLogEntity, Long> {
    /**
     * Busca todos los logs de un libro, usando el PUBLIC ID del libro.
     * JPA navegará: log -> auxiliaryBook -> publicId
     *
     * @param bookPublicId El ID público del AuxiliaryBook
     * @return Lista de logs
     */
    List<AuxiliaryBookLogEntity> findByAuxiliaryBookPublicId(String bookPublicId);
}
