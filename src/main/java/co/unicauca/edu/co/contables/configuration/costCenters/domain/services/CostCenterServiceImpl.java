package co.unicauca.edu.co.contables.configuration.costCenters.domain.services;

import co.unicauca.edu.co.contables.configuration.commons.exceptions.costCenters.CostCentersAlreadyExistsException;
import co.unicauca.edu.co.contables.configuration.commons.exceptions.costCenters.CostCentersNotFoundException;
import co.unicauca.edu.co.contables.configuration.commons.exceptions.costCenters.CostCenterHasChildrenException;
import co.unicauca.edu.co.contables.configuration.commons.utils.StringStandardizationUtils;
import co.unicauca.edu.co.contables.configuration.costCenters.dataAccess.entity.CostCenterEntity;
import co.unicauca.edu.co.contables.configuration.costCenters.dataAccess.mapper.CostCenterDataMapper;
import co.unicauca.edu.co.contables.configuration.costCenters.dataAccess.repository.CostCenterRepository;
import co.unicauca.edu.co.contables.configuration.costCenters.dataAccess.repository.CostCenterSpecifications;
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

@Service
@RequiredArgsConstructor
public class CostCenterServiceImpl implements ICostCenterService {

	private final CostCenterRepository repository;
	private final CostCenterDataMapper dataMapper;
	private final CostCenterDomainMapper domainMapper;

	@Override
	@Transactional
	public CostCenter create(CostCenterCreateReq request) {
		// Validación de unicidad por código y nombre dentro de la empresa
		if (repository.existsByCodeAndIdEnterprise(request.getCode(), request.getIdEnterprise())) {
			throw new CostCentersAlreadyExistsException(request.getCode(), request.getIdEnterprise());
		}
		// Estandarizar nombre: primera letra mayúscula, resto minúsculas, colapsar espacios
		String standardizedName = StringStandardizationUtils.standardizeName(request.getName());
		request.setName(standardizedName);

		// Validación de nombre exacto (tras estandarización)
		if (repository.existsByNameAndIdEnterprise(standardizedName, request.getIdEnterprise())) {
			throw new CostCentersAlreadyExistsException(request.getName(), request.getIdEnterprise(), true);
		}

		CostCenter costCenter = domainMapper.toDomain(request);
		CostCenterEntity entity = dataMapper.toEntity(costCenter);
		if (request.getParentId() != null) {
			CostCenterEntity parent = repository.findByIdAndIdEnterprise(request.getParentId(), request.getIdEnterprise())
					.orElseThrow(CostCentersNotFoundException::new);
			entity.setParent(parent);
		}

		CostCenterEntity saved = repository.save(entity);
		return dataMapper.toDomain(saved);
	}

	@Override
	@Transactional
	public CostCenter update(CostCenterUpdateReq request) {
		CostCenterEntity current = repository.findByIdAndIdEnterprise(request.getId(), request.getIdEnterprise())
				.orElseThrow(CostCentersNotFoundException::new);

		// Estandarizar nombre antes de validar
		String standardizedName = StringStandardizationUtils.standardizeName(request.getName());
		request.setName(standardizedName);

		// Si cambian code o name, validar que no exista otro con esos datos en la misma empresa
        boolean codeChanged = request.getCode() != null && !request.getCode().equals(current.getCode());
		boolean nameChanged = request.getName() != null && !request.getName().equals(current.getName());
		boolean enterpriseChanged = request.getIdEnterprise() != null && !request.getIdEnterprise().equals(current.getIdEnterprise());

		String targetEnterprise = enterpriseChanged ? request.getIdEnterprise() : current.getIdEnterprise();

        if (codeChanged || enterpriseChanged) {
            boolean existsCode = repository.existsByCodeAndIdEnterprise(request.getCode(), targetEnterprise);
			if (existsCode) {
				throw new CostCentersAlreadyExistsException(request.getCode(), targetEnterprise);
			}
		}
		if (nameChanged || enterpriseChanged) {
			boolean existsName = repository.existsByNameAndIdEnterpriseAndIdNot(request.getName(), targetEnterprise, current.getId());
			if (existsName) {
				throw new CostCentersAlreadyExistsException(request.getName(), targetEnterprise, true);
			}
		}

		current.setIdEnterprise(request.getIdEnterprise());
		current.setCode(request.getCode());
		current.setName(request.getName());
		if (request.getParentId() != null) {
			CostCenterEntity parent = repository.findByIdAndIdEnterprise(request.getParentId(), targetEnterprise)
					.orElseThrow(CostCentersNotFoundException::new);
			current.setParent(parent);
		} else {
			current.setParent(null);
		}
		return dataMapper.toDomain(repository.save(current));
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
	@Transactional
	public CostCenter changeState(Long id, String idEnterprise, Boolean status) {
		CostCenterEntity current = repository.findByIdAndIdEnterprise(id, idEnterprise)
				.orElseThrow(CostCentersNotFoundException::new);

		// Cambiar el estado del centro de costo actual
		current.setStatus(status);
		CostCenterEntity saved = repository.save(current);

		// Cambiar recursivamente el estado de todos los hijos
		changeChildrenStateRecursively(id, status);

		return dataMapper.toDomain(saved);
	}

	@Override
	@Transactional
	public CostCenter delete(Long id, String idEnterprise) {
		CostCenterEntity current = repository.findByIdAndIdEnterprise(id, idEnterprise)
				.orElseThrow(CostCentersNotFoundException::new);

		// Validar que no tenga centros de costo hijos
		if (repository.existsByParentId(id)) {
			throw new CostCenterHasChildrenException(id, current.getCode());
		}

		// Eliminación física del centro de costo
		repository.delete(current);
		return dataMapper.toDomain(current);
	}

	/**
	 * Cambia recursivamente el estado de todos los centros de costo hijos (y descendientes) de un centro de costo padre.
	 * @param parentId ID del centro de costo padre
	 */
	private void changeChildrenStateRecursively(Long parentId, Boolean status) {
		// Obtener todos los hijos del centro de costo padre
		List<CostCenterEntity> children = repository.findByParentId(parentId);
		
		// Cambiar el estado de cada hijo y procesar recursivamente sus descendientes
		for (CostCenterEntity child : children) {
			// Cambiar el estado del hijo
			child.setStatus(status);
			repository.save(child);
			
			// Procesar recursivamente los hijos de este hijo
			changeChildrenStateRecursively(child.getId(), status);
		}
	}

	/**
	 * Agrega recursivamente todos los hijos de un centro de costo a la lista resultado
	 */
	private void addChildrenRecursively(CostCenterEntity parent, List<CostCenter> result) {
		List<CostCenterEntity> children = repository.findByParentId(parent.getId());
		for (CostCenterEntity child : children) {
			result.add(dataMapper.toDomain(child));
			// Recursivamente agregar los hijos de este hijo
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
		// Usar Specification para filtrar directamente en la base de datos
		// Esto es más eficiente que traer todos los registros y filtrar en memoria
		List<CostCenterEntity> auxiliaryCostCenters = repository.findAll(
			CostCenterSpecifications.isAuxiliaryCostCenter(idEnterprise)
		);
		
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

}


