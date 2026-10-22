package co.unicauca.edu.co.contables.configuration.enterprise.domain.services;

import co.unicauca.edu.co.contables.configuration.enterprise.dataAccess.mapper.TaxLiabilityDataMapper;
import co.unicauca.edu.co.contables.configuration.enterprise.dataAccess.repository.TaxLiabilityRepository;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.models.TaxLiability;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TaxLiabilityServiceImpl implements ITaxLiabilityService {

    private final TaxLiabilityRepository repository;
    private final TaxLiabilityDataMapper dataMapper;

    @Override
    public List<TaxLiability> getAll() {
        return dataMapper.toDomainList(repository.findAll());
    }
}
