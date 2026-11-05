package co.unicauca.edu.co.contables.configuration.thirds.application.service.third;

import co.unicauca.edu.co.contables.configuration.thirds.application.ports.input.GetThirdUseCase;
import co.unicauca.edu.co.contables.configuration.thirds.application.ports.output.ThirdOutputPort;
import co.unicauca.edu.co.contables.configuration.thirds.domain.exceptions.third.ThirdNotFound;
import co.unicauca.edu.co.contables.configuration.thirds.domain.model.Third;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GetThirdService implements GetThirdUseCase {

    private final ThirdOutputPort thirdOutputPort;

    @Override
    public Third getThirdById(Long id, String entId) {
        return thirdOutputPort.getThirdById(id, entId)
                .orElseThrow(
                        () -> new ThirdNotFound("Tercero no encontrado con ID: " + id + " para la empresa: " + entId));
    }
    
    @Override
    public boolean existThirdById(long id, String entId) {
        return thirdOutputPort.existThirdById(id, entId);
    }
}
