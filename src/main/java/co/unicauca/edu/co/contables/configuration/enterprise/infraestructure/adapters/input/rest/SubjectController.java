package co.unicauca.edu.co.contables.configuration.enterprise.infraestructure.adapters.input.rest;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import co.unicauca.edu.co.contables.configuration.enterprise.application.ports.input.ISubjectManagerPort;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.models.Subject;
import co.unicauca.edu.co.contables.configuration.enterprise.infraestructure.adapters.input.rest.data.request.SubjectRequest;
import co.unicauca.edu.co.contables.configuration.enterprise.infraestructure.adapters.input.rest.data.response.SubjectResponse;
import co.unicauca.edu.co.contables.configuration.enterprise.infraestructure.adapters.input.rest.mapper.interfaces.ISubjectRestMapper;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;

/**
 * Controlador REST del CRUD de materias (subjects).
 *
 * <p>Expone {@code /api/subjects} con el contrato que ya consume el
 * frontend: listado y creación admiten barra final ({@code subjects/}),
 * el código es la clave de búsqueda y las respuestas son
 * {@code {code, name}}.
 *
 * @author CONTAPP
 * @version 1.0
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/subjects")
@Tag(name = "Subjects", description = "Gestión de materias")
@AllArgsConstructor
public class SubjectController {

    /**
     * Puerto de entrada con la lógica de negocio de materias.
     */
    private final ISubjectManagerPort subjectManagerPort;

    /**
     * Mapper REST entre DTOs y modelo de dominio.
     */
    private final ISubjectRestMapper subjectRestMapper;

    /**
     * Lista todas las materias.
     *
     * @return 200 con el arreglo de materias
     */
    @Operation(summary = "Obtener todas las materias")
    @GetMapping({"", "/"})
    public ResponseEntity<List<SubjectResponse>> getAllSubjects() {
        return ResponseEntity.ok(
                subjectRestMapper.toResponseList(subjectManagerPort.getAll()));
    }

    /**
     * Consulta una materia por su código.
     *
     * @param code el código único de la materia
     * @return 200 con la materia, o 404 si no existe
     */
    @Operation(summary = "Obtener una materia por código")
    @GetMapping("/{code}")
    public ResponseEntity<SubjectResponse> getSubjectByCode(
            @PathVariable String code) {
        return ResponseEntity.ok(
                subjectRestMapper.toResponse(subjectManagerPort.getByCode(code)));
    }

    /**
     * Crea una materia nueva.
     *
     * @param request cuerpo con code y name (ambos obligatorios)
     * @return 201 con la materia creada; 409 si el código ya existe; 400 si
     *         falta algún campo
     */
    @Operation(summary = "Crear una materia")
    @PostMapping({"", "/"})
    public ResponseEntity<SubjectResponse> createSubject(
            @Valid @RequestBody SubjectRequest request) {
        Subject created = subjectManagerPort.create(
                subjectRestMapper.toDomain(request));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(subjectRestMapper.toResponse(created));
    }

    /**
     * Actualiza una materia existente.
     *
     * @param code    el código de la materia a actualizar
     * @param request cuerpo con los nuevos valores
     * @return 200 con la materia actualizada; 404 si no existe; 409 si el
     *         nuevo código ya pertenece a otra materia
     */
    @Operation(summary = "Actualizar una materia por código")
    @PutMapping("/{code}")
    public ResponseEntity<SubjectResponse> updateSubject(
            @PathVariable String code,
            @Valid @RequestBody SubjectRequest request) {
        Subject updated = subjectManagerPort.update(code,
                subjectRestMapper.toDomain(request));
        return ResponseEntity.ok(subjectRestMapper.toResponse(updated));
    }

    /**
     * Elimina una materia.
     *
     * @param code el código de la materia a eliminar
     * @return 204 sin contenido; 404 si no existe
     */
    @Operation(summary = "Eliminar una materia por código")
    @DeleteMapping("/{code}")
    public ResponseEntity<Void> deleteSubject(@PathVariable String code) {
        subjectManagerPort.delete(code);
        return ResponseEntity.noContent().build();
    }
}
