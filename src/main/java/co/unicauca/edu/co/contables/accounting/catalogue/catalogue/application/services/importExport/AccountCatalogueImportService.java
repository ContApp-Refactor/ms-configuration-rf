package co.unicauca.edu.co.contables.accounting.catalogue.catalogue.application.services.importExport;

import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.application.input.IAccountCatalogueImportInputPort;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.application.services.validation.AccountCatalogueFileValidationService;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.domain.models.ImportJobStatus;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.infraestructure.adapters.input.rest.dto.request.AccountCatalogueImportRequest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Optional;

/**
 * @brief Servicio para importación asíncrona de cuentas contables desde Excel
 *
 * Coordina el inicio de importaciones asíncronas de catálogo de cuentas y
 * gestiona el seguimiento del estado de los trabajos de importación.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AccountCatalogueImportService implements IAccountCatalogueImportInputPort {

        private final AccountCatalogueImportJobTracker jobTracker;
        private final AccountCatalogueAsyncImportProcessor asyncImportProcessor;
        private final AccountCatalogueFileValidationService fileValidationService;

        /**
         * @brief Inicia la importación asíncrona de cuentas contables
         * @details Valida el archivo, crea un trabajo de importación y delega el procesamiento
         * al procesador asíncrono en un hilo separado
         * @param request solicitud con archivo Excel y configuración
         * @return jobId único para rastrear el estado de la importación
         */
        @Override
        public String importAccountCatalogueAsync(AccountCatalogueImportRequest request) {
                String entId = request.getEntId();
                String fileName = request.getExcelFile().getOriginalFilename();

                try {
                        // Validar archivo antes de crear el trabajo
                        fileValidationService.validate(request.getExcelFile());

                        // Crear trabajo de importación
                        String jobId = jobTracker.createJob(entId, fileName);

                        // Convertir archivo a bytes para procesamiento asíncrono
                        byte[] fileBytes = request.getExcelFile().getBytes();

                        // Iniciar procesamiento asíncrono
                        asyncImportProcessor.processImportAsync(request, jobId, fileBytes);

                        return jobId;

                } catch (IOException e) {
                        throw new RuntimeException("Error al leer el archivo: " + e.getMessage(), e);
                }
        }

        /**
         * @brief Obtiene el estado actual de una importación asíncrona
         * @param jobId identificador del trabajo de importación
         * @return Optional con el estado del trabajo si existe
         */
        @Override
        public Optional<ImportJobStatus> getImportStatus(String jobId) {
                return jobTracker.getJobStatus(jobId);
        }
}
