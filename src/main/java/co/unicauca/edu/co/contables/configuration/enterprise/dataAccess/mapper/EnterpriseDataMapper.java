package co.unicauca.edu.co.contables.configuration.enterprise.dataAccess.mapper;

import co.unicauca.edu.co.contables.configuration.enterprise.dataAccess.entity.EnterpriseEntity;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.models.*;
import org.springframework.stereotype.Component;

/**
 * @brief Mapeador de datos para empresas
 *
 * Convierte entre la entidad JPA {@link EnterpriseEntity} y el modelo de
 * dominio {@link Enterprise}, incluyendo todas sus relaciones anidadas
 * (ubicación, tipo de persona, tipo de contribuyente, tipo de empresa y
 * responsabilidades tributarias).
 *
 * Se implementa como clase manual (no MapStruct) porque el grafo de
 * relaciones de Enterprise es más profundo que el de sus pares
 * (classesOfDocuments, costCenters, etc.) y requiere construir objetos de
 * dominio anidados explícitamente.
 */
@Component
public class EnterpriseDataMapper {

    public EnterpriseEntity toEntity(Enterprise domain) {
        if (domain == null) {
            return null;
        }
        return EnterpriseEntity.builder()
                .id(domain.getId())
                .idUser(domain.getIdUser())
                .name(domain.getName())
                .nit(domain.getNit())
                .DV(domain.getDV())
                .phone(domain.getPhone())
                .branch(domain.getBranch())
                .email(domain.getEmail())
                .logo(domain.getLogo())
                .state(domain.getState())
                .inventoryConfigurationType(domain.getInventoryConfigurationType())
                .mainActivity(domain.getMainActivity())
                .secondaryActivity(domain.getSecondaryActivity())
                .taxLiabilities(toTaxLiabilityEntityList(domain.getTaxLiabilities()))
                .taxPayerType(toTaxPayerTypeEntity(domain.getTaxPayerType()))
                .enterpriseType(toEnterpriseTypeEntity(domain.getEnterpriseType()))
                .personType(toPersonTypeEntity(domain.getPersonType()))
                .location(toLocationEntity(domain.getLocation()))
                .build();
    }

    public Enterprise toDomain(EnterpriseEntity entity) {
        if (entity == null) {
            return null;
        }
        return Enterprise.builder()
                .id(entity.getId())
                .idUser(entity.getIdUser())
                .name(entity.getName())
                .nit(entity.getNit())
                .DV(entity.getDV())
                .phone(entity.getPhone())
                .branch(entity.getBranch())
                .email(entity.getEmail())
                .logo(entity.getLogo())
                .state(entity.getState())
                .inventoryConfigurationType(entity.getInventoryConfigurationType())
                .mainActivity(entity.getMainActivity())
                .secondaryActivity(entity.getSecondaryActivity())
                .taxLiabilities(toTaxLiabilityDomainList(entity.getTaxLiabilities()))
                .taxPayerType(toTaxPayerTypeDomain(entity.getTaxPayerType()))
                .enterpriseType(toEnterpriseTypeDomain(entity.getEnterpriseType()))
                .personType(toPersonTypeDomain(entity.getPersonType()))
                .location(toLocationDomain(entity.getLocation()))
                .build();
    }

    private java.util.List<co.unicauca.edu.co.contables.configuration.enterprise.dataAccess.entity.TaxLiabilityEntity> toTaxLiabilityEntityList(
            java.util.List<TaxLiability> taxLiabilities) {
        if (taxLiabilities == null) {
            return null;
        }
        return taxLiabilities.stream()
                .map(t -> co.unicauca.edu.co.contables.configuration.enterprise.dataAccess.entity.TaxLiabilityEntity
                        .builder().id(t.getId()).name(t.getName()).build())
                .toList();
    }

    private java.util.List<TaxLiability> toTaxLiabilityDomainList(
            java.util.List<co.unicauca.edu.co.contables.configuration.enterprise.dataAccess.entity.TaxLiabilityEntity> entities) {
        if (entities == null) {
            return null;
        }
        return entities.stream()
                .map(e -> TaxLiability.builder().id(e.getId()).name(e.getName()).build())
                .toList();
    }

    private co.unicauca.edu.co.contables.configuration.enterprise.dataAccess.entity.TaxPayerTypeEntity toTaxPayerTypeEntity(
            TaxPayerType taxPayerType) {
        if (taxPayerType == null) {
            return null;
        }
        return co.unicauca.edu.co.contables.configuration.enterprise.dataAccess.entity.TaxPayerTypeEntity.builder()
                .id(taxPayerType.getId()).name(taxPayerType.getName()).build();
    }

    private TaxPayerType toTaxPayerTypeDomain(
            co.unicauca.edu.co.contables.configuration.enterprise.dataAccess.entity.TaxPayerTypeEntity entity) {
        if (entity == null) {
            return null;
        }
        return TaxPayerType.builder().id(entity.getId()).name(entity.getName()).build();
    }

    private co.unicauca.edu.co.contables.configuration.enterprise.dataAccess.entity.EnterpriseTypeEntity toEnterpriseTypeEntity(
            EnterpriseType enterpriseType) {
        if (enterpriseType == null) {
            return null;
        }
        return co.unicauca.edu.co.contables.configuration.enterprise.dataAccess.entity.EnterpriseTypeEntity.builder()
                .id(enterpriseType.getId()).name(enterpriseType.getName()).build();
    }

    private EnterpriseType toEnterpriseTypeDomain(
            co.unicauca.edu.co.contables.configuration.enterprise.dataAccess.entity.EnterpriseTypeEntity entity) {
        if (entity == null) {
            return null;
        }
        return EnterpriseType.builder().id(entity.getId()).name(entity.getName()).build();
    }

    private co.unicauca.edu.co.contables.configuration.enterprise.dataAccess.entity.PersonTypeEntity toPersonTypeEntity(
            PersonType personType) {
        if (personType == null) {
            return null;
        }
        return co.unicauca.edu.co.contables.configuration.enterprise.dataAccess.entity.PersonTypeEntity.builder()
                .id(personType.getId())
                .type(personType.getType())
                .name(personType.getName())
                .surname(personType.getSurname())
                .bussinessName(personType.getBussinessName())
                .build();
    }

    private PersonType toPersonTypeDomain(
            co.unicauca.edu.co.contables.configuration.enterprise.dataAccess.entity.PersonTypeEntity entity) {
        if (entity == null) {
            return null;
        }
        return PersonType.builder()
                .id(entity.getId())
                .type(entity.getType())
                .name(entity.getName())
                .surname(entity.getSurname())
                .bussinessName(entity.getBussinessName())
                .build();
    }

    private co.unicauca.edu.co.contables.configuration.enterprise.dataAccess.entity.LocationEntity toLocationEntity(
            Location location) {
        if (location == null) {
            return null;
        }
        return co.unicauca.edu.co.contables.configuration.enterprise.dataAccess.entity.LocationEntity.builder()
                .id(location.getId())
                .address(location.getAddress())
                .city(location.getCity() == null ? null
                        : co.unicauca.edu.co.contables.configuration.enterprise.dataAccess.entity.CityEntity.builder()
                                .id(location.getCity().getId()).name(location.getCity().getName()).build())
                .country(location.getCountry() == null ? null
                        : co.unicauca.edu.co.contables.configuration.enterprise.dataAccess.entity.CountryEntity
                                .builder().id(location.getCountry().getId()).name(location.getCountry().getName())
                                .build())
                .department(location.getDepartment() == null ? null
                        : co.unicauca.edu.co.contables.configuration.enterprise.dataAccess.entity.DepartmentEntity
                                .builder().id(location.getDepartment().getId())
                                .name(location.getDepartment().getName()).build())
                .build();
    }

    private Location toLocationDomain(
            co.unicauca.edu.co.contables.configuration.enterprise.dataAccess.entity.LocationEntity entity) {
        if (entity == null) {
            return null;
        }
        return Location.builder()
                .id(entity.getId())
                .address(entity.getAddress())
                .city(entity.getCity() == null ? null
                        : City.builder().id(entity.getCity().getId()).name(entity.getCity().getName()).build())
                .country(entity.getCountry() == null ? null
                        : Country.builder().id(entity.getCountry().getId()).name(entity.getCountry().getName())
                                .build())
                .department(entity.getDepartment() == null ? null
                        : Department.builder().id(entity.getDepartment().getId())
                                .name(entity.getDepartment().getName()).build())
                .build();
    }
}
