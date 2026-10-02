package co.unicauca.edu.co.contables.accounting.facture.application.service.skeleton.controller;

import co.unicauca.edu.co.contables.accounting.facture.application.service.skeleton.dto.*;
import co.unicauca.edu.co.contables.accounting.facture.application.service.skeleton.model.SkeletonFacture;
import co.unicauca.edu.co.contables.accounting.facture.application.service.skeleton.model.SkeletonReturn;
import co.unicauca.edu.co.contables.accounting.facture.application.service.skeleton.repository.SkeletonFactureRepository;
import co.unicauca.edu.co.contables.accounting.facture.application.service.skeleton.service.SkeletonEventService;
import co.unicauca.edu.co.contables.accounting.facture.application.service.skeleton.service.SkeletonFactureService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/factures/skeleton")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Skeleton Factures", description = "API para gestión de facturas prototipo")
public class SkeletonFactureController {
    
    private final SkeletonFactureService factureService;
    private final SkeletonEventService eventService;
    private final SkeletonFactureRepository factureRepository;
    
    @PostMapping("/purchase")
    @Operation(summary = "Crear factura de compra")
    public ResponseEntity<SkeletonFactureDetailDto> createPurchase(
            @Valid @RequestBody SkeletonFactureRequestDto request) {
        
        log.info("Recibida solicitud de compra para factura: {} con configuración: {}", 
                request.getFactCode(), request.getInventoryConfigType());
        
        // Guardar factura
        SkeletonFactureDetailDto savedFacture = factureService.createPurchase(request);
        
        // Obtener entidad completa para publicar eventos
        SkeletonFacture facture = factureRepository.findByIdWithProducts(savedFacture.getId())
                .orElseThrow();
        
        // Publicar eventos de forma asíncrona según la configuración
        eventService.publishPurchaseEvents(facture, request.getInventoryConfigType());
        
        return ResponseEntity.status(HttpStatus.CREATED).body(savedFacture);
    }
    
    @PostMapping("/sale")
    @Operation(summary = "Crear factura de venta con recibo")
    public ResponseEntity<SkeletonFactureDetailDto> createSale(
            @Valid @RequestBody SkeletonFactureRequestDto request) {
        
        log.info("Recibida solicitud de venta para factura: {} con configuración: {}", 
                request.getFactCode(), request.getInventoryConfigType());
        
        // Guardar factura
        SkeletonFactureDetailDto savedFacture = factureService.createFacture(request);
        
        // Obtener entidad completa para publicar eventos
        SkeletonFacture facture = factureRepository.findByIdWithProducts(savedFacture.getId())
                .orElseThrow();
        
        // SIEMPRE publicar evento de recibo para cartera
        eventService.publishSaleReceiptEvent(facture);
        
        // Publicar eventos de inventario según la configuración (PEPS o WeightedAverage)
        eventService.publishSaleEvents(facture, request.getInventoryConfigType());
        
        return ResponseEntity.status(HttpStatus.CREATED).body(savedFacture);
    }
    
    @PostMapping("/return-on-sale")
    @Operation(summary = "Procesar devolución en venta")
    public ResponseEntity<String> processReturnOnSale(
            @Valid @RequestBody ReturnRequestDto returnRequest) {
        
        log.info("Recibida solicitud de devolución en venta para factura: {} con configuración: {}", 
                returnRequest.getFactCode(), returnRequest.getInventoryConfigType());
        
        // Validar y guardar devoluciones
        factureService.processReturn(returnRequest, SkeletonReturn.ReturnType.RETURN_ON_SALE);
        
        // Publicar eventos uno por uno según la configuración
        for (ReturnRequestDto.ProductReturnDto product : returnRequest.getProducts()) {
            eventService.publishReturnOnSaleEvents(returnRequest.getFactCode(), product, 
                    returnRequest.getInventoryConfigType());
        }
        
        return ResponseEntity.ok("Devolución en venta procesada exitosamente");
    }
    
    @PostMapping("/return-on-purchase")
    @Operation(summary = "Procesar devolución en compra")
    public ResponseEntity<String> processReturnOnPurchase(
            @Valid @RequestBody ReturnRequestDto returnRequest) {
        
        log.info("Recibida solicitud de devolución en compra para factura: {} con configuración: {}", 
                returnRequest.getFactCode(), returnRequest.getInventoryConfigType());
        
        // Validar y guardar devoluciones
        factureService.processReturn(returnRequest, SkeletonReturn.ReturnType.RETURN_ON_PURCHASE);
        
        // Publicar eventos uno por uno según la configuración
        for (ReturnRequestDto.ProductReturnDto product : returnRequest.getProducts()) {
            eventService.publishReturnOnPurchaseEvents(returnRequest.getFactCode(), product,
                    returnRequest.getInventoryConfigType());
        }
        
        return ResponseEntity.ok("Devolución en compra procesada exitosamente");
    }
    
    @PostMapping("/non-commercial-entry")
    @Operation(summary = "Crear entrada no comercial")
    public ResponseEntity<SkeletonFactureDetailDto> createNonCommercialEntry(
            @Valid @RequestBody SkeletonFactureRequestDto request) {
        
        log.info("Recibida solicitud de entrada no comercial para factura: {} con configuración: {}", 
                request.getFactCode(), request.getInventoryConfigType());
        
        SkeletonFactureDetailDto savedFacture = factureService.createFacture(request);
        
        SkeletonFacture facture = factureRepository.findByIdWithProducts(savedFacture.getId())
                .orElseThrow();
        
        eventService.publishNonCommercialEntryEvents(facture, request.getInventoryConfigType(),request.getTagTitle());
        
        return ResponseEntity.status(HttpStatus.CREATED).body(savedFacture);
    }
    
    @PostMapping("/non-commercial-exit")
    @Operation(summary = "Crear salida no comercial")
    public ResponseEntity<SkeletonFactureDetailDto> createNonCommercialExit(
            @Valid @RequestBody SkeletonFactureRequestDto request) {
        
        log.info("Recibida solicitud de salida no comercial para factura: {} con configuración: {}", 
                request.getFactCode(), request.getInventoryConfigType());
        
        SkeletonFactureDetailDto savedFacture = factureService.createFacture(request);
        
        SkeletonFacture facture = factureRepository.findByIdWithProducts(savedFacture.getId())
                .orElseThrow();
        
        eventService.publishNonCommercialExitEvents(facture, request.getInventoryConfigType(),request.getTagTitle());
        
        return ResponseEntity.status(HttpStatus.CREATED).body(savedFacture);
    }
    
    @GetMapping
    @Operation(summary = "Listar todas las facturas (resumen)")
    public ResponseEntity<List<SkeletonFactureSummaryDto>> getAllFactures() {
        List<SkeletonFactureSummaryDto> factures = factureService.getAllFactures();
        return ResponseEntity.ok(factures);
    }

    @PutMapping("/purchase/{id}")
    @Operation(summary = "Actualizar factura de compra y sincronizar Tesoreria")
    public ResponseEntity<SkeletonFactureDetailDto> updatePurchase(@PathVariable Long id,
            @Valid @RequestBody SkeletonFactureRequestDto request) {
        return ResponseEntity.ok(factureService.updatePurchase(id, request));
    }

    @PostMapping("/purchase/{id}/void")
    @Operation(summary = "Anular factura de compra y sincronizar Tesoreria")
    public ResponseEntity<SkeletonFactureDetailDto> voidPurchase(@PathVariable Long id) {
        return ResponseEntity.ok(factureService.voidPurchase(id));
    }

    @PostMapping("/purchase/replay")
    @Operation(summary = "Republicar facturas de compra para sincronización de Tesorería")
    public ResponseEntity<Integer> replayPurchases(@RequestParam String enterpriseId) {
        return ResponseEntity.ok(factureService.replayPurchases(enterpriseId));
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Obtener factura por ID con productos")
    public ResponseEntity<SkeletonFactureDetailDto> getFactureById(@PathVariable Long id) {
        SkeletonFactureDetailDto facture = factureService.getFactureById(id);
        return ResponseEntity.ok(facture);
    }
    
    @GetMapping("/by-code/{factCode}")
    @Operation(summary = "Obtener factura por código con productos")
    public ResponseEntity<SkeletonFactureDetailDto> getFactureByCode(@PathVariable Long factCode) {
        SkeletonFactureDetailDto facture = factureService.getFactureByFactCode(factCode);
        return ResponseEntity.ok(facture);
    }
    
    @GetMapping("/{factCode}/returns")
    @Operation(summary = "Obtener historial de devoluciones de una factura")
    public ResponseEntity<List<SkeletonReturn>> getReturns(@PathVariable Long factCode) {
        List<SkeletonReturn> returns = factureService.getReturnsByFactCode(factCode);
        return ResponseEntity.ok(returns);
    }
    
    @GetMapping("/{factCode}/products/{productId}/returned-quantity")
    @Operation(summary = "Obtener cantidad total devuelta de un producto")
    public ResponseEntity<Integer> getTotalReturnedQuantity(
            @PathVariable Long factCode,
            @PathVariable Long productId) {
        
        Integer total = factureService.getTotalReturnedQuantity(factCode, productId);
        return ResponseEntity.ok(total);
    }
}
