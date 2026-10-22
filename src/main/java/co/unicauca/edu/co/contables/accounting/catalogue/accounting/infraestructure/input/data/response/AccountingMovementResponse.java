package co.unicauca.edu.co.contables.accounting.catalogue.accounting.infraestructure.input.data.response;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AccountingMovementResponse {
      private Long id;
    private Long account; // ID de la cuenta contable
    private Long thirdPartyId;
    private String description;
    private Long debit;
    private Long credit;
}
