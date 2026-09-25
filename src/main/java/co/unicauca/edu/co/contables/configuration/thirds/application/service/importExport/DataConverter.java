package co.unicauca.edu.co.contables.configuration.thirds.application.service.importExport;

import co.unicauca.edu.co.contables.configuration.thirds.domain.model.*;
import co.unicauca.edu.co.contables.configuration.thirds.domain.utils.StringNormalizer;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @brief Servicio especializado en conversión de datos Excel a entidades de dominio
 *
 * Utiliza cache pre-cargado para evitar consultas N+1 y optimizar
 * el rendimiento durante conversiones masivas de datos.
 */

@Service
@RequiredArgsConstructor
public class DataConverter {

    /**
     * @brief Convierte datos de Excel a entidad Third usando cache pre-cargado
     * @param excelData Datos extraídos del archivo Excel
     * @param cache Cache con datos de referencia pre-cargados
     * @return Entidad Third construida o null si faltan campos requeridos
     */
    public Third convertWithCache(ThirdExcelData excelData, BatchValidationService.ReferenceDataCache cache) {
        if (!excelData.hasRequiredFields()) {
                        return null;
        }

        // Obtener TypeId desde cache
        TypeId typeId = getTypeIdFromCache(excelData.getTypeIdName(), cache);
        if (typeId == null) {            
            return null;
        }

        // Obtener ThirdTypes desde cache
        Set<ThirdType> thirdTypes = getThirdTypesFromCache(excelData.getThirdTypesNames(), cache);

        // Obtener geografía completa desde cache
        Country country = null;
        State state = null;
        City city = null;
        
        if (excelData.getCountryName() != null && !excelData.getCountryName().trim().isEmpty()) {
            country = cache.getCountry(excelData.getCountryName().trim());
        }
        
        if (excelData.getStateName() != null && !excelData.getStateName().trim().isEmpty()) {
            state = cache.getState(excelData.getStateName().trim());
        }
        
        if (excelData.getCityName() != null && !excelData.getCityName().trim().isEmpty() && state != null) {
            city = cache.getCity(excelData.getCityName().trim(), state.getStateCode());
        }

        return Third.builder()
                .entId(excelData.getEntId())
                .typeId(typeId)
                .thirdTypes(thirdTypes)
                .personType(excelData.getPersonType())
                .names(normalizeString(excelData.getNames()))
                .lastNames(normalizeString(excelData.getLastNames()))
                .socialReason(normalizeString(excelData.getSocialReason()))
                .gender(excelData.getGender())
                .idNumber(excelData.getIdNumber())
                .verificationNumber(excelData.getVerificationNumber())
                .state(excelData.getState() != null ? excelData.getState() : true)
                .address(excelData.getAddress())
                .phoneNumber(excelData.getPhoneNumber())
                .email(excelData.getEmail())
                .country(country)    
                .province(state)     
                .city(city)          
                .build();
    }

    /**
     * @brief Obtiene los códigos geográficos resueltos para un registro
     * @param excelData Datos del Excel con nombres geográficos
     * @param cache Cache con datos de referencia geográficos
     * @return Objeto GeographyData con códigos resueltos
     */
    public GeographyData getGeographyData(ThirdExcelData excelData, BatchValidationService.ReferenceDataCache cache) {
        return resolveGeographyFromCache(excelData, cache);
    }

    /**
     * @brief Obtiene TypeId desde cache evitando consultas individuales
     * @param typeIdName Nombre del tipo de identificación
     * @param cache Cache con datos de referencia
     * @return TypeId encontrado o null si no existe
     */
    private TypeId getTypeIdFromCache(String typeIdName, BatchValidationService.ReferenceDataCache cache) {
        if (typeIdName == null || typeIdName.trim().isEmpty()) {
            return null;
        }
        return cache.getTypeId(typeIdName.trim().toUpperCase());
    }

    /**
     * @brief Obtiene ThirdTypes desde cache evitando consultas individuales
     * @param typeNames Conjunto de nombres de tipos de tercero
     * @param cache Cache con datos de referencia
     * @return Set de ThirdType encontrados (puede estar vacío)
     */
    private Set<ThirdType> getThirdTypesFromCache(Set<String> typeNames, BatchValidationService.ReferenceDataCache cache) {
        if (typeNames == null || typeNames.isEmpty()) {
            return new HashSet<>();
        }

        return typeNames.stream()
                .map(name -> cache.getThirdType(name.trim()))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    /**
     * @brief Resuelve geografía desde cache
     *
     * Convierte nombres geográficos a códigos usando la jerarquía:
     * país -> estado -> ciudad. Todos los campos son opcionales.
     *
     * @param excelData Datos del Excel con nombres geográficos
     * @param cache Cache con datos de referencia geográficos
     * @return GeographyData con códigos resueltos (pueden ser null)
     */
    private GeographyData resolveGeographyFromCache(ThirdExcelData excelData, BatchValidationService.ReferenceDataCache cache) {
        String countryCode = null;
        String stateCode = null;
        String cityCode = null;

      
        if (excelData.getCountryName() != null && !excelData.getCountryName().trim().isEmpty()) {
            if (cache.hasCountry(excelData.getCountryName().trim())) {                
                countryCode = "COL"; // Por ahora solo Colombia
            }
        }

        
        if (countryCode != null && 
            excelData.getStateName() != null && !excelData.getStateName().trim().isEmpty()) {
            State state = cache.getState(excelData.getStateName().trim());
            if (state != null) {
                stateCode = state.getStateCode();
            }
        }

       
        if (stateCode != null && 
            excelData.getCityName() != null && !excelData.getCityName().trim().isEmpty()) {
            if (cache.hasCity(excelData.getCityName().trim(), stateCode)) {
                City city = cache.getCity(excelData.getCityName().trim(), stateCode);
                if (city != null) {
                    cityCode = city.getCityCode();
                }
            }
        }

        return new GeographyData(countryCode, stateCode, cityCode);
    }

    /**
     * @brief Normaliza strings preservando case cuando es necesario
     * @param value String a normalizar
     * @return String normalizado o null si el valor original es null
     */
    private String normalizeString(String value) {
        return value != null ? StringNormalizer.normalizePreservingCase(value) : null;
    }

    /**
     * @brief Clase auxiliar para encapsular datos geográficos resueltos
     *
     * Contiene los códigos geográficos resueltos desde nombres,
     * manteniendo la jerarquía: país -> estado -> ciudad.
     */
    @Getter
    @AllArgsConstructor
    public static class GeographyData {
        private final String countryCode;
        private final String stateCode;
        private final String cityCode;
    }
}
