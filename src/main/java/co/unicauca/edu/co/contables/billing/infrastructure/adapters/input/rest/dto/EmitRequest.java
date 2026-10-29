package co.unicauca.edu.co.contables.billing.infrastructure.adapters.input.rest.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Cuerpo opcional de la emision: {@code version} se verifica si se envia (DRF-3). Cualquier otro
 * campo (numero, totales) se ignora: los calcula el servidor.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record EmitRequest(Long version) {
}
