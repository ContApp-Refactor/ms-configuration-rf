package co.unicauca.edu.co.contables.configuration.costCenters.presentation.DTO.response;

import lombok.*;

/**
 * @brief DTO de respuesta para centros de costo
 *
 * DTO que representa la respuesta de operaciones con centros de costo,
 * incluyendo información jerárquica y estado.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CostCenterRes {
    private Long id;
    private String idEnterprise;
    private String code;
    private String name;
    private Long parentId;
    private Boolean status;
    private Integer usageCount;
}
