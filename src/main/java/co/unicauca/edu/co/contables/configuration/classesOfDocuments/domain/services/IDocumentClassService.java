package co.unicauca.edu.co.contables.configuration.classesOfDocuments.domain.services;

import org.springframework.data.domain.Page;

import co.unicauca.edu.co.contables.configuration.classesOfDocuments.domain.models.DocumentClass;
import co.unicauca.edu.co.contables.configuration.classesOfDocuments.presentation.DTO.request.DocumentClassCreateReq;
import co.unicauca.edu.co.contables.configuration.classesOfDocuments.presentation.DTO.request.DocumentClassUpdateReq;

/**
 * @brief Interfaz del servicio de dominio para clases de documento
 *
 * Define las operaciones de negocio para gestionar clases de documento,
 * incluyendo creación, actualización, consulta, eliminación y búsquedas paginadas.
 */
public interface IDocumentClassService {

	DocumentClass create(DocumentClassCreateReq request);

	DocumentClass update(DocumentClassUpdateReq request);

	DocumentClass findById(Long id, String idEnterprise);

	Page<DocumentClass> findAllByEnterprise(String idEnterprise, int page, int size);

	Page<DocumentClass> findAllByEnterprise(String idEnterprise, int page, int size, String sortField, String sortOrder);

	Page<DocumentClass> findAllByEnterpriseAndStatus(String idEnterprise, Boolean status, int page, int size);

	DocumentClass changeState(Long id, String idEnterprise, Boolean status);

	DocumentClass Delete(Long id, String idEnterprise);

	/**
	 * Cuenta el total de clases de documento por empresa
	 * @param idEnterprise ID de la empresa
	 * @return Número total de clases de documento
	 */
	long countAllByEnterprise(String idEnterprise);

	/**
	 * Cuenta el total de clases de documento filtradas por estado
	 * @param idEnterprise ID de la empresa
	 * @param status Estado de la clase de documento
	 * @return Número total de clases de documento con el estado especificado
	 */
	long countAllByEnterpriseAndStatus(String idEnterprise, Boolean status);

	/**
	 * Busca clases de documento por empresa y nombre (búsqueda parcial)
	 * @param idEnterprise ID de la empresa
	 * @param search Término de búsqueda
	 * @param page Número de página
	 * @param size Tamaño de página
	 * @param sortField Campo de ordenamiento
	 * @param sortOrder Orden (asc/desc)
	 * @return Página de clases de documento que coinciden con la búsqueda
	 */
	Page<DocumentClass> findByEnterpriseAndNameContaining(String idEnterprise, String search, int page, int size, String sortField, String sortOrder);

	/**
	 * Cuenta clases de documento por empresa y nombre (búsqueda parcial)
	 * @param idEnterprise ID de la empresa
	 * @param search Término de búsqueda
	 * @return Número total de clases de documento que coinciden con la búsqueda
	 */
	long countByEnterpriseAndNameContaining(String idEnterprise, String search);
}
