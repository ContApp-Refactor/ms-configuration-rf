package co.unicauca.edu.co.contables.accounting.facture.application.ports.input;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import co.unicauca.edu.co.contables.accounting.facture.domain.model.Facture;

public interface ListFactureUseCase {
    Page<Facture> getAllFacturesBy(String entId, Pageable pageable); 
    Page<Facture> getAllSalesFacturesBy(String entId, Pageable pageable);
    Page<Facture> getAllShoppingFacturesBy(String entId, Pageable pageable);
}
