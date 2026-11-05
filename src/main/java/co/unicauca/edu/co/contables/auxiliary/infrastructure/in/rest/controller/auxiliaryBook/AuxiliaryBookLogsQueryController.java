package co.unicauca.edu.co.contables.auxiliary.infrastructure.in.rest.controller.auxiliaryBook;

import co.unicauca.edu.co.contables.auxiliary.application.ports.in.log.IAuxiliaryBookLogQueryPort;
import co.unicauca.edu.co.contables.auxiliary.domain.models.log.AuxiliaryBookLog;
import co.unicauca.edu.co.contables.auxiliary.infrastructure.in.rest.dto.response.ResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @brief Controlador REST de consulta de logs de libros auxiliares.
 *
 * Expone endpoints para listar los registros de log asociados a un
 * libro auxiliar específico, identificándolo por su ID público.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auxiliary-books")
public class AuxiliaryBookLogsQueryController {

    private final IAuxiliaryBookLogQueryPort auxiliaryBookLogQueryPort;

    @GetMapping("/logs")
    public ResponseEntity<ResponseDTO<List<AuxiliaryBookLog>>> getAuxiliaryBookLogsById(@RequestParam String auxiliaryBookId){
        List<AuxiliaryBookLog> response = this.auxiliaryBookLogQueryPort.findAllByAuxiliaryBookPublicId(auxiliaryBookId);
        ResponseDTO<List<AuxiliaryBookLog>> responseDTO = ResponseDTO.<List<AuxiliaryBookLog>>builder()
                .data(response)
                .statusCode(200)
                .message("Logs successful obtained of Auxiliary Book with ID: "+auxiliaryBookId)
                .build();
        return responseDTO.of();
    }
}
