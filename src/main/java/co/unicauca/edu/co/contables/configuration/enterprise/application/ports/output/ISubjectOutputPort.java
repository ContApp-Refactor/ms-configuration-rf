package co.unicauca.edu.co.contables.configuration.enterprise.application.ports.output;

import java.util.List;
import java.util.Optional;

import co.unicauca.edu.co.contables.configuration.enterprise.domain.models.Subject;

/**
 * Puerto de salida para la persistencia de materias (subjects).
 * Define las operaciones necesarias sobre el almacenamiento.
 *
 * @author CONTAPP
 * @version 1.0
 * @since 1.0.0
 */
public interface ISubjectOutputPort {

    /**
     * Recupera todas las materias registradas.
     *
     * @return lista de materias
     */
    List<Subject> findAll();

    /**
     * Busca una materia por su código.
     *
     * @param code el código único de la materia
     * @return la materia encontrada, o vacío si no existe
     */
    Optional<Subject> findByCode(String code);

    /**
     * Indica si ya existe una materia con el código dado.
     *
     * @param code el código a verificar
     * @return true si existe
     */
    boolean existsByCode(String code);

    /**
     * Persiste una materia (crea o actualiza según el id).
     *
     * @param subject la materia a guardar
     * @return la materia persistida
     */
    Subject save(Subject subject);

    /**
     * Elimina una materia existente.
     *
     * @param subject la materia a eliminar (debe tener id)
     */
    void delete(Subject subject);
}
