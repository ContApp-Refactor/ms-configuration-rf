package co.unicauca.edu.co.contables.configuration.enterprise.domain.services;

import co.unicauca.edu.co.contables.configuration.enterprise.domain.models.TaxLiability;

import java.util.List;

public interface ITaxLiabilityService {
    List<TaxLiability> getAll();
}
