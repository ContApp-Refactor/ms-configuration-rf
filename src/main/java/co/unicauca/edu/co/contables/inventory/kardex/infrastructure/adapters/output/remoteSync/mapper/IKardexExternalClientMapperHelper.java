package co.unicauca.edu.co.contables.inventory.kardex.infrastructure.adapters.output.remoteSync.mapper;

import java.util.List;

import org.springframework.stereotype.Component;

import co.unicauca.edu.co.contables.inventory.kardex.infrastructure.adapters.input.rest.dto.request.KardexBatchDtoRequest;
import co.unicauca.edu.co.contables.inventory.peps.domain.model.KardexMigration;

import lombok.RequiredArgsConstructor;

/**
 * @brief Reads the PEPS module in-process and reshapes it to this module's wire DTO (paso 7).
 *
 * PEPS and kardex are sibling aggregates with different domain models, so the
 * translation goes through {@link KardexBatchDtoRequest}, the exact DTO the
 * pre-migration HTTP contract carried, and the existing MapStruct mapper does
 * the rest.
 */
@Component
@RequiredArgsConstructor
public class IKardexExternalClientMapperHelper {

    private final co.unicauca.edu.co.contables.inventory.peps.application.ports.input.IKardexQueryPort pepsQueryPort;

    public List<KardexBatchDtoRequest> loadFromPeps(String enterpriseId) {
        return pepsQueryPort.findLastKardexForAllProducts(enterpriseId).stream()
                .map(this::toWireDto)
                .toList();
    }

    private KardexBatchDtoRequest toWireDto(KardexMigration source) {
        KardexBatchDtoRequest target = new KardexBatchDtoRequest();
        target.setProductId(source.getProductId());
        target.setQuantity(source.getQuantity() == null ? null : source.getQuantity().intValue());
        target.setFactCode(source.getFactCode());
        target.setUnitPrice(source.getUnitPrice());
        target.setDetails(source.getDetails());
        target.setType(source.getType() == null ? null
                : co.unicauca.edu.co.contables.inventory.kardex.domain.model.MovementType.valueOf(source.getType().name()));
        target.setBalanceQuantity(source.getBalanceQuantity() == null ? null : source.getBalanceQuantity().intValue());
        target.setBalanceUnitPrice(source.getBalanceUnitPrice());
        target.setTotalBalance(source.getTotalBalance());
        return target;
    }
}
