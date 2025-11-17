package co.unicauca.edu.co.contables.configuration.costCenters.domain.models;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CostCenter {
    private Long id;
    private String idEnterprise;
    private String code;
    private String name;
    private CostCenter parent;
    private List<CostCenter> children;
    @Builder.Default
    private Boolean status = true;

    @Builder.Default
    private Integer usageCount = 0;

    /**
     * @brief Incrementa el contador de uso del centro de costo
     */
    public void incrementUsageCount() {
        this.usageCount = this.usageCount == null ? 1 : this.usageCount + 1;
    }

    /**
     * @brief Verifica si el centro de costo está siendo usado
     * @return true si el centro de costo tiene uso registrado
     */
    public boolean isInUse() {
        return this.usageCount != null && this.usageCount > 0;
    }
}
