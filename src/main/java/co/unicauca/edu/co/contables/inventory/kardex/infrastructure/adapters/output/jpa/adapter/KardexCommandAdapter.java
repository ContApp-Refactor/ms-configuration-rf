package co.unicauca.edu.co.contables.inventory.kardex.infrastructure.adapters.output.jpa.adapter;

import org.springframework.stereotype.Repository;

import co.unicauca.edu.co.contables.inventory.kardex.domain.model.Kardex;
import co.unicauca.edu.co.contables.inventory.kardex.domain.port.kardex.IKardexCommandRepositoryPort;
import co.unicauca.edu.co.contables.inventory.kardex.infrastructure.adapters.output.jpa.entity.KardexEntity;
import co.unicauca.edu.co.contables.inventory.kardex.infrastructure.adapters.output.jpa.mapper.IKardexEntityCommandMapper;
import co.unicauca.edu.co.contables.inventory.kardex.infrastructure.adapters.output.jpa.repository.IKardexRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @brief JPA adapter for Kardex write operations
 * 
 * Implements the output port for persisting inventory movement records
 * using JPA repository and entity mapping.
 */
@Repository("kardexKardexCommandAdapter")@RequiredArgsConstructor
@Slf4j
public class KardexCommandAdapter implements IKardexCommandRepositoryPort{

    private final IKardexEntityCommandMapper kardexEntityMapper;
    private final IKardexRepository kardexRepository;

    /**
     * @brief Persists a purchase transaction to database
     * @param kardex Purchase record to save
     * @return Saved kardex with generated database identifiers
     */
    @Override
    public Kardex registerPurchase(Kardex kardex) {
        KardexEntity kardexEntity = kardexEntityMapper.toEntity(kardex);
        KardexEntity savedEntity = kardexRepository.save(kardexEntity);
        log.info("Registered purchase for product: {}", kardex.getProductId());
        return kardexEntityMapper.toDomain(savedEntity);
    }

    /**
     * @brief Persists a sale transaction to database
     * @param kardex Sale record to save
     * @return Saved kardex with generated database identifiers
     */
    @Override
    public Kardex registerSale(Kardex kardex) {
        KardexEntity kardexEntity = kardexEntityMapper.toEntity(kardex);
        KardexEntity savedEntity = kardexRepository.save(kardexEntity);
        log.info("Registered sale for product: {}", kardex.getProductId());
        return kardexEntityMapper.toDomain(savedEntity);
    }

    /**
     * @brief Persists a purchase return transaction to database
     * @param kardex Return record to save
     * @return Saved kardex with generated database identifiers
     */
    @Override
    public Kardex registerReturnOnPurchase(Kardex kardex) {
        KardexEntity kardexEntity = kardexEntityMapper.toEntity(kardex);
        KardexEntity savedEntity = kardexRepository.save(kardexEntity);
        log.info("Registered return on purchase for product: {}", kardex.getProductId());
        return kardexEntityMapper.toDomain(savedEntity);
    }

    /**
     * @brief Persists a sale return transaction to database
     * @param kardex Return record to save
     * @return Saved kardex with generated database identifiers
     */
    @Override
    public Kardex registerReturnOnSale(Kardex kardex) {
        KardexEntity kardexEntity = kardexEntityMapper.toEntity(kardex);
        KardexEntity savedEntity = kardexRepository.save(kardexEntity);
        log.info("Registered return on sale for product: {}", kardex.getProductId());
        return kardexEntityMapper.toDomain(savedEntity);
    }

    /**
     * @brief Deletes all kardex records from database
     */
    @Override
    public void deleteAll() {
        log.info("Deleting all kardex records from database");
        kardexRepository.deleteAll();
        log.info("All kardex records deleted from database");
    }

}
