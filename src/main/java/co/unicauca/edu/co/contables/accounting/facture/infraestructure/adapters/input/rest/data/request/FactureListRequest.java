package co.unicauca.edu.co.contables.accounting.facture.infraestructure.adapters.input.rest.data.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class FactureListRequest {
    @NotNull(message = "Enterprise Id cannot ve null.")
    private String entId;

    /**
     * Página 0-based; 0 es la primera página de Spring Data. Al ser primitiva,
     * un {@code @NotNull} no podría dispararse nunca, así que la garantía
     * aplicable es que el número de página no sea negativo.
     */
    @Min(value = 0, message = "Page number cannot be negative.")
    private int numPage;
}
