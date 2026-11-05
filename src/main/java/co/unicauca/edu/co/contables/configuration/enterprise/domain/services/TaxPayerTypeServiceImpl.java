package co.unicauca.edu.co.contables.configuration.enterprise.domain.services;

import co.unicauca.edu.co.contables.configuration.enterprise.dataAccess.mapper.TaxPayerTypeDataMapper;
import co.unicauca.edu.co.contables.configuration.enterprise.dataAccess.repository.TaxPayerTypeRepository;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.models.TaxPayerType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TaxPayerTypeServiceImpl implements ITaxPayerTypeService {

    private final TaxPayerTypeRepository repository;
    private final TaxPayerTypeDataMapper dataMapper;

    @Override
    public List<TaxPayerType> getAll() {
        return dataMapper.toDomainList(repository.findAll());
    }
}
