package co.unicauca.edu.co.contables.configuration.typesOfDocuments.domain.services;

import org.springframework.data.domain.Page;

import co.unicauca.edu.co.contables.configuration.typesOfDocuments.domain.models.DocumentModule;
import co.unicauca.edu.co.contables.configuration.typesOfDocuments.domain.models.DocumentType;
import co.unicauca.edu.co.contables.configuration.typesOfDocuments.presentation.DTO.request.DocumentTypeCreateReq;
import co.unicauca.edu.co.contables.configuration.typesOfDocuments.presentation.DTO.request.DocumentTypeUpdateReq;

import java.util.List;

public interface IDocumentTypeService {

	DocumentType create(DocumentTypeCreateReq request);

	DocumentType update(DocumentTypeUpdateReq request);

	DocumentType findById(Long id, String idEnterprise);

	Page<DocumentType> findAllByEnterprise(String idEnterprise, int page, int size);

	Page<DocumentType> findAllByEnterprise(String idEnterprise, int page, int size, String sortField, String sortOrder);
	
	/**
	 * Busca tipos de documento por ID de módulo y empresa
	 * @param moduleId ID del módulo
	 * @param idEnterprise ID de la empresa
	 * @param page Número de página
	 * @param size Tamaño de página
	 * @return Página de tipos de documento
	 */
	Page<DocumentType> findAllByModuleAndEnterprise(Integer moduleId, String idEnterprise, int page, int size);

	DocumentType changeState(Long id, String idEnterprise, Boolean status);

	DocumentType Delete(Long id, String idEnterprise);

	/**
	 * Cuenta el total de tipos de documento por empresa
	 * @param idEnterprise ID de la empresa
	 * @return Número total de tipos de documento
	 */
	long countAllByEnterprise(String idEnterprise);

	/**
	 * Cuenta el total de tipos de documento filtrados por ID de módulo
	 * @param moduleId ID del módulo
	 * @param idEnterprise ID de la empresa
	 * @return Número total de tipos de documento del módulo especificado
	 */
	long countAllByModuleAndEnterprise(Integer moduleId, String idEnterprise);

	/**
	 * Obtiene todos los módulos disponibles en el sistema
	 * @return Lista de módulos disponibles
	 */
	List<DocumentModule> getAllModules();
}
