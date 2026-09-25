package co.unicauca.edu.co.contables.configuration.thirds.application.service.importExport;

import co.unicauca.edu.co.contables.configuration.thirds.application.ports.output.PdfRUTContentOutput;
import co.unicauca.edu.co.contables.configuration.thirds.domain.exceptions.third.PdfRutInvalidFormatException;
import co.unicauca.edu.co.contables.configuration.thirds.domain.exceptions.third.ThirdInvalidDataException;
import co.unicauca.edu.co.contables.configuration.thirds.domain.model.PdfRUTContent;
import co.unicauca.edu.co.contables.configuration.thirds.infrastructure.adapters.input.validation.FileValidator;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @brief Servicio para extracción de contenido de PDFs de RUT
 *
 * Proporciona funcionalidad para extraer y procesar contenido textual
 * de archivos PDF correspondientes a Registros Únicos Tributarios (RUT).
 */
@Service("thirdsPdfRUTService")
public class PdfRUTService {

    private final FileValidator fileValidator;

    public PdfRUTService(@Qualifier("pdfFileValidator") FileValidator fileValidator) {
        this.fileValidator = fileValidator;
    }

    /**
     * @brief Extrae el contenido de un archivo PDF de RUT
     * @param request objeto con la información del archivo PDF
     * @return objeto con la información extraída del archivo PDF
     * @throws IOException si ocurre un error al cargar el archivo PDF
     * @throws ThirdInvalidDataException si el request es null o el archivo es inválido
     * @throws PdfRutInvalidFormatException si el PDF no tiene el formato válido de RUT
     */
    public PdfRUTContentOutput extractContent(PdfRUTContent request) throws IOException {
        if (request == null) {
            throw new ThirdInvalidDataException("El request no puede ser null");
        }

        if (request.getFile() == null || request.getFile().isEmpty()) {
            throw new ThirdInvalidDataException("El archivo PDF no puede ser null o vacío");
        }

        fileValidator.validate(request.getFile());
        
        // Crear archivo temporal en directorio seguro
        Path tempDir = Files.createTempDirectory("pdf-processing-");
        File tempFile = tempDir.resolve("upload.pdf").toFile();

        // Transferir el archivo recibido a un archivo temporal
        request.getFile().transferTo(tempFile);

        try (PDDocument document = PDDocument.load(tempFile, "1")) {
            PDFTextStripper pdfStripper = new PDFTextStripper();
            pdfStripper.setStartPage(1);
            pdfStripper.setEndPage(1);
            String content = pdfStripper.getText(document);
            String typeId = "";
            int idPerson = 0;
            String razonSocial = "";
            String names = "";
            String lastNames = "";
            String[] ubication = null;
            String pais = "";
            String departamento = "";
            String ciudad = "";
            String direccion = "";
            String correo = "";
            long cell = 0;
            try {
                String[] extractedLines = extractAfterClasificacion(content);
                String[] extractedUbication = extractUbicationThird(content);
                String[] personaJuridica;
                String[] personaNatural;
                String typePerson;
                String[] aux = separateNumbersAndText(extractedLines[3]);
                typePerson = aux[0];
                if (extractedLines[3].contains("Persona jurídica")) {
                    // Extraer la primera parte de identificacion para persona juridica
                    typeId = "NIT";
                    personaJuridica = separateNumbersAndText(cleanString(extractedLines[2]));
                    idPerson = Integer.parseInt(String.valueOf(personaJuridica[0]).length() > 0
                            ? String.valueOf(personaJuridica[0]).substring(0,
                                    String.valueOf(personaJuridica[0]).length() - 1)
                            : String.valueOf(personaJuridica[0]));
                    razonSocial = extractedLines[5];
                } else {
                    // Extraer la primera parte de identificacion para persona natural
                    String[] aux1 = separateNumbersAndText(cleanString(extractedLines[3]));
                    typeId = aux1[2];
                    typeId = typeId.trim();
                    personaNatural = separateNumbersAndText(cleanString(extractedLines[2]));
                    idPerson = Integer.parseInt(String.valueOf(personaNatural[0]).length() > 0
                            ? String.valueOf(personaNatural[0]).substring(0,
                                    String.valueOf(personaNatural[0]).length() - 1)
                            : String.valueOf(personaNatural[0]));
                    String[] datos = new String[4];
                    datos = splitBySpaceAndUpperCase(extractedLines[5]);
                    lastNames = datos[0] + " " + datos[1];
                    names = datos[2] + " " + datos[3];
                }
                ubication = separateNumbersAndText(cleanString(extractedUbication[0]));
                pais = cleanString(ubication[0]);
                departamento = cleanString(ubication[2]);
                ciudad = cleanString(ubication[4]);
                direccion = extractedUbication[1];
                correo = extractedUbication[2];
                String[] contact = separateAndJoinNumbers(extractedUbication[3]);
                cell = Long.parseLong(contact[1]);
                String infoThird = typePerson + ";" + typeId + ";" + idPerson + ";" + razonSocial + ";" + lastNames
                        + ";" + names + ";" + pais + ";" + departamento + ";" + ciudad + ";" + direccion + ";" + correo
                        + ";" + cell;
                return new PdfRUTContentOutput(infoThird);
            } catch (Exception e) {
                throw new PdfRutInvalidFormatException(
                        "El archivo PDF no tiene el formato válido de RUT de la DIAN o no se pudo procesar correctamente");
            }
        } finally {
            // Limpiar archivo y directorio temporal de forma segura
            if (tempFile != null && tempFile.exists()) {
                try {
                    tempFile.delete();
                } catch (Exception e) {
                    // Log error but don't throw to avoid masking original exception
                }
            }
            // Limpiar directorio temporal
            if (tempDir != null) {
                try {
                    Files.deleteIfExists(tempDir);
                } catch (Exception e) {
                    // Log error but don't throw to avoid masking original exception
                }
            }
        }
    }

    /**
     * @brief Extrae el contenido después de la palabra "CLASIFICACIÓN"
     * @param content Contenido del archivo PDF
     * @return Arreglo con las líneas después de la palabra "CLASIFICACIÓN"
     */
    private String[] extractAfterClasificacion(String content) {
        int index = content.indexOf("CLASIFICACIÓN");
        if (index != -1) {
            String result = content.substring(index + "CLASIFICACIÓN".length()).trim();
            String[] lines = result.split("\\r?\\n");
            return lines;
        }
        return new String[0];
    }

    /**
     * @brief Extrae la información de ubicación de un tercero desde el PDF de RUT
     * @param content Contenido del archivo PDF
     * @return Arreglo con las líneas de la ubicación de un tercero
     */
    private String[] extractUbicationThird(String content) {
        int index = content.lastIndexOf("COLOMBIA");
        if (index != -1) {

            String result = content.substring(index).trim();
            String[] lines = result.split("\\r?\\n");
            return lines;
        }
        return new String[0];
    }

    /**
     * @brief Limpia el contenido de un string
     * @param input String a limpiar
     * @return String limpio
     */
    public static String cleanString(String input) {
        String cleaned = input.replaceAll("[ \\t]*\\n[ \\t]*", "\n") 
                .replaceAll("\\s{2,}", " ") 
                .trim();         
        cleaned = cleaned.replaceAll("(\\d)\\s+(?=\\d)", "$1");
        return cleaned;
    }

    /**
     * @brief Separa números y texto en un string
     * @param input String con números y texto
     * @return Arreglo con los números y texto separados
     */
    public static String[] separateNumbersAndText(String input) {
        // Regex para separar cuando hay un cambio de numeros a letras
        return input.split("(?<=\\d)(?=\\D)|(?<=\\D)(?=\\d)");
    }

    /**
     * @brief Separa y une números en un string
     * @param input String con números
     * @return Arreglo con los números separados
     */
    public static String[] separateAndJoinNumbers(String input) {
        // Reemplaza multiples espacios con un separador especial
        String modifiedInput = input.replaceAll("\\s{2,}", ",");
        // Reemplaza espacios simples entre numeros
        modifiedInput = modifiedInput.replaceAll("\\s+", "");
        // Divide en un vector usando la coma como delimitador
        return modifiedInput.split(",");
    }

    /**
     * @brief Divide un string por espacios y letras mayúsculas
     * @param input String a dividir
     * @return Arreglo con las partes del string
     */
    public static String[] splitBySpaceAndUpperCase(String input) {
        return input.split("(?<=\\s)(?=[A-Z])");
    }
}