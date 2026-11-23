package co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.output.jpa.adapter;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import co.unicauca.edu.co.contables.inventory.peps.domain.model.Kardex;
import co.unicauca.edu.co.contables.inventory.peps.domain.model.KardexMigration;
import co.unicauca.edu.co.contables.inventory.peps.domain.port.output.query.IKardexQueryOutputPort;
import co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.output.jpa.entity.KardexEntity;
import co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.output.jpa.mapper.IKardexEntityQueryMapper;
import co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.output.jpa.repository.IKardexRepository;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
/**
 * @brief Adapter for Kardex query operations
 * 
 * Implements the output port for retrieving Kardex information
 * from the database using JPA repository.
 */
public class KardexQueryAdapter implements IKardexQueryOutputPort{
    private final IKardexEntityQueryMapper kardexEntityQueryMapper;
    private final IKardexRepository kardexRepository;

    /**
     * @brief Retrieves all Kardex entries for a product
     * @param idProduct The product ID
     * @return List of Kardex entries
     */
    @Override
    public List<Kardex> getAllKardex(Long idProduct) {
        List<KardexEntity> listKardex=kardexRepository.findByProductId(idProduct);
        return kardexEntityQueryMapper.toDomainList(listKardex);
    }

    /**
     * @brief Calculates total available amount for a product
     * @param idProduct The product ID
     * @return Total available quantity
     */
    @Override
    public int getAvailableAmountByProduct(Long idProduct) {
       return  kardexRepository.sumAvailableAmountByProduct(idProduct);
    }

    /**
     * @brief Finds the first record with available amount (FIFO)
     * @param idProduct The product ID
     * @return Optional containing the first available lot
     */
    @Override
    public Optional<Kardex> getFirstRecordByAmountAvailable(Long idProduct) {
        
        return kardexRepository.findFirstByAvailableAmount(idProduct)
        .map(kardexEntityQueryMapper::toDomain);
    }

    /**
     * @brief Finds movements within a date range
     * @param productId The product ID
     * @param startDate Start date
     * @param endDate End date
     * @return List of movements in range
     */
    @Override
    public List<Kardex> findMovementsByProductAndDateRange(Long productId, LocalDate startDate, LocalDate endDate) {
        ZonedDateTime startDateTime=startDate.atStartOfDay(ZoneId.systemDefault());
        ZonedDateTime endDateTime=endDate.plusDays(1).atStartOfDay(ZoneId.systemDefault()); 
      return kardexRepository.findMovementsInDateRange(productId, startDateTime, endDateTime)
      .stream().map(kardexEntityQueryMapper::toDomain)
      .collect(Collectors.toList());
    }

    /**
     * @brief Finds movements before a specific date
     * @param productId The product ID
     * @param startDate The cutoff date
     * @return List of movements before the date
     */
    @Override
    public List<Kardex> findMovementsByProductBeforeDate(Long productId, LocalDate startDate) {
        ZonedDateTime startDateTime=startDate.atStartOfDay(ZoneId.systemDefault());
        return kardexRepository.findMovementsBeforeDate(productId,startDateTime)
        .stream().map(kardexEntityQueryMapper::toDomain)
        .collect(Collectors.toList());
    }

    /**
     * @brief Finds a Kardex entry by invoice reference and product
     * @param factCode Invoice code
     * @param productId Product ID
     * @return Optional containing the Kardex entry
     */
    @Override
    public Optional<Kardex> findByRefFacture(String factCode, Long productId) {
        return kardexRepository
            .findFirstByFactCodeAndProduct_ProductIdOrderByDateAsc(factCode, productId)
            .map(kardexEntityQueryMapper::toDomain);
    }

    /**
     * @brief Finds available purchases ordered by date (FIFO)
     * @param idProduct The product ID
     * @return List of available purchase entries
     */
    @Override
    public List<Kardex> findAvailablePurchasesOrderedByDate(Long idProduct) {
        List<KardexEntity> kardex=this.kardexRepository.findAvailablePurchasesOrderedByDate(idProduct);
        return  kardexEntityQueryMapper.toDomainList(kardex);
       
    }

    /**
     * @brief Finds a Kardex entry by ID
     * @param id The ID to search for
     * @return Optional containing the Kardex entry
     */
    @Override
    public Optional<Kardex> findById(Long id) {
        return kardexRepository.findById(id)
        .map(kardexEntityQueryMapper::toDomain);
    }
    
    /**
     * @brief Finds the last Kardex entry for all products of an enterprise
     * @param enterpriseId The enterprise ID
     * @return List of migration data
     */
    @Override
    public List<KardexMigration> findLastKardexForAllProducts(String enterpriseId) {
       List<KardexMigration> kardexMigrations=kardexRepository.findLastKardexForAllProductsByEnterpriseId(enterpriseId);
       return kardexMigrations;
    }

    /**
     * @brief Gets the latest Kardex entry for a product
     * @param productId The product ID
     * @return The latest Kardex entry or null
     */
    @Override
    public Kardex getLatestKardexByProductId(Long productId) {
      
        KardexEntity kardexEntity=kardexRepository.findTopByProductIdOrderByDateDesc(productId);
        return kardexEntity != null ? kardexEntityQueryMapper.toDomain(kardexEntity):null;

    }

    /**
     * @brief Checks if any Kardex entries exist for a product
     * @param productId The product ID
     * @return True if exists, false otherwise
     */
    @Override
    public boolean existsByProduct_ProductId(Long productId) {
      return kardexRepository.existsByProduct_ProductId(productId);
    }

  
    
}
