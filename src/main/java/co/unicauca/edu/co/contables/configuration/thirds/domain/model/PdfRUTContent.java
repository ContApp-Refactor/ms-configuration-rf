package co.unicauca.edu.co.contables.configuration.thirds.domain.model;

import lombok.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * @brief Representa el contenido de un archivo PDF que contiene el RUT de un tercero
 *
 * Esta clase se utiliza para manejar cargas de archivos PDF que contienen
 * certificados RUT (Registro Único Tributario) de la DIAN a través de MultipartFile.
 */
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PdfRUTContent {

    private MultipartFile file ;
}
