package co.unicauca.edu.co.contables.configuration.typesOfDocuments.presentation.controller;

import co.unicauca.edu.co.contables.configuration.commons.utils.PaginationHelper;
import co.unicauca.edu.co.contables.configuration.typesOfDocuments.domain.models.DocumentModule;
import co.unicauca.edu.co.contables.configuration.typesOfDocuments.domain.models.DocumentType;
import co.unicauca.edu.co.contables.configuration.typesOfDocuments.domain.services.IDocumentTypeService;
import co.unicauca.edu.co.contables.configuration.typesOfDocuments.domain.mapper.DocumentTypeDomainMapper;
import co.unicauca.edu.co.contables.configuration.typesOfDocuments.presentation.DTO.request.DocumentTypeCreateReq;
import co.unicauca.edu.co.contables.configuration.typesOfDocuments.presentation.DTO.request.DocumentTypeUpdateReq;
import co.unicauca.edu.co.contables.configuration.typesOfDocuments.presentation.DTO.response.DocumentModuleRes;
import co.unicauca.edu.co.contables.configuration.typesOfDocuments.presentation.DTO.response.DocumentTypeRes;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/** @brief Controlador REST para gestión de tipos de documentos */
@RestController
@RequestMapping("/api/config/document-types")
@RequiredArgsConstructor
public class DocumentTypeController {

    private final IDocumentTypeService service;
    private final DocumentTypeDomainMapper mapper;
    private final PaginationHelper paginationHelper;

    @PostMapping("/create")
    public ResponseEntity<DocumentTypeRes> create(@Valid @RequestBody DocumentTypeCreateReq request) {
        DocumentType created = service.create(request);
        return ResponseEntity.ok(mapper.toRes(created));
    }

    @PutMapping("/update")
    public ResponseEntity<DocumentTypeRes> update(@Valid @RequestBody DocumentTypeUpdateReq request) {
        DocumentType updated = service.update(request);
        return ResponseEntity.ok(mapper.toRes(updated));
    }

    @GetMapping("/findById/{id}/{enterpriseId}")
    public ResponseEntity<DocumentTypeRes> getById(@PathVariable Long id, @PathVariable String enterpriseId) {
        return ResponseEntity.ok(mapper.toRes(service.findById(id, enterpriseId)));
    }

    @GetMapping("/findAll/{enterpriseId}")
    public ResponseEntity<Page<DocumentTypeRes>> list(
            @PathVariable("enterpriseId") String enterpriseId,
            @RequestParam(required = false) Optional<Integer> page,
            @RequestParam(required = false) Optional<Integer> size,
            @RequestParam(defaultValue = "name") String sortField,
            @RequestParam(defaultValue = "asc") String sortOrder,
            @RequestParam(required = false) String search) {

        // Contar total de registros (con o sin filtro)
        long totalRecords = (search != null && !search.trim().isEmpty()) 
            ? service.countByEnterpriseAndNameContaining(enterpriseId, search)
            : service.countAllByEnterprise(enterpriseId);

        // Crear Pageable flexible
        Pageable pageable = paginationHelper.createFlexiblePageable(page, size, totalRecords);

        // Obtener página de datos (con o sin filtro)
        Page<DocumentType> pageResult = (search != null && !search.trim().isEmpty())
            ? service.findByEnterpriseAndNameContaining(enterpriseId, search, pageable.getPageNumber(),
                    pageable.getPageSize(), sortField, sortOrder)
            : service.findAllByEnterprise(enterpriseId, pageable.getPageNumber(),
                    pageable.getPageSize(), sortField, sortOrder);
        
        return ResponseEntity.ok(pageResult.map(mapper::toRes));
    }
  
    @GetMapping("/findAllByModule/{enterpriseId}")
    public ResponseEntity<List<DocumentTypeRes>> listByModule(
            @PathVariable("enterpriseId") String enterpriseId,
            @RequestParam Integer moduleId) {

        List<DocumentType> documentTypes = service.findAllByModuleAndEnterprise(moduleId, enterpriseId);
        List<DocumentTypeRes> response = documentTypes.stream()
                .map(mapper::toRes)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/changeState/{id}/{enterpriseId}")
    public ResponseEntity<DocumentTypeRes> changeState(
            @PathVariable Long id,
            @PathVariable String enterpriseId,
            @RequestParam Boolean status) {
        DocumentType updated = service.changeState(id, enterpriseId, status);
        return ResponseEntity.ok(mapper.toRes(updated));
    }

    @DeleteMapping("/delete/{id}/{enterpriseId}")
    public ResponseEntity<DocumentTypeRes> Delete(
            @PathVariable Long id,
            @PathVariable String enterpriseId) {
        DocumentType deleted = service.Delete(id, enterpriseId);
        return ResponseEntity.ok(mapper.toRes(deleted));
    }

    /**
     * Obtiene todos los módulos disponibles en el sistema.
     * Los módulos son globales y no dependen de la empresa.
     * 
     * @return Lista de módulos con su ID y nombre
     */
    @GetMapping("/modules")
    public ResponseEntity<List<DocumentModuleRes>> getAllModules() {
        List<DocumentModule> modules = service.getAllModules();
        
        List<DocumentModuleRes> response = modules.stream()
                .map(module -> DocumentModuleRes.builder()
                        .id(module.getId())
                        .name(module.getName())
                        .build())
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(response);
    }
}