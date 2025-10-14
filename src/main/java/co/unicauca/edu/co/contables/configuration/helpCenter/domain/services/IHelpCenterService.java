package co.unicauca.edu.co.contables.configuration.helpCenter.domain.services;

import co.unicauca.edu.co.contables.configuration.helpCenter.domain.models.HelpCenter;
import co.unicauca.edu.co.contables.configuration.helpCenter.presentation.DTO.request.HelpCenterCreateReq;
import co.unicauca.edu.co.contables.configuration.helpCenter.presentation.DTO.request.HelpCenterUpdateReq;
import org.springframework.data.domain.Page;

import java.util.List;

public interface IHelpCenterService {

    HelpCenter create(HelpCenterCreateReq request);

    HelpCenter update(HelpCenterUpdateReq request);

    HelpCenter findById(Long id);

    Page<HelpCenter> findAll(int page, int size);

    Page<HelpCenter> findAll(int page, int size, String sortField, String sortOrder);

    List<HelpCenter> findAllByModule(Integer moduleId);

    HelpCenter changeState(Long id, Boolean status);

    HelpCenter delete(Long id);

    long countAll();

    Page<HelpCenter> findByNameContaining(String search, int page, int size, String sortField, String sortOrder);

    long countByNameContaining(String search);
}
