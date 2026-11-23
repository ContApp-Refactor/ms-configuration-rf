package co.unicauca.edu.co.contables.configuration.thirds.infrastructure.adapters.output.persistence;

import co.unicauca.edu.co.contables.configuration.thirds.application.ports.output.IdOutputPort;
import co.unicauca.edu.co.contables.configuration.thirds.domain.exceptions.thirdType.ThirdTypeInvalidDataException;
import co.unicauca.edu.co.contables.configuration.thirds.domain.exceptions.thirdType.ThirdTypeNameAlreadyExistsException;
import co.unicauca.edu.co.contables.configuration.thirds.domain.exceptions.thirdType.ThirdTypeNotFound;
import co.unicauca.edu.co.contables.configuration.thirds.domain.exceptions.typeId.*;
import co.unicauca.edu.co.contables.configuration.thirds.domain.model.ThirdType;
import co.unicauca.edu.co.contables.configuration.thirds.domain.model.TypeId;
import co.unicauca.edu.co.contables.configuration.thirds.domain.utils.StringNormalizer;
import co.unicauca.edu.co.contables.configuration.thirds.infrastructure.adapters.output.persistence.entity.ThirdTypeEntity;
import co.unicauca.edu.co.contables.configuration.thirds.infrastructure.adapters.output.persistence.entity.TypeIdEntity;
import co.unicauca.edu.co.contables.configuration.thirds.infrastructure.adapters.output.persistence.mapper.IdPersistenceMapper;
import co.unicauca.edu.co.contables.configuration.thirds.infrastructure.adapters.output.persistence.repository.ThirdRepository;
import co.unicauca.edu.co.contables.configuration.thirds.infrastructure.adapters.output.persistence.repository.ThirdTypeRepository;
import co.unicauca.edu.co.contables.configuration.thirds.infrastructure.adapters.output.persistence.repository.ThirdsAndTypesRepository;
import co.unicauca.edu.co.contables.configuration.thirds.infrastructure.adapters.output.persistence.repository.TypeIdRepository;
import co.unicauca.edu.co.contables.commons.multitenancy.utils.TenantContext;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * @brief Adaptador principal de persistencia para tipos de tercero e identificación
 *
 * Implementa IdOutputPort para gestionar operaciones CRUD de TypeId y ThirdType
 * con validaciones avanzadas, normalización de datos y soporte multi-tenant.
 * Maneja búsquedas paginadas, validaciones de unicidad y operaciones batch.
 */
@Component
@RequiredArgsConstructor
public class IdPersistenceAdapter implements IdOutputPort {

    @PersistenceContext
    private EntityManager entityManager;

    private final ThirdRepository thirdRepository;
    private final ThirdTypeRepository thirdTypeRepository;
    private final ThirdsAndTypesRepository thirdsAndTypesRepository;
    private final TypeIdRepository typeIdRepository;
    private final IdPersistenceMapper idPersistenceMapper;

    /**
     * @brief Guarda un tipo de tercero.
     * 
     * @param thirdType Tipo de tercero a guardar.
     * @return Tipo de tercero guardado.
     */
    @Override
    public ThirdType saveThirdType(ThirdType thirdType) {
        ThirdTypeEntity thirdTypeEntity = idPersistenceMapper.toThirdTypeEntity(thirdType);

        // Asignar tenant ID del contexto actual
        thirdTypeEntity.setTenantId(TenantContext.getTenantId());

        thirdTypeRepository.save(thirdTypeEntity);
        ThirdType result = idPersistenceMapper.toThirdType(thirdTypeEntity);
        return result;
    }

    /**
     * @brief Obtiene todos los tipos de terceros.
     * 
     * @param entId Id de la entidad.
     * @return Lista de tipos de terceros.
     */
    @Override
    public List<ThirdType> getALLThirdTypes(String entId) {
        return idPersistenceMapper.toThirdTypeList(thirdTypeRepository.findAllByTtentId(entId));
    }

    /**
     * @brief Crea nuevo tipo de identificación con validaciones exhaustivas
     * @details Valida datos requeridos, normaliza códigos y nombres, verifica unicidad
     * tanto de código como nombre (case-insensitive), valida clasificación de persona,
     * asigna tenant ID y persiste la entidad.
     * @param typeId objeto TypeId con datos del nuevo tipo de identificación
     * @return TypeId creado con ID asignado
     * @throws TypeIdInvalidDataException si faltan datos requeridos
     * @throws PersonClassificationInvalidException si la clasificación es inválida
     * @throws TypeIdAlreadyExists si ya existe código o nombre similar
     */
    @Override
    public TypeId saveTypeId(TypeId typeId) {
        if (typeId == null) {
            throw new TypeIdInvalidDataException("El tipo de identificación no puede ser null");
        }

        if (typeId.getTypeId() == null || typeId.getTypeId().trim().isEmpty()) {
            throw new TypeIdInvalidDataException("El código del tipo de identificación no puede estar vacío");
        }

        if (typeId.getTypeIdname() == null || typeId.getTypeIdname().trim().isEmpty()) {
            throw new TypeIdInvalidDataException("El nombre del tipo de identificación no puede estar vacío");
        }

        if (typeId.getClassification() == null) {
            throw PersonClassificationInvalidException.forNullClassification();
        }

        // Normalizar valores para validaciones
        String normalizedTypeId = StringNormalizer.normalizeCode(typeId.getTypeId());
        String normalizedTypeIdName = StringNormalizer.normalizePreservingCase(typeId.getTypeIdname());

        if (typeIdRepository.existsByTiIdAndTientId(normalizedTypeId, typeId.getEntId())) {
            throw new TypeIdAlreadyExists(
                    "Ya existe un tipo de identificación con el código '" + normalizedTypeId + "'");
        }

        // Validar que no exista un typeIdname similar (case-insensitive) usando el
        // nombre normalizado
        if (typeIdRepository.existsByTiNameIgnoreCaseAndTientId(normalizedTypeIdName, typeId.getEntId())) {
            throw new TypeIdNameAlreadyExistsException(typeId.getTypeIdname());
        }

        // Crear el modelo normalizado para guardar
        TypeId normalizedTypeIdModel = TypeId.builder()
                .typeId(normalizedTypeId)
                .typeIdname(normalizedTypeIdName)
                .entId(typeId.getEntId())
                .status(typeId.getStatus())
                .classification(typeId.getClassification())
                .build();

        TypeIdEntity typeIdEntity = idPersistenceMapper.toTypeIdEntity(normalizedTypeIdModel);

        // Asignar tenant ID del contexto actual
        typeIdEntity.setTenantId(TenantContext.getTenantId());

        if (typeIdEntity.getTiId() == null) {
            typeIdEntity.setTiId(normalizedTypeId);
        }

        typeIdRepository.save(typeIdEntity);
        TypeId result = idPersistenceMapper.toTypeId(typeIdEntity);
        return result;
    }

    /**
     * @brief Obtiene todos los tipos de identificación.     * 
     * @param entId Id de la entidad.
     * @return Lista de tipos de identificación.
     */
    @Override
    public List<TypeId> getAllTypeIds(String entId) {
        return idPersistenceMapper.toTypeIdList(typeIdRepository.findAllByTientId(entId));
    }

    /**
     * @brief Actualiza tipo de identificación existente con validaciones completas
     * @details Verifica existencia, valida pertenencia a empresa, normaliza datos,
     * verifica unicidad excluyendo el registro actual, valida clasificación de persona,
     * preserva datos de auditoría y actualiza la entidad.
     * @param typeId objeto TypeId con datos actualizados (debe incluir ID)
     * @return TypeId actualizado
     * @throws TypeIdInvalidDataException si faltan datos o no pertenece a la empresa
     * @throws PersonClassificationInvalidException si la clasificación es inválida
     * @throws TypeIdNotFound si no existe el tipo de identificación
     * @throws TypeIdAlreadyExists si ya existe código o nombre similar
     */
    @Override
    public TypeId updateTypeId(TypeId typeId) {
        if (typeId == null) {
            throw new TypeIdInvalidDataException("El tipo de identificación no puede ser null");
        }

        if (typeId.getTypeId() == null || typeId.getTypeId().trim().isEmpty()) {
            throw new TypeIdInvalidDataException("El código del tipo de identificación no puede estar vacío");
        }

        if (typeId.getTypeIdname() == null || typeId.getTypeIdname().trim().isEmpty()) {
            throw new TypeIdInvalidDataException("El nombre del tipo de identificación no puede estar vacío");
        }

        if (typeId.getClassification() == null) {
            throw PersonClassificationInvalidException.forNullClassification();
        }

        // Normalizar valores para validaciones
        String normalizedTypeId = StringNormalizer.normalizeCode(typeId.getTypeId());
        String normalizedTypeIdName = StringNormalizer.normalizePreservingCase(typeId.getTypeIdname());

        // Verificar que el tipo de identificación existe por ID
        Optional<TypeIdEntity> existingEntity = typeIdRepository.findById(typeId.getId());
        if (existingEntity.isEmpty()) {
            throw new TypeIdNotFound("No se encontró el tipo de identificación con ID '" + typeId.getId() + "'");
        }

        TypeIdEntity currentEntity = existingEntity.get();

        // Validar que pertenece a la misma entidad
        if (!currentEntity.getTientId().equals(typeId.getEntId())) {
            throw new TypeIdInvalidDataException("El tipo de identificación no pertenece a la entidad especificada");
        }

        // Validar que no exista otro typeId con el mismo código (case-insensitive)
        // excluyendo el actual
        if (!currentEntity.getTiId().equals(normalizedTypeId) &&
                typeIdRepository.existsByTiIdAndTientId(normalizedTypeId, typeId.getEntId())) {
            throw new TypeIdAlreadyExists(
                    "Ya existe un tipo de identificación con el código '" + typeId.getTypeId() + "'");
        }

        // Validar que no exista otro typeIdname similar (case-insensitive) excluyendo
        // el actual
        if (!currentEntity.getTiName().equalsIgnoreCase(normalizedTypeIdName) &&
                typeIdRepository.existsByTiNameIgnoreCaseAndTientId(normalizedTypeIdName, typeId.getEntId())) {
            throw new TypeIdNameAlreadyExistsException(typeId.getTypeIdname());
        }

        // Crear el modelo normalizado para actualizar
        TypeId normalizedTypeIdModel = TypeId.builder()
                .id(currentEntity.getId())
                .typeId(normalizedTypeId)
                .typeIdname(normalizedTypeIdName)
                .entId(typeId.getEntId())
                .status(typeId.getStatus())
                .classification(typeId.getClassification())
                .build();

        TypeIdEntity typeIdEntity = idPersistenceMapper.toTypeIdEntity(normalizedTypeIdModel);

        // Preservar datos de auditoría
        typeIdEntity.setTenantId(currentEntity.getTenantId());

        typeIdRepository.save(typeIdEntity);
        return idPersistenceMapper.toTypeId(typeIdEntity);
    }

    /**
     * @brief Actualiza un tipo de tercero.     * 
     * @param thirdType Tipo de tercero a actualizar.
     * @return Tipo de tercero actualizado.
     */
    @Override
    public ThirdType updateThirdType(ThirdType thirdType) {
        if (thirdType == null) {
            throw new ThirdTypeInvalidDataException("El tipo de tercero no puede ser null");
        }

        if (thirdType.getThirdTypeId() == null) {
            throw new ThirdTypeInvalidDataException("El ID del tipo de tercero no puede ser null");
        }

        if (thirdType.getThirdTypeName() == null || thirdType.getThirdTypeName().trim().isEmpty()) {
            throw new ThirdTypeInvalidDataException("El nombre del tipo de tercero no puede estar vacío");
        }

        // Normalizar el nombre para validaciones
        String normalizedThirdTypeName = StringNormalizer.normalizePreservingCase(thirdType.getThirdTypeName());

        // Verificar que el tipo de tercero existe
        Optional<ThirdTypeEntity> existingEntity = thirdTypeRepository.findById(thirdType.getThirdTypeId());
        if (existingEntity.isEmpty()) {
            throw new ThirdTypeNotFound(
                    "No se encontró el tipo de tercero con ID '" + thirdType.getThirdTypeId() + "'");
        }

        ThirdTypeEntity currentEntity = existingEntity.get();

        // Validar que pertenece a la misma entidad
        if (!currentEntity.getTtentId().equals(thirdType.getEntId())) {
            throw new ThirdTypeInvalidDataException("El tipo de tercero no pertenece a la entidad especificada");
        }

        // Validar que no exista otro thirdTypeName similar (case-insensitive)
        // excluyendo el actual
        if (!currentEntity.getTtName().equalsIgnoreCase(normalizedThirdTypeName) &&
                thirdTypeRepository.existsByTtNameIgnoreCaseAndTtentId(normalizedThirdTypeName, thirdType.getEntId())) {
            throw new ThirdTypeNameAlreadyExistsException(thirdType.getThirdTypeName());
        }

        // Crear el modelo normalizado para actualizar
        ThirdType normalizedThirdTypeModel = ThirdType.builder()
                .thirdTypeId(thirdType.getThirdTypeId())
                .thirdTypeName(normalizedThirdTypeName)
                .entId(thirdType.getEntId())
                .status(thirdType.getStatus())
                .build();

        ThirdTypeEntity thirdTypeEntity = idPersistenceMapper.toThirdTypeEntity(normalizedThirdTypeModel);

        thirdTypeEntity.setTenantId(currentEntity.getTenantId());

        thirdTypeRepository.save(thirdTypeEntity);
        return idPersistenceMapper.toThirdType(thirdTypeEntity);
    }

    /**
     * @brief Verifica si existe un tipo de identificación por su ID.     * 
     * @param typeIdId El ID del tipo de identificación
     * @return true si existe, false en caso contrario
     */
    @Override
    public boolean existsTypeIdById(Long typeIdId) {
        if (typeIdId == null) {
            return false;
        }
        return typeIdRepository.existsById(typeIdId);
    }

    /**
     * @brief Verifica si existe un tipo de tercero por su ID.     * 
     * @param thirdTypeId El ID del tipo de tercero
     * @return true si existe, false en caso contrario
     */
    @Override
    public boolean existsThirdTypeById(Long thirdTypeId) {
        if (thirdTypeId == null) {
            return false;
        }
        return thirdTypeRepository.existsById(thirdTypeId);
    }

    /**
     * @brief Obtiene un tipo de identificación completo por su ID.     * 
     * @param typeIdId El ID del tipo de identificación
     * @return El tipo de identificación completo o null si no existe
     */
    @Override
    public TypeId getTypeIdById(Long typeIdId) {
        if (typeIdId == null) {
            return null;
        }

        return typeIdRepository.findById(typeIdId)
                .map(idPersistenceMapper::toTypeId)
                .orElse(null);
    }

    /**
     * @brief Obtiene un tipo de tercero completo por su ID.     * 
     * @param thirdTypeId El ID del tipo de tercero
     * @return El tipo de tercero completo o null si no existe
     */
    @Override
    public ThirdType getThirdTypeById(Long thirdTypeId) {
        if (thirdTypeId == null) {
            return null;
        }

        return thirdTypeRepository.findById(thirdTypeId)
                .map(idPersistenceMapper::toThirdType)
                .orElse(null);
    }

    /**
     * @brief Elimina un tipo de tercero del sistema.     * 
     * @param thirdTypeId El ID del tipo de tercero a eliminar
     * @param entId       El ID de la empresa
     * @return true si se eliminó correctamente, false en caso contrario
     */
    @Override
    public boolean deleteThirdType(Long thirdTypeId, String entId) {
        if (thirdTypeId == null || entId == null || entId.trim().isEmpty()) {
            return false;
        }

        String currentTenant = TenantContext.getTenantId();
        try {
            TenantContext.setTenantId(currentTenant);

            Optional<ThirdTypeEntity> thirdTypeEntity = thirdTypeRepository.findByTtIdAndTtentId(thirdTypeId, entId);

            if (thirdTypeEntity.isPresent()) {
                thirdTypeRepository.delete(thirdTypeEntity.get());
                return true;
            }

            return false;

        } catch (Exception e) {
            // Log del error pero no lanzar excepción para mantener el contrato del método
            return false;
        } finally {
            TenantContext.setTenantId(currentTenant);
        }
    }

    /**
     * @brief Verifica si un tipo de tercero está siendo utilizado por terceros existentes.     * 
     * @param thirdTypeId El ID del tipo de tercero
     * @param entId       El ID de la empresa
     * @return true si está en uso, false en caso contrario
     */
    @Override
    public boolean isThirdTypeInUse(Long thirdTypeId, String entId) {
        if (thirdTypeId == null || entId == null || entId.trim().isEmpty()) {
            return false;
        }

        String currentTenant = TenantContext.getTenantId();
        try {
            TenantContext.setTenantId(currentTenant);

            // Verificar si existe algún tercero que use este tipo de tercero
            return thirdsAndTypesRepository.existsByTtId(thirdTypeId);

        } finally {
            TenantContext.setTenantId(currentTenant);
        }
    }

    /**
     * @brief Elimina tipo de identificación con validación de uso
     * @details Verifica que el tipo de identificación no esté siendo usado por terceros existentes,
     * valida pertenencia a la empresa y elimina el registro con manejo transaccional.
     * @param typeIdId ID del tipo de identificación a eliminar
     * @param entId ID de la empresa para validación de pertenencia
     * @return true si se eliminó correctamente, false si hay errores o está en uso
     */
    @Override
    public boolean deleteTypeId(Long typeIdId, String entId) {
        if (typeIdId == null || entId == null || entId.trim().isEmpty()) {
            return false;
        }

        String currentTenant = TenantContext.getTenantId();
        try {
            TenantContext.setTenantId(currentTenant);

            Optional<TypeIdEntity> typeIdEntity = typeIdRepository.findById(typeIdId);

            if (typeIdEntity.isPresent() && entId.equals(typeIdEntity.get().getTientId())) {
                typeIdRepository.delete(typeIdEntity.get());
                return true;
            }

            return false;

        } catch (Exception e) {
            // Log del error pero no lanzar excepción para mantener el contrato del método
            return false;
        } finally {
            TenantContext.setTenantId(currentTenant);
        }
    }

    /**
     * @brief Verifica si tipo de identificación está siendo usado por terceros
     * @details Consulta la existencia de terceros que utilicen este tipo de identificación
     * dentro de la empresa especificada. Esencial para validaciones de eliminación.
     * @param typeIdId ID del tipo de identificación a verificar
     * @param entId ID de la empresa para el alcance de la verificación
     * @return true si hay terceros usando este tipo, false si está libre
     */
    @Override
    public boolean isTypeIdInUse(Long typeIdId, String entId) {
        if (typeIdId == null || entId == null || entId.trim().isEmpty()) {
            return false;
        }

        String currentTenant = TenantContext.getTenantId();
        try {
            TenantContext.setTenantId(currentTenant);

            // Primero obtener el código del tipo de identificación
            Optional<TypeIdEntity> typeIdEntity = typeIdRepository.findById(typeIdId);
            if (typeIdEntity.isEmpty()) {
                return false;
            }

            String typeIdCode = typeIdEntity.get().getTiId();

            // Verificar si existe algún tercero que use este tipo de identificación
            return thirdRepository.existsByTypeIdTiIdAndEntId(typeIdCode, entId);

        } finally {
            TenantContext.setTenantId(currentTenant);
        }
    }

    /**
     * @brief Obtiene todos los tipos de identificación con paginación y ordenamiento.     * 
     * @param entId El id de la empresa
     * @param page Número de página
     * @param size Tamaño de página
     * @param sortField Campo de ordenamiento
     * @param sortOrder Orden (asc/desc)
     * @return Página de tipos de identificación
     */
    @Override
    public Page<TypeId> getAllTypeIdsWithSort(String entId, int page, int size, String sortField, String sortOrder) {
        String entitySortField = mapTypeIdSortField(sortField);
        Sort sort = "desc".equalsIgnoreCase(sortOrder)
                ? Sort.by(entitySortField).descending()
                : Sort.by(entitySortField).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<TypeIdEntity> entityPage = typeIdRepository.findAllByTientIdPageable(entId, pageable);

        return entityPage.map(idPersistenceMapper::toTypeId);
    }

    /**
     * @brief Busca tipos de identificación por empresa y término de búsqueda.
     * 
     * @param entId El id de la empresa
     * @param search Término de búsqueda
     * @param page Número de página
     * @param size Tamaño de página
     * @param sortField Campo de ordenamiento
     * @param sortOrder Orden (asc/desc)
     * @return Página de tipos de identificación que coinciden
     */
    @Override
    public Page<TypeId> findByEntIdAndSearch(String entId, String search, int page, int size, String sortField,
            String sortOrder) {
        String entitySortField = mapTypeIdSortField(sortField);
        Sort sort = "desc".equalsIgnoreCase(sortOrder)
                ? Sort.by(entitySortField).descending()
                : Sort.by(entitySortField).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<TypeIdEntity> entityPage = typeIdRepository.findByTientIdAndSearch(entId, search, pageable);

        return entityPage.map(idPersistenceMapper::toTypeId);
    }

    /**
     * @brief Cuenta tipos de identificación por empresa.     * 
     * @param entId El id de la empresa
     * @return Cantidad de tipos de identificación
     */
    @Override
    public long countByEntId(String entId) {
        return typeIdRepository.countByTientId(entId);
    }

    /**
     * @brief Cuenta tipos de identificación por empresa y término de búsqueda.     * 
     * @param entId  El id de la empresa
     * @param search Término de búsqueda
     * @return Cantidad de tipos de identificación que coinciden
     */
    @Override
    public long countByEntIdAndSearch(String entId, String search) {
        return typeIdRepository.countByTientIdAndSearch(entId, search);
    }

    /**
     * @brief Obtiene todos los tipos de tercero con paginación y ordenamiento.     * 
     * @param entId     El id de la empresa
     * @param page      Número de página
     * @param size      Tamaño de página
     * @param sortField Campo de ordenamiento
     * @param sortOrder Orden (asc/desc)
     * @return Página de tipos de tercero
     */
    @Override
    public Page<ThirdType> getAllThirdTypesWithSort(String entId, int page, int size, String sortField,
            String sortOrder) {
        Sort sort = "desc".equalsIgnoreCase(sortOrder)
                ? Sort.by(sortField).descending()
                : Sort.by(sortField).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<ThirdTypeEntity> entityPage = thirdTypeRepository.findAllByTtentIdPageable(entId, pageable);

        return entityPage.map(idPersistenceMapper::toThirdType);
    }

    /**
     * @brief Busca tipos de tercero por empresa y término de búsqueda.     * 
     * @param entId     El id de la empresa
     * @param search    Término de búsqueda
     * @param page      Número de página
     * @param size      Tamaño de página
     * @param sortField Campo de ordenamiento
     * @param sortOrder Orden (asc/desc)
     * @return Página de tipos de tercero que coinciden
     */
    @Override
    public Page<ThirdType> findThirdTypesByEntIdAndSearch(String entId, String search, int page, int size,
            String sortField, String sortOrder) {
        Sort sort = "desc".equalsIgnoreCase(sortOrder)
                ? Sort.by(sortField).descending()
                : Sort.by(sortField).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<ThirdTypeEntity> entityPage = thirdTypeRepository.findByTtentIdAndSearch(entId, search, pageable);

        return entityPage.map(idPersistenceMapper::toThirdType);
    }

    /**
     * @brief Cuenta tipos de tercero por empresa.     * 
     * @param entId El id de la empresa
     * @return Cantidad de tipos de tercero
     */
    @Override
    public long countThirdTypesByEntId(String entId) {
        return thirdTypeRepository.countByTtentId(entId);
    }

    /**
     * @brief Cuenta tipos de tercero por empresa y término de búsqueda.     * 
     * @param entId  El id de la empresa
     * @param search Término de búsqueda
     * @return Cantidad de tipos de tercero que coinciden
     */
    @Override
    public long countThirdTypesByEntIdAndSearch(String entId, String search) {
        return thirdTypeRepository.countByTtentIdAndSearch(entId, search);
    }

    /**
     * @brief Obtiene todos los tipos de identificación activos con paginación simple
     * (ordenado por tiName asc).     * 
     * @param entId El id de la empresa
     * @param page  Número de página
     * @param size  Tamaño de página
     * @return Página de tipos de identificación activos ordenados por nombre
     */
    @Override
    public Page<TypeId> getAllActiveTypeIds(String entId, int page, int size) {
        Sort sort = Sort.by("tiName").ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<TypeIdEntity> entityPage = typeIdRepository.findActiveByTientIdPageable(entId, pageable);

        return entityPage.map(idPersistenceMapper::toTypeId);
    }

    /**
     * @brief Cuenta tipos de identificación activos por empresa.     * 
     * @param entId El id de la empresa
     * @return Cantidad de tipos de identificación activos
     */
    @Override
    public long countActiveByEntId(String entId) {
        return typeIdRepository.countActiveByTientId(entId);
    }

    /**
     * @brief Obtiene todos los tipos de tercero activos con paginación simple (ordenado por ttName asc).
     * @param entId El id de la empresa
     * @param page Número de página
     * @param size Tamaño de página
     * @return Página de tipos de tercero activos ordenados por nombre
     */
    @Override
    public Page<ThirdType> getAllActiveThirdTypes(String entId, int page, int size) {
        Sort sort = Sort.by("ttName").ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<ThirdTypeEntity> entityPage = thirdTypeRepository.findActiveByTtentIdPageable(entId, pageable);

        return entityPage.map(idPersistenceMapper::toThirdType);
    }

    /**
     * @brief Cuenta tipos de tercero activos por empresa.     * 
     * @param entId El id de la empresa
     * @return Cantidad de tipos de tercero activos
     */
    @Override
    public long countActiveThirdTypesByEntId(String entId) {
        return thirdTypeRepository.countActiveByTtentId(entId);
    }

    /**
     * @brief Mapea campos de ordenamiento del dominio TypeId a entidad JPA
     * @details Traduce nombres de campos del modelo TypeId a campos correspondientes
     * en TypeIdEntity para consultas de ordenamiento. Solo permite campos seguros.
     * @param sortField nombre del campo en el modelo de dominio
     * @return nombre del campo correspondiente en la entidad JPA
     */
    private String mapTypeIdSortField(String sortField) {
        if (sortField == null || sortField.trim().isEmpty()) {
            return "tiName"; // Default
        }
        switch (sortField.toLowerCase()) {
            case "typeid":
                return "tiId";
            case "typeidname":
                return "tiName";
            case "status":
                return "status";
            default:
                return "tiName"; // Default
        }
    }

    /**
     * @brief Verifica si tipo de identificación tiene terceros con movimientos contables
     * @details Verifica si hay terceros asociados a este tipo de identificación que tengan
     * movimientos contables (usageCount > 0), lo que impediría su edición.
     * @param typeIdId El ID del tipo de identificación
     * @param entId El ID de la empresa
     * @return true si tiene terceros con movimientos contables, false en caso contrario
     */
    @Override
    public boolean hasTypeIdThirdsWithMovements(Long typeIdId, String entId) {
        if (typeIdId == null || entId == null || entId.trim().isEmpty()) {
            return false;
        }

        String currentTenant = TenantContext.getTenantId();
        try {
            TenantContext.setTenantId(currentTenant);

            // Primero obtener el código del tipo de identificación
            Optional<TypeIdEntity> typeIdEntity = typeIdRepository.findById(typeIdId);
            if (typeIdEntity.isEmpty()) {
                return false;
            }

            String typeIdCode = typeIdEntity.get().getTiId();

            // Verificar si existe algún tercero con movimientos contables que use este tipo de identificación
            return thirdRepository.existsByTypeIdTiIdAndEntIdAndUsageCountGreaterThan(typeIdCode, entId, 0);

        } finally {
            TenantContext.setTenantId(currentTenant);
        }
    }

    /**
     * @brief Verifica si tipo de tercero tiene terceros con movimientos contables
     * @details Verifica si hay terceros asociados a este tipo de tercero que tengan
     * movimientos contables (usageCount > 0), lo que impediría su edición.
     * @param thirdTypeId El ID del tipo de tercero
     * @param entId El ID de la empresa
     * @return true si tiene terceros con movimientos contables, false en caso contrario
     */
    @Override
    public boolean hasThirdTypeThirdsWithMovements(Long thirdTypeId, String entId) {
        if (thirdTypeId == null || entId == null || entId.trim().isEmpty()) {
            return false;
        }

        String currentTenant = TenantContext.getTenantId();
        try {
            TenantContext.setTenantId(currentTenant);

            // Verificar si existe algún tercero con movimientos contables asociado a este tipo de tercero
            // Usar consulta personalizada en ThirdRepository o lógica en el servicio
            return thirdRepository.existsByThirdTypeIdAndEntIdAndUsageCountGreaterThan(thirdTypeId, entId, 0);

        } finally {
            TenantContext.setTenantId(currentTenant);
        }
    }
}
