package co.unicauca.edu.co.contables.accounting.catalogue.accounting.infraestructure.output.messageBroker.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;


import co.unicauca.edu.co.contables.accounting.catalogue.accounting.domain.models.InvoiceReplica;
import co.unicauca.edu.co.contables.accounting.catalogue.accounting.infraestructure.output.messageBroker.DTO.InvoiceSyncDto;

@Mapper(componentModel = "spring") // Para que Spring pueda inyectarlo
public interface IInvoiceEventMapper {

    

    /**
     * Convierte el DTO que llega del evento de RabbitMQ a un objeto de dominio InvoiceReplica.
     * El 'id' y el 'status' del dominio no se mapean desde el DTO, lo cual es correcto
     * ya que serán gestionados por la lógica de negocio y la persistencia.
     * @param dto El objeto de transferencia de datos del evento.
     * @return Un objeto de dominio InvoiceReplica.
     */
    @Mapping(target = "id", ignore = true) // El ID se generará al persistir
    @Mapping(target = "status", ignore = true) // El estado se definirá por la lógica de negocio
    InvoiceReplica toDomain(InvoiceSyncDto dto);
}
