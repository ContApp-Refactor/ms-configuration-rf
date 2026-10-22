package co.unicauca.edu.co.contables.configuration.enterprise.domain.services;

import co.unicauca.edu.co.contables.configuration.enterprise.domain.models.TaxPayerType;

import java.util.List;

public interface ITaxPayerTypeService {
    List<TaxPayerType> getAll();
}
