package co.unicauca.edu.co.contables.configuration.enterprise.application.ports.output;

import co.unicauca.edu.co.contables.configuration.enterprise.domain.models.TaxPayerType;

import java.util.List;

/**
 * Puerto de salida para la consulta de tipos de contribuyentes.
 * Define las operaciones necesarias para recuperar información sobre
 * los tipos de contribuyentes desde el sistema de almacenamiento.
 *
 * @author CONTAPP
 * @version 1.0
 * @since 1.0.0
 */
public interface ITaxPayerTypeOutputPort {
    
    /**
     * Recupera todos los tipos de contribuyentes registrados en el sistema.
     *
     * @return List<TaxPayerType> Lista de todos los tipos de contribuyentes disponibles
     * @see TaxPayerType
     */
    List<TaxPayerType> getAllTaxPayerTypes();
}