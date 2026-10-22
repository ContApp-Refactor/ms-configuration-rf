package co.unicauca.edu.co.contables.configuration.thirds.application.service.typeId;

import co.unicauca.edu.co.contables.configuration.thirds.application.ports.output.IdOutputPort;
import co.unicauca.edu.co.contables.configuration.thirds.domain.exceptions.typeId.TypeIdForeignKeyViolationException;
import co.unicauca.edu.co.contables.configuration.thirds.domain.model.Third;
import co.unicauca.edu.co.contables.configuration.thirds.domain.model.TypeId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * @brief Servicio para carga y validación de TypeId
 *
 * Centraliza la lógica de carga completa de TypeId desde la base de datos.
 */
@Service
@RequiredArgsConstructor
public class TypeIdLoaderService {

    private final IdOutputPort idOutputPort;

    /**
     * @brief Carga el TypeId completo desde la base de datos si es necesario
     * @param third tercero que puede tener un TypeId incompleto
     * @return tercero con el TypeId completo cargado
     * @throws TypeIdForeignKeyViolationException si el TypeId no existe
     */
    public Third loadCompleteTypeId(Third third) {
        if (third.getTypeId() == null || third.getTypeId().getId() == null) {
            return third;
        }

        // Si el TypeId ya tiene código, no es necesario cargarlo
        if (third.getTypeId().getTypeId() != null && !third.getTypeId().getTypeId().trim().isEmpty()) {
            return third;
        }

        // Cargar TypeId completo desde la base de datos
        TypeId completeTypeId = idOutputPort.getTypeIdById(third.getTypeId().getId());
        if (completeTypeId == null) {
            throw new TypeIdForeignKeyViolationException(third.getTypeId().getId().toString());
        }

        // Retornar tercero con TypeId completo
        return Third.builder()
                .thId(third.getThId())
                .entId(third.getEntId())
                .personType(third.getPersonType())
                .typeId(completeTypeId)
                .thirdTypes(third.getThirdTypes())
                .names(third.getNames())
                .lastNames(third.getLastNames())
                .socialReason(third.getSocialReason())
                .gender(third.getGender())
                .idNumber(third.getIdNumber())
                .verificationNumber(third.getVerificationNumber())
                .state(third.getState())
                .address(third.getAddress())
                .phoneNumber(third.getPhoneNumber())
                .email(third.getEmail())
                .country(third.getCountry())
                .province(third.getProvince())
                .city(third.getCity())
                .build();
    }

    /**
     * @brief Valida que el TypeId existe en la base de datos
     * @param typeIdId identificador único del TypeId a validar
     * @return true si existe, false si no existe
     */
    public boolean existsTypeId(Long typeIdId) {
        if (typeIdId == null) {
            return false;
        }
        return idOutputPort.existsTypeIdById(typeIdId);
    }
}
