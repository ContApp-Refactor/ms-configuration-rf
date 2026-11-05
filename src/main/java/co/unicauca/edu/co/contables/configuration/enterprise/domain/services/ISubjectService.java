package co.unicauca.edu.co.contables.configuration.enterprise.domain.services;

import co.unicauca.edu.co.contables.configuration.enterprise.domain.models.Subject;

import java.util.List;

public interface ISubjectService {
    List<Subject> getAll();

    Subject getByCode(String code);

    Subject create(Subject subject);

    Subject update(String code, Subject subject);

    void delete(String code);
}
