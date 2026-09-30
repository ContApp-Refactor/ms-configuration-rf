package co.unicauca.edu.co.contables.accounting.catalogue.accounting.infraestructure.output.jpaAdapter;

import java.util.Optional;

import org.springframework.stereotype.Component;

import co.unicauca.edu.co.contables.accounting.catalogue.accounting.application.output.IReceiptPersistenceOutputPort;
import co.unicauca.edu.co.contables.accounting.catalogue.accounting.domain.models.Receipt;
import co.unicauca.edu.co.contables.accounting.catalogue.accounting.infraestructure.output.jpaAdapter.entity.ReceiptEntity;
import co.unicauca.edu.co.contables.accounting.catalogue.accounting.infraestructure.output.jpaAdapter.mapper.IReceiptMapper;
import co.unicauca.edu.co.contables.accounting.catalogue.accounting.infraestructure.output.jpaAdapter.repository.IReceiptRepository;

import lombok.AllArgsConstructor;

@Component
@AllArgsConstructor
public class ReceiptPersistenceJpaAdapter implements IReceiptPersistenceOutputPort {
    private final IReceiptRepository receiptRepository;
    private final IReceiptMapper receiptMapper;

    public Receipt save(Receipt receipt) {
        // La lógica para distinguir entre crear y actualizar es la presencia del ID.
        if (receipt.getOriginalReceiptId() == null) {
            // --- CASO DE CREACIÓN ---
            // El receipt no tiene ID, es una nueva entidad.
            ReceiptEntity newReceiptEntity = receiptMapper.toEntity(receipt);
            ReceiptEntity savedEntity = receiptRepository.save(newReceiptEntity);
            return receiptMapper.toDomain(savedEntity);
        } else {
            // --- CASO DE ACTUALIZACIÓN ---
            // El receipt ya tiene un ID, por lo que debemos actualizar el registro existente.
            
            // 1. Buscamos la entidad existente en la base de datos.
            ReceiptEntity existingEntity = receiptRepository.findById(receipt.getId())
                    .orElseThrow(() -> new IllegalStateException("Se intentó actualizar un recibo con ID " + receipt.getId() + " que no existe."));

            // 2. Usamos el mapper para actualizar los campos de la entidad existente
            //    con los valores del objeto de dominio.
            //    ¡Asegúrate de que tu IReceiptMapper tiene un método updateEntityFromDomain!
            receiptMapper.updateEntityFromDomain(receipt, existingEntity);

            // 3. Guardamos la entidad actualizada.
            ReceiptEntity updatedEntity = receiptRepository.save(existingEntity);
            return receiptMapper.toDomain(updatedEntity);
        }
    }

    @Override
    public Optional<Receipt> findByOriginalReceiptId(Long originalReceiptId) {
        return receiptRepository.findByOriginalReceiptId(originalReceiptId)
                .map(receiptMapper::toDomain);
    }

    @Override
    public Optional<Receipt> findByReceiptCode(String receiptCode) {
        return receiptRepository.findByReceiptCode(receiptCode)
                .map(receiptMapper::toDomain);
    }

    @Override
    public boolean existsByReceiptCode(String receiptCode) {
        return receiptRepository.existsByReceiptCode(receiptCode);
    }

}
