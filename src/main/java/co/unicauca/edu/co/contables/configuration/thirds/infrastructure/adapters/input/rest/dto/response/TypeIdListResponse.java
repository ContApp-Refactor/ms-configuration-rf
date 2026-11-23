package co.unicauca.edu.co.contables.configuration.thirds.infrastructure.adapters.input.rest.dto.response;

import co.unicauca.edu.co.contables.configuration.thirds.domain.model.TypeId;
import lombok.*;

import java.util.List;

/**
 * @brief DTO de respuesta para lista de tipos de identificación
 */
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TypeIdListResponse {
    private List<TypeId> typeId;
}
