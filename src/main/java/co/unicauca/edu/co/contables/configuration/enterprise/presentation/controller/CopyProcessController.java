package co.unicauca.edu.co.contables.configuration.enterprise.presentation.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import co.unicauca.edu.co.contables.commons.backup.RestoreActor;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.mapper.CopyProcessDomainMapper;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.models.CopyProcess;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.services.ICopyProcessService;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.services.coordination.BackupModuleInfo;
import co.unicauca.edu.co.contables.configuration.enterprise.presentation.DTO.request.CopyProcessCreateReq;
import co.unicauca.edu.co.contables.configuration.enterprise.presentation.DTO.request.CopyProcessRestoreReq;
import co.unicauca.edu.co.contables.configuration.enterprise.presentation.DTO.response.CopyProcessRes;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * @brief Controlador REST de procesos de copia (BACKUP/DUPLICATE/RESTORE) de
 * empresas, con el contrato orientado a procesos que consume el frontend
 * (Hito 6).
 *
 * Tenant y usuario se capturan AQUÍ, en el hilo de la petición, con
 * {@link RestoreActor#fromCurrentContext()} (TenantContext + {@code sub} del
 * JWT del SecurityContext) y viajan explícitos hasta el coordinador. No se usa
 * {@code IJwtUtils}: el converter singleton guarda el último JWT y con
 * concurrencia devuelve el de otro usuario.
 *
 * Nota de seguridad: estas rutas no están en {@code permitAll} de
 * {@code SecurityConfig}: caen en {@code .anyRequest().authenticated()} y
 * además exigen uno de los roles de cliente de la empresa.
 */
@RequestMapping("/api/enterprises/copy")
@RestController
@RequiredArgsConstructor
@Tag(name = "Enterprise copy processes", description = "Procesos de copia (backup/duplicate/restore) de empresas")
@PreAuthorize("hasRole('admin_client') or hasRole('user_client') or hasRole('super_client')")
public class CopyProcessController {

    private final ICopyProcessService copyProcessService;
    private final CopyProcessDomainMapper mapper;

    @Operation(summary = "Listar los procesos de copia, más reciente primero")
    @GetMapping("/processes")
    public ResponseEntity<List<CopyProcessRes>> getProcesses() {
        List<CopyProcessRes> body = copyProcessService.findAll().stream().map(mapper::toRes).toList();
        return ResponseEntity.ok(body);
    }

    @Operation(summary = "Crear y ejecutar un proceso BACKUP o DUPLICATE")
    @PostMapping("/processes")
    public ResponseEntity<CopyProcessRes> createProcess(@Valid @RequestBody CopyProcessCreateReq request) {
        RestoreActor actor = RestoreActor.fromCurrentContext();
        CopyProcess process = copyProcessService.create(request.getTipo(), request.getEmpresaOrigen(),
                request.resolveEmpresaDestinoNombre(), actor, iniciadoPor(actor));
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toRes(process));
    }

    @Operation(summary = "Obtener el estado actual de un proceso de copia")
    @GetMapping("/processes/{id}")
    public ResponseEntity<CopyProcessRes> getProcess(@PathVariable("id") UUID id) {
        return ResponseEntity.ok(mapper.toRes(copyProcessService.findById(id)));
    }

    @Operation(summary = "Descargar el ZIP de backup de un proceso")
    @GetMapping("/processes/{id}/backup")
    public ResponseEntity<Resource> downloadProcessBackup(@PathVariable("id") UUID id) {
        Resource resource = copyProcessService.loadBackupFile(id);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(id + ".zip").build().toString())
                .body(resource);
    }

    @Operation(summary = "Eliminar el registro de un proceso de copia (debe estar en estado terminal)")
    @DeleteMapping("/processes/{id}")
    public ResponseEntity<Void> deleteProcess(@PathVariable("id") UUID id) {
        copyProcessService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Crear y ejecutar un proceso RESTORE desde un backup existente (backupRef del tenant)")
    @PostMapping("/restore")
    public ResponseEntity<CopyProcessRes> restore(@Valid @RequestBody CopyProcessRestoreReq request) {
        RestoreActor actor = RestoreActor.fromCurrentContext();
        CopyProcess process = copyProcessService.restoreFromBackupRef(request.getBackupRef(),
                new ICopyProcessService.RestoreRequest(request.getEmpresaDestino(), request.isInplace(),
                        request.getNombreDestino(), request.getNitDestino(), request.isSkipUnknownModules(), actor,
                        iniciadoPor(actor)));
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toRes(process));
    }

    @Operation(summary = "Crear y ejecutar un proceso RESTORE desde un archivo de backup subido")
    @PostMapping(value = "/restore/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CopyProcessRes> restoreUpload(
            @RequestParam("file") MultipartFile file,
            @RequestParam("empresaDestino") String empresaDestino,
            @RequestParam(value = "inplace", defaultValue = "false") boolean inplace,
            @RequestParam(value = "nombreDestino", required = false) String nombreDestino,
            @RequestParam(value = "nitDestino", required = false) String nitDestino,
            @RequestParam(value = "skipUnknownModules", defaultValue = "false") boolean skipUnknownModules) {
        RestoreActor actor = RestoreActor.fromCurrentContext();
        CopyProcess process = copyProcessService.restoreFromUpload(file,
                new ICopyProcessService.RestoreRequest(empresaDestino, inplace, nombreDestino, nitDestino,
                        skipUnknownModules, actor, iniciadoPor(actor)));
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toRes(process));
    }

    @Operation(summary = "Módulos que incluye la copia de seguridad completa, en orden de restauración")
    @GetMapping("/modules")
    public ResponseEntity<List<BackupModuleInfo>> getModules() {
        return ResponseEntity.ok(copyProcessService.supportedModules());
    }

    /** {@code preferred_username} del JWT de ESTA petición; si no viene, el {@code sub}. */
    private static String iniciadoPor(RestoreActor actor) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth instanceof JwtAuthenticationToken jwt) {
            String username = jwt.getToken().getClaimAsString("preferred_username");
            if (username != null && !username.isBlank()) {
                return username;
            }
        }
        return actor.userId();
    }
}
