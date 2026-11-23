package co.unicauca.edu.co.contables.configuration.thirds.application.service.thirdType;

import co.unicauca.edu.co.contables.configuration.thirds.application.ports.input.ListThirdTypeUseCase;
import co.unicauca.edu.co.contables.configuration.thirds.application.ports.output.IdOutputPort;
import co.unicauca.edu.co.contables.configuration.thirds.domain.model.ThirdType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ListThirdTypeService implements ListThirdTypeUseCase {

    private final IdOutputPort idOutputPort;

    @Override
    public List<ThirdType> getAllThirdTypes(String entId) {
        return idOutputPort.getALLThirdTypes(entId);
    }

  
    @Override
    public Page<ThirdType> getAllThirdTypesWithSort(String entId, int page, int size, String sortField, String sortOrder) {
        return idOutputPort.getAllThirdTypesWithSort(entId, page, size, sortField, sortOrder);
    }
  
    @Override
    public Page<ThirdType> findThirdTypesByEntIdAndSearch(String entId, String search, int page, int size, String sortField, String sortOrder) {
        return idOutputPort.findThirdTypesByEntIdAndSearch(entId, search, page, size, sortField, sortOrder);
    }
   
    @Override
    public long countThirdTypesByEntId(String entId) {
        return idOutputPort.countThirdTypesByEntId(entId);
    }
   
    @Override
    public long countThirdTypesByEntIdAndSearch(String entId, String search) {
        return idOutputPort.countThirdTypesByEntIdAndSearch(entId, search);
    }
   
    @Override
    public long countActiveThirdTypesByEntId(String entId) {
        return idOutputPort.countActiveThirdTypesByEntId(entId);
    }
   
    @Override
    public Page<ThirdType> getAllActiveThirdTypes(String entId, int page, int size) {
        return idOutputPort.getAllActiveThirdTypes(entId, page, size);
    }

}
