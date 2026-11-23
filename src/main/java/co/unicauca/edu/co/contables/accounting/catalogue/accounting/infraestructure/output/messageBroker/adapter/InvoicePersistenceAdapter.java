package co.unicauca.edu.co.contables.accounting.catalogue.accounting.infraestructure.output.messageBroker.adapter;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import co.unicauca.edu.co.contables.accounting.catalogue.accounting.application.output.IInvoiceProviderPort;
import co.unicauca.edu.co.contables.accounting.catalogue.accounting.domain.enums.InvoiceStatus;
import co.unicauca.edu.co.contables.accounting.catalogue.accounting.domain.models.InvoiceReplica;
import co.unicauca.edu.co.contables.accounting.catalogue.accounting.infraestructure.output.jpaAdapter.entity.InvoiceReplicaEntity;
import co.unicauca.edu.co.contables.accounting.catalogue.accounting.infraestructure.output.jpaAdapter.mapper.IInvoiceReplicaPersistenceMapper;
import co.unicauca.edu.co.contables.accounting.catalogue.accounting.infraestructure.output.jpaAdapter.repository.IInvoiceRepository;

import lombok.RequiredArgsConstructor;

@Component("invoiceAccountingPersistenceAdapter")
@RequiredArgsConstructor
public class InvoicePersistenceAdapter implements IInvoiceProviderPort {

    private final IInvoiceReplicaPersistenceMapper invoiceMapper;
    private final IInvoiceRepository invoiceRepository;

    @Transactional
    @Override
    public void saveOrUpdate(InvoiceReplica invoice) {
        InvoiceReplicaEntity entity = new InvoiceReplicaEntity();
        if(invoice.getId() != null){
            entity = invoiceRepository.findById(invoice.getId())
            .orElse(new InvoiceReplicaEntity());
        }
        
        entity.setFactCode(Long.valueOf(invoice.getFactCode()));
        entity.setEntId(invoice.getEntId());
        entity.setThirdId(invoice.getThirdId());
        entity.setTotalValue(invoice.getTotalValue());
        entity.setTotalPay(invoice.getTotalPay());
        entity.setPendingValue(invoice.getPendingValue());
        entity.setExpirationDate(invoice.getExpirationDate());
        entity.setLastUpdateAt(LocalDate.now());
        entity.setActive(true); 
        entity.setStatus(InvoiceStatus.PENDING);
        entity.setAccountingAccount(invoice.getAccountingAccount());
        entity.setCreationDate(invoice.getCreationDate());
        invoiceRepository.save(entity);
    }

    public void delete(Long factCode) {
        invoiceRepository.deleteById(factCode);
    }

    @Override
    public Optional<Long> getInvoiceBalance(Long invoiceId) {
        Optional<InvoiceReplicaEntity> invoiceEntityOptional = invoiceRepository.findById(invoiceId);

        return invoiceEntityOptional
            .map(InvoiceReplicaEntity::getPendingValue)
            .map(bd -> bd.longValue());
    }

    /**
     * Busca una factura por su ID en la base de datos.
     * @param invoiceId El ID de la factura a buscar.
     * @return Un Optional que contiene el objeto de dominio InvoiceReplica si se encuentra,
     *         o un Optional vacío si no.
     */
    @Transactional(readOnly = true) // Es una operación de solo lectura
    @Override
    public Optional<InvoiceReplica> findInvoiceById(Long invoiceId) {
        Optional<InvoiceReplicaEntity> entityOptional = invoiceRepository.findById(invoiceId);
        return entityOptional.map(invoiceMapper::toDomain);
    }

    /**
     * Actualiza una factura en la base de datos.
     * @param invoice El objeto de dominio InvoiceReplica con los datos actualizados.
     */
    @Override
    public void updateInvoice(InvoiceReplica invoice) {
        InvoiceReplicaEntity invoiceToUpdate = invoiceRepository.getReferenceById(invoice.getId());
        invoiceToUpdate.setPendingValue(invoice.getPendingValue());
        invoiceToUpdate.setTotalPay(invoice.getTotalPay());
        invoiceToUpdate.setTotalValue(invoice.getTotalValue());
        invoiceToUpdate.setStatus(invoice.getStatus()); //Nuevo campo estado
        invoiceRepository.save(invoiceToUpdate);
    }

    @Override
    public List<InvoiceReplica> findPendingInvoicesByClientId(Long clientId) {
        var invoiceEntityList = invoiceRepository.findByThirdIdAndPendingValueGreaterThan(clientId, 0L);
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
    public Optional<InvoiceReplica> findInvoiceByFactCode(Long factCode) {
        Optional<InvoiceReplicaEntity> entityOptional = invoiceRepository.findByFactCode(factCode);
        return entityOptional.map(invoiceMapper::toDomain);
    }

}
