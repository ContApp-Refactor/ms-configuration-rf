package co.unicauca.edu.co.contables.configuration.typesOfDocuments.domain.services;

import org.springframework.data.domain.Page;

import co.unicauca.edu.co.contables.configuration.typesOfDocuments.domain.models.DocumentModule;
import co.unicauca.edu.co.contables.configuration.typesOfDocuments.domain.models.DocumentType;
import co.unicauca.edu.co.contables.configuration.typesOfDocuments.presentation.DTO.request.DocumentTypeCreateReq;
import co.unicauca.edu.co.contables.configuration.typesOfDocuments.presentation.DTO.request.DocumentTypeUpdateReq;

import java.util.List;

/** @brief Interfaz del servicio de tipos de documentos */
public interface IDocumentTypeService {

	DocumentType create(DocumentTypeCreateReq request);

	DocumentType update(DocumentTypeUpdateReq request);

	DocumentType findById(Long id, String idEnterprise);

	Page<DocumentType> findAllByEnterprise(String idEnterprise, int page, int size);

	Page<DocumentType> findAllByEnterprise(String idEnterprise, int page, int size, String sortField, String sortOrder);
	
	/**
	 * @brief Obtiene todos los tipos de documento por ID de módulo y empresa
	 * @param moduleId ID del módulo
	 * @param idEnterprise ID de la empresa
	 * @return Lista de tipos de documento del módulo
	 */
	List<DocumentType> findAllByModuleAndEnterprise(Integer moduleId, String idEnterprise);

	DocumentType changeState(Long id, String idEnterprise, Boolean status);

	DocumentType Delete(Long id, String idEnterprise);

	/**
	 * @brief Actualiza el contador de uso de un tipo de documento
	 * @param id ID del tipo de documento
	 * @param enterpriseId ID de la empresa
	 * @param usageCount Nuevo valor del contador de uso
	 */
	void updateUsageCount(Long id, String enterpriseId, Integer usageCount);

	/**
	 * @brief Cuenta el total de tipos de documento por empresa
	 * @param idEnterprise ID de la empresa
	 * @return Número total de tipos de documento
	 */
	long countAllByEnterprise(String idEnterprise);


	/**
	 * @brief Obtiene todos los módulos disponibles en el sistema
	 * @return Lista de módulos disponibles
	 */
	List<DocumentModule> getAllModules();

	/**
	 * @brief Busca tipos de documento por empresa y nombre (búsqueda parcial)
	 * @param idEnterprise ID de la empresa
	 * @param search Término de búsqueda
	 * @param page Número de página
	 * @param size Tamaño de página
	 * @param sortField Campo de ordenamiento
	 * @param sortOrder Orden (asc/desc)
	 * @return Página de tipos de documento que coinciden con la búsqueda
	 */
	Page<DocumentType> findByEnterpriseAndNameContaining(String idEnterprise, String search, int page, int size, String sortField, String sortOrder);

	/**
	 * @brief Cuenta tipos de documento por empresa y nombre (búsqueda parcial)
	 * @param idEnterprise ID de la empresa
	 * @param search Término de búsqueda
	 * @return Número total de tipos de documento que coinciden con la búsqueda
	 */
	long countByEnterpriseAndNameContaining(String idEnterprise, String search);
}
