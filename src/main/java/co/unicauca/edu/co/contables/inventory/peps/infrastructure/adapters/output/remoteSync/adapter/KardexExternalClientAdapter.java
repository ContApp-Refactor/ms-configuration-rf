package co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.output.remoteSync.adapter;

import java.util.List;

import org.springframework.stereotype.Component;

import co.unicauca.edu.co.contables.inventory.kardex.domain.model.Kardex;
import co.unicauca.edu.co.contables.inventory.peps.domain.port.output.IKardexExternalClientPort;
import co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.input.rest.dto.request.KardexBatchDTORequest;
import co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.output.remoteSync.mapper.IKardexExternalClientMapper;
import co.unicauca.edu.co.contables.inventory.stock.domain.port.IFormatterResultOutputPort;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @brief Reads the kardex module in-process (paso 7 del módulo 7).
 *
 * Replaces the former RestClient proxy to lb://KARDEX: both modules now live
 * in this monolith, so the sibling query port is called directly and its
 * records are reshaped through the existing wire DTO, which the pre-existing
 * MapStruct mapper turns into a PEPS domain model.
 */
@Component("pepsKardexExternalClientAdapter")
@RequiredArgsConstructor
@Slf4j
public class KardexExternalClientAdapter implements IKardexExternalClientPort {

    private final IKardexExternalClientMapper kardexExternalClientMapper;
    private final IFormatterResultOutputPort formatterResultOutputPort;
    /** Puerto del módulo kardex; el simple name choca con el de PEPS, por eso el FQN. */
    private final co.unicauca.edu.co.contables.inventory.kardex.application.ports.input.kardex.IKardexQueryPort kardexQueryPort;

    @Override
    public List<co.unicauca.edu.co.contables.inventory.peps.domain.model.Kardex> findKardexByEnterpriseId(
            String enterpriseId) {
        try {
            log.info("Fetching kardex records for enterprise: {} (in-process kardex)", enterpriseId);
            List<KardexBatchDTORequest> payload = loadFromKardex(enterpriseId);
            if (payload == null) {
                log.warn("No data received from kardex for enterprise: {}", enterpriseId);
                formatterResultOutputPort.returnErrorGenericResponse(500,
                        "No data received from external kardex service");
            }
            log.info("Retrieved {} kardex records from kardex", payload.size());
            return payload.stream().map(kardexExternalClientMapper::toDomain).toList();
        } catch (Exception e) {
            log.error("Unexpected error reading kardex records: {}", e.getMessage());
            formatterResultOutputPort.returnErrorGenericResponse(500,
                    "Unexpected error communicating with kardex external service");
        }
        return List.of();
    }

    private List<KardexBatchDTORequest> loadFromKardex(String enterpriseId) {
        return kardexQueryPort.findLastKardexForAllProducts(enterpriseId).stream()
                .map(this::toWireDto)
                .toList();
    }

    private KardexBatchDTORequest toWireDto(Kardex source) {
        KardexBatchDTORequest target = new KardexBatchDTORequest();
        target.setProductId(source.getProductId());
        target.setQuantity(source.getQuantity());
        target.setFactCode(source.getFactCode());
        target.setUnitPrice(source.getUnitPrice());
        target.setDetails(source.getDetails());
        target.setType(source.getType() == null ? null
                : co.unicauca.edu.co.contables.inventory.peps.domain.enums.MovementType.valueOf(source.getType().name()));
        return target;
    }
}
