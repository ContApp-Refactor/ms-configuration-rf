package co.unicauca.edu.co.contables.accounting.catalogue.banks.domain.services;

import org.springframework.data.domain.Page;

import co.unicauca.edu.co.contables.accounting.catalogue.banks.domain.model.Bank;
import co.unicauca.edu.co.contables.accounting.catalogue.banks.presentation.DTO.request.BankCreateReq;
import co.unicauca.edu.co.contables.accounting.catalogue.banks.presentation.DTO.request.BankUpdateReq;

/**
 * @brief Contrato para servicios de gestión de bancos
 *
 * Define las operaciones principales para crear, actualizar, consultar y gestionar
 * bancos con filtros por empresa, estado y búsqueda por código o nombre.
 */
public interface IBankService {

    /**
     * @brief Crea un nuevo banco con la información proporcionada
     *
     * @param request solicitud de creación de banco con todos los datos requeridos
     * @return el banco creado
     */
    Bank create(BankCreateReq request);

    /**
     * @brief Actualiza un banco existente con la información proporcionada
     *
     * @param request solicitud de actualización de banco con los nuevos datos
     * @return el banco actualizado
     */
    Bank update(BankUpdateReq request);

    /**
     * @brief Busca un banco por su ID y ID de empresa
     *
     * @param id el ID del banco
     * @param idEnterprise el ID de la empresa
     * @return el banco encontrado
     */
    Bank findById(Long id, String idEnterprise);

    /**
     * @brief Busca todos los bancos de una empresa con filtrado avanzado y capacidades de búsqueda
     *
     * @param idEnterprise el ID de la empresa
     * @param page el número de página (0-based)
     * @param size el tamaño de página
     * @param sortField el campo para ordenar (code/name)
     * @param sortOrder el orden de clasificación (asc/desc)
     * @param search el término de búsqueda para código o nombre
     * @return una página de bancos filtrados
     */
    Page<Bank> findAllByEnterpriseWithFilters(String idEnterprise, Integer page, Integer size, String sortField, String sortOrder, String search);

    /**
     * @brief Busca todos los bancos activos de una empresa
     *
     * @param idEnterprise el ID de la empresa
     * @param page el número de página
     * @param size el tamaño de página
     * @return una página de bancos activos
     */
    Page<Bank> findAllActiveByEnterprise(String idEnterprise, Integer page, Integer size);

    /**
     * @brief Cambia el estado de un banco (activo/inactivo)
     *
     * @param id el ID del banco
     * @param idEnterprise el ID de la empresa
     * @param newState el nuevo estado (true para activo, false para inactivo)
     * @return el banco actualizado
     */
    Bank changeState(Long id, String idEnterprise, Boolean newState);

    /**
     * @brief Elimina un banco por su ID y ID de empresa
     *
     * @param id el ID del banco
     * @param idEnterprise el ID de la empresa
     * @return el banco eliminado
     */
    Bank delete(Long id, String idEnterprise);
}
