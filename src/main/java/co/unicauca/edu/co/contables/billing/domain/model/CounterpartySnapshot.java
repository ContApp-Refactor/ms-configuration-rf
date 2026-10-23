package co.unicauca.edu.co.contables.billing.domain.model;

import java.util.Objects;

/** Copia congelable de los datos del tercero (cliente o proveedor) al momento de usarlo. */
public record CounterpartySnapshot(
        long thirdId,
        CounterpartyRole role,
        String name,
        String idType,
        String idNumber,
        String verificationNumber,
        String address,
        String email,
        String phone,
        boolean active) {

    public CounterpartySnapshot {
        Objects.requireNonNull(role, "role");
    }
}
