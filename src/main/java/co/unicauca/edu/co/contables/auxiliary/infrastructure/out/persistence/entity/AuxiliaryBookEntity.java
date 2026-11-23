package co.unicauca.edu.co.contables.auxiliary.infrastructure.out.persistence.entity;

import co.unicauca.edu.co.contables.auxiliary.domain.models.enums.EAuxiliaryBookFormat;
import co.unicauca.edu.co.contables.auxiliary.domain.models.enums.EAuxiliaryBookType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * @brief Entidad JPA que representa un libro auxiliar.
 *
 * Mapea el registro principal del libro auxiliar: tipo, formato,
 * empresa, usuario y fecha de creación. Relaciona los agregados de
 * plantilla, criterios, logs e historial que componen el libro.
 */
@Entity
@Data
@AllArgsConstructor @NoArgsConstructor
@Table(name = "AUXILIARY_BOOK")
public class AuxiliaryBookEntity {

    // Table Columns

    /**
     * @brief Unique identifier for the auxiliary book.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * @brief Public identifier for the auxiliary book. (External ID)
     */
    @Column(unique = true, nullable = false, updatable = false)
    private String publicId;

    /**
     * @brief Type of the auxiliary book.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EAuxiliaryBookType type;

    /**
     * @brief Identifier of the entity (organization or company) that owns the book.
     */
    @Column(nullable = false)
    private String entId;

    /**
     * @brief Identifier of the user who created the book.
     */
    @Column(nullable = false)
    private Long userId;

    /**
     * @brief Format of the auxiliary book.
     */
    @Enumerated(EnumType.STRING)
    @Column
    private EAuxiliaryBookFormat format;

    /**
     * @brief Timestamp when the auxiliary book was created.
     */
    @Column
    @CreationTimestamp
    private LocalDateTime createdAt;

    // Relationships

    /**
     * @brief Reference to the template entity associated with this auxiliary book.
     */
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "abTemplateId", referencedColumnName = "id")
    private AuxiliaryBookTemplateEntity template;

    /**
     * @brief Reference to the criteria entity associated with this auxiliary book.
     */
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "abCriteriaId", referencedColumnName = "id")
    private AuxiliaryBookCriteriaEntity criteria;

    /**
     * @brief Reference to the log entity associated with this auxiliary book.
     */
    @OneToMany(mappedBy = "auxiliaryBook", cascade = CascadeType.ALL)
    private List<AuxiliaryBookLogEntity> log = new ArrayList<>();

    /**
     * @brief Reference to the history entity associated with this auxiliary book.
     */
    @OneToOne(mappedBy = "auxiliaryBook", cascade = CascadeType.ALL)
    private AuxiliaryBookHistoryEntity history;

}
