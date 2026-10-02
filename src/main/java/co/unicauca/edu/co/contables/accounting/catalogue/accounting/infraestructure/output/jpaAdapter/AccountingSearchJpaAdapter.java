package co.unicauca.edu.co.contables.accounting.catalogue.accounting.infraestructure.output.jpaAdapter;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import co.unicauca.edu.co.contables.accounting.catalogue.accounting.application.output.IAccountingSearchOutputPort;
import co.unicauca.edu.co.contables.accounting.catalogue.accounting.domain.models.AccountingEntry;
import co.unicauca.edu.co.contables.accounting.catalogue.accounting.domain.models.AccountingMovement;
import co.unicauca.edu.co.contables.accounting.catalogue.accounting.domain.models.InvoiceReplica;
import co.unicauca.edu.co.contables.accounting.catalogue.accounting.domain.models.ReceiptDetail;
import co.unicauca.edu.co.contables.accounting.catalogue.accounting.infraestructure.output.jpaAdapter.entity.AccountingMovementEntity;
import co.unicauca.edu.co.contables.accounting.catalogue.accounting.infraestructure.output.jpaAdapter.entity.InvoiceReplicaEntity;
import co.unicauca.edu.co.contables.accounting.catalogue.accounting.infraestructure.output.jpaAdapter.mapper.IAccountCatalogueMapper;
import co.unicauca.edu.co.contables.accounting.catalogue.accounting.infraestructure.output.jpaAdapter.mapper.IAccountingEntryMapper;
import co.unicauca.edu.co.contables.accounting.catalogue.accounting.infraestructure.output.jpaAdapter.mapper.IAccountingMovementMapper;
import co.unicauca.edu.co.contables.accounting.catalogue.accounting.infraestructure.output.jpaAdapter.mapper.IInvoiceReplicaPersistenceMapper;
import co.unicauca.edu.co.contables.accounting.catalogue.accounting.infraestructure.output.jpaAdapter.mapper.IReceiptDetailMapper;
import co.unicauca.edu.co.contables.accounting.catalogue.accounting.infraestructure.output.jpaAdapter.repository.IAccountingEntryRepository;
import co.unicauca.edu.co.contables.accounting.catalogue.accounting.infraestructure.output.jpaAdapter.repository.IAccountingMovementRepository;
import co.unicauca.edu.co.contables.accounting.catalogue.accounting.infraestructure.output.jpaAdapter.repository.IInvoiceRepository;
import co.unicauca.edu.co.contables.accounting.catalogue.accounting.infraestructure.output.jpaAdapter.repository.IReceiptDetailRepository;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.domain.models.AccountCatalogue;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.infraestructure.adapters.output.jpaAdapter.entity.AccountCatalogueEntity;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.infraestructure.adapters.output.jpaAdapter.repository.IAccountCatalogueRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AccountingSearchJpaAdapter implements IAccountingSearchOutputPort {
    private final IAccountingEntryRepository accountingEntryRepository;
    private final IAccountingMovementRepository movementRepository;
    private final IAccountingEntryMapper accountingEntryMapper;
    private final IAccountingMovementMapper movementMapper;
    private final IInvoiceReplicaPersistenceMapper invoiceMapper;
    private final IInvoiceRepository invoiceRepository;
    private final IReceiptDetailMapper receiptDetailMapper;
    private final IReceiptDetailRepository receiptDetailRepository;
    private final IAccountCatalogueRepository accountCatalogueRepository;
    private final IAccountCatalogueMapper accountCatalogueMapper;

    @Override
    public Optional<AccountingEntry> findById(Long id) {
        return accountingEntryRepository.findByIdWithMovements(id).map(accountingEntryMapper::toDomain);
    }

    @Override
    public Optional<AccountingEntry> findByReceiptId(Long receiptId) {
        return accountingEntryRepository.findBySourceDocumentId(receiptId).map(accountingEntryMapper::toDomain);
    }

    @Override
    public List<AccountingMovement> findMovementsByAccountId(Long accountId) {
        return movementRepository.findByAccount(accountId).stream()
                .map(movementMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<AccountingMovement> findMovementsByThirdPartyId(Long thirdPartyId) {
        List<AccountingMovementEntity> entities = movementRepository.findByThirdPartyId(thirdPartyId);

        // El resto del código no cambia y ahora funcionará correctamente
        return movementMapper.toDomainList(entities);
    }

    @Override
    public Optional<AccountingEntry> findBySourceDocumentIdAndType(Long sourceDocumentId, String type) {
        return accountingEntryRepository.findBySourceDocumentIdAndType(sourceDocumentId, type)
                .map(accountingEntryMapper::toDomain);

    }

    @Override
    public boolean existsBySourceDocumentIdAndType(Long sourceDocumentId, String type) {
        return accountingEntryRepository.existsBySourceDocumentIdAndType(sourceDocumentId, type);
    }

    @Override
    public List<InvoiceReplica> findPendingInvoicesByClientIds(List<Long> clientIds) {
        List<InvoiceReplicaEntity> entities = invoiceRepository.findByPendingValueGreaterThanAndThirdIdIn(0L,
                clientIds);
        return invoiceMapper.toInvoiceReplicaList(entities);
    }

    @Override
    public List<InvoiceReplica> findPendingInvoicesByClientId(Long clientId) {
        List<InvoiceReplicaEntity> entities = invoiceRepository.findByThirdIdAndPendingValueGreaterThan(clientId, 0L);
        return invoiceMapper.toInvoiceReplicaList(entities);
    }

    @Override
    public List<ReceiptDetail> findReceiptDetailsByInvoiceId(Long invoiceId) {
        // Asumiendo que IReceiptDetailMapper tiene un método toReceiptDetailList
        return receiptDetailMapper.toReceiptDetailList(
                receiptDetailRepository.findByOriginalInvoiceId(invoiceId));
    }

    @Override
    public List<AccountCatalogue> findAllAccountsByEnterprise(String enterpriseId) {
        // 1. Llamar al método del repositorio que trae todas las entidades para esa
        // empresa
        List<AccountCatalogueEntity> accountEntities = accountCatalogueRepository
                .findByIdEnterpriseOrderByCode(enterpriseId);

        // 2. Usar el mapper para convertir la lista de entidades a una lista de modelos
        // de dominio
        return accountCatalogueMapper.toDomainList(accountEntities);

    }

    @Override
    public List<AccountingMovement> findMovementsByThirdAndAccountCodes(Long thirdId, List<String> accountCodes) {
        // 1. Llama al nuevo método del repositorio para obtener las entidades desde la
        // BD.
        List<AccountingMovementEntity> entities = movementRepository.findByThirdAndAccountCodes(thirdId);

        // 2. Usa el mapper para convertir la lista de entidades a la lista de modelos
        // de dominio.
        return movementMapper.toDomainList(entities);
    }

    @Override
    public Optional<AccountingEntry> findEntryByMovementId(Long movementId) {
        Optional<AccountingMovementEntity> movementEntityOptional = movementRepository.findById(movementId);

        // 2. Si el movimiento existe, accede a su asiento asociado (que JPA cargará perezosamente)
        // 3. Mapea la entidad del asiento a su modelo de dominio y devuélvela
        return movementEntityOptional
                .map(AccountingMovementEntity::getAccountingEntry)
                .map(accountingEntryMapper::toDomain);
    }

}
