package co.unicauca.edu.co.contables.configuration.thirds.domain.model;

import co.unicauca.edu.co.contables.configuration.thirds.domain.enums.ePersonType;
import co.unicauca.edu.co.contables.configuration.thirds.domain.enums.eThirdGender;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

/**
 * @brief Modelo de dominio que representa un tercero en el sistema
 *
 * Entidad principal que representa a personas naturales o jurídicas (terceros)
 * con información completa de identificación, contacto, ubicación geográfica
 * y categorización por tipos (cliente, proveedor, empleado, etc.).
 */
@Builder
@Getter
@Setter
@EqualsAndHashCode(of = {"entId", "idNumber", "typeId"})
@ToString(of = {"entId", "idNumber", "names", "lastNames", "socialReason"})
@AllArgsConstructor
@NoArgsConstructor
public class Third {

    private Long thId;

    @NotBlank(message = "El ID de la empresa no puede estar vacío")
    @Size(max = 50, message = "El ID de la empresa no puede exceder los 50 caracteres")
    private String entId;

    @NotNull(message = "El tipo de identificación es obligatorio")
    private TypeId typeId;

    @Builder.Default
    @NotNull(message = "Los tipos de tercero no pueden ser null")
    private Set<ThirdType> thirdTypes = new HashSet<>();

    @NotNull(message = "El tipo de persona es obligatorio")
    private ePersonType personType;

    @Size(max = 100, message = "Los nombres no pueden exceder los 100 caracteres")
    private String names;
    @Size(max = 100, message = "Los apellidos no pueden exceder los 100 caracteres")
    private String lastNames;
    @Size(max = 200, message = "La razón social no puede exceder los 200 caracteres")
    private String socialReason;
    private eThirdGender gender;

    @NotNull(message = "El número de identificación es obligatorio")
    private Long idNumber;
    private Long verificationNumber;


    @NotNull(message = "El estado del tercero es obligatorio")
    @Builder.Default
    private Boolean state = true;

    private Country country;
    private State province;
    private City city;

    @Size(max = 300, message = "La dirección no puede exceder los 300 caracteres")
    private String address;

    @Pattern(regexp = "^\\+?[0-9\\s\\-\\(\\)]{7,20}$", message = "El formato del teléfono no es válido")
    @Size(max = 20, message = "El teléfono no puede exceder los 20 caracteres")
    private String phoneNumber;

    @Email(message = "El formato del correo electrónico no es válido")
    @Size(max = 150, message = "El correo electrónico no puede exceder los 150 caracteres")
    private String email;

    @Builder.Default
    private Integer usageCount = 0;

    @JsonIgnore
    public boolean isActive() {
        return Boolean.TRUE.equals(state);
    }

    public void activate() {
        this.state = true;
    }

    public void deactivate() {
        this.state = false;
    }

    public boolean isLegalEntity() {
        return ePersonType.Juridica.equals(personType);
    }

    public boolean isNaturalPerson() {
        return ePersonType.Natural.equals(personType);
    }

    /**
     * @brief Verifica si el tercero está siendo usado
     * @return true si el tercero tiene uso registrado
     */
    @JsonIgnore
    public boolean isInUse() {
        return this.usageCount != null && this.usageCount > 0;
    }
}