package co.unicauca.edu.co.contables.auxiliary.infrastructure.out.persistence.entity;

import co.unicauca.edu.co.contables.auxiliary.domain.models.enums.EAlignment;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @brief Entidad JPA que representa la plantilla de un libro auxiliar.
 *
 * Mapea la configuración visual de la plantilla: nombre, ruta del
 * logotipo, alineación, fuente y color principal. Mantiene la relación
 * uno-a-uno con el libro auxiliar que usa la plantilla.
 */
@Entity
@Data
@AllArgsConstructor @NoArgsConstructor
@Table(name = "AUXILIARY_BOOK_TEMPLATE")
public class AuxiliaryBookTemplateEntity {

    // Table Columns

    /**
     * @brief Unique identifier for the auxiliary book template.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * @brief Name of the template.
     */
    @Column(nullable = false)
    private String name;

    /**
     * @brief Path to the logotype image for the template.
     */
    @Column(nullable = false)
    private String pathLogotype;

    /**
     * @brief Alignment setting for the template.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EAlignment aligment;

    /**
     * @brief Font used in the template.
     */
    @Column(nullable = false)
    private String font;

    /**
     * @brief Main color used in the template.
     */
    @Column(nullable = false)
    private String mainColor;

    // Relationships

    /**
     * @brief Reference to the associated auxiliary book entity.
     */
    @OneToOne(mappedBy = "template")
    private AuxiliaryBookEntity auxiliaryBook;
}
