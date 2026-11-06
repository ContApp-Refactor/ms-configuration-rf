package co.unicauca.edu.co.contables.billing.infrastructure.adapters.input.rest.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Cuerpo de la anulacion. {@code reason} NO lleva validacion de bean a proposito: el dominio es
 * quien decide (recorta, exige 1..500 caracteres) y responde 400 {@code BILLING_ANNUL_REASON_REQUIRED}
 * (ANL-1), el codigo especifico que el front espera. {@code version} se verifica si se envia
 * (DRF-3). Cualquier otro campo se ignora.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record AnnulRequest(String reason, Long version) {
}
