package co.unicauca.edu.co.contables.accounting.catalogue.accounting.infraestructure.input.data.request;

import java.util.List;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ClientPortfolioRequest {
    @NotEmpty(message = "La lista de IDs de clientes no puede estar vacía")
    @NotNull(message = "La lista de IDs de clientes no puede ser nula")
    private List<Long> clientIds;
}
