package co.unicauca.edu.co.contables.audit.infrastructure.config.rabbitConfig;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * @brief Configuración de tareas asíncronas específica del módulo audit.
 *
 *        El nombre de la clase difiere del {@code AsyncConfig} de
 *        {@code commons.multitenancy.async} para evitar confusión. El bean
 *        {@code exportTaskExecutor} lo usan los 3 adaptadores de exportación
 *        con {@code @Async("exportTaskExecutor")}: el ejecutor por defecto de
 *        multitenancy no lo define, así que quitarlo rompería el arranque.
 */
@Configuration
@EnableAsync
public class AuditExportAsyncConfig {
    @Bean(name = "exportTaskExecutor")
    public TaskExecutor exportTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(4);
        executor.setQueueCapacity(50);
        executor.setThreadNamePrefix("export-");
        executor.initialize();
        return executor;
    }
}
