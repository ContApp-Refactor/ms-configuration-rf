package co.unicauca.edu.co.contables.notifications.infrastructure.config;

/**
 * @brief Constantes de la cola de actualización de terceros que consume este módulo.
 *
 * En el monolito la topología completa de {@code third.updated.queue} (fanout
 * exchange, cola durable y binding) ya la declara la configuración RabbitMQ del
 * módulo thirds, idéntica a la del origen. Repetirla aquí chocaría con los
 * nombres de bean de esa configuración, así que esta clase se conserva solo
 * como portador de las constantes que usan los listeners.
 */
public final class RabbitThirdsEventsConfig {
    public static final String THIRD_UPDATED_EXCHANGE = "third.updated.exchange";
    public static final String THIRD_UPDATED_QUEUE = "third.updated.queue";

    private RabbitThirdsEventsConfig() {
    }
}
