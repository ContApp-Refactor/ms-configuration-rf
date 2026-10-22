package co.unicauca.edu.co.contables.configuration.enterprise.presentation.controller;

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

import co.unicauca.edu.co.contables.configuration.enterprise.domain.mapper.SubjectDomainMapper;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.models.Subject;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.services.ISubjectService;
import co.unicauca.edu.co.contables.configuration.enterprise.presentation.DTO.request.SubjectReq;
import co.unicauca.edu.co.contables.configuration.enterprise.presentation.DTO.response.SubjectRes;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * @brief Controlador REST del CRUD de materias (subjects).
 *
 * Expone {@code /api/subjects} con el contrato que consume el frontend:
 * listado y creación admiten barra final, el código es la clave de
 * búsqueda y las respuestas son {@code {code, name}}.
 */
@RestController
@RequestMapping("/api/subjects")
@Tag(name = "Subjects", description = "Gestión de materias")
@RequiredArgsConstructor
public class SubjectController {

    private final ISubjectService service;
    private final SubjectDomainMapper mapper;

    @Operation(summary = "Obtener todas las materias")
    @GetMapping({ "", "/" })
    public ResponseEntity<List<SubjectRes>> getAllSubjects() {
        return ResponseEntity.ok(mapper.toResList(service.getAll()));
    }

    @Operation(summary = "Obtener una materia por código")
    @GetMapping("/{code}")
    public ResponseEntity<SubjectRes> getSubjectByCode(@PathVariable String code) {
        return ResponseEntity.ok(mapper.toRes(service.getByCode(code)));
    }

    @Operation(summary = "Crear una materia")
    @PostMapping({ "", "/" })
    public ResponseEntity<SubjectRes> createSubject(@Valid @RequestBody SubjectReq request) {
        Subject created = service.create(mapper.toDomain(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toRes(created));
    }

    @Operation(summary = "Actualizar una materia por código")
    @PutMapping("/{code}")
    public ResponseEntity<SubjectRes> updateSubject(@PathVariable String code, @Valid @RequestBody SubjectReq request) {
        Subject updated = service.update(code, mapper.toDomain(request));
        return ResponseEntity.ok(mapper.toRes(updated));
    }

    @Operation(summary = "Eliminar una materia por código")
    @DeleteMapping("/{code}")
    public ResponseEntity<Void> deleteSubject(@PathVariable String code) {
        service.delete(code);
        return ResponseEntity.noContent().build();
    }
}
