package co.unicauca.edu.co.contables.configuration.enterprise.domain.services;

import java.util.UUID;

/**
 * @brief Puerto de salida para duplicar una empresa.
 *
 * Hallazgo confirmado por auditoría del microservicio de referencia
 * ({@code copy}): DUPLICATE no es un mecanismo aparte, es un BACKUP del
 * origen encadenado con un RESTORE en un destino nuevo ({@code inplace =
 * false}).
 */
public interface IEnterpriseDuplicateService {

    /** @return el id de la empresa nueva creada a partir de la empresa origen. */
    UUID duplicate(UUID enterpriseId);
}
