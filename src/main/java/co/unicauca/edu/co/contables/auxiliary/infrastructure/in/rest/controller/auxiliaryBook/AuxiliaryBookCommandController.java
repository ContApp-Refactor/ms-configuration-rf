package co.unicauca.edu.co.contables.auxiliary.infrastructure.in.rest.controller.auxiliaryBook;

import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import co.unicauca.edu.co.contables.auxiliary.application.ports.in.auxiliaryBook.IAuxiliaryBookCommandPort;
import co.unicauca.edu.co.contables.auxiliary.application.ports.in.export.IExportReportPort;
import co.unicauca.edu.co.contables.auxiliary.domain.models.core.AuxiliaryBook;
import co.unicauca.edu.co.contables.auxiliary.infrastructure.in.rest.dto.request.ExportAuxiliaryBookRequest;
import co.unicauca.edu.co.contables.auxiliary.infrastructure.in.rest.dto.request.GenerateAuxiliaryBookRequest;
import co.unicauca.edu.co.contables.auxiliary.infrastructure.in.rest.dto.response.AuxiliaryBookResponseDTO;
import co.unicauca.edu.co.contables.auxiliary.infrastructure.in.rest.dto.response.ResponseDTO;
import co.unicauca.edu.co.contables.auxiliary.infrastructure.in.rest.mapper.IAuxiliaryBookRestMapper;
import co.unicauca.edu.co.contables.auxiliary.infrastructure.in.rest.mapper.IExportRestMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @brief Controlador REST de comandos para libros auxiliares.
 *
 * Expone los endpoints HTTP para registrar nuevos libros auxiliares
 * y exportarlos en distintos formatos. Delega la lógica a los puertos
 * de aplicación y utiliza mappers para transformar DTOs de la capa
 * REST al dominio y viceversa.
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auxiliary-books")
public class AuxiliaryBookCommandController {

    private final IAuxiliaryBookCommandPort auxiliaryBookCommandPort;
    private final IAuxiliaryBookRestMapper auxiliaryBookRestMapper;

    private final IExportReportPort jasperReportGeneratorPort;
    private final IExportRestMapper exportRestMapper;

    private final ObjectMapper objectMapper;

    @GetMapping("/test")
    public ResponseEntity<?> test() {
        return ResponseEntity.ok("Auxiliary Book Command Controller is working!");
    }

    @PostMapping("/register")
    public ResponseEntity<ResponseDTO<AuxiliaryBookResponseDTO>> registerAuxiliaryBook(@Validated @RequestBody GenerateAuxiliaryBookRequest request) {
        try {
            log.info("[AuxBook] /register payload completo: {}", objectMapper.writeValueAsString(request));
        } catch (JsonProcessingException e) {
            log.warn("[AuxBook] No se pudo serializar el request para log: {}", e.getMessage());
        }
        AuxiliaryBook auxBookRegistered = this.auxiliaryBookCommandPort.registerAuxiliaryBook(this.auxiliaryBookRestMapper.toDomain(request));
        List<?> accountingData = this.auxiliaryBookCommandPort.genereteAuxiliaryBookInfo(auxBookRegistered);
        AuxiliaryBookResponseDTO response = new  AuxiliaryBookResponseDTO(auxBookRegistered, accountingData);
        ResponseDTO<AuxiliaryBookResponseDTO> responseDTO = ResponseDTO.<AuxiliaryBookResponseDTO>builder()
                .data(response)
                .statusCode(200)
                .message("Auxiliary Book of type "+request.getType().name()+" successfully registered!")
                .build();
        return responseDTO.of();
    }

    @PostMapping("/export")
    public ResponseEntity<byte[]> exportReport(@RequestBody ExportAuxiliaryBookRequest request) {
        byte[] reportBytes = this.jasperReportGeneratorPort.exportReport(this.exportRestMapper.toDomain(request));
        HttpHeaders headers = this.jasperReportGeneratorPort.getHttpHeaders(request.getFormat(), request.getAuxiliaryBook().getType());
        return new ResponseEntity<>(reportBytes, headers, HttpStatus.OK);
    }
}
