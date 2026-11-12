package co.unicauca.edu.co.contables.billing.infrastructure.adapters.output.integration;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

/** La regla "metodo de pago -> cuenta bancaria" se elimino: el pago solo lleva el metodo. */
class PaymentMethodBankRuleTest {

    @Test
    void the_bank_rule_class_no_longer_exists() {
        assertThatThrownBy(() -> Class.forName(
                "co.unicauca.edu.co.contables.billing.infrastructure.adapters.output.integration.PaymentMethodBankRule"))
                .isInstanceOf(ClassNotFoundException.class);
    }
}
