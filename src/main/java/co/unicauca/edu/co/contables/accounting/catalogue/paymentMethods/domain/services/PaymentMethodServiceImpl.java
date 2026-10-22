package co.unicauca.edu.co.contables.accounting.catalogue.paymentMethods.domain.services;

import co.unicauca.edu.co.contables.accounting.catalogue.commons.exceptions.paymentMethods.PaymentMethodsAlreadyExistsException;
import co.unicauca.edu.co.contables.accounting.catalogue.commons.exceptions.paymentMethods.PaymentMethodsNotFoundException;
import co.unicauca.edu.co.contables.commons.audit.annotation.Auditable;
import co.unicauca.edu.co.contables.commons.audit.annotation.OperationType;
import co.unicauca.edu.co.contables.accounting.catalogue.commons.exceptions.paymentMethods.InvalidAccountingAccountException;
import co.unicauca.edu.co.contables.accounting.catalogue.commons.exceptions.paymentMethods.PaymentMethodInUseException;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.application.services.validation.AccountCatalogueValidationService;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.domain.models.AccountCatalogue;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.infraestructure.adapters.output.jpaAdapter.entity.AccountCatalogueEntity;

import co.unicauca.edu.co.contables.accounting.catalogue.paymentMethods.dataAccess.entity.PaymentMethodEntity;
import co.unicauca.edu.co.contables.accounting.catalogue.paymentMethods.dataAccess.mapper.PaymentMethodDataMapper;
import co.unicauca.edu.co.contables.accounting.catalogue.paymentMethods.dataAccess.repository.PaymentMethodRepository;
import co.unicauca.edu.co.contables.accounting.catalogue.paymentMethods.domain.mapper.PaymentMethodDomainMapper;
import co.unicauca.edu.co.contables.accounting.catalogue.paymentMethods.domain.model.PaymentMethod;
import co.unicauca.edu.co.contables.accounting.catalogue.paymentMethods.presentation.DTO.request.PaymentMethodCreateReq;
import co.unicauca.edu.co.contables.accounting.catalogue.paymentMethods.presentation.DTO.request.PaymentMethodUpdateReq;
import co.unicauca.edu.co.contables.commons.utils.PaginationHelper;
import co.unicauca.edu.co.contables.commons.utils.StringStandardizationUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;

/**
 * @brief Implementación de servicios para gestión de métodos de pago
 *
 *        Proporciona operaciones CRUD completas con validaciones de negocio,
 *        filtros avanzados y manejo de relaciones con cuentas contables.
 */
@Service
@RequiredArgsConstructor
public class PaymentMethodServiceImpl implements IPaymentMethodService {

    private final PaymentMethodRepository repository;
    private final PaymentMethodDataMapper dataMapper;
    private final PaymentMethodDomainMapper domainMapper;
    private final AccountCatalogueValidationService accountCatalogueValidationService;
    private final PaginationHelper paginationHelper;

    @Override
    @Transactional
    @Auditable(operationType = OperationType.CREATE, affectedTable = "PAYMENT_METHOD", moduleName = "PAYMENT_METHODS")
    public PaymentMethod create(PaymentMethodCreateReq request) {
        // Implementa validaciones de unicidad por nombre normalizado y cuenta auxiliar
        String normalizedNameForValidation = StringStandardizationUtils.standardizeName(request.getName());

        // Validar que la cuenta contable existe por ID y empresa
        AccountCatalogue account = accountCatalogueValidationService.validateAccountExistsByIdAndEnterprise(
                request.getAccountingAccountId(), request.getIdEnterprise());

        // Validar que la cuenta contable es auxiliar
        validateAccountingAccount(account.getCode(), request.getIdEnterprise());

        // Validar unicidad por empresa comparando nombres normalizados
        List<PaymentMethodEntity> existingMethods = repository
                .findAllByIdEnterprise(request.getIdEnterprise(), Pageable.unpaged()).getContent();
        boolean nameExists = existingMethods.stream()
                .anyMatch(method -> StringStandardizationUtils.standardizeName(method.getName())
                        .equals(normalizedNameForValidation));
        if (nameExists) {
            throw new PaymentMethodsAlreadyExistsException("nombre", normalizedNameForValidation,
                    request.getIdEnterprise());
        }

        PaymentMethod domain = domainMapper.toDomain(request);
        // Guardar el nombre tal como lo ingresó el usuario (sin normalizar)

        // Crear el dominio con la entidad de cuenta
        domain.setAccountingAccountEntity(account);

        // Crear la entidad con la relación usando la entidad completa
        PaymentMethodEntity toSave = dataMapper.toEntity(domain);
        // Crear la entidad AccountCatalogueEntity completa con todos los campos
        AccountCatalogueEntity accountEntity = new AccountCatalogueEntity();
        accountEntity.setId(account.getId());
        accountEntity.setCode(account.getCode());
        accountEntity.setDescription(account.getDescription());
        toSave.setAccountingAccount(accountEntity);

        PaymentMethodEntity saved = repository.save(toSave);
        return dataMapper.toDomain(saved);
    }

    @Override
    @Transactional
    @Auditable(operationType = OperationType.UPDATE, affectedTable = "PAYMENT_METHOD", moduleName = "PAYMENT_METHODS")
    public PaymentMethod update(PaymentMethodUpdateReq request) {
        // Implementa validaciones de unicidad excluyendo el registro actual
        PaymentMethodEntity current = repository.findByIdAndIdEnterprise(request.getId(), request.getIdEnterprise())
                .orElseThrow(PaymentMethodsNotFoundException::new);

        // Validar que el método de pago no tenga movimientos contables registrados
        PaymentMethod domain = dataMapper.toDomain(current);
        if (domain.isInUse()) {
            throw new PaymentMethodInUseException(current.getName(), true); // true indica operación de edición
        }

        // Validar que la nueva cuenta contable existe y es auxiliar (si se está
        // cambiando)
        AccountCatalogue newAccount = accountCatalogueValidationService.validateAccountExistsByIdAndEnterprise(
                request.getAccountingAccountId(), request.getIdEnterprise());
        validateAccountingAccount(newAccount.getCode(), request.getIdEnterprise());

        // Normalizar nombre solo para validación de unicidad
        String normalizedNameForValidation = StringStandardizationUtils.standardizeName(request.getName());

        String currentNormalizedName = StringStandardizationUtils.standardizeName(current.getName());
        if (!normalizedNameForValidation.equals(currentNormalizedName)) {
            List<PaymentMethodEntity> existingMethods = repository
                    .findAllByIdEnterprise(request.getIdEnterprise(), Pageable.unpaged()).getContent();
            boolean nameExists = existingMethods.stream()
                    .filter(method -> !method.getId().equals(current.getId())) // Excluir el registro actual
                    .anyMatch(method -> StringStandardizationUtils.standardizeName(method.getName())
                            .equals(normalizedNameForValidation));
            if (nameExists) {
                throw new PaymentMethodsAlreadyExistsException("nombre", normalizedNameForValidation,
                        request.getIdEnterprise());
            }
        }

        // Guardar el nombre tal como lo ingresó el usuario (sin normalizar)
        current.setName(request.getName());

        // Actualizar la cuenta contable
        AccountCatalogueEntity accountEntity = new AccountCatalogueEntity();
        accountEntity.setId(newAccount.getId());
        accountEntity.setCode(newAccount.getCode());
        accountEntity.setDescription(newAccount.getDescription());
        current.setAccountingAccount(accountEntity);

        PaymentMethodEntity saved = repository.save(current);
        return dataMapper.toDomain(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentMethod findById(Long id, String idEnterprise) {
        // Implementa búsqueda con carga de relaciones usando EntityGraph
        return dataMapper.toDomain(repository.findByIdAndIdEnterprise(id, idEnterprise)
                .orElseThrow(PaymentMethodsNotFoundException::new));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PaymentMethod> findAllByEnterprise(String idEnterprise, Optional<Integer> page, Optional<Integer> size,
            String sortField, String sortOrder, String search) {
        // Implementa búsqueda con JOIN en relaciones contables usando query nativo
        long totalRecords;
        if (StringUtils.hasText(search)) {
            totalRecords = repository.countByIdEnterpriseAndSearch(idEnterprise, search.trim());
        } else {
            totalRecords = repository.countByIdEnterprise(idEnterprise);
        }

        Sort sort = Sort.by(sortOrder.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC, sortField);

        Pageable pageable = paginationHelper.createFlexiblePageable(page, size, totalRecords);
        pageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);

        // Ejecutar consulta con filtros
        Page<PaymentMethodEntity> result;
        if (StringUtils.hasText(search)) {
            // Búsqueda por nombre o cuenta contable
            result = repository.findByIdEnterpriseAndSearch(idEnterprise, search.trim(), pageable);
        } else {
            // Sin búsqueda
            result = repository.findAllByIdEnterprise(idEnterprise, pageable);
        }

        return result.map(dataMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PaymentMethod> findAllActiveByEnterprise(String idEnterprise, Optional<Integer> page,
            Optional<Integer> size) {
        // Implementa consulta solo de registros activos con ordenamiento fijo

        long totalRecords = repository.countByIdEnterpriseAndStatus(idEnterprise, true);

        Sort sort = Sort.by(Sort.Direction.ASC, "name");

        Pageable pageable = paginationHelper.createFlexiblePageable(page, size, totalRecords);
        pageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);

        return repository.findAllByIdEnterpriseAndStatus(idEnterprise, true, pageable)
                .map(dataMapper::toDomain);
    }

    @Override
    @Transactional
    @Auditable(operationType = OperationType.INACTIVATE, affectedTable = "PAYMENT_METHOD", moduleName = "PAYMENT_METHODS", idArgIndex = 0, enterpriseIdArgIndex = 1)
    public PaymentMethod changeState(Long id, String idEnterprise, Boolean newState) {
        // Implementa cambio de estado sin validaciones adicionales
        PaymentMethodEntity current = repository.findByIdAndIdEnterprise(id, idEnterprise)
                .orElseThrow(PaymentMethodsNotFoundException::new);

        current.setStatus(newState);
        PaymentMethodEntity saved = repository.save(current);
        return dataMapper.toDomain(saved);
    }

    @Override
    @Transactional
    @Auditable(operationType = OperationType.DELETE, affectedTable = "PAYMENT_METHOD", moduleName = "PAYMENT_METHODS", idArgIndex = 0, enterpriseIdArgIndex = 1)
    public PaymentMethod delete(Long id, String idEnterprise) {
        // Implementa eliminación sin validaciones de relaciones
        PaymentMethodEntity current = repository.findByIdAndIdEnterprise(id, idEnterprise)
                .orElseThrow(PaymentMethodsNotFoundException::new);

        // Validar que el método de pago no tenga movimientos contables registrados
        PaymentMethod domain = dataMapper.toDomain(current);
        if (domain.isInUse()) {
            throw new PaymentMethodInUseException(current.getName(), false); // false indica operación de eliminación
        }

        repository.delete(current);
        return domain;
    }

    /**
     * @brief Valida existencia y formato de cuenta contable auxiliar
     * @param accountingAccount el código de la cuenta contable
     * @param idEnterprise      el ID de la empresa
     */
    private void validateAccountingAccount(String accountingAccount, String idEnterprise) {
        if (accountingAccount == null || accountingAccount.trim().isEmpty()) {
            throw new InvalidAccountingAccountException("La cuenta contable no puede estar vacía");
        }

        String trimmedAccount = accountingAccount.trim();

        // Validar que la cuenta tenga exactamente 8 dígitos (cuenta auxiliar)
        if (!trimmedAccount.matches("^\\d{8}$")) {
            throw new InvalidAccountingAccountException(
                    "La cuenta contable debe ser una cuenta auxiliar de exactamente 8 dígitos.");
        }

        // Validar que la cuenta existe en el catálogo
        try {
            AccountCatalogue account = accountCatalogueValidationService.validateAccountExists(trimmedAccount,
                    idEnterprise);
            if (account == null) {
                throw new InvalidAccountingAccountException(
                        "La cuenta contable '" + trimmedAccount
                                + "' no existe en el catálogo de cuentas para la empresa '" + idEnterprise + "'");
            }
        } catch (Exception e) {
            if (e instanceof InvalidAccountingAccountException) {
                throw e;
            }
            throw new InvalidAccountingAccountException(
                    "La cuenta contable '" + trimmedAccount + "' no existe en el catálogo de cuentas para la empresa '"
                            + idEnterprise + "'");
        }
    }

    @Override
    @Transactional
    public void updateUsageCount(Long id, Integer usageCount) {
        PaymentMethodEntity entity = repository.findById(id)
                .orElseThrow(() -> new PaymentMethodsNotFoundException("Método de pago no encontrado con ID: " + id));

        entity.setUsageCount(usageCount);
        repository.save(entity);
    }
}
