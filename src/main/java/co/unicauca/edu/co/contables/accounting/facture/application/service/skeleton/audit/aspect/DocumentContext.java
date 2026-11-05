package co.unicauca.edu.co.contables.accounting.facture.application.service.skeleton.audit.aspect;

import java.time.LocalDate;

public record DocumentContext(
                String documentId,
                String documentCode,
                String enterpriseId,
                String documentType,
                LocalDate documentDate,
                String thirdPartyId,
                String thirdPartyName) {
}
