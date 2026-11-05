package co.unicauca.edu.co.contables.auxiliary.infrastructure.out.persistence.entity;

import co.unicauca.edu.co.contables.auxiliary.domain.models.enums.EDeliveryWay;
import co.unicauca.edu.co.contables.auxiliary.domain.models.enums.EState;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @brief Entidad JPA que representa el historial de un libro auxiliar.
 *
 * Mapea el estado del historial y la vía de entrega asociada al
 * libro auxiliar. Mantiene la relación uno-a-uno con el libro auxiliar
 * correspondiente.
 */
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "AUXILIARY_BOOK_HISTORY")
public class AuxiliaryBookHistoryEntity {
    /**
     * @brief Unique identifier for the auxiliary book history.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    /**
     * @brief Public identifier for the auxiliary book. (External ID)
     */
    @Column(unique = true, nullable = false, updatable = false)
    private String publicId;


    /**
     * @brief State of the auxiliary book history.
     */
    @Column(nullable = false)
    private EState state;

    /**
     * @brief Delivery way for the auxiliary book history.
     */
    @Enumerated(EnumType.STRING)
    private EDeliveryWay deliveryWay;

    // Relationships

    /**
     * @brief Reference to the associated auxiliary book entity.
     */
    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "auxiliary_book_id", nullable = false)
    private AuxiliaryBookEntity auxiliaryBook;
}
