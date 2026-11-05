package co.unicauca.edu.co.contables.accounting.facture.application.ports.output;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import co.unicauca.edu.co.contables.accounting.facture.domain.model.Facture;

public interface FactureGetOutputPort {
    Optional<Facture> getFactureById(Long factId);
    
    Page<Facture> getAllFacturesBy(String entId, Pageable pageable); 
    Page<Facture> getAllSalesFacturesBy(String entId, Pageable pageable);
    Page<Facture> getAllShoppingFacturesBy(String entId, Pageable pageable);
}
