package co.unicauca.edu.co.contables.configuration.costCenters.domain.services;

import co.unicauca.edu.co.contables.configuration.commons.exceptions.costCenters.CostCentersAlreadyExistsException;
import co.unicauca.edu.co.contables.configuration.commons.exceptions.costCenters.CostCentersNotFoundException;
import co.unicauca.edu.co.contables.configuration.commons.audit.annotation.Auditable;
import co.unicauca.edu.co.contables.configuration.commons.audit.annotation.OperationType;
import co.unicauca.edu.co.contables.configuration.commons.exceptions.costCenters.CostCenterHasChildrenException;
import co.unicauca.edu.co.contables.configuration.commons.exceptions.costCenters.CostCenterInUseException;
import co.unicauca.edu.co.contables.configuration.commons.exceptions.costCenters.CostCenterInvalidCodePrefixException;
import co.unicauca.edu.co.contables.configuration.commons.utils.StringStandardizationUtils;
import co.unicauca.edu.co.contables.configuration.costCenters.dataAccess.entity.CostCenterEntity;
import co.unicauca.edu.co.contables.configuration.costCenters.dataAccess.mapper.CostCenterDataMapper;
import co.unicauca.edu.co.contables.configuration.costCenters.dataAccess.repository.CostCenterRepository;
import co.unicauca.edu.co.contables.configuration.costCenters.domain.mapper.CostCenterDomainMapper;
import co.unicauca.edu.co.contables.configuration.costCenters.domain.models.CostCenter;
import co.unicauca.edu.co.contables.configuration.costCenters.presentation.DTO.request.CostCenterCreateReq;
import co.unicauca.edu.co.contables.configuration.costCenters.presentation.DTO.request.CostCenterUpdateReq;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.ArrayList;
import org.springframework.data.domain.PageImpl;

/**
 * @brief Implementación del servicio de centros de costo
 *
 *        Implementación principal del servicio de centros de costo que maneja
 *        operaciones CRUD, jerarquía, validaciones y lógica de negocio.
 */
@Service
@RequiredArgsConstructor
public class CostCenterServiceImpl implements ICostCenterService {

	private final CostCenterRepository repository;
	private final CostCenterDataMapper dataMapper;
	private final CostCenterDomainMapper domainMapper;

	@Override
	@Transactional
	@Auditable(operationType = OperationType.CREATE, affectedTable = "COST_CENTER", moduleName = "COST_CENTERS")
	public CostCenter create(CostCenterCreateReq request) {
		// Validación de unicidad por código y nombre dentro de la empresa
		if (repository.existsByCodeAndIdEnterprise(request.getCode(), request.getIdEnterprise())) {
			throw new CostCentersAlreadyExistsException(request.getCode(), request.getIdEnterprise());
		}
		// Estandarizar nombre: primera letra mayúscula, resto minúsculas, colapsar
		// espacios
		String standardizedName = StringStandardizationUtils.standardizeName(request.getName());
		request.setName(standardizedName);

		// Validación de nombre exacto (tras estandarización)
		if (repository.existsByNameAndIdEnterprise(standardizedName, request.getIdEnterprise())) {
			throw new CostCentersAlreadyExistsException(request.getName(), request.getIdEnterprise(), true);
		}

		CostCenter costCenter = domainMapper.toDomain(request);
		CostCenterEntity entity = dataMapper.toEntity(costCenter);
		if (request.getParentId() != null) {
			CostCenterEntity parent = repository
					.findByIdAndIdEnterprise(request.getParentId(), request.getIdEnterprise())
					.orElseThrow(CostCentersNotFoundException::new);
			entity.setParent(parent);

			// Activar automáticamente todas las cuentas padre si están inactivas
			activateParentHierarchy(parent);
		}

		CostCenterEntity saved = repository.save(entity);
		return dataMapper.toDomain(saved);
	}

	@Override
	@Transactional
	@Auditable(operationType = OperationType.UPDATE, affectedTable = "COST_CENTER", moduleName = "COST_CENTERS")
	public CostCenter update(CostCenterUpdateReq request) {
		CostCenterEntity current = repository.findByIdAndIdEnterprise(request.getId(), request.getIdEnterprise())
				.orElseThrow(CostCentersNotFoundException::new);

		// Validar que el centro de costo no tenga movimientos contables asociados
		if (current.getUsageCount() != null && current.getUsageCount() > 0) {
			throw new CostCenterInUseException(current.getCode(), true); // true indica operación de edición
		}

		// Estandarizar nombre antes de validar
		String standardizedName = StringStandardizationUtils.standardizeName(request.getName());
		request.setName(standardizedName);

		// Si cambian code o name, validar que no exista otro con esos datos en la misma
		// empresa
		boolean codeChanged = request.getCode() != null && !request.getCode().equals(current.getCode());
		boolean nameChanged = request.getName() != null && !request.getName().equals(current.getName());
		boolean enterpriseChanged = request.getIdEnterprise() != null
				&& !request.getIdEnterprise().equals(current.getIdEnterprise());

		String targetEnterprise = enterpriseChanged ? request.getIdEnterprise() : current.getIdEnterprise();

		// Validar que si tiene padre, el nuevo código mantenga el prefijo del código
		// del padre
		if (codeChanged && current.getParent() != null) {
			String parentCode = current.getParent().getCode();
			if (!request.getCode().startsWith(parentCode)) {
				throw new CostCenterInvalidCodePrefixException(parentCode, request.getCode());
			}
		}

		if (codeChanged || enterpriseChanged) {
			boolean existsCode = repository.existsByCodeAndIdEnterprise(request.getCode(), targetEnterprise);
			if (existsCode) {
				throw new CostCentersAlreadyExistsException(request.getCode(), targetEnterprise);
			}
		}
		if (nameChanged || enterpriseChanged) {
			boolean existsName = repository.existsByNameAndIdEnterpriseAndIdNot(request.getName(), targetEnterprise,
					current.getId());
			if (existsName) {
				throw new CostCentersAlreadyExistsException(request.getName(), targetEnterprise, true);
			}
		}

		// Si el código cambió, actualizar códigos de hijos en cascada
		String oldCode = current.getCode();
		String newCode = request.getCode();

		current.setIdEnterprise(request.getIdEnterprise());
		current.setCode(newCode);
		current.setName(request.getName());
		if (request.getParentId() != null) {
			CostCenterEntity parent = repository.findByIdAndIdEnterprise(request.getParentId(), targetEnterprise)
					.orElseThrow(CostCentersNotFoundException::new);
			current.setParent(parent);
		} else {
			current.setParent(null);
		}

		CostCenterEntity saved = repository.save(current);

		// Actualizar códigos de hijos si el código cambió
		if (codeChanged) {
			updateChildrenCodes(current.getId(), oldCode, newCode);
		}

		return dataMapper.toDomain(saved);
	}

	@Override
	@Transactional(readOnly = true)
	public Page<CostCenter> findAllByEnterpriseAndStatus(String idEnterprise, Boolean status, int page, int size) {
		Pageable pageable = PageRequest.of(page, size);
		return repository.findAllByIdEnterpriseAndStatus(idEnterprise, status, pageable)
				.map(dataMapper::toDomain);
	}

	@Override
	@Transactional(readOnly = true)
	public Page<CostCenter> findAllByEnterpriseHierarchical(String idEnterprise, int page, int size) {
		// Obtener solo los centros de costo raíz (padres)
		List<CostCenterEntity> rootCostCenters = repository.findByIdEnterpriseAndParentIsNullOrderByCode(idEnterprise);

		// Calcular el total de elementos una sola vez
		long totalElements = countAllNodesInRoots(rootCostCenters);

		// Calcular paginación sobre las familias raíz
		int totalRoots = rootCostCenters.size();
		int startIndex = page * size;

		// Si el índice de inicio es mayor que el total, devolver página vacía
		if (startIndex >= totalRoots) {
			return new PageImpl<>(new ArrayList<>(), PageRequest.of(page, size), totalElements);
		}

		// Obtener las familias raíz para esta página
		int endIndex = Math.min(startIndex + size, totalRoots);
		List<CostCenterEntity> pageRoots = rootCostCenters.subList(startIndex, endIndex);

		// Construir lista con todas las familias completas (padres + hijos + nietos)
		List<CostCenter> result = new ArrayList<>();
		for (CostCenterEntity root : pageRoots) {
			// Agregar el padre
			result.add(dataMapper.toDomain(root));

			// Agregar todos sus descendientes recursivamente
			addChildrenRecursively(root, result);
		}

		return new PageImpl<>(result, PageRequest.of(page, size), totalElements);
	}

	@Override
	@Transactional(readOnly = true)
	public CostCenter findById(Long id, String idEnterprise) {
		return dataMapper.toDomain(repository.findByIdAndIdEnterprise(id, idEnterprise)
				.orElseThrow(CostCentersNotFoundException::new));
	}

	@Override
	public CostCenter findById(Long id) {
		return dataMapper.toDomain(repository.findById(id)
				.orElse(null));
	}

	@Override
	@Transactional
	@Auditable(operationType = OperationType.INACTIVATE, affectedTable = "COST_CENTER", moduleName = "COST_CENTERS", idArgIndex = 0, enterpriseIdArgIndex = 1)
	public CostCenter changeState(Long id, String idEnterprise, Boolean status) {
		CostCenterEntity current = repository.findByIdAndIdEnterprise(id, idEnterprise)
				.orElseThrow(CostCentersNotFoundException::new);

		// Si se activa cuenta hija se activan padres
		if (status && current.getParent() != null) {
			activateParentHierarchy(current.getParent());
		}

		// Cambiar el estado del centro de costo actual
		current.setStatus(status);
		CostCenterEntity saved = repository.save(current);

		// Si se está inactivando, inactivar todos los hijos recursivamente
		if (!status) {
			changeChildrenStateRecursively(id, status);
		}

		return dataMapper.toDomain(saved);
	}

	@Override
	@Transactional
	@Auditable(operationType = OperationType.DELETE, affectedTable = "COST_CENTER", moduleName = "COST_CENTERS", idArgIndex = 0, enterpriseIdArgIndex = 1)
	public CostCenter delete(Long id, String idEnterprise) {
		CostCenterEntity current = repository.findByIdAndIdEnterprise(id, idEnterprise)
				.orElseThrow(CostCentersNotFoundException::new);

		// Validar que el centro de costo no tenga movimientos contables asociados
		if (current.getUsageCount() != null && current.getUsageCount() > 0) {
			throw new CostCenterInUseException(current.getCode(), false); // false indica operación de eliminación
		}

		// Validar que no tenga centros de costo hijos
		if (repository.existsByParentId(id)) {
			throw new CostCenterHasChildrenException(current.getCode());
		}

		// Eliminación física del centro de costo
		repository.delete(current);
		return dataMapper.toDomain(current);
	}

	/**
	 * Cambia recursivamente el estado de todos los centros de costo hijos (y
	 * descendientes) de un centro de costo padre.
	 * 
	 * @param parentId ID del centro de costo padre
	 */
	private void changeChildrenStateRecursively(Long parentId, Boolean status) {
		// Obtener todos los hijos del centro de costo padre
		List<CostCenterEntity> children = repository.findByParentId(parentId);

		for (CostCenterEntity child : children) {
			child.setStatus(status);
			repository.save(child);

			changeChildrenStateRecursively(child.getId(), status);
		}
	}

	/**
	 * Agrega recursivamente todos los hijos de un centro de costo a la lista
	 * resultado
	 */
	private void addChildrenRecursively(CostCenterEntity parent, List<CostCenter> result) {
		List<CostCenterEntity> children = repository.findByParentId(parent.getId());
		for (CostCenterEntity child : children) {
			result.add(dataMapper.toDomain(child));
			addChildrenRecursively(child, result);
		}
	}

	/**
	 * Cuenta el total de nodos en todas las familias raíz
	 */
	private long countAllNodesInRoots(List<CostCenterEntity> rootCostCenters) {
		long count = 0;
		for (CostCenterEntity root : rootCostCenters) {
			count += 1 + countChildrenRecursively(root); // 1 para el padre + hijos
		}
		return count;
	}

	/**
	 * Cuenta recursivamente todos los hijos de un centro de costo
	 */
	private long countChildrenRecursively(CostCenterEntity parent) {
		List<CostCenterEntity> children = repository.findByParentId(parent.getId());
		long count = children.size();
		for (CostCenterEntity child : children) {
			count += countChildrenRecursively(child);
		}
		return count;
	}

	@Override
	@Transactional(readOnly = true)
	public List<CostCenter> findActiveLastLevelCostCenters(String idEnterprise) {

		// Filtra por empresa, estado activo y código con longitud >= 5
		List<CostCenterEntity> auxiliaryCostCenters = repository.findAuxiliaryCostCenters(idEnterprise);

		// Mapear entidades a modelos de dominio
		return auxiliaryCostCenters.stream()
				.map(dataMapper::toDomain)
				.toList();
	}

	@Override
	@Transactional(readOnly = true)
	public long countAllByEnterprise(String idEnterprise) {
		return repository.countByIdEnterprise(idEnterprise);
	}

	@Override
	@Transactional(readOnly = true)
	public long countAllByEnterpriseAndStatus(String idEnterprise, Boolean status) {
		return repository.countByIdEnterpriseAndStatus(idEnterprise, status);
	}

	@Override
	@Transactional(readOnly = true)
	public Page<CostCenter> findByEnterpriseAndSearch(String idEnterprise, String search, int page, int size) {
		Pageable pageable = PageRequest.of(page, size);
		return repository.findByIdEnterpriseAndCodeContainingIgnoreCaseOrIdEnterpriseAndNameContainingIgnoreCase(
				idEnterprise, search, idEnterprise, search, pageable).map(dataMapper::toDomain);
	}

	@Override
	@Transactional(readOnly = true)
	public long countByEnterpriseAndSearch(String idEnterprise, String search) {
		return repository.countByIdEnterpriseAndCodeContainingIgnoreCaseOrIdEnterpriseAndNameContainingIgnoreCase(
				idEnterprise, search, idEnterprise, search);
	}

	/**
	 * Activa recursivamente toda la jerarquía de cuentas padre si están inactivas.
	 * 
	 * @param parent Centro de costo padre a activar (junto con sus ancestros)
	 */
	private void activateParentHierarchy(CostCenterEntity parent) {
		if (parent == null) {
			return;
		}

		// Si el padre está inactivo, activarlo
		if (!parent.getStatus()) {
			parent.setStatus(true);
			repository.save(parent);
		}

		// Recursivamente activar el padre del padre
		if (parent.getParent() != null) {
			activateParentHierarchy(parent.getParent());
		}
	}

	/**
	 * Actualiza recursivamente los códigos de todos los centros de costo hijos
	 * cuando cambia el código del padre.
	 * Reemplaza el prefijo del código padre antiguo por el nuevo en todos los
	 * descendientes.
	 * 
	 * @param parentId      ID del centro de costo padre cuyo código cambió
	 * @param oldParentCode Código antiguo del padre
	 * @param newParentCode Código nuevo del padre
	 */
	private void updateChildrenCodes(Long parentId, String oldParentCode, String newParentCode) {
		// Obtener todos los hijos directos del padre
		List<CostCenterEntity> children = repository.findByParentId(parentId);

		for (CostCenterEntity child : children) {
			String oldChildCode = child.getCode();

			// Verificar que el código del hijo comience con el código del padre antiguo
			if (oldChildCode.startsWith(oldParentCode)) {
				// Reemplazar el prefijo del código padre antiguo por el nuevo
				String newChildCode = newParentCode + oldChildCode.substring(oldParentCode.length());

				// Actualizar el código del hijo
				child.setCode(newChildCode);
				repository.save(child);

				// Actualizar recursivamente los códigos de los descendientes de este hijo
				updateChildrenCodes(child.getId(), oldChildCode, newChildCode);
			}
		}
	}

	/**
	 * Actualiza el contador de uso de un centro de costo
	 * Utilizado principalmente para operaciones de mensajería
	 * 
	 * @param id         ID del centro de costo
	 * @param usageCount Nuevo valor del contador de uso
	 */
	@Transactional
	public void updateUsageCount(Long id, Integer usageCount) {
		CostCenterEntity entity = repository.findById(id)
				.orElseThrow(() -> new CostCentersNotFoundException("Centro de costo no encontrado con ID: " + id));

		entity.setUsageCount(usageCount);
		repository.save(entity);
	}
}
