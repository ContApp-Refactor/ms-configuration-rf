package co.unicauca.edu.co.contables.accounting.catalogue.accounting.infraestructure.output.jpaAdapter.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import co.unicauca.edu.co.contables.accounting.catalogue.accounting.infraestructure.output.jpaAdapter.entity.AccountingMovementEntity;

public interface IAccountingMovementRepository extends JpaRepository<AccountingMovementEntity, Long> {
    // NUEVO: Buscar movimientos por ID de cuenta contable
    List<AccountingMovementEntity> findByAccount(Long accountId);

    // NUEVO: Buscar movimientos por ID de tercero
    //List<AccountingMovementEntity> findByThirdPartyId(Long thirdPartyId);

     // Aseguramos que la entidad 'accountingEntry' se cargue en la misma consulta.
    @Query("SELECT m FROM AccountingMovementEntity m LEFT JOIN FETCH m.accountingEntry WHERE m.thirdPartyId = :thirdPartyId")
    List<AccountingMovementEntity> findByThirdPartyId(@Param("thirdPartyId") Long thirdPartyId);
    
    
    /**
     * Busca todos los movimientos para un tercero específico que pertenecen a un conjunto de cuentas contables.
     * 
     * @param thirdId      El ID del tercero (cliente).
     * @param accountCodes La lista de códigos de las cuentas de cartera a buscar.
     * @return Una lista de entidades de movimiento contable.
     */
    @Query("SELECT m FROM AccountingMovementEntity m LEFT JOIN FETCH m.accountingEntry WHERE m.thirdPartyId = :thirdPartyId")
    List<AccountingMovementEntity> findByThirdAndAccountCodes(@Param("thirdPartyId") Long thirdPartyId);
}
