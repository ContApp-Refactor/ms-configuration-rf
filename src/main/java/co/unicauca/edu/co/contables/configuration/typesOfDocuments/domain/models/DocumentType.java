package co.unicauca.edu.co.contables.configuration.typesOfDocuments.domain.models;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentType {
    private Long id;
    private String prefix;
    private String name;
    private Long documentClassId;
    private Integer moduleId;
    private String idEnterprise;
    @Builder.Default
    private Boolean status = true;
    @Builder.Default
    private Integer usageCount = 0;

    /**
     * @brief Verifica si el tipo de documento está siendo usado
     * @return true si el tipo de documento tiene uso registrado
     */
    public boolean isInUse() {
        return this.usageCount != null && this.usageCount > 0;
    }
}
