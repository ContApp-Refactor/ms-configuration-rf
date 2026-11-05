package co.unicauca.edu.co.contables.billing.infrastructure.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/** Habilita las propiedades tipadas del modulo billing. */
@Configuration("billingInfrastructureConfig")
@EnableConfigurationProperties(BillingProperties.class)
public class BillingInfrastructureConfig {
}
