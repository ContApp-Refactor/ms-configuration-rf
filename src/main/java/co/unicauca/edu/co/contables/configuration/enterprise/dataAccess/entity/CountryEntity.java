package co.unicauca.edu.co.contables.configuration.enterprise.dataAccess.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * @brief Entidad JPA para países
 *
 * Mapea la tabla 'country' en la base de datos.
 */
@Entity(name = "EnterpriseCountryEntity")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "country")
public class CountryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
}
