package co.unicauca.edu.co.contables.auxiliary.domain.models.core;

import co.unicauca.edu.co.contables.auxiliary.domain.models.core.criteria.AuxiliaryBookCriteria;
import co.unicauca.edu.co.contables.auxiliary.domain.models.core.export.AuxiliaryBookTemplate;
import co.unicauca.edu.co.contables.auxiliary.domain.models.enums.EAuxiliaryBookFormat;
import co.unicauca.edu.co.contables.auxiliary.domain.models.enums.EAuxiliaryBookType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * @brief Modelo de dominio que representa un Libro Auxiliar.
 *
 * Encapsula el tipo de libro, la entidad y usuario propietarios, el
 * formato de salida, la plantilla de exportación, los criterios de
 * generación y la fecha de creación. Es la raíz del agregado alrededor
 * del cual giran las operaciones de registro, generación y exportación.
 */
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuxiliaryBook {
    private Long id;
    private String publicId;
    private EAuxiliaryBookType type;
    private String entId;
    private Long userId;
    private EAuxiliaryBookFormat format;
    private AuxiliaryBookTemplate template;
    private AuxiliaryBookCriteria criteria;
    private LocalDateTime createdAt;

    public void changeFormat(EAuxiliaryBookFormat format) {
        this.format = format;
    }
}
