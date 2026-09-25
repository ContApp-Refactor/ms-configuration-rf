package co.unicauca.edu.co.contables.configuration.thirds.infrastructure.adapters.input.rest.controller;

import co.unicauca.edu.co.contables.configuration.thirds.application.ports.input.*;
import co.unicauca.edu.co.contables.configuration.thirds.application.ports.output.PdfRUTContentOutput;
import co.unicauca.edu.co.contables.configuration.thirds.application.service.importExport.PdfRUTService;
import co.unicauca.edu.co.contables.configuration.thirds.application.service.third.CreateThirdService;
import co.unicauca.edu.co.contables.configuration.thirds.application.service.third.UpdateThirdService;
import co.unicauca.edu.co.contables.configuration.thirds.domain.enums.ExportableField;
import co.unicauca.edu.co.contables.configuration.thirds.domain.enums.ImportStatus;
import co.unicauca.edu.co.contables.configuration.thirds.domain.model.ExportJobStatus;
import co.unicauca.edu.co.contables.configuration.thirds.domain.model.ImportJobStatus;
import co.unicauca.edu.co.contables.configuration.thirds.domain.model.PdfRUTContent;
import co.unicauca.edu.co.contables.configuration.thirds.domain.model.Third;
import co.unicauca.edu.co.contables.configuration.thirds.domain.utils.ValidationUtils;
import co.unicauca.edu.co.contables.configuration.thirds.infrastructure.adapters.input.rest.dto.request.ThirdCreateRequest;
import co.unicauca.edu.co.contables.configuration.thirds.infrastructure.adapters.input.rest.dto.request.ThirdExportRequest;
import co.unicauca.edu.co.contables.configuration.thirds.infrastructure.adapters.input.rest.dto.request.ThirdImportRequest;
import co.unicauca.edu.co.contables.configuration.thirds.infrastructure.adapters.input.rest.dto.request.ThirdUpdateRequest;
import co.unicauca.edu.co.contables.configuration.thirds.infrastructure.adapters.input.rest.dto.response.BulkStateChangeResponse;
import co.unicauca.edu.co.contables.configuration.thirds.infrastructure.adapters.input.rest.dto.response.ChangeThirdStateResponse;
import co.unicauca.edu.co.contables.configuration.thirds.infrastructure.adapters.input.rest.dto.response.ThirdResponse;
import co.unicauca.edu.co.contables.configuration.thirds.infrastructure.adapters.input.rest.mapper.ThirdRestMapper;
import co.unicauca.edu.co.contables.configuration.thirds.infrastructure.utils.ExcelFileNameGenerator;
import co.unicauca.edu.co.contables.configuration.thirds.infrastructure.utils.PaginationHelper;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * @brief Controlador REST principal para gestión completa de terceros
 *
 *        Adaptador de entrada que expone la API completa para gestión de
 *        terceros:
 *        CRUD completo, importación/exportación, cambios de estado masivos,
 *        validación de RUT, y consultas avanzadas con filtros y paginación.
 */
@RestController
@RequestMapping("/api/thirds")
@Validated
@RequiredArgsConstructor
// @PreAuthorize("hasRole('admin_client') or hasRole('super_client')")
public class ThirdRestController {

    private final ListThirdsUseCase listThirdsUseCase;
    private final GetThirdUseCase getThirdUseCase;
    private final ChangeThirdStateUseCase changeThirdStateUseCase;
    private final BulkChangeThirdStateUseCase bulkChangeThirdStateUseCase;
    private final DeleteThirdUseCase deleteThirdUseCase;
    private final ExportThirdUseCase exportThirdUseCase;
    private final ImportThirdUseCase importThirdUseCase;
    private final ThirdRestMapper thirdRestMapper;
    private final PdfRUTService pdfRUTService;
    private final CreateThirdService createThirdService;
    private final UpdateThirdService updateThirdService;
    private final ExcelFileNameGenerator fileNameGenerator;

    @PreAuthorize("hasAuthority('TD#C')")
    @PostMapping("/")
    public ResponseEntity<ThirdResponse> createThird(@RequestBody @Valid ThirdCreateRequest thirdCreateRequest) {

        Third third = thirdRestMapper.toThird(thirdCreateRequest);

        // Use geography validation service for proper geography integration
        third = createThirdService.createThird(third,
                thirdCreateRequest.getCountryCode(),
                thirdCreateRequest.getStateCode(),
                thirdCreateRequest.getCityCode());

        return new ResponseEntity<>(thirdRestMapper.toThirdCreateResponse(third), HttpStatus.CREATED);
    }

    @PreAuthorize("hasAuthority('TD#U')")
    @PostMapping("/update")
    public ResponseEntity<ThirdResponse> updateThird(@RequestBody @Valid ThirdUpdateRequest thirdUpdateRequest) {

        Third third = thirdRestMapper.toThird(thirdUpdateRequest);

        // Use geography validation service for proper geography integration
        third = updateThirdService.updateThirdWithGeography(third,
                thirdUpdateRequest.getCountryCode(),
                thirdUpdateRequest.getStateCode(),
                thirdUpdateRequest.getCityCode());

        return new ResponseEntity<>(thirdRestMapper.toThirdCreateResponse(third), HttpStatus.OK);
    }

    @PreAuthorize("hasAuthority('TD#CS')")
    @PutMapping("/")
    public ResponseEntity<ChangeThirdStateResponse> changeThirdState(
            @NotNull(message = "thId es requerido") @RequestParam Long thId,
            @NotNull(message = "entId es requerido") @RequestParam String entId) {
        Boolean result = changeThirdStateUseCase.changeThirdState(thId, entId);

        return new ResponseEntity<>(thirdRestMapper.toChangeThirdStateResponse(result), HttpStatus.OK);
    }

    @PreAuthorize("hasAuthority('TD#CSA')")
    @PatchMapping("/allState")
    public ResponseEntity<BulkStateChangeResponse> changeAllThirdsState(
            @NotBlank(message = "entId es requerido y no puede estar vacío") @RequestParam String entId,
            @NotNull(message = "newState es requerido") @RequestParam Boolean newState) {

        int updatedCount = bulkChangeThirdStateUseCase.changeAllThirdsState(entId, newState);

        String message = String.format("Se actualizaron %d terceros al estado %s",
                updatedCount, newState ? "activo" : "inactivo");

        BulkStateChangeResponse response = BulkStateChangeResponse.builder()
                .updatedCount(updatedCount)
                .newState(newState)
                .message(message)
                .build();

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/third")
    public ResponseEntity<Third> getThirdById(
            @NotNull(message = "thId es requerido") @RequestParam Long thId,
            @NotNull(message = "entId es requerido") @RequestParam String entId) {

        Third third = getThirdUseCase.getThirdById(thId, entId);

        return new ResponseEntity<>(third, HttpStatus.OK);
    }

    @GetMapping("/existBy")
    public ResponseEntity<Boolean> existThirdById(
            @NotNull(message = "idNumber es requerido") @RequestParam Long idNumber,
            @NotNull(message = "entId es requerido") @RequestParam String entId) {

        boolean exists = getThirdUseCase.existThirdById(idNumber, entId);

        return new ResponseEntity<>(exists, HttpStatus.OK);
    }

    @GetMapping("/")
    public ResponseEntity<Page<Third>> getThirdsList(
            @NotNull(message = "entId es requerido") @RequestParam String entId,
            @RequestParam(required = false) Optional<Integer> numPage,
            @RequestParam(required = false) Optional<Integer> size,
            @RequestParam(defaultValue = "names") String sortField,
            @RequestParam(defaultValue = "asc") String sortOrder,
            @RequestParam(required = false) String search) {

        // Contar total de registros (con o sin filtro)
        long totalRecords = (search != null && !search.trim().isEmpty())
                ? listThirdsUseCase.countByEntIdAndSearch(entId, search)
                : listThirdsUseCase.countAllThirdsByEntId(entId);

        // Crear Pageable flexible
        Pageable pageable = PaginationHelper.createFlexiblePageable(numPage, size, totalRecords);

        // Obtener página de datos (con o sin filtro)
        Page<Third> page = (search != null && !search.trim().isEmpty())
                ? listThirdsUseCase.findByEntIdAndSearch(entId, search, pageable.getPageNumber(),
                        pageable.getPageSize(), sortField, sortOrder)
                : listThirdsUseCase.getAllThirdsByWithSort(entId, pageable.getPageNumber(),
                        pageable.getPageSize(), sortField, sortOrder);

        return new ResponseEntity<>(page, HttpStatus.OK);
    }

    @GetMapping("/findAllActive")
    public ResponseEntity<Page<Third>> getActiveThirds(
            @NotNull(message = "entId es requerido") @RequestParam String entId,
            @RequestParam(required = false) Optional<Integer> numPage,
            @RequestParam(required = false) Optional<Integer> size,
            @RequestParam(defaultValue = "names") String sortField,
            @RequestParam(defaultValue = "asc") String sortOrder) {

        // Contar total de registros activos
        long totalRecords = listThirdsUseCase.countActiveThirdsByEntId(entId);

        // Crear Pageable flexible
        Pageable pageable = PaginationHelper.createFlexiblePageable(numPage, size, totalRecords);

        // Obtener página de datos activos
        Page<Third> page = listThirdsUseCase.getAllActiveThirdsByWithSort(entId, pageable.getPageNumber(),
                pageable.getPageSize(), sortField, sortOrder);

        return new ResponseEntity<>(page, HttpStatus.OK);
    }

    @PreAuthorize("hasAuthority('TD#IPR')")
    @PostMapping("/content-PDF-RUT")
    public ResponseEntity<PdfRUTContentOutput> uploadPdf(@RequestParam MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(null);
        }
        PdfRUTContent request = new PdfRUTContent(file);
        PdfRUTContentOutput response = pdfRUTService.extractContent(request);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAuthority('TD#ET')")
    @GetMapping("/template/excel")
    public ResponseEntity<Resource> exportThirdTemplate(
            @RequestParam String entId) {

        Resource templateFile = exportThirdUseCase.exportThirdTemplateWithValidations(entId);
        String filename = fileNameGenerator.generateTemplateFileName();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(
                        MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(templateFile);
    }

    @GetMapping("/export/excel")
    public ResponseEntity<Map<String, String>> exportThirdsWithValidationsAsync(
            @NotNull(message = "entId es requerido") @RequestParam String entId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String companyName,
            @RequestParam(required = false) Set<ExportableField> optionalFields) {

        // Convertir status de String a Boolean, manejando strings vacíos
        Boolean statusBoolean = ValidationUtils.parseOptionalBoolean(status);

        // Si no se especifican campos opcionales, usar conjunto vacío
        Set<ExportableField> fields = optionalFields != null ? optionalFields : Set.of();

        ThirdExportRequest exportRequest = ThirdExportRequest.builder()
                .entId(entId)
                .status(statusBoolean)
                .companyName(companyName)
                .optionalFields(fields)
                .build();

        String jobId = exportThirdUseCase.exportThirdsAsync(exportRequest);

        Map<String, String> response = Map.of(
                "jobId", jobId,
                "message", "Exportación iniciada correctamente",
                "downloadUrl", "/api/thirds/export/download/" + jobId);

        return ResponseEntity.accepted().body(response);
    }

    @GetMapping("/export/status/{jobId}")
    public ResponseEntity<ExportJobStatus> getExportStatus(@PathVariable String jobId) {
        Optional<ExportJobStatus> jobStatus = exportThirdUseCase
                .getExportStatus(jobId);
        return jobStatus.map(status -> new ResponseEntity<>(status, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @PreAuthorize("hasAuthority('TD#E')")
    @GetMapping("/export/download/{jobId}")
    public ResponseEntity<Resource> downloadExportedFile(@PathVariable String jobId) {
        Optional<ExportJobStatus> jobStatusOpt = exportThirdUseCase.getExportStatus(jobId);

        if (jobStatusOpt.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        ExportJobStatus jobStatus = jobStatusOpt.get();

        // Verificar que el job esté completado
        if (jobStatus.getStatus() != ImportStatus.COMPLETED) {
            return ResponseEntity.status(HttpStatus.ACCEPTED)
                    .body(null); // Indicar que aún no está listo
        }

        // Verificar que los datos del archivo existan
        if (jobStatus.getFileData() == null || jobStatus.getFileData().length == 0) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        // Crear recurso con los datos del archivo
        ByteArrayResource resource = new ByteArrayResource(jobStatus.getFileData());

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + jobStatus.getFileName() + "\"")
                .contentType(
                        MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(resource);
    }

    @PreAuthorize("hasAuthority('TD#I')")
    @PostMapping("/import/excel")
    public ResponseEntity<Map<String, String>> importThirdsFromExcel(
            @NotNull(message = "entId es requerido") @RequestParam String entId,
            @NotNull(message = "El archivo Excel es obligatorio") @RequestParam("file") MultipartFile file) {

        ThirdImportRequest importRequest = ThirdImportRequest.builder()
                .entId(entId)
                .excelFile(file)
                .fileName(file.getOriginalFilename())
                .build();

        // Crea el job y lanza el procesamiento asíncrono (retorna inmediatamente)
        String jobId = importThirdUseCase.importThirdsFromExcel(importRequest);

        Map<String, String> response = Map.of(
                "jobId", jobId,
                "message", "Importación iniciada correctamente",
                "statusUrl", "/api/thirds/import/status/" + jobId);
        return ResponseEntity.accepted().body(response);
    }

    @GetMapping("/import/status/{jobId}")
    public ResponseEntity<ImportJobStatus> getImportStatus(@PathVariable String jobId) {
        return importThirdUseCase.getImportStatus(jobId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PreAuthorize("hasAuthority('TD#D')")
    @DeleteMapping("/delete")
    public ResponseEntity<Boolean> deleteThird(
            @NotNull(message = "thirdId es requerido") @RequestParam Long thirdId,
            @NotNull(message = "entId es requerido") @RequestParam String entId) {

        boolean deleted = deleteThirdUseCase.deleteThird(thirdId, entId);

        return new ResponseEntity<>(deleted, HttpStatus.OK);
    }

    @GetMapping("/by-type")
    public ResponseEntity<Page<Third>> getThirdsByType(
            @NotNull(message = "entId es requerido") @RequestParam String entId,
            @NotNull(message = "thirdTypeName es requerido") @RequestParam String thirdTypeName,
            @RequestParam(required = false) Optional<Integer> numPage,
            @RequestParam(required = false) Optional<Integer> size) {

        long totalRecords = listThirdsUseCase.countThirdsByEntIdAndThirdTypeName(entId, thirdTypeName);

        Pageable pageable = PaginationHelper.createFlexiblePageable(numPage, size, totalRecords);
        Page<Third> page = listThirdsUseCase.getThirdsByEntIdAndThirdTypeName(entId, thirdTypeName,
                pageable.getPageNumber(), pageable.getPageSize());

        return new ResponseEntity<>(page, HttpStatus.OK);
    }

}
