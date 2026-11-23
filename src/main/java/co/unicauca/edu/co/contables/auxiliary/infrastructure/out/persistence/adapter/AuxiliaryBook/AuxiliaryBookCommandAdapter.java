package co.unicauca.edu.co.contables.auxiliary.infrastructure.out.persistence.adapter.AuxiliaryBook;

import co.unicauca.edu.co.contables.auxiliary.domain.models.core.AuxiliaryBook;
import co.unicauca.edu.co.contables.auxiliary.domain.ports.AuxiliaryBook.IAuxiliaryBookCommandRepositoryPort;
import co.unicauca.edu.co.contables.auxiliary.infrastructure.out.persistence.entity.AuxiliaryBookEntity;
import co.unicauca.edu.co.contables.auxiliary.infrastructure.out.persistence.mapper.IAuxiliaryBookCommandEntityMapper;
import co.unicauca.edu.co.contables.auxiliary.infrastructure.out.persistence.mapper.IAuxiliaryBookCriteriaCommandEntityMapper;
import co.unicauca.edu.co.contables.auxiliary.infrastructure.out.persistence.repository.IAuxiliaryBookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * @brief Adaptador para las operaciones de escritura del libro auxiliar.
 *
 * Implementa {@link IAuxiliaryBookCommandRepositoryPort} persistiendo
 * los libros auxiliares en el almacén subyacente. Transforma el
 * dominio en entidad JPA y asegura el mapeo explícito del agregado
 * de criterios antes de guardar.
 */
@Component
@RequiredArgsConstructor
public class AuxiliaryBookCommandAdapter implements IAuxiliaryBookCommandRepositoryPort {

    private final IAuxiliaryBookCommandEntityMapper auxiliaryBookCommandEntityMapper;
    private final IAuxiliaryBookCriteriaCommandEntityMapper auxiliaryBookCriteriaCommandEntityMapper;
    private final IAuxiliaryBookRepository auxiliaryBookRepository;

    /**
     * @brief Persiste un libro auxiliar.
     * @param auxiliaryBook Libro auxiliar a guardar.
     * @return Libro auxiliar persistido con los identificadores generados.
     */
    @Override
    public AuxiliaryBook registerAuxiliaryBook(AuxiliaryBook auxiliaryBook) {

        AuxiliaryBookEntity abEntity = this.auxiliaryBookCommandEntityMapper.toEntity(auxiliaryBook);
        abEntity.setCriteria(this.auxiliaryBookCriteriaCommandEntityMapper.toCriteriaEntity(auxiliaryBook.getCriteria()));
        return this.auxiliaryBookCommandEntityMapper.toDomain(this.auxiliaryBookRepository.save(abEntity));
    }
}
