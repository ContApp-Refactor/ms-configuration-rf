package co.unicauca.edu.co.contables.accounting.facture.infraestructure.adapters.input.rest;

import java.io.ByteArrayInputStream;

import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import co.unicauca.edu.co.contables.accounting.facture.application.ports.input.CreateFactureUseCase;
import co.unicauca.edu.co.contables.accounting.facture.application.ports.input.GenerateFacturePDFUseCase;
import co.unicauca.edu.co.contables.accounting.facture.application.ports.input.GetFactureUseCase;
import co.unicauca.edu.co.contables.accounting.facture.application.ports.input.ListFactureUseCase;
import co.unicauca.edu.co.contables.accounting.facture.domain.model.Facture;
import co.unicauca.edu.co.contables.accounting.facture.infraestructure.adapters.input.rest.data.request.FactureCreateRequest;
import co.unicauca.edu.co.contables.accounting.facture.infraestructure.adapters.input.rest.data.request.FactureListRequest;
import co.unicauca.edu.co.contables.accounting.facture.infraestructure.adapters.input.rest.data.response.FactureGetResponse;
import co.unicauca.edu.co.contables.accounting.facture.infraestructure.adapters.input.rest.data.response.FactureListResponse;
import co.unicauca.edu.co.contables.accounting.facture.infraestructure.adapters.input.rest.mapper.FactureRestMapper;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequestMapping("/api/factures/")
@RequiredArgsConstructor
public class FactureRestAdapter {
    private final CreateFactureUseCase createFactureUseCase;
    private final GetFactureUseCase getFactureUseCase;
    private final ListFactureUseCase listFactureUseCase;
    private final GenerateFacturePDFUseCase generateFacturePDFUseCase;

    private final FactureRestMapper factureRestMapper;

    @PostMapping("/")
    @CircuitBreaker(name = "external", fallbackMethod = "fallback")
    @Operation(summary = "Crear una factura", description = "Crea una nueva factura y genera un archivo PDF asociado.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Factura creada exitosamente, PDF generado", content = @Content(mediaType = "application/pdf")),
            @ApiResponse(responseCode = "500", description = "Error interno al crear la factura o generar el PDF", content = @Content)
    })
    public ResponseEntity<InputStreamResource> createFacture(
            @RequestBody @Valid FactureCreateRequest factureCreateRequest) {

        // Convertir la solicitud en una entidad de Facture
        Facture facture = this.factureRestMapper.toFacture(factureCreateRequest);

        // Generar el PDF inicial
        byte[] pdfBytes = this.generateFacturePDFUseCase.generetePDFFacture(facture);
        if (pdfBytes == null || pdfBytes.length == 0) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }

        // Crear la factura en la base de datos
        Facture facture2 = this.createFactureUseCase.createFacture(facture);
        facture.setFactId(facture2.getFactId());

        // Regenerar el PDF con el ID actualizado
        pdfBytes = this.generateFacturePDFUseCase.generetePDFFacture(facture);
        ByteArrayInputStream bais = new ByteArrayInputStream(pdfBytes);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=facture_" + facture.getFactCode() + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .contentLength(pdfBytes.length)
                .body(new InputStreamResource(bais));
    }

    @PostMapping("/generatePreview")
    @CircuitBreaker(name = "external", fallbackMethod = "fallback")
    @Operation(summary = "Generar previsualización de factura", description = "Genera un PDF de previsualización de una factura sin almacenarla en la base de datos.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Previsualización de factura generada exitosamente", content = @Content(mediaType = "application/pdf")),
            @ApiResponse(responseCode = "500", description = "Error interno al generar la previsualización", content = @Content)
    })
    public ResponseEntity<InputStreamResource> generateFacturePreview(
            @RequestBody @Valid FactureCreateRequest facturePreviewRequest) {

        // Convertir la solicitud en una entidad de Facture
        Facture facture = this.factureRestMapper.toFacture(facturePreviewRequest);

        // Generar el PDF de previsualización
        byte[] pdfBytes = this.generateFacturePDFUseCase.generetePDFFacture(facture);
        if (pdfBytes == null || pdfBytes.length == 0) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }

        ByteArrayInputStream bais = new ByteArrayInputStream(pdfBytes);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=facture_preview.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .contentLength(pdfBytes.length)
                .body(new InputStreamResource(bais));
    }

    @GetMapping("/facture/")
    @Operation(summary = "Generar QR de una factura", description = "Genera un código QR para una factura específica y lo devuelve como una imagen JPEG.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Código QR generado exitosamente", content = @Content(mediaType = "image/jpeg")),
            @ApiResponse(responseCode = "500", description = "Error interno al generar el QR", content = @Content)
    })
    public ResponseEntity<byte[]> generateFactureQR(@RequestParam @Valid long factId) {

        // Obtener la factura usando el ID proporcionado
        Facture facture = this.getFactureUseCase.getFactureBy(factId);
        if (facture == null) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }

        // Generar el QR como un arreglo de bytes
        byte[] imageBytes = this.generateFacturePDFUseCase.generateFactureQR(facture);
        if (imageBytes == null) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }

        // Configurar la respuesta para descargar el QR como un archivo JPEG
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=facture_qr.jpg")
                .contentType(MediaType.IMAGE_JPEG)
                .contentLength(imageBytes.length)
                .body(imageBytes);
    }

    @GetMapping("/")
    @Operation(summary = "Obtener factura por ID", description = "Recupera los detalles de una factura específica utilizando su ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Factura recuperada exitosamente", content = @Content(mediaType = "application/json", schema = @Schema(implementation = FactureGetResponse.class))),
            @ApiResponse(responseCode = "500", description = "Error interno al recuperar la factura", content = @Content)
    })
    public ResponseEntity<FactureGetResponse> getFactureBy(@RequestParam @Valid long factId) {

        // Recuperar la factura usando el caso de uso correspondiente
        Facture facture = this.getFactureUseCase.getFactureBy(factId);

        // Mapear la entidad a la respuesta de DTO y devolverla con estado HTTP 200
        return new ResponseEntity<>(this.factureRestMapper.toGetFactureResponse(facture), HttpStatus.OK);
    }

    @GetMapping("/all")
    @Operation(summary = "Obtener todas las facturas de una empresa", description = "Recupera una lista paginada de facturas asociadas a una empresa específica.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Facturas recuperadas exitosamente", content = @Content(mediaType = "application/json", schema = @Schema(implementation = FactureListResponse.class))),
            @ApiResponse(responseCode = "500", description = "Error interno al recuperar las facturas", content = @Content)
    })
    public ResponseEntity<FactureListResponse> getAllFacturesBy(@RequestBody @Valid FactureListRequest request) {

        // Crear una configuración de paginación basada en la página solicitada
        Pageable page = PageRequest.of(request.getNumPage(), 10);

        // Obtener las facturas paginadas mediante el caso de uso
        Page<Facture> pageFactures = this.listFactureUseCase.getAllFacturesBy(request.getEntId(), page);

        // Mapear las facturas a una respuesta de tipo DTO y devolverla con estado HTTP
        // 200
        return new ResponseEntity<>(this.factureRestMapper.toFactureListResponse(pageFactures), HttpStatus.OK);
    }

    @GetMapping("/sales")
    @Operation(summary = "Obtener todas las facturas de ventas de una empresa", description = "Recupera una lista paginada de facturas de ventas asociadas a una empresa específica.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Facturas de ventas recuperadas exitosamente", content = @Content(mediaType = "application/json", schema = @Schema(implementation = FactureListResponse.class))),
            @ApiResponse(responseCode = "500", description = "Error interno al recuperar las facturas de ventas", content = @Content)
    })
    public ResponseEntity<FactureListResponse> getAllSalesFacturesBy(@RequestBody @Valid FactureListRequest request) {

        // Crear una configuración de paginación basada en la página solicitada
        Pageable page = PageRequest.of(request.getNumPage(), 10);

        // Obtener las facturas de ventas paginadas mediante el caso de uso
        Page<Facture> pageFactures = this.listFactureUseCase.getAllSalesFacturesBy(request.getEntId(), page);

        // Mapear las facturas a una respuesta de tipo DTO y devolverla con estado HTTP
        // 200
        return new ResponseEntity<>(this.factureRestMapper.toFactureListResponse(pageFactures), HttpStatus.OK);
    }

    @GetMapping("/shopping")
    @Operation(summary = "Obtener todas las facturas de compras de una empresa", description = "Recupera una lista paginada de facturas de compras asociadas a una empresa específica.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Facturas de compras recuperadas exitosamente", content = @Content(mediaType = "application/json", schema = @Schema(implementation = FactureListResponse.class))),
            @ApiResponse(responseCode = "500", description = "Error interno al recuperar las facturas de compras", content = @Content)
    })
    public ResponseEntity<FactureListResponse> getAllShoppingFacturesBy(
            @RequestBody @Valid FactureListRequest request) {

        // Crear una configuración de paginación basada en la página solicitada
        Pageable page = PageRequest.of(request.getNumPage(), 10);

        // Obtener las facturas de compras paginadas mediante el caso de uso
        Page<Facture> pageFactures = this.listFactureUseCase.getAllShoppingFacturesBy(request.getEntId(), page);

        // Mapear las facturas a una respuesta de tipo DTO y devolverla con estado HTTP
        // 200
        return new ResponseEntity<>(this.factureRestMapper.toFactureListResponse(pageFactures), HttpStatus.OK);
    }

}
