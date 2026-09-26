package co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.output.jpa.adapter;

import co.unicauca.edu.co.contables.accounting.debt_payments.application.output.IPortfolioWriteOffPersistencePort;
import co.unicauca.edu.co.contables.accounting.debt_payments.domain.model.PortfolioWriteOff;
import co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.output.jpa.entity.PortfolioWriteOffEntity;
import co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.output.jpa.mapper.IPortfolioWriteOffPersistenceMapper;
import co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.output.jpa.repository.IPortfolioWriteOffRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Persistence adapter for managing Portfolio Write-Offs.
 * Implements the IPortfolioWriteOffPersistencePort interface to provide CRUD operations for Portfolio Write-Off entities using JPA.
 */

@Component
@RequiredArgsConstructor
public class PortfolioWriteOffPersistenceAdapter implements IPortfolioWriteOffPersistencePort {

    private final IPortfolioWriteOffRepository portfolioWriteOffRepository;
    private final IPortfolioWriteOffPersistenceMapper portfolioWriteOffMapper;

    @Override
    public PortfolioWriteOff save(PortfolioWriteOff writeOff) {
        PortfolioWriteOffEntity entityToSave = portfolioWriteOffMapper.toEntity(writeOff);
        entityToSave.getDetails().forEach(detail -> detail.setPortfolioWriteOff(entityToSave));
        PortfolioWriteOffEntity savedEntity = portfolioWriteOffRepository.save(entityToSave);
        return portfolioWriteOffMapper.toDomain(savedEntity);
    }

    @Override
    public Optional<PortfolioWriteOff> findById(Long writeOffId) {
        return portfolioWriteOffRepository.findById(writeOffId)
                .map(portfolioWriteOffMapper::toDomain);
    }

    @Override
    public PortfolioWriteOff update(PortfolioWriteOff writeOff) {
        if (writeOff.getId() == null || !portfolioWriteOffRepository.existsById(writeOff.getId())) {
            throw new IllegalArgumentException("El castigo con ID " + writeOff.getId() + " no existe.");
        }
        PortfolioWriteOffEntity entityToUpdate = portfolioWriteOffMapper.toEntity(writeOff);
        entityToUpdate.getDetails().forEach(detail -> detail.setPortfolioWriteOff(entityToUpdate));
        PortfolioWriteOffEntity updatedEntity = portfolioWriteOffRepository.save(entityToUpdate);
        return portfolioWriteOffMapper.toDomain(updatedEntity);
    }

    @Override
    public List<PortfolioWriteOff> findByEnterpriseId(String enterpriseId) {
        List<PortfolioWriteOffEntity> entities = portfolioWriteOffRepository.findByEnterpriseId(enterpriseId);
        return portfolioWriteOffMapper.toDomainList(entities);
    }

    @Override
    public List<PortfolioWriteOff> findConfirmedOrVoidedByEnterpriseId(String enterpriseId) {
        List<PortfolioWriteOffEntity> entities = portfolioWriteOffRepository.findConfirmedOrVoidedByEnterpriseId(enterpriseId);
        return portfolioWriteOffMapper.toDomainList(entities);
    }
    
}
