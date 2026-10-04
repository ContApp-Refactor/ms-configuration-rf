package co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.output.jpa.adapter;

import java.util.List;

import org.springframework.stereotype.Component;

import jakarta.transaction.Transactional;
import co.unicauca.edu.co.contables.inventory.peps.domain.model.DetailOutput;
import co.unicauca.edu.co.contables.inventory.peps.domain.model.Kardex;
import co.unicauca.edu.co.contables.inventory.peps.domain.port.output.command.IKardexCommandOutputPort;
import co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.output.jpa.entity.DetailOutputEntity;
import co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.output.jpa.entity.KardexEntity;
import co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.output.jpa.entity.ProductEntity;
import co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.output.jpa.mapper.IKardexEntityCommandMapper;
import co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.output.jpa.repository.IDetailOutPutRepository;
import co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.output.jpa.repository.IKardexRepository;
import co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.output.jpa.repository.IProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
/**
 * @brief Adapter for Kardex command operations
 * 
 * Implements the output port for performing write operations on Kardex entries,
 * including registration of purchases, sales, returns, and non-commercial movements.
 */
public class KardexCommandAdapter implements IKardexCommandOutputPort {
    private final IKardexEntityCommandMapper kardexEntityCommandMapper;
    private final IKardexRepository kardexRepository;
    private final IDetailOutPutRepository detailOutPutRepository;
    private final IProductRepository productRepository;

    /**
     * @brief Registers a purchase in the Kardex
     * @param kardex The purchase entry to register
     * @return The registered Kardex entry
     */
    @Override
    public Kardex registerPurchase(Kardex kardex) {
        ProductEntity productEntity=productRepository.getReferenceById(kardex.getProduct().getId());
        KardexEntity kardexEntity=kardexEntityCommandMapper.toEntity(kardex);
        kardexEntity.setProduct(productEntity);
       return kardexEntityCommandMapper.toDomain(kardexRepository.save(kardexEntity));
    }

    /**
     * @brief Registers a sale and updates affected lots
     * @param kardex The sale entry to register
     * @param lotsToUpdate List of lots (purchases) to update available amounts
     * @return The registered sale entry
     */
    @Override
    @Transactional
    public Kardex registerSale(Kardex kardex, List<Kardex> lotsToUpdate) {
        ProductEntity productEntity=productRepository.getReferenceById(kardex.getProduct().getId());



        //actualizar el lote
        for(Kardex domainLot:lotsToUpdate){
            KardexEntity entityToUpdate=kardexEntityCommandMapper.toEntity(domainLot);
            entityToUpdate.setProduct(productEntity);
            kardexRepository.save(entityToUpdate);
        }

        //Guardamos primero el kardex de la venta
        KardexEntity kardexEntity=kardexEntityCommandMapper.toEntity(kardex);
        kardexEntity.setProduct(productEntity);
        kardexEntity=kardexRepository.save(kardexEntity);


        //Guardar detalles de la venta
        for(DetailOutput detail:kardex.getDetailsOutput()){
            DetailOutputEntity detailEntity=new DetailOutputEntity();
            detailEntity.setQuantityUsed(detail.getQuantityUsed());
            detailEntity.setUnitPrice(detail.getUnitPrice());

            //movimientos de venta
            detailEntity.setMovementSale(kardexEntity);

            // Usar el ID del movimiento de origen desde la referencia del objeto
            KardexEntity originEntity=kardexRepository.getReferenceById(detail.getMovementOrigin().getIdKardex());
            detailEntity.setMovementOrigin(originEntity);

            detailOutPutRepository.save(detailEntity);

        }
       
        return kardexEntityCommandMapper.toDomain(kardexEntity);
    }

    /**
     * @brief Registers a purchase return
     * @param kardex The purchase return entry
     * @return The registered return entry
     */
    @Override
    @Transactional
    public Kardex registerPurchaseReturn(Kardex kardex){
        
        ProductEntity productEntity=productRepository.getReferenceById(kardex.getProduct().getId());
        KardexEntity kardexEntity=kardexEntityCommandMapper.toEntity(kardex);
        kardexEntity.setProduct(productEntity);
        return kardexEntityCommandMapper.toDomain(kardexRepository.save(kardexEntity));
        
    }

    /**
     * @brief Registers a sale return
     * @param kardex The sale return entry
     * @return The registered return entry
     */
    @Override
    @Transactional
    public Kardex registerSaleReturn(Kardex kardex) {
        
        ProductEntity productEntity=productRepository.getReferenceById(kardex.getProduct().getId());
        KardexEntity kardexEntity=kardexEntityCommandMapper.toEntity(kardex);
        kardexEntity.setProduct(productEntity);
        return kardexEntityCommandMapper.toDomain(kardexRepository.save(kardexEntity));
    }

    /**
     * @brief Updates the available amount of a Kardex entry
     * @param idKardex The ID of the Kardex entry
     * @param newAmount The new available amount
     * @return Number of rows affected
     */
    @Override
    public int updateAvaliableAmount(Long idKardex, int newAmount) {
        return kardexRepository.updateAvaliableAmount(idKardex, newAmount);
    }

    /**
     * @brief Registers a non-commercial exit
     * @param kardex The exit entry
     * @param lotsToUpdate List of lots to update
     * @return The registered exit entry
     */
    @Override
    public Kardex registerNonCommercialExit(Kardex kardex,List<Kardex> lotsToUpdate) {
       
         ProductEntity productEntity=productRepository.getReferenceById(kardex.getProduct().getId());



        //actualizar el lote
        for(Kardex domainLot:lotsToUpdate){
            KardexEntity entityToUpdate=kardexEntityCommandMapper.toEntity(domainLot);
            entityToUpdate.setProduct(productEntity);
            kardexRepository.save(entityToUpdate);
        }

        //Guardamos primero el kardex de la venta
        KardexEntity kardexEntity=kardexEntityCommandMapper.toEntity(kardex);
        kardexEntity.setProduct(productEntity);
        kardexEntity=kardexRepository.save(kardexEntity);


        //Guardar detalles de la venta
        for(DetailOutput detail:kardex.getDetailsOutput()){
            DetailOutputEntity detailEntity=new DetailOutputEntity();
            detailEntity.setQuantityUsed(detail.getQuantityUsed());
            detailEntity.setUnitPrice(detail.getUnitPrice());

            //movimientos de venta
            detailEntity.setMovementSale(kardexEntity);

            // Usar el ID del movimiento de origen desde la referencia del objeto
            KardexEntity originEntity=kardexRepository.getReferenceById(detail.getMovementOrigin().getIdKardex());
            detailEntity.setMovementOrigin(originEntity);

            detailOutPutRepository.save(detailEntity);

        }
       
        return kardexEntityCommandMapper.toDomain(kardexEntity);

    }

    /**
     * @brief Registers a non-commercial entry
     * @param kardex The entry to register
     * @return The registered entry
     */
    @Override
    public Kardex registerNonCommercialEntry(Kardex kardex) {
        ProductEntity productEntity=productRepository.getReferenceById(kardex.getProduct().getId());
        KardexEntity kardexEntity=kardexEntityCommandMapper.toEntity(kardex);
        kardexEntity.setProduct(productEntity);
       return kardexEntityCommandMapper.toDomain(kardexRepository.save(kardexEntity));
    }

    /**
     * @brief Deletes all Kardex records
     */
    @Override
    public void deleteAll() {
        log.info("Deleting all kardex records from database");
        kardexRepository.deleteAll();
        log.info("All kardex records deleted from database");
    }
    
   

}
