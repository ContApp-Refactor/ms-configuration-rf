package co.unicauca.edu.co.contables.accounting.catalogue.accounting.manualnotes.domain.port;

public interface ThirdPartyPort {

    boolean existsAndIsActive(String documentNumber, String enterpriseId);
}
