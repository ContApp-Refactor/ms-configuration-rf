package co.unicauca.edu.co.contables.auxiliary.infrastructure.out.clients.accountingInfo;

import java.util.List;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import co.unicauca.edu.co.contables.auxiliary.application.ports.out.IAccountingInfoClient;
import co.unicauca.edu.co.contables.auxiliary.domain.models.external.accountingInfo.AccountingInfo;

/**
 * @brief Cliente para obtener información contable desde un servicio externo.
 *
 * Implementa el puerto {@link IAccountingInfoClient} usando un
 * {@link WebClient} configurado con la URL base del servicio externo
 * de información contable y propagación del token JWT.
 */
@Component
public class AccountInfoService implements IAccountingInfoClient {

    private final WebClient webClient;

    public AccountInfoService(
            @Qualifier("externalWebClientBuilder") WebClient.Builder webClientBuilder,
            @Value("${services.account-info-service.base-url}") String url
    ) {
        this.webClient = webClientBuilder.baseUrl(url).build();
    }

    /**
     * @brief Obtiene todos los registros de información contable del servicio externo.
     * @return Lista de AccountingInfo (vacía si el servicio no retorna datos).
     */
    @Override
    public List<AccountingInfo> getAllAccountInfo() {
        List<AccountingInfo> lst = this.webClient.get().retrieve().bodyToFlux(AccountingInfo.class).collectList().block();
        return lst != null ? lst : List.of();
    }
}
