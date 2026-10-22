package co.unicauca.edu.co.contables.accounting.catalogue.accounting.infraestructure.input.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import co.unicauca.edu.co.contables.accounting.catalogue.accounting.domain.models.AccountingEntry;
import co.unicauca.edu.co.contables.accounting.catalogue.accounting.domain.models.AccountingMovement;
import co.unicauca.edu.co.contables.accounting.catalogue.accounting.domain.models.InvoiceReplica;
import co.unicauca.edu.co.contables.accounting.catalogue.accounting.infraestructure.input.data.response.AccountingEntryResponse;
import co.unicauca.edu.co.contables.accounting.catalogue.accounting.infraestructure.input.data.response.AccountingMovementResponse;
import co.unicauca.edu.co.contables.accounting.catalogue.accounting.infraestructure.input.data.response.InvoiceDetailResponse;

@Mapper(componentModel = "spring")
public interface IAccountingRestMapper {
    /**
     * Convierte un objeto de dominio AccountingEntry a su DTO de respuesta.
     * MapStruct es lo suficientemente inteligente como para ver que `movements` es una lista
     * y usará automáticamente el método `toMovementResponse` para cada elemento.
     */
    AccountingEntryResponse toEntryResponse(AccountingEntry accountingEntry);

    /**
     * Convierte un objeto de dominio AccountingMovement a su DTO de respuesta.
     * Como los nombres de los campos coinciden, no se necesitan anotaciones @Mapping.
     */
    AccountingMovementResponse toMovementResponse(AccountingMovement accountingMovement);

    // --- MÉTODO NUEVO PARA EL REPORTE DE CARTERA ---
    /**
     * Convierte un objeto de dominio InvoiceReplica (que representa una factura)
     * al DTO InvoiceDetailResponse que se enviará al frontend.
     * 
     * @param invoice El objeto de dominio de la factura.
     * @return El DTO con los datos de la factura para la API.
     */
    @Mapping(source = "thirdId", target = "clientId") // Mapea el campo 'thirdId' a 'clientId' en la respuesta
    @Mapping(target = "daysInArrears", ignore = true)
    InvoiceDetailResponse toInvoiceDetailResponse(InvoiceReplica invoice);
    // ------------------------------------------------
}
