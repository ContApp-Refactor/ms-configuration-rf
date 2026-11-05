package co.unicauca.edu.co.contables.configuration.thirds.application.service.importExport;

import co.unicauca.edu.co.contables.configuration.thirds.application.ports.output.ThirdOutputPort;
import co.unicauca.edu.co.contables.configuration.thirds.domain.enums.ImportStatus;
import co.unicauca.edu.co.contables.configuration.thirds.domain.model.Third;
import co.unicauca.edu.co.contables.configuration.thirds.infrastructure.adapters.input.rest.dto.request.ThirdExportRequest;
import co.unicauca.edu.co.contables.configuration.thirds.infrastructure.utils.ExcelStyleHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @brief Servicio para procesar exportaciones de terceros de forma asíncrona
 *
 * Este servicio ejecuta la exportación en un hilo separado para no bloquear
 * la petición HTTP, permitiendo exportaciones de grandes volúmenes de datos.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AsyncExportProcessor {

    private final ThirdOutputPort thirdOutputPort;
    private final ExportJobTracker jobTracker;
    private final ExcelValidationService excelValidationService;
    
    private static final int EXPORT_PAGE_SIZE = 5000;

    /**
     * @brief Procesa la exportación de forma asíncrona
     * @param exportRequest solicitud de exportación con filtros
     * @param jobId identificador del trabajo
     */
    @Async
    public void processExportAsync(ThirdExportRequest exportRequest, String jobId) {
        int totalRecords = 0;

        try {
            jobTracker.updateJobStatus(jobId, ImportStatus.PROCESSING);
            jobTracker.updateProgress(jobId, 10);

            // FASE 1: Obtener datos filtrados con paginación
            List<Third> thirds = getFilteredThirds(exportRequest, jobId);

            totalRecords = thirds.size();
            jobTracker.updateTotalRecords(jobId, totalRecords);
            jobTracker.updateProgress(jobId, 50);

            // Validar que existan datos
            if (thirds == null || thirds.isEmpty()) {
                handleNoDataError(exportRequest, jobId);
                return;
            }

            // FASE 2: Generar archivo Excel
            byte[] excelData = generateExcelFile(thirds, exportRequest, jobId);

            jobTracker.updateProgress(jobId, 90);

            // FASE 3: Almacenar archivo en memoria
            jobTracker.setFileData(jobId, excelData);

            // Completar
            jobTracker.updateJobStatus(jobId, ImportStatus.COMPLETED);
            jobTracker.updateProgress(jobId, 100);

        } catch (Exception e) {
            handleAsyncError(jobId, e);
        }
    }
    

    /**
     * @brief Obtiene terceros filtrados con paginación optimizada para exportación
     * @details Utiliza métodos optimizados que eliminan el problema N+1 queries mediante
     * carga batch de todas las relaciones. Reduce drásticamente el tiempo de obtención de datos.
     * @param request solicitud de exportación con filtros
     * @param jobId identificador del trabajo (para logging)
     * @return lista completa de terceros filtrados con todas sus relaciones pre-cargadas
     */
    private List<Third> getFilteredThirds(ThirdExportRequest request, String jobId) {
        List<Third> allThirds = new ArrayList<>();
        int currentPage = 0;
        Page<Third> page;

        do {
            Pageable pageable = PageRequest.of(currentPage, EXPORT_PAGE_SIZE);
            
            // Usar métodos optimizados específicos para exportación
            if (request.getStatus() != null) {
                page = thirdOutputPort.getAllThirdsByStateForExport(request.getEntId(), request.getStatus(), pageable);
            } else {
                page = thirdOutputPort.getAllThirdsForExport(request.getEntId(), pageable);
            }
            
            if (page != null && page.hasContent()) {
                allThirds.addAll(page.getContent());
            }
            
            currentPage++;
            
        } while (page != null && page.hasNext());
        
        return allThirds;
    }

    /**
     * @brief Genera el archivo Excel con los datos de terceros
     * @param thirds lista de terceros a exportar
     * @param request solicitud de exportación con configuración
     * @param jobId identificador del trabajo
     * @return arreglo de bytes con el contenido del archivo Excel
     * @throws IOException si ocurre un error al escribir el archivo
     */
    private byte[] generateExcelFile(List<Third> thirds, ThirdExportRequest request, String jobId) throws IOException {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Terceros");

            ExcelStyleHelper styleHelper = new ExcelStyleHelper(workbook);
            ExcelDataWriter excelDataWriter = new ExcelDataWriter(styleHelper);

            excelDataWriter.createHeaders(sheet, request);

            excelDataWriter.fillData(sheet, thirds, request);

            excelValidationService.createReferenceDataSheet(workbook, request.getEntId());

            excelDataWriter.applyValidations(sheet, request, thirds.size(), excelValidationService);

            excelDataWriter.autoSizeColumns(sheet, request);

            workbook.write(outputStream);

            return outputStream.toByteArray();
        }
    }

    /**
     * @brief Maneja el error cuando no hay datos para exportar
     * @param exportRequest solicitud de exportación
     * @param jobId identificador del trabajo
     */
    private void handleNoDataError(ThirdExportRequest exportRequest, String jobId) {
        String errorMessage;
        
        if (exportRequest.getStatus() != null) {
            errorMessage = "No hay terceros " + (exportRequest.getStatus() ? "activos" : "inactivos") + " para exportar";
        } else {
            errorMessage = "No hay terceros para exportar";
        }
        
        jobTracker.updateJobStatus(jobId, ImportStatus.FAILED);
        jobTracker.setErrorMessage(jobId, errorMessage);
        jobTracker.updateProgress(jobId, 100);
    }

    /**
     * @brief Maneja errores durante la exportación asíncrona
     * @param jobId identificador del trabajo
     * @param e excepción ocurrida
     */
    private void handleAsyncError(String jobId, Exception e) {
        String errorMessage = e.getMessage() != null ? e.getMessage() : "Error desconocido durante la exportación";
        jobTracker.updateJobStatus(jobId, ImportStatus.FAILED);
        jobTracker.setErrorMessage(jobId, errorMessage);
        jobTracker.updateProgress(jobId, 100);
    }
}

