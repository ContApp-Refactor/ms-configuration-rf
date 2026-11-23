package co.unicauca.edu.co.contables.auxiliary.infrastructure.out.persistence.adapter.AuxiliaryBookHistory;

import co.unicauca.edu.co.contables.auxiliary.infrastructure.out.persistence.entity.AuxiliaryBookHistoryEntity;
import co.unicauca.edu.co.contables.auxiliary.infrastructure.out.persistence.mapper.IAuxiliaryBookHistoryCommandEntityMapper;
import co.unicauca.edu.co.contables.auxiliary.infrastructure.out.persistence.repository.IAuxiliaryBookHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import co.unicauca.edu.co.contables.auxiliary.domain.models.history.AuxiliaryBookHistory;
import co.unicauca.edu.co.contables.auxiliary.domain.ports.AuxiliaryBookHistory.IAuxiliaryBookHistoryQueryRepositoryPort;
import org.springframework.stereotype.Component;

/**
 * @brief Adaptador para las operaciones de lectura del historial de libros auxiliares.
 *
 * Implementa {@link IAuxiliaryBookHistoryQueryRepositoryPort}
 * consultando el repositorio JPA y mapeando las entidades a su
 * representación de dominio, con soporte para búsquedas paginadas
 * por empresa y búsqueda puntual por ID interno del libro.
 */
@Component
@RequiredArgsConstructor
public class AuxiliaryBookHistoryQueryAdapter implements IAuxiliaryBookHistoryQueryRepositoryPort {


    private final IAuxiliaryBookHistoryRepository auxiliaryBookHistoryRepository;
    private final IAuxiliaryBookHistoryCommandEntityMapper auxiliaryBookHistoryMapper;

    /**
     * @brief Obtiene una página de registros de historial para una empresa.
     * @param entId Identificador de la empresa cuyo historial se consulta.
     * @param pageable Información de paginación.
     * @return Página de {@link AuxiliaryBookHistory} para la empresa indicada.
     */
    @Override
    public Page<AuxiliaryBookHistory> findPageByEntId(String entId, Pageable pageable) {
        // 1. Llamar al repositorio de JPA
        Page<AuxiliaryBookHistoryEntity> entityPage = this.auxiliaryBookHistoryRepository.findByAuxiliaryBookEntId(entId, pageable);

        return entityPage.map(this.auxiliaryBookHistoryMapper::toDomain);
    }

    /**
     * @brief Busca el historial de un libro por el ID interno del libro.
     * @param bookId El ID (Long) interno del AuxiliaryBook.
     * @return El AuxiliaryBookHistory, o null si no se encuentra.
     */
    @Override
    public AuxiliaryBookHistory findByBookId(Long bookId) {
        // 1. Llamar al repositorio de JPA
        AuxiliaryBookHistoryEntity entity = this.auxiliaryBookHistoryRepository.findByAuxiliaryBookId(bookId);

        // 2. Manejar el caso nulo
        if (entity == null) {
            return null;
        }

        // 3. Mapear de Entidad a Dominio y retornar
        return this.auxiliaryBookHistoryMapper.toDomain(entity);
    }
}
