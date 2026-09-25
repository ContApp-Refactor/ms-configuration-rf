package co.unicauca.edu.co.contables.configuration.enterprise.application.ports.output;

import lombok.*;

/**
 * Clase que representa el contenido de salida de un archivo PDF del RUT procesado.
 * Se utiliza para transportar el contenido extraído de documentos RUT
 * después de su procesamiento.
 *
 * @author CONTAPP
 * @version 1.0
 * @since 1.0.0
 */
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PdfRUTContentOutput {
    
    /**
     * Contenido extraído del documento PDF del RUT.
     */
    private String content;
}