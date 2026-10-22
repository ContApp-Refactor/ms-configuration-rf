package co.unicauca.edu.co.contables.configuration.enterprise.presentation.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import co.unicauca.edu.co.contables.commons.security.IJwtUtils;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.dto.EnterpriseInfoDto;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.mapper.EnterpriseDomainMapper;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.mapper.EnterpriseTypeDomainMapper;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.mapper.TaxLiabilityDomainMapper;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.mapper.TaxPayerTypeDomainMapper;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.models.Enterprise;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.enums.StateEnum;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.services.IEnterpriseService;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.services.IEnterpriseTypeService;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.services.ITaxLiabilityService;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.services.ITaxPayerTypeService;
import co.unicauca.edu.co.contables.configuration.enterprise.presentation.DTO.request.EnterpriseCreateReq;
import co.unicauca.edu.co.contables.configuration.enterprise.presentation.DTO.request.EnterpriseUpdateReq;
import co.unicauca.edu.co.contables.configuration.enterprise.presentation.DTO.request.UpdateInventoryConfigReq;
import co.unicauca.edu.co.contables.configuration.enterprise.presentation.DTO.response.EnterpriseRes;
import co.unicauca.edu.co.contables.configuration.enterprise.presentation.DTO.response.EnterpriseTypeRes;
import co.unicauca.edu.co.contables.configuration.enterprise.presentation.DTO.response.TaxLiabilityRes;
import co.unicauca.edu.co.contables.configuration.enterprise.presentation.DTO.response.TaxPayerTypeRes;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @brief Controlador REST para la gestión de empresas.
 *
 * Expone endpoints para crear, actualizar, consultar y eliminar empresas,
 * así como para consultar los catálogos de responsabilidades tributarias,
 * tipos de contribuyente y tipos de empresa que se usan al crearlas.
 */
@RequestMapping("/api/enterprises")
@RestController
@RequiredArgsConstructor
@Validated
@Slf4j
@PreAuthorize("hasRole('admin_client') or hasRole('user_client') or hasRole('super_client')")
public class EnterpriseController {

    private final IEnterpriseService service;
    private final EnterpriseDomainMapper mapper;

    private final ITaxLiabilityService taxLiabilityService;
    private final TaxLiabilityDomainMapper taxLiabilityMapper;

    private final ITaxPayerTypeService taxPayerTypeService;
    private final TaxPayerTypeDomainMapper taxPayerTypeMapper;

    private final IEnterpriseTypeService enterpriseTypeService;
    private final EnterpriseTypeDomainMapper enterpriseTypeMapper;

    private final IJwtUtils jwtUtils;

    @Operation(summary = "Obtener todas las responsabilidades tributarias")
    @GetMapping("/taxliabilities")
    public ResponseEntity<List<TaxLiabilityRes>> getAllTaxLiability() {
        return ResponseEntity.ok(taxLiabilityMapper.toResList(taxLiabilityService.getAll()));
    }

    @Operation(summary = "Obtener todos los tipos de contribuyentes")
    @GetMapping("/taxpayertype")
    public ResponseEntity<List<TaxPayerTypeRes>> getTaxPayer() {
        return ResponseEntity.ok(taxPayerTypeMapper.toResList(taxPayerTypeService.getAll()));
    }

    @Operation(summary = "Obtener todos los tipos de empresa")
    @GetMapping("/enterprisetypes")
    public ResponseEntity<List<EnterpriseTypeRes>> getAllEnterpriseTypes() {
        return ResponseEntity.ok(enterpriseTypeMapper.toResList(enterpriseTypeService.getAll()));
    }

    @Operation(summary = "Obtener todas las empresas activas")
    @GetMapping("/")
    public ResponseEntity<List<EnterpriseInfoDto>> getAllEnterprises() {
        return ResponseEntity.ok(service.findAllActive());
    }

    @Operation(summary = "Obtener todas las empresas inactivas")
    @GetMapping("/inactive")
    public ResponseEntity<List<EnterpriseInfoDto>> getAllEnterprisesInactive() {
        return ResponseEntity.ok(service.findAllInactive());
    }

    @Operation(summary = "Crear una empresa")
    @PostMapping("/")
    public ResponseEntity<EnterpriseRes> createEnterprise(@Valid @RequestBody EnterpriseCreateReq request) {
        Enterprise enterprise = mapper.toDomain(request);
        enterprise.setIdUser(jwtUtils.getId());
        enterprise = service.create(enterprise);
        return ResponseEntity.ok(mapper.toRes(enterprise));
    }

    @Operation(summary = "Actualizar una empresa por id")
    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateEnterprise(@PathVariable("id") UUID id,
            @Valid @RequestBody EnterpriseUpdateReq request) {
        Enterprise enterpriseExist = service.findById(id);
        if (enterpriseExist == null) {
            return ResponseEntity.badRequest().build();
        }
        service.update(id, mapper.toDomain(request));
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Actualizar el estado de una empresa por id")
    @PutMapping("/update/status/{id}/{state}")
    public ResponseEntity<?> updateEnterpriseStatus(@PathVariable("id") UUID id, @PathVariable("state") String state) {
        try {
            StateEnum stateEnum;
            switch (state) {
                case "ACTIVE" -> stateEnum = StateEnum.ACTIVE;
                case "INACTIVE" -> stateEnum = StateEnum.INACTIVE;
                case "SUSPENDED" -> stateEnum = StateEnum.SUSPENDED;
                default -> {
                    return ResponseEntity.badRequest().build();
                }
            }
            service.updateStatus(id, stateEnum);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @Operation(summary = "Obtener una empresa por ID")
    @GetMapping("/enterprise/{id}")
    public ResponseEntity<EnterpriseRes> getEnterpriseById(@PathVariable("id") UUID id) {
        return ResponseEntity.ok(mapper.toRes(service.findById(id)));
    }

    /**
     * Elimina (soft delete) una empresa por su ID marcándola como INACTIVE.
     * Compatible con el frontend que llama DELETE /api/enterprises/enterprise/{id}
     */
    @DeleteMapping("/enterprise/{id}")
    public ResponseEntity<Void> deleteEnterprise(@PathVariable("id") UUID id) {
        Enterprise enterprise = service.findById(id);
        if (enterprise == null) {
            return ResponseEntity.notFound().build();
        }
        service.updateStatus(id, StateEnum.INACTIVE);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/enterprise/activate/{id}")
    public ResponseEntity<Void> activateEnterprise(@PathVariable("id") UUID id) {
        Enterprise enterprise = service.findById(id);
        if (enterprise == null) {
            return ResponseEntity.notFound().build();
        }
        service.updateStatus(id, StateEnum.ACTIVE);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/inventory-config/{id}")
    public ResponseEntity<Void> inventoryConfigurationType(@PathVariable("id") UUID id,
            @Valid @RequestBody UpdateInventoryConfigReq request) {
        Enterprise enterprise = service.findById(id);
        if (enterprise == null) {
            return ResponseEntity.notFound().build();
        }
        service.updateInventoryConfiguration(id, request.getInventoryConfigType());
        return ResponseEntity.noContent().build();
    }

    /**
     * Elimina permanentemente una empresa de la base de datos (hard delete).
     * Esta operación no se puede deshacer.
     */
    @Operation(summary = "Eliminar permanentemente una empresa")
    @DeleteMapping("/enterprise/hard/{id}")
    public ResponseEntity<Void> deleteEnterpriseHard(@PathVariable("id") UUID id) {
        Enterprise enterprise = service.findById(id);
        if (enterprise == null) {
            return ResponseEntity.notFound().build();
        }
        try {
            service.delete(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error("Error al eliminar permanentemente la empresa con id: {}", id, e);
            return ResponseEntity.status(500).build();
        }
    }
}
