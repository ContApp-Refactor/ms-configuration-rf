package co.unicauca.edu.co.contables.configuration.thirds.application.service.thirdType;

import co.unicauca.edu.co.contables.configuration.thirds.application.ports.input.CreateThirdTypeUseCase;
import co.unicauca.edu.co.contables.configuration.thirds.application.ports.output.IdOutputPort;
import co.unicauca.edu.co.contables.configuration.thirds.domain.exceptions.thirdType.ThirdTypeNameAlreadyExistsException;
import co.unicauca.edu.co.contables.configuration.thirds.domain.model.ThirdType;
import co.unicauca.edu.co.contables.configuration.thirds.domain.utils.StringNormalizer;
import co.unicauca.edu.co.contables.configuration.thirds.infrastructure.adapters.output.persistence.repository.ThirdTypeRepository;
import co.unicauca.edu.co.contables.configuration.commons.audit.annotation.Auditable;
import co.unicauca.edu.co.contables.configuration.commons.audit.annotation.OperationType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CreateThirdTypeService implements CreateThirdTypeUseCase {

    private final IdOutputPort idOutputPort;
    private final ThirdTypeRepository thirdTypeRepository;

    @Auditable(operationType = OperationType.CREATE, affectedTable = "THIRD_TYPE")
    @Override
    @Transactional
    public ThirdType createThirdType(ThirdType thirdType) {
        // Normalizar el nombre del tipo de tercero para almacenamiento
        ThirdType normalizedThirdType = ThirdType.builder()
                .thirdTypeName(StringNormalizer.normalizePreservingCase(thirdType.getThirdTypeName()))
                .entId(thirdType.getEntId())
                .status(thirdType.getStatus())
                .build();

        // Validar duplicados usando nombre normalizado (validación de negocio)
        validateDuplicateThirdTypeName(normalizedThirdType.getThirdTypeName(), normalizedThirdType.getEntId());

        // Guardar el tipo de tercero
        ThirdType createdThirdType = idOutputPort.saveThirdType(normalizedThirdType);

        return createdThirdType;
    }

    /**
     * @brief Valida que no exista un tipo de tercero con el mismo nombre
     * @param normalizedThirdTypeName nombre del tipo de tercero ya normalizado
     * @param entId identificador de la entidad
     * @throws ThirdTypeNameAlreadyExistsException si ya existe un tipo de tercero con el mismo nombre
     */
    private void validateDuplicateThirdTypeName(String normalizedThirdTypeName, String entId) {
        if (thirdTypeRepository.existsByTtNameIgnoreCaseAndTtentId(normalizedThirdTypeName, entId)) {
            throw new ThirdTypeNameAlreadyExistsException(normalizedThirdTypeName);
        }
    }

}
