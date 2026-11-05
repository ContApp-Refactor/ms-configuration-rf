package co.unicauca.edu.co.contables.configuration.thirds.application.service.importExport;

import co.unicauca.edu.co.contables.configuration.thirds.application.ports.input.ExportThirdUseCase;
import co.unicauca.edu.co.contables.configuration.thirds.application.ports.output.ThirdOutputPort;
import co.unicauca.edu.co.contables.configuration.thirds.domain.enums.ExportableField;
import co.unicauca.edu.co.contables.configuration.thirds.domain.exceptions.third.ThirdExportException;
import co.unicauca.edu.co.contables.configuration.thirds.domain.exceptions.third.ThirdsErrorCode;
import co.unicauca.edu.co.contables.configuration.thirds.domain.model.ExportConfiguration;
import co.unicauca.edu.co.contables.configuration.thirds.domain.model.ExportJobStatus;
import co.unicauca.edu.co.contables.configuration.thirds.domain.model.Third;
import co.unicauca.edu.co.contables.configuration.thirds.domain.model.ThirdType;
import co.unicauca.edu.co.contables.configuration.thirds.infrastructure.adapters.input.rest.dto.request.ThirdExportRequest;
import co.unicauca.edu.co.contables.configuration.thirds.infrastructure.utils.ExcelFileNameGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @brief Servicio para exportar terceros en formato Excel
 *
 * Gestiona la exportación completa de terceros con filtros, paginación
 * automática y generación de archivos Excel con validaciones integradas.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ExportThirdService implements ExportThirdUseCase {

    private final ThirdOutputPort thirdOutputPort;
    private final ExcelValidationService excelValidationService;
    private final ExportJobTracker exportJobTracker;
    private final AsyncExportProcessor asyncExportProcessor;
    private final ExcelFileNameGenerator fileNameGenerator;
  
    private static final int EXPORT_PAGE_SIZE = 5000;

    /**
     * @brief Obtiene terceros filtrados con paginación optimizada para exportación
     *
     * Utiliza paginación automática para exportar todos los registros sin límite,
     * optimizando el uso de memoria mediante procesamiento por lotes.
     * 
     * @param request solicitud de exportación con filtros
     * @return lista completa de terceros filtrados con todas sus relaciones pre-cargadas
     */
    private List<Third> getFilteredThirds(ThirdExportRequest request) {
        List<Third> allThirds = new ArrayList<>();
        int currentPage = 0;
        Page<Third> page;
                
        do {
            // Crear pageable para la página actual
            Pageable pageable = PageRequest.of(currentPage, EXPORT_PAGE_SIZE);
            
            // Obtener página según filtros usando métodos optimizados para exportación
            if (request.getStatus() != null) {
                page = thirdOutputPort.getAllThirdsByStateForExport(request.getEntId(), request.getStatus(), pageable);
            } else {
                page = thirdOutputPort.getAllThirdsForExport(request.getEntId(), pageable);
            }
            
            // Agregar contenido de esta página a la lista total
            if (page != null && page.hasContent()) {
                allThirds.addAll(page.getContent());
            }
            
            currentPage++;
            
        } while (page != null && page.hasNext());
        
        return allThirds;
    }

    /**
     * @brief Crea estilo para encabezados principales de Excel
     * @param workbook libro de Excel donde crear el estilo
     * @return estilo configurado para encabezados principales
     */
    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setWrapText(true);
        return style;
    }

    /**
     * @brief Crea estilo para encabezados de columnas opcionales
     * @param workbook libro de Excel donde crear el estilo
     * @return estilo configurado para encabezados opcionales
     */
    private CellStyle createOptionalHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.BLACK.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setWrapText(true);
        return style;
    }

    /**
     * @brief Crea estilo para celdas de datos en Excel
     * @param workbook libro de Excel donde crear el estilo
     * @return estilo configurado para celdas de datos
     */
    private CellStyle createDataStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    /**
     * @brief Crea estilo para celdas de fechas en Excel
     * @param workbook libro de Excel donde crear el estilo
     * @return estilo configurado para celdas de fechas
     */
    private CellStyle createDateStyle(Workbook workbook) {
        CellStyle style = createDataStyle(workbook);
        CreationHelper createHelper = workbook.getCreationHelper();
        style.setDataFormat(createHelper.createDataFormat().getFormat("dd/mm/yyyy"));
        return style;
    }

    /**
     * @brief Crea los encabezados de las columnas en la hoja de Excel
     * @param sheet hoja de Excel donde crear los encabezados
     * @param headerStyle estilo para encabezados principales
     * @param request solicitud de exportación con configuración
     */
    private void createHeaders(Sheet sheet, CellStyle headerStyle, ThirdExportRequest request) {
        CellStyle optionalHeaderStyle = createOptionalHeaderStyle(sheet.getWorkbook());
        Row headerRow = sheet.createRow(0);
        int colIndex = 0;
        ExportConfiguration config = request.getExportConfiguration();

        // Encabezados básicos requeridos con indicativos de requerimiento
        createHeaderCell(headerRow, colIndex++, "Tipo Identificación\n(Requerido)", headerStyle);
        createHeaderCell(headerRow, colIndex++, "Número Identificación\n(Requerido)", headerStyle);
        createHeaderCell(headerRow, colIndex++, "Dígito Verificación\n(Requerido para persona jurídica con NIT)", headerStyle);
        createHeaderCell(headerRow, colIndex++, "Tipo Persona\n(Requerido)", headerStyle);
        createHeaderCell(headerRow, colIndex++, "Nombres\n(Requerido para persona natural)", headerStyle);
        createHeaderCell(headerRow, colIndex++, "Apellidos\n(Requerido para persona natural)", headerStyle);
        createHeaderCell(headerRow, colIndex++, "Razón Social\n(Requerido para persona jurídica)", headerStyle);
        
        // Género - OPCIONAL (solo si está configurado)
        if (config.includes(ExportableField.GENDER)) {
            createHeaderCell(headerRow, colIndex++, "Género\n(Opcional)", optionalHeaderStyle);
        }
        
        // Estado - Con estilo gris por ser campo no editable
        createHeaderCell(headerRow, colIndex++, "Estado\n(No se requiere)", optionalHeaderStyle);

        // Tipos de tercero - SIEMPRE incluido (no opcional)
        createHeaderCell(headerRow, colIndex++, "Tipos de Tercero\n(Requerido)", headerStyle);

        // Campos geográficos - OPCIONALES (solo si están configurados)
        if (config.includes(ExportableField.COUNTRY)) {
            createHeaderCell(headerRow, colIndex++, "País\n(Opcional)", optionalHeaderStyle);
        }
        if (config.includes(ExportableField.STATE)) {
            createHeaderCell(headerRow, colIndex++, "Departamento\n(Opcional)", optionalHeaderStyle);
        }
        if (config.includes(ExportableField.CITY)) {
            createHeaderCell(headerRow, colIndex++, "Ciudad\n(Opcional)", optionalHeaderStyle);
        }

        // Campos de contacto requeridos
        createHeaderCell(headerRow, colIndex++, "Dirección\n(Requerido)", headerStyle);
        createHeaderCell(headerRow, colIndex++, "Teléfono\n(Requerido)", headerStyle);
        createHeaderCell(headerRow, colIndex++, "Email\n(Requerido)", headerStyle);
        
        // Ajustar altura de la fila de encabezados para mostrar múltiples líneas
        headerRow.setHeightInPoints(35);
    }

    /**
     * @brief Crea una celda de encabezado con estilo específico
     * @param row fila donde crear la celda
     * @param colIndex índice de columna
     * @param value valor a colocar en la celda
     * @param style estilo a aplicar a la celda
     */
    private void createHeaderCell(Row row, int colIndex, String value, CellStyle style) {
        Cell cell = row.createCell(colIndex);
        cell.setCellValue(value);
        cell.setCellStyle(style);
    }

    /**
     * @brief Llena la hoja de Excel con los datos de terceros
     * @param sheet hoja de Excel donde colocar los datos
     * @param thirds lista de terceros a exportar
     * @param dataStyle estilo para celdas de datos
     * @param dateStyle estilo para celdas de fechas
     * @param request solicitud de exportación con configuración
     */
    private void fillData(Sheet sheet, List<Third> thirds, CellStyle dataStyle, CellStyle dateStyle,
            ThirdExportRequest request) {
        int rowIndex = 1;
        ExportConfiguration config = request.getExportConfiguration();

        for (Third third : thirds) {
            Row row = sheet.createRow(rowIndex++);
            int colIndex = 0;

            // Datos básicos requeridos
            createDataCell(row, colIndex++, third.getTypeId() != null ? third.getTypeId().getTypeId() : "",
                    dataStyle);
            createDataCell(row, colIndex++, third.getIdNumber(), dataStyle);
            createDataCell(row, colIndex++, third.getVerificationNumber(), dataStyle);
            createDataCell(row, colIndex++, third.getPersonType() != null ? third.getPersonType().getCode() : "",
                    dataStyle);
            createDataCell(row, colIndex++, third.getNames(), dataStyle);
            createDataCell(row, colIndex++, third.getLastNames(), dataStyle);
            createDataCell(row, colIndex++, third.getSocialReason(), dataStyle);
            
            // Género - Campo opcional (solo si está configurado)
            if (config.includes(ExportableField.GENDER)) {
                createDataCell(row, colIndex++, third.getGender() != null ? third.getGender().toString() : "", dataStyle);
            }
            
            // Estado
            createDataCell(row, colIndex++, third.getState() != null && third.getState() ? "ACTIVO" : "INACTIVO",
                    dataStyle);

            // Tipos de tercero - SIEMPRE incluido
            String types = third.getThirdTypes().stream()
                    .map(ThirdType::getThirdTypeName)
                    .collect(Collectors.joining(", "));
            createDataCell(row, colIndex++, types, dataStyle);

            // Campos geográficos - Solo si están configurados
            if (config.includes(ExportableField.COUNTRY)) {
                createDataCell(row, colIndex++, 
                    third.getCountry() != null ? third.getCountry().getCountryName() : "", 
                    dataStyle);
            }
            
            if (config.includes(ExportableField.STATE)) {
                createDataCell(row, colIndex++, 
                    third.getProvince() != null ? third.getProvince().getStateName() : "", 
                    dataStyle);
            }
            
            if (config.includes(ExportableField.CITY)) {
                createDataCell(row, colIndex++, 
                    third.getCity() != null ? third.getCity().getCityName() : "", 
                    dataStyle);
            }

            // Campos de contacto requeridos
            createDataCell(row, colIndex++, third.getAddress(), dataStyle);
            createDataCell(row, colIndex++, third.getPhoneNumber(), dataStyle);
            createDataCell(row, colIndex++, third.getEmail(), dataStyle);
        }
    }

    /**
     * @brief Crea una celda de datos con valor y estilo específico
     * @param row fila donde crear la celda
     * @param colIndex índice de columna
     * @param value valor a colocar en la celda (maneja null, Number y String)
     * @param style estilo a aplicar a la celda
     */
    private void createDataCell(Row row, int colIndex, Object value, CellStyle style) {
        Cell cell = row.createCell(colIndex);

        if (value == null) {
            cell.setCellValue("");
        } else if (value instanceof Number) {
            cell.setCellValue(((Number) value).doubleValue());
        } else {
            cell.setCellValue(value.toString());
        }

        cell.setCellStyle(style);
    }

    /**
     * @brief Ajusta automáticamente el ancho de las columnas de Excel
     * @param sheet hoja de Excel cuyas columnas ajustar
     * @param columnCount número total de columnas a ajustar
     */
    private void autoSizeColumns(Sheet sheet, int columnCount) {
        for (int i = 0; i < columnCount; i++) {
            // Primero aplicar el auto-sizing basado en el contenido
            sheet.autoSizeColumn(i);

            // Obtener el ancho calculado automáticamente
            int autoWidth = sheet.getColumnWidth(i);

            // Establecer límites razonables
            int minWidth = 1500; // Ancho mínimo más pequeño
            int maxWidth = 25000; // Ancho máximo más generoso

            // Aplicar los límites
            if (autoWidth < minWidth) {
                sheet.setColumnWidth(i, minWidth);
            } else if (autoWidth > maxWidth) {
                sheet.setColumnWidth(i, maxWidth);
            }

            // Agregar un pequeño padding al ancho calculado para mejor legibilidad
            int finalWidth = Math.min(Math.max(autoWidth + 500, minWidth), maxWidth);
            sheet.setColumnWidth(i, finalWidth);
        }
    }

    /**
     * @brief Calcula el número total de columnas según la configuración de exportación
     * @param request solicitud de exportación con configuración de campos
     * @return número total de columnas que tendrá la exportación
     */
    private int getColumnCount(ThirdExportRequest request) {
        ExportConfiguration config = request.getExportConfiguration();
        
        // Columnas siempre presentes: 
        // Tipo ID, Número ID, Dígito Verif, Tipo Persona, Nombres, Apellidos, Razón Social, Estado
        int count = 8;

        // Género - OPCIONAL (según configuración)
        if (config.includes(ExportableField.GENDER)) {
            count++;
        }

        // Tipos de tercero - SIEMPRE incluido
        count++;

        // Geografía - OPCIONAL (según configuración)
        if (config.includes(ExportableField.COUNTRY)) {
            count++;
        }
        if (config.includes(ExportableField.STATE)) {
            count++;
        }
        if (config.includes(ExportableField.CITY)) {
            count++;
        }

        // Contacto - SIEMPRE presente (Dirección, Teléfono, Email)
        count += 3;

        return count;
    }

    
    @Override
    public Resource exportThirdTemplateWithValidations(String entId) {

        try {
            byte[] templateData = generateTemplateWithValidations(entId);
            return new ByteArrayResource(templateData);

        } catch (Exception e) {
            throw new ThirdExportException(ThirdsErrorCode.THIRD_EXPORT_ERROR,
                    "Error al generar plantilla", e);
        }
    }

 
    @Override
    public Resource exportThirdsWithValidations(ThirdExportRequest exportRequest) {

        try {
            // Obtener datos según filtros
            List<Third> thirds = getFilteredThirds(exportRequest);

            // Validar que existan terceros para exportar
            if (thirds == null || thirds.isEmpty()) {
                // Determinar el código de error específico según el filtro de estado
                ThirdsErrorCode errorCode;
                if (exportRequest.getStatus() != null) {
                    errorCode = exportRequest.getStatus() 
                        ? ThirdsErrorCode.THIRD_EXPORT_NO_ACTIVE_DATA 
                        : ThirdsErrorCode.THIRD_EXPORT_NO_INACTIVE_DATA;
                } else {
                    errorCode = ThirdsErrorCode.THIRD_EXPORT_NO_DATA;
                }
                throw new ThirdExportException(errorCode);
            }

            // Generar archivo Excel con datos y validaciones
            byte[] excelData = generateExcelFileWithValidations(thirds, exportRequest);

            return new ByteArrayResource(excelData);

        } catch (ThirdExportException e) {
            // Re-lanzar excepciones de negocio sin modificar
            throw e;
        } catch (Exception e) {
            throw new ThirdExportException(ThirdsErrorCode.THIRD_EXPORT_ERROR,
                    "Error al generar archivo de exportación", e);
        }
    }

    /**
     * @brief Inicia exportación asíncrona de terceros
     * @param exportRequest solicitud de exportación con filtros
     * @return jobId único para consultar el estado
     */
    @Override
    public String exportThirdsAsync(ThirdExportRequest exportRequest) {
        try {
            // Generar nombre de archivo descriptivo
            String fileName = generateFileName(exportRequest);
            
            // Crear job de exportación y obtener ID (síncrono, retorna inmediatamente)
            String jobId = exportJobTracker.createJob(exportRequest.getEntId(), fileName);

            // Ejecutar exportación de forma asíncrona usando servicio separado
            asyncExportProcessor.processExportAsync(exportRequest, jobId);

            return jobId;

        } catch (Exception e) {
            throw new ThirdExportException(ThirdsErrorCode.THIRD_EXPORT_ERROR,
                    "Error al iniciar la exportación: " + e.getMessage(), e);
        }
    }

    /**
     * @brief Obtiene el estado de un job de exportación
     * @param jobId identificador único del job
     * @return estado del job si existe
     */
    @Override
    public Optional<ExportJobStatus> getExportStatus(String jobId) {
        return exportJobTracker.getJobStatus(jobId);
    }

    /**
     * @brief Genera un nombre de archivo descriptivo para la exportación
     * @param exportRequest solicitud de exportación
     * @return nombre de archivo generado
     */
    private String generateFileName(ThirdExportRequest exportRequest) {
        return fileNameGenerator.generateExportFileName(
                exportRequest.getEntId(), 
                exportRequest.getCompanyName(), 
                exportRequest.getStatus()
        );
    }

    /**
     * @brief Genera archivo Excel de plantilla con validaciones integradas
     * @param entId identificador de la entidad para datos de referencia
     * @return arreglo de bytes con el contenido del archivo Excel
     * @throws IOException si ocurre un error al escribir el archivo
     */
    private byte[] generateTemplateWithValidations(String entId) throws IOException {
        try (Workbook workbook = new XSSFWorkbook();
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Plantilla_Terceros");

            // Crear estilos
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle templateStyle = createTemplateStyle(workbook);

            // Crear encabezados con todos los campos opcionales habilitados para plantilla
            ThirdExportRequest templateRequest = ThirdExportRequest.builder()
                    .entId(entId)
                    .optionalFields(Set.of(
                        ExportableField.GENDER,
                        ExportableField.COUNTRY,
                        ExportableField.STATE,
                        ExportableField.CITY
                    ))
                    .build();

            createHeaders(sheet, headerStyle, templateRequest);

            // Crear filas de ejemplo con estilos
            createTemplateRows(sheet, templateStyle, templateRequest, 10); // 10 filas de ejemplo

            // Crear hoja de datos de referencia
            excelValidationService.createReferenceDataSheet(workbook, entId);

            // Aplicar validaciones de datos
            applyValidationsToTemplate(sheet, entId, templateRequest);

            // Ajustar ancho de columnas
            autoSizeColumns(sheet, getColumnCount(templateRequest));

            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }

    /**
     * @brief Crea estilo para celdas de plantilla en Excel
     * @param workbook libro de Excel donde crear el estilo
     * @return estilo configurado para celdas de plantilla
     */
    private CellStyle createTemplateStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setItalic(true);
        style.setFont(font);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    /**
     * @brief Crea filas de ejemplo en la plantilla de Excel
     * @param sheet hoja de Excel donde crear las filas
     * @param templateStyle estilo para las celdas de plantilla
     * @param request solicitud de exportación con configuración
     * @param numberOfRows número de filas de ejemplo a crear
     */
    private void createTemplateRows(Sheet sheet, CellStyle templateStyle, ThirdExportRequest request,
            int numberOfRows) {
        ExportConfiguration config = request.getExportConfiguration();
        
        for (int i = 1; i <= numberOfRows; i++) {
            Row row = sheet.createRow(i);
            int colIndex = 0;

            if (i == 1) {
                // Primera fila con indicadores (estilo normal)
                createTemplateCell(row, colIndex++, "Seleccionar...", templateStyle);
                createTemplateCell(row, colIndex++, "123456789", templateStyle);
                createTemplateCell(row, colIndex++, "1", templateStyle);
                createTemplateCell(row, colIndex++, "Seleccionar...", templateStyle);
                createTemplateCell(row, colIndex++, "Nombres", templateStyle);
                createTemplateCell(row, colIndex++, "Apellidos", templateStyle);
                createTemplateCell(row, colIndex++, "Razón Social", templateStyle);
                
                // Género - OPCIONAL (solo si está configurado)
                if (config.includes(ExportableField.GENDER)) {
                    createTemplateCell(row, colIndex++, "Seleccionar...", templateStyle);
                }
                
                createTemplateCell(row, colIndex++, "ACTIVO", templateStyle); // Estado

                // Tipos de tercero - SIEMPRE incluido
                createTemplateCell(row, colIndex++, "Cliente, Proveedor", templateStyle);

                // Geografía - OPCIONAL (solo si está configurada)
                if (config.includes(ExportableField.COUNTRY)) {
                    createTemplateCell(row, colIndex++, "Seleccionar...", templateStyle);
                }
                if (config.includes(ExportableField.STATE)) {
                    createTemplateCell(row, colIndex++, "Seleccionar...", templateStyle);
                }
                if (config.includes(ExportableField.CITY)) {
                    createTemplateCell(row, colIndex++, "Seleccionar...", templateStyle);
                }

                createTemplateCell(row, colIndex++, "Dirección ejemplo", templateStyle);
                createTemplateCell(row, colIndex++, "3001234567", templateStyle);
                createTemplateCell(row, colIndex++, "ejemplo@correo.com", templateStyle);
            } else {
                // Filas adicionales vacías con el mismo estilo
                createEmptyTemplateRow(row, request, templateStyle);
            }
        }
    }

    /**
     * @brief Crea una fila vacía de plantilla con todas las columnas
     * @param row fila de Excel a llenar
     * @param request solicitud de exportación con configuración
     * @param templateStyle estilo para las celdas de plantilla
     */
    private void createEmptyTemplateRow(Row row, ThirdExportRequest request, CellStyle templateStyle) {
        int totalColumns = getColumnCount(request);
        for (int i = 0; i < totalColumns; i++) {
            createTemplateCell(row, i, "", templateStyle);
        }
    }

    /**
     * @brief Crea una celda de plantilla con valor y estilo específico
     * @param row fila donde crear la celda
     * @param colIndex índice de columna
     * @param value valor a colocar en la celda
     * @param style estilo a aplicar a la celda
     */
    private void createTemplateCell(Row row, int colIndex, String value, CellStyle style) {
        Cell cell = row.createCell(colIndex);
        cell.setCellValue(value);
        cell.setCellStyle(style);
    }

    /**
     * @brief Aplica validaciones de datos a una hoja de plantilla
     * @param sheet hoja de Excel donde aplicar validaciones
     * @param entId identificador de la entidad
     * @param request solicitud de exportación con configuración
     */
    private void applyValidationsToTemplate(Sheet sheet, String entId, ThirdExportRequest request) {
        int startRow = 1; // Después del encabezado
        int endRow = 1000; // Permitir muchas filas para la plantilla

        // Calcular índices dinámicos de columnas
        int genderColumnIndex = getGenderColumnIndex(request);
        int stateColumnIndex = getStateColumnIndex(request);
        int typesColumnIndex = getTypesColumnIndex(request);
        int[] geoColumns = getGeographyColumnIndexes(request);

        // Aplicar validaciones con índices dinámicos
        excelValidationService.applyThirdValidationsWithTypes(sheet, entId, startRow, endRow,
                genderColumnIndex, stateColumnIndex, typesColumnIndex);

        // Aplicar validaciones geográficas
        excelValidationService.applyGeographyValidations(sheet, startRow, endRow,
                geoColumns[0], geoColumns[1], geoColumns[2]);
    }

    /**
     * @brief Calcula el índice de la columna Género
     * @param request solicitud de exportación con configuración
     * @return índice de columna o -1 si no está incluido
     */
    private int getGenderColumnIndex(ThirdExportRequest request) {
        ExportConfiguration config = request.getExportConfiguration();
        // Género está en la columna 7 si está incluido
        return config.includes(ExportableField.GENDER) ? 7 : -1;
    }

    /**
     * @brief Calcula el índice de la columna Estado
     * @param request solicitud de exportación con configuración
     * @return índice de columna (dinámico según si género está incluido)
     */
    private int getStateColumnIndex(ThirdExportRequest request) {
        ExportConfiguration config = request.getExportConfiguration();
        // Estado está en columna 7 si género NO está incluido, o en columna 8 si sí está
        return config.includes(ExportableField.GENDER) ? 8 : 7;
    }

    /**
     * @brief Calcula el índice de la columna Tipos de Tercero
     * @param request solicitud de exportación con configuración
     * @return índice de columna (dinámico según si género está incluido)
     */
    private int getTypesColumnIndex(ThirdExportRequest request) {
        ExportConfiguration config = request.getExportConfiguration();
        // Tipos de tercero aparece después de Estado
        // Columnas básicas: 0-6 (7 columnas) + Género (opcional) + Estado = 8 o 9
        return config.includes(ExportableField.GENDER) ? 9 : 8;
    }

    /**
     * @brief Calcula los índices de las columnas geográficas
     * @param request solicitud de exportación con configuración
     * @return arreglo con índices de columnas [país, departamento, ciudad]
     */
    private int[] getGeographyColumnIndexes(ThirdExportRequest request) {
        ExportConfiguration config = request.getExportConfiguration();
        
        // Comenzar después de tipos de tercero
        int baseIndex = getTypesColumnIndex(request) + 1;
        
        int countryIndex = -1;
        int stateIndex = -1;
        int cityIndex = -1;
        
        // Calcular índices dinámicamente según campos incluidos
        if (config.includes(ExportableField.COUNTRY)) {
            countryIndex = baseIndex++;
        }
        if (config.includes(ExportableField.STATE)) {
            stateIndex = baseIndex++;
        }
        if (config.includes(ExportableField.CITY)) {
            cityIndex = baseIndex;
        }
        
        return new int[] { countryIndex, stateIndex, cityIndex };
    }

    /**
     * @brief Genera archivo Excel con datos reales y validaciones aplicadas
     * @param thirds lista de terceros a exportar
     * @param request solicitud de exportación con configuración
     * @return arreglo de bytes con el contenido del archivo Excel
     * @throws IOException si ocurre un error al escribir el archivo
     */
    private byte[] generateExcelFileWithValidations(List<Third> thirds, ThirdExportRequest request) throws IOException {
        try (Workbook workbook = new XSSFWorkbook();
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Terceros");

            // Crear estilos (reutilizando métodos existentes)
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dataStyle = createDataStyle(workbook);
            CellStyle dateStyle = createDateStyle(workbook);

            // Crear encabezados (reutilizando método existente)
            createHeaders(sheet, headerStyle, request);

            fillData(sheet, thirds, dataStyle, dateStyle, request);

            // Crear hoja de datos de referencia para validaciones
            excelValidationService.createReferenceDataSheet(workbook, request.getEntId());

            // Aplicar validaciones de datos (reutilizando método existente)
            applyValidationsToDataSheet(sheet, request.getEntId(), request, thirds.size());

            // Ajustar ancho de columnas (reutilizando método existente)
            autoSizeColumns(sheet, getColumnCount(request));

            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }

    /**
     * @brief Aplica validaciones de datos a una hoja con datos existentes
     * @param sheet hoja de Excel donde aplicar validaciones
     * @param entId identificador de la entidad
     * @param request solicitud de exportación con configuración
     * @param dataRowCount número de filas con datos existentes
     */
    private void applyValidationsToDataSheet(Sheet sheet, String entId, ThirdExportRequest request, int dataRowCount) {
        int startRow = 1; // Después del encabezado
        int endRow = Math.max(dataRowCount + 100, 1000); // Datos existentes + filas adicionales para edición

        // Calcular índices dinámicos de columnas
        int genderColumnIndex = getGenderColumnIndex(request);
        int stateColumnIndex = getStateColumnIndex(request);
        int typesColumnIndex = getTypesColumnIndex(request);
        int[] geoColumns = getGeographyColumnIndexes(request);

        // Aplicar validaciones con índices dinámicos
        excelValidationService.applyThirdValidationsWithTypes(sheet, entId, startRow, endRow,
                genderColumnIndex, stateColumnIndex, typesColumnIndex);

        // Aplicar validaciones geográficas
        excelValidationService.applyGeographyValidations(sheet, startRow, endRow,
                geoColumns[0], geoColumns[1], geoColumns[2]);
    }
}
