package co.unicauca.edu.co.contables.configuration.enterprise.application.ports.input;

import java.util.List;

import co.unicauca.edu.co.contables.configuration.enterprise.domain.models.Subject;

/**
 * Puerto de entrada para la gestión de materias (subjects).
 * Define las operaciones disponibles para listar, consultar, crear,
 * actualizar y eliminar materias.
 *
 * @author CONTAPP
 * @version 1.0
 * @since 1.0.0
 */
public interface ISubjectManagerPort {

    /**
     * Recupera el listado completo de materias.
     *
     * @return lista de materias registradas
     */
    List<Subject> getAll();

    /**
     * Consulta una materia por su código.
     *
     * @param code el código único de la materia
     * @return la materia encontrada
     * @throws co.unicauca.edu.co.contables.configuration.enterprise.infraestructure.adapters.input.rest.exception.SubjectNotFoundException
     *         si no existe una materia con ese código
     */
    Subject getByCode(String code);

    /**
     * Crea una materia nueva.
     *
     * @param subject la materia a crear (code y name)
     * @return la materia creada con su identificador
     * @throws co.unicauca.edu.co.contables.configuration.enterprise.infraestructure.adapters.input.rest.exception.SubjectCodeAlreadyExistsException
     *         si ya existe una materia con ese código
     */
    Subject create(Subject subject);

    /**
     * Actualiza el nombre (y opcionalmente el código) de una materia existente.
     *
     * @param code    el código de la materia a actualizar
     * @param subject los nuevos datos de la materia
     * @return la materia actualizada
     * @throws co.unicauca.edu.co.contables.configuration.enterprise.infraestructure.adapters.input.rest.exception.SubjectNotFoundException
     *         si no existe la materia con el código indicado
     * @throws co.unicauca.edu.co.contables.configuration.enterprise.infraestructure.adapters.input.rest.exception.SubjectCodeAlreadyExistsException
     *         si el nuevo código ya pertenece a otra materia
     */
    Subject update(String code, Subject subject);

    /**
     * Elimina una materia por su código.
     *
     * @param code el código de la materia a eliminar
     * @throws co.unicauca.edu.co.contables.configuration.enterprise.infraestructure.adapters.input.rest.exception.SubjectNotFoundException
     *         si no existe la materia con el código indicado
     */
    void delete(String code);
}
