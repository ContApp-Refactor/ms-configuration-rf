package co.unicauca.edu.co.contables.accounting.catalogue.banks.domain.services;

import co.unicauca.edu.co.contables.commons.audit.annotation.Auditable;
import co.unicauca.edu.co.contables.commons.audit.annotation.OperationType;
import co.unicauca.edu.co.contables.accounting.catalogue.commons.exceptions.banks.BankAlreadyExistsException;
import co.unicauca.edu.co.contables.accounting.catalogue.commons.exceptions.banks.BankHasAssociatedAccountsException;
import co.unicauca.edu.co.contables.accounting.catalogue.commons.exceptions.banks.BankInUseException;
import co.unicauca.edu.co.contables.accounting.catalogue.commons.exceptions.banks.BankNotFoundException;
import co.unicauca.edu.co.contables.accounting.catalogue.commons.exceptions.banks.InvalidBankCodeException;
import co.unicauca.edu.co.contables.commons.utils.PaginationHelper;
import co.unicauca.edu.co.contables.accounting.catalogue.banks.dataAccess.entity.BankEntity;
import co.unicauca.edu.co.contables.accounting.catalogue.banks.dataAccess.mapper.BankDataMapper;
import co.unicauca.edu.co.contables.accounting.catalogue.banks.dataAccess.repository.BankRepository;
import co.unicauca.edu.co.contables.accounting.catalogue.bankAccounts.dataAccess.repository.BankAccountRepository;
import co.unicauca.edu.co.contables.accounting.catalogue.banks.domain.mapper.BankDomainMapper;
import co.unicauca.edu.co.contables.accounting.catalogue.banks.domain.model.Bank;
import co.unicauca.edu.co.contables.accounting.catalogue.banks.presentation.DTO.request.BankCreateReq;
import co.unicauca.edu.co.contables.accounting.catalogue.banks.presentation.DTO.request.BankUpdateReq;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.util.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.Optional;

/**
 * @brief Implementación de servicios para gestión de bancos
 *
 *        Proporciona operaciones CRUD completas con validaciones de negocio,
 *        filtros avanzados, búsqueda y estandarización de datos.
 */
@Service
@RequiredArgsConstructor
public class BankServiceImpl implements IBankService {

    private final BankRepository repository;
    private final BankDataMapper dataMapper;
    private final BankDomainMapper domainMapper;
    private final BankAccountRepository bankAccountRepository;
    private final PaginationHelper paginationHelper;

    @Override
    @Transactional
    @Auditable(operationType = OperationType.CREATE, affectedTable = "BANK", moduleName = "BANKS")
    public Bank create(BankCreateReq request) {
        // Validar formato del código
        validateBankCode(request.getCode());

        // Estandarización del nombre a mayúsculas
        String standardizedName = standardizeName(request.getName());

        // Validar unicidad del código por empresa
        if (repository.existsByCodeAndIdEnterprise(request.getCode(), request.getIdEnterprise())) {
            throw new BankAlreadyExistsException("código", request.getCode(), request.getIdEnterprise());
        }

        // Validar unicidad del nombre por empresa
        if (repository.existsByNameAndIdEnterprise(standardizedName, request.getIdEnterprise())) {
            throw new BankAlreadyExistsException("nombre", standardizedName, request.getIdEnterprise());
        }

        Bank domain = domainMapper.toDomain(request);
        domain.setName(standardizedName);
        BankEntity toSave = dataMapper.toEntity(domain);

        BankEntity saved = repository.save(toSave);
        return dataMapper.toDomain(saved);
    }

    @Override
    @Transactional
    @Auditable(operationType = OperationType.UPDATE, affectedTable = "BANK", moduleName = "BANKS")
    public Bank update(BankUpdateReq request) {
        BankEntity current = repository.findByIdAndIdEnterprise(request.getId(), request.getIdEnterprise())
                .orElseThrow(BankNotFoundException::new);

        // Validar que el banco no tenga cuentas bancarias con movimientos registrados
        boolean hasAccountsWithMovements = bankAccountRepository
                .existsByBankIdAndIdEnterpriseAndUsageCountGreaterThanZero(current.getId(), request.getIdEnterprise());
        if (hasAccountsWithMovements) {
            throw new BankInUseException(current.getName(), true); // true indica operación de edición
        }

        // Validar formato del código
        validateBankCode(request.getCode());

        // Validar unicidad del código si cambió
        if (!current.getCode().equals(request.getCode()) &&
                repository.existsByCodeAndIdEnterprise(request.getCode(), request.getIdEnterprise())) {
            throw new BankAlreadyExistsException("código", request.getCode(), request.getIdEnterprise());
        }

        String standardizedName = standardizeName(request.getName());

        // Validar unicidad del nombre si cambió (solo entre registros no eliminados)
        if (!standardizedName.equals(current.getName()) &&
                repository.existsByNameAndIdEnterpriseAndIdNot(standardizedName, request.getIdEnterprise(),
                        current.getId())) {
            throw new BankAlreadyExistsException("nombre", standardizedName, request.getIdEnterprise());
        }

        current.setCode(request.getCode());
        current.setName(standardizedName);
        current.setCurrencies(request.getCurrencies());
        current.setStatus(request.getStatus());

        BankEntity saved = repository.save(current);
        return dataMapper.toDomain(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Bank findById(Long id, String idEnterprise) {
        return dataMapper.toDomain(repository.findByIdAndIdEnterprise(id, idEnterprise)
                .orElseThrow(BankNotFoundException::new));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Bank> findAllByEnterpriseWithFilters(String idEnterprise, Integer page, Integer size,
            String sortField, String sortOrder, String search) {
        // Validar sortField - solo permitir "code" y "name"
        if (sortField != null && !sortField.isEmpty()) {
            if (!"code".equals(sortField) && !"name".equals(sortField)) {
                throw new IllegalArgumentException("El campo de ordenamiento debe ser 'code' o 'name'");
            }
        } else {
            sortField = "name";
        }

        Sort.Direction direction = Sort.Direction.ASC;
        if (sortOrder != null && !sortOrder.isEmpty()) {
            if ("desc".equalsIgnoreCase(sortOrder)) {
                direction = Sort.Direction.DESC;
            } else if (!"asc".equalsIgnoreCase(sortOrder)) {
                throw new IllegalArgumentException("El orden debe ser 'asc' o 'desc'");
            }
        }

        // Determinar el número total de registros (considerando búsqueda si existe)
        long totalRecords;
        if (StringUtils.hasText(search)) {
            totalRecords = repository.countByIdEnterpriseAndSearch(idEnterprise, search.trim());
        } else {
            totalRecords = repository.countByIdEnterprise(idEnterprise);
        }

        // Crear paginación
        Pageable pageable = paginationHelper.createFlexiblePageable(
                Optional.ofNullable(page),
                Optional.ofNullable(size),
                totalRecords);

        // Aplicar ordenamiento
        Pageable pageableWithSort = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                Sort.by(direction, sortField));

        Page<BankEntity> result;
        if (StringUtils.hasText(search)) {
            result = repository.findByIdEnterpriseAndSearch(idEnterprise, search.trim(), pageableWithSort);
        } else {
            result = repository.findAllByIdEnterprise(idEnterprise, pageableWithSort);
        }

        return result.map(dataMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Bank> findAllActiveByEnterprise(String idEnterprise, Integer page, Integer size) {

        long totalRecords = repository.countByIdEnterpriseAndStatus(idEnterprise, true);

        Pageable pageable = paginationHelper.createFlexiblePageable(
                Optional.ofNullable(page),
                Optional.ofNullable(size),
                totalRecords);

        // Crear nuevo Pageable con ordenamiento por nombre
        Pageable pageableWithSort = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                Sort.by(Sort.Direction.ASC, "name"));

        return repository.findAllByIdEnterpriseAndStatus(idEnterprise, true, pageableWithSort)
                .map(dataMapper::toDomain);
    }

    @Override
    @Transactional
    @Auditable(operationType = OperationType.INACTIVATE, affectedTable = "BANK", moduleName = "BANKS", idArgIndex = 0, enterpriseIdArgIndex = 1)
    public Bank changeState(Long id, String idEnterprise, Boolean newState) {
        BankEntity current = repository.findByIdAndIdEnterprise(id, idEnterprise)
                .orElseThrow(BankNotFoundException::new);

        current.setStatus(newState);
        BankEntity saved = repository.save(current);
        return dataMapper.toDomain(saved);
    }

    @Override
    @Transactional
    @Auditable(operationType = OperationType.DELETE, affectedTable = "BANK", moduleName = "BANKS", idArgIndex = 0, enterpriseIdArgIndex = 1)
    public Bank delete(Long id, String idEnterprise) {
        BankEntity current = repository.findByIdAndIdEnterprise(id, idEnterprise)
                .orElseThrow(BankNotFoundException::new);

        // Verificar si el banco tiene cuentas bancarias asociadas (sin importar
        // movimientos)
        boolean hasAssociatedAccounts = bankAccountRepository
                .findAllByIdEnterpriseAndBankId(idEnterprise, id, PageRequest.of(0, 1)).hasContent();
        if (hasAssociatedAccounts) {
            throw new BankHasAssociatedAccountsException(current.getName());
        }

        Bank domain = dataMapper.toDomain(current);
        repository.delete(current);
        return domain;
    }

    /**
     * @brief Estandariza el nombre del banco a mayúsculas.
     *
     * @param input el nombre original del banco
     * @return el nombre estandarizado en mayúsculas, o null si input es null
     */
    private String standardizeName(String input) {
        if (input == null)
            return null;
        return input.trim().toUpperCase(new Locale("es", "ES"));
    }

    /**
     * @brief Valida el formato del código bancario.
     *
     * @param codigo el código del banco a validar
     */
    private void validateBankCode(String codigo) {
        if (codigo == null || codigo.trim().isEmpty()) {
            throw new InvalidBankCodeException("El código del banco no puede estar vacío");
        }

        String trimmedCodigo = codigo.trim();

        if (!trimmedCodigo.matches("^\\d{2}$")) {
            throw new InvalidBankCodeException(
                    "El código debe ser exactamente 2 dígitos (01-99). Código proporcionado: '" + trimmedCodigo + "'");
        }

        int codigoInt = Integer.parseInt(trimmedCodigo);
        if (codigoInt < 1 || codigoInt > 99) {
            throw new InvalidBankCodeException(
                    "El código debe estar entre 01 y 99. Código proporcionado: '" + trimmedCodigo + "'");
        }
    }
}
