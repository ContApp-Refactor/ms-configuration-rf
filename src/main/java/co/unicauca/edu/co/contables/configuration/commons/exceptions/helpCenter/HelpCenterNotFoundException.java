package co.unicauca.edu.co.contables.configuration.commons.exceptions.helpCenter;

import co.unicauca.edu.co.contables.configuration.commons.exceptions.BaseBusinessException;

public class HelpCenterNotFoundException extends BaseBusinessException {
    
    public HelpCenterNotFoundException() {
        super(HelpCenterErrorCode.HELP_CENTER_NOT_FOUND);
    }

    public HelpCenterNotFoundException(Long id) {
        super(HelpCenterErrorCode.HELP_CENTER_NOT_FOUND,
                String.format("El registro de ayuda con ID %d no fue encontrado", id));
    }
}
