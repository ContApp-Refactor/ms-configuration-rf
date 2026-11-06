package co.unicauca.edu.co.contables.billing.application.ports.input;

/** Publica los eventos pendientes del outbox de facturacion (EVT-4). Lo invoca el planificador. */
public interface RelayBillingOutboxUseCase {

    /**
     * Procesa un lote de filas vencidas del outbox (PENDING con {@code next_attempt_at <= ahora}),
     * de cualquier tenant. Entrega al menos una vez: una fila puede publicarse mas de una vez si el
     * proceso cae entre el envio y el marcado.
     *
     * @return cantidad de filas publicadas y marcadas PUBLISHED en esta corrida
     */
    int relayDue();
}
