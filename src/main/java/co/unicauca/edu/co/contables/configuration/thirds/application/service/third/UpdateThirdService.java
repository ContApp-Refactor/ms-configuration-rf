package co.unicauca.edu.co.contables.configuration.thirds.application.service.third;

import co.unicauca.edu.co.contables.configuration.thirds.application.ports.input.UpdateThirdUseCase;
import co.unicauca.edu.co.contables.configuration.thirds.application.ports.output.IThirdEventPublisher;
import co.unicauca.edu.co.contables.configuration.thirds.application.ports.output.IdOutputPort;
import co.unicauca.edu.co.contables.configuration.thirds.application.ports.output.ThirdOutputPort;
import co.unicauca.edu.co.contables.configuration.thirds.application.service.typeId.TypeIdLoaderService;
import co.unicauca.edu.co.contables.configuration.thirds.domain.exceptions.third.ThirdAlreadyExistsException;
import co.unicauca.edu.co.contables.configuration.thirds.domain.exceptions.third.ThirdInUseException;
import co.unicauca.edu.co.contables.configuration.thirds.domain.exceptions.third.ThirdInvalidDataException;
import co.unicauca.edu.co.contables.configuration.thirds.domain.exceptions.third.ThirdNotFound;
import co.unicauca.edu.co.contables.configuration.thirds.domain.exceptions.thirdType.ThirdTypeForeignKeyViolationException;
import co.unicauca.edu.co.contables.configuration.thirds.domain.exceptions.typeId.TypeIdForeignKeyViolationException;
import co.unicauca.edu.co.contables.configuration.thirds.domain.exceptions.typeId.TypeIdInvalidDataException;
import co.unicauca.edu.co.contables.configuration.thirds.domain.model.*;
import co.unicauca.edu.co.contables.configuration.thirds.domain.utils.StringNormalizer;
import co.unicauca.edu.co.contables.configuration.thirds.infrastructure.adapters.output.persistence.repository.ThirdRepository;
import co.unicauca.edu.co.contables.configuration.commons.audit.annotation.Auditable;
import co.unicauca.edu.co.contables.configuration.commons.audit.annotation.OperationType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UpdateThirdService implements UpdateThirdUseCase {

    private final ThirdOutputPort thirdOutputPort;
    private final ThirdGeographyValidationService geographyValidationService;
    private final ThirdValidationService thirdValidationService;
    private final TypeIdLoaderService typeIdLoaderService;
    private final IdOutputPort idOutputPort;
    private final ThirdRepository thirdRepository;
    private final IThirdEventPublisher thirdEventPublisher;

    @Auditable(operationType = OperationType.UPDATE, affectedTable = "THIRD")
    @Override
    @Transactional
    public Third updateThirdWithGeography(Third third, String countryCode, String stateCode, String cityCode) {
        if (third == null) {
            throw new IllegalArgumentException("El tercero no puede ser null");
        }

        if (third.getThId() == null) {
            throw new IllegalArgumentException("El ID del tercero no puede ser null para actualizar");
        }

        if (!thirdOutputPort.existThirdById(third.getThId(), third.getEntId())) {
            throw new ThirdNotFound("El tercero con ID " + third.getThId() + " no existe");
        }

        // Validar que el tercero no tenga movimientos contables registrados
        Third existingThird = thirdOutputPort.getThirdById(third.getThId(), third.getEntId())
                .orElseThrow(() -> new ThirdNotFound("El tercero con ID " + third.getThId() + " no existe"));
        if (existingThird.isInUse()) {
            throw new ThirdInUseException(existingThird.getIdNumber().toString(), true); // true indica operación de edición
        }

        thirdValidationService.validatePersonTypeConsistency(third);

        validateTypeIdExists(third);
        Third thirdWithCompleteTypeId = typeIdLoaderService.loadCompleteTypeId(third);

        if (!Boolean.TRUE.equals(thirdWithCompleteTypeId.getTypeId().getStatus())) {
            throw new TypeIdInvalidDataException("El tipo de identificación debe estar activo para actualizar un tercero");
        }

        thirdValidationService.validateTypeIdPersonTypeCompatibility(thirdWithCompleteTypeId);

        thirdValidationService.validateNitFormat(thirdWithCompleteTypeId);

        thirdValidationService.validateVerificationDigit(thirdWithCompleteTypeId);

        validateThirdTypesExist(third);

        validateDuplicateThirdOnUpdate(third.getThId(), third.getIdNumber(), third.getEntId());


        Country country = third.getCountry();
        State state = third.getProvince();
        City city = third.getCity();

        if (countryCode != null && stateCode != null && cityCode != null) {
            Object[] geography = geographyValidationService.validateAndGetGeography(countryCode, stateCode, cityCode);
            country = (Country) geography[0];
            state = (State) geography[1];
            city = (City) geography[2];
        }

        Third normalizedThird = Third.builder()
                .thId(thirdWithCompleteTypeId.getThId())
                .entId(thirdWithCompleteTypeId.getEntId())
                .personType(thirdWithCompleteTypeId.getPersonType())
                .typeId(thirdWithCompleteTypeId.getTypeId())
                .thirdTypes(third.getThirdTypes())
                .names(third.getNames() != null ? StringNormalizer.normalizePreservingCase(third.getNames()) : null)
                .lastNames(third.getLastNames() != null ? StringNormalizer.normalizePreservingCase(third.getLastNames())
                        : null)
                .socialReason(third.getSocialReason() != null
                        ? StringNormalizer.normalizePreservingCase(third.getSocialReason())
                        : null)
                .gender(third.getGender())
                .idNumber(third.getIdNumber())
                .verificationNumber(third.getVerificationNumber())
                .state(third.getState() != null ? third.getState() : true)
                .address(third.getAddress())
                .phoneNumber(third.getPhoneNumber())
                .email(third.getEmail())
                .country(country)
                .province(state)
                .city(city)
                .build();

        Third updatedThird = thirdOutputPort.updateThird(normalizedThird);

        //Publicacion de tercero actualizado para notificaciones
        thirdEventPublisher.publishThirdUpdatedEvent(updatedThird);

        return updatedThird;
    }

    /**
     * @brief Valida que el TypeId existe en el sistema
     * @param third tercero que contiene el TypeId a validar
     * @throws ThirdInvalidDataException si el TypeId es null
     * @throws TypeIdForeignKeyViolationException si el TypeId no existe
     */
    private void validateTypeIdExists(Third third) {
        if (third.getTypeId() == null || third.getTypeId().getId() == null) {
            throw new ThirdInvalidDataException("El tipo de identificación no puede ser null");
        }

        if (!typeIdLoaderService.existsTypeId(third.getTypeId().getId())) {
            throw new TypeIdForeignKeyViolationException(third.getTypeId().getId().toString());
        }
    }

    /**
     * @brief Valida que todos los ThirdTypes existen en el sistema y están activos
     * @param third tercero que contiene los ThirdTypes a validar
     * @throws ThirdTypeForeignKeyViolationException si algún ThirdType no existe
     * @throws ThirdInvalidDataException si algún ThirdType está inactivo
     */
    private void validateThirdTypesExist(Third third) {
        if (third.getThirdTypes() == null || third.getThirdTypes().isEmpty()) {
            throw new ThirdInvalidDataException("Los tipos de tercero no pueden estar vacíos");
        }

        for (ThirdType thirdType : third.getThirdTypes()) {
            if (thirdType.getThirdTypeId() == null) {
                throw new ThirdInvalidDataException("El ID del tipo de tercero no puede ser null");
            }

            if (!idOutputPort.existsThirdTypeById(thirdType.getThirdTypeId())) {
                throw new ThirdTypeForeignKeyViolationException(thirdType.getThirdTypeId().toString());
            }

            ThirdType completeThirdType = idOutputPort.getThirdTypeById(thirdType.getThirdTypeId());
            if (!Boolean.TRUE.equals(completeThirdType.getStatus())) {
                throw new ThirdInvalidDataException("El tipo de tercero '" + completeThirdType.getThirdTypeName() + "' está inactivo");
            }
        }
    }

    /**
     * @brief Valida que no exista otro tercero con el mismo número de identificación al actualizar
     * @param thId identificador del tercero que se está actualizando
     * @param newIdNumber nuevo número de identificación
     * @param entId identificador de la entidad
     * @throws ThirdNotFound si el tercero original no existe
     * @throws ThirdAlreadyExistsException si ya existe otro tercero con el mismo idNumber
     */
    private void validateDuplicateThirdOnUpdate(Long thId, Long newIdNumber, String entId) {
        // Obtener el tercero original para comparar el idNumber
        Third originalThird = thirdOutputPort.getThirdById(thId, entId)
                .orElseThrow(() -> new ThirdNotFound("El tercero con ID " + thId + " no existe"));

        // Solo validar si el idNumber cambió
        if (!originalThird.getIdNumber().equals(newIdNumber)) {
            // Verificar si el nuevo idNumber ya existe en otro tercero
            if (thirdRepository.existThirdBy(newIdNumber, entId)) {
                throw new ThirdAlreadyExistsException(newIdNumber.toString());
            }
        }
    }
}
