package co.unicauca.edu.co.contables.configuration.helpCenter.domain.mapper;

import co.unicauca.edu.co.contables.configuration.helpCenter.domain.enums.DocumentModule;
import co.unicauca.edu.co.contables.configuration.helpCenter.domain.models.HelpCenter;
import co.unicauca.edu.co.contables.configuration.helpCenter.presentation.DTO.request.HelpCenterCreateReq;
import co.unicauca.edu.co.contables.configuration.helpCenter.presentation.DTO.request.HelpCenterUpdateReq;
import co.unicauca.edu.co.contables.configuration.helpCenter.presentation.DTO.response.HelpCenterRes;

import org.springframework.stereotype.Component;

/**
 * @brief Mapeador de dominio para registros del centro de ayuda
 *
 * Mapeador manual que convierte entre modelos de dominio y DTOs
 * de presentación para registros del centro de ayuda.
 */
@Component
public class HelpCenterDomainMapper {

    public HelpCenter toDomain(HelpCenterCreateReq request) {
        if (request == null) {
            return null;
        }
        
        return HelpCenter.builder()
                .moduleId(request.getModuleId())
                .name(request.getName())
                .description(request.getDescription())
                .build();
    }

    public HelpCenter toDomain(HelpCenterUpdateReq request) {
        if (request == null) {
            return null;
        }
        
        return HelpCenter.builder()
                .id(request.getId())
                .moduleId(request.getModuleId())
                .name(request.getName())
                .description(request.getDescription())
                .build();
    }

    public HelpCenterRes toRes(HelpCenter domain) {
        if (domain == null) {
            return null;
        }
        
        String moduleName = null;
        if (domain.getModuleId() != null) {
            try {
                DocumentModule module = DocumentModule.fromId(domain.getModuleId());
                moduleName = module.getName();
            } catch (Exception e) {
                moduleName = "Módulo desconocido";
            }
        }
        
        return HelpCenterRes.builder()
                .id(domain.getId())
                .moduleId(domain.getModuleId())
                .moduleName(moduleName)
                .name(domain.getName())
                .description(domain.getDescription())
                .status(domain.getStatus())
                .build();
    }
}
