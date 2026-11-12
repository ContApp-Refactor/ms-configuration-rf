package co.unicauca.edu.co.contables.accounting.catalogue.accounting.manualnotes.domain.port;

import java.util.Optional;

/** Contrato para consultar los tipos de documento configurados por empresa. */
public interface DocumentTypePort {

    Optional<DocumentType> findActiveByPrefix(String prefix, String enterpriseId);

    record DocumentType(String prefix, String name, Integer moduleId) {
    }
}
