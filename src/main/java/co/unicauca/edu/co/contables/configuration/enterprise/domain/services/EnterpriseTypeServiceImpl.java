package co.unicauca.edu.co.contables.configuration.enterprise.domain.services;

import co.unicauca.edu.co.contables.configuration.enterprise.dataAccess.mapper.EnterpriseTypeDataMapper;
import co.unicauca.edu.co.contables.configuration.enterprise.dataAccess.repository.EnterpriseTypeRepository;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.models.EnterpriseType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EnterpriseTypeServiceImpl implements IEnterpriseTypeService {

    private final EnterpriseTypeRepository repository;
    private final EnterpriseTypeDataMapper dataMapper;

    @Override
    public List<EnterpriseType> getAll() {
        return dataMapper.toDomainList(repository.findAll());
    }
}
