package co.unicauca.edu.co.contables.accounting.facture.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Enterprise {
   private String entName;
   private String entAddress;
   private String entNIT;
   private String entContact;
   private String entLogo; 
}
