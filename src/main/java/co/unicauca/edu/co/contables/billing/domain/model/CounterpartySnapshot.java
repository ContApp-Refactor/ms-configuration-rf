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
        boolean active,
        String city) {

    public CounterpartySnapshot {
        Objects.requireNonNull(role, "role");
    }

    /** Sin ciudad (usos anteriores y documentos historicos). */
    public CounterpartySnapshot(long thirdId, CounterpartyRole role, String name, String idType, String idNumber,
            String verificationNumber, String address, String email, String phone, boolean active) {
        this(thirdId, role, name, idType, idNumber, verificationNumber, address, email, phone, active, null);
    }
}
