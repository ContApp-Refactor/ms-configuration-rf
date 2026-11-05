package co.unicauca.edu.co.contables.inventory.kardex.infrastructure.adapters.output.remoteSync.adapter;

import java.util.List;

import org.springframework.stereotype.Component;

import co.unicauca.edu.co.contables.inventory.kardex.domain.model.Kardex;
import co.unicauca.edu.co.contables.inventory.kardex.domain.port.kardex.IKardexExternalClientPort;
import co.unicauca.edu.co.contables.inventory.kardex.infrastructure.adapters.input.rest.dto.request.KardexBatchDtoRequest;
import co.unicauca.edu.co.contables.inventory.kardex.infrastructure.adapters.output.remoteSync.mapper.IKardexExternalClientMapper;
import co.unicauca.edu.co.contables.inventory.kardex.infrastructure.adapters.output.remoteSync.mapper.IKardexExternalClientMapperHelper;
import co.unicauca.edu.co.contables.inventory.stock.domain.port.IFormatterResultOutputPort;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @brief In-process adapter for the PEPS module (paso 7).
 *
 * Replaces the former WebClient proxy to lb://INVENTORYPEPS: PEPS already
 * lives in this monolith (módulo 6), so the kardex records are read from
 * {@code inventory.peps}'s query port and translated into this module's wire
 * DTO, which the existing MapStruct mapper turns into a kardex domain model.
 */
@Component("kardexExternalClientAdapter")
@RequiredArgsConstructor
@Slf4j
public class KardexExternalClientAdapter implements IKardexExternalClientPort {

    private final IKardexExternalClientMapper kardexExternalClientMapper;
    private final IKardexExternalClientMapperHelper helper;
    private final IFormatterResultOutputPort formatterResultOutputPort;

    @SuppressWarnings("null")
    @Override
    public List<Kardex> findKardexByEnterpriseId(String enterpriseId) {
        try {
            log.info("Fetching kardex records for enterprise: {} (in-process peps)", enterpriseId);
            List<KardexBatchDtoRequest> payload = helper.loadFromPeps(enterpriseId);
            if (payload == null) {
                log.warn("No data received from peps for enterprise: {}", enterpriseId);
                formatterResultOutputPort.returnErrorGenericResponse(500,
                        "No data received from external kardex service");
            }
            log.info("Retrieved {} kardex records from peps", payload.size());
            return payload.stream().map(kardexExternalClientMapper::toDomain).toList();
        } catch (Exception e) {
            log.error("Unexpected error reading kardex records from peps: {}", e.getMessage());
            formatterResultOutputPort.returnErrorGenericResponse(500,
                    "Unexpected error communicating with kardex external service");
        }
        return List.of();
    }
}
