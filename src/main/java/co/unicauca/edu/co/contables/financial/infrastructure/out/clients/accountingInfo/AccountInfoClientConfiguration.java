package co.unicauca.edu.co.contables.financial.infrastructure.out.clients.accountingInfo;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(AccountInfoClientProperties.class)
public class AccountInfoClientConfiguration {
}