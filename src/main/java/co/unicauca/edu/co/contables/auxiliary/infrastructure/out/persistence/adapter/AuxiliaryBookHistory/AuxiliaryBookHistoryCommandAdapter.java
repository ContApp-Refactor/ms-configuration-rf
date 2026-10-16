package co.unicauca.edu.co.contables.auxiliary.infrastructure.out.persistence.adapter.AuxiliaryBookHistory;

import co.unicauca.edu.co.contables.auxiliary.domain.models.history.AuxiliaryBookHistory;
import co.unicauca.edu.co.contables.auxiliary.domain.ports.AuxiliaryBookHistory.IAuxiliaryBookHistoryCommandRepositoryPort;
import co.unicauca.edu.co.contables.auxiliary.infrastructure.out.persistence.entity.AuxiliaryBookEntity;
import co.unicauca.edu.co.contables.auxiliary.infrastructure.out.persistence.entity.AuxiliaryBookHistoryEntity;
import co.unicauca.edu.co.contables.auxiliary.infrastructure.out.persistence.mapper.IAuxiliaryBookHistoryCommandEntityMapper;
import co.unicauca.edu.co.contables.auxiliary.infrastructure.out.persistence.repository.IAuxiliaryBookHistoryRepository;
import co.unicauca.edu.co.contables.auxiliary.infrastructure.out.persistence.repository.IAuxiliaryBookRepository;
import org.springframework.transaction.annotation.Transactional; // <-- Import corregido
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * @brief Adaptador para las operaciones de escritura del historial de libros auxiliares.
 *
 * Implementa {@link IAuxiliaryBookHistoryCommandRepositoryPort}
 * persistiendo y actualizando el historial. Recupera la entidad
 * {@link AuxiliaryBookEntity} asociada desde el repositorio con
 * {@code findById} para evitar {@code TransientObjectException} al
 * guardar el historial.
 */
@Component
@RequiredArgsConstructor
public class AuxiliaryBookHistoryCommandAdapter implements IAuxiliaryBookHistoryCommandRepositoryPort {

    private final IAuxiliaryBookHistoryCommandEntityMapper auxiliaryBookHistoryCommandEntityMapper;
    private final IAuxiliaryBookHistoryRepository auxiliaryBookHistoryRepository;
    private final IAuxiliaryBookRepository auxiliaryBookRepository;

    /**
     * @brief Persiste un registro de historial de libro auxiliar.
     * @param auxiliaryBookHistory Historial a guardar.
     * @return Historial persistido con los identificadores generados.
     */
    @Override
    @Transactional
    public AuxiliaryBookHistory registerAuxiliaryBookHistory(AuxiliaryBookHistory auxiliaryBookHistory) {
        AuxiliaryBookHistoryEntity abHistoryEntity = this.auxiliaryBookHistoryCommandEntityMapper.toEntity(auxiliaryBookHistory);

        // --- CORRECCIÓN AL TransientObjectException ---
        // Cambiamos getReferenceById (perezoso) por findById (activo).
        // Esto trae la entidad 'Book' real a la sesión de Hibernate.
        if (abHistoryEntity.getAuxiliaryBook() != null && abHistoryEntity.getAuxiliaryBook().getId() != null) {

            AuxiliaryBookEntity managedBook = this.auxiliaryBookRepository.findById(
                    abHistoryEntity.getAuxiliaryBook().getId()
            ).orElseThrow(() -> new jakarta.persistence.EntityNotFoundException(
                    "AuxiliaryBook not found with id: " + abHistoryEntity.getAuxiliaryBook().getId()
            ));

            abHistoryEntity.setAuxiliaryBook(managedBook);
        }

        AuxiliaryBookHistoryEntity savedEntity = this.auxiliaryBookHistoryRepository.save(abHistoryEntity);
        return this.auxiliaryBookHistoryCommandEntityMapper.toDomain(savedEntity);
    }

    /**
     * @brief Actualiza un registro de historial existente.
     * @param auxiliaryBookHistory Historial (con ID) a actualizar.
     * @return Historial actualizado mapeado al dominio.
     */
    @Override
    @Transactional
    // --- CORRECCIÓN DE NOMBRE ---
    // El método debe llamarse 'updateHistory' para coincidir con el puerto
    public AuxiliaryBookHistory updateAuxiliaryBookHistory(AuxiliaryBookHistory auxiliaryBookHistory) {
        AuxiliaryBookHistoryEntity abHistoryEntity = this.auxiliaryBookHistoryCommandEntityMapper.toEntity(auxiliaryBookHistory);

        // --- Aplicar la misma lógica de findById aquí ---
        if (abHistoryEntity.getAuxiliaryBook() != null && abHistoryEntity.getAuxiliaryBook().getId() != null) {

            AuxiliaryBookEntity managedBook = this.auxiliaryBookRepository.findById(
                    abHistoryEntity.getAuxiliaryBook().getId()
            ).orElseThrow(() -> new jakarta.persistence.EntityNotFoundException(
                    "AuxiliaryBook not found with id: " + abHistoryEntity.getAuxiliaryBook().getId()
            ));
            abHistoryEntity.setAuxiliaryBook(managedBook);
        }

        AuxiliaryBookHistoryEntity savedEntity = this.auxiliaryBookHistoryRepository.save(abHistoryEntity);

        return this.auxiliaryBookHistoryCommandEntityMapper.toDomain(savedEntity);
    }
}