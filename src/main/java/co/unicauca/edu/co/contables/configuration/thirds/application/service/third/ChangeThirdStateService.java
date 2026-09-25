package co.unicauca.edu.co.contables.configuration.thirds.application.service.third;

import co.unicauca.edu.co.contables.configuration.thirds.application.ports.input.ChangeThirdStateUseCase;
import co.unicauca.edu.co.contables.configuration.thirds.application.ports.output.ThirdOutputPort;
import co.unicauca.edu.co.contables.configuration.thirds.domain.exceptions.third.ThirdStateNotChanged;
import co.unicauca.edu.co.contables.configuration.commons.audit.annotation.Auditable;
import co.unicauca.edu.co.contables.configuration.commons.audit.annotation.OperationType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChangeThirdStateService implements ChangeThirdStateUseCase {

    private final ThirdOutputPort thirdOutputPort;

    @Auditable(operationType = OperationType.INACTIVATE, affectedTable = "THIRD", idArgIndex = 0, enterpriseIdArgIndex = 1)
    @Override
    public boolean changeThirdState(Long thId, String entId) {
        boolean result = thirdOutputPort.changeThirdState(thId, entId);

        if (!result) {
            throw new ThirdStateNotChanged("El estado no se pudo cambiar debido a un error interno");
        }

        return result;
    }

}
