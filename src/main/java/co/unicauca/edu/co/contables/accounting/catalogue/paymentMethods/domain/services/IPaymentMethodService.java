package co.unicauca.edu.co.contables.accounting.catalogue.paymentMethods.domain.services;

import org.springframework.data.domain.Page;

import co.unicauca.edu.co.contables.accounting.catalogue.paymentMethods.domain.model.PaymentMethod;
import co.unicauca.edu.co.contables.accounting.catalogue.paymentMethods.presentation.DTO.request.PaymentMethodCreateReq;
import co.unicauca.edu.co.contables.accounting.catalogue.paymentMethods.presentation.DTO.request.PaymentMethodUpdateReq;

import java.util.Optional;

/**
 * @brief Contrato para servicios de gestión de métodos de pago
 *
 * Define las operaciones principales para crear, actualizar, consultar y gestionar
 * métodos de pago con filtros por empresa y búsqueda avanzada.
 */
public interface IPaymentMethodService {

    /**
     * @brief Crea un nuevo método de pago con validaciones completas
     * @param request datos de creación del método de pago
     * @return el método de pago creado
     */
    PaymentMethod create(PaymentMethodCreateReq request);

    /**
     * @brief Actualiza un método de pago existente con validaciones
     * @param request datos de actualización del método de pago
     * @return el método de pago actualizado
     */
    PaymentMethod update(PaymentMethodUpdateReq request);

    /**
     * @brief Busca un método de pago por su ID y empresa
     * @param id el ID del método de pago
     * @param idEnterprise el ID de la empresa
     * @return el método de pago encontrado
     */
    PaymentMethod findById(Long id, String idEnterprise);

    /**
     * @brief Consulta métodos de pago con filtros avanzados y búsqueda
     * @param idEnterprise ID de la empresa
     * @param page Número de página (opcional)
     * @param size Tamaño de página (opcional)
     * @param sortField Campo para ordenamiento
     * @param sortOrder Dirección del ordenamiento (asc/desc)
     * @param search Término de búsqueda por nombre o cuenta contable
     * @return Página de métodos de pago filtrados
     */
    Page<PaymentMethod> findAllByEnterprise(String idEnterprise, Optional<Integer> page, Optional<Integer> size, String sortField, String sortOrder, String search);

    /**
     * @brief Consulta métodos de pago activos por empresa
     * @param idEnterprise ID de la empresa
     * @param page Número de página (opcional)
     * @param size Tamaño de página (opcional)
     * @return Página de métodos de pago activos
     */
    Page<PaymentMethod> findAllActiveByEnterprise(String idEnterprise, Optional<Integer> page, Optional<Integer> size);

    /**
     * @brief Cambia el estado activo/inactivo de un método de pago
     * @param id el ID del método de pago
     * @param idEnterprise el ID de la empresa
     * @param newState el nuevo estado (true para activo, false para inactivo)
     * @return el método de pago con estado actualizado
     */
    PaymentMethod changeState(Long id, String idEnterprise, Boolean newState);

    /**
     * @brief Elimina un método de pago por su ID y empresa
     * @param id el ID del método de pago
     * @param idEnterprise el ID de la empresa
     * @return el método de pago eliminado
     */
    PaymentMethod delete(Long id, String idEnterprise);

    /**
     * @brief Actualiza el contador de uso de un método de pago
     * @param id el ID del método de pago
     * @param usageCount el nuevo valor del contador de uso
     */
    void updateUsageCount(Long id, Integer usageCount);
}
