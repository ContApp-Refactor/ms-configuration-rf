package co.unicauca.edu.co.contables.inventory.kardex.infrastructure.adapters.output.jpa.adapter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import co.unicauca.edu.co.contables.inventory.kardex.domain.model.Kardex;
import co.unicauca.edu.co.contables.inventory.kardex.domain.model.MovementType;
import co.unicauca.edu.co.contables.inventory.kardex.domain.port.kardex.IKardexQueryRepositoryPort;
import co.unicauca.edu.co.contables.inventory.kardex.infrastructure.adapters.output.jpa.entity.KardexEntity;
import co.unicauca.edu.co.contables.inventory.kardex.infrastructure.adapters.output.jpa.mapper.IKardexEntityQueryMapper;
import co.unicauca.edu.co.contables.inventory.kardex.infrastructure.adapters.output.jpa.repository.IKardexRepository;

import lombok.RequiredArgsConstructor;

/**
 * @brief JPA adapter for Kardex query operations
 * 
 * Implements the repository port for querying Kardex data from the database.
 * Handles pagination, date filtering, and movement type filtering.
 */
@Repository("kardexKardexQueryAdapter")@RequiredArgsConstructor
public class KardexQueryAdapter implements IKardexQueryRepositoryPort {

    private final IKardexEntityQueryMapper kardexEntityMapper;
    private final IKardexRepository kardexRepository;

    /**
     * @brief Finds Kardex records by product ID within a date range
     * @param productId The product identifier
     * @param pageable Pagination parameters
     * @param startDate Start date for filtering
     * @param endDate End date for filtering
     * @return Paginated Kardex records
     */
    @Override
    public Page<Kardex> findProductIdAndDate(Long productId, Pageable pageable, LocalDate startDate, LocalDate endDate) {
        ZonedDateTime startDateTime = startDate.atStartOfDay(ZoneId.systemDefault());
        ZonedDateTime endDateTime = endDate.plusDays(1).atStartOfDay(ZoneId.systemDefault());
        Page<KardexEntity> kardexEntities = kardexRepository.findByProductIdAndDateBetween(productId, startDateTime, endDateTime, pageable);
        return kardexEntities.map(kardexEntityMapper::toDomain);
    }

    /**
     * @brief Finds Kardex records by product ID
     * @param productId The product identifier
     * @param pageable Pagination parameters
     * @return Paginated Kardex records
     */
    @Override
    public Page<Kardex> findProductId(Long productId, Pageable pageable) {
        Page<KardexEntity> kardexEntities = kardexRepository.findByProductId(productId, pageable);
        return kardexEntities.map(kardexEntityMapper::toDomain);
    }

    /**
     * @brief Finds Kardex records by fact code, product ID and movement type
     * @param factCode The fact/invoice code
     * @param productId The product identifier
     * @param type The movement type
     * @return List of matching Kardex records
     */
    @Override
    public List<Kardex> findByFactCodeAndProductIdAndType(String factCode, Long productId, MovementType type) {
        List<KardexEntity> kardexEntities = kardexRepository.findByFactCodeAndProductIdAndType(factCode, productId, type);
        return kardexEntityMapper.toDomainList(kardexEntities);
    }

    /**
     * @brief Gets the latest Kardex record for a product
     * @param productId The product identifier
     * @return Latest Kardex record or null if none exists
     */
    @Override
    public Kardex getLatestKardexByProductId(Long productId) {
        KardexEntity kardexEntity = kardexRepository.findTopByProductIdOrderByDateDesc(productId);
        return kardexEntity != null ? kardexEntityMapper.toDomain(kardexEntity) : null;
    }

    /**
     * @brief Gets the unit price of the latest Kardex record of a product among the given types
     * @param productId The product identifier
     * @param types Movement types to consider
     * @return Unit price or empty if none exists
     */
    @Override
    public Optional<BigDecimal> findLastEntryUnitPrice(Long productId, Collection<MovementType> types) {
        return kardexRepository.findTopByProductIdAndTypeInOrderByDateDescIdDesc(productId, types)
                .map(KardexEntity::getUnitPrice);
    }

    /**
     * @brief Gets the current weighted average (balance unit price) of the latest row with a positive balance
     * @param productId The product identifier
     * @return Balance unit price or empty if none exists
     */
    @Override
    public Optional<BigDecimal> findCurrentAverageUnitCost(Long productId) {
        return kardexRepository
                .findTopByProductIdAndBalanceUnitPriceGreaterThanOrderByDateDescIdDesc(productId, BigDecimal.ZERO)
                .map(KardexEntity::getBalanceUnitPrice);
    }

    /**
     * @brief Checks if a Kardex record exists for the given parameters
     * @param factCode The fact/invoice code
     * @param productId The product identifier
     * @param type The movement type
     * @return True if record exists, false otherwise
     */
    @Override
    public boolean existsByFactCodeAndProductIdAndType(String factCode, Long productId, MovementType type) {
        return kardexRepository.existsByFactCodeAndProductIdAndType(factCode, productId, type);
    }

    /**
     * @brief Gets the last kardex record for all products of an enterprise
     * @param enterpriseId Enterprise identifier to filter products
     * @return List of latest kardex records for each product
     */
    @Override
    public List<Kardex> findLastKardexForAllProducts(String enterpriseId) {
        List<KardexEntity> kardexEntities = kardexRepository.findLastKardexForAllProductsByEnterpriseId(enterpriseId);
        return kardexEntityMapper.toDomainList(kardexEntities);
    }
    
}
