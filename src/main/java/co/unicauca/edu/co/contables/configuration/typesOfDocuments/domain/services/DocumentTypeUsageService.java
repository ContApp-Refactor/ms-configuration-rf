package co.unicauca.edu.co.contables.configuration.typesOfDocuments.domain.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

import co.unicauca.edu.co.contables.configuration.typesOfDocuments.domain.models.DocumentType;

/**
 * @brief Servicio para manejo del contador de uso de tipos de documento
 *
 * Implementa el caso de uso de incremento de contador de uso.
 * Mantiene la separación arquitectónica entre adaptadores de entrada (listeners)
 * y adaptadores de salida (persistencia).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentTypeUsageService implements IDocumentTypeUsage {

    private final IDocumentTypeService documentTypeService;

    @Override
    public void incrementUsageCount(Long documentTypeId, String enterpriseId) {
        log.info("Incrementing usage count for documentTypeId: {} in enterprise: {}", documentTypeId, enterpriseId);

        // Obtener el tipo de documento actual para incrementar el contador
        DocumentType currentDocumentType = documentTypeService.findById(documentTypeId, enterpriseId);
        Integer newUsageCount = currentDocumentType.getUsageCount() + 1;

        documentTypeService.updateUsageCount(documentTypeId, enterpriseId, newUsageCount);

        log.info("Usage count incremented successfully for documentTypeId: {}", documentTypeId);
    }
}
