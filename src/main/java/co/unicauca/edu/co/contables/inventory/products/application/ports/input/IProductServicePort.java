package co.unicauca.edu.co.contables.inventory.products.application.ports.input;

import java.util.List;
import java.util.Optional;

import co.unicauca.edu.co.contables.inventory.products.domain.model.Product;

import org.springframework.data.domain.Page;

/**
 * @brief Puerto de entrada para operaciones CRUD completas de productos
 *
 * Define contrato completo de operaciones para gestión de productos:
 * - Consultas avanzadas con filtros y paginación
 * - Operaciones de búsqueda por múltiples criterios
 * - Gestión de inventario y estadísticas
 * - Sincronización y exportación de datos
 */
public interface IProductServicePort {

  /**
   * @brief Busca un producto por su ID y empresa
   * @param id el ID del producto a buscar
   * @param enterpriseId el ID de la empresa
   * @return el producto encontrado
   */
  Product findById(Long id, String enterpriseId);

  /**
   * @brief Obtiene productos con filtros de búsqueda y paginación
   * @param enterpriseId el ID de la empresa
   * @param search el término de búsqueda (opcional)
   * @param pageNumber el número de página
   * @param pageSize el tamaño de página
   * @param sortField el campo de ordenamiento
   * @param sortOrder el orden (asc/desc)
   * @return una página de productos
   */
  Page<Product> findAllWithFilters(String enterpriseId, String search, int pageNumber, int pageSize, String sortField, String sortOrder);

  /**
   * @brief Obtiene productos con paginación opcional y filtros
   * @param enterpriseId el ID de la empresa
   * @param numPage el número de página (opcional)
   * @param size el tamaño de página (opcional)
   * @param sortField el campo de ordenamiento
   * @param sortOrder el orden (asc/desc)
   * @param search el término de búsqueda (opcional)
   * @return una página de productos
   */
  Page<Product> findAllPaginated(String enterpriseId, Optional<Integer> numPage, Optional<Integer> size, String sortField, String sortOrder, Optional<String> search);

  /**
   * @brief Cuenta productos por empresa con filtros de búsqueda
   * @param enterpriseId el ID de la empresa
   * @param search el término de búsqueda (opcional)
   * @return el número de productos que coinciden
   */
  long countByEnterpriseIdWithFilters(String enterpriseId, String search);

  /**
   * @brief Cuenta todos los productos por empresa
   * @param enterpriseId el ID de la empresa
   * @return el número total de productos
   */
  long countByEnterpriseId(String enterpriseId);

  /**
   * @brief Cuenta productos activos por empresa
   * @param enterpriseId el ID de la empresa
   * @return el número de productos activos
   */
  long countActivatedByEnterpriseId(String enterpriseId);

  /**
   * @brief Obtiene productos activos con paginación
   * @param enterpriseId el ID de la empresa
   * @param pageNumber el número de página
   * @param pageSize el tamaño de página
   * @return una página de productos activos
   */
  Page<Product> findActivatedWithPagination(String enterpriseId, int pageNumber, int pageSize);

  /**
   * @brief Obtiene productos activos con paginación opcional
   * @param enterpriseId el ID de la empresa
   * @param numPage el número de página (opcional)
   * @param size el tamaño de página (opcional)
   * @return una página de productos activos
   */
  Page<Product> findActivatedPaginated(String enterpriseId, Optional<Integer> numPage, Optional<Integer> size);

  /**
   * @brief Crea un nuevo producto
   * @param product el producto a crear
   * @return el producto creado con ID asignado
   */
  Product create(Product product);

  /**
   * @brief Actualiza un producto existente
   * @param id el ID del producto a actualizar
   * @param product los datos del producto actualizado
   * @param enterpriseId el ID de la empresa
   * @return el producto actualizado con los nuevos datos
   */
  Product update(Long id, Product product, String enterpriseId);

  /**
   * @brief Elimina un producto por su ID
   * @param id el ID del producto a eliminar
   * @param enterpriseId el ID de la empresa
   */
  void deleteById(Long id, String enterpriseId);

  /**
   * @brief Cambia el estado de un producto (activar/desactivar)
   * @param id el ID del producto cuyo estado se va a cambiar
   * @param enterpriseId el ID de la empresa
   */
  void changeState(Long id, String enterpriseId);

  /**
   * @brief Obtiene todos los productos asociados a una categoría
   * @param categoryId el ID de la categoría
   * @return una lista de todos los productos de la categoría
   */
  List<Product> findAllByCategory(Long categoryId);

  /**
   * @brief Obtiene todos los productos asociados a una unidad de medida
   * @param unitOfMeasureId el ID de la unidad de medida
   * @return una lista de todos los productos de la unidad de medida
   */
  List<Product> findAllByUnitOfMeasure(Long unitOfMeasureId);

  /**
   * @brief Obtiene todos los productos asociados a un tipo de producto
   * @param productTypeId el ID del tipo de producto
   * @return una lista de todos los productos del tipo de producto
   */
  List<Product> findAllByProductType(Long productTypeId);
}
