package co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.output.jpa.adapter;

import org.springframework.stereotype.Component;

import co.unicauca.edu.co.contables.inventory.peps.domain.port.output.command.IDetailOutputCommandOutPutPort;
import co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.output.jpa.repository.IDetailOutPutRepository;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DetailOutputCommandAdapter implements IDetailOutputCommandOutPutPort {

    private final IDetailOutPutRepository detailOutPutRepository;
    /**
     * @brief Deletes all detail output records from the database
     */
    @Override
    public void deleteAll() {
        detailOutPutRepository.deleteAll();
    }
    
}
