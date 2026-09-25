package co.unicauca.edu.co.contables.configuration.enterprise.application.ports.services;

import co.unicauca.edu.co.contables.configuration.enterprise.application.ports.input.PdfRUTContent;
import co.unicauca.edu.co.contables.configuration.enterprise.application.ports.output.PdfRUTContentOutput;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;

/**
 * Servicio que implementa las operaciones de extracción de contenido de archivos RUT en PDF.
 * Gestiona la lógica de negocio para procesar y extraer información relevante
 * de documentos RUT en formato PDF.
 *
 * @author CONTAPP
 * @version 1.0
 * @since 1.0.0
 */
@Service("enterprisePdfRUTService")
public class PdfRUTService {
    
    /**
     * Extrae el contenido de un archivo PDF RUT y lo procesa para obtener información estructurada.
     *
     * @param request Objeto PdfRUTContent que contiene el archivo PDF a procesar
     * @return PdfRUTContentOutput Objeto con la información extraída y procesada del PDF
     * @throws IOException Si ocurre un error durante la lectura o procesamiento del archivo
     */
    public PdfRUTContentOutput extractContent(PdfRUTContent request) throws IOException {
        File tempFile = File.createTempFile("upload", ".pdf");

        // Transferir el archivo recibido a un archivo temporal
        request.getFile().transferTo(tempFile);

        try (PDDocument document = PDDocument.load(tempFile,"1")) {
            PDFTextStripper pdfStripper = new PDFTextStripper();
            pdfStripper.setStartPage(1);
            pdfStripper.setEndPage(1);  
            String content = pdfStripper.getText(document);
            String typeId = "";
            int idPerson = 0;
            String razonSocial="";
            String names="";
            String lastNames="";
            String [] ubication = null;
            String pais = "";
            String departamento = "";
            String ciudad = "";
            String direccion = "";
            String correo = "";
            long cell = 0;
            try{
                String[] extractedLines = extractAfterClasificacion(content);
                String[] extractedUbication = extractUbicationThird(content);
                String[] personaJuridica;
                String[] personaNatural;
                String typePerson;
                String[] aux = separateNumbersAndText(extractedLines[3]);
                typePerson = aux[0];
                if(extractedLines[3].contains("Persona jurídica")){
                    //Extraer la primera parte de identificacion para persona juridica 
                    typeId = "NIT";
                    personaJuridica = separateNumbersAndText(cleanString(extractedLines[2]));
                    idPerson = Integer.parseInt(String.valueOf(personaJuridica[0]).length() > 0 ? String.valueOf(personaJuridica[0]).substring(0, String.valueOf(personaJuridica[0]).length() - 1) : String.valueOf(personaJuridica[0]));
                    razonSocial = extractedLines[5];
                }else{
                    //Extraer la primera parte de identificacion para persona natural
                    String [] aux1 = separateNumbersAndText(cleanString(extractedLines[3]));
                    typeId = aux1[2];
                    typeId = typeId.trim();
                    personaNatural = separateNumbersAndText(cleanString(extractedLines[2]));
                    idPerson = Integer.parseInt(String.valueOf(personaNatural[0]).length() > 0 ? String.valueOf(personaNatural[0]).substring(0, String.valueOf(personaNatural[0]).length() - 1) : String.valueOf(personaNatural[0]));
                    String[] datos = new String[4];
                    datos = splitBySpaceAndUpperCase(extractedLines[5]);
                    lastNames = datos[0]+" "+ datos[1];
                    names = datos[2]+" "+ datos[3];
                }
                ubication =  separateNumbersAndText(cleanString(extractedUbication[0]));
                pais = cleanString(ubication[0]);
                departamento = cleanString(ubication[2]);
                ciudad = cleanString(ubication[4]);
                direccion = extractedUbication[1];
                correo = extractedUbication[2];
                String [] contact = separateAndJoinNumbers(extractedUbication[3]);
                cell = Long.parseLong(contact[1]);
                System.out.println("Tipo de persona: "+typePerson);
                System.out.println("Tipo de identifiacion: "+ typeId);
                System.out.println("Numero Identificacion: "+ idPerson);
                System.out.println("Razon social: "+razonSocial);
                System.out.println("Apellidos: "+lastNames);
                System.out.println("Nombres: "+names);
                System.out.println("Pais: "+pais);
                System.out.println("Departamento: "+departamento);
                System.out.println("Ciudad: "+ciudad);
                System.out.println("Direccion: "+direccion);
                System.out.println("Correo: "+correo);
                System.out.println("Telefono: "+cell);
                String infoThird = typePerson+";"+typeId+";"+idPerson+";"+razonSocial+";"+lastNames+";"+names+";"+pais+";"+departamento+";"+ciudad+";"+direccion+";"+correo+";"+cell;
                return new PdfRUTContentOutput(infoThird);
            }catch(Exception e){
                System.err.println("Error processing PDF content: " + e.getMessage());
                e.printStackTrace();
            }
            String infoThird = ""+";"+typeId+";"+idPerson+";"+razonSocial+";"+lastNames+";"+names+";"+pais+";"+departamento+";"+ciudad+";"+direccion+";"+correo+";"+cell;
            return new PdfRUTContentOutput(infoThird);
        } finally {
            tempFile.delete();
        }
    }

    /**
     * Extrae las líneas de texto después de la sección "CLASIFICACIÓN".
     *
     * @param content Contenido completo del PDF
     * @return String[] Arreglo con las líneas de texto extraídas
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
     * Extrae la información de ubicación después de la palabra "COLOMBIA".
     *
     * @param content Contenido completo del PDF
     * @return String[] Arreglo con la información de ubicación
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
     * Limpia una cadena de texto eliminando espacios y saltos de línea innecesarios.
     *
     * @param input Texto a limpiar
     * @return String Texto limpio
     */
    public static String cleanString(String input) {
        String cleaned = input.replaceAll("\\s*\\n\\s*", "\n") // Limpiar saltos de linea con espacios
                              .replaceAll("\\s{2,}", " ")    // Reemplaza multiples espacios por uno
                              .trim();                       // Elimina espacios al principio y al final
        // Elimina espacios entre varios numeros consecutivos
        cleaned = cleaned.replaceAll("(\\d)\\s+(?=\\d)", "$1");
        return cleaned;
    }

    /**
     * Separa números y texto en un arreglo.
     *
     * @param input Texto a separar
     * @return String[] Arreglo con números y texto separados
     */
    public static String[] separateNumbersAndText(String input) {
        // Regex para separar cuando hay un cambio de numeros a letras
        return input.split("(?<=\\d)(?=\\D)|(?<=\\D)(?=\\d)");
    }

    /**
     * Separa y une números en un texto.
     *
     * @param input Texto con números a procesar
     * @return String[] Arreglo con los números procesados
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
     * Divide una cadena por espacios y letras mayúsculas.
     *
     * @param input Texto a dividir
     * @return String[] Arreglo con el texto dividido
     */
    public static String[] splitBySpaceAndUpperCase(String input) {
        return input.split("(?<=\\s)(?=[A-Z])");
    }
}