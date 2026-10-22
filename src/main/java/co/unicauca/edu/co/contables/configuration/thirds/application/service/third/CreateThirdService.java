package co.unicauca.edu.co.contables.configuration.thirds.application.service.third;

import co.unicauca.edu.co.contables.configuration.thirds.application.ports.input.CreateThirdUseCase;
import co.unicauca.edu.co.contables.configuration.thirds.application.ports.output.IThirdEventPublisher;
import co.unicauca.edu.co.contables.configuration.thirds.application.ports.output.IdOutputPort;
import co.unicauca.edu.co.contables.configuration.thirds.application.ports.output.ThirdOutputPort;
import co.unicauca.edu.co.contables.configuration.thirds.application.service.typeId.TypeIdLoaderService;
import co.unicauca.edu.co.contables.configuration.thirds.domain.exceptions.third.ThirdAlreadyExistsException;
import co.unicauca.edu.co.contables.configuration.thirds.domain.exceptions.third.ThirdInvalidDataException;
import co.unicauca.edu.co.contables.configuration.thirds.domain.exceptions.thirdType.ThirdTypeForeignKeyViolationException;
import co.unicauca.edu.co.contables.configuration.thirds.domain.exceptions.typeId.TypeIdInvalidDataException;
import co.unicauca.edu.co.contables.configuration.thirds.domain.model.*;
import co.unicauca.edu.co.contables.configuration.thirds.domain.utils.StringNormalizer;
import co.unicauca.edu.co.contables.configuration.thirds.infrastructure.adapters.output.persistence.repository.ThirdRepository;
import co.unicauca.edu.co.contables.commons.audit.annotation.Auditable;
import co.unicauca.edu.co.contables.commons.audit.annotation.OperationType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CreateThirdService implements CreateThirdUseCase {

    private final ThirdOutputPort thirdOutputPort;
    private final ThirdRepository thirdRepository;
    private final ThirdGeographyValidationService geographyValidationService;
    private final ThirdValidationService thirdValidationService;
    private final TypeIdLoaderService typeIdLoaderService;
    private final IdOutputPort idOutputPort;
    private final IThirdEventPublisher thirdEventPublisher;
    
    @Auditable(operationType = OperationType.CREATE, affectedTable = "THIRD")
    @Override
    @Transactional
    public Third createThird(Third third, String countryCode, String stateCode, String cityCode) {
       
        thirdValidationService.validatePersonTypeConsistency(third);

        Third thirdWithCompleteTypeId = typeIdLoaderService.loadCompleteTypeId(third);
        thirdValidationService.validateTypeIdPersonTypeCompatibility(thirdWithCompleteTypeId);

        if (thirdWithCompleteTypeId.getTypeId() != null && !Boolean.TRUE.equals(thirdWithCompleteTypeId.getTypeId().getStatus())) {
            throw new TypeIdInvalidDataException("El tipo de identificación seleccionado está inactivo");
        }

        thirdValidationService.validateNitFormat(thirdWithCompleteTypeId);

        thirdValidationService.validateVerificationDigit(thirdWithCompleteTypeId);

        validateThirdTypesExistAndActive(third);

        Country country = null;
        State state = null;
        City city = null;
        
        if (countryCode != null && stateCode != null && cityCode != null) {
            Object[] geography = geographyValidationService.validateAndGetGeography(countryCode, stateCode, cityCode);
            country = (Country) geography[0];
            state = (State) geography[1];
            city = (City) geography[2];
        }

        Third normalizedThird = Third.builder()
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

        validateDuplicateThird(normalizedThird.getIdNumber(), normalizedThird.getEntId());

        Third createdThird = thirdOutputPort.saveThird(normalizedThird);

        //Publicacion de tercero creado para notificaciones
        thirdEventPublisher.publishThirdUpdatedEvent(createdThird);

        return createdThird;
    }

    /**
     * @brief Prepara un tercero para guardado en lote (sin persistir) - VERSIÓN OPTIMIZADA
     * @details Solo normaliza datos y valida lógica de negocio. NO consulta BD.
     * Asume que ya se validaron en fases previas:
     *  - Fase 2: TypeIds completos, ThirdTypes existentes, compatibilidades
     *  - Fase 3: Duplicados
     * @param third El tercero a preparar (ya validado)
     * @param countryCode Código del país (ya validado)
     * @param stateCode Código del estado (ya validado)
     * @param cityCode Código de la ciudad (ya validado)
     * @return Third normalizado listo para persistir
     */
    public Third prepareThirdForBatchSave(Third third, String countryCode, String stateCode, String cityCode) {
        
        // Solo validaciones de lógica pura (sin acceso a BD)
        thirdValidationService.validatePersonTypeConsistency(third);
        thirdValidationService.validateTypeIdPersonTypeCompatibility(third);

        if (third.getTypeId() != null && !Boolean.TRUE.equals(third.getTypeId().getStatus())) {
            throw new TypeIdInvalidDataException("El tipo de identificación seleccionado está inactivo");
        }

        thirdValidationService.validateNitFormat(third);
        thirdValidationService.validateVerificationDigit(third);

        Third normalizedThird = Third.builder()
                .entId(third.getEntId())
                .personType(third.getPersonType())
                .typeId(third.getTypeId())  // Ya viene completo
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
                .country(third.getCountry())   
                .province(third.getProvince()) 
                .city(third.getCity())          
                .build();

        return normalizedThird;
    }

    /**
     * @brief Valida que no exista un tercero duplicado con el mismo número de identificación
     * @param idNumber número de identificación
     * @param entId identificador de la entidad
     * @throws ThirdAlreadyExistsException si ya existe un tercero con el mismo número de identificación
     */
    private void validateDuplicateThird(Long idNumber, String entId) {
        if (thirdRepository.existThirdBy(idNumber, entId)) {
            throw new ThirdAlreadyExistsException(idNumber.toString());
        }
    }

    /**
     * @brief Valida que todos los ThirdTypes existen en el sistema y están activos
     * @param third tercero que contiene los ThirdTypes a validar
     * @throws ThirdTypeForeignKeyViolationException si algún ThirdType no existe
     * @throws ThirdInvalidDataException si algún ThirdType está inactivo
     */
    private void validateThirdTypesExistAndActive(Third third) {
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

            // Validar que el ThirdType esté activo
            ThirdType completeThirdType = idOutputPort.getThirdTypeById(thirdType.getThirdTypeId());
            if (!Boolean.TRUE.equals(completeThirdType.getStatus())) {
                throw new ThirdInvalidDataException("El tipo de tercero '" + completeThirdType.getThirdTypeName() + "' está inactivo");
            }
        }
    }
}
