package co.unicauca.edu.co.contables.configuration.costCenters.presentation.controller;

import co.unicauca.edu.co.contables.configuration.costCenters.domain.models.CostCenter;
import co.unicauca.edu.co.contables.configuration.costCenters.domain.mapper.CostCenterDomainMapper;
import co.unicauca.edu.co.contables.configuration.costCenters.domain.services.ICostCenterService;
import co.unicauca.edu.co.contables.configuration.costCenters.presentation.DTO.request.CostCenterCreateReq;
import co.unicauca.edu.co.contables.configuration.costCenters.presentation.DTO.request.CostCenterUpdateReq;
import co.unicauca.edu.co.contables.configuration.costCenters.presentation.DTO.response.CostCenterRes;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

 

@RestController
@RequestMapping("/api/config/cost-centers")
@RequiredArgsConstructor
public class CostCenterController {

    private final ICostCenterService service;
    private final CostCenterDomainMapper mapper;

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



    @GetMapping("/findAll/{enterpriseId}")
    public ResponseEntity<Page<CostCenterRes>> listHierarchical(
            @PathVariable String enterpriseId,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "30") Integer size) {
        Page<CostCenter> pageResult = service.findAllByEnterpriseHierarchical(enterpriseId, page, size);
        Page<CostCenterRes> mapped = pageResult.map(mapper::toRes);
        return ResponseEntity.ok(mapped);
    }

    @GetMapping("/findAllByStatus/{enterpriseId}")
    public ResponseEntity<Page<CostCenterRes>> listByStatus(
            @PathVariable String enterpriseId,
            @RequestParam Boolean status,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "30") Integer size) {
        Page<CostCenter> pageResult = service.findAllByEnterpriseAndStatus(enterpriseId, status, page, size);
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
    public ResponseEntity<CostCenterRes> softDelete(
            @PathVariable Long id,
            @PathVariable String enterpriseId) {
        CostCenter deleted = service.softDelete(id, enterpriseId);
        return ResponseEntity.ok(mapper.toRes(deleted));
    }
}
