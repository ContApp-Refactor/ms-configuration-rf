package co.unicauca.edu.co.contables.accounting.catalogue.catalogue.infraestructure.adapters.input.rest.controller;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
//import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.application.input.IAccountCatalogueChangeStateInputPort;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.application.input.IAccountCatalogueCreateInputPort;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.application.input.IAccountCatalogueDeleteInputPort;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.application.input.IAccountCatalogueExportInputPort;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.application.input.IAccountCatalogueImportInputPort;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.application.input.IAccountCatalogueSearchInputPort;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.application.input.IAccountCatalogueUpdateInputPort;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.domain.enums.ImportStatus;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.domain.models.AccountCatalogue;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.domain.models.ExportJobStatus;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.infraestructure.adapters.input.rest.dto.request.AccountCatalogueExportRequest;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.infraestructure.adapters.input.rest.dto.request.AccountCatalogueCreateReq;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.infraestructure.adapters.input.rest.dto.request.AccountCatalogueImportRequest;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.infraestructure.adapters.input.rest.dto.request.AccountCatalogueUpdateReq;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.infraestructure.adapters.input.rest.dto.response.AccountCatalogueChangeStateRes;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.infraestructure.adapters.input.rest.dto.response.AccountCatalogueCreateRes;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.infraestructure.adapters.input.rest.dto.response.AccountCatalogueListRes;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.infraestructure.adapters.input.rest.dto.response.AccountCatalogueUpdateRes;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.infraestructure.adapters.input.rest.dto.response.AuxiliaryAccountListRes;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.infraestructure.adapters.input.rest.dto.response.ItemAccountCatalogueSearchRes;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.infraestructure.adapters.input.rest.mapper.IAccountChangeStateRestMapper;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.infraestructure.adapters.input.rest.mapper.IAccountCreateRestMapper;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.infraestructure.adapters.input.rest.mapper.IAccountSearchRestMapper;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.infraestructure.adapters.input.rest.mapper.IAccountUpdateRestMapper;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.infraestructure.adapters.input.rest.mapper.IAuxiliaryAccountRestMapper;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.infraestructure.adapters.input.rest.mapper.IItemAccountSearchRestMapper;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.infraestructure.utils.AccountCatalogueExcelFileNameGenerator;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import org.springframework.validation.annotation.Validated;

/**
 * @brief Controlador REST para gestión completa del catálogo de cuentas
 *
 * Expone endpoints para operaciones CRUD, importación/exportación Excel,
 * búsqueda jerárquica y gestión de estados de cuentas contables.
 */
@RequestMapping("/api/accountCatalogue")
@RestController
@AllArgsConstructor
@Validated
// @PreAuthorize("hasRole('admin_client')")
public class AccountCatalogueController {
    private final IAccountCatalogueCreateInputPort accountCatalogueCreateInputPort;
    private final IAccountCreateRestMapper accountCreateRestMapper;
    private final IAccountSearchRestMapper accountSearchRestMapper;
    private final IAccountCatalogueSearchInputPort accountCatalogueSearchInputPort;
    private final IItemAccountSearchRestMapper itemAccountSearchRestMapper;
    private final IAccountUpdateRestMapper accountUpdateRestMapper;
    private final IAccountCatalogueDeleteInputPort accountCatalogueDeleteInputPort;
    private final IAccountCatalogueUpdateInputPort accountCatalogueUpdateInputPort;
    private final IAccountCatalogueChangeStateInputPort accountCatalogueChangeStateInputPort;
    private final IAccountChangeStateRestMapper accountChangeStateRestMapper;
    private final IAuxiliaryAccountRestMapper auxiliaryAccountRestMapper;
    private final IAccountCatalogueExportInputPort accountCatalogueExportInputPort;
    private final IAccountCatalogueImportInputPort accountCatalogueImportInputPort;
    private final AccountCatalogueExcelFileNameGenerator fileNameGenerator;

    @PreAuthorize("hasAuthority('AC#C')")
    @PostMapping("/")
    public ResponseEntity<AccountCatalogueCreateRes> createAccountCatalogue(
            @Valid @RequestBody AccountCatalogueCreateReq accountCatalogueCreateReq) {
        AccountCatalogue padre = null;
        if (accountCatalogueCreateReq.getParent() != null) {
            padre = accountCatalogueSearchInputPort.getAccountCatalogueById(accountCatalogueCreateReq.getParent(), accountCatalogueCreateReq.getIdEnterprise());
        }
        AccountCatalogue account = accountCreateRestMapper.toDomain(accountCatalogueCreateReq, padre);
        account = accountCatalogueCreateInputPort.createAccountCatalogue(account);
        return ResponseEntity.ok(accountCreateRestMapper.toCreateResponse(account));
    }

    @PreAuthorize("hasAuthority('AC#U')")
    @PutMapping("/{id}")
    public ResponseEntity<AccountCatalogueUpdateRes> updateAccountCatalogue(@PathVariable("id") int id,
            @Valid @RequestBody AccountCatalogueUpdateReq accountCatalogueUpdateReq) {
        // Convertir el request a dominio (incluye idEnterprise)
        AccountCatalogue updateAccountCatalogue = accountUpdateRestMapper.toDomain(accountCatalogueUpdateReq);
        
        // Actualizar la cuenta
        updateAccountCatalogue = accountCatalogueUpdateInputPort.updateAccountCatalogue(id, updateAccountCatalogue);
        return ResponseEntity.ok(accountUpdateRestMapper.toUpdateResponse(updateAccountCatalogue));
    }

    @GetMapping("/accountByCode/{code}/{idEnterprise}")
    public ResponseEntity<ItemAccountCatalogueSearchRes> getAccountCatalogue(@PathVariable String code,
            @PathVariable String idEnterprise) {
        AccountCatalogue accountCatalogue = accountCatalogueSearchInputPort.getAccountCatalogueByCode(code, idEnterprise);
        return ResponseEntity.ok(itemAccountSearchRestMapper.toItemAccountCatalogueSearch(accountCatalogue));
    }

    @PreAuthorize("hasAuthority('AC#D')")
    @DeleteMapping("/{id}/{idEnterprise}")
    public ResponseEntity<Void> deleteByCode(@PathVariable Long id, @PathVariable String idEnterprise) {
        accountCatalogueDeleteInputPort.deleteById(id, idEnterprise);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping("/tree/{code}/{idEnterprise}")
    public ResponseEntity<AccountCatalogueListRes> getAccountCatalogueTree(@PathVariable String code,
            @PathVariable String idEnterprise) {
        AccountCatalogue accountCatalogue = accountCatalogueSearchInputPort.getAccountCatalogueTree(code, idEnterprise);
        return ResponseEntity.ok(accountSearchRestMapper.toAccountCatalogueListRes(accountCatalogue));
    }

    @GetMapping("/search/{idEnterprise}")
    public ResponseEntity<List<ItemAccountCatalogueSearchRes>> searchAccountCatalogues(
            @PathVariable String idEnterprise,
            @RequestParam(required = false) String search) {

        List<AccountCatalogue> accounts = (search != null && !search.trim().isEmpty())
                ? accountCatalogueSearchInputPort.getAccountsByCodeOrDescription(idEnterprise, search)
                : accountCatalogueSearchInputPort.getAllAccountsByEnterprise(idEnterprise);

        List<ItemAccountCatalogueSearchRes> response = accounts.stream()
                .map(itemAccountSearchRestMapper::toItemAccountCatalogueSearch)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/trees/{idEnterprise}")
    public ResponseEntity<List<AccountCatalogueListRes>> getAccountCatalogueTrees(
            @PathVariable String idEnterprise) {
        List<AccountCatalogue> accountCatalogueTrees = accountCatalogueSearchInputPort.getAccountCatalogueTrees(idEnterprise);
        List<AccountCatalogueListRes> response = accountCatalogueTrees.stream()
                .map(accountSearchRestMapper::toAccountCatalogueListRes)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAuthority('AC#CS')")
    @PatchMapping("/changeState/{id}/{enterpriseId}")
    public ResponseEntity<AccountCatalogueChangeStateRes> changeState(
            @PathVariable Long id,
            @PathVariable String enterpriseId,
            @RequestParam Boolean status) {
        
        AccountCatalogue updatedAccount = accountCatalogueChangeStateInputPort.changeState(
                id, enterpriseId, status);
        
        AccountCatalogueChangeStateRes response = accountChangeStateRestMapper.toChangeStateResponse(updatedAccount);
        return ResponseEntity.ok(response);
    }

   
    @GetMapping("/auxiliary/{idEnterprise}")
    public ResponseEntity<AuxiliaryAccountListRes> getAuxiliaryAccounts(
            @PathVariable String idEnterprise) {
        
        List<AccountCatalogue> auxiliaryAccounts = accountCatalogueSearchInputPort.getAuxiliaryAccounts(idEnterprise);
        AuxiliaryAccountListRes response = auxiliaryAccountRestMapper.toAuxiliaryAccountListRes(auxiliaryAccounts, idEnterprise);
        
        return ResponseEntity.ok(response);
    }

   
    @GetMapping("/auxiliary/crossing/{idEnterprise}")
    public ResponseEntity<AuxiliaryAccountListRes> getAuxiliaryAccountsWithCrossing(
            @PathVariable String idEnterprise) {
        
        List<AccountCatalogue> auxiliaryAccountsWithCrossing = accountCatalogueSearchInputPort.getAuxiliaryAccountsWithCrossing(idEnterprise);
        AuxiliaryAccountListRes response = auxiliaryAccountRestMapper.toAuxiliaryAccountListRes(auxiliaryAccountsWithCrossing, idEnterprise);
        
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAuthority('AC#ET')")
    @GetMapping("/template/excel")
    public ResponseEntity<Resource> exportAccountCatalogueTemplate(
            @RequestParam String entId) {

        Resource templateFile = accountCatalogueExportInputPort.exportAccountCatalogueTemplate(entId);
        String filename = fileNameGenerator.generateTemplateFileName();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(templateFile);
    }


    @GetMapping("/export/excel")
    public ResponseEntity<Map<String, String>> exportAccountCatalogueAsync(
            @RequestParam @NotBlank(message = "El ID de empresa es requerido") String entId,
            @RequestParam(required = false) String companyName,
            @RequestParam(required = false) Boolean status) {


        AccountCatalogueExportRequest request = AccountCatalogueExportRequest.builder()
                .entId(entId)
                .companyName(companyName)
                .status(status)
                .build();

        String jobId = accountCatalogueExportInputPort.exportAccountCatalogueAsync(request);


        return ResponseEntity.accepted()
                .body(Map.of(
                        "jobId", jobId,
                        "message", "Exportación iniciada exitosamente",
                        "statusEndpoint", "/api/accountCatalogue/export/status/" + jobId
                ));
    }

    @GetMapping("/export/status/{jobId}")
    public ResponseEntity<?> getExportStatus(@PathVariable String jobId) {

        return accountCatalogueExportInputPort.getExportStatus(jobId)
                .map(status -> {
                    return ResponseEntity.ok(status);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PreAuthorize("hasAuthority('AC#E')")
    @GetMapping("/export/download/{jobId}")
    public ResponseEntity<Resource> downloadExportedFile(@PathVariable String jobId) {

        Optional<ExportJobStatus> jobStatus = accountCatalogueExportInputPort.getExportStatus(jobId);

        if (jobStatus.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        ExportJobStatus status = jobStatus.get();

        if (status.getStatus() != ImportStatus.COMPLETED) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        if (status.getFileData() == null) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

        Resource resource = new ByteArrayResource(status.getFileData());

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + status.getFileName() + "\"")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(resource);
    }

  
    @PreAuthorize("hasAuthority('AC#I')")
    @PostMapping("/import/excel")
    public ResponseEntity<Map<String, String>> importFromExcel(
            @RequestParam @NotBlank(message = "El ID de empresa es requerido") String entId,
            @RequestParam MultipartFile file) {


        // Construir request
        AccountCatalogueImportRequest request = AccountCatalogueImportRequest.from(entId, file);

        // Iniciar importación asíncrona
        String jobId = accountCatalogueImportInputPort.importAccountCatalogueAsync(request);


        // Retornar jobId para que el cliente pueda consultar el estado
        return ResponseEntity.accepted()
                .body(Map.of(
                        "jobId", jobId,
                        "message", "Importación iniciada exitosamente",
                        "statusEndpoint", "/api/accountCatalogue/import/status/" + jobId
                ));
    }

    @GetMapping("/import/status/{jobId}")
    public ResponseEntity<?> getImportStatus(
            @PathVariable @NotBlank(message = "El jobId es requerido") String jobId) {

        return accountCatalogueImportInputPort.getImportStatus(jobId)
                .map(status -> {
                    return ResponseEntity.ok(status);
                })
                .orElse(ResponseEntity.notFound().build());
    }

}
