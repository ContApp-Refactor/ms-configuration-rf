package co.unicauca.edu.co.contables.accounting.catalogue.accounting.manualnotes.domain.port;

public interface CostCenterPort {

    boolean existsAndIsActive(String code, String enterpriseId);
}
