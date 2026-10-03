package co.unicauca.edu.co.contables.configuration.enterprise.application.ports.services;

import java.util.List;

import org.springframework.stereotype.Service;

import co.unicauca.edu.co.contables.configuration.enterprise.application.ports.input.ISubjectManagerPort;
import co.unicauca.edu.co.contables.configuration.enterprise.application.ports.output.ISubjectOutputPort;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.models.Subject;
import co.unicauca.edu.co.contables.configuration.enterprise.infraestructure.adapters.input.rest.exception.SubjectCodeAlreadyExistsException;
import co.unicauca.edu.co.contables.configuration.enterprise.infraestructure.adapters.input.rest.exception.SubjectNotFoundException;

import lombok.AllArgsConstructor;

/**
 * Servicio que implementa las operaciones de gestión de materias (subjects).
 * Contiene la lógica de negocio del CRUD: validación de código duplicado,
 * errores de entidad inexistente y orquestación del puerto de salida.
 *
 * @author CONTAPP
 * @version 1.0
 * @since 1.0.0
 */
@Service
@AllArgsConstructor
public class SubjectService implements ISubjectManagerPort {

    /**
     * Puerto de salida para la persistencia de materias.
     */
    private final ISubjectOutputPort subjectOutputPort;

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Subject> getAll() {
        return subjectOutputPort.findAll();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Subject getByCode(String code) {
        return subjectOutputPort.findByCode(code)
                .orElseThrow(() -> new SubjectNotFoundException(code));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Subject create(Subject subject) {
        if (subjectOutputPort.existsByCode(subject.getCode())) {
            throw new SubjectCodeAlreadyExistsException(subject.getCode());
        }
        return subjectOutputPort.save(subject);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Subject update(String code, Subject subject) {
        Subject existing = subjectOutputPort.findByCode(code)
                .orElseThrow(() -> new SubjectNotFoundException(code));

        String newCode = subject.getCode();
        if (newCode != null && !newCode.equals(code)
                && subjectOutputPort.existsByCode(newCode)) {
            throw new SubjectCodeAlreadyExistsException(newCode);
        }

        existing.setName(subject.getName());
        if (newCode != null) {
            existing.setCode(newCode);
        }
        return subjectOutputPort.save(existing);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void delete(String code) {
        Subject existing = subjectOutputPort.findByCode(code)
                .orElseThrow(() -> new SubjectNotFoundException(code));
        subjectOutputPort.delete(existing);
    }
}
