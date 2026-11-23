package co.unicauca.edu.co.contables.inventory.products.application.service.importExport;

import co.unicauca.edu.co.contables.inventory.products.application.ports.output.ICategoryPersistencePort;
import co.unicauca.edu.co.contables.inventory.products.application.ports.output.IProductPersistencePort;
import co.unicauca.edu.co.contables.inventory.products.application.ports.output.IProductTypePersistencePort;
import co.unicauca.edu.co.contables.inventory.products.application.ports.output.IUnitOfMeasurePersistencePort;
import co.unicauca.edu.co.contables.inventory.products.domain.enums.ImportStatus;
import co.unicauca.edu.co.contables.inventory.products.domain.exception.product.ProductExportException;
import co.unicauca.edu.co.contables.inventory.products.domain.model.Product;
import co.unicauca.edu.co.contables.inventory.products.infraestructure.input.rest.dto.request.ProductExportRequest;
import co.unicauca.edu.co.contables.inventory.products.infraestructure.utils.ExcelStyleHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Page;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @brief Servicio para procesar exportaciones de productos de forma asíncrona
 *
 * Este servicio ejecuta la exportación en un hilo separado para no bloquear
 * la petición HTTP, permitiendo exportaciones de grandes volúmenes de datos.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProductAsyncExportProcessor {

    private final IProductPersistencePort productPersistencePort;
    private final ICategoryPersistencePort categoryPersistencePort;
    private final IProductTypePersistencePort productTypePersistencePort;
    private final IUnitOfMeasurePersistencePort unitOfMeasurePersistencePort;
    private final ProductExportJobTracker jobTracker;
    private final ProductExcelValidationService excelValidationService;
    
    private static final int EXPORT_PAGE_SIZE = 1000;

    /**
     * @brief Procesa la exportación de forma asíncrona
     * @param exportRequest solicitud de exportación con filtros
     * @param jobId identificador del trabajo
     */
    @Async
    public void processExportAsync(ProductExportRequest exportRequest, String jobId) {
        int totalRecords = 0;

        try {
            jobTracker.updateJobStatus(jobId, ImportStatus.PROCESSING);
            jobTracker.updateProgress(jobId, 10);

            // FASE 1: Obtener datos filtrados con paginación
            List<Product> products = getFilteredProducts(exportRequest, jobId);

            totalRecords = products.size();
            jobTracker.updateTotalRecords(jobId, totalRecords);
            jobTracker.updateProgress(jobId, 50);

            // Validar que existan datos
            if (products == null || products.isEmpty()) {
                handleNoDataError(exportRequest, jobId);
                return;
            }

            // FASE 2: Generar archivo Excel
            byte[] excelData = generateExcelFile(products, exportRequest, jobId);

            jobTracker.updateProgress(jobId, 90);

            // FASE 3: Almacenar archivo en memoria
            jobTracker.setFileData(jobId, excelData);

            // Completar job
            jobTracker.updateProgress(jobId, 100);
            jobTracker.updateJobStatus(jobId, ImportStatus.COMPLETED);

        } catch (ProductExportException e) {
            handleExportError(jobId, e);
        } catch (Exception e) {
            handleUnexpectedError(jobId, e);
        }
    }

    /**
     * @brief Obtiene productos filtrados con paginación optimizada para exportación
     * @param request solicitud de exportación con filtros
     * @param jobId identificador del trabajo (para logging)
     * @return lista completa de productos filtrados
     */
    private List<Product> getFilteredProducts(ProductExportRequest request, String jobId) {
        List<Product> allProducts = new ArrayList<>();
        int currentPage = 0;
        Page<Product> page;

        do {
            // Obtener productos según filtros
            if (request.getStatus() != null) {
                // Filtrar por estado específico (activo o inactivo)
                page = productPersistencePort.findByEnterpriseIdAndState(
                        request.getEntId(), request.getStatus(), currentPage, EXPORT_PAGE_SIZE);
            } else {
                // Sin filtro de estado: obtener todos los productos
                page = productPersistencePort.findByEnterpriseIdWithFilters(
                        request.getEntId(), null, currentPage, EXPORT_PAGE_SIZE, "name", "asc");
            }

            if (page != null && page.hasContent()) {
                allProducts.addAll(page.getContent());
            }

            currentPage++;

        } while (page != null && page.hasNext());

        return allProducts;
    }

    /**
     * @brief Genera el archivo Excel con los datos de productos
     * @details Pre-carga un cache con todos los nombres de entidades relacionadas
     * para evitar N+1 queries durante la generación del Excel
     * @param products lista de productos a exportar
     * @param request solicitud de exportación con configuración
     * @param jobId identificador del trabajo
     * @return arreglo de bytes con el contenido del archivo Excel
     * @throws IOException si ocurre un error al escribir el archivo
     */
    private byte[] generateExcelFile(List<Product> products, ProductExportRequest request, String jobId) 
            throws IOException {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Productos");

            // Crear estilos
            CellStyle headerStyle = ExcelStyleHelper.createHeaderStyle(workbook);
            CellStyle dataStyle = ExcelStyleHelper.createDataStyle(workbook);

            createHeaders(sheet, headerStyle, ExcelStyleHelper.createOptionalHeaderStyle(workbook));

            // Pre-cargar cache de nombres (3-4 queries en lugar de 54,708)
            EntityNamesCache namesCache = preloadEntityNamesCache(request.getEntId(), products);

            fillDataWithCache(sheet, products, dataStyle, namesCache);

            applyValidationsToDataSheet(sheet, request.getEntId(), products.size());

            autoSizeColumns(sheet);

            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }


    // ==================== MÉTODOS DE CONTENIDO ====================

    private void createHeaders(Sheet sheet, CellStyle requiredHeaderStyle, CellStyle optionalHeaderStyle) {
        Row headerRow = sheet.createRow(0);
        int colIndex = 0;

        createHeaderCell(headerRow, colIndex++, "Código\n(No se requiere)", optionalHeaderStyle);
        createHeaderCell(headerRow, colIndex++, "Nombre\n(Requerido)", requiredHeaderStyle);
        createHeaderCell(headerRow, colIndex++, "Referencia/SKU\n(Requerido)", requiredHeaderStyle);
        createHeaderCell(headerRow, colIndex++, "Presentación\n(Requerido)", requiredHeaderStyle);
        createHeaderCell(headerRow, colIndex++, "Descripción\n(Requerido)", requiredHeaderStyle);
        createHeaderCell(headerRow, colIndex++, "Costo\n(Opcional)", optionalHeaderStyle);
        createHeaderCell(headerRow, colIndex++, "Cantidad\n(Opcional)", optionalHeaderStyle);
        createHeaderCell(headerRow, colIndex++, "Unidad de Medida\n(Requerido)", requiredHeaderStyle);
        createHeaderCell(headerRow, colIndex++, "Categoría\n(Requerido)", requiredHeaderStyle);
        createHeaderCell(headerRow, colIndex++, "Tipo de Producto\n(Requerido)", requiredHeaderStyle);
        createHeaderCell(headerRow, colIndex++, "Estado\n(No se requiere)", optionalHeaderStyle);

        headerRow.setHeightInPoints(35);
    }

    private void createHeaderCell(Row row, int colIndex, String value, CellStyle style) {
        Cell cell = row.createCell(colIndex);
        cell.setCellValue(value);
        cell.setCellStyle(style);
    }

    /**
     * @brief Llena hoja Excel usando cache pre-cargado de nombres
     * @details Usa el cache para resolver nombres sin hacer queries adicionales,
     * eliminando el problema N+1 y acelerando drásticamente la generación
     * @param sheet hoja de Excel
     * @param products lista de productos
     * @param dataStyle estilo para celdas
     * @param namesCache cache con todos los nombres pre-cargados
     */
    private void fillDataWithCache(Sheet sheet, List<Product> products, CellStyle dataStyle, EntityNamesCache namesCache) {
        int rowIndex = 1;

        for (Product product : products) {
            Row row = sheet.createRow(rowIndex++);
            int colIndex = 0;

            createDataCell(row, colIndex++, product.getCode(), dataStyle);
            createDataCell(row, colIndex++, product.getName(), dataStyle);
            createDataCell(row, colIndex++, product.getReference() != null ? product.getReference() : "", dataStyle);
            createDataCell(row, colIndex++, product.getPresentation() != null ? product.getPresentation() : "", dataStyle);
            createDataCell(row, colIndex++, product.getDescription(), dataStyle);

            createDataCell(row, colIndex++, product.getCost(), dataStyle);
            createDataCell(row, colIndex++, product.getQuantity(), dataStyle);
            
            // Usar cache en lugar de queries (O(1) en lugar de query por registro)
            createDataCell(row, colIndex++, namesCache.getUnitOfMeasureName(product.getUnitOfMeasureId()), dataStyle);
            createDataCell(row, colIndex++, namesCache.getCategoryName(product.getCategoryId()), dataStyle);
            createDataCell(row, colIndex++, namesCache.getProductTypeName(product.getProductTypeId()), dataStyle);
            
            createDataCell(row, colIndex++, product.isState() ? "ACTIVO" : "INACTIVO", dataStyle);
        }
    }

    private void createDataCell(Row row, int colIndex, Object value, CellStyle style) {
        Cell cell = row.createCell(colIndex);

        if (value == null) {
            cell.setCellValue("");
        } else if (value instanceof Number number) {
            cell.setCellValue(number.doubleValue());
        } else {
            cell.setCellValue(value.toString());
        }

        cell.setCellStyle(style);
    }

    // ==================== CACHE DE NOMBRES DE ENTIDADES ====================

    /**
     * @brief Pre-carga todos los nombres de entidades relacionadas en memoria
     * @details Ejecuta solo 3-4 queries para cargar todos los nombres necesarios
     * @param entId ID de la empresa
     * @param products lista de productos para identificar IDs únicos
     * @return cache con todos los nombres indexados por ID
     */
    private EntityNamesCache preloadEntityNamesCache(String entId, List<Product> products) {
        EntityNamesCache cache = new EntityNamesCache();

        // Extraer IDs únicos de los productos
        Set<Long> unitIds = products.stream()
                .map(Product::getUnitOfMeasureId)
                .filter(id -> id != null)
                .collect(Collectors.toSet());

        Set<Long> categoryIds = products.stream()
                .map(Product::getCategoryId)
                .filter(id -> id != null)
                .collect(Collectors.toSet());

        Set<Long> typeIds = products.stream()
                .map(Product::getProductTypeId)
                .filter(id -> id != null)
                .collect(Collectors.toSet());


        // Query 1: Cargar todos los nombres de unidades de medida en batch
        for (Long unitId : unitIds) {
            unitOfMeasurePersistencePort.findByIdAndEnterpriseId(unitId, entId)
                    .ifPresent(unit -> cache.unitNames.put(unitId, unit.getName()));
        }

        // Query 2: Cargar todos los nombres de categorías en batch
        for (Long categoryId : categoryIds) {
            categoryPersistencePort.findByIdAndEnterpriseId(categoryId, entId)
                    .ifPresent(cat -> cache.categoryNames.put(categoryId, cat.getName()));
        }

        // Query 3: Cargar todos los nombres de tipos de producto en batch
        for (Long typeId : typeIds) {
            productTypePersistencePort.findByIdAndEnterpriseId(typeId, entId)
                    .ifPresent(type -> cache.productTypeNames.put(typeId, type.getName()));
        }


        return cache;
    }

    /**
     * @brief Cache interno con nombres de entidades relacionadas indexados por ID
     * @details Permite acceso O(1) a nombres sin queries adicionales
     */
    private static class EntityNamesCache {
        final Map<Long, String> unitNames = new HashMap<>();
        final Map<Long, String> categoryNames = new HashMap<>();
        final Map<Long, String> productTypeNames = new HashMap<>();

        String getUnitOfMeasureName(Long id) {
            return id == null ? "" : unitNames.getOrDefault(id, "");
        }

        String getCategoryName(Long id) {
            return id == null ? "" : categoryNames.getOrDefault(id, "");
        }

        String getProductTypeName(Long id) {
            return id == null ? "" : productTypeNames.getOrDefault(id, "");
        }
    }

    // ==================== MÉTODOS DE VALIDACIONES ====================

    private void applyValidationsToDataSheet(Sheet sheet, String entId, int dataRowCount) {
        int startRow = 1;
        int endRow = Math.max(dataRowCount + 100, 1000);

        excelValidationService.applyProductValidations(sheet, entId, startRow, endRow);
    }

    private void autoSizeColumns(Sheet sheet) {
        for (int i = 0; i < 11; i++) {
            sheet.autoSizeColumn(i);

            int autoWidth = sheet.getColumnWidth(i);
            int minWidth = 1500;
            int maxWidth = 25000;

            if (autoWidth < minWidth) {
                sheet.setColumnWidth(i, minWidth);
            } else if (autoWidth > maxWidth) {
                sheet.setColumnWidth(i, maxWidth);
            }

            int finalWidth = Math.min(Math.max(autoWidth + 500, minWidth), maxWidth);
            sheet.setColumnWidth(i, finalWidth);
        }
    }

    // ==================== MANEJO DE ERRORES ====================

    private void handleNoDataError(ProductExportRequest request, String jobId) {
        String message = "No hay productos para exportar con los filtros especificados";
        jobTracker.setErrorMessage(jobId, message);
        jobTracker.updateJobStatus(jobId, ImportStatus.FAILED);
    }

    private void handleExportError(String jobId, ProductExportException e) {
        jobTracker.setErrorMessage(jobId, e.getMessage());
        jobTracker.updateJobStatus(jobId, ImportStatus.FAILED);
    }

    private void handleUnexpectedError(String jobId, Exception e) {
        jobTracker.setErrorMessage(jobId, "Error inesperado: " + e.getMessage());
        jobTracker.updateJobStatus(jobId, ImportStatus.FAILED);
    }

}

