package co.unicauca.edu.co.contables.accounting.catalogue.accounting.infraestructure.output.messageBroker.mapper;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import org.mapstruct.Mapper;

import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

import co.unicauca.edu.co.contables.accounting.catalogue.accounting.domain.models.PortfolioWriteOff;
import co.unicauca.edu.co.contables.accounting.catalogue.accounting.domain.models.WriteOffDetail;
import co.unicauca.edu.co.contables.accounting.catalogue.accounting.infraestructure.output.messageBroker.DTO.PortfolioWriteOffResponse;
import co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.input.rest.dto.response.WriteOffDetailResponse;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface IPortfolioWriteOffInboundMapper {
    @Mapping(source = "id", target = "originalWriteOffId") // El ID del DTO es el ID del origen
    @Mapping(target = "id", ignore = true) // El ID local se generará al guardar
    @Mapping(source = "details", target = "details", qualifiedByName = "mapDetails")
    PortfolioWriteOff toDomain(PortfolioWriteOffResponse dto);

    @Named("mapDetails")
    default List<WriteOffDetail> mapDetails(List<WriteOffDetailResponse> detailDtos) {
        if (detailDtos == null) {
            return null;
        }
        return detailDtos.stream()
                .map(this::mapDetail)
                .collect(Collectors.toList());
    }

    default WriteOffDetail mapDetail(WriteOffDetailResponse detailDto) {
        return WriteOffDetail.builder()
                .amountWrittenOff(BigDecimal.valueOf(detailDto.getAmountWrittenOff())) // Convertir Long a BigDecimal
                .invoiceId(detailDto.getInvoice().getId())
                .invoiceCode(detailDto.getInvoice().getFactCode())
                // --- ¡AQUÍ ESTÁ LA LÓGICA CLAVE! ---
                .creditInvoiceAccount(detailDto.getInvoice().getAccountingAccount())
                .build();
    }
}
