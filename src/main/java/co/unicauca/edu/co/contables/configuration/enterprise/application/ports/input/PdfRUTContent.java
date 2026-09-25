package co.unicauca.edu.co.contables.configuration.enterprise.application.ports.input;

import lombok.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * Clase que representa el contenido de un archivo PDF del RUT (Registro Único Tributario).
 * Se utiliza para manejar la carga y transferencia de archivos PDF relacionados
 * con documentos RUT en el sistema.
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
public class PdfRUTContent {
    
    /**
     * Archivo MultipartFile que contiene el documento PDF del RUT.
     */
    private MultipartFile file;
}
