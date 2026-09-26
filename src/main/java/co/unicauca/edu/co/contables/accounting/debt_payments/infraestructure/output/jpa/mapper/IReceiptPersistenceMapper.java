package co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.output.jpa.mapper;

import co.unicauca.edu.co.contables.accounting.debt_payments.domain.model.Receipt;
import co.unicauca.edu.co.contables.accounting.debt_payments.domain.model.ReceiptDetail;
import co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.output.jpa.entity.ReceiptDetailEntity;
import co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.output.jpa.entity.ReceiptEntity;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

import java.util.List;


/**
 * Mapper interface for converting between Receipt domain models and Receipt JPA entities.
 * Utilizes MapStruct for automatic generation of mapping implementations.
 */
@Mapper(componentModel = "spring", 
        unmappedTargetPolicy = ReportingPolicy.IGNORE) 
public interface IReceiptPersistenceMapper {

    Receipt toDomain(ReceiptEntity entity);
    List<Receipt> toDomainList(List<ReceiptEntity> entityList);
    ReceiptDetail toDomain(ReceiptDetailEntity detailEntity);

    ReceiptEntity toEntity(Receipt domain);
    ReceiptDetailEntity toEntity(ReceiptDetail detailDomain);
    List<ReceiptEntity> toEntityList(List<Receipt> domainList);

    /**
     * Esta anotación le dice a MapStruct que ejecute este método DESPUÉS de haber mapeadoun objeto Receipt a un ReceiptEntity.
     * @param receiptEntity El objeto de destino (@MappingTarget) que acaba de ser creado.
     * 
     * El método itera sobre cada detalle (hijo) y le asigna una referencia a su recibo (padre).
     * Esto asegura que cuando JPA intente guardar, el vínculo bidireccional esté completoy la columna de la clave foránea 'receipt_id' se rellene correctamente.
     */
    @AfterMapping
    default void linkDetails(@MappingTarget ReceiptEntity entity) {
        if (entity.getDetails() != null) {
            entity.getDetails().forEach(detail -> detail.setReceipt(entity));
        }
    }
}
