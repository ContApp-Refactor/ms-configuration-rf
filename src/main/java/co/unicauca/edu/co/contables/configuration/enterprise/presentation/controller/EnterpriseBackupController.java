package co.unicauca.edu.co.contables.configuration.enterprise.presentation.controller;

import java.util.UUID;

import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import co.unicauca.edu.co.contables.configuration.enterprise.domain.dto.BackupReferenceDto;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.services.IEnterpriseBackupService;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.services.IEnterpriseDuplicateService;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.services.IEnterpriseRestoreService;
import co.unicauca.edu.co.contables.configuration.enterprise.presentation.DTO.response.BackupReferenceRes;
import co.unicauca.edu.co.contables.configuration.enterprise.presentation.DTO.response.DuplicateResultRes;
import co.unicauca.edu.co.contables.configuration.enterprise.presentation.DTO.response.RestoreResultRes;
import co.unicauca.edu.co.contables.configuration.enterprise.presentation.exception.InvalidRestoreRequestException;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

/**
 * @brief Controlador REST de copia de seguridad (backup/restore/duplicate)
 * de empresas.
 *
 * Nota de seguridad: estas rutas NO se agregan a la lista {@code permitAll}
 * de {@code SecurityConfig}; por lo tanto caen en
 * {@code .anyRequest().authenticated()} y además exigen uno de los roles de
 * cliente de la empresa, igual que {@code EnterpriseController}.
 */
@RequestMapping("/api/enterprises")
@RestController
@RequiredArgsConstructor
@Tag(name = "Enterprise backup", description = "Copia de seguridad, restore y duplicado de empresas")
@PreAuthorize("hasRole('admin_client') or hasRole('user_client') or hasRole('super_client')")
public class EnterpriseBackupController {

    private final IEnterpriseBackupService backupService;
    private final IEnterpriseRestoreService restoreService;
    private final IEnterpriseDuplicateService duplicateService;

    @Operation(summary = "Crear un backup de una empresa")
    @PostMapping("/{id}/backup")
    public ResponseEntity<BackupReferenceRes> createBackup(@PathVariable("id") UUID id) {
        BackupReferenceDto reference = backupService.backup(id);
        return ResponseEntity.ok(BackupReferenceRes.builder()
                .backupId(reference.getBackupId())
                .sourceEnterpriseId(reference.getSourceEnterpriseId())
                .fileName(reference.getFileName())
                .sizeBytes(reference.getSizeBytes())
                .createdAt(reference.getCreatedAt())
                .build());
    }

    @Operation(summary = "Descargar el ZIP de un backup existente")
    @GetMapping("/backups/{backupId}")
    public ResponseEntity<Resource> downloadBackup(@PathVariable("backupId") String backupId) {
        Resource resource = backupService.loadBackupFile(backupId);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(backupId + ".zip").build().toString())
                .body(resource);
    }

    @Operation(summary = "Restaurar una empresa desde un backup (por backupId existente o archivo subido)")
    @PostMapping(value = "/restore", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<RestoreResultRes> restore(
            @RequestParam(value = "backupId", required = false) String backupId,
            @RequestParam(value = "file", required = false) MultipartFile file,
            @RequestParam(value = "inplace", defaultValue = "false") boolean inplace,
            @RequestParam(value = "targetEnterpriseId", required = false) UUID targetEnterpriseId) {

        boolean hasBackupId = backupId != null && !backupId.isBlank();
        boolean hasFile = file != null && !file.isEmpty();
        if (hasBackupId == hasFile) {
            throw new InvalidRestoreRequestException(
                    "Debe indicar exactamente uno de 'backupId' o 'file' para restaurar");
        }

        UUID enterpriseId = hasBackupId
                ? restoreService.restoreFromBackupId(backupId, inplace, targetEnterpriseId)
                : restoreService.restoreFromFile(file, inplace, targetEnterpriseId);

        return ResponseEntity.ok(RestoreResultRes.builder().enterpriseId(enterpriseId).inplace(inplace).build());
    }

    @Operation(summary = "Duplicar una empresa (backup del origen + restore en una empresa nueva)")
    @PostMapping("/{id}/duplicate")
    public ResponseEntity<DuplicateResultRes> duplicate(@PathVariable("id") UUID id) {
        UUID newId = duplicateService.duplicate(id);
        return ResponseEntity.ok(DuplicateResultRes.builder().sourceEnterpriseId(id).newEnterpriseId(newId).build());
    }
}
