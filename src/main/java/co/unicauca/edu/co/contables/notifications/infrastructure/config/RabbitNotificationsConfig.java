package co.unicauca.edu.co.contables.notifications.infrastructure.config;

/**
 * @brief Constantes de la cola de notificaciones consumida por este módulo.
 *
 * En el monolito la topología completa de {@code notifications.queue} (fanout
 * exchange, cola durable y binding) ya la declara la configuración RabbitMQ de
 * debt_payments, idéntica a la del origen. Repetirla aquí chocaría con los
 * nombres de bean de esa configuración, así que esta clase se conserva solo
 * como portador de las constantes que usan los listeners.
 */
public final class RabbitNotificationsConfig {
    public static final String NOTIFICATIONS_EXCHANGE = "notifications.exchange";
    public static final String NOTIFICATIONS_QUEUE = "notifications.queue";

    private RabbitNotificationsConfig() {
    }
}
