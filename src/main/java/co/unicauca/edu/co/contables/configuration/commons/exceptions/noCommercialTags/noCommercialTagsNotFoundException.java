package co.unicauca.edu.co.contables.configuration.commons.exceptions.noCommercialTags;

import co.unicauca.edu.co.contables.configuration.commons.exceptions.BaseBusinessException;

public class noCommercialTagsNotFoundException extends BaseBusinessException {

    public noCommercialTagsNotFoundException(NoCommercialTagsErrorCode errorCode) {
        super(errorCode);
    }

}
