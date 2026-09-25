package co.unicauca.edu.co.contables.configuration.thirds.domain.model;

import co.unicauca.edu.co.contables.configuration.thirds.domain.enums.ePersonType;
import co.unicauca.edu.co.contables.configuration.thirds.domain.enums.eThirdGender;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

/**
 * @brief Modelo que representa los datos de un tercero parseados desde Excel
 *
 *        Contiene los datos en formato raw tal como se leen del archivo Excel,
 *        antes de ser validados y convertidos a entidades de dominio completas.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ThirdExcelData {

    private Integer rowNumber;

    private String entId;

    private String typeIdName;

    private Long idNumber;

    private Long verificationNumber;

    private ePersonType personType;

    private String names;

    private String lastNames;

    private String socialReason;

    private eThirdGender gender;

    private Boolean state;

    private String countryName;

    private String stateName;

    private String cityName;

    private String address;

    private String phoneNumber;

    private String email;

    private Set<String> thirdTypesNames;

    public boolean hasRequiredFields() {
        return typeIdName != null && !typeIdName.trim().isEmpty()
                && idNumber != null
                && personType != null
                && address != null && !address.trim().isEmpty()
                && phoneNumber != null && !phoneNumber.trim().isEmpty()
                && email != null && !email.trim().isEmpty();
    }

    /**
     * @brief Verifica si es una persona natural con datos completos
     *
     *        Valida que el registro tenga toda la información requerida para una
     *        persona natural:
     *        nombres, apellidos, razón social ausente.
     *        El género es opcional.
     * @return true si los datos corresponden a una persona natural válida
     */
    public boolean isValidNaturalPerson() {
        return personType == ePersonType.Natural
                && names != null && !names.trim().isEmpty()
                && lastNames != null && !lastNames.trim().isEmpty()
                && (socialReason == null || socialReason.trim().isEmpty());
    }

    /**
     * @brief Verifica si es una persona jurídica con datos completos
     *
     *        Valida que el registro tenga toda la información requerida para una
     *        persona jurídica:
     *        razón social presente, campos de persona natural ausentes.
     *        El género es opcional.
     * @return true si los datos corresponden a una persona jurídica válida
     */
    public boolean isValidLegalEntity() {
        return personType == ePersonType.Juridica
                && socialReason != null && !socialReason.trim().isEmpty()
                && (names == null || names.trim().isEmpty())
                && (lastNames == null || lastNames.trim().isEmpty());
    }

}
