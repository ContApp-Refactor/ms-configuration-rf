package co.unicauca.edu.co.contables.configuration.thirds.application.service.importExport;

import co.unicauca.edu.co.contables.configuration.thirds.domain.enums.ExportableField;
import co.unicauca.edu.co.contables.configuration.thirds.domain.model.ExportConfiguration;
import co.unicauca.edu.co.contables.configuration.thirds.domain.model.Third;
import co.unicauca.edu.co.contables.configuration.thirds.domain.model.ThirdType;
import co.unicauca.edu.co.contables.configuration.thirds.infrastructure.adapters.input.rest.dto.request.ThirdExportRequest;
import co.unicauca.edu.co.contables.configuration.thirds.infrastructure.utils.ExcelStyleHelper;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @brief Clase auxiliar para escribir datos en hojas de Excel
 *
 * Maneja la creación de encabezados, escritura de datos y aplicación
 * de validaciones en archivos Excel de exportación de terceros.
 */
public class ExcelDataWriter {

    private final ExcelStyleHelper styleHelper;

    public ExcelDataWriter(ExcelStyleHelper styleHelper) {
        this.styleHelper = styleHelper;
    }

    /**
     * @brief Crea los encabezados de las columnas en la hoja de Excel
     */
    public void createHeaders(Sheet sheet, ThirdExportRequest request) {
        Row headerRow = sheet.createRow(0);
        int colIndex = 0;
        ExportConfiguration config = request.getExportConfiguration();

        // Encabezados básicos requeridos
        createHeaderCell(headerRow, colIndex++, "Tipo Identificación\n(Requerido)", styleHelper.getHeaderStyle());
        createHeaderCell(headerRow, colIndex++, "Número Identificación\n(Requerido)", styleHelper.getHeaderStyle());
        createHeaderCell(headerRow, colIndex++, "Dígito Verificación\n(Requerido para persona jurídica con NIT)", styleHelper.getHeaderStyle());
        createHeaderCell(headerRow, colIndex++, "Tipo Persona\n(Requerido)", styleHelper.getHeaderStyle());
        createHeaderCell(headerRow, colIndex++, "Nombres\n(Requerido para persona natural)", styleHelper.getHeaderStyle());
        createHeaderCell(headerRow, colIndex++, "Apellidos\n(Requerido para persona natural)", styleHelper.getHeaderStyle());
        createHeaderCell(headerRow, colIndex++, "Razón Social\n(Requerido para persona jurídica)", styleHelper.getHeaderStyle());
        
        // Género - OPCIONAL
        if (config.includes(ExportableField.GENDER)) {
            createHeaderCell(headerRow, colIndex++, "Género\n(Opcional)", styleHelper.getOptionalHeaderStyle());
        }
        
        // Estado
        createHeaderCell(headerRow, colIndex++, "Estado\n(No se requiere)", styleHelper.getOptionalHeaderStyle());

        // Tipos de tercero - SIEMPRE incluido
        createHeaderCell(headerRow, colIndex++, "Tipos de Tercero\n(Requerido)", styleHelper.getHeaderStyle());

        // Campos geográficos - OPCIONALES
        if (config.includes(ExportableField.COUNTRY)) {
            createHeaderCell(headerRow, colIndex++, "País\n(Opcional)", styleHelper.getOptionalHeaderStyle());
        }
        if (config.includes(ExportableField.STATE)) {
            createHeaderCell(headerRow, colIndex++, "Departamento\n(Opcional)", styleHelper.getOptionalHeaderStyle());
        }
        if (config.includes(ExportableField.CITY)) {
            createHeaderCell(headerRow, colIndex++, "Ciudad\n(Opcional)", styleHelper.getOptionalHeaderStyle());
        }

        // Campos de contacto requeridos
        createHeaderCell(headerRow, colIndex++, "Dirección\n(Requerido)", styleHelper.getHeaderStyle());
        createHeaderCell(headerRow, colIndex++, "Teléfono\n(Requerido)", styleHelper.getHeaderStyle());
        createHeaderCell(headerRow, colIndex++, "Email\n(Requerido)", styleHelper.getHeaderStyle());
        
        // Ajustar altura de la fila de encabezados
        headerRow.setHeightInPoints(35);
    }

    /**
     * @brief Llena la hoja de Excel con los datos de terceros
     */
    public void fillData(Sheet sheet, List<Third> thirds, ThirdExportRequest request) {
        int rowIndex = 1;
        ExportConfiguration config = request.getExportConfiguration();

        for (Third third : thirds) {
            Row row = sheet.createRow(rowIndex++);
            int colIndex = 0;

            // Datos básicos requeridos
            createDataCell(row, colIndex++, third.getTypeId() != null ? third.getTypeId().getTypeId() : "");
            createDataCell(row, colIndex++, third.getIdNumber());
            createDataCell(row, colIndex++, third.getVerificationNumber());
            createDataCell(row, colIndex++, third.getPersonType() != null ? third.getPersonType().getCode() : "");
            createDataCell(row, colIndex++, third.getNames());
            createDataCell(row, colIndex++, third.getLastNames());
            createDataCell(row, colIndex++, third.getSocialReason());
            
            // Género - Campo opcional
            if (config.includes(ExportableField.GENDER)) {
                createDataCell(row, colIndex++, third.getGender() != null ? third.getGender().toString() : "");
            }
            
            // Estado
            createDataCell(row, colIndex++, third.getState() != null && third.getState() ? "ACTIVO" : "INACTIVO");

            // Tipos de tercero
            String types = third.getThirdTypes().stream()
                    .map(ThirdType::getThirdTypeName)
                    .collect(Collectors.joining(", "));
            createDataCell(row, colIndex++, types);

            // Campos geográficos
            if (config.includes(ExportableField.COUNTRY)) {
                createDataCell(row, colIndex++, 
                    third.getCountry() != null ? third.getCountry().getCountryName() : "");
            }
            if (config.includes(ExportableField.STATE)) {
                createDataCell(row, colIndex++, 
                    third.getProvince() != null ? third.getProvince().getStateName() : "");
            }
            if (config.includes(ExportableField.CITY)) {
                createDataCell(row, colIndex++, 
                    third.getCity() != null ? third.getCity().getCityName() : "");
            }

            // Campos de contacto
            createDataCell(row, colIndex++, third.getAddress());
            createDataCell(row, colIndex++, third.getPhoneNumber());
            createDataCell(row, colIndex++, third.getEmail());
        }
    }

    /**
     * @brief Aplica validaciones de datos a la hoja
     */
    public void applyValidations(Sheet sheet, ThirdExportRequest request, int dataRowCount, 
                                 ExcelValidationService validationService) {
        int startRow = 1;
        int endRow = Math.max(dataRowCount + 100, 1000);

        int genderColumnIndex = getGenderColumnIndex(request);
        int stateColumnIndex = getStateColumnIndex(request);
        int typesColumnIndex = getTypesColumnIndex(request);
        int[] geoColumns = getGeographyColumnIndexes(request);

        validationService.applyThirdValidationsWithTypes(sheet, request.getEntId(), startRow, endRow,
                genderColumnIndex, stateColumnIndex, typesColumnIndex);

        validationService.applyGeographyValidations(sheet, startRow, endRow,
                geoColumns[0], geoColumns[1], geoColumns[2]);
    }

    /**
     * @brief Ajusta automáticamente el ancho de las columnas
     */
    public void autoSizeColumns(Sheet sheet, ThirdExportRequest request) {
        int columnCount = getColumnCount(request);
        for (int i = 0; i < columnCount; i++) {
            sheet.autoSizeColumn(i);
            int autoWidth = sheet.getColumnWidth(i);
            int minWidth = 1500;
            int maxWidth = 25000;
            int finalWidth = Math.min(Math.max(autoWidth + 500, minWidth), maxWidth);
            sheet.setColumnWidth(i, finalWidth);
        }
    }

    private void createHeaderCell(Row row, int colIndex, String value, CellStyle style) {
        Cell cell = row.createCell(colIndex);
        cell.setCellValue(value);
        cell.setCellStyle(style);
    }

    private void createDataCell(Row row, int colIndex, Object value) {
        Cell cell = row.createCell(colIndex);
        if (value == null) {
            cell.setCellValue("");
        } else if (value instanceof Number) {
            cell.setCellValue(((Number) value).doubleValue());
        } else {
            cell.setCellValue(value.toString());
        }
        cell.setCellStyle(styleHelper.getDataStyle());
    }

    private int getGenderColumnIndex(ThirdExportRequest request) {
        ExportConfiguration config = request.getExportConfiguration();
        return config.includes(ExportableField.GENDER) ? 7 : -1;
    }

    private int getStateColumnIndex(ThirdExportRequest request) {
        ExportConfiguration config = request.getExportConfiguration();
        return config.includes(ExportableField.GENDER) ? 8 : 7;
    }

    private int getTypesColumnIndex(ThirdExportRequest request) {
        ExportConfiguration config = request.getExportConfiguration();
        return config.includes(ExportableField.GENDER) ? 9 : 8;
    }

    private int[] getGeographyColumnIndexes(ThirdExportRequest request) {
        ExportConfiguration config = request.getExportConfiguration();
        int baseIndex = getTypesColumnIndex(request) + 1;
        
        int countryIndex = -1;
        int stateIndex = -1;
        int cityIndex = -1;
        
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

    private int getColumnCount(ThirdExportRequest request) {
        ExportConfiguration config = request.getExportConfiguration();
        int count = 8; // Básicos
        
        if (config.includes(ExportableField.GENDER)) {
            count++;
        }
        count++; // Tipos de tercero
        
        if (config.includes(ExportableField.COUNTRY)) {
            count++;
        }
        if (config.includes(ExportableField.STATE)) {
            count++;
        }
        if (config.includes(ExportableField.CITY)) {
            count++;
        }
        
        count += 3; // Contacto
        return count;
    }
}

