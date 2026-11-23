package co.unicauca.edu.co.contables.accounting.facture.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Third {
    private Long verificationNumber; 
    private String typeId;
    private Long idNumber;
    private String names; 
    private String lastNames; 
    private String address;
    private String country;
    private String province;
    private String city; 
    private String phoneNumber; 
    private String email; 
    private String personType; 
}
