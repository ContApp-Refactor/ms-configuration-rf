package co.unicauca.edu.co.contables.configuration.thirds.application.ports.output;

import lombok.*;

/**
 * @brief Salida de contenido extraído de PDF de RUT
 *
 * Encapsula el contenido textual extraído de un archivo PDF
 * correspondiente a un RUT (Registro Único Tributario).
 *
 * Utiliza anotaciones Lombok para reducir código repetitivo:
 * - @Builder: Patrón builder para creación de instancias
 * - @Getter: Genera métodos getter para todos los campos
 * - @Setter: Genera métodos setter para todos los campos
 * - @AllArgsConstructor: Constructor con todos los parámetros
 * - @NoArgsConstructor: Constructor sin argumentos
 */
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PdfRUTContentOutput {
    /**
     * @brief Contenido textual extraído del archivo PDF
     *
     * Contiene el texto en bruto extraído del PDF, que puede incluir
     * información como el RUT, nombre de empresa, dirección u otros datos
     * relevantes dependiendo de la estructura del documento PDF.
     */
    private String content;
}
