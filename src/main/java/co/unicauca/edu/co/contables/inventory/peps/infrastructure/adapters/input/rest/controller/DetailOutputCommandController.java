package co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.input.rest.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import co.unicauca.edu.co.contables.inventory.peps.application.ports.input.IDetailOutputCommandPort;
import co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.input.rest.dto.ResponseDTO;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/kardex/peps")
public class DetailOutputCommandController {

    private final IDetailOutputCommandPort detailOutputCommandPort;

    @DeleteMapping("/details-output")
    public ResponseEntity<ResponseDTO<Void>> deleteAllDetailsOutput() {
        detailOutputCommandPort.deleteAll();

        ResponseDTO<Void> responseDto = ResponseDTO.<Void>builder()
            .data(null)
            .status(200)
            .message("All details output records deleted successfully")
            .build();
        return responseDto.of();
    }       

}