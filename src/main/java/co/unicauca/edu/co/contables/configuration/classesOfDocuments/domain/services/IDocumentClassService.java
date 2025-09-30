package co.unicauca.edu.co.contables.configuration.classesOfDocuments.domain.services;

import org.springframework.data.domain.Page;

import co.unicauca.edu.co.contables.configuration.classesOfDocuments.domain.models.DocumentClass;
import co.unicauca.edu.co.contables.configuration.classesOfDocuments.presentation.DTO.request.DocumentClassCreateReq;
import co.unicauca.edu.co.contables.configuration.classesOfDocuments.presentation.DTO.request.DocumentClassUpdateReq;

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
}
