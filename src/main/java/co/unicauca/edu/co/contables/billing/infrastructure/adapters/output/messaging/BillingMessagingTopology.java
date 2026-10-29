package co.unicauca.edu.co.contables.billing.infrastructure.adapters.output.messaging;

/**
 * Nombres de la topologia de mensajeria de facturacion (EVT-5, D14). Todos con prefijo
 * {@code billing.}: jamas se tocan {@code invoice.exchange} ni {@code purchase.invoice.exchange}.
 * Solo constantes; la declaracion de beans AMQP llega en H6 (BillingRabbitConfig).
 */
public final class BillingMessagingTopology {

    public static final String DOCUMENTS_EXCHANGE = "billing.documents.exchange";
    public static final String UNROUTED_EXCHANGE = "billing.documents.unrouted.exchange";
    public static final String UNROUTED_QUEUE = "billing.documents.unrouted.queue";
    public static final String DEAD_LETTER_EXCHANGE = "billing.documents.dlx";
    public static final String DEAD_LETTER_QUEUE = "billing.documents.dlq";

    private BillingMessagingTopology() {
    }
}
