package co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.output.jpa.adapter;

import co.unicauca.edu.co.contables.accounting.debt_payments.application.output.IReceiptCommandPersistencePort;
import co.unicauca.edu.co.contables.accounting.debt_payments.application.output.IReceiptQueryPersistencePort;
import co.unicauca.edu.co.contables.accounting.debt_payments.domain.model.Receipt;
import co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.output.jpa.entity.ReceiptEntity;
import co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.output.jpa.mapper.IReceiptPersistenceMapper;
import co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.output.jpa.repository.IReceiptRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Persistence Adapter for Receipts.
 * This class implements the output ports for persistence and acts as a bridge between the application layer and the JPA-based persistence technology.
 */
@Repository
@RequiredArgsConstructor
public class ReceiptPersistenceAdapter implements IReceiptCommandPersistencePort, IReceiptQueryPersistencePort {

    private final IReceiptRepository receiptRepository;
    private final IReceiptPersistenceMapper receiptMapper;

    @Override
    public Receipt save(Receipt receipt) {
        ReceiptEntity receiptEntity = receiptMapper.toEntity(receipt);

        ReceiptEntity savedEntity = receiptRepository.save(receiptEntity);
        return receiptMapper.toDomain(savedEntity);
    }

    @Override
    public Optional<Receipt> findById(Long id) {
        Optional<ReceiptEntity> entityOpt = receiptRepository.findById(id);
        return entityOpt.map(receiptMapper::toDomain);
    }

    @Override
    public List<Receipt> findByInvoiceId(String invoiceId) {
        List<ReceiptEntity> entityList = receiptRepository.findByInvoiceId(Long.valueOf(invoiceId));
        return receiptMapper.toDomainList(entityList);
    }

    @Override
    public List<Receipt> findByThirdPartyId(String thirdPartyId, String enterpriseId) {
        List<ReceiptEntity> entityList = receiptRepository.findByThirdPartyIdAndEnterpriseId(Long.valueOf(thirdPartyId), enterpriseId);
        return receiptMapper.toDomainList(entityList);
    }

    @Override
    public List<Receipt> findByEnterpriseId(String enterpriseId) {
        List<ReceiptEntity> entityList = receiptRepository.findAllByEnterpriseId(enterpriseId);
        return receiptMapper.toDomainList(entityList);
    }

    @Override
    public boolean existsById(Long id) {
        return receiptRepository.existsById(id);
    }

}
