package co.unicauca.edu.co.contables.accounting.catalogue.accounting.application.output;

import java.util.Optional;

import co.unicauca.edu.co.contables.accounting.catalogue.accounting.domain.models.PortfolioWriteOff;

public interface IPortfolioWriteOffPersistenceOutputPort {
    
    /**
     * Guarda o actualiza una instancia de PortfolioWriteOff en la base de datos.
     *
     * @param writeOff El objeto de dominio a persistir.
     * @return El objeto de dominio persistido, posiblemente con un ID asignado por la BD.
     */
    PortfolioWriteOff save(PortfolioWriteOff writeOff);

    /**
     * Busca un PortfolioWriteOff por su código de negocio único.
     *
     * @param code El código del castigo a buscar.
     * @return Un Optional que contiene el PortfolioWriteOff si se encuentra, o un Optional vacío si no.
     */
    Optional<PortfolioWriteOff> findByCode(String code);

    /**
     * Verifica si ya existe un PortfolioWriteOff con el código de negocio especificado.
     * Este método es crucial para garantizar la idempotencia.
     *
     * @param code El código del castigo a verificar.
     * @return true si ya existe, false en caso contrario.
     */
    boolean existsByCode(String code);
}
