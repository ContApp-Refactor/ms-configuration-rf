package co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.input.rest.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import co.unicauca.edu.co.contables.inventory.peps.domain.model.Kardex;
import co.unicauca.edu.co.contables.inventory.peps.domain.model.KardexMigration;
import co.unicauca.edu.co.contables.inventory.peps.domain.model.KardexReport;
import co.unicauca.edu.co.contables.inventory.peps.domain.model.Product;
import co.unicauca.edu.co.contables.inventory.peps.domain.model.SaleDetail;
import co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.input.rest.dto.response.KardexAvailableQuantityDTOResponse;
import co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.input.rest.dto.request.AdjustmentEntryDTORequest;
import co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.input.rest.dto.request.AdjustmentExitDTORequest;
import co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.input.rest.dto.response.AdjustmentEntryDTOResponse;
import co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.input.rest.dto.response.KardexRecordsDTOResponse;
import co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.input.rest.dto.response.AdjustmentExitDTOResponse;
import co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.input.rest.dto.response.ListLastProductKardexDtoResponse;
import co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.input.rest.dto.response.SaleDetailDTO;

@Mapper(componentModel ="spring")
public interface IKardexRestMapper {
    
    @Mapping(target = "product", source = "productId", qualifiedByName = "idToProduct")
   @Mapping(target = "date", ignore = true)
    @Mapping(target = "idKardex", ignore = true)
    @Mapping(target = "type", ignore = true)
    Kardex toDomain(AdjustmentEntryDTORequest adjustmentEntryDTORequest);

    @Mapping(target = "product", source = "productId", qualifiedByName = "idToProduct")
    @Mapping(target = "date", ignore = true)
    @Mapping(target = "idKardex", ignore = true)
    @Mapping(target = "type", ignore = true)
    Kardex toDomain(AdjustmentExitDTORequest AdjustmentDTORequest);


    AdjustmentEntryDTOResponse toDTOResponse(Kardex kardex);

    @Named("idToProduct")
    default Product mapIdToProduct(Long productId) {
        if (productId == null) {
            return null;
        }
        Product product = new Product();
        product.setProductId(productId); // Asignamos el ID al campo 'id' del producto
        return product;
    }

 
    AdjustmentExitDTOResponse toDTOResponseSale(Kardex Kardex);

    List<KardexRecordsDTOResponse> toDTORenponseRecords(List<KardexReport> kardex);

    List<AdjustmentEntryDTOResponse> toDTOSaleReturn(List<Kardex> sales);
    List<KardexAvailableQuantityDTOResponse> toKardexDTO(List<Kardex> listKardex);

    KardexRecordsDTOResponse toDTORecord(KardexReport kardexReport);
    
    // Mapea los detalles de salida
    SaleDetailDTO toSaleDetailDTO(SaleDetail saleDetail);
    
    // Mapea listas de detalles
    List<SaleDetailDTO> toSaleDetailDTOs(List<SaleDetail> saleDetails);

    List<ListLastProductKardexDtoResponse> toListLastProductKardexDtoResponseList(List<KardexMigration> kardexList);

    
}
