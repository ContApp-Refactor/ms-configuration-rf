package co.unicauca.edu.co.contables.configuration.thirds.infrastructure.adapters.output.persistence;

import co.unicauca.edu.co.contables.configuration.thirds.application.ports.output.GeographyOutputPort;
import co.unicauca.edu.co.contables.configuration.thirds.application.ports.output.ThirdOutputPort;
import co.unicauca.edu.co.contables.configuration.thirds.application.service.geography.GeographyLoaderService;
import co.unicauca.edu.co.contables.configuration.thirds.domain.exceptions.third.ThirdNotFound;
import co.unicauca.edu.co.contables.configuration.thirds.domain.exceptions.thirdType.ThirdTypeForeignKeyViolationException;
import co.unicauca.edu.co.contables.configuration.thirds.domain.exceptions.typeId.TypeIdForeignKeyViolationException;
import co.unicauca.edu.co.contables.configuration.thirds.domain.model.*;
import co.unicauca.edu.co.contables.configuration.thirds.infrastructure.adapters.output.persistence.entity.ThirdEntity;
import co.unicauca.edu.co.contables.configuration.thirds.infrastructure.adapters.output.persistence.entity.ThirdTypeEntity;
import co.unicauca.edu.co.contables.configuration.thirds.infrastructure.adapters.output.persistence.entity.ThirdsAndTypesEntity;
import co.unicauca.edu.co.contables.configuration.thirds.infrastructure.adapters.output.persistence.entity.TypeIdEntity;
import co.unicauca.edu.co.contables.configuration.thirds.infrastructure.adapters.output.persistence.mapper.ThirdPersistenceMapper;
import co.unicauca.edu.co.contables.configuration.thirds.infrastructure.adapters.output.persistence.repository.ThirdRepository;
import co.unicauca.edu.co.contables.configuration.thirds.infrastructure.adapters.output.persistence.repository.ThirdTypeRepository;
import co.unicauca.edu.co.contables.configuration.thirds.infrastructure.adapters.output.persistence.repository.ThirdsAndTypesRepository;
import co.unicauca.edu.co.contables.configuration.thirds.infrastructure.adapters.output.persistence.repository.TypeIdRepository;
import co.unicauca.edu.co.contables.configuration.commons.multitenancy.utils.TenantContext;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * @brief Adaptador principal de persistencia para operaciones CRUD de terceros con multi-tenancy
 *
 * Implementa el puerto de salida ThirdOutputPort para gestionar todas las operaciones de persistencia
 * de terceros, incluyendo creación, actualización, eliminación, búsqueda y carga de datos geográficos.
 * Maneja validaciones de claves foráneas, relaciones muchos-a-muchos con tipos de tercero y
 * operaciones masivas optimizadas.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ThirdPersistenceAdapter implements ThirdOutputPort{
    @PersistenceContext
    private EntityManager entityManager;

    private final ThirdRepository thirdRepository;
    private final ThirdTypeRepository thirdTypeRepository;
    private final TypeIdRepository typeIdRepository;
    private final ThirdsAndTypesRepository thirdsAndTypesRepository;
    private final ThirdPersistenceMapper thirdPersistenceMapper;
    private final GeographyLoaderService geographyLoaderService;
    private final GeographyOutputPort geographyOutputPort;

    /**
     * @brief Guarda un tercero con validaciones y relaciones de tipos de tercero
     * @details Realiza validaciones de claves foráneas, asigna tenant ID, guarda el tercero principal
     * y crea las relaciones muchos-a-muchos con los tipos de tercero. Finalmente carga datos geográficos completos.
     * @param third Tercero a guardar con sus datos y tipos asociados
     * @return Tercero guardado con datos geográficos completos
     * @throws IllegalArgumentException si el tercero o su tipo de identificación son null
     */
    @Override
    public Third saveThird(Third third) {
        if (third == null) {
            throw new IllegalArgumentException("El tercero no puede ser null");
        }

        if (third.getTypeId() == null || third.getTypeId().getId() == null) {
            throw new IllegalArgumentException("El tipo de identificación del tercero no puede ser null");
        }

        validateTypeIdExists(third.getTypeId().getId());
        validateThirdTypesExist(third.getThirdTypes());

        // Preparar la entidad principal
        ThirdEntity thirdEntity = thirdPersistenceMapper.toThirdEntity(third);
        thirdEntity.setTenantId(TenantContext.getTenantId());

        // Obtener la referencia del tipo de identificación
        Optional<TypeIdEntity> typeIdEntityOpt = typeIdRepository.findById(third.getTypeId().getId());
        if (typeIdEntityOpt.isEmpty()) {
            throw new TypeIdForeignKeyViolationException(third.getTypeId().getId().toString());
        }
        thirdEntity.setTypeId(typeIdEntityOpt.get());

        // Guardar la entidad del tercero
        try {
            thirdEntity = thirdRepository.save(thirdEntity);
        } catch (DataIntegrityViolationException e) {
            throw e;
        }

        // Crear las relaciones con tipos de tercero
        if (third.getThirdTypes() != null && !third.getThirdTypes().isEmpty()) {
            for (ThirdType tt : third.getThirdTypes()) {
                ThirdsAndTypesEntity relationEntity = ThirdsAndTypesEntity.builder()
                        .thId(thirdEntity.getThId())
                        .ttId(tt.getThirdTypeId())
                        .tenantId(TenantContext.getTenantId())
                        .build();

                try {
                    thirdsAndTypesRepository.save(relationEntity);
                } catch (DataIntegrityViolationException e) {
                    throw e;
                }
            }
        }

        // Convertir de vuelta al dominio
        Third domainThird = thirdPersistenceMapper.toThird(thirdEntity);
        return loadGeographyData(domainThird, thirdEntity);
    }

    /**
     * @brief Guarda múltiples terceros en lote usando saveAll (optimizado)
     * @param thirds Lista de terceros a guardar (asume que ya están validados)
     * @return Lista de terceros guardados con datos geográficos completos
     * @details Este método está optimizado para importación masiva y asume que los datos
     * ya fueron validados previamente. NO realiza validaciones de existencia de TypeIds o ThirdTypes
     * para evitar redundancia con la fase de validación.
     */
    @Override
    public List<Third> saveAllThirds(List<Third> thirds) {
        if (thirds == null || thirds.isEmpty()) {
            return new ArrayList<>();
        }

        String currentTenant = TenantContext.getTenantId();
        List<ThirdEntity> entitiesToSave = new ArrayList<>();
        List<ThirdsAndTypesEntity> relationsToSave = new ArrayList<>();

        // PASO 1: Obtener todos los TypeIds únicos en UN SOLO batch query
        Set<Long> uniqueTypeIds = new HashSet<>();
        for (Third third : thirds) {
            if (third != null && third.getTypeId() != null && third.getTypeId().getId() != null) {
                uniqueTypeIds.add(third.getTypeId().getId());
            }
        }
        
        // Consulta batch de todos los TypeIds de una vez
        List<TypeIdEntity> typeIdEntities = typeIdRepository.findAllById(uniqueTypeIds);
        Map<Long, TypeIdEntity> typeIdCache = new HashMap<>();
        for (TypeIdEntity entity : typeIdEntities) {
            // Usar el ID (PK) de TypeIdEntity, NO el tiId (que es el código como "CC")
            typeIdCache.put(entity.getId(), entity);
        }

        // PASO 2: Preparar todas las entidades USANDO EL CACHE (sin más consultas)
        for (Third third : thirds) {
            if (third == null) {
                continue;
            }

            // Preparar la entidad principal
            ThirdEntity thirdEntity = thirdPersistenceMapper.toThirdEntity(third);
            thirdEntity.setTenantId(currentTenant);
            
            // Obtener del cache (SIN consulta a BD)
            TypeIdEntity typeIdEntity = typeIdCache.get(third.getTypeId().getId());
            if (typeIdEntity != null) {
                thirdEntity.setTypeId(typeIdEntity);
            }

            entitiesToSave.add(thirdEntity);
        }

        // Guardar todas las entidades principales en un solo batch
        List<ThirdEntity> savedEntities;
        try {
            savedEntities = thirdRepository.saveAll(entitiesToSave);
        } catch (DataIntegrityViolationException e) {
            throw e;
        }

        // Crear todas las relaciones con tipos de tercero
        for (int i = 0; i < savedEntities.size(); i++) {
            ThirdEntity savedEntity = savedEntities.get(i);
            Third originalThird = thirds.get(i);

            if (originalThird.getThirdTypes() != null && !originalThird.getThirdTypes().isEmpty()) {
                for (ThirdType tt : originalThird.getThirdTypes()) {
                    ThirdsAndTypesEntity relationEntity = ThirdsAndTypesEntity.builder()
                            .thId(savedEntity.getThId())
                            .ttId(tt.getThirdTypeId())
                            .tenantId(currentTenant)
                            .build();
                    relationsToSave.add(relationEntity);
                }
            }
        }

        // Guardar todas las relaciones en un solo batch
        if (!relationsToSave.isEmpty()) {
            try {
                thirdsAndTypesRepository.saveAll(relationsToSave);
            } catch (DataIntegrityViolationException e) {
                throw e;
            }
        }

        // Convertir de vuelta al dominio REUTILIZANDO la geografía original
        List<Third> result = new ArrayList<>();
        for (int i = 0; i < savedEntities.size(); i++) {
            ThirdEntity savedEntity = savedEntities.get(i);
            Third originalThird = thirds.get(i);
            
            // Reutilizar geografía del Third original (ya validada en Fase 2)
            Third savedThird = Third.builder()
                    .thId(savedEntity.getThId())
                    .entId(savedEntity.getEntId())
                    .typeId(originalThird.getTypeId())
                    .thirdTypes(originalThird.getThirdTypes())  
                    .personType(originalThird.getPersonType())
                    .names(originalThird.getNames())
                    .lastNames(originalThird.getLastNames())
                    .socialReason(originalThird.getSocialReason())
                    .gender(originalThird.getGender())
                    .idNumber(originalThird.getIdNumber())
                    .verificationNumber(originalThird.getVerificationNumber())
                    .state(originalThird.getState())
                    .country(originalThird.getCountry())
                    .province(originalThird.getProvince())  
                    .city(originalThird.getCity())  
                    .address(originalThird.getAddress())
                    .phoneNumber(originalThird.getPhoneNumber())
                    .email(originalThird.getEmail())
                    .build();
            
            result.add(savedThird);
        }       

        return result;
    }

    /**
     * @brief Carga datos geográficos completos desde servicios especializados
     * @details Utiliza GeographyLoaderService para cargar objetos completos de Country, State y City
     * basándose en los códigos almacenados en la entidad. Crea un nuevo objeto Third con toda la información geográfica.
     * @param third objeto Third del dominio con datos básicos
     * @param thirdEntity entidad JPA con códigos geográficos (country, province, city)
     * @return nuevo objeto Third con datos geográficos completos cargados
     */
    private Third loadGeographyData(Third third, ThirdEntity thirdEntity) {
        return Third.builder()
                .thId(third.getThId())
                .entId(third.getEntId())
                .typeId(third.getTypeId())
                .thirdTypes(third.getThirdTypes())
                .personType(third.getPersonType())
                .names(third.getNames())
                .lastNames(third.getLastNames())
                .socialReason(third.getSocialReason())
                .gender(third.getGender())
                .idNumber(third.getIdNumber())
                .verificationNumber(third.getVerificationNumber())
                .state(third.getState())
                .country(geographyLoaderService.loadCountryByCode(thirdEntity.getCountry()))
                .province(geographyLoaderService.loadStateByCode(thirdEntity.getProvince(), thirdEntity.getCountry()))
                .city(geographyLoaderService.loadCityByCode(thirdEntity.getCity(), thirdEntity.getProvince(), thirdEntity.getCountry()))
                .address(third.getAddress())
                .phoneNumber(third.getPhoneNumber())
                .email(third.getEmail())
                .build();
    }

    /**
     * @brief Valida existencia de tipo de identificación para claves foráneas
     * @details Verifica que el tipo de identificación referenciado exista en la base de datos
     * antes de proceder con operaciones que dependan de esta relación.
     * @param typeId ID del tipo de identificación a validar
     * @throws TypeIdForeignKeyViolationException si el tipo de identificación no existe
     */
    private void validateTypeIdExists(Long typeId) {
        if (!typeIdRepository.existsById(typeId)) {
            throw new TypeIdForeignKeyViolationException(typeId.toString());
        }
    }

    /**
     * @brief Valida existencia de todos los tipos de tercero para relaciones muchos-a-muchos
     * @details Verifica que cada tipo de tercero en el conjunto exista en la base de datos
     * antes de crear relaciones muchos-a-muchos en la tabla intermedia thirds_and_types.
     * @param thirdTypes conjunto de tipos de tercero a validar
     * @throws ThirdTypeForeignKeyViolationException si algún tipo de tercero no existe
     */
    private void validateThirdTypesExist(Set<ThirdType> thirdTypes) {
        if (thirdTypes != null && !thirdTypes.isEmpty()) {
            for (ThirdType tt : thirdTypes) {
                if (!thirdTypeRepository.existsById(tt.getThirdTypeId())) {
                    throw new ThirdTypeForeignKeyViolationException(String.valueOf(tt.getThirdTypeId()));
                }
            }
        }
    }

    /**
     * @brief Obtiene un tercero por su identificador y empresa.
     * @param id Identificador del tercero.
     * @param entId Identificador de la empresa.
     * @return Tercero encontrado.
     */
    @Override
    public Optional<Third> getThirdById(Long id, String entId) {
        Optional<ThirdEntity> thirdEntity = thirdRepository.findByThIdAndEntId(id, entId);

        if(thirdEntity.isEmpty()) {
            return Optional.empty();
        }

        Third third = convertToThird(thirdEntity.get());
        return Optional.of(third);
    }

    /**
     * @brief Encuentra un tercero por su ID (sin filtrar por empresa)
     * @param id El id del tercero
     * @return El tercero si existe, null en caso contrario
     */
    @Override
    public Third findById(Long id) {
        Optional<ThirdEntity> thirdEntity = thirdRepository.findByIdWithTypeId(id);

        if(thirdEntity.isEmpty()) {
            return null;
        }

        return convertToThird(thirdEntity.get());
    }

    /**
     * @brief Verifica existencia de tercero por ID y empresa
     * @details Consulta optimizada para verificar si existe un tercero con el ID especificado
     * dentro de la empresa indicada, sin cargar toda la entidad.
     * @param id ID del tercero a verificar
     * @param entId ID de la empresa para el alcance de la verificación
     * @return true si existe el tercero, false en caso contrario
     */
    @Override
    public boolean existThirdById(long id, String entId) {
        return thirdRepository.existThirdByThIdAndEntId(id, entId);
    }

    /**
     * @brief Cambia el estado de un tercero.
     * @param thId Identificador del tercero.
     * @param entId Identificador de la empresa.
     * @return Verdadero si el estado del tercero cambió, falso en caso contrario.
     */
    @Override
    public boolean changeThirdState(Long thId, String entId) {
        Optional<ThirdEntity> thirdEntity = thirdRepository.findByThIdAndEntId(thId, entId);

        if(thirdEntity.isEmpty()) {
            return false;
        }

        ThirdEntity entity = thirdEntity.get();
        Boolean currentState = entity.getState();
        Boolean newState = !Boolean.TRUE.equals(currentState);

        entity.setState(newState);
        thirdRepository.save(entity);

        return true;
    }

    /**
     * @brief Obtiene una página de terceros filtrados por el identificador de entidad.
     * @param entId El identificador de la entidad por la cual se filtrarán los terceros.
     * @param page El objeto Pageable que contiene la información de paginación.
     * @return Una página de objetos Third que representan los terceros.
     */
    @Override
    public Page<Third> getAllThirdsBy(String entId, Pageable page) {
        Page<ThirdEntity> pageEntities = thirdRepository.getThirdsBy(entId, page);
        Page<Third> pageThirds = pageEntities.map(this::convertToThird);

        return pageThirds;
    }

    /**
     * @brief Obtiene todos los terceros de una empresa filtrados por estado.   
     * @param entId El identificador de la entidad
     * @param state Estado de los terceros (true=activos, false=inactivos)
     * @param page El objeto Pageable que contiene la información de paginación
     * @return Una página de objetos Third filtrados por estado
     */
    @Override
    public Page<Third> getAllThirdsByState(String entId, Boolean state, Pageable page) {
        Page<ThirdEntity> pageEntities = thirdRepository.getThirdsByEntIdAndState(entId, state, page);
        Page<Third> pageThirds = pageEntities.map(this::convertToThird);

        return pageThirds;
    }

    /**
     * @brief Obtiene terceros optimizado para exportación (elimina problema N+1)
     * @details Utiliza consulta optimizada con JOIN FETCH y carga batch de ThirdTypes.
     * Este método es específicamente diseñado para operaciones de exportación masiva,
     * reduciendo drasticamente el número de queries a la base de datos.
     * @param entId El identificador de la entidad
     * @param page El objeto Pageable que contiene la información de paginación
     * @return Una página de objetos Third con todas sus relaciones cargadas eficientemente
     */
    public Page<Third> getAllThirdsForExport(String entId, Pageable page) {
        Page<ThirdEntity> pageEntities = thirdRepository.findAllForExport(entId, page);
        return convertPageForExport(pageEntities);
    }

    /**
     * @brief Obtiene terceros filtrados por estado optimizado para exportación
     * @details Similar a getAllThirdsForExport pero con filtro de estado.
     * Optimizado para eliminar el problema N+1 queries.
     * @param entId El identificador de la entidad
     * @param state Estado de los terceros (true=activos, false=inactivos)
     * @param page El objeto Pageable que contiene la información de paginación
     * @return Una página de objetos Third filtrados por estado con todas sus relaciones cargadas
     */
    public Page<Third> getAllThirdsByStateForExport(String entId, Boolean state, Pageable page) {
        Page<ThirdEntity> pageEntities = thirdRepository.findAllByStateForExport(entId, state, page);
        return convertPageForExport(pageEntities);
    }

    /**
     * @brief Convierte una página de ThirdEntity a Third optimizado para exportación
     * @details Carga todos los ThirdTypes y datos geográficos en consultas batch para evitar N+1 queries.
     * Pre-carga TODOS los datos geográficos una sola vez y crea un cache en memoria para acceso O(1).
     * @param pageEntities página de entidades a convertir
     * @return página de objetos Third con todas sus relaciones cargadas
     */
    private Page<Third> convertPageForExport(Page<ThirdEntity> pageEntities) {
        if (pageEntities.isEmpty()) {
            return Page.empty();
        }

        List<ThirdEntity> entities = pageEntities.getContent();
        
        // Extraer todos los IDs de terceros
        List<Long> thirdIds = entities.stream()
                .map(ThirdEntity::getThId)
                .toList();

        // Cargar TODAS las relaciones tercero-tipo en una sola consulta batch
        List<ThirdsAndTypesEntity> allRelations = thirdsAndTypesRepository.findByThIdInWithThirdType(thirdIds);

        // Agrupar relaciones por thId para acceso rápido O(1)
        Map<Long, List<ThirdsAndTypesEntity>> relationsByThirdId = allRelations.stream()
                .collect(java.util.stream.Collectors.groupingBy(ThirdsAndTypesEntity::getThId));

        // PRE-CARGAR TODOS los datos geográficos en 3 consultas (una sola vez por TODA la exportación)
        GeographyCache geoCache = loadGeographyCacheForExport();

        // Convertir entidades a modelo de dominio usando relaciones y geografía pre-cargadas
        List<Third> thirds = entities.stream()
                .map(entity -> convertToThirdForExport(
                    entity, 
                    relationsByThirdId.getOrDefault(entity.getThId(), Collections.emptyList()),
                    geoCache
                ))
                .toList();

        return new org.springframework.data.domain.PageImpl<>(thirds, pageEntities.getPageable(), pageEntities.getTotalElements());
    }

    /**
     * @brief Cache interno para datos geográficos (optimizado para exportación)
     * @details Almacena mapas en memoria para búsqueda O(1) de países, estados y ciudades
     */
    private static class GeographyCache {
        private final Map<String, Country> countries = new HashMap<>();
        private final Map<String, State> states = new HashMap<>(); // Key: stateCode_countryCode
        private final Map<String, City> cities = new HashMap<>(); // Key: cityCode_stateCode_countryCode

        public void putCountry(Country country) {
            if (country != null && country.getCountryCode() != null) {
                countries.put(country.getCountryCode().toUpperCase(), country);
            }
        }

        public void putState(State state) {
            if (state != null && state.getStateCode() != null && state.getCountryCode() != null) {
                String key = state.getStateCode() + "_" + state.getCountryCode().toUpperCase();
                states.put(key, state);
            }
        }

        public void putCity(City city) {
            if (city != null && city.getCityCode() != null && city.getStateCode() != null && city.getCountryCode() != null) {
                String key = city.getCityCode() + "_" + city.getStateCode() + "_" + city.getCountryCode().toUpperCase();
                cities.put(key, city);
            }
        }

        public Country getCountry(String countryCode) {
            return countryCode != null ? countries.get(countryCode.toUpperCase()) : null;
        }

        public State getState(String stateCode, String countryCode) {
            if (stateCode == null || countryCode == null) return null;
            String key = stateCode + "_" + countryCode.toUpperCase();
            return states.get(key);
        }

        public City getCity(String cityCode, String stateCode, String countryCode) {
            if (cityCode == null || stateCode == null || countryCode == null) return null;
            String key = cityCode + "_" + stateCode + "_" + countryCode.toUpperCase();
            return cities.get(key);
        }
    }

    /**
     * @brief Carga todos los datos geográficos en memoria para exportación
     * @details Ejecuta solo 3 queries para cargar TODOS los países, estados y ciudades.
     * Crea un cache en memoria para búsqueda O(1). Elimina ~54,000+ queries.
     * @return Cache con todos los datos geográficos indexados
     */
    private GeographyCache loadGeographyCacheForExport() {
        GeographyCache cache = new GeographyCache();

        // Query 1: Cargar TODOS los países activos (1 query)
        List<Country> countries = geographyOutputPort.getAllActiveCountries();
        countries.forEach(cache::putCountry);

        // Query 2: Cargar TODOS los estados activos (1 query)
        List<State> states = geographyOutputPort.getAllActiveStates();
        states.forEach(state -> {
            // Asociar país al estado
            if (state.getCountryCode() != null) {
                Country country = cache.getCountry(state.getCountryCode());
                if (country != null) {
                    state = State.builder()
                            .stateCode(state.getStateCode())
                            .stateName(state.getStateName())
                            .countryCode(state.getCountryCode())
                            .country(country)
                            .build();
                }
            }
            cache.putState(state);
        });

        // Query 3: Cargar TODAS las ciudades activas (1 query)
        List<City> cities = geographyOutputPort.getAllActiveCities();
        cities.forEach(city -> {
            // Asociar estado (que incluye país) a la ciudad
            if (city.getStateCode() != null && city.getCountryCode() != null) {
                State state = cache.getState(city.getStateCode(), city.getCountryCode());
                if (state != null) {
                    city = City.builder()
                            .cityCode(city.getCityCode())
                            .cityName(city.getCityName())
                            .stateCode(city.getStateCode())
                            .countryCode(city.getCountryCode())
                            .state(state)
                            .build();
                }
            }
            cache.putCity(city);
        });

        return cache;
    }

    /**
     * @brief Convierte ThirdEntity a Third usando relaciones y geografía pre-cargadas (CERO consultas adicionales)
     * @details Versión ultra-optimizada que usa SOLO datos en memoria. No realiza ninguna consulta a BD.
     * Elimina completamente el problema N+1 tanto para ThirdTypes como para datos geográficos.
     * @param thirdEntity entidad JPA con datos básicos del tercero
     * @param relations relaciones tercero-tipo ya cargadas con sus ThirdTypes
     * @param geoCache cache con todos los datos geográficos pre-cargados
     * @return objeto Third completo con datos geográficos y tipos de tercero
     */
    private Third convertToThirdForExport(ThirdEntity thirdEntity, List<ThirdsAndTypesEntity> relations, GeographyCache geoCache) {
        // Convertir entidad base a dominio
        Third obj = this.thirdPersistenceMapper.toThird(thirdEntity);
        
        // Cargar datos geográficos desde CACHE (sin consultas a BD)
        obj = loadGeographyDataFromCache(obj, thirdEntity, geoCache);

        // Mapear los tipos de tercero desde las relaciones PRE-CARGADAS (sin consultas adicionales)
        for (ThirdsAndTypesEntity relation : relations) {
            ThirdTypeEntity tt = relation.getThirdType(); // Ya está cargado por JOIN FETCH
            if (tt != null) {
                ThirdType thirdType = ThirdType.builder()
                        .thirdTypeId(tt.getTtId())
                        .thirdTypeName(tt.getTtName())
                        .entId(tt.getTtentId())
                        .status(tt.getStatus())
                        .build();
                obj.getThirdTypes().add(thirdType);
            }
        }

        obj.setUsageCount(thirdEntity.getUsageCount());
        return obj;
    }

    /**
     * @brief Carga datos geográficos desde cache en memoria (sin consultas a BD)
     * @details Búsqueda O(1) en mapas pre-cargados. NO hace ninguna consulta a la base de datos.
     * @param third objeto Third del dominio con datos básicos
     * @param thirdEntity entidad JPA con códigos geográficos
     * @param geoCache cache con datos geográficos pre-cargados
     * @return nuevo objeto Third con datos geográficos completos desde cache
     */
    private Third loadGeographyDataFromCache(Third third, ThirdEntity thirdEntity, GeographyCache geoCache) {
        Country country = geoCache.getCountry(thirdEntity.getCountry());
        State state = geoCache.getState(thirdEntity.getProvince(), thirdEntity.getCountry());
        City city = geoCache.getCity(thirdEntity.getCity(), thirdEntity.getProvince(), thirdEntity.getCountry());

        return Third.builder()
                .thId(third.getThId())
                .entId(third.getEntId())
                .typeId(third.getTypeId())
                .thirdTypes(third.getThirdTypes())
                .personType(third.getPersonType())
                .names(third.getNames())
                .lastNames(third.getLastNames())
                .socialReason(third.getSocialReason())
                .gender(third.getGender())
                .idNumber(third.getIdNumber())
                .verificationNumber(third.getVerificationNumber())
                .state(third.getState())
                .country(country)
                .province(state)
                .city(city)
                .address(third.getAddress())
                .phoneNumber(third.getPhoneNumber())
                .email(third.getEmail())
                .build();
    }

    /**
     * @brief Convierte ThirdEntity a Third con carga completa de relaciones
     * @details Mapea la entidad JPA al modelo de dominio, carga datos geográficos completos,
     * y recupera todas las relaciones muchos-a-muchos con tipos de tercero desde la tabla intermedia.
     * @param thirdEntity entidad JPA con datos básicos del tercero
     * @return objeto Third completo con datos geográficos y tipos de tercero asociados
     */
    private Third convertToThird(ThirdEntity thirdEntity) {


        Third obj = this.thirdPersistenceMapper.toThird(thirdEntity);
        obj = loadGeographyData(obj, thirdEntity);

        // Cargar los tipos de tercero desde la tabla de relación
        List<ThirdsAndTypesEntity> relations = thirdsAndTypesRepository.findByThId(thirdEntity.getThId());
        
        for(ThirdsAndTypesEntity relation : relations) {
            Optional<ThirdTypeEntity> thirdTypeEntity = thirdTypeRepository.findById(relation.getTtId());
            if (thirdTypeEntity.isPresent()) {
                ThirdTypeEntity tt = thirdTypeEntity.get();
                ThirdType thirdType = ThirdType.builder()
                    .thirdTypeId(tt.getTtId())
                    .thirdTypeName(tt.getTtName())
                    .entId(tt.getTtentId())
                    .status(tt.getStatus())                    
                    .build();
                obj.getThirdTypes().add(thirdType);
            }
        }
        // Asegurar que usageCount se copie correctamente desde la entidad
        obj.setUsageCount(thirdEntity.getUsageCount());
        return obj;
    }

    /**
     * @brief Actualiza tercero existente con validaciones y manejo de relaciones
     * @details Actualiza todos los campos del tercero, valida claves foráneas, actualiza las relaciones
     * muchos-a-muchos con tipos de tercero (eliminando existentes y creando nuevas), y retorna el tercero
     * actualizado con datos geográficos completos.
     * @param third objeto Third con datos actualizados (debe incluir thId)
     * @return Third actualizado con datos geográficos completos
     * @throws IllegalArgumentException si third es null o no tiene ID
     * @throws ThirdNotFound si no existe el tercero con el ID especificado
    */
    @Override
    public Third updateThird(Third third) {
        if (third == null) {
            throw new IllegalArgumentException("El tercero no puede ser null");
        }

        if (third.getThId() == null) {
            throw new IllegalArgumentException("El ID del tercero no puede ser null para actualizar");
        }

        // Buscar la entidad existente
        Optional<ThirdEntity> existingEntityOpt = thirdRepository.findById(third.getThId());
        if (existingEntityOpt.isEmpty()) {
            throw new ThirdNotFound("No se encontró el tercero con ID: " + third.getThId());
        }

        ThirdEntity thirdEntity = existingEntityOpt.get();

 

        // Validar que el TypeId existe antes de actualizar
        if (third.getTypeId() != null && third.getTypeId().getId() != null) {
            validateTypeIdExists(third.getTypeId().getId());
            Optional<TypeIdEntity> typeIdEntityOpt = typeIdRepository.findById(third.getTypeId().getId());
            if (typeIdEntityOpt.isPresent()) {
                thirdEntity.setTypeId(typeIdEntityOpt.get());
            }
        }

        // Validar que los ThirdTypes existen antes de actualizar
        if (third.getThirdTypes() != null && !third.getThirdTypes().isEmpty()) {
            validateThirdTypesExist(third.getThirdTypes());
        }

        // Actualizar los campos de la entidad principal
        thirdEntity.setNames(third.getNames());
        thirdEntity.setLastNames(third.getLastNames());
        thirdEntity.setIdNumber(third.getIdNumber());
        thirdEntity.setSocialReason(third.getSocialReason());
        thirdEntity.setAddress(third.getAddress());
        thirdEntity.setCity(third.getCity() != null ? third.getCity().getCityCode() : null);
        thirdEntity.setCountry(third.getCountry() != null ? third.getCountry().getCountryCode() : null);
        thirdEntity.setProvince(third.getProvince() != null ? third.getProvince().getStateCode() : null);
        thirdEntity.setPersonType(third.getPersonType());
        thirdEntity.setGender(third.getGender() != null ? third.getGender().name() : null);
        thirdEntity.setState(third.getState() != null ? third.getState() : true);
        thirdEntity.setPhoneNumber(third.getPhoneNumber());
        thirdEntity.setEmail(third.getEmail());

        // Guardar la entidad principal actualizada
        try {
            thirdEntity = thirdRepository.save(thirdEntity);
        } catch (DataIntegrityViolationException e) {
            throw e;
        }

        // Actualizar las relaciones de tipos de tercero
        updateThirdTypeRelations(thirdEntity.getThId(), third.getThirdTypes());

        // Convertir de vuelta al dominio
        Third updatedThird = thirdPersistenceMapper.toThird(thirdEntity);
        return loadGeographyData(updatedThird, thirdEntity);
    }


    /**
     * @brief Actualiza relaciones muchos-a-muchos con tipos de tercero
     * @details Elimina todas las relaciones existentes en la tabla intermedia thirds_and_types
     * para el tercero especificado, luego crea las nuevas relaciones basadas en el conjunto
     * de tipos de tercero proporcionado.
     * @param thirdId ID del tercero cuyas relaciones se van a actualizar
     * @param thirdTypes conjunto de nuevos tipos de tercero a asociar
     */
    private void updateThirdTypeRelations(Long thirdId, Set<ThirdType> thirdTypes) {
        // Eliminar todas las relaciones existentes
        List<ThirdsAndTypesEntity> existingRelations = thirdsAndTypesRepository.findByThId(thirdId);
        if (!existingRelations.isEmpty()) {
            thirdsAndTypesRepository.deleteAll(existingRelations);
        }
        
        // Crear las nuevas relaciones si hay tipos de tercero
        if (thirdTypes != null && !thirdTypes.isEmpty()) {
            for (ThirdType thirdType : thirdTypes) {
                ThirdsAndTypesEntity relationEntity = ThirdsAndTypesEntity.builder()
                        .thId(thirdId)
                        .ttId(thirdType.getThirdTypeId())
                        .tenantId(TenantContext.getTenantId())
                        .build();

                try {
                    thirdsAndTypesRepository.save(relationEntity);
                } catch (DataIntegrityViolationException e) {
                    throw e;
                }
            }
        }
    }
    
    /**
     * @brief Elimina tercero y sus asociaciones con manejo transaccional
     * @details Elimina primero las asociaciones en la tabla intermedia thirds_and_types,
     * luego elimina el tercero principal. Utiliza contexto de tenant para asegurar
     * aislamiento multi-tenant.
     * @param thirdId ID del tercero a eliminar
     * @param entId ID de la empresa para validación de pertenencia
     * @return true si se eliminó correctamente
     * @throws ThirdNotFound si no se encuentra el tercero
     * @throws IllegalArgumentException si los parámetros son inválidos
     */
    @Override
    @Transactional
    public boolean deleteThird(Long thirdId, String entId) {
        if (thirdId == null || entId == null || entId.trim().isEmpty()) {
            throw new IllegalArgumentException("El ID del tercero y el ID de la empresa son requeridos");
        }

        String currentTenant = TenantContext.getTenantId();
        try {
            TenantContext.setTenantId(currentTenant);

            // Buscar el tercero por ID y empresa
            Optional<ThirdEntity> thirdEntity = thirdRepository.findByThIdAndEntId(thirdId, entId);

            if (thirdEntity.isPresent()) {
                ThirdEntity entity = thirdEntity.get();



                // Eliminar las asociaciones del tercero
                List<ThirdsAndTypesEntity> relations = thirdsAndTypesRepository.findByThId(thirdId);
                if (!relations.isEmpty()) {
                    thirdsAndTypesRepository.deleteAll(relations);
                }

                // Eliminar el tercero
                thirdRepository.delete(entity);
                return true;
            }

            throw new ThirdNotFound("No se encontró el tercero con ID: " + thirdId + " en la empresa: " + entId);

        } finally {
            TenantContext.setTenantId(currentTenant);
        }
    }

    /**
     * @brief Cuenta el total de terceros por empresa
     * @details Retorna el conteo total de terceros registrados para la empresa especificada,
     * incluyendo tanto activos como inactivos.
     * @param entId ID de la empresa
     * @return número total de terceros de la empresa
     * @throws IllegalArgumentException si entId es null o vacío
     */
    @Override
    public long countAllThirdsByEntId(String entId) {
        if (entId == null || entId.trim().isEmpty()) {
            throw new IllegalArgumentException("El ID de la empresa no puede ser null o vacío");
        }
        return thirdRepository.countByEntId(entId);
    }

    /**
     * @brief Actualización masiva del estado de terceros con query optimizada
     * @details Utiliza una consulta UPDATE nativa del repositorio para cambiar el estado
     * de todos los terceros de una empresa de forma eficiente. Maneja el contexto de tenant
     * para asegurar aislamiento multi-tenant.
     * @param entId ID de la empresa cuyos terceros serán actualizados
     * @param newState nuevo estado a asignar (true=activo, false=inactivo)
     * @return cantidad de terceros actualizados
     */
    @Override
    @Transactional
    public int bulkUpdateThirdState(String entId, Boolean newState) {
        String currentTenant = TenantContext.getTenantId();
        try {
            TenantContext.setTenantId(currentTenant);
            return thirdRepository.bulkUpdateStateByEntId(entId, newState);
        } finally {
            TenantContext.setTenantId(currentTenant);
        }
    }

    /**
     * @brief Valida existencia de números de identificación para importaciones batch
     * @details Utiliza consulta optimizada del repositorio para verificar qué números de identificación
     * del conjunto proporcionado ya existen en la base de datos para la empresa especificada.
     * Esencial para validaciones durante procesos de importación masiva.
     * @param idNumbers conjunto de números de identificación a validar
     * @param entId ID de la empresa para filtrar la búsqueda
     * @return conjunto de números de identificación que ya existen en BD
     */
    @Override
    public Set<Long> findExistingIdNumbers(Set<Long> idNumbers, String entId) {
        if (idNumbers == null || idNumbers.isEmpty()) {
            return Collections.emptySet();
        }
        
        if (entId == null || entId.trim().isEmpty()) {
            throw new IllegalArgumentException("El ID de la empresa no puede ser null o vacío");
        }

        // Usar consulta batch optimizada del repositorio
        List<Long> existingList = thirdRepository.findExistingIdNumbers(idNumbers, entId);
        return new HashSet<>(existingList);
    }

    /**
     * @brief Busca terceros por empresa y término de búsqueda con ordenamiento.
     * @param entId El id de la empresa
     * @param search Término de búsqueda
     * @param page Número de página
     * @param size Tamaño de página
     * @param sortField Campo de ordenamiento
     * @param sortOrder Orden (asc/desc)
     * @return Página de terceros que coinciden con la búsqueda
     */
    @Override
    @Transactional(readOnly = true)
    public Page<Third> findByEntIdAndSearch(String entId, String search, int page, int size, String sortField, String sortOrder) {
        String entitySortField = mapThirdSortField(sortField);
        Sort sort = "desc".equalsIgnoreCase(sortOrder) 
            ? Sort.by(entitySortField).descending() 
            : Sort.by(entitySortField).ascending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<ThirdEntity> pageEntities = thirdRepository.findByEntIdAndSearch(entId, search, pageable);
        Page<Third> pageThirds = pageEntities.map(this::convertToThird);
        
        return pageThirds;
    }

    /**
     * @brief Cuenta terceros por empresa y término de búsqueda.
     * @param entId El id de la empresa
     * @param search Término de búsqueda
     * @return Cantidad de terceros que coinciden
     */
    @Override
    @Transactional(readOnly = true)
    public long countByEntIdAndSearch(String entId, String search) {
        return thirdRepository.countByEntIdAndSearch(entId, search);
    }

    /**
     * @brief Obtiene todos los terceros con ordenamiento.
     * @param entId El id de la empresa
     * @param page Número de página
     * @param size Tamaño de página
     * @param sortField Campo de ordenamiento
     * @param sortOrder Orden (asc/desc)
     * @return Página de terceros ordenados
     */
    @Override
    @Transactional(readOnly = true)
    public Page<Third> getAllThirdsByWithSort(String entId, int page, int size, String sortField, String sortOrder) {
        String entitySortField = mapThirdSortField(sortField);
        Sort sort = "desc".equalsIgnoreCase(sortOrder) 
            ? Sort.by(entitySortField).descending() 
            : Sort.by(entitySortField).ascending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<ThirdEntity> pageEntities = thirdRepository.getThirdsBy(entId, pageable);
        Page<Third> pageThirds = pageEntities.map(this::convertToThird);
        
        return pageThirds;
    }    

    /**
     * @brief Obtiene todos los terceros activos con ordenamiento.
     *
     * @param entId El id de la empresa
     * @param page Número de página
     * @param size Tamaño de página
     * @param sortField Campo de ordenamiento
     * @param sortOrder Orden (asc/desc)
     * @return Página de terceros activos ordenados
     */
    @Override
    @Transactional(readOnly = true)
    public Page<Third> getAllActiveThirdsByWithSort(String entId, int page, int size, String sortField, String sortOrder) {
        String entitySortField = mapThirdSortField(sortField);
        Sort sort = "desc".equalsIgnoreCase(sortOrder)
            ? Sort.by(entitySortField).descending()
            : Sort.by(entitySortField).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<ThirdEntity> pageEntities = thirdRepository.getActiveThirdsBy(entId, pageable);
        Page<Third> pageThirds = pageEntities.map(this::convertToThird);

        return pageThirds;
    }

    /**
     * @brief Cuenta el total de terceros activos por empresa.     *
     * @param entId el ID de la empresa
     * @return el número total de terceros activos
     */
    @Override
    @Transactional(readOnly = true)
    public long countActiveThirdsByEntId(String entId) {
        return thirdRepository.countActiveByEntId(entId);
    }


    /**
     * @brief Mapea campos de ordenamiento del dominio a la entidad JPA
     * @details Traduce los nombres de campos del modelo Third a los nombres de campos
     * correspondientes en ThirdEntity para consultas de ordenamiento. Solo permite
     * ordenamiento por campos seguros: nombres y número de identificación.
     * @param sortField nombre del campo de ordenamiento en el modelo de dominio
     * @return nombre del campo correspondiente en la entidad JPA
     */
    private String mapThirdSortField(String sortField) {
        if (sortField == null || sortField.trim().isEmpty()) {
            return "names"; // Default
        }
        switch (sortField.toLowerCase()) {
            case "names":
                return "names";
            case "idnumber":
                return "idNumber";
            default:
                return "names"; // Default para cualquier otro campo
        }
    }

    /**
     * @brief Obtiene terceros filtrados por empresa y nombre de tipo de tercero activo con paginación
     * @param entId El id de la empresa
     * @param thirdTypeName El nombre del tipo de tercero activo (case insensitive)
     * @param page El objeto pageable para paginación con ordenamiento ASC por defecto
     * @return La página de terceros filtrados por tipo activo
     */
    @Override
    @Transactional(readOnly = true)
    public Page<Third> getThirdsByEntIdAndThirdTypeName(String entId, String thirdTypeName, Pageable page) {
        Page<ThirdEntity> pageEntities = thirdRepository.findByEntIdAndThirdTypeName(entId, thirdTypeName, page);
        Page<Third> pageThirds = pageEntities.map(this::convertToThird);

        return pageThirds;
    }

    /**
     * @brief Cuenta el total de terceros por empresa y nombre de tipo de tercero activo
     * @param entId El id de la empresa
     * @param thirdTypeName El nombre del tipo de tercero activo (case insensitive)
     * @return El número total de terceros del tipo activo especificado
     */
    @Override
    @Transactional(readOnly = true)
    public long countThirdsByEntIdAndThirdTypeName(String entId, String thirdTypeName) {
        return thirdRepository.countByEntIdAndThirdTypeName(entId, thirdTypeName);
    }

    /**
     * @brief Incrementa el contador de uso de un tercero de forma optimizada
     * @details Utiliza una query optimizada para incrementar el usageCount sin pasar
     * por las validaciones de actualización completa. Esto permite incrementar el
     * contador incluso cuando el tercero ya tiene movimientos contables asociados.
     * @param thirdId ID del tercero cuyo contador se va a incrementar
     * @return true si se incrementó correctamente, false si el tercero no existe
     */
    @Override
    @Transactional
    public boolean incrementUsageCount(Long thirdId) {
        if (thirdId == null) {
            log.warn("Intento de incrementar usageCount con thirdId null");
            return false;
        }

        int updatedRows = thirdRepository.incrementUsageCountByThirdId(thirdId);
        boolean success = updatedRows > 0;  
        
        return success;
    }

}
