package co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.output.jpa.adapter;

import java.math.BigDecimal;
import java.util.List;
import org.springframework.stereotype.Component;
import co.unicauca.edu.co.contables.inventory.peps.domain.model.DetailOutput;
import co.unicauca.edu.co.contables.inventory.peps.domain.port.output.query.IDetailQueryOutPutPort;
import co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.output.jpa.entity.DetailOutputEntity;
import co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.output.jpa.mapper.IDetailOutPutSaleReturnMapper;
import co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.output.jpa.repository.IDetailOutPutRepository;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
/**
 * @brief Adapter for querying detail output data
 * 
 * Implements the output port for retrieving detail output information
 * from the database using JPA repository.
 */
public class DetailOutputQueryAdapter implements IDetailQueryOutPutPort {

    private final IDetailOutPutRepository detailOutPutRepository;
    private final IDetailOutPutSaleReturnMapper detailOutPutSaleReturnMapper;

    /**
     * @brief Finds detail outputs by movement sale ID
     * @param idMovementsale The ID of the movement sale
     * @return List of detail outputs associated with the sale
     */
    @Override
    public List<DetailOutput>  findByMovementSale(Long idMovementsale) {
        return detailOutPutSaleReturnMapper.toDomainList(detailOutPutRepository.findByMovementSaleId(idMovementsale));
    }

    /**
     * @brief Deletes a detail output by ID
     * @param id The ID of the detail output to delete
     */
    @Override
    public void deleteById(Long id) {
        detailOutPutRepository.deleteById(id);
    }

    /**
     * @brief Finds detail outputs by movement sale ID ordered descending
     * @param idKardexSale The ID of the kardex sale
     * @return List of detail outputs ordered by ID descending
     */
    @Override
    public List<DetailOutput> findByMovementSaleOrderedDesc(Long idKardexSale) {
         
        List<DetailOutputEntity> entities = detailOutPutRepository.findByMovementSaleOrderedDesc(idKardexSale);
        return detailOutPutSaleReturnMapper.toDomainList(entities);
        
    }

    /**
     * @brief Updates a detail output
     * @param detail The detail output to update
     * @return The updated detail output
     */
    @Override
    public DetailOutput update(DetailOutput detail) {
       DetailOutputEntity entity = detailOutPutSaleReturnMapper.toEntity(detail);
        DetailOutputEntity saved = detailOutPutRepository.save(entity);
        return detailOutPutSaleReturnMapper.toDomain(saved);
    }
    
    /**
     * @brief Updates quantity and price of a detail output
     * @param detailId The ID of the detail output
     * @param newQuantity The new quantity
     * @param newUnitPrice The new unit price
     */
    @Override
    public void updateQuantityAndPrice(Long detailId, int newQuantity, BigDecimal newUnitPrice) {
        detailOutPutRepository.updateQuantityAndPrice(detailId, newQuantity, newUnitPrice);
    }
    
}

