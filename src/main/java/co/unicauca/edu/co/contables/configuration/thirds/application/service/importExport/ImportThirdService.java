package co.unicauca.edu.co.contables.configuration.thirds.application.service.importExport;

import co.unicauca.edu.co.contables.configuration.thirds.application.ports.input.ImportThirdUseCase;
import co.unicauca.edu.co.contables.configuration.thirds.domain.exceptions.third.ThirdImportException;
import co.unicauca.edu.co.contables.configuration.thirds.domain.exceptions.third.ThirdsErrorCode;
import co.unicauca.edu.co.contables.configuration.thirds.domain.model.ImportJobStatus;
import co.unicauca.edu.co.contables.configuration.thirds.infrastructure.adapters.input.rest.dto.request.ThirdImportRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * @brief Servicio principal para importación asíncrona masiva de terceros desde Excel
 *
 * Orquesta el proceso completo de importación asíncrona con tracking de estado,
 * validación por lotes, detección de duplicados y procesamiento transaccional optimizado.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ImportThirdService implements ImportThirdUseCase {

    private final ImportJobTracker jobTracker;
    private final AsyncImportProcessor asyncImportProcessor; 

    
    /**
     * @brief Inicia una importación asíncrona de terceros desde Excel
     * 
     * Este método crea el job de forma síncrona y retorna el jobId inmediatamente.
     * Lee el archivo a bytes antes de lanzar el procesamiento asíncrono para evitar 
     * problemas con el MultipartFile que no está disponible fuera del contexto HTTP.
     * 
     * @param importRequest solicitud de importación con el archivo
     * @return ID único del job de importación iniciado
     */
    @Override
    public String importThirdsFromExcel(ThirdImportRequest importRequest) {
        try {
            // Crear job de importación y obtener ID (síncrono, retorna inmediatamente)
            String jobId = jobTracker.createJob(importRequest.getEntId(), importRequest.getFileName());
            
            // Leer el archivo a bytes ANTES de lanzar el procesamiento asíncrono
            // Esto evita problemas con MultipartFile que solo está disponible durante la petición HTTP
            byte[] fileBytes = importRequest.getExcelFile().getBytes();

            // Ejecutar importación de forma asíncrona usando servicio separado (evita self-invocation)
            asyncImportProcessor.processImportAsync(fileBytes, importRequest.getEntId(),
                    importRequest.getFileName(), jobId);

            return jobId;

        } catch (Exception e) {
            throw new ThirdImportException(ThirdsErrorCode.THIRD_EXPORT_ERROR,
                    "Error al leer el archivo: " + e.getMessage(), e);
        }
    }

    /**
     * @brief Obtiene el estado de un job de importación
     * @param jobId identificador único del job
     * @return estado del job si existe
     */
    @Override
    public Optional<ImportJobStatus> getImportStatus(String jobId) {
        return jobTracker.getJobStatus(jobId);
    }
}
