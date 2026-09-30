package co.unicauca.edu.co.contables.accounting.catalogue.accounting.infraestructure.output.jpaAdapter.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import co.unicauca.edu.co.contables.accounting.catalogue.accounting.infraestructure.output.jpaAdapter.entity.ReceiptDetailEntity;

@Repository
public interface  IReceiptDetailRepository extends JpaRepository<ReceiptDetailEntity, Long> {
    
    List<ReceiptDetailEntity> findByOriginalInvoiceId(Long invoiceId);
}
