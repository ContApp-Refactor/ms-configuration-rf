package co.unicauca.edu.co.contables.configuration.costCenters.presentation.controller;

import co.unicauca.edu.co.contables.configuration.commons.utils.PaginationHelper;
import co.unicauca.edu.co.contables.configuration.costCenters.domain.models.CostCenter;
import co.unicauca.edu.co.contables.configuration.costCenters.domain.mapper.CostCenterDomainMapper;
import co.unicauca.edu.co.contables.configuration.costCenters.domain.services.ICostCenterService;
import co.unicauca.edu.co.contables.configuration.costCenters.domain.services.IExportCostCenterService;
import co.unicauca.edu.co.contables.configuration.costCenters.presentation.DTO.request.CostCenterCreateReq;
import co.unicauca.edu.co.contables.configuration.costCenters.presentation.DTO.request.CostCenterUpdateReq;
import co.unicauca.edu.co.contables.configuration.costCenters.presentation.DTO.response.CostCenterRes;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/api/config/cost-centers")
@RequiredArgsConstructor
public class CostCenterController {

    private final ICostCenterService service;
    private final IExportCostCenterService exportService;
    private final CostCenterDomainMapper mapper;
    private final PaginationHelper paginationHelper;

    @PostMapping("/create")
    public ResponseEntity<CostCenterRes> create(@Valid @RequestBody CostCenterCreateReq request) {
        CostCenter created = service.create(request);
        return ResponseEntity.ok(mapper.toRes(created));
    }

    @PutMapping("/update")
    public ResponseEntity<CostCenterRes> update(@Valid @RequestBody CostCenterUpdateReq request) {
        CostCenter updated = service.update(request);
        return ResponseEntity.ok(mapper.toRes(updated));
    }

    @GetMapping("/findById/{id}/{enterpriseId}")
    public ResponseEntity<CostCenterRes> getById(@PathVariable Long id, @PathVariable String enterpriseId) {
        return ResponseEntity.ok(mapper.toRes(service.findById(id, enterpriseId)));
    }

    /**
     * Obtiene centros de costo con paginación jerárquica flexible.
     * Si no se especifican parámetros de paginación, retorna todos los centros de
     * costo.
     * 
     * @param enterpriseId ID de la empresa
     * @param page         Número de página (opcional)
     * @param size         Tamaño de página (opcional)
     * @param search       Término de búsqueda (opcional)
     * @return Página de centros de costo jerárquicos
     */
    @GetMapping("/findAll/{enterpriseId}")
    public ResponseEntity<Page<CostCenterRes>> listHierarchical(
            @PathVariable String enterpriseId,
            @RequestParam(required = false) Optional<Integer> page,
            @RequestParam(required = false) Optional<Integer> size,
            @RequestParam(required = false) String search) {

        // Contar total de registros (con o sin filtro)
        long totalRecords = (search != null && !search.trim().isEmpty())
                ? service.countByEnterpriseAndSearch(enterpriseId, search)
                : service.countAllByEnterprise(enterpriseId);

        // Crear Pageable flexible
        Pageable pageable = paginationHelper.createFlexiblePageable(page, size, totalRecords);

        // Obtener página de datos (con o sin filtro)
        Page<CostCenter> pageResult = (search != null && !search.trim().isEmpty())
                ? service.findByEnterpriseAndSearch(enterpriseId, search, pageable.getPageNumber(),
                        pageable.getPageSize())
                : service.findAllByEnterpriseHierarchical(enterpriseId, pageable.getPageNumber(),
                        pageable.getPageSize());

        Page<CostCenterRes> mapped = pageResult.map(mapper::toRes);
        return ResponseEntity.ok(mapped);
    }

    /**
     * Obtiene centros de costo filtrados por estado con paginación flexible.
     * Si no se especifican parámetros de paginación, retorna todos los centros de
     * costo del estado especificado.
     * 
     * @param enterpriseId ID de la empresa
     * @param status       Estado del centro de costo
     * @param page         Número de página (opcional)
     * @param size         Tamaño de página (opcional)
     * @return Página de centros de costo filtrados por estado
     */
    @GetMapping("/findAllByStatus/{enterpriseId}")
    public ResponseEntity<Page<CostCenterRes>> listByStatus(
            @PathVariable String enterpriseId,
            @RequestParam Boolean status,
            @RequestParam(required = false) Optional<Integer> page,
            @RequestParam(required = false) Optional<Integer> size) {

        // Contar total de registros con el filtro de estado
        long totalRecords = service.countAllByEnterpriseAndStatus(enterpriseId, status);

        // Crear Pageable flexible
        Pageable pageable = paginationHelper.createFlexiblePageable(page, size, totalRecords);

        // Obtener página de datos
        Page<CostCenter> pageResult = service.findAllByEnterpriseAndStatus(enterpriseId, status,
                pageable.getPageNumber(), pageable.getPageSize());
        Page<CostCenterRes> mapped = pageResult.map(mapper::toRes);
        return ResponseEntity.ok(mapped);
    }

    @PatchMapping("/changeState/{id}/{enterpriseId}")
    public ResponseEntity<CostCenterRes> changeState(
            @PathVariable Long id,
            @PathVariable String enterpriseId,
            @RequestParam Boolean status) {
        CostCenter updated = service.changeState(id, enterpriseId, status);
        return ResponseEntity.ok(mapper.toRes(updated));
    }

    @DeleteMapping("/delete/{id}/{enterpriseId}")
    public ResponseEntity<CostCenterRes> delete(
            @PathVariable Long id,
            @PathVariable String enterpriseId) {
        CostCenter deleted = service.delete(id, enterpriseId);
        return ResponseEntity.ok(mapper.toRes(deleted));
    }

    /**
     * Obtiene los centros de costo activos de último nivel (código con 5 o más
     * caracteres)
     * 
     * @param enterpriseId ID de la empresa
     * @return Lista de centros de costo de último nivel activos
     */
    @GetMapping("/findAuxiliary/{enterpriseId}")
    public ResponseEntity<List<CostCenterRes>> findActiveLastLevelCostCenters(
            @PathVariable String enterpriseId) {
        List<CostCenter> costCenters = service.findActiveLastLevelCostCenters(enterpriseId);
        List<CostCenterRes> response = costCenters.stream()
                .map(mapper::toRes)
                .toList();
        return ResponseEntity.ok(response);
    }

    /**
     * Exporta centros de costo de una empresa a Excel.
     * Permite filtrar por estado mediante parámetro opcional.
     * 
     * @param enterpriseId ID de la empresa
     * @param status       Estado de los centros de costo (true=activos,
     *                     false=inactivos, null=todos)
     * @param companyName  Nombre de la empresa para el archivo (opcional)
     * @return Archivo Excel con los centros de costo
     */
    @GetMapping("/export/excel/{enterpriseId}")
    public ResponseEntity<Resource> exportCostCenters(
            @PathVariable String enterpriseId,
            @RequestParam(required = false) Boolean status,
            @RequestParam(required = false) String companyName) {

        Resource excelFile = exportService.exportCostCenters(enterpriseId, status);
        String filename = generateFileName(enterpriseId, status, companyName);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(excelFile);
    }

    /**
     * Genera el nombre del archivo Excel con timestamp.
     */
    private String generateFileName(String enterpriseId, Boolean status, String companyName) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String statusSuffix = status != null ? (status ? "_activos" : "_inactivos") : "";
        
        // Si se proporciona el nombre de la empresa, incluirlo en el nombre del archivo
        if (companyName != null && !companyName.trim().isEmpty()) {
            String sanitizedCompanyName = companyName.trim().replaceAll("[^a-zA-Z0-9_-]", "_");
            return String.format("centros_costo_%s%s_%s.xlsx", sanitizedCompanyName, statusSuffix, timestamp);
        }
        
        // Si no se proporciona nombre, exportar sin identificador de empresa
        return String.format("centros_costo%s_%s.xlsx", statusSuffix, timestamp);
    }
}
