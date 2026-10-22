package co.unicauca.edu.co.contables.accounting.catalogue.accounting.infraestructure.output.jpaAdapter;

import java.util.Optional;

import org.springframework.stereotype.Component;

import co.unicauca.edu.co.contables.accounting.catalogue.accounting.application.output.IAccountingEntryPersistenceOutputPort;
import co.unicauca.edu.co.contables.accounting.catalogue.accounting.domain.models.AccountingEntry;
import co.unicauca.edu.co.contables.accounting.catalogue.accounting.infraestructure.output.jpaAdapter.entity.AccountingEntryEntity;
import co.unicauca.edu.co.contables.accounting.catalogue.accounting.infraestructure.output.jpaAdapter.mapper.IAccountingEntryMapper;
import co.unicauca.edu.co.contables.accounting.catalogue.accounting.infraestructure.output.jpaAdapter.repository.IAccountingEntryRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AccountingEntryPersistenceJpaAdapter implements IAccountingEntryPersistenceOutputPort{
    
    private final IAccountingEntryRepository accountingEntryRepository;
    private final IAccountingEntryMapper accountingEntryMapper;
    
    @Override
    public AccountingEntry save(AccountingEntry accountingEntry) {
        // 1. Mapear el dominio a la entidad. El 'sourceDocumentId' ya está incluido.
        AccountingEntryEntity accountingEntryEntity = accountingEntryMapper.toEntity(accountingEntry);

        // 2. Guardar la entidad. La lógica de buscar el ReceiptEntity ya no es necesaria.
        AccountingEntryEntity savedEntity = accountingEntryRepository.save(accountingEntryEntity);
        
        // 3. Mapear de vuelta al dominio.
        return accountingEntryMapper.toDomain(savedEntity);
    }

    @Override
    public Optional<AccountingEntry> findBySourceDocumentId(Long sourceDocumentId) {
        return accountingEntryRepository.findBySourceDocumentId(sourceDocumentId)
            .map(accountingEntryMapper::toDomain);
    }

    
    
}
