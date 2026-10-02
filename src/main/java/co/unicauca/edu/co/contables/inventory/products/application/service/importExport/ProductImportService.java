package co.unicauca.edu.co.contables.inventory.products.application.service.importExport;

import co.unicauca.edu.co.contables.inventory.products.application.ports.input.IProductImportUseCase;
import co.unicauca.edu.co.contables.inventory.products.domain.model.ImportJobStatus;
import co.unicauca.edu.co.contables.inventory.products.infraestructure.input.rest.dto.request.ProductImportRequest;
import co.unicauca.edu.co.contables.inventory.products.infraestructure.input.validation.ExcelFileValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Optional;

/**
 * @brief Servicio principal de importación asíncrona de productos desde Excel
 *
 * Orquesta el inicio del proceso de importación asíncrona: validación del archivo,
 * conversión a bytes y delegación a ProductAsyncImportProcessor para procesamiento
 * en thread separado.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProductImportService implements IProductImportUseCase {

    private final ExcelFileValidator excelFileValidator;
    private final ProductAsyncImportProcessor asyncImportProcessor;
    private final ProductImportJobTracker jobTracker;

    /**
     * @brief Inicia importación asíncrona de productos
     * @details Valida el archivo, lo convierte a bytes y delega el procesamiento
     * a ProductAsyncImportProcessor que se ejecuta en un thread separado.
     * Retorna inmediatamente con un jobId para tracking.
     * @param request solicitud de importación con archivo Excel
     * @return jobId único para consultar el estado del proceso
     */
    @Override
    public String importProductsAsync(ProductImportRequest request) {
        String entId = request.getEntId();
        String fileName = request.getExcelFile().getOriginalFilename();

        try {
            // Validación rápida del archivo
            excelFileValidator.validate(request.getExcelFile());

            // Convertir archivo a bytes para procesamiento asíncrono
            // (MultipartFile no es serializable para threads asíncronos)
            byte[] fileBytes = request.getExcelFile().getBytes();

            // Crear job de importación y obtener ID
            String jobId = jobTracker.createJob(entId, fileName);

            // Ejecutar importación de forma asíncrona
            asyncImportProcessor.processImportAsync(fileBytes, entId, fileName, jobId);

            return jobId;

        } catch (IOException e) {
            throw new RuntimeException("Error al procesar el archivo: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new RuntimeException("Error al iniciar la importación: " + e.getMessage(), e);
        }
    }

    /**
     * @brief Obtiene el estado de un job de importación asíncrona
     * @param jobId identificador único del job
     * @return Optional con el estado del job si existe
     */
    @Override
    public Optional<ImportJobStatus> getImportStatus(String jobId) {
        return jobTracker.getJobStatus(jobId);
    }
}