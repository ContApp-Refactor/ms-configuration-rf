package co.unicauca.edu.co.contables.inventory.peps.application.service.command;

import org.springframework.stereotype.Service;

import co.unicauca.edu.co.contables.inventory.peps.application.ports.input.IDetailOutputCommandPort;
import co.unicauca.edu.co.contables.inventory.peps.domain.port.output.command.IDetailOutputCommandOutPutPort;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DetailOutputCommandService implements IDetailOutputCommandPort {
    private final IDetailOutputCommandOutPutPort detailOutputCommandOutPutPort;

    @Override
    public void deleteAll() {
        detailOutputCommandOutPutPort.deleteAll();
    }

}
