package co.unicauca.edu.co.contables.configuration.thirds.application.service.third;

import co.unicauca.edu.co.contables.configuration.thirds.application.ports.input.ListThirdsUseCase;
import co.unicauca.edu.co.contables.configuration.thirds.application.ports.output.ThirdOutputPort;
import co.unicauca.edu.co.contables.configuration.thirds.domain.model.Third;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ListThirdsService implements ListThirdsUseCase {

    private final ThirdOutputPort thirdOutputPort;
    
    @Override
    public Page<Third> getAllThirdsBy(String entId, Pageable pageable) {
        return thirdOutputPort.getAllThirdsBy(entId, pageable);
    }

   
    @Override
    public long countAllThirdsByEntId(String entId) {
        return thirdOutputPort.countAllThirdsByEntId(entId);
    }


    @Override
    public Page<Third> findByEntIdAndSearch(String entId, String search, int page, int size, String sortField, String sortOrder) {
        return thirdOutputPort.findByEntIdAndSearch(entId, search, page, size, sortField, sortOrder);
    }

   
    @Override
    public long countByEntIdAndSearch(String entId, String search) {
        return thirdOutputPort.countByEntIdAndSearch(entId, search);
    }

    @Override
    public Page<Third> getAllThirdsByWithSort(String entId, int page, int size, String sortField, String sortOrder) {
        return thirdOutputPort.getAllThirdsByWithSort(entId, page, size, sortField, sortOrder);
    }
   
    @Override
    public Page<Third> getAllActiveThirdsByWithSort(String entId, int page, int size, String sortField, String sortOrder) {
        return thirdOutputPort.getAllActiveThirdsByWithSort(entId, page, size, sortField, sortOrder);
    }

    @Override
    public long countActiveThirdsByEntId(String entId) {
        return thirdOutputPort.countActiveThirdsByEntId(entId);
    }

    @Override
    public Page<Third> getThirdsByEntIdAndThirdTypeName(String entId, String thirdTypeName, int page, int size) {
        return thirdOutputPort.getThirdsByEntIdAndThirdTypeName(entId, thirdTypeName, PageRequest.of(page, size,
            Sort.by("names").ascending()));
    }

    @Override
    public long countThirdsByEntIdAndThirdTypeName(String entId, String thirdTypeName) {
        return thirdOutputPort.countThirdsByEntIdAndThirdTypeName(entId, thirdTypeName);
    }

}
