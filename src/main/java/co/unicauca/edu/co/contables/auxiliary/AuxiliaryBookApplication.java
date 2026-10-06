package co.unicauca.edu.co.contables.auxiliary;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @brief Clase principal de arranque del microservicio de libros auxiliares.
 *
 * Habilita la autoconfiguración de Spring Boot, el registro como cliente
 * de descubrimiento de servicios ({@code @EnableDiscoveryClient}) y la
 * ejecución de tareas programadas ({@code @EnableScheduling}) requerida
 * por el planificador de reportes.
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableScheduling
public class AuxiliaryBookApplication {

	/**
	 * @brief Punto de entrada de la aplicación.
	 * @param args Argumentos de línea de comandos pasados al contexto de Spring.
	 */
	public static void main(String[] args) {
		SpringApplication.run(AuxiliaryBookApplication.class, args);
	}

}
