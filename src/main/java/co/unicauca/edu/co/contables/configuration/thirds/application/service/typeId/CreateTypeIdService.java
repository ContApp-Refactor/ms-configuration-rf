package co.unicauca.edu.co.contables.configuration.thirds.application.service.typeId;

import co.unicauca.edu.co.contables.configuration.thirds.application.ports.input.CreateTypeIdUseCase;
import co.unicauca.edu.co.contables.configuration.thirds.application.ports.output.IdOutputPort;
import co.unicauca.edu.co.contables.configuration.thirds.domain.model.TypeId;
import co.unicauca.edu.co.contables.configuration.commons.audit.annotation.Auditable;
import co.unicauca.edu.co.contables.configuration.commons.audit.annotation.OperationType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CreateTypeIdService implements CreateTypeIdUseCase {

    private final IdOutputPort idOutputPort;

    /**
     * @brief Crea un nuevo tipo de identificación en el sistema
     * @param typeId tipo de identificación a crear
     * @return tipo de identificación creado con su ID asignado
     */
    @Auditable(operationType = OperationType.CREATE, affectedTable = "TYPE_ID")
    @Override
    public TypeId createTypeId(TypeId typeId) {
        TypeId createdTypeId = idOutputPort.saveTypeId(typeId);
        return createdTypeId;
    }

}
