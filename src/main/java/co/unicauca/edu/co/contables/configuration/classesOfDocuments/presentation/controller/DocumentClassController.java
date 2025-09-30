package co.unicauca.edu.co.contables.configuration.classesOfDocuments.presentation.controller;

import co.unicauca.edu.co.contables.configuration.commons.utils.PaginationHelper;
import co.unicauca.edu.co.contables.configuration.classesOfDocuments.domain.models.DocumentClass;
import co.unicauca.edu.co.contables.configuration.classesOfDocuments.domain.services.IDocumentClassService;
import co.unicauca.edu.co.contables.configuration.classesOfDocuments.domain.mapper.DocumentClassDomainMapper;
import co.unicauca.edu.co.contables.configuration.classesOfDocuments.presentation.DTO.request.DocumentClassCreateReq;
import co.unicauca.edu.co.contables.configuration.classesOfDocuments.presentation.DTO.request.DocumentClassUpdateReq;
import co.unicauca.edu.co.contables.configuration.classesOfDocuments.presentation.DTO.response.DocumentClassRes;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;


@RestController
@RequestMapping("/api/config/document-classes")
@RequiredArgsConstructor
public class DocumentClassController {

    private final IDocumentClassService service;
    private final DocumentClassDomainMapper mapper;
    private final PaginationHelper paginationHelper;

    @PostMapping("/create")
    public ResponseEntity<DocumentClassRes> create(@Valid @RequestBody DocumentClassCreateReq request) {
        DocumentClass created = service.create(request);
        return ResponseEntity.ok(mapper.toRes(created));
    }

    @PutMapping("/update")
    public ResponseEntity<DocumentClassRes> update(@Valid @RequestBody DocumentClassUpdateReq request) {
        DocumentClass updated = service.update(request);
        return ResponseEntity.ok(mapper.toRes(updated));
    }

    @GetMapping("/findById/{id}/{enterpriseId}")
    public ResponseEntity<DocumentClassRes> getById(@PathVariable Long id, @PathVariable String enterpriseId) {
        return ResponseEntity.ok(mapper.toRes(service.findById(id, enterpriseId)));
    }

    /**
     * Obtiene clases de documento con paginación flexible.
     * Si no se especifican parámetros de paginación, retorna todas las clases de documento.
     * 
     * @param enterpriseId ID de la empresa
     * @param page         Número de página (opcional)
     * @param size         Tamaño de página (opcional)
     * @param sortField    Campo de ordenamiento (opcional)
     * @param sortOrder    Orden (asc/desc) (opcional)
     * @return Página de clases de documento
     */
    @GetMapping("/findAll/{enterpriseId}")
    public ResponseEntity<Page<DocumentClassRes>> list(
            @PathVariable("enterpriseId") String enterpriseId,
            @RequestParam(required = false) Optional<Integer> page,
            @RequestParam(required = false) Optional<Integer> size,
            @RequestParam(defaultValue = "name") String sortField,
            @RequestParam(defaultValue = "asc") String sortOrder) {

        // Contar total de registros
        long totalRecords = service.countAllByEnterprise(enterpriseId);

        // Crear Pageable flexible
        Pageable pageable = paginationHelper.createFlexiblePageable(page, size, totalRecords);

        // Obtener página de datos
        Page<DocumentClass> pageResult = service.findAllByEnterprise(enterpriseId, pageable.getPageNumber(),
                pageable.getPageSize(), sortField, sortOrder);
        return ResponseEntity.ok(pageResult.map(mapper::toRes));
    }   

    /**
     * Obtiene clases de documento activas con paginación flexible.
     * Si no se especifican parámetros de paginación, retorna todas las clases de documento activas.
     * 
     * @param enterpriseId ID de la empresa
     * @param page         Número de página (opcional)
     * @param size         Tamaño de página (opcional)
     * @return Página de clases de documento activas
     */
    @GetMapping("/findAllActive/{enterpriseId}")
    public ResponseEntity<Page<DocumentClassRes>> listActive(
            @PathVariable("enterpriseId") String enterpriseId,
            @RequestParam(required = false) Optional<Integer> page,
            @RequestParam(required = false) Optional<Integer> size) {

        // Contar total de registros activos
        long totalRecords = service.countAllByEnterpriseAndStatus(enterpriseId, true);

        // Crear Pageable flexible
        Pageable pageable = paginationHelper.createFlexiblePageable(page, size, totalRecords);

        // Obtener página de datos
        Page<DocumentClass> pageResult = service.findAllByEnterpriseAndStatus(enterpriseId, true,
                pageable.getPageNumber(), pageable.getPageSize());
        return ResponseEntity.ok(pageResult.map(mapper::toRes));
    }

    @PatchMapping("/changeState/{id}/{enterpriseId}")
    public ResponseEntity<DocumentClassRes> changeState(
            @PathVariable Long id,
            @PathVariable String enterpriseId,
            @RequestParam Boolean status) {
        DocumentClass updated = service.changeState(id, enterpriseId, status);
        return ResponseEntity.ok(mapper.toRes(updated));
    }

    @DeleteMapping("/delete/{id}/{enterpriseId}")
    public ResponseEntity<DocumentClassRes> softDelete(
            @PathVariable Long id,
            @PathVariable String enterpriseId) {
        DocumentClass deleted = service.softDelete(id, enterpriseId);
        return ResponseEntity.ok(mapper.toRes(deleted));
    }
}


