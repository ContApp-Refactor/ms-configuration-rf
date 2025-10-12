package co.unicauca.edu.co.contables.configuration.commons.exceptions.helpCenter;

import co.unicauca.edu.co.contables.configuration.commons.exceptions.BaseBusinessException;

public class HelpCenterAlreadyExistsException extends BaseBusinessException {
    
    public HelpCenterAlreadyExistsException() {
        super(HelpCenterErrorCode.HELP_CENTER_ALREADY_EXISTS);
    }
    
    public HelpCenterAlreadyExistsException(String name, String idEnterprise) {
        super(HelpCenterErrorCode.HELP_CENTER_ALREADY_EXISTS,
                String.format("Ya existe un registro de ayuda con el nombre '%s' para la empresa %s", name, idEnterprise));
    }
}
