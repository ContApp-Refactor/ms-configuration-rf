package co.unicauca.edu.co.contables.auxiliary.infrastructure.out.clients.thirdParty;

import com.fasterxml.jackson.databind.JsonNode;
import co.unicauca.edu.co.contables.auxiliary.application.ports.out.IThirdPartyInfoClient;
import co.unicauca.edu.co.contables.auxiliary.domain.models.external.thirdParty.ThirdParty;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * @brief Cliente para obtener información de terceros desde un servicio externo.
 *
 * Implementa el puerto {@link IThirdPartyInfoClient} utilizando un
 * {@link WebClient} load-balanced. Convierte la respuesta JSON cruda
 * en un modelo de dominio {@link ThirdParty}, combinando nombres y
 * apellidos o usando la razón social según disponibilidad.
 */
@Component
public class ThirdPartyService implements IThirdPartyInfoClient {

    private final WebClient webClient;

    public ThirdPartyService(
            @Qualifier("loadBalancedWebClientBuilder") WebClient.Builder webClientBuilder,
            @Value("${services.third-service.base-url}") String url
    ) {
        this.webClient = webClientBuilder.baseUrl(url).build();
    }

    /**
     * @brief Obtiene un tercero por su ID desde el servicio externo.
     * @param thirdPartyId ID del tercero a consultar.
     * @return Objeto ThirdParty si existe, {@code null} si no fue encontrado.
     */
    @Override
    public ThirdParty getThirdPartyById(Long thirdPartyId) {
        JsonNode thirdPartyResponse = this.webClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("third").
                        queryParam("thId", thirdPartyId)
                        .build())
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block();
        return (thirdPartyResponse!=null) ? this.convertJsonToThirdParty(thirdPartyResponse) : null;
    }

    /**
     * @brief Convierte un JsonNode en un objeto ThirdParty.
     * @param jsonNode JsonNode con los datos del tercero.
     * @return Objeto ThirdParty poblado con los datos del JSON.
     */
    private ThirdParty convertJsonToThirdParty(JsonNode jsonNode) {

        ThirdParty objThirdParty = new ThirdParty();

        String name = jsonNode.get("names").asText();
        String LastName = jsonNode.get("lastNames").asText();

        if(name.isEmpty() || LastName.isEmpty()) {
            objThirdParty.setName(jsonNode.get("socialReason").asText());
        }else{
            objThirdParty.setName(name + " " + LastName);
        }

        objThirdParty.setId(jsonNode.get("thId").asLong());
        objThirdParty.setIdentificationNumber(jsonNode.get("identificationNumber").asLong());
        objThirdParty.setTypeId(jsonNode.get("typeId").get("typeId").asText());

        return objThirdParty;
    }
}
