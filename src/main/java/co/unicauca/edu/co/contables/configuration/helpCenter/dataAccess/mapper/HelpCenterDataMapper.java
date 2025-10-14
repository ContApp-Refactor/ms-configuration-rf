package co.unicauca.edu.co.contables.configuration.helpCenter.dataAccess.mapper;

import co.unicauca.edu.co.contables.configuration.helpCenter.dataAccess.entity.HelpCenterEntity;
import co.unicauca.edu.co.contables.configuration.helpCenter.domain.models.HelpCenter;
import co.unicauca.edu.co.contables.configuration.helpCenter.domain.models.DocumentModule;
import org.springframework.stereotype.Component;

@Component
public class HelpCenterDataMapper {

    public HelpCenter toDomain(HelpCenterEntity entity) {
        if (entity == null) {
            return null;
        }
        
        return HelpCenter.builder()
                .id(entity.getId())
                .moduleId(entity.getModule() != null ? entity.getModule().getId() : null)
                .name(entity.getName())
                .description(entity.getDescription())
                .status(entity.getStatus())
                .build();
    }

    public HelpCenterEntity toEntity(HelpCenter domain) {
        if (domain == null) {
            return null;
        }
        
        return HelpCenterEntity.builder()
                .id(domain.getId())
                .module(domain.getModuleId() != null ? DocumentModule.fromId(domain.getModuleId()) : null)
                .name(domain.getName())
                .description(domain.getDescription())
                .status(domain.getStatus())
                .build();
    }
}
