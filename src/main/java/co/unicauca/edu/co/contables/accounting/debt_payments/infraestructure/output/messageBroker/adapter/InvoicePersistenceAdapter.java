package co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.output.messageBroker.adapter;

import co.unicauca.edu.co.contables.accounting.debt_payments.application.output.IInvoiceProviderPort;
import co.unicauca.edu.co.contables.accounting.debt_payments.domain.enums.InvoiceStatus;
import co.unicauca.edu.co.contables.accounting.debt_payments.domain.model.Replica.InvoiceReplica;
import co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.output.jpa.entity.replicas.InvoiceReplicaEntity;
import co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.output.jpa.mapper.replicas.IInvoicePersistenceMapper;
import co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.output.jpa.repository.replicas.IInvoiceRepository;
import co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.output.messageBroker.dto.InvoiceSyncDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Adapter class responsible for handling invoice persistence operations.
 * It implements the IInvoiceProviderPort interface.
 */
@Component
@RequiredArgsConstructor
public class InvoicePersistenceAdapter implements IInvoiceProviderPort {

    private final IInvoicePersistenceMapper invoiceMapper;
    private final IInvoiceRepository invoiceRepository;

    @Transactional
    public void saveOrUpdate(InvoiceSyncDto dto) {
        InvoiceReplicaEntity entity = invoiceRepository.findById(dto.getFactCode())
            .orElse(new InvoiceReplicaEntity());
        
        entity.setFactCode(dto.getFactCode());
        entity.setEntId(dto.getEntId());
        entity.setThirdId(dto.getThirdId());
        entity.setTotalValue(dto.getTotalValue());
        entity.setTotalPay(dto.getTotalPay());
        entity.setPendingValue(dto.getPendingValue());
        entity.setCreationDate(dto.getCreationDate());
        entity.setExpirationDate(dto.getExpirationDate());
        entity.setLastUpdateAt(LocalDate.now());
        entity.setActive(true); 
        entity.setStatus(InvoiceStatus.PENDING);
        entity.setAccountingAccount(dto.getAccountingAccount());
        invoiceRepository.save(entity);
    }

    public void delete(Long factCode) {
        invoiceRepository.deleteById(factCode);
    }

    @Override
    public Optional<Long> getInvoiceBalance(Long invoiceId) {
        Optional<InvoiceReplicaEntity> invoiceEntityOptional = invoiceRepository.findById(invoiceId);

        return invoiceEntityOptional.map(InvoiceReplicaEntity::getPendingValue);
    }

    @Override
    public Optional<InvoiceReplica> findInvoiceById(Long invoiceId) {
        Optional<InvoiceReplicaEntity> entityOptional = invoiceRepository.findById(invoiceId);
        return entityOptional.map(invoiceMapper::toDomain);
    }

    @Override
    public void updateInvoice(InvoiceReplica invoice) {
        InvoiceReplicaEntity invoiceToUpdate = invoiceRepository.getReferenceById(invoice.getId());
        invoiceToUpdate.setPendingValue(invoice.getPendingValue());
        invoiceToUpdate.setTotalPay(invoice.getTotalPay());
        invoiceToUpdate.setTotalValue(invoice.getTotalValue());
        invoiceToUpdate.setStatus(invoice.getStatus()); 
        invoiceToUpdate.setExpirationDate(invoice.getExpirationDate());
        invoiceRepository.save(invoiceToUpdate);
    }

    @Override
    public List<InvoiceReplica> findPendingInvoicesByClientIdAndEnterpriseId(Long clientId, String enterpriseId) {
        var invoiceEntityList = invoiceRepository.findByThirdIdAndEntIdAndPendingValueGreaterThan(clientId, enterpriseId, 0L);
        return invoiceMapper.toInvoiceReplicaList(invoiceEntityList);
    }

    @Override
    public List<InvoiceReplica> findInvoicesByIds(List<Long> invoiceIds) {
        var invoiceEntityList = invoiceRepository.findAllById(invoiceIds);
        return invoiceMapper.toInvoiceReplicaList(invoiceEntityList);
    }

    @Override
    public List<InvoiceReplica> findInvoicesByEnterpriseId(String enterpriseId) {
        var invoiceEntityList = invoiceRepository.findByEntIdAndStatus(enterpriseId, InvoiceStatus.PENDING);
        return invoiceMapper.toInvoiceReplicaList(invoiceEntityList);
    }

    @Override
    public List<InvoiceReplica> findPendingInvoicesByEnterpriseId(String enterpriseId) {
        var invoiceEntityList = invoiceRepository.findByEntIdAndStatus(enterpriseId, InvoiceStatus.PENDING);
        return invoiceMapper.toInvoiceReplicaList(invoiceEntityList);
    }

    @Override
    public List<InvoiceReplica> findStatusInvoicesByClientId(Long clientId, InvoiceStatus status, String enterpriseId) {
        var invoiceEntityList = invoiceRepository.findByThirdIdAndStatusAndEntId(clientId, status, enterpriseId);
        return invoiceMapper.toInvoiceReplicaList(invoiceEntityList);
    }

    @Override
    public List<InvoiceReplica> findExpiringInvoices(String enterpriseId, InvoiceStatus status, LocalDate startDate,
            LocalDate endDate) {
        List<InvoiceReplicaEntity> entities = invoiceRepository.findByEntIdAndStatusAndExpirationDateBetween(
            enterpriseId, 
            status, 
            startDate, 
            endDate
        );
        return invoiceMapper.toInvoiceReplicaList(entities);
    }

    @Override
    public List<InvoiceReplica> findInvoicesByExpirationDate(LocalDate expirationLocalDate) {
        List<InvoiceReplicaEntity> entities = invoiceRepository.findByExpirationDate(expirationLocalDate);
        return invoiceMapper.toInvoiceReplicaList(entities);
    }

}
