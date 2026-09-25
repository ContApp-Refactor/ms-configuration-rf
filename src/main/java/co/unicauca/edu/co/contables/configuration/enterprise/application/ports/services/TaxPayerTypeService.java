package co.unicauca.edu.co.contables.configuration.enterprise.application.ports.services;

import co.unicauca.edu.co.contables.configuration.enterprise.application.ports.input.ITaxPayerTypeManagerPort;
import co.unicauca.edu.co.contables.configuration.enterprise.application.ports.output.ITaxPayerTypeOutputPort;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.models.TaxPayerType;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Servicio que implementa las operaciones de gestión de tipos de contribuyentes.
 * Gestiona la lógica de negocio para la consulta y recuperación de información
 * sobre los diferentes tipos de contribuyentes en el sistema.
 *
 * @author CONTAPP
 * @version 1.0
 * @since 1.0.0
 */
@Service
@AllArgsConstructor
public class TaxPayerTypeService implements ITaxPayerTypeManagerPort{

    /**
     * Puerto de salida para operaciones de gestión de tipos de contribuyentes.
     */
    private final ITaxPayerTypeOutputPort taxPayerTypeOutputPort;

    /**
     * {@inheritDoc}
     */
    @Override
    public List<TaxPayerType> getAllTaxPayerTypes() {
        return taxPayerTypeOutputPort.getAllTaxPayerTypes();     
    } 
}
