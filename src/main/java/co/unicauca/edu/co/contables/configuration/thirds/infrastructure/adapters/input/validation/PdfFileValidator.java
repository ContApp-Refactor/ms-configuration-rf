package co.unicauca.edu.co.contables.configuration.thirds.infrastructure.adapters.input.validation;

import co.unicauca.edu.co.contables.configuration.thirds.domain.exceptions.third.FileSizeExceededException;
import co.unicauca.edu.co.contables.configuration.thirds.domain.exceptions.third.FileValidationException;
import co.unicauca.edu.co.contables.configuration.thirds.infrastructure.config.FileUploadProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * @brief Validador específico para archivos PDF con validación de MIME type
 */
@Component
@Qualifier("pdfFileValidator")
@RequiredArgsConstructor
public class PdfFileValidator implements FileValidator {

    private final FileUploadProperties fileProperties;

    @Override
    public void validate(MultipartFile file) {
        validateNotNull(file);
        validateNotEmpty(file);
        validateSize(file);
        validateExtension(file);
        validateMimeType(file);
    }

    /**
     * @brief Valida que el archivo no sea null
     * @param file archivo a validar
     */
    private void validateNotNull(MultipartFile file) {
        if (file == null) {
            throw FileValidationException.forNullFile();
        }
    }

    /**
     * @brief Valida que el archivo no esté vacío
     * @param file archivo a validar
     */
    private void validateNotEmpty(MultipartFile file) {
        if (file.isEmpty() || file.getSize() == 0) {
            throw FileValidationException.forEmptyFile(file.getOriginalFilename());
        }
    }

    /**
     * @brief Valida que el tamaño del archivo no exceda el límite configurado
     * @param file archivo a validar
     */
    private void validateSize(MultipartFile file) {
        if (file.getSize() > fileProperties.getMaxSize()) {
            throw new FileSizeExceededException(fileProperties.getMaxSize());
        }
    }

    /**
     * @brief Valida que la extensión del archivo sea válida para PDF
     * @param file archivo a validar
     */
    private void validateExtension(MultipartFile file) {
        String filename = file.getOriginalFilename();
        if (filename == null) {
            throw FileValidationException.forInvalidExtension("archivo sin nombre", getSupportedExtensions());
        }

        String lowerFilename = filename.toLowerCase();
        boolean validExtension = false;

        for (String ext : getSupportedExtensions()) {
            if (lowerFilename.endsWith(ext)) {
                validExtension = true;
                break;
            }
        }

        if (!validExtension) {
            throw FileValidationException.forInvalidExtension(filename, getSupportedExtensions());
        }
    }

    /**
     * @brief Valida que el tipo MIME del archivo sea válido para PDF
     * @param file archivo a validar
     */
    private void validateMimeType(MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType == null) {
            throw FileValidationException.forInvalidMimeType(
                file.getOriginalFilename(),
                "null",
                getSupportedMimeTypes()
            );
        }

        boolean validMimeType = false;
        for (String mimeType : getSupportedMimeTypes()) {
            if (contentType.equals(mimeType)) {
                validMimeType = true;
                break;
            }
        }

        if (!validMimeType) {
            throw FileValidationException.forInvalidMimeType(
                file.getOriginalFilename(),
                contentType,
                getSupportedMimeTypes()
            );
        }
    }

    @Override
    public String[] getSupportedMimeTypes() {
        List<String> mimeTypes = fileProperties.getAllowedMimeTypes().get("pdf");
        return mimeTypes != null ? mimeTypes.toArray(new String[0]) : new String[0];
    }

    @Override
    public String[] getSupportedExtensions() {
        List<String> extensions = fileProperties.getAllowedExtensions().get("pdf");
        return extensions != null ? extensions.toArray(new String[0]) : new String[0];
    }
}
