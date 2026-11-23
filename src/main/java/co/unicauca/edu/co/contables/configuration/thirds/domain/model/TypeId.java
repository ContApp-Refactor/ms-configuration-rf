package co.unicauca.edu.co.contables.configuration.thirds.domain.model;

import co.unicauca.edu.co.contables.configuration.thirds.domain.utils.StringNormalizer;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;


/**
 * @brief Modelo de dominio que representa un tipo de identificación
 *
 * Entidad que define los tipos de documentos de identificación válidos en el sistema
 * (cédula, NIT, pasaporte, etc.) y su clasificación según el tipo de persona.
 */
@Builder
@Getter
@Setter
@EqualsAndHashCode(of = { "id" })
@ToString(of = { "id", "typeId", "typeIdname", "classification" })
@AllArgsConstructor
@NoArgsConstructor
public class TypeId {

    private Long id;

    @NotBlank(message = "El ID de la empresa no puede estar vacío")
    @Size(max = 50, message = "El ID de la empresa no puede exceder los 50 caracteres")
    private String entId;

    @NotBlank(message = "El código del tipo de identificación no puede estar vacío")
    @Size(max = 10, message = "El código del tipo de identificación no puede exceder los 10 caracteres")
    private String typeId;

    @NotBlank(message = "El nombre del tipo de identificación no puede estar vacío")
    @Size(max = 100, message = "El nombre del tipo de identificación no puede exceder los 100 caracteres")
    private String typeIdname;

    @Builder.Default
    private Boolean status = true;

    @NotNull(message = "La clasificación de persona no puede estar vacía")
    private PersonClassification classification;

    /**
     * @brief Verifica si este tipo de identificación es válido para personas naturales
     *
     * Determina si el tipo de documento puede ser utilizado por personas naturales
     * basándose en la clasificación configurada.
     * @return true si es válido para personas naturales, false en caso contrario
     */
    public boolean isValidForNaturalPerson() {
        return classification != null && classification.isValidForNaturalPerson();
    }

    /**
     * @brief Verifica si este tipo de identificación es válido para personas jurídicas
     *
     * Determina si el tipo de documento puede ser utilizado por personas jurídicas
     * basándose en la clasificación configurada.
     * @return true si es válido para personas jurídicas, false en caso contrario
     */
    public boolean isValidForLegalEntity() {
        return classification != null && classification.isValidForLegalEntity();
    }

    /**
     * @brief Obtiene el código del tipo de identificación normalizado
     *
     * Retorna el código del tipo de identificación normalizado para búsquedas
     * y comparaciones, eliminando caracteres especiales y normalizando el formato.
     * @return código normalizado del tipo de identificación
     */
    public String getNormalizedTypeId() {
        return StringNormalizer.normalizeCode(typeId);
    }
}
