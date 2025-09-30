package co.unicauca.edu.co.contables.configuration.typesOfDocuments.domain.services;

import org.springframework.data.domain.Page;

import co.unicauca.edu.co.contables.configuration.typesOfDocuments.domain.models.DocumentType;
import co.unicauca.edu.co.contables.configuration.typesOfDocuments.presentation.DTO.request.DocumentTypeCreateReq;
import co.unicauca.edu.co.contables.configuration.typesOfDocuments.presentation.DTO.request.DocumentTypeUpdateReq;

public interface IDocumentTypeService {

	DocumentType create(DocumentTypeCreateReq request);

	DocumentType update(DocumentTypeUpdateReq request);

	DocumentType findById(Long id, String idEnterprise);

	Page<DocumentType> findAllByEnterprise(String idEnterprise, int page, int size);

	Page<DocumentType> findAllByEnterprise(String idEnterprise, int page, int size, String sortField, String sortOrder);
	
	Page<DocumentType> findAllByModuleAndEnterprise(String module, String idEnterprise, int page, int size);

	DocumentType changeState(Long id, String idEnterprise, Boolean status);

	DocumentType Delete(Long id, String idEnterprise);

	/**
	 * Cuenta el total de tipos de documento por empresa
	 * @param idEnterprise ID de la empresa
	 * @return Número total de tipos de documento
	 */
	long countAllByEnterprise(String idEnterprise);

	/**
	 * Cuenta el total de tipos de documento filtrados por módulo
	 * @param module Módulo del tipo de documento
	 * @param idEnterprise ID de la empresa
	 * @return Número total de tipos de documento del módulo especificado
	 */
	long countAllByModuleAndEnterprise(String module, String idEnterprise);
}
