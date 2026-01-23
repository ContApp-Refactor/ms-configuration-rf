package co.unicauca.edu.co.contables.configuration.helpCenter.presentation.controller;

import co.unicauca.edu.co.contables.configuration.commons.utils.PaginationHelper;
import co.unicauca.edu.co.contables.configuration.helpCenter.domain.enums.DocumentModule;
import co.unicauca.edu.co.contables.configuration.helpCenter.domain.mapper.HelpCenterDomainMapper;
import co.unicauca.edu.co.contables.configuration.helpCenter.domain.models.HelpCenter;
import co.unicauca.edu.co.contables.configuration.helpCenter.domain.services.IHelpCenterService;
import co.unicauca.edu.co.contables.configuration.helpCenter.presentation.DTO.request.HelpCenterCreateReq;
import co.unicauca.edu.co.contables.configuration.helpCenter.presentation.DTO.request.HelpCenterUpdateReq;
import co.unicauca.edu.co.contables.configuration.helpCenter.presentation.DTO.response.HelpCenterRes;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @brief Controlador REST para centro de ayuda
 *
 * Expone endpoints REST para gestionar operaciones CRUD de registros del centro de ayuda,
 * incluyendo búsqueda, filtrado por módulo y obtención de módulos disponibles.
 */
@RestController
@RequestMapping("/api/config/help-center")
@RequiredArgsConstructor
public class HelpCenterController {

    private final IHelpCenterService service;
    private final HelpCenterDomainMapper mapper;
    private final PaginationHelper paginationHelper;

    @PreAuthorize("hasAuthority('Create_Help_Center')")
    @PostMapping("/create")
    public ResponseEntity<HelpCenterRes> create(@Valid @RequestBody HelpCenterCreateReq request) {
        HelpCenter created = service.create(request);
        return ResponseEntity.ok(mapper.toRes(created));
    }

    @PreAuthorize("hasAuthority('Update_Help_Center')")
    @PutMapping("/update")
    public ResponseEntity<HelpCenterRes> update(@Valid @RequestBody HelpCenterUpdateReq request) {
        HelpCenter updated = service.update(request);
        return ResponseEntity.ok(mapper.toRes(updated));
    }

    @GetMapping("/findById/{id}")
    public ResponseEntity<HelpCenterRes> getById(@PathVariable Long id) {
        return ResponseEntity.ok(mapper.toRes(service.findById(id)));
    }

    /**
     * Obtiene registros de ayuda con paginación flexible.
     * Si no se especifican parámetros de paginación, retorna todos los registros.
     * 
     * @param page         Número de página (opcional)
     * @param size         Tamaño de página (opcional)
     * @param sortField    Campo de ordenamiento (opcional, por defecto: name)
     * @param sortOrder    Orden (asc/desc) (opcional, por defecto: asc)
     * @param search       Término de búsqueda (opcional)
     * @return Página de registros de ayuda
     */
    @GetMapping("/findAll")
    public ResponseEntity<Page<HelpCenterRes>> list(
            @RequestParam(required = false) Optional<Integer> page,
            @RequestParam(required = false) Optional<Integer> size,
            @RequestParam(defaultValue = "name") String sortField,
            @RequestParam(defaultValue = "asc") String sortOrder,
            @RequestParam(required = false) String search) {

        // Contar total de registros (con o sin filtro)
        long totalRecords = (search != null && !search.trim().isEmpty()) 
            ? service.countByNameContaining(search)
            : service.countAll();

        // Crear Pageable flexible
        Pageable pageable = paginationHelper.createFlexiblePageable(page, size, totalRecords);

        // Obtener página de datos (con o sin filtro)
        Page<HelpCenter> pageResult = (search != null && !search.trim().isEmpty())
            ? service.findByNameContaining(search, pageable.getPageNumber(),
                    pageable.getPageSize(), sortField, sortOrder)
            : service.findAll(pageable.getPageNumber(),
                    pageable.getPageSize(), sortField, sortOrder);
        
        return ResponseEntity.ok(pageResult.map(mapper::toRes));
    }

    /**
     * Obtiene todos los registros de ayuda filtrados por ID de módulo y estado activo.
     * Retorna solo nombres y descripciones asociadas al módulo para registros activos.
     * 
     * @param moduleId     ID del módulo (1-8)
     * @return Lista de registros de ayuda activos del módulo
     */
    @GetMapping("/findAllByModule")
    public ResponseEntity<List<HelpCenterRes>> listByModule(
            @RequestParam Integer moduleId) {

        List<HelpCenter> helpCenters = service.findAllByModule(moduleId);
        List<HelpCenterRes> response = helpCenters.stream()
                .map(mapper::toRes)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAuthority('Change_State_Help_Center')")
    @PatchMapping("/changeState/{id}")
    public ResponseEntity<HelpCenterRes> changeState(
            @PathVariable Long id,
            @RequestParam Boolean status) {
        HelpCenter updated = service.changeState(id, status);
        return ResponseEntity.ok(mapper.toRes(updated));
    }

    @PreAuthorize("hasAuthority('Delete_Help_Center')")
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<HelpCenterRes> delete(@PathVariable Long id) {
        HelpCenter deleted = service.delete(id);
        return ResponseEntity.ok(mapper.toRes(deleted));
    }

    /**
     * Obtiene la lista de módulos disponibles para centros de ayuda.
     * 
     * @return Lista de módulos con ID y nombre
     */
    @GetMapping("/modules")
    public ResponseEntity<List<Map<String, Object>>> getModules() {
        List<Map<String, Object>> modules = Arrays.stream(DocumentModule.values())
                .map(module -> {
                    Map<String, Object> map = new java.util.HashMap<>();
                    map.put("id", module.getId());
                    map.put("name", module.getName());
                    return map;
                })
                .collect(Collectors.toList());
        return ResponseEntity.ok(modules);
    }
}
