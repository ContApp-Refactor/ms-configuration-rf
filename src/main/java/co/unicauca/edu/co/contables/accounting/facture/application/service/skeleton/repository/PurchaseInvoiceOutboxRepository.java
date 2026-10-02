package co.unicauca.edu.co.contables.accounting.facture.application.service.skeleton.repository;

import co.unicauca.edu.co.contables.accounting.facture.application.service.skeleton.model.PurchaseInvoiceOutboxEvent;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface PurchaseInvoiceOutboxRepository extends JpaRepository<PurchaseInvoiceOutboxEvent,Long> {
    @Query(value="select * from purchase_invoice_outbox where status in ('PENDING','FAILED') and attempts < 5 and next_attempt_at <= now() order by created_at limit 100",nativeQuery=true)
    List<PurchaseInvoiceOutboxEvent> findReadyAcrossTenants();
}
