package co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.input.rest.mapper;

import co.unicauca.edu.co.contables.accounting.debt_payments.domain.model.PortfolioWriteOff;
import co.unicauca.edu.co.contables.accounting.debt_payments.domain.model.WriteOffDetail;
import co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.input.rest.dto.request.CreateWriteOffRequest;
import co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.input.rest.dto.request.WriteOffDetailRequest;
import co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.input.rest.dto.response.PortfolioWriteOffResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

/**
 * Mapper interface for converting PortfolioWriteOff domain models to REST request and response DTOs.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface IPortfolioWriteOffRestMapper {
        
        PortfolioWriteOff toDomain(CreateWriteOffRequest request);

        WriteOffDetail toDomain(WriteOffDetailRequest detailRequest);

        /**
         * Convierte un modelo de dominio a un DTO de respuesta.
         * OJO: Este método asume que el enriquecimiento (datos de facturas) ya se ha
         * hecho.
         * Nuestro servicio devuelve el DTO enriquecido, por lo que este mapper
         * no será usado directamente para la conversión compleja, pero es bueno
         * tenerlo.
         */
        @Mapping(target = "details", ignore = true)
        PortfolioWriteOffResponse toResponse(PortfolioWriteOff domain);

        List<PortfolioWriteOffResponse> toResponseList(List<PortfolioWriteOff> domainList);
}
