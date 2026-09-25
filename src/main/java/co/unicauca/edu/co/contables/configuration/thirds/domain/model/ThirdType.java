package co.unicauca.edu.co.contables.configuration.thirds.domain.model;

import co.unicauca.edu.co.contables.configuration.thirds.domain.utils.StringNormalizer;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;


/**
 * @brief Modelo de dominio que representa un tipo de tercero
 *
 * Entidad que define las categorías o tipos de terceros disponibles en el sistema
 * (cliente, proveedor, empleado, etc.). Cada tipo pertenece a una empresa específica.
 */
@Builder
@Getter
@Setter
@EqualsAndHashCode(of = { "entId", "thirdTypeId" })
@ToString(of = { "thirdTypeId", "thirdTypeName" })
@AllArgsConstructor
@NoArgsConstructor
public class ThirdType {

    @NotBlank(message = "El ID de la empresa no puede estar vacío")
    @Size(max = 50, message = "El ID de la empresa no puede exceder los 50 caracteres")
    private String entId;

    @NotNull(message = "El ID del tipo de tercero es obligatorio")
    private Long thirdTypeId;

    @NotBlank(message = "El nombre del tipo de tercero no puede estar vacío")
    @Size(max = 100, message = "El nombre del tipo de tercero no puede exceder los 100 caracteres")
    @Pattern(regexp = "^[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]+$", message = "El nombre solo puede contener letras y espacios")
    private String thirdTypeName;

    @Builder.Default
    private Boolean status = true;

    /**
     * @brief Obtiene el nombre normalizado del tipo de tercero
     *
     * Retorna el nombre del tipo de tercero con caracteres especiales normalizados
     * (acentos eliminados) pero preservando el formato de mayúsculas/minúsculas original.
     * @return nombre normalizado sin acentos pero con formato de caso preservado
     */
    public String getNormalizedName() {
        return StringNormalizer.normalizePreservingCase(thirdTypeName);
    }
}
