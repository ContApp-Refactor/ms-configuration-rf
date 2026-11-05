package co.unicauca.edu.co.contables.configuration.enterprise.domain.services;

import java.util.List;

import org.springframework.stereotype.Service;

import co.unicauca.edu.co.contables.configuration.enterprise.dataAccess.mapper.SubjectDataMapper;
import co.unicauca.edu.co.contables.configuration.enterprise.dataAccess.repository.SubjectRepository;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.models.Subject;
import co.unicauca.edu.co.contables.configuration.enterprise.presentation.exception.SubjectCodeAlreadyExistsException;
import co.unicauca.edu.co.contables.configuration.enterprise.presentation.exception.SubjectNotFoundException;

import lombok.RequiredArgsConstructor;

/**
 * @brief Implementación del servicio de dominio para materias (subjects).
 *
 * Contiene la lógica de negocio del CRUD: validación de código duplicado y
 * errores de entidad inexistente.
 */
@Service
@RequiredArgsConstructor
public class SubjectServiceImpl implements ISubjectService {

    private final SubjectRepository repository;
    private final SubjectDataMapper dataMapper;

    @Override
    public List<Subject> getAll() {
        return dataMapper.toDomainList(repository.findAll());
    }

    @Override
    public Subject getByCode(String code) {
        return repository.findByCode(code)
                .map(dataMapper::toDomain)
                .orElseThrow(() -> new SubjectNotFoundException(code));
    }

    @Override
    public Subject create(Subject subject) {
        if (repository.existsByCode(subject.getCode())) {
            throw new SubjectCodeAlreadyExistsException(subject.getCode());
        }
        return dataMapper.toDomain(repository.save(dataMapper.toEntity(subject)));
    }

    @Override
    public Subject update(String code, Subject subject) {
        var existing = repository.findByCode(code)
                .orElseThrow(() -> new SubjectNotFoundException(code));

        String newCode = subject.getCode();
        if (newCode != null && !newCode.equals(code) && repository.existsByCode(newCode)) {
            throw new SubjectCodeAlreadyExistsException(newCode);
        }

        existing.setName(subject.getName());
        if (newCode != null) {
            existing.setCode(newCode);
        }
        return dataMapper.toDomain(repository.save(existing));
    }

    @Override
    public void delete(String code) {
        var existing = repository.findByCode(code)
                .orElseThrow(() -> new SubjectNotFoundException(code));
        repository.delete(existing);
    }
}
