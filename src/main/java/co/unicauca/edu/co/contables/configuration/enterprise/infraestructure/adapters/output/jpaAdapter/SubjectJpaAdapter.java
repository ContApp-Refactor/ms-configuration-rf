package co.unicauca.edu.co.contables.configuration.enterprise.infraestructure.adapters.output.jpaAdapter;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import co.unicauca.edu.co.contables.configuration.enterprise.application.ports.output.ISubjectOutputPort;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.models.Subject;
import co.unicauca.edu.co.contables.configuration.enterprise.infraestructure.adapters.output.jpaAdapter.entity.SubjectEntity;
import co.unicauca.edu.co.contables.configuration.enterprise.infraestructure.adapters.output.jpaAdapter.mapper.ISubjectMapper;
import co.unicauca.edu.co.contables.configuration.enterprise.infraestructure.adapters.output.jpaAdapter.repository.ISubjectRepository;

import lombok.AllArgsConstructor;

/**
 * Adaptador de salida que implementa el puerto {@link ISubjectOutputPort}
 * sobre el repositorio JPA de materias. Traduce entre la entidad JPA
 * {@link SubjectEntity} y el modelo de dominio {@link Subject}.
 *
 * @author CONTAPP
 * @version 1.0
 * @since 1.0.0
 */
@Component
@AllArgsConstructor
public class SubjectJpaAdapter implements ISubjectOutputPort {

    /**
     * Repositorio JPA de materias.
     */
    private final ISubjectRepository subjectRepository;

    /**
     * Mapper entre entidad JPA y modelo de dominio.
     */
    private final ISubjectMapper subjectMapper;

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Subject> findAll() {
        return subjectMapper.toDomainList(subjectRepository.findAll());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Subject> findByCode(String code) {
        Optional<SubjectEntity> entity = subjectRepository.findByCode(code);
        return entity.map(subjectMapper::toDomain);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean existsByCode(String code) {
        return subjectRepository.existsByCode(code);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Subject save(Subject subject) {
        SubjectEntity entity = subjectMapper.toEntity(subject);
        SubjectEntity saved = subjectRepository.save(entity);
        return subjectMapper.toDomain(saved);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void delete(Subject subject) {
        subjectRepository.findById(subject.getId())
                .ifPresent(subjectRepository::delete);
    }
}
