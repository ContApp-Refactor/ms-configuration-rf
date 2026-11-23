package co.unicauca.edu.co.contables.auxiliary.infrastructure.in.rest.controller.auxiliaryBook;

import co.unicauca.edu.co.contables.auxiliary.domain.models.history.AuxiliaryBookHistory;
import co.unicauca.edu.co.contables.auxiliary.domain.ports.AuxiliaryBookHistory.IAuxiliaryBookHistoryQueryRepositoryPort;
import co.unicauca.edu.co.contables.auxiliary.infrastructure.in.rest.dto.response.AuxiliaryBookHistoryResponseDTO;
import co.unicauca.edu.co.contables.auxiliary.infrastructure.in.rest.dto.response.ResponseDTO;
import co.unicauca.edu.co.contables.auxiliary.infrastructure.in.rest.mapper.IAuxiliaryBookHistoryResponseMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @brief Controlador REST de consulta del historial de libros auxiliares.
 *
 * Expone endpoints para consultar paginadamente el historial de libros
 * auxiliares filtrado por empresa, convirtiendo los resultados del
 * dominio a DTOs mediante el mapper correspondiente.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auxiliary-books")
public class AuxiliaryBookHistoryQueryController {

    private final IAuxiliaryBookHistoryQueryRepositoryPort auxiliaryBookHistoryQueryRepositoryPort;
    private final IAuxiliaryBookHistoryResponseMapper auxiliaryBookHistoryResponseMapper;

    @GetMapping("/history")
    public ResponseDTO<Page<AuxiliaryBookHistoryResponseDTO>> getAuxiliaryBookHistoryByEnterprise(@RequestParam String enterpriseId, Pageable pageable){
        Page<AuxiliaryBookHistory> historyPage = this.auxiliaryBookHistoryQueryRepositoryPort.findPageByEntId(enterpriseId,pageable);
        Page<AuxiliaryBookHistoryResponseDTO> response = historyPage.map(auxiliaryBookHistoryResponseMapper::toDtoResponse);
        return ResponseDTO.<Page<AuxiliaryBookHistoryResponseDTO>>builder()
                .data(response)
                .statusCode(200)
                .message("History for enterprise ("+enterpriseId+") successful obtained")
                .build();
    }
}

